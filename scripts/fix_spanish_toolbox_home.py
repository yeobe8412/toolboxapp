from pathlib import Path

root = Path('es/calculator')
changed = []
old = '<a class="brand" href="/">toolbox</a>'
new = '<a class="brand" href="/es/">toolbox</a>'

for path in sorted(root.glob('*/index.html')):
    text = path.read_text(encoding='utf-8')
    if old in text:
        updated = text.replace(old, new, 1)
        path.write_text(updated, encoding='utf-8')
        changed.append(str(path))

if len(changed) != 400:
    raise SystemExit(f'Expected exactly 400 Spanish tool pages, changed {len(changed)}')

print(f'Changed {len(changed)} Spanish tool pages')
