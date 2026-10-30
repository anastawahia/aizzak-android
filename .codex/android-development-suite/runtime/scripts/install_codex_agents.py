#!/usr/bin/env python3
"""Install the twelve native Codex agents and a self-contained resource snapshot.

Project installation requires --project; personal installation requires --user.
No config.toml, AGENTS.md, auth, model, permission or hook trust settings change.
Managed local edits and unrelated agents are never overwritten. Python 3.10+.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path, PurePosixPath
import stat
import sys
import tempfile

from suite import PLUGIN, SECRET_FILE, SuiteError, read_json

OWNER = 'android-development-suite'
STATE = OWNER + '/installation.json'
RUNTIME = OWNER + '/runtime/'
RESOURCE_DIRS = ('scripts', 'skills', 'references', 'workflows', 'templates',
                 'agents', 'hooks', '.codex-plugin', '.claude-plugin',
                 'codex-skills', 'codex-agents', 'docs')


def load_agent(path):
    """Read our generated three-basic-string TOML subset, also on Python 3.10.

    This is deliberately not a general TOML parser. Generated values use JSON's
    common basic-string escapes, a strict subset accepted by native Codex TOML.
    """
    data = {}
    for line in Path(path).read_text(encoding='utf-8').splitlines():
        if not line.strip() or line.startswith('#'):
            continue
        key, separator, value = line.partition(' = ')
        if not separator or key in data or key not in {'name', 'description', 'developer_instructions'}:
            raise SuiteError(f'Unexpected native agent schema: {path}')
        data[key] = json.loads(value)
    if set(data) != {'name', 'description', 'developer_instructions'} or not all(
            isinstance(value, str) and value.strip() for value in data.values()):
        raise SuiteError(f'Incomplete native agent: {path}')
    if data['name'] != Path(path).stem:
        raise SuiteError(f'Agent name differs from filename: {path}')
    return data


def encode_agent(data):
    return ('\n'.join(f'{key} = {json.dumps(value, ensure_ascii=False)}'
                      for key, value in data.items()) + '\n').encode('utf-8')


def sha(content):
    return hashlib.sha256(content).hexdigest()


def is_link(path):
    return path.is_symlink() or (path.exists() and bool(
        getattr(path.lstat(), 'st_file_attributes', 0) & getattr(stat, 'FILE_ATTRIBUTE_REPARSE_POINT', 1024)))


def safe_path(base, relative, names):
    parts = PurePosixPath(relative).parts
    allowed = (relative == STATE or relative.startswith(RUNTIME) or
               relative in {'agents/' + name + '.toml' for name in names})
    if (not allowed or not parts or PurePosixPath(relative).as_posix() != relative
            or any(part in {'..', '.'} or ':' in part or '\\' in part for part in parts)):
        raise SuiteError('Invalid managed installation path.')
    target = base.joinpath(*parts)
    if not target.resolve().is_relative_to(base.resolve()):
        raise SuiteError('Managed path escapes the Codex directory.')
    for path in [base, *target.relative_to(base).parents]:
        candidate = path if path == base else base / path
        if is_link(candidate):
            raise SuiteError(f'Refusing symlink/junction in installation: {candidate}')
    if is_link(target):
        raise SuiteError(f'Refusing linked installation file: {target}')
    return target


def payload(base, source, names):
    files = {}
    runtime = (base / RUNTIME).resolve().as_posix()
    for name in names:
        agent = load_agent(source / 'codex-agents' / (name + '.toml'))
        if agent['developer_instructions'].count('@@PLUGIN_ROOT@@') != 1:
            raise SuiteError(f'Native agent must contain one resource root token: {name}')
        agent['developer_instructions'] = agent['developer_instructions'].replace('@@PLUGIN_ROOT@@', runtime)
        files['agents/' + name + '.toml'] = encode_agent(agent)
    for directory in RESOURCE_DIRS:
        root = source / directory
        if not root.is_dir() or is_link(root):
            raise SuiteError('Missing or linked runtime resource directory: ' + directory)
        for current, dirs, filenames in os.walk(root, followlinks=False):
            for entry in dirs + filenames:
                if is_link(Path(current) / entry):
                    raise SuiteError('Linked runtime resource.')
            dirs[:] = sorted(d for d in dirs if d not in {'__pycache__', '.checks', '.git'})
            for filename in sorted(filenames):
                file = Path(current) / filename
                if SECRET_FILE.search(filename):
                    raise SuiteError('Credential-like file in runtime resources; review before installation.')
                if file.suffix in {'.pyc', '.pyo'}:
                    continue
                files[RUNTIME + file.relative_to(source).as_posix()] = file.read_bytes()
    for filename in ['LICENSE', 'README.md']:
        file = source / filename
        if file.is_file() and not is_link(file):
            files[RUNTIME + filename] = file.read_bytes()
    return files


def atomic_write(path, content):
    path.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.NamedTemporaryFile('wb', dir=path.parent, delete=False) as out:
        out.write(content)
        temporary = Path(out.name)
    try:
        os.replace(temporary, path)
    finally:
        temporary.unlink(missing_ok=True)


def install(base, source=PLUGIN, uninstall=False, dry_run=False):
    source = Path(source).resolve()
    base = Path(base).absolute()
    names = read_json(source / 'references/catalog.json')['agents']
    state_path = safe_path(base, STATE, names)
    if state_path.exists() and not state_path.is_file():
        raise SuiteError('Installation ownership record is not a regular file.')
    state = read_json(state_path) if state_path.is_file() else None
    if state is not None and (not isinstance(state, dict) or state.get('owner') != OWNER or state.get('schema') != 1
                              or not isinstance(state.get('files'), dict)):
        raise SuiteError('Invalid installation ownership record.')
    previous = state['files'] if state else {}
    # Preflight every owned path, including stale files on upgrade/removal.
    for relative, expected_hash in previous.items():
        file = safe_path(base, relative, names)
        if relative == STATE or not isinstance(expected_hash, str):
            raise SuiteError('Invalid installation inventory.')
        if file.exists() and (not file.is_file() or sha(file.read_bytes()) != expected_hash):
            raise SuiteError(f'Managed file was edited; preserve/reconcile it before retrying: {file}')
    files = {} if uninstall else payload(base, source, names)
    for relative, content in files.items():
        file = safe_path(base, relative, names)
        if file.exists() and relative not in previous:
            raise SuiteError(f'Unmanaged file already exists; no files changed: {file}')
    version = read_json(source / '.codex-plugin/plugin.json')['version']
    result = {'status': 'passed', 'action': 'uninstall' if uninstall else 'install',
              'dry_run': dry_run, 'codex_directory': str(base), 'version': version,
              'agents': [] if uninstall else names, 'runtime': str(base / RUNTIME),
              'files': len(files), 'config_changed': False}
    if dry_run or (uninstall and not state):
        return result
    if not uninstall:
        files[STATE] = (json.dumps({'schema': 1, 'owner': OWNER, 'version': version,
                                   'files': {path: sha(content) for path, content in sorted(files.items())}},
                                  indent=2) + '\n').encode('utf-8')
    touched = set(previous) | set(files) | ({STATE} if state else set())
    backup = {path: (safe_path(base, path, names).read_bytes()
                     if safe_path(base, path, names).is_file() else None) for path in touched}
    try:
        for relative, content in files.items():
            file = safe_path(base, relative, names)
            if backup.get(relative) != content:
                atomic_write(file, content)
        for relative in touched - set(files):
            safe_path(base, relative, names).unlink(missing_ok=True)
    except OSError:
        for relative, content in backup.items():
            file = safe_path(base, relative, names)
            if content is None:
                file.unlink(missing_ok=True)
            else:
                atomic_write(file, content)
        raise
    # Remove only empty managed directories, never arbitrary project content.
    parents = {parent for relative in touched for parent in safe_path(base, relative, names).parents
               if parent == base or base in parent.parents}
    for directory in sorted(parents, key=lambda p: len(p.parts), reverse=True):
        try:
            directory.rmdir()
        except OSError:
            pass
    return result


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    target = parser.add_mutually_exclusive_group(required=True)
    target.add_argument('--project', type=Path, help='Existing Android project root; installs under .codex/.')
    target.add_argument('--user', action='store_true', help='Explicit personal install into CODEX_HOME or ~/.codex.')
    parser.add_argument('--uninstall', action='store_true', help='Remove only unchanged, managed files.')
    parser.add_argument('--dry-run', action='store_true', help='Check for conflicts and show target without writing.')
    args = parser.parse_args(argv)
    try:
        if args.project:
            if not args.project.is_dir():
                raise SuiteError('Project root must be an existing directory.')
            base = args.project.resolve() / '.codex'
        else:
            base = Path(os.environ.get('CODEX_HOME', str(Path.home() / '.codex')))
        print(json.dumps(install(base, uninstall=args.uninstall, dry_run=args.dry_run), indent=2))
        return 0
    except (SuiteError, ValueError, OSError) as exc:
        print(str(exc), file=sys.stderr)
        return 1


if __name__ == '__main__':
    sys.exit(main())
