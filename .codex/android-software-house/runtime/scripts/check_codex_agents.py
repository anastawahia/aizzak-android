#!/usr/bin/env python3
"""Exercise real Codex custom-agent loading, spawning and handoff with a local fixture.

Uses a disposable CODEX_HOME and a deterministic localhost Responses fixture,
not an OpenAI model or credentials. Verifies runtime wiring, not Android reasoning.
"""
import argparse
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import tempfile
import threading

from install_codex_agents import install, load_agent
from suite import PLUGIN, SuiteError, read_json, redact, write_json


class AgentFixture:
    def __init__(self, base, names):
        self.names = names
        self.instructions = {name: load_agent(base / 'agents' / (name + '.toml'))['developer_instructions']
                             for name in names}
        self.registered = []
        self.invoked = []
        self.handoffs = []
        self.errors = []
        self.parent_calls = 0
        self.local_requests = 0
        self.namespace = None

    def reply(self, request):
        self.local_requests += 1
        developer = '\n'.join(part.get('text', '') for item in request.get('input', [])
                              if item.get('role') == 'developer'
                              for part in item.get('content', []) if isinstance(part, dict))
        role = re.search(r'Android suite native role: ([a-z-]+)', developer)
        if role:
            name = role.group(1)
            if name not in self.instructions or self.instructions[name] not in developer:
                raise SuiteError('Child did not receive its exact installed native contract.')
            if request.get('model') != 'suite-fixture':
                raise SuiteError('Child did not inherit the configured fixture model.')
            self.invoked.append(name)
            return self.message('ACK:' + name)
        if self.parent_calls == 0:
            functions = []
            for tool in request.get('tools', []):
                if tool.get('type') == 'namespace':
                    for function in tool.get('tools', []):
                        functions.append((tool['name'], function))
                else:
                    functions.append((None, tool))
            namespace, spawn = next((ns, tool) for ns, tool in functions if tool.get('name') == 'spawn_agent')
            description = spawn['parameters']['properties']['agent_type']['description']
            self.registered = [name for name in self.names if name + ': {' in description]
            if set(self.registered) != set(self.names):
                raise SuiteError('Native spawn tool does not advertise every installed agent.')
            self.namespace = namespace
        step = self.parent_calls
        self.parent_calls += 1
        if step >= 3 * len(self.names):
            return self.message('SUITE_NATIVE_AGENTS_OK')
        name = self.names[step // 3]
        phase = step % 3
        outputs = [item for item in request.get('input', []) if item.get('type') == 'function_call_output']
        if phase == 0:
            tool = 'spawn_agent'
            arguments = {'agent_type': name, 'fork_context': False,
                         'message': 'Native agent wiring test only. Return ACK:' + name + '. No tools, files or external actions.'}
        else:
            spawn_outputs = [json.loads(item['output']) for item in outputs if item.get('call_id') == f'suite_call_{step - phase}']
            agent_id = spawn_outputs[-1]['agent_id']
            if phase == 1:
                tool, arguments = 'wait_agent', {'targets': [agent_id], 'timeout_ms': 10000}
            else:
                wait = next(item['output'] for item in reversed(outputs) if item.get('call_id') == f'suite_call_{step - 1}')
                if 'ACK:' + name not in wait or 'completed' not in wait:
                    raise SuiteError('Native wait did not return the completed specialist handoff: ' + name)
                self.handoffs.append(name)
                tool, arguments = 'close_agent', {'target': agent_id}
        item = {'type': 'function_call', 'id': f'suite_fc_{step}', 'call_id': f'suite_call_{step}',
                'name': tool, 'arguments': json.dumps(arguments)}
        if self.namespace:
            item['namespace'] = self.namespace
        return item

    @staticmethod
    def message(text):
        return {'type': 'message', 'id': 'suite_msg', 'role': 'assistant',
                'content': [{'type': 'output_text', 'text': text, 'annotations': []}]}


def check(scope='project'):
    cli = shutil.which('codex')
    if not cli:
        return {'status': 'blocked', 'reason': 'Codex CLI is not installed.'}
    if scope not in {'project', 'user'}:
        raise SuiteError('Unknown agent test scope.')
    names = read_json(PLUGIN / 'references/codex-catalog.json')['agents']
    with tempfile.TemporaryDirectory(prefix='android-suite-native-agents-') as temporary:
        root = Path(temporary)
        home, project = root / 'codex-home', root / 'project with spaces'
        home.mkdir()
        project.mkdir()
        base = project / '.codex' if scope == 'project' else home
        install(base)
        fixture = AgentFixture(base, names)

        class Handler(BaseHTTPRequestHandler):
            def log_message(self, *_):
                pass

            def do_POST(self):
                try:
                    if self.path != '/v1/responses':
                        raise SuiteError('Unexpected fixture endpoint.')
                    request = json.loads(self.rfile.read(int(self.headers['Content-Length'])))
                    item = fixture.reply(request)
                except Exception as exc:
                    fixture.errors.append(str(exc))
                    item = fixture.message('SUITE_FIXTURE_FAILED')
                response = {'id': 'suite_response', 'object': 'response', 'status': 'completed', 'output': [item],
                            'usage': {'input_tokens': 1, 'output_tokens': 1, 'total_tokens': 2}}
                events = [('response.created', {'response': dict(response, status='in_progress', output=[])}),
                          ('response.output_item.done', {'output_index': 0, 'item': item}),
                          ('response.completed', {'response': response})]
                content = ''.join('event: ' + kind + '\ndata: ' + json.dumps(dict(type=kind, **data)) + '\n\n'
                                  for kind, data in events).encode('utf-8')
                self.send_response(200)
                self.send_header('Content-Type', 'text/event-stream')
                self.send_header('Content-Length', str(len(content)))
                self.end_headers()
                self.wfile.write(content)

        server = ThreadingHTTPServer(('127.0.0.1', 0), Handler)
        thread = threading.Thread(target=server.serve_forever, daemon=True)
        thread.start()
        # Only this disposable project is trusted, in this disposable home.
        config = f'''model = "suite-fixture"
model_provider = "suite_fixture"
[model_providers.suite_fixture]
name = "Local deterministic protocol fixture"
base_url = "http://127.0.0.1:{server.server_port}/v1"
wire_api = "responses"
requires_openai_auth = false
[analytics]
enabled = false
[projects.{json.dumps(str(project))}]
trust_level = "trusted"
'''
        (home / 'config.toml').write_text(config, encoding='utf-8')
        env = {key: value for key, value in os.environ.items()
               if key not in {'OPENAI_API_KEY', 'OPENAI_BASE_URL', 'CODEX_API_KEY'}}
        env['CODEX_HOME'] = str(home)
        env['NO_PROXY'] = env.get('NO_PROXY', '') + ',127.0.0.1,localhost'
        try:
            version = subprocess.run([cli, '--version'], env=env, capture_output=True, text=True, timeout=10).stdout.strip()
            run = subprocess.run([cli, 'exec', '--skip-git-repo-check', '--ephemeral', '--json', '-C', str(project),
                                  'Test all installed Android specialist agents using native spawn, wait and close. '
                                  'No implementation work or file writes are authorized.'],
                                 cwd=project, env=env, stdin=subprocess.DEVNULL, capture_output=True,
                                 text=True, encoding='utf-8', errors='replace', timeout=55)
        finally:
            server.shutdown()
            server.server_close()
            thread.join(timeout=5)
        passed = (run.returncode == 0 and not fixture.errors
                  and set(fixture.invoked) == set(names) and len(fixture.invoked) == len(names)
                  and fixture.handoffs == names and 'SUITE_NATIVE_AGENTS_OK' in run.stdout)
        return {'status': 'passed' if passed else 'failed', 'version': version, 'scope': scope,
                'isolated': True, 'external_model_calls': 0, 'local_fixture_requests': fixture.local_requests,
                'registered': fixture.registered, 'invoked': fixture.invoked, 'handoffs': fixture.handoffs,
                'exact_contracts_verified': len(fixture.invoked), 'exit_code': run.returncode,
                'errors': fixture.errors, 'hook_trust_granted': False,
                **({'output': redact(run.stdout[-4000:]), 'diagnostic': redact(run.stderr[-4000:])} if not passed else {})}


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--scope', choices=['project', 'user'], default='project')
    parser.add_argument('--report')
    args = parser.parse_args()
    try:
        result = check(args.scope)
    except (SuiteError, OSError, ValueError, subprocess.TimeoutExpired) as exc:
        result = {'status': 'failed', 'reason': redact(str(exc))}
    if args.report:
        write_json(args.report, result)
    print(json.dumps(result, indent=2))
    raise SystemExit(0 if result['status'] == 'passed' else 1)
