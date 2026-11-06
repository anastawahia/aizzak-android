"""Task lifecycle, human checkpoints, approved deviations and design conformance.

The lifecycle record `<records>/tasks/<task>/lifecycle.json` is written only
by these functions. Records establish provenance (who decided what, bound to
which file hashes and source fingerprint); they are not cryptographic proof and
a local user can forge them. They exist so that an agent cannot report a stage,
design match or task completion that the recorded evidence does not support.
"""
from __future__ import annotations

import re
import time
from pathlib import Path

from suite import PLUGIN, SuiteError, digest, gate, read_json, recorded_file, records_child, safe_child, source_fingerprint, write_json

TASK = re.compile(r'[a-z0-9][a-z0-9-]{0,63}')
SCREEN_ID = re.compile(r'[A-Z][A-Z0-9-]{1,47}')
ELEMENT_KINDS = {'header', 'navigation', 'icon', 'illustration', 'image', 'title', 'text', 'label', 'field',
                 'action', 'section', 'panel', 'list', 'card', 'notice', 'indicator', 'spacing', 'chrome', 'other'}
STATUSES = {'matched', 'deviation', 'missing'}
RTL_LOCALES = {'ar', 'fa', 'he', 'iw', 'ur', 'ps', 'sd', 'ug', 'yi', 'ckb'}
CONFIG_KEYS = ('locale', 'theme', 'width_dp', 'font_scale')
DONE = {'passed', 'not_applicable'}


def stage_defs():
    return {s['id']: s for s in read_json(PLUGIN / 'references/lifecycle.json')['stages']}


def record_path(root, task):
    if not isinstance(task, str) or not TASK.fullmatch(task):
        raise SuiteError('Task slug must be lowercase letters, digits and hyphens (max 64).')
    return records_child(root, f'tasks/{task}/lifecycle.json')


def load(root, task):
    file = record_path(root, task)
    if not file.is_file():
        raise SuiteError(f'No lifecycle for task {task}; open it with lifecycle --init --workflow <id>.')
    data = read_json(file)
    if data.get('schema') != 1 or data.get('task') != task or not isinstance(data.get('stages'), dict):
        raise SuiteError('Invalid lifecycle record; it is written only by suite.py.')
    return data


def save(root, task, data):
    write_json(record_path(root, task), data)


def now():
    return time.strftime('%Y-%m-%dT%H:%M:%S%z')


def hashed(root, paths):
    root = Path(root).resolve()
    out = []
    for path in paths:
        file = safe_child(root, path)
        if not file.is_file():
            raise SuiteError(f'Packet/evidence file not found inside the project: {path}')
        out.append({'path': file.relative_to(root).as_posix(), 'sha256': digest(file)})
    return out


def changed(root, files):
    reasons = []
    for item in files:
        file = recorded_file(root, item['path'])
        if not file.is_file() or digest(file) != item['sha256']:
            reasons.append(f'{item["path"]} changed after it was recorded')
    return reasons


def prerequisites(stage, present, defs):
    """Nearest prerequisites that the task actually uses (absent stages are transparent)."""
    found = []
    for required in defs[stage]['requires']:
        if required in present:
            found.append(required)
        else:
            found.extend(p for p in prerequisites(required, present, defs) if p not in found)
    return found


def init(root, task, workflow):
    flows = read_json(PLUGIN / 'workflows/catalog.json')
    if workflow not in flows:
        raise SuiteError('Unknown workflow.')
    flow = flows[workflow]
    if flow['mode'] == 'review' or not flow['stages']:
        raise SuiteError(f'The {workflow} workflow is review-only and has no lifecycle; return results in the conversation.')
    file = record_path(root, task)
    if file.exists():
        raise SuiteError('This task already has a lifecycle; resume it instead of reopening it.')
    data = {'schema': 1, 'task': task, 'workflow': workflow, 'created': now(), 'sequence': 0,
            'mandatory': flow['mandatory'], 'stages': {s: {'state': 'pending'} for s in flow['stages']},
            'deviations': {}, 'history': [{'at': now(), 'event': 'opened', 'workflow': workflow}]}
    save(root, task, data)
    return evaluate(root, data)


