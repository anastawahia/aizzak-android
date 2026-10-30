#!/usr/bin/env python3
"""Android Development Suite: portable, standard-library-only project tooling.

Inspection never executes project code. Verification executes a trusted wrapper only
when explicitly requested. All command arguments are passed as an argv array.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
from pathlib import Path
import platform
import re
import shutil
import subprocess
import sys
import tempfile
import time
import urllib.request
import xml.etree.ElementTree as ET

PLUGIN = Path(__file__).resolve().parents[1]
IGNORED = {'.git', '.gradle', '.idea', 'build', 'node_modules', '__pycache__', '.android-suite', '.test-output'}
SECRET_FILE = re.compile(r'(?i)(\.env(?:\.|$)|\.(?:jks|keystore|p12|pem)$|service.account|credentials|local\.properties)')
TOKEN = re.compile(r'@@([A-Z_]+)@@')
KOTLIN_KEYWORDS = set('as break class continue do else false for fun if in interface is null object package return super this throw true try typealias typeof val var when while'.split())


class SuiteError(Exception):
    pass


def read_json(file):
    return json.loads(Path(file).read_text(encoding='utf-8-sig'))


def digest(file):
    h = hashlib.sha256()
    with Path(file).open('rb') as f:
        for block in iter(lambda: f.read(1024 * 1024), b''):
            h.update(block)
    return h.hexdigest()


def redact(text):
    text = re.sub(r'-----BEGIN [^-]*PRIVATE KEY-----.*?-----END [^-]*PRIVATE KEY-----', '[REDACTED PRIVATE KEY]', str(text), flags=re.S)
    text = re.sub(r'(?i)Bearer\s+[A-Za-z0-9._~+/=-]+', 'Bearer [REDACTED]', text)
    text = re.sub(r'''(?im)(["']?(?:password|passwd|secret|access[_-]?token|api[_-]?key|authorization|storePassword|keyPassword)["']?\s*[=:]\s*)(?:"[^"\r\n]*"|'[^'\r\n]*'|[^\s,;]+)''', r'\1[REDACTED]', text)
    for key, value in os.environ.items():
        if re.search(r'(?i)(secret|token|password|passwd|api_key|apikey)', key) and len(value) >= 6:
            text = text.replace(value, '[REDACTED]')
    return re.sub(r'https://[^\s/@]+:[^\s/@]+@', 'https://[REDACTED]@', text)


def write_json(file, data):
    file = Path(file)
    file.parent.mkdir(parents=True, exist_ok=True)
    # Atomic replacement on the same filesystem. Reports are explicitly requested outputs.
    with tempfile.NamedTemporaryFile('w', encoding='utf-8', dir=file.parent, delete=False) as tmp:
        tmp.write(json.dumps(data, indent=2, ensure_ascii=False) + '\n')
        name = tmp.name
    try:
        os.replace(name, file)
    finally:
        if Path(name).exists():
            Path(name).unlink()


def files_under(root):
    """Bounded walk, no symlink traversal, no secret contents."""
    root = Path(root).resolve()
    count = 0
    for base, dirs, names in os.walk(root, followlinks=False):
        dirs[:] = sorted(d for d in dirs if d not in IGNORED and not Path(base, d).is_symlink())
        for name in sorted(names):
            file = Path(base, name)
            if file.is_symlink():
                continue
            count += 1
            if count > 25000:
                raise SuiteError('Inspection exceeded 25,000 files; select a narrower project root.')
            yield file


def safe_child(root, relative):
    root = Path(root).resolve()
    out = (root / relative).resolve()
    if not out.is_relative_to(root) or out == root:
        raise SuiteError('Output path escapes the selected project root.')
    return out


def run(argv, cwd, timeout=120):
    start = time.monotonic()
    try:
        result = subprocess.run([str(x) for x in argv], cwd=cwd, capture_output=True,
                                text=True, encoding='utf-8', errors='replace', timeout=timeout,
                                shell=False)
        return {'exit_code': result.returncode, 'output': redact(result.stdout + result.stderr),
                'duration_seconds': round(time.monotonic() - start, 3)}
    except subprocess.TimeoutExpired as exc:
        captured = []
        for value in [exc.stdout, exc.stderr]:
            if value:
                captured.append(value.decode('utf-8', errors='replace') if isinstance(value, bytes) else value)
        return {'exit_code': 124, 'output': redact(''.join(captured)) + '\nCommand timed out; inspect any Gradle daemon before retrying.',
                'duration_seconds': round(time.monotonic() - start, 3)}
    except OSError as exc:
        return {'exit_code': 127, 'output': redact(str(exc)), 'duration_seconds': 0}


def git_state(root):
    if not shutil.which('git'):
        return {'available': False}
    revision = run(['git', 'rev-parse', 'HEAD'], root, 10)
    status = run(['git', '--no-optional-locks', 'status', '--porcelain=v1', '--untracked-files=normal'], root, 15)
    return {'available': revision['exit_code'] == 0, 'revision': revision['output'].strip() if revision['exit_code'] == 0 else None,
            'status': status['output'].splitlines() if status['exit_code'] == 0 else []}


def inspect_project(root):
    root = Path(root).resolve(strict=True)
    configs, modules, tests, kotlin = [], [], [], []
    versions = {}
    for f in files_under(root):
        rel = f.relative_to(root).as_posix()
        if SECRET_FILE.search(f.name):
            continue
        if f.suffix in {'.kt', '.java'}:
            kotlin.append(rel)
            if '/test/' in '/' + rel or '/androidTest/' in '/' + rel:
                tests.append(rel)
        if f.name in {'build.gradle.kts', 'build.gradle', 'settings.gradle.kts', 'settings.gradle',
                      'libs.versions.toml', 'gradle-wrapper.properties', 'AndroidManifest.xml'}:
            configs.append(rel)
            if f.name in {'build.gradle.kts', 'build.gradle'} and f.parent != root:
                modules.append(f.parent.relative_to(root).as_posix())
            if f.stat().st_size < 1024 * 1024:
                content = f.read_text(encoding='utf-8', errors='replace')
                for key in ['agp', 'kotlin', 'compose-bom']:
                    match = re.search(r'(?m)^' + key + r'\s*=\s*"([\w.\-]+)"', content)
                    if match:
                        versions[key] = match.group(1)
                inline_patterns = {
                    'agp': r'''id\s*(?:\(\s*)?["']com\.android\.(?:application|library)["']\s*\)?\s*version\s*["']([\d.]+)["']''',
                    'kotlin': r'''(?:id\s*(?:\(\s*)?["']org\.jetbrains\.kotlin\.android["']\s*\)?|kotlin\(\s*["']android["']\s*\))\s*version\s*["']([\d.]+)["']''',
                    'gradle': r'gradle-([\d.]+)-(?:bin|all)\.zip',
                }
                for key, pattern in inline_patterns.items():
                    match = re.search(pattern, content)
                    if match:
                        versions.setdefault(key, match.group(1))
    return {'root': str(root), 'git': git_state(root), 'modules': modules,
            'configuration_files': configs, 'source_files': len(kotlin), 'test_files': tests,
            'versions': versions, 'wrapper_present': (root / 'gradlew').is_file() or (root / 'gradlew.bat').is_file(),
            'read_only': True}


def sdk_path(root):
    for key in ['ANDROID_HOME', 'ANDROID_SDK_ROOT']:
        if os.environ.get(key):
            return Path(os.environ[key]).expanduser()
    props = Path(root) / 'local.properties'
    if props.is_file():
        match = re.search(r'(?m)^sdk\.dir=(.+)$', props.read_text(encoding='utf-8'))
        if match:
            return Path(match.group(1).replace('\\:', ':').replace('\\\\', '\\'))
    return None


def doctor(root, devices=False):
    sdk = sdk_path(root)
    java = str(Path(os.environ['JAVA_HOME']) / 'bin' / ('java.exe' if os.name == 'nt' else 'java')) if os.environ.get('JAVA_HOME') else shutil.which('java')
    adb = sdk / 'platform-tools' / ('adb.exe' if os.name == 'nt' else 'adb') if sdk else None
    java_result = run([java, '-version'], root, 15) if java else {'exit_code': 127, 'output': 'Install JDK 17+ and configure JAVA_HOME.'}
    report = {'os': platform.system(), 'host': platform.platform(),
              'wsl': 'microsoft' in platform.release().lower(),
              'java': java_result, 'sdk': str(sdk) if sdk else None,
              'sdk_exists': bool(sdk and sdk.is_dir()),
              'platforms': sorted(p.name for p in (sdk / 'platforms').glob('*')) if sdk else [],
              'build_tools': sorted(p.name for p in (sdk / 'build-tools').glob('*')) if sdk else [],
              'adb': str(adb) if adb and adb.is_file() else None,
              'emulator_binary': bool(sdk and (sdk / 'emulator' / ('emulator.exe' if os.name == 'nt' else 'emulator')).is_file()),
              'git': shutil.which('git'), 'global_gradle': shutil.which('gradle'),
              'wrapper': (Path(root) / 'gradlew').is_file() or (Path(root) / 'gradlew.bat').is_file(),
              'devices_checked': devices, 'diagnostics': []}
    report['configured_versions'] = inspect_project(root)['versions']
    report['javac'] = shutil.which('javac')
    java_major = re.search(r'version "(?:1\.)?(\d+)', java_result['output'])
    report['java_major'] = int(java_major.group(1)) if java_major else None
    if java_result['exit_code'] != 0 or not java_major or report['java_major'] < 17:
        report['diagnostics'].append('Baseline requires JDK 17. Select a compatible JDK with JAVA_HOME; do not assume a JRE can compile Android sources.')
    if not report['sdk_exists']:
        report['diagnostics'].append('Set ANDROID_HOME to an installed SDK; no system installation was attempted.')
    if report['wsl']:
        report['diagnostics'].append('WSL build host is separate from emulator/USB host. Configure adb networking or USB forwarding explicitly.')
    if not report['wrapper']:
        report['diagnostics'].append('Gradle Wrapper absent. Generate or bootstrap it; do not substitute arbitrary global Gradle.')
    if devices:
        report['devices'] = run([str(adb), 'devices', '-l'], root, 20) if adb and adb.is_file() else {'exit_code': 127, 'output': 'ADB not found.'}
        # adb devices reports physical and emulator serials; offline/unauthorized devices are not usable.
        report['usable_devices'] = re.findall(r'(?m)^(\S+)\s+device\b', report['devices']['output'])
        report['running_emulators'] = [s for s in report['usable_devices'] if s.startswith('emulator-')]
    return report


def validate_identifier(value, kind='package'):
    regex = r'[a-z][a-z0-9_]*(?:\.[a-z][a-z0-9_]*)+' if kind == 'package' else r'[A-Z][A-Za-z0-9]*'
    if not re.fullmatch(regex, value):
        raise SuiteError(f'Invalid {kind}: {value}')
    if kind == 'package' and any(p in KOTLIN_KEYWORDS | {'import'} for p in value.split('.')):
        raise SuiteError('Package contains a Kotlin keyword.')


def render_tree(template, destination, values, extra_files=None):
    destination = Path(destination).absolute()
    if destination.exists() or destination.is_symlink():
        raise SuiteError(f'Refusing to overwrite existing destination: {destination}')
    destination.parent.mkdir(parents=True, exist_ok=True)
    staged = Path(tempfile.mkdtemp(prefix='.android-suite-', dir=destination.parent))
    try:
        for file in sorted(Path(template).rglob('*')):
            if not file.is_file():
                continue
            relative = file.relative_to(template).as_posix()
            for key, value in values.items():
                relative = relative.replace('@@' + key + '@@', str(value))
            if relative.endswith('.tmpl'):
                relative = relative[:-5]
            target = safe_child(staged, relative)
            target.parent.mkdir(parents=True, exist_ok=True)
            content = file.read_text(encoding='utf-8')
            for key, value in values.items():
                content = content.replace('@@' + key + '@@', str(value))
            if TOKEN.search(content) or TOKEN.search(relative):
                raise SuiteError(f'Unresolved template variable in {relative}')
            target.write_text(content, encoding='utf-8', newline='\n')
        for relative, content in (extra_files or {}).items():
            target = safe_child(staged, relative)
            if target.exists() or TOKEN.search(content):
                raise SuiteError(f'Conflicting or unresolved generated file: {relative}')
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_text(content, encoding='utf-8', newline='\n')
        staged.rename(destination)
    finally:
        if staged.exists():
            resolved_stage = staged.resolve()
            if not staged.is_symlink() and resolved_stage.is_relative_to(destination.parent.resolve()) and staged.name.startswith('.android-suite-'):
                shutil.rmtree(resolved_stage)
            else:
                raise SuiteError('Unexpected staging path; refusing recursive cleanup.')
    return {'destination': str(destination), 'files': len(list(destination.rglob('*'))), 'built': False}


def init_project(args):
    validate_identifier(args.package)
    validate_identifier(args.application_id or args.package)
    if not re.fullmatch(r'[A-Za-z][A-Za-z0-9 _-]{0,59}', args.name):
        raise SuiteError('App name must be 1–60 plain letters, digits, spaces, underscores or hyphens.')
    if not 23 <= args.min_sdk <= args.target_sdk <= args.compile_sdk <= 36:
        raise SuiteError('This baseline supports 23 <= minSdk <= targetSdk <= compileSdk <= 36. Review the version profile for newer APIs.')
    profile = read_json(PLUGIN / 'references' / 'versions.json')
    values = {k.upper().replace('-', '_'): v for k, v in profile['versions'].items()}
    values.update(PACKAGE=args.package, PACKAGE_PATH=args.package.replace('.', '/'),
                  APPLICATION_ID=args.application_id or args.package, APP_NAME=args.name,
                  MIN_SDK=args.min_sdk, TARGET_SDK=args.target_sdk, COMPILE_SDK=args.compile_sdk)
    return render_tree(PLUGIN / 'templates' / 'android-project', args.destination, values)


def new_feature(args):
    validate_identifier(args.package)
    validate_identifier(args.name, 'class')
    return render_tree(PLUGIN / 'templates' / 'feature-module', args.destination,
                       {'PACKAGE': args.package, 'PACKAGE_PATH': args.package.replace('.', '/'), 'FEATURE': args.name})


def openapi_model(spec, schema):
    if not isinstance(schema, dict):
        raise SuiteError('Each schema must be a JSON object.')
    if '$ref' in schema:
        ref = schema['$ref']
        if not ref.startswith('#/components/schemas/'):
            raise SuiteError('Only local component schema references are supported; no remote fetch is performed.')
        name = ref.rsplit('/', 1)[1]
        if name not in spec.get('components', {}).get('schemas', {}):
            raise SuiteError(f'Unresolved schema reference: {ref}')
        validate_identifier(name, 'class')
        return name
    kind = schema.get('type')
    if schema.get('nullable') or isinstance(kind, list):
        raise SuiteError('Nullable/union schemas require manual contract design; use an explicit adapter.')
    if kind == 'array':
        return f"List<{openapi_model(spec, schema.get('items', {}))}>"
    if kind in {'string', 'integer', 'number', 'boolean'}:
        return {'string': 'String', 'integer': 'Long' if schema.get('format') == 'int64' else 'Int', 'number': 'Double', 'boolean': 'Boolean'}[kind]
    raise SuiteError('Inline objects, compositions, maps, and untyped schemas require manual DTO design.')


def generate_api(args):
    validate_identifier(args.package)
    spec = read_json(args.spec)
    if not isinstance(spec, dict):
        raise SuiteError('OpenAPI document must be a JSON object.')
    if not str(spec.get('openapi', '')).startswith('3.0.'):
        raise SuiteError('Generator accepts JSON OpenAPI 3.0 only. YAML/3.1/complex contracts need the integration skill, not guessed code.')
    components = spec.get('components', {})
    if not isinstance(components, dict):
        raise SuiteError('OpenAPI components must be an object.')
    schemas = components.get('schemas', {})
    if not isinstance(schemas, dict) or not schemas:
        raise SuiteError('Expected components.schemas.')
    models = {}
    for name, schema in schemas.items():
        validate_identifier(name, 'class')
        if not isinstance(schema, dict) or schema.get('type') != 'object' or not isinstance(schema.get('properties'), dict) or not schema['properties'] or any(k in schema for k in ['allOf', 'oneOf', 'anyOf', 'additionalProperties']):
            raise SuiteError(f'Unsupported object schema: {name}')
        props = []
        required = schema.get('required', [])
        if not isinstance(required, list) or not all(isinstance(field, str) for field in required):
            raise SuiteError('Schema required must be a list of property names.')
        for field, field_schema in schema['properties'].items():
            if not re.fullmatch(r'[a-z][A-Za-z0-9_]*', field) or field in KOTLIN_KEYWORDS:
                raise SuiteError(f'Property {field} requires a manual SerialName mapping.')
            typ = openapi_model(spec, field_schema)
            props.append(f'    val {field}: {typ}' + ('' if field in required else '? = null'))
        models[name + '.kt'] = 'package ' + args.package + '\n\nimport kotlinx.serialization.Serializable\n\n@Serializable\ndata class ' + name + '(\n' + ',\n'.join(props) + '\n)\n'
    endpoints = []
    returns = []
    paths = spec.get('paths', {})
    if not isinstance(paths, dict):
        raise SuiteError('OpenAPI paths must be an object.')
    for url, methods in paths.items():
        if not re.fullmatch(r'/[A-Za-z0-9/_-]*', url):
            raise SuiteError('Parameterized paths need manual parameter mapping.')
        if not isinstance(methods, dict):
            raise SuiteError('Each OpenAPI path item must be an object.')
        for method, operation in methods.items():
            if method in {'summary', 'description'}:
                continue
            if method != 'get' or not isinstance(operation, dict) or operation.get('parameters') or operation.get('requestBody'):
                raise SuiteError('Automated API generation supports parameterless GET endpoints only.')
            name = operation.get('operationId', '')
            if not re.fullmatch(r'[a-z][A-Za-z0-9]*', name) or name in KOTLIN_KEYWORDS:
                raise SuiteError('A Kotlin-safe operationId is required.')
            schema = operation.get('responses', {}).get('200', {}).get('content', {}).get('application/json', {}).get('schema', {})
            typ = openapi_model(spec, schema)
            endpoints.append(f'    @GET("{url.lstrip(chr(47))}")\n    suspend fun {name}(): {typ}')
            returns.append((name, typ))
    if not endpoints:
        raise SuiteError('No supported endpoints.')
    if len({name for name, _ in returns}) != len(returns):
        raise SuiteError('Operation IDs must be unique.')
    destination = Path(args.destination)
    if destination.exists():
        raise SuiteError('API destination already exists.')
    # Complete validation before any output is written.
    result = render_tree(PLUGIN / 'templates' / 'api-integration', destination,
                         {'PACKAGE': args.package, 'ENDPOINTS': '\n\n'.join(endpoints),
                          'REPOSITORY_METHODS': '\n\n'.join(f'    suspend fun {n}(): ApiResult<{t}> = safeApiCall {{ api.{n}() }}' for n, t in returns)},
                         extra_files=models)
    result.update({'operations': [n for n, _ in returns], 'compiled': False, 'contract_sha256': digest(args.spec)})
    return result


def source_fingerprint(root):
    h = hashlib.sha256()
    for file in files_under(root):
        if SECRET_FILE.search(file.name):
            continue
        rel = file.relative_to(root).as_posix()
        h.update(rel.encode())
        h.update(bytes.fromhex(digest(file)))
    return h.hexdigest()


def wrapper_command(root):
    root = Path(root).resolve()
    if os.name == 'nt':
        wrapper = root / 'gradlew.bat'
        # cmd.exe has expansion and metacharacter rules even with shell=False.
        if any(c in str(wrapper) for c in '&|<>^%!"\r\n'):
            raise SuiteError('Move this project to a Windows path without cmd metacharacters before building.')
    else:
        wrapper = root / 'gradlew'
    if not wrapper.is_file():
        raise SuiteError('Gradle Wrapper missing. Run wrapper --download after reviewing the version profile.')
    return [str(wrapper)] if os.name == 'nt' else ['sh', str(wrapper)]


def verify(root, kind, module='app', attempts=1, timeout=900):
    if attempts not in range(1, 4) or not 1 <= timeout <= 7200:
        raise SuiteError('Verification permits 1–3 attempts and a timeout from 1 to 7200 seconds.')
    if not re.fullmatch(r'[A-Za-z][A-Za-z0-9_-]*(?::[A-Za-z][A-Za-z0-9_-]*)*', module):
        raise SuiteError('Invalid Gradle module path.')
    tasks = {'build': ['assembleDebug'], 'unit': ['testDebugUnitTest'], 'lint': ['lintDebug'], 'ui-compile': ['assembleDebugAndroidTest'],
             'ui': ['connectedDebugAndroidTest'], 'release': ['bundleRelease'],
             'dependencies': ['dependencies'], 'static': ['detekt'],
             'all': ['assembleDebug', 'testDebugUnitTest', 'lintDebug']}[kind]
    if kind == 'ui':
        env = doctor(root, True)
        if not env.get('usable_devices'):
            return {'status': 'blocked', 'kind': kind, 'reason': 'No authorized online device.', 'attempts': []}
    command = wrapper_command(root) + [f':{module}:{t}' for t in tasks] + ['--console=plain', '--no-daemon']
    record = {'kind': kind, 'status': 'failed', 'source_sha256': source_fingerprint(Path(root).resolve()),
              'command': command, 'timestamp': int(time.time()), 'attempts': [], 'artifacts': []}
    for _ in range(attempts):
        result = run(command, root, timeout)
        record['attempts'].append(result)
        if result['exit_code'] == 0:
            record['status'] = 'passed'
            break
        # Retry only transient dependency transport failures. No automatic source edits.
        if not re.search(r'(?i)(connection reset|connection timed out|temporary failure|HTTP.*(?:502|503|504))', result['output']):
            break
    if record['status'] != 'passed':
        record['recovery'] = 'Inspect the first failure: JDK/SDK, dependency transport, then compiler/test failure. Apply the smallest reviewed correction; never disable checks. Maximum three attempts per incident.'
    for file in sorted(Path(root).glob(module.replace(':', '/') + '/build/outputs/**/*')):
        if file.is_file() and file.suffix in {'.apk', '.aab'}:
            record['artifacts'].append({'path': str(file.resolve()), 'sha256': digest(file)})
    if kind in {'unit', 'all', 'ui'}:
        pattern = 'outputs/androidTest-results/connected/**/*.xml' if kind == 'ui' else 'test-results/testDebugUnitTest/*.xml'
        reports = sorted((Path(root) / module.replace(':', '/') / 'build').glob(pattern))
        summary = {'tests': 0, 'failures': 0, 'errors': 0, 'skipped': 0, 'reports': []}
        for file in reports:
            try:
                xml = ET.parse(file).getroot()
                suites = [xml] if xml.tag == 'testsuite' else list(xml.iter('testsuite'))
                for item in suites:
                    for key in ['tests', 'failures', 'errors', 'skipped']:
                        summary[key] += int(item.get(key, 0))
                summary['reports'].append(str(file.resolve()))
            except (ET.ParseError, ValueError):
                summary.setdefault('invalid_reports', []).append(str(file.resolve()))
        record['test_summary'] = summary
        if record['status'] == 'passed' and (not summary['tests'] or summary.get('invalid_reports') or summary['failures'] or summary['errors']):
            record['status'] = 'failed'
            record['reason'] = 'Gradle exited successfully but expected test execution evidence was absent or reported failures.'
        if summary['skipped']:
            record['test_limitations'] = 'Some tests were skipped; inspect their acceptance criteria before approving the gate.'
    # Build success is not signing or publication success.
    record['signed_verified'] = False
    record['published'] = False
    return record


def bootstrap_wrapper(root, download=False):
    root = Path(root).resolve(strict=True)
    properties = root / 'gradle/wrapper/gradle-wrapper.properties'
    if not properties.is_file():
        raise SuiteError('Wrapper properties missing; inspect the project first.')
    content = properties.read_text(encoding='utf-8')
    match = re.search(r'gradle-([0-9.]+)-bin\.zip', content)
    if not match or match.group(1) != read_json(PLUGIN / 'references/versions.json')['versions']['gradle']:
        raise SuiteError('Wrapper bootstrap is restricted to the reviewed baseline; use the existing project wrapper otherwise.')
    version = match.group(1)
    paths = [root / 'gradlew', root / 'gradlew.bat', root / 'gradle/wrapper/gradle-wrapper.jar']
    if any(p.exists() for p in paths):
        raise SuiteError('Refusing to replace an existing or partial wrapper. Review it manually.')
    if not download:
        return {'status': 'blocked', 'reason': 'Use --download to authorize fetching official wrapper files.'}
    base = f'https://raw.githubusercontent.com/gradle/gradle/v{version}.0/'
    def fetch(url):
        with urllib.request.urlopen(url, timeout=60) as response:
            return response.read(4 * 1024 * 1024)
    wrapper_hash = fetch(f'https://services.gradle.org/distributions/gradle-{version}-wrapper.jar.sha256').decode().strip()
    dist_hash = fetch(f'https://services.gradle.org/distributions/gradle-{version}-bin.zip.sha256').decode().strip()
    jar = fetch(base + 'gradle/wrapper/gradle-wrapper.jar')
    if hashlib.sha256(jar).hexdigest() != wrapper_hash or not re.fullmatch('[a-f0-9]{64}', dist_hash):
        raise SuiteError('Official Gradle checksum verification failed.')
    payloads = [fetch(base + 'gradlew'), fetch(base + 'gradlew.bat'), jar]
    for file, data in zip(paths, payloads):
        file.parent.mkdir(parents=True, exist_ok=True)
        with file.open('xb') as out:
            out.write(data)
    paths[0].chmod(0o755)
    if 'distributionSha256Sum=' not in content:
        properties.write_text(content.rstrip() + '\ndistributionSha256Sum=' + dist_hash + '\n', encoding='utf-8')
    return {'status': 'passed', 'version': version, 'wrapper_sha256': wrapper_hash,
            'distribution_sha256': dist_hash, 'distribution_downloaded': False}


def init_memory(root):
    destination = safe_child(root, '.android-suite')
    destination.mkdir(exist_ok=True)
    created = []
    for template in (PLUGIN / 'templates/documentation').glob('*.md'):
        target = destination / template.name
        if not target.exists():
            with target.open('x', encoding='utf-8') as out:
                out.write(template.read_text(encoding='utf-8'))
            created.append(target.name)
    return {'created': created, 'root': str(destination)}


def route(workflow, small=False):
    catalog = read_json(PLUGIN / 'workflows/catalog.json')
    if workflow not in catalog:
        raise SuiteError('Unknown workflow.')
    data = dict(catalog[workflow])
    if small and workflow in {'feature', 'bugfix', 'ui-redesign'}:
        data['agents'] = ['compose-developer' if workflow == 'ui-redesign' else 'android-core-developer', 'android-qa-engineer']
        data['steps'] = ['Inspect affected files and current changes.', 'Implement a minimal correction with focused regression coverage.', 'Run relevant compile/test/lint tasks and review the diff.']
        data['gates'] = ['implementation']
    return {'workflow': workflow, **data, 'execution': 'instruction-driven through the active host; this function selects a plan, not agents'}


def gate(root, evidence_file, stage):
    evidence = read_json(evidence_file)
    required = read_json(PLUGIN / 'references/gates.json')[stage]
    errors = []
    if evidence.get('source_sha256') != source_fingerprint(Path(root).resolve()):
        errors.append('Evidence does not match the current project source fingerprint.')
    checks = evidence.get('checks', {})
    automated_kinds = {'compile': {'build', 'all'}, 'static_analysis': {'lint', 'static', 'all'},
                       'relevant_tests': {'unit', 'ui', 'all'}, 'contract_tests': {'unit', 'ui', 'all'},
                       'release_build': {'release'}}
    for key in required:
        check = checks.get(key, {})
        if check.get('status') != 'passed':
            errors.append(f'{key}: not passed; explicit exceptions belong in the human-reviewed workflow report, not automatic progression.')
            continue
        files = check.get('evidence', [])
        if not files:
            errors.append(f'{key}: no evidence files.')
        execution_valid = False
        for item in files:
            f = safe_child(root, item.get('path', ''))
            if not f.is_file() or digest(f) != item.get('sha256'):
                errors.append(f'{key}: missing or changed evidence file.')
                continue
            if key in automated_kinds and f.suffix == '.json':
                record = read_json(f)
                attempts = record.get('attempts', [])
                valid = (record.get('status') == 'passed' and record.get('kind') in automated_kinds[key]
                         and record.get('source_sha256') == evidence.get('source_sha256')
                         and bool(attempts) and attempts[-1].get('exit_code') == 0)
                if key in {'relevant_tests', 'contract_tests'}:
                    tests = record.get('test_summary', {})
                    valid = valid and tests.get('tests', 0) > 0 and not any(tests.get(k, 0) for k in ['failures', 'errors', 'skipped'])
                if key == 'release_build':
                    bundles = [a for a in record.get('artifacts', []) if a.get('path', '').endswith('.aab')]
                    valid = valid and bool(bundles)
                    for artifact in bundles:
                        bundle = Path(artifact['path']).resolve()
                        valid = valid and bundle.is_relative_to(Path(root).resolve()) and bundle.is_file() and digest(bundle) == artifact.get('sha256')
                execution_valid = execution_valid or valid
        if key in automated_kinds and not execution_valid:
            errors.append(f'{key}: no matching successful execution report with current source and required results.')
    return {'stage': stage, 'status': 'failed' if errors else 'passed', 'errors': errors,
            'limitations': 'Evidence hashes establish provenance, not semantic correctness. Human review is required for manual checks.'}


def audit_release(root, bundle):
    root, bundle = Path(root).resolve(), Path(bundle).resolve()
    if not bundle.is_file() or bundle.suffix != '.aab':
        raise SuiteError('Select an existing Android App Bundle.')
    jarsigner = shutil.which('jarsigner')
    if not jarsigner and os.environ.get('JAVA_HOME'):
        candidate = Path(os.environ['JAVA_HOME']) / 'bin' / ('jarsigner.exe' if os.name == 'nt' else 'jarsigner')
        jarsigner = str(candidate) if candidate.is_file() else None
    signature = run([jarsigner, '-verify', '-verbose', '-certs', str(bundle)], root, 90) if jarsigner else {'exit_code': 127, 'output': 'jarsigner missing.'}
    verified = signature['exit_code'] == 0 and 'jar verified.' in signature['output'].lower() and 'jar is unsigned' not in signature['output'].lower()
    # Trust of the upload certificate is a separate manual Play Console comparison.
    return {'bundle': str(bundle), 'sha256': digest(bundle), 'signature_verified': verified,
            'signature': signature, 'certificate_matches_play': 'not_checked',
            'publish_ready': False, 'published': False,
            'required_manual_checks': ['Compare upload certificate fingerprint with Play Console.', 'Run release gate.', 'Check current Play policy, declarations, listing and track eligibility.', 'Obtain artifact-and-track-specific approval.']}


def dependency_audit(root, inventory=None):
    report = {'declared': [], 'license_findings': [], 'vulnerability_scan': 'not_run', 'legal_clearance': False}
    for file in files_under(root):
        if file.suffix == '.toml' and file.name.endswith('.versions.toml'):
            for group, artifact in re.findall(r'module\s*=\s*"([A-Za-z0-9_.-]+):([A-Za-z0-9_.-]+)"', file.read_text(encoding='utf-8')):
                report['declared'].append(group + ':' + artifact)
    if inventory:
        sbom = read_json(inventory)
        if sbom.get('bomFormat') != 'CycloneDX':
            raise SuiteError('License inventory must be a CycloneDX JSON SBOM.')
        for component in sbom.get('components', []):
            licenses = [lic.get('license', {}).get('id') or lic.get('expression') or lic.get('license', {}).get('name') or 'unknown' for lic in component.get('licenses', [])]
            report['license_findings'].append({'component': component.get('name'), 'licenses': licenses or ['unknown'],
                                               'review_required': not licenses or any('GPL' in x.upper() or x.upper() in {'UNKNOWN', 'NOASSERTION', 'NONE'} for x in licenses)})
    else:
        report['diagnostic'] = 'Declared coordinates are not a resolved SBOM. Generate a CycloneDX report using a reviewed Gradle plugin; supply --sbom. Use OSV-Scanner or an approved scanner for known vulnerabilities.'
    return report


def validate_plugin(root=PLUGIN):
    """Validate both host layouts; native host discovery is checked separately."""
    root = Path(root)
    errors = []
    manifest = read_json(root / '.claude-plugin/plugin.json')
    if manifest.get('name') != 'android-development-suite':
        errors.append('Unexpected plugin name.')
    try:
        codex = read_json(root / '.codex-plugin/plugin.json')
        for field in ['name', 'version', 'license', 'author']:
            if codex.get(field) != manifest.get(field):
                errors.append(f'Host manifests disagree on {field}.')
        if codex.get('skills') != './codex-skills/' or codex.get('hooks') != './hooks/codex.json':
            errors.append('Codex must select its packaged skill directory and hook adapter.')
    except (ValueError, OSError):
        errors.append('Missing or invalid Codex manifest.')
    expected = read_json(root / 'references/catalog.json')
    from install_codex_agents import load_agent
    native_agents = sorted((root / 'codex-agents').glob('*.toml'))
    if {file.stem for file in native_agents} != set(expected['agents']):
        errors.append('Codex native agent files differ from the shared agent catalog.')
    for file in native_agents:
        try:
            agent = load_agent(file)
            shared = (root / 'agents' / (file.stem + '.md')).read_text(encoding='utf-8')
            body = shared.split('---', 2)[2].strip()
            if body not in agent['developer_instructions'] or agent['developer_instructions'].count('@@PLUGIN_ROOT@@') != 1:
                errors.append(f'{file}: Codex role differs from the shared contract or lacks a resource root.')
        except (SuiteError, ValueError, OSError, IndexError):
            errors.append(f'{file}: invalid native Codex agent.')
    for kind, filename in [('agents', '*.md'), ('skills', '*/SKILL.md'), ('commands', '*.md')]:
        actual = sorted(root.joinpath(kind).glob(filename))
        names = {p.parent.name if kind == 'skills' else p.stem for p in actual}
        if names != set(expected[kind]):
            errors.append(f'{kind}: catalog and files differ: {sorted(names.symmetric_difference(expected[kind]))}')
        for file in actual:
            text = file.read_text(encoding='utf-8')
            if not text.startswith('---\n') or '\n---\n' not in text[4:]:
                errors.append(f'{file}: missing frontmatter')
                continue
            front = text.split('---', 2)[1]
            if not re.search(r'(?m)^description: .+', front):
                errors.append(f'{file}: missing description')
            if kind != 'commands' and not re.search(r'(?m)^name: [a-z0-9-]+$', front):
                errors.append(f'{file}: invalid name')
            if kind == 'agents' and 'bypassPermissions' in front:
                errors.append(f'{file}: permission bypass')
    codex_names = set(expected['skills']) | {'android-' + name for name in expected['commands']}
    actual_codex = sorted((root / 'codex-skills').glob('*/SKILL.md'))
    if {file.parent.name for file in actual_codex} != codex_names:
        errors.append('Codex skill discovery differs from the shared skill and command catalogs.')
    for file in actual_codex:
        text = file.read_text(encoding='utf-8')
        if not text.startswith('---\n') or '\n---\n' not in text[4:]:
            errors.append(f'{file}: missing frontmatter')
            continue
        front = text.split('---', 2)[1]
        if f'\nname: {file.parent.name}\n' not in front or not re.search(r'(?m)^description: .+', front):
            errors.append(f'{file}: invalid Codex skill metadata')
        if '${CLAUDE_PLUGIN_ROOT}' in text or '$ARGUMENTS' in text:
            errors.append(f'{file}: Claude-only runtime dependency')
    try:
        catalog = read_json(root / 'references/codex-catalog.json')
        if catalog.get('agents') != expected['agents']:
            errors.append('Codex native agent catalog differs from the shared roles.')
        if set(catalog['skills']) != codex_names:
            errors.append('Codex catalog differs from shared capabilities.')
        if catalog['entrypoints'] != {'android-' + name: flow for name, flow in
                                      ((p.stem, re.search(r'workflows/([a-z-]+)\.md', p.read_text(encoding='utf-8')).group(1))
                                       for p in (root / 'commands').glob('*.md'))}:
            errors.append('Codex and Claude workflow routing differ.')
    except (ValueError, OSError, KeyError, AttributeError):
        errors.append('Missing or invalid Codex routing catalog.')
    hooks = read_json(root / 'hooks/hooks.json')
    if set(hooks) != {'hooks'} or set(hooks['hooks']) != {'PreToolUse'}:
        errors.append('Unexpected hook configuration.')
    try:
        codex_hooks = read_json(root / 'hooks/codex.json')['hooks']['PreToolUse']
        if not codex_hooks or not all('--host codex' in hook['command']
                                    for group in codex_hooks for hook in group['hooks']):
            errors.append('Codex hooks must use the Codex decision adapter.')
    except (ValueError, OSError, KeyError, TypeError):
        errors.append('Missing or invalid Codex hooks.')
    for workflow in read_json(root / 'workflows/catalog.json').values():
        for field in ['preconditions', 'agents', 'skills', 'steps', 'parallel', 'artifacts', 'gates', 'failure', 'approval', 'report']:
            if not workflow.get(field):
                errors.append(f'Workflow missing {field}')
        if not set(workflow['agents']).issubset(expected['agents']) or not set(workflow['skills']).issubset(expected['skills']):
            errors.append('Workflow references unknown component.')
    for file in root.rglob('*.json'):
        try:
            read_json(file)
        except (ValueError, OSError):
            errors.append(f'Invalid JSON: {file}')
    for file in root.rglob('*.md'):
        for link in re.findall(r'\]\(([^)]+)\)', file.read_text(encoding='utf-8')):
            if any(x in link for x in ['://', '${', '#', '<', '>']):
                continue
            if not (file.parent / link).exists():
                errors.append(f'Broken local link {file.relative_to(root)} -> {link}')
    return {'status': 'failed' if errors else 'passed', 'errors': errors,
            'agents': len(expected['agents']), 'skills': len(expected['skills']),
            'commands': len(expected['commands']), 'codex_skills': len(actual_codex),
            'codex_agents': len(native_agents)}


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--report', help='Explicit JSON report output; overwritten atomically.')
    sub = parser.add_subparsers(dest='action', required=True)
    for name in ['inspect', 'doctor', 'init-memory', 'status']:
        p = sub.add_parser(name)
        p.add_argument('--project', default='.')
        if name == 'doctor':
            p.add_argument('--devices', action='store_true', help='Query adb; may start its local server.')
    p = sub.add_parser('init')
    p.add_argument('destination')
    p.add_argument('--name', required=True)
    p.add_argument('--package', required=True)
    p.add_argument('--application-id')
    p.add_argument('--min-sdk', type=int, default=26)
    p.add_argument('--target-sdk', type=int, default=36)
    p.add_argument('--compile-sdk', type=int, default=36)
    p = sub.add_parser('feature')
    p.add_argument('destination')
    p.add_argument('--package', required=True)
    p.add_argument('--name', required=True)
    p = sub.add_parser('api')
    p.add_argument('destination')
    p.add_argument('--package', required=True)
    p.add_argument('--spec', required=True)
    p = sub.add_parser('verify')
    p.add_argument('--project', default='.')
    p.add_argument('--kind', choices=['build', 'unit', 'lint', 'ui', 'ui-compile', 'release', 'dependencies', 'static', 'all'], default='all')
    p.add_argument('--module', default='app')
    p.add_argument('--execute', action='store_true', help='Authorize executing this trusted project wrapper.')
    p.add_argument('--attempts', type=int, choices=range(1, 4), default=1)
    p.add_argument('--timeout', type=int, default=900)
    p = sub.add_parser('wrapper')
    p.add_argument('--project', default='.')
    p.add_argument('--download', action='store_true')
    p = sub.add_parser('route')
    p.add_argument('workflow')
    p.add_argument('--small', action='store_true')
    p = sub.add_parser('gate')
    p.add_argument('--project', default='.')
    p.add_argument('--stage', choices=['requirements', 'architecture', 'implementation', 'integration', 'release'], required=True)
    p.add_argument('--evidence', required=True)
    p = sub.add_parser('audit-release')
    p.add_argument('--project', default='.')
    p.add_argument('--bundle', required=True)
    p = sub.add_parser('dependencies')
    p.add_argument('--project', default='.')
    p.add_argument('--sbom')
    p = sub.add_parser('validate')
    p.add_argument('--plugin', default=str(PLUGIN))
    args = parser.parse_args(argv)
    try:
        if args.action == 'inspect': result = inspect_project(args.project)
        elif args.action == 'doctor': result = doctor(args.project, args.devices)
        elif args.action == 'init-memory': result = init_memory(args.project)
        elif args.action == 'status':
            memory = Path(args.project) / '.android-suite'
            result = {'memory_present': memory.is_dir(), 'documents': sorted(p.name for p in memory.glob('*.md')), 'git': git_state(args.project), 'inferred_success': False}
        elif args.action == 'init': result = init_project(args)
        elif args.action == 'feature': result = new_feature(args)
        elif args.action == 'api': result = generate_api(args)
        elif args.action == 'verify':
            if not args.execute: raise SuiteError('Review and trust the wrapper, then pass --execute. Inspection alone never executes project code.')
            result = verify(args.project, args.kind, args.module, args.attempts, args.timeout)
        elif args.action == 'wrapper': result = bootstrap_wrapper(args.project, args.download)
        elif args.action == 'route': result = route(args.workflow, args.small)
        elif args.action == 'gate': result = gate(args.project, args.evidence, args.stage)
        elif args.action == 'audit-release': result = audit_release(args.project, args.bundle)
        elif args.action == 'dependencies': result = dependency_audit(args.project, args.sbom)
        elif args.action == 'validate': result = validate_plugin(args.plugin)
        if args.report:
            write_json(args.report, result)
        print(json.dumps(result, indent=2, ensure_ascii=False))
        return 1 if result.get('status') in {'failed', 'blocked'} else 0
    except (SuiteError, OSError, ValueError, KeyError, TypeError) as exc:
        print(json.dumps({'status': 'failed', 'error': redact(exc)}), file=sys.stderr)
        return 2


if __name__ == '__main__':
    sys.exit(main())
