#!/usr/bin/env python3
"""Build a marked demo with a disposable local signing key, then destroy the key.

This tests signing mechanics only, not Play upload-certificate matching or release
eligibility. It cannot target arbitrary product projects and never publishes.
Passwords are random, remain in child process environment, and are never printed.
"""
import argparse
import os
from pathlib import Path
import secrets
import shutil
import sys
import tempfile

from suite import SuiteError, audit_release, run, safe_child, verify, write_json


def smoke(project, jdk=None, execute=False):
    project = Path(project).resolve(strict=True)
    marker = project / '.suite-demo'
    if not marker.is_file() or marker.read_text(encoding='utf-8').strip() != 'android-development-suite-demo-v1':
        raise SuiteError('Signing smoke tests accept only a marked suite demo.')
    if not execute:
        return {'status': 'blocked', 'reason': 'Pass --execute to create a disposable test key and build the local demo.', 'published': False}
    compiler = shutil.which('javac')
    jdk = Path(jdk).resolve() if jdk else Path(compiler).resolve().parents[1] if compiler else None
    keytool = jdk / 'bin' / ('keytool.exe' if os.name == 'nt' else 'keytool') if jdk else None
    if not keytool or not keytool.is_file():
        raise SuiteError('A complete JDK with keytool is required; supply --jdk if not discoverable.')
    evidence = project / '.android-suite/evidence'
    private = safe_child(project, '.android-suite/evidence/private')
    private.mkdir(parents=True, exist_ok=True)
    directory = Path(tempfile.mkdtemp(prefix='signing-smoke-', dir=private))
    key = directory / 'throwaway-upload.jks'
    names = ['JAVA_HOME', 'ANDROID_KEYSTORE_PATH', 'ANDROID_KEYSTORE_PASSWORD', 'ANDROID_KEY_ALIAS', 'ANDROID_KEY_PASSWORD']
    original = {name: os.environ.get(name) for name in names}
    password = secrets.token_urlsafe(32)
    os.environ.update(JAVA_HOME=str(jdk), ANDROID_KEYSTORE_PATH=str(key), ANDROID_KEYSTORE_PASSWORD=password,
                      ANDROID_KEY_ALIAS='suite-test-upload', ANDROID_KEY_PASSWORD=password)
    try:
        generated = run([str(keytool), '-genkeypair', '-noprompt', '-storetype', 'JKS', '-keystore', str(key),
                         '-alias', 'suite-test-upload', '-keyalg', 'RSA', '-keysize', '3072', '-validity', '1',
                         '-dname', 'CN=Android Suite Local Test', '-storepass:env', 'ANDROID_KEYSTORE_PASSWORD',
                         '-keypass:env', 'ANDROID_KEY_PASSWORD'], project, 60)
        if generated['exit_code'] != 0:
            raise SuiteError('Disposable key generation failed: ' + generated['output'])
        build = verify(project, 'release', timeout=600)
        write_json(evidence / 'test-signing-build.json', build)
        bundles = [a for a in build['artifacts'] if a['path'].endswith('.aab')]
        if build['status'] != 'passed' or not bundles:
            return {'status': 'failed', 'build': build, 'published': False, 'test_key_only': True}
        audit = audit_release(project, bundles[-1]['path'])
        write_json(evidence / 'test-signing-audit.json', audit)
        return {'status': 'passed' if audit['signature_verified'] else 'failed', 'test_key_only': True,
                'bundle': audit['bundle'], 'sha256': audit['sha256'], 'signature_verified': audit['signature_verified'],
                'play_certificate_matched': False, 'release_gate_passed': False, 'published': False}
    finally:
        # Delete only this generated file and empty temporary directory; never
        # recursively delete a computed user path or reuse a production keystore.
        try:
            if key.exists(): key.unlink()
            if directory.exists(): directory.rmdir()
        finally:
            for name, value in original.items():
                if value is None: os.environ.pop(name, None)
                else: os.environ[name] = value


if __name__ == '__main__':
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('--project', required=True)
    p.add_argument('--jdk')
    p.add_argument('--execute', action='store_true')
    p.add_argument('--report')
    args = p.parse_args()
    try:
        result = smoke(args.project, args.jdk, args.execute)
        if args.report: write_json(args.report, result)
        import json
        print(json.dumps(result, indent=2))
        sys.exit(0 if result['status'] == 'passed' else 1)
    except (SuiteError, OSError, ValueError) as exc:
        print(str(exc), file=sys.stderr)
        sys.exit(2)