def evaluate(root, data):
    root = Path(root).resolve()
    defs = stage_defs()
    present = list(data['stages'])
    current = source_fingerprint(root)
    effective, rows = {}, []
    for sid in present:
        entry = data['stages'][sid]
        state, reasons = entry['state'], []
        if state == 'passed':
            if defs[sid].get('source_bound') and entry.get('source_sha256') != current:
                reasons.append('source changed after this stage passed')
            reasons += changed(root, entry.get('packet', []) + entry.get('evidence', []))
            for p in prerequisites(sid, present, defs):
                if effective[p] not in DONE:
                    reasons.append(f'prerequisite {p} is {effective[p]}')
                elif data['stages'][p].get('seq', 0) > entry.get('seq', 0):
                    reasons.append(f'prerequisite {p} was decided again after this stage')
        effective[sid] = 'stale' if reasons else state
        row = {'stage': sid, 'kind': defs[sid]['kind'], 'owner': defs[sid]['owner'], 'state': state,
               'effective': effective[sid]}
        if reasons:
            row['stale_reasons'] = reasons
        if state == 'not_applicable':
            row['reason'] = entry.get('reason')
        rows.append(row)
    ready = [sid for sid in present if effective[sid] not in DONE
             and all(effective[p] in DONE for p in prerequisites(sid, present, defs))]
    return {'status': 'passed', 'task': data['task'], 'workflow': data['workflow'], 'stages': rows,
            'next_stage': ready[0] if ready else None,
            'completed': all(effective[s] in DONE for s in present),
            'open_deviations_approved': sorted(data['deviations']), 'source_sha256': current,
            'limitations': 'Provenance record, not cryptographic proof. Human decisions are recorded as reported by the director.'}


def status(root, task):
    return evaluate(root, load(root, task))


def require_ready(root, data, sid):
    defs = stage_defs()
    if sid not in data['stages']:
        raise SuiteError(f'Stage {sid} is not part of the {data["workflow"]} workflow.')
    view = {row['stage']: row['effective'] for row in evaluate(root, data)['stages']}
    blocked = [f'{p} ({view[p]})' for p in prerequisites(sid, list(data['stages']), defs) if view[p] not in DONE]
    if blocked:
        raise SuiteError(f'Stage {sid} cannot start before: {", ".join(blocked)}.')


def mark(data, sid, entry, event):
    data['sequence'] += 1
    entry['seq'] = data['sequence']
    data['stages'][sid] = entry
    data['history'].append({'at': entry['at'], 'event': event, 'stage': sid, 'state': entry['state']})


def validate_inventory(file):
    errors = []
    try:
        inventory = read_json(file)
    except (ValueError, OSError) as exc:
        return [f'Inventory unreadable: {exc}']
    if inventory.get('schema') != 1:
        errors.append('Inventory schema must be 1.')
    if not inventory.get('reference'):
        errors.append('Inventory must name its design reference files.')
    configs = inventory.get('required_configs') or []
    for config in configs:
        if (not isinstance(config, dict) or set(config) != set(CONFIG_KEYS) or config['theme'] not in {'light', 'dark'}
                or not isinstance(config['width_dp'], int) or not isinstance(config['font_scale'], (int, float))):
            errors.append(f'Invalid screenshot configuration: {config}')
    valid = [c for c in configs if isinstance(c, dict) and set(c) == set(CONFIG_KEYS)]
    if {c['theme'] for c in valid} != {'light', 'dark'}:
        errors.append('Required configurations must cover both light and dark themes.')
    if not any(isinstance(c['font_scale'], (int, float)) and c['font_scale'] >= 1.3 for c in valid):
        errors.append('Required configurations must include a large font scale (1.3 or more).')
    if inventory.get('adaptive', True) and not any(isinstance(c['width_dp'], int) and c['width_dp'] >= 600 for c in valid):
        errors.append('Adaptive apps need a configuration of at least 600dp width (set "adaptive": false otherwise).')
    locales = set(inventory.get('locales') or [])
    if locales & RTL_LOCALES and not any(str(c['locale']).split('-')[0] in RTL_LOCALES for c in valid):
        errors.append('An RTL locale is supported, so an RTL configuration is required.')
    screens, seen = inventory.get('screens') or [], set()
    if not screens:
        errors.append('Inventory has no screens.')
    for screen in screens:
        sid = screen.get('id', '')
        if not SCREEN_ID.fullmatch(str(sid)) or sid in seen:
            errors.append(f'Invalid or duplicate screen ID: {sid}')
        seen.add(sid)
        if not screen.get('elements'):
            errors.append(f'{sid}: no elements listed.')
        for element in screen.get('elements') or []:
            eid = element.get('id', '')
            if not SCREEN_ID.fullmatch(str(eid)) or eid in seen:
                errors.append(f'Invalid or duplicate element ID: {eid}')
            seen.add(eid)
            if element.get('kind') not in ELEMENT_KINDS:
                errors.append(f'{eid}: unknown element kind {element.get("kind")}')
            if not str(element.get('description', '')).strip():
                errors.append(f'{eid}: description required.')
    return errors


def inventory_check(file):
    errors = validate_inventory(file)
    return {'status': 'failed' if errors else 'passed', 'errors': errors}


