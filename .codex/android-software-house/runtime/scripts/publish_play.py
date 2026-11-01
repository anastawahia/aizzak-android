#!/usr/bin/env python3
"""Explicit Google Play edit/upload/track/validate/commit; no bundled credentials.

Dry-run is the default. Obtain a short-lived OAuth access token with Android
Publisher scope through an approved provider. Set PLAY_ACCESS_TOKEN in the
process environment, never in CLI args or checked-in files. No external write
is made before artifact-bound approval and the current release gate pass.
"""
from __future__ import annotations
import argparse
import json
from pathlib import Path
import re
import os
import sys
import time
import urllib.error
import urllib.parse
import urllib.request

from suite import SuiteError, audit_release, digest, gate, read_json, redact, write_json

API = 'https://androidpublisher.googleapis.com/androidpublisher/v3/applications/'
UPLOAD = 'https://androidpublisher.googleapis.com/upload/androidpublisher/v3/applications/'


def check_approval(data, package, track, sha256, version_code, rollout):
    expected = {'package': package, 'track': track, 'sha256': sha256,
                'version_code': version_code, 'rollout': rollout, 'action': 'commit-google-play-edit'}
    if any(data.get(k) != v for k, v in expected.items()):
        raise SuiteError('Approval does not match the exact artifact, package, track, version and rollout.')
    now = int(time.time())
    if not isinstance(data.get('approved_at'), int) or not now - 3600 <= data['approved_at'] <= now:
        raise SuiteError('Approval must be explicitly recorded within the last hour.')
    if not data.get('approved_by') or not data.get('authorization_reference'):
        raise SuiteError('Approval must identify a human and the explicit authorization reference. Never self-authorize.')


def request(method, url, token, body=None, binary=False):
    headers = {'Authorization': 'Bearer ' + token, 'Content-Type': 'application/octet-stream' if binary else 'application/json'}
    if body is not None and not binary:
        body = json.dumps(body).encode('utf-8')
    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=180) as response:
            return json.load(response) if response.status != 204 else {}
    except urllib.error.HTTPError as exc:
        # Responses can contain sensitive request data; expose status only.
        raise SuiteError(f'Google Play returned HTTP {exc.code}; inspect Play Console. No mutation is automatically retried.') from None


def publish(args):
    if not re.fullmatch(r'[a-z][a-z0-9_]*(?:\.[a-z][a-z0-9_]*)+', args.package):
        raise SuiteError('Invalid application ID.')
    if not re.fullmatch(r'[A-Za-z][A-Za-z0-9_-]{0,79}', args.track):
        raise SuiteError('Track must be internal, alpha, beta, production, or an existing safe custom track ID.')
    if args.version_code < 1 or not 0 < args.rollout <= 1:
        raise SuiteError('Positive versionCode and rollout in (0, 1] are required.')
    bundle = Path(args.bundle).resolve(strict=True)
    sha = digest(bundle)
    plan = {'package': args.package, 'track': args.track, 'version_code': args.version_code,
            'sha256': sha, 'rollout': args.rollout, 'published': False, 'dry_run': not args.execute}
    if not args.execute:
        return plan
    if not args.approval or not args.evidence:
        raise SuiteError('--execute requires --approval and --evidence; dry-run has no external effects.')
    check_approval(read_json(args.approval), args.package, args.track, sha, args.version_code, args.rollout)
    release_gate = gate(args.project, args.evidence, 'release')
    if release_gate['status'] != 'passed':
        raise SuiteError('Release gate failed: ' + '; '.join(release_gate['errors']))
    signature = audit_release(args.project, bundle)
    if not signature['signature_verified']:
        raise SuiteError('Bundle signature verification failed.')
    token = os.environ.get('PLAY_ACCESS_TOKEN')
    if not token:
        raise SuiteError('PLAY_ACCESS_TOKEN is not configured; use an approved OAuth credential provider.')
    if bundle.stat().st_size > 512 * 1024 * 1024:
        raise SuiteError('This uploader supports bundles up to 512 MiB; use the official resumable upload API for larger bundles.')
    app = urllib.parse.quote(args.package, safe='')
    edit_id = None
    committed = False
    try:
        edit_id = request('POST', API + app + '/edits', token, {})['id']
        edit = API + app + '/edits/' + urllib.parse.quote(edit_id, safe='')
        uploaded = request('POST', UPLOAD + app + '/edits/' + urllib.parse.quote(edit_id, safe='') + '/bundles?uploadType=media', token, bundle.read_bytes(), True)
        if int(uploaded.get('versionCode', -1)) != args.version_code or uploaded.get('sha256', '').lower() != sha:
            raise SuiteError('Uploaded bundle identity differs from the approved artifact.')
        release = {'versionCodes': [str(args.version_code)], 'status': 'completed' if args.rollout == 1 else 'inProgress'}
        if args.rollout < 1:
            release['userFraction'] = args.rollout
        request('PUT', edit + '/tracks/' + urllib.parse.quote(args.track, safe=''), token, {'track': args.track, 'releases': [release]})
        request('POST', edit + ':validate', token, {})
        request('POST', edit + ':commit', token, {})
        committed = True
        # Read after commit is distinct from approval/review/public availability.
        plan.update(dry_run=False, edit_committed=True, edit_id=edit_id, uploaded_version_code=args.version_code,
                    publication_status='submitted; verify review and serving status in Play Console', published=False)
        return plan
    finally:
        if edit_id and not committed:
            try:
                request('DELETE', API + app + '/edits/' + urllib.parse.quote(edit_id, safe=''), token)
            except (SuiteError, OSError):
                print('Could not discard the uncommitted edit. Inspect Play Console before retrying.', file=sys.stderr)


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--project', default='.')
    parser.add_argument('--bundle', required=True)
    parser.add_argument('--package', required=True)
    parser.add_argument('--track', default='internal')
    parser.add_argument('--version-code', type=int, required=True)
    parser.add_argument('--rollout', type=float, default=1.0)
    parser.add_argument('--execute', action='store_true')
    parser.add_argument('--approval')
    parser.add_argument('--evidence')
    parser.add_argument('--report')
    args = parser.parse_args(argv)
    try:
        result = publish(args)
        if args.report: write_json(args.report, result)
        print(json.dumps(result, indent=2))
        return 0
    except (SuiteError, OSError, ValueError, KeyError) as exc:
        print(json.dumps({'status': 'failed', 'error': redact(exc), 'published': False}), file=sys.stderr)
        return 2


if __name__ == '__main__':
    sys.exit(main())
