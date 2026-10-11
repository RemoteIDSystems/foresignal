#!/usr/bin/env python3
"""Export the EDP JSON Schemas from docs/OpenAPI.json into docs/protocol/edp-1.0/.

Usage: python3 scripts/export-edp-schemas.py docs/OpenAPI.json docs/protocol/edp-1.0
The files are derived, never hand-edited (docs/External-Data-Protocol.md).
"""
import copy, json, pathlib, sys

spec = json.load(open(sys.argv[1]))
out = pathlib.Path(sys.argv[2]); out.mkdir(parents=True, exist_ok=True)
S = spec['components']['schemas']
ROOTS = {'manifest': 'EdpManifest', 'batch': 'EdpBatch', 'item': 'EdpItem', 'feature': 'EdpFeatureItem',
         'event': 'EdpEventItem', 'observation': 'EdpObservationItem', 'document': 'EdpDocumentItem',
         'forecast': 'EdpForecastItem', 'delete': 'EdpDeleteItem', 'status': 'EdpStatusItem'}

def refs(o, acc):
    if isinstance(o, dict):
        for k, v in o.items():
            if k == '$ref' and isinstance(v, str) and v.startswith('#/components/schemas/'):
                n = v.split('/')[-1]
                if n not in acc:
                    acc.add(n); refs(S[n], acc)
            else:
                refs(v, acc)
    elif isinstance(o, list):
        for v in o: refs(v, acc)

def rewrite(o):
    if isinstance(o, dict):
        return {k: (v.replace('#/components/schemas/', '#/$defs/') if k == '$ref' and isinstance(v, str) else rewrite(v))
                for k, v in o.items() if k != 'discriminator'}
    if isinstance(o, list): return [rewrite(v) for v in o]
    return o

for name, root in ROOTS.items():
    acc = set(); refs(S[root], acc); acc.discard(root)
    doc = {'$schema': 'https://json-schema.org/draft/2020-12/schema',
           '$id': f'https://foresignal.ai/protocol/edp-1.0/{name}.schema.json',
           'title': f'Foresignal EDP 1.0 {name}', **rewrite(copy.deepcopy(S[root]))}
    if acc: doc['$defs'] = {n: rewrite(copy.deepcopy(S[n])) for n in sorted(acc)}
    (out / f'{name}.schema.json').write_text(json.dumps(doc, indent=2, ensure_ascii=False) + '\n')
print(f'exported {len(ROOTS)} schemas to {out}')