def checkpoint(root, task, sid, packet, decision, ref):
    data = load(root, task)
    defs = stage_defs()
    if sid not in defs or defs[sid]['kind'] != 'human':
        raise SuiteError(f'{sid} is not a human checkpoint.')
    if decision not in {'approved', 'changes_requested'}:
        raise SuiteError('Decision must be approved or changes_requested.')
    if not str(ref or '').strip():
        raise SuiteError("Record the owner's exact decision and date with --ref; silence is never approval.")
    if not packet:
        raise SuiteError('A checkpoint records the packet the owner reviewed; pass --packet for each file.')
    require_ready(root, data, sid)
    files = hashed(root, packet)
    if decision == 'approved' and defs[sid].get('inventory'):
        inventories = [f for f in files if f['path'].endswith('screen-inventory.json')]
        if len(inventories) != 1 or len(files) < 2:
            raise SuiteError('Design approval needs the visual reference and exactly one screen-inventory.json in the packet.')
        problems = validate_inventory(safe_child(root, inventories[0]['path']))
        if problems:
            raise SuiteError('Screen inventory is invalid: ' + '; '.join(problems))
    if decision == 'approved' and defs[sid].get('requires_report'):
        required = defs[sid]['requires_report']
        if required in data['stages'] and data['stages'][required]['state'] == 'passed':
            files += data['stages'][required].get('evidence', [])
    entry = {'state': 'passed' if decision == 'approved' else 'changes_requested', 'decision': decision,
             'ref': ref, 'packet': files, 'at': now()}
    if sid == 'design' and decision == 'approved':
        data['deviations'] = {}
    mark(data, sid, entry, 'checkpoint')
    save(root, task, data)
    return evaluate(root, data)


def approved_design(root, data):
    entry = data['stages'].get('design')
    if not entry or entry['state'] != 'passed':
        raise SuiteError('No approved design in this task; design QA needs the owner-approved reference and inventory.')
    inventory = next(f for f in entry['packet'] if f['path'].endswith('screen-inventory.json'))
    references = [f for f in entry['packet'] if f is not inventory]
    return inventory, references


def deviation(root, task, element, reason, ref):
    data = load(root, task)
    inventory, _ = approved_design(root, data)
    ids = {e['id'] for s in read_json(recorded_file(root, inventory['path']))['screens'] for e in s['elements']}
    if element not in ids:
        raise SuiteError(f'{element} is not an element of the approved inventory.')
    if not str(reason or '').strip() or not str(ref or '').strip():
        raise SuiteError("A deviation needs its reason and the owner's exact approval (--ref).")
    data['deviations'][element] = {'reason': reason, 'ref': ref, 'at': now(), 'inventory_sha256': inventory['sha256']}
    data['history'].append({'at': now(), 'event': 'deviation-approved', 'element': element})
    save(root, task, data)
    return {'status': 'passed', 'task': task, 'element': element, 'approved_deviations': sorted(data['deviations'])}


