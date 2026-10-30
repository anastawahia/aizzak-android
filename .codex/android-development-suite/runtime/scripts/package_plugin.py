#!/usr/bin/env python3
"""Validate and create a deterministic plugin ZIP without caches or credentials."""
import argparse
import hashlib
from pathlib import Path
import sys
import zipfile

from suite import PLUGIN, SECRET_FILE, SuiteError, read_json, validate_plugin


def package(destination, marketplace=False):
    result = validate_plugin()
    if result['status'] != 'passed':
        raise SuiteError('; '.join(result['errors']))
    destination = Path(destination).resolve()
    if destination.is_relative_to(PLUGIN):
        raise SuiteError('Archive output must be outside the plugin directory.')
    if destination.exists():
        raise SuiteError('Archive already exists; choose a new output path.')
    files = []
    for file in sorted(PLUGIN.rglob('*')):
        if not file.is_file() or any(p in {'__pycache__', '.git', '.gradle', 'build', '.test-output', '.checks'} for p in file.relative_to(PLUGIN).parts):
            continue
        if file.is_symlink() or SECRET_FILE.search(file.name):
            raise SuiteError('Credential-like or symlink file in package input; review before distributing.')
        files.append((PLUGIN.name + '/' + file.relative_to(PLUGIN).as_posix(), file.read_bytes(), file.suffix == '.sh'))
    prefix = ''
    if marketplace:
        repo = PLUGIN.parent
        codex = read_json(repo / '.agents/plugins/marketplace.json')
        claude = read_json(repo / '.claude-plugin/marketplace.json')
        if codex['name'] != claude['name'] or codex['name'] != 'android-development-tools':
            raise SuiteError('Host marketplace names must agree.')
        if (codex['plugins'][0]['source'] != {'source': 'local', 'path': './' + PLUGIN.name}
                or claude['plugins'][0]['source'] != './' + PLUGIN.name
                or claude['plugins'][0]['version'] != read_json(PLUGIN / '.claude-plugin/plugin.json')['version']):
            raise SuiteError('Marketplace source or version differs from the packaged plugin.')
        prefix = codex['name'] + '/'
        for relative in ['.agents/plugins/marketplace.json', '.claude-plugin/marketplace.json']:
            file = repo / relative
            if file.is_symlink():
                raise SuiteError('Marketplace metadata cannot be a symlink.')
            files.append((relative, file.read_bytes(), False))
        instructions = '''# Android Development Suite: Codex and Claude Code

Extract this archive, open a terminal in this directory, then run:

Codex:
```sh
codex plugin marketplace add .
codex plugin add android-development-suite@android-development-tools
python ./android-development-suite/scripts/install_codex_agents.py --project /absolute/path/to/android-project
```
Restart/open a new chat and select `android-development-suite:android-orchestrate`
for one coordinator. Supply the Android project path/destination and task. It
routes specialists and pauses for applicable human requirements/design/PR review.
Review/trust optional hooks in Codex when prompted.
The explicit installer registers twelve native agents for the selected project,
with a self-contained runtime snapshot. Open that trusted project in a new chat.
Alternatively use `--user` for personal agents. Config/auth/hook trust is unchanged.

Claude Code:
```sh
claude --plugin-dir ./android-development-suite
```
Invoke `/android-development-suite:orchestrate` and describe the task.

Python 3.10+ is required. See [the full guide](android-development-suite/README.md)
and [compatibility evidence](android-development-suite/docs/compatibility.md).
'''
        files.append(('README.md', instructions.encode('utf-8'), False))
    destination.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(destination, 'x', zipfile.ZIP_DEFLATED) as out:
        for relative, content, executable in sorted(files):
            info = zipfile.ZipInfo(prefix + relative, (2026, 1, 1, 0, 0, 0))
            info.compress_type = zipfile.ZIP_DEFLATED
            info.external_attr = (0o100755 if executable else 0o100644) << 16
            out.writestr(info, content)
    print(f'{destination}\nfiles={len(files)}\nsha256={hashlib.sha256(destination.read_bytes()).hexdigest()}')


if __name__ == '__main__':
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('--output', required=True)
    p.add_argument('--marketplace', action='store_true', help='Include both host catalogs under an installable marketplace root.')
    try:
        args = p.parse_args()
        package(args.output, marketplace=args.marketplace)
    except (SuiteError, OSError) as exc:
        print(str(exc), file=sys.stderr)
        sys.exit(1)
