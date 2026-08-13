from pathlib import Path
import re

BASE = "https://www.toolboxapp.cc"
ALT_RE = re.compile(r'''\s*<link\b(?=[^>]*\brel=["']alternate["'])(?=[^>]*\bhreflang=["'](?:en|es|x-default)["'])[^>]*>\s*''', re.I)
CANON_RE = re.compile(r'''<link\b(?=[^>]*\brel=["']canonical["'])[^>]*>''', re.I)
ES_ALT_RE = re.compile(r'''<link\b(?=[^>]*\brel=["']alternate["'])(?=[^>]*\bhreflang=["']es["'])[^>]*>''', re.I)

def update(path, en_url, es_url):
    p = Path(path)
    text = p.read_text(encoding="utf-8")
    text = ALT_RE.sub("\n", text)
    block = (
        f'\n  <link rel="alternate" hreflang="en" href="{en_url}" />'
        f'\n  <link rel="alternate" hreflang="es" href="{es_url}" />'
        f'\n  <link rel="alternate" hreflang="x-default" href="{en_url}" />'
    )
    m = CANON_RE.search(text)
    if not m:
        raise SystemExit(f"Missing canonical: {path}")
    text = text[:m.end()] + block + text[m.end():]
    p.write_text(text, encoding="utf-8")

spanish_tools = sorted(p.parent.name for p in Path("es/calculator").glob("*/index.html"))
if len(spanish_tools) != 1000:
    raise SystemExit(f"Expected 1000 Spanish tools, found {len(spanish_tools)}")

targets = []
for slug in spanish_tools:
    en_path = Path(f"calculator/{slug}/index.html")
    if not en_path.exists():
        raise SystemExit(f"Missing English page: {en_path}")
    text = en_path.read_text(encoding="utf-8")
    if not ES_ALT_RE.search(text):
        targets.append(slug)

if len(targets) != 600:
    raise SystemExit(f"Expected exactly 600 English pages without Spanish hreflang, found {len(targets)}")

for slug in targets:
    update(
        f"calculator/{slug}/index.html",
        f"{BASE}/calculator/{slug}/",
        f"{BASE}/es/calculator/{slug}/",
    )

print("TOTAL_UPDATED", len(targets))