def conformance(root, task, record_file):
    root = Path(root).resolve()
    data = load(root, task)
    inventory_entry, references = approved_design(root, data)
    errors = []
    view = {row['stage']: row for row in evaluate(root, data)['stages']}
    locked = not changed(root, [inventory_entry] + references)
    if not locked or view['design']['effective'] != 'passed':
        errors.append('reference_locked: the approved reference or inventory changed, or the design approval is stale.')
    inventory = read_json(recorded_file(root, inventory_entry['path']))
    record = read_json(record_file)
    current = source_fingerprint(root)
    if record.get('task') != task:
        errors.append('Record belongs to a different task.')
    if record.get('inventory_sha256') != inventory_entry['sha256']:
        errors.append('Record was not made against the approved inventory.')
    if record.get('source_sha256') != current:
        errors.append('screenshots_current: record source fingerprint differs from the current source.')

    reviewer = record.get('reviewer') or {}
    implementers = record.get('implementers') or []
    independent = (reviewer.get('role') == 'design-qa-reviewer' and str(reviewer.get('id', '')).strip()
                   and implementers and all(str(i.get('id', '')).strip() for i in implementers)
                   and reviewer['id'] not in {i['id'] for i in implementers}
                   and 'design-qa-reviewer' not in {i.get('role') for i in implementers})
    if not independent:
        errors.append('independent_design_review: reviewer must be a design-qa-reviewer distinct from every named implementer.')

    configs = [tuple(c[k] for k in CONFIG_KEYS) for c in inventory['required_configs']]
    reviewed = {s.get('id'): s for s in record.get('screens', [])}
    counts = {status: 0 for status in STATUSES}
    complete = shots_ok = True
    for extra in set(reviewed) - {s['id'] for s in inventory['screens']}:
        errors.append(f'{extra}: screen is not in the approved inventory.')
    for screen in inventory['screens']:
        sid = screen['id']
        if sid not in reviewed:
            errors.append(f'inventory_complete: screen {sid} is absent from the record.')
            complete = False
            continue
        judged = {e.get('id'): e for e in reviewed[sid].get('elements', [])}
        for extra in set(judged) - {e['id'] for e in screen['elements']}:
            errors.append(f'{sid}: element {extra} is not in the approved inventory.')
        for element in screen['elements']:
            eid = element['id']
            status = judged.get(eid, {}).get('status')
            if status not in STATUSES:
                errors.append(f'inventory_complete: {eid} has no matched/deviation/missing judgment.')
                complete = False
                continue
            counts[status] += 1
            if status == 'missing':
                errors.append(f'{eid} is missing: {element["description"]}')
            elif status == 'deviation' and eid not in data['deviations']:
                errors.append(f'{eid} deviates without owner approval (record it with suite.py deviation).')
        shots = reviewed[sid].get('screenshots', [])
        covered = set()
        for shot in shots:
            try:
                file = recorded_file(root, shot['path'])
                if not file.is_file() or digest(file) != shot.get('sha256'):
                    errors.append(f'screenshots_current: {sid} screenshot {shot["path"]} is missing or changed.')
                    shots_ok = False
                    continue
                covered.add(tuple(shot['config'][k] for k in CONFIG_KEYS))
            except (KeyError, TypeError, SuiteError):
                errors.append(f'{sid}: malformed screenshot entry.')
                shots_ok = False
        for config in configs:
            if config not in covered:
                errors.append(f'screenshots_current: {sid} has no screenshot for {dict(zip(CONFIG_KEYS, config))}.')
                shots_ok = False
    checks = {'reference_locked': locked, 'inventory_complete': complete and not any(
              e.startswith(('inventory_complete', 'Record')) for e in errors),
              'screenshots_current': shots_ok and record.get('source_sha256') == current,
              'independent_design_review': bool(independent)}
    return {'stage': 'design_conformance', 'task': task, 'status': 'failed' if errors else 'passed',
            'errors': errors, 'checks': checks, 'summary': counts,
            'approved_deviations': sorted(data['deviations']), 'source_sha256': current,
            'inventory_sha256': inventory_entry['sha256'], 'record': Path(record_file).resolve().as_posix(),
            'record_sha256': digest(record_file),
            'limitations': ('Element judgments are the reviewer\'s; suite.py verifies completeness, approvals, '
                            'screenshot provenance, independence claims and freshness, not visual similarity.')}


def advance(root, task, sid, evidence=None, record=None, note=None, not_applicable=None):
    root = Path(root).resolve()
    data = load(root, task)
    defs = stage_defs()
    if sid not in data['stages']:
        raise SuiteError(f'Stage {sid} is not part of the {data["workflow"]} workflow.')
    kind = defs[sid]['kind']
    if not_applicable is not None:
        if not str(not_applicable).strip():
            raise SuiteError('Give the reason the stage does not apply.')
        if sid in data['mandatory'] or not defs[sid]['skippable']:
            raise SuiteError(f'{sid} is mandatory in the {data["workflow"]} workflow.')
        if sid == 'design-qa' and data['stages'].get('design', {}).get('state') == 'passed':
            raise SuiteError('Design QA cannot be skipped: the design stage approved a screen inventory.')
        require_ready(root, data, sid)
        mark(data, sid, {'state': 'not_applicable', 'reason': not_applicable, 'at': now()}, 'not-applicable')
        save(root, task, data)
        return evaluate(root, data)
    if kind == 'human':
        raise SuiteError(f'{sid} is a human checkpoint; record the owner decision with suite.py checkpoint.')
    require_ready(root, data, sid)
    entry = {'state': 'passed', 'at': now(), 'source_sha256': source_fingerprint(root)}
    if kind == 'work':
        if not str(note or '').strip():
            raise SuiteError('Record the accepted handoff with --note (what was accepted and where).')
        entry['note'] = note
    else:
        if kind == 'conformance':
            if not record:
                raise SuiteError('Design QA advances only with --record <conformance record>.')
            report = conformance(root, task, record)
        else:
            if not evidence:
                raise SuiteError(f'{sid} advances only with --evidence <gate evidence for {defs[sid]["gate"]}>.')
            report = gate(root, evidence, defs[sid]['gate'])
        report_file = records_child(root, f'tasks/{task}/reports/{sid}.json')
        write_json(report_file, report)
        if report['status'] != 'passed':
            return {**report, 'advanced': False, 'report_file': report_file.as_posix()}
        entry['evidence'] = hashed(root, [report_file.relative_to(root).as_posix()])
    mark(data, sid, entry, 'advanced')
    save(root, task, data)
    return {**evaluate(root, data), 'advanced': sid}
