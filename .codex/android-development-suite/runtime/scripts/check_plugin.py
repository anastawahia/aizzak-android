#!/usr/bin/env python3
"""Run plugin checks; preserve each exit status instead of masking earlier failure."""
import argparse
from pathlib import Path
import shutil
import sys

from suite import PLUGIN, read_json, run, validate_plugin, write_json


def check(include_claude=False, report=None, include_codex=False):
    results = [{'name': 'structure', **validate_plugin()}]
    result = run([sys.executable, '-m', 'unittest', 'discover', '-s', str(PLUGIN / 'tests'), '-v'], PLUGIN, 120)
    results.append({'name': 'behavior', 'status': 'passed' if result['exit_code'] == 0 else 'failed', **result})
    if include_claude:
        cli = shutil.which('claude')
        if not cli:
            results.append({'name': 'claude', 'status': 'blocked', 'reason': 'Claude Code not installed.'})
        else:
            for directory in [PLUGIN, PLUGIN / 'agents', PLUGIN / 'skills', PLUGIN / 'commands']:
                result = run([cli, 'plugin', 'validate', str(directory), '--strict', '--json'], PLUGIN, 60)
                results.append({'name': 'claude:' + directory.name, 'status': 'passed' if result['exit_code'] == 0 else 'failed', **result})
            result = run([cli, '--plugin-dir', str(PLUGIN), 'plugin', 'details', 'android-development-suite'], PLUGIN, 60)
            catalog = read_json(PLUGIN / 'references/catalog.json')
            discovered = (result['exit_code'] == 0
                          and f"Agents ({len(catalog['agents'])})" in result['output']
                          and f"Skills ({len(catalog['skills']) + len(catalog['commands'])})" in result['output'])
            results.append({'name': 'runtime-discovery', 'status': 'passed' if discovered else 'failed', **result})
    if include_codex:
        import check_codex
        import check_codex_agents
        try:
            results.append({'name': 'codex-runtime-discovery', **check_codex.check()})
        except (OSError, ValueError, check_codex.SuiteError, check_codex.subprocess.TimeoutExpired) as exc:
            results.append({'name': 'codex-runtime-discovery', 'status': 'failed', 'reason': str(exc)})
        for scope in ['project', 'user']:
            try:
                results.append({'name': 'codex-native-agents:' + scope, **check_codex_agents.check(scope)})
            except (OSError, ValueError, check_codex.SuiteError, check_codex.subprocess.TimeoutExpired) as exc:
                results.append({'name': 'codex-native-agents:' + scope, 'status': 'failed', 'reason': str(exc)})
    overall = {'status': 'passed' if all(r['status'] == 'passed' for r in results) else 'failed', 'checks': results}
    write_json(report or PLUGIN / '.checks/report.json', overall)
    for result in results:
        print(f"{result['name']}: {result['status']}")
        if result['status'] != 'passed': print(result.get('output', result.get('errors', result.get('reason'))))
    return 0 if overall['status'] == 'passed' else 1


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--claude', action='store_true')
    parser.add_argument('--codex', action='store_true', help='Isolated native skills and agent tests; local fixture only, no external model calls.')
    parser.add_argument('--report')
    args = parser.parse_args()
    sys.exit(check(args.claude, args.report, args.codex))
