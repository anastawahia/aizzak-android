#!/usr/bin/env python3
"""Probe real Codex installation and skill discovery in a disposable CODEX_HOME.

No model calls, credentials, personal configuration changes or hook trust grants.
Requires a repository marketplace; installed archives can be tested after placing
them under a marketplace root with the bundled catalog format.
"""
import argparse
import json
import os
from pathlib import Path
import queue
import shutil
import subprocess
import tempfile
import threading
import time

from suite import PLUGIN, SuiteError, read_json, redact, write_json


class CodexRpc:
    def __init__(self, cli, cwd, env):
        self.process = subprocess.Popen([cli, 'app-server'], cwd=cwd, env=env,
                                        stdin=subprocess.PIPE, stdout=subprocess.PIPE,
                                        stderr=subprocess.DEVNULL, text=True, encoding='utf-8', errors='replace')
        self.messages = queue.Queue()
        self.next_id = 0
        def receive():
            for line in self.process.stdout:
                try:
                    self.messages.put(json.loads(line))
                except ValueError:
                    continue
            self.messages.put(None)
        self.reader = threading.Thread(target=receive, daemon=True)
        self.reader.start()

    def request(self, method, params):
        self.next_id += 1
        self.process.stdin.write(json.dumps({'id': self.next_id, 'method': method, 'params': params}) + '\n')
        self.process.stdin.flush()
        deadline = time.monotonic() + 45
        while True:
            try:
                message = self.messages.get(timeout=max(0, deadline - time.monotonic()))
            except queue.Empty as exc:
                raise SuiteError(f'Codex timed out during {method}.') from exc
            if message is None:
                raise SuiteError(f'Codex exited during {method}.')
            if message.get('id') != self.next_id:
                continue
            if 'error' in message:
                raise SuiteError(f'Codex {method}: {redact(message["error"])}')
            return message['result']

    def close(self):
        self.process.stdin.close()
        self.process.terminate()
        try:
            self.process.wait(timeout=5)
        except subprocess.TimeoutExpired:
            self.process.kill()
            self.process.wait(timeout=5)
        self.reader.join(timeout=5)
        self.process.stdout.close()


def check(marketplace_root=None):
    cli = shutil.which('codex')
    if not cli:
        return {'status': 'blocked', 'reason': 'Codex CLI is not installed.'}
    repo = Path(marketplace_root or PLUGIN.parent).resolve()
    marketplace = read_json(repo / '.agents/plugins/marketplace.json')
    selector = 'android-development-suite@' + marketplace['name']
    expected = set(read_json(PLUGIN / 'references/codex-catalog.json')['skills'])
    with tempfile.TemporaryDirectory(prefix='android-suite-codex-') as directory:
        scratch = Path(directory)
        home = scratch / 'codex-home'
        project = scratch / 'empty-project'
        home.mkdir()
        project.mkdir()
        (home / 'config.toml').write_text('[analytics]\nenabled = false\n', encoding='utf-8')
        env = dict(os.environ, CODEX_HOME=str(home))
        def command(*args):
            result = subprocess.run([cli, *args], cwd=project, env=env, capture_output=True,
                                    text=True, encoding='utf-8', timeout=60, shell=False)
            if result.returncode:
                raise SuiteError(redact(result.stdout + result.stderr))
            return result.stdout.strip()
        version = command('--version')
        command('plugin', 'marketplace', 'add', str(repo), '--json')
        installed = json.loads(command('plugin', 'add', selector, '--json'))
        available = json.loads(command('plugin', 'list', '--marketplace', marketplace['name'], '--json'))
        rpc = CodexRpc(cli, project, env)
        try:
            rpc.request('initialize', {'clientInfo': {'name': 'android-suite-check', 'version': read_json(PLUGIN / '.codex-plugin/plugin.json')['version']},
                                       'capabilities': {'experimentalApi': True}})
            rpc.process.stdin.write(json.dumps({'method': 'initialized'}) + '\n')
            rpc.process.stdin.flush()
            listing = rpc.request('skills/list', {'cwds': [str(project)], 'forceReload': True})
            detail = rpc.request('plugin/read', {'pluginName': 'android-development-suite',
                                                'marketplacePath': str(repo / '.agents/plugins/marketplace.json')})
        finally:
            rpc.close()
        skills = [skill for entry in listing['data'] for skill in entry['skills']
                  if skill.get('pluginId') == selector or
                  (str(home).replace('\\', '/') in skill['path'].replace('\\', '/') and
                   '/android-development-suite/' in skill['path'].replace('\\', '/'))]
        names = {skill['name'].removeprefix('android-development-suite:') for skill in skills}
        errors = [error for entry in listing['data'] for error in entry['errors']]
        missing = sorted(expected - names)
        unexpected = sorted(names - expected)
        disabled = sorted(skill['name'] for skill in skills if not skill['enabled'])
        hooks = detail['plugin']['hooks']
        hooks_ok = len(hooks) == 1 and hooks[0]['eventName'] == 'preToolUse' and 'hooks/codex.json:' in hooks[0]['key']
        enabled = detail['plugin']['summary']['enabled']
        passed = not (missing or unexpected or disabled or errors) and hooks_ok and enabled
        return {'status': 'passed' if passed else 'failed',
                'version': version, 'isolated': True, 'model_calls': 0,
                'installed': installed, 'available': available,
                'skills': sorted(skill['name'] for skill in skills), 'detail': detail,
                'missing': missing, 'unexpected': unexpected, 'disabled': disabled, 'errors': errors,
                'hook_discovery': 'passed' if hooks_ok else 'failed', 'enabled': enabled}


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--marketplace-root')
    parser.add_argument('--report')
    args = parser.parse_args()
    try:
        result = check(args.marketplace_root)
    except (SuiteError, OSError, ValueError, subprocess.TimeoutExpired) as exc:
        result = {'status': 'failed', 'reason': redact(str(exc))}
    if args.report:
        write_json(args.report, result)
    print(json.dumps({key: value for key, value in result.items() if key not in {'installed', 'available', 'detail', 'skills'}}, indent=2))
    print('Codex skills discovered:', len(result.get('skills', [])))
    raise SystemExit(0 if result['status'] == 'passed' else 1)
