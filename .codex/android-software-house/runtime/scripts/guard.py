#!/usr/bin/env python3
"""PreToolUse heuristic: deny obvious credential writes; request review for risky shells.

No repository writes and no raw input echo. This is defense in depth, not a shell
parser or a security sandbox. The host's permissions remain authoritative.
"""
import argparse
import json
import re
import sys

SENSITIVE = re.compile(r'(?i)(?:^|/)(?:\.env(?:\..*)?|[^/]*\.(?:jks|keystore|p12|pem)|[^/]*service.account[^/]*\.json)$')
PRIVATE_KEY = re.compile(r'-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----')
RISKY = re.compile(r'(?i)(?:\bgit\s+(?:reset\b[^\r\n]*--hard|clean\b[^\r\n]*-[a-z]*f|push\b[^\r\n]*(?:--force|-f\b))|\brm\s+[^\r\n]*-[a-z]*r[a-z]*f|\bRemove-Item\b[^\r\n]*-Recurse|\b(?:fastlane\s+\S*\s*(?:supply|upload_to_play_store)|gradle\S*\s+[^\r\n]*publish|curl\s+[^\r\n]*androidpublisher))')


def decision(payload):
    name = payload.get('tool_name', '').rsplit('.', 1)[-1]
    data = payload.get('tool_input', {})
    if name == 'apply_patch':
        patch = data if isinstance(data, str) else data.get('command', data.get('input', data.get('patch', '')))
        if not isinstance(patch, str):
            raise ValueError('Patch must be text.')
        # Inspect added content, not removed secrets being cleaned up.
        added = '\n'.join(line[1:] for line in patch.splitlines() if line.startswith('+'))
        if PRIVATE_KEY.search(added):
            return 'deny', 'Private key material must stay in an approved credential provider; do not write it into project files.'
        paths = re.findall(r'^\*\*\* (?:Add File|Update File|Delete File|Move to): (.+)$', patch, re.MULTILINE)
        if any(SENSITIVE.search(p.replace('\\', '/')) and not p.endswith('.env.example') for p in paths):
            return 'ask', 'This patch touches a credential or signing file. Review the destination and approved secret handling.'
        return None
    if not isinstance(data, dict):
        raise ValueError('Tool input must be an object.')
    path = str(data.get('file_path', '')).replace('\\', '/')
    content = str(data.get('content', data.get('new_string', '')))
    if name in {'Write', 'Edit'}:
        if PRIVATE_KEY.search(content):
            return 'deny', 'Private key material must stay in an approved credential provider; do not write it into project files.'
        if SENSITIVE.search(path) and not path.endswith('.env.example'):
            return 'ask', 'This file can contain credentials or signing keys. Confirm the destination and approved secret handling.'
    if name in {'Bash', 'PowerShell', 'exec_command', 'shell_command'} and RISKY.search(str(data.get('command', data.get('cmd', '')))):
        return 'ask', 'Review this destructive or publication command and its exact target before execution.'
    return None


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--host', choices=['claude', 'codex'], default='claude')
    args = parser.parse_args(argv)
    try:
        data = sys.stdin.read(2 * 1024 * 1024 + 1)
        if len(data) > 2 * 1024 * 1024:
            raise ValueError('Hook payload too large.')
        result = decision(json.loads(data))
        if result:
            verdict, reason = result
            if args.host == 'codex' and verdict == 'ask':
                # Codex currently treats PreToolUse "ask" as a failed hook and
                # continues the tool. Deny so review cannot accidentally fail open.
                verdict = 'deny'
                reason += ' Codex requires review before retrying; PreToolUse ask is unsupported.'
            print(json.dumps({'hookSpecificOutput': {'hookEventName': 'PreToolUse',
                                                     'permissionDecision': verdict,
                                                     'permissionDecisionReason': reason}}))
        return 0
    except (ValueError, TypeError, AttributeError):
        # An unparseable event must not silently disable the credential check.
        print('Android Suite could not inspect this tool event; review input before retrying.', file=sys.stderr)
        return 2


if __name__ == '__main__':
    sys.exit(main())
