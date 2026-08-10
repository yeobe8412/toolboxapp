from pathlib import Path
import re

BASE = "https://www.toolboxapp.cc"
ALT_RE = re.compile(r'''\s*<link\b(?=[^>]*\brel=["']alternate["'])(?=[^>]*\bhreflang=["'](?:en|es|x-default)["'])[^>]*>\s*''', re.I)
CANON_RE = re.compile(r'''<link\b(?=[^>]*\brel=["']canonical["'])[^>]*>''', re.I)

def update(path, en_url, es_url):
    p = Path(path)
    if not p.exists():
        print("MISSING", path)
        return False
    text = p.read_text(encoding="utf-8")
    text = ALT_RE.sub("\n", text)
    block = (
        f'\n  <link rel="alternate" hreflang="en" href="{en_url}" />'
        f'\n  <link rel="alternate" hreflang="es" href="{es_url}" />'
        f'\n  <link rel="alternate" hreflang="x-default" href="{en_url}" />'
    )
    m = CANON_RE.search(text)
    text = text[:m.end()] + block + text[m.end():] if m else text.replace("</head>", block + "\n</head>", 1)
    p.write_text(text, encoding="utf-8")
    print("UPDATED", path)
    return True

spanish_tools = sorted(p.parent.name for p in Path("es/calculator").glob("*/index.html"))
spanish_categories = sorted(p.parent.name for p in Path("es/calculator/categories").glob("*/index.html"))
if len(spanish_tools) != 400:
    raise SystemExit(f"Expected 400 Spanish tools, found {len(spanish_tools)}")
if len(spanish_categories) != 21:
    raise SystemExit(f"Expected 21 Spanish categories, found {len(spanish_categories)}")

count = update("index.html", BASE + "/", BASE + "/es/")
for cat in spanish_categories:
    count += update(f"categories/{cat}/index.html", f"{BASE}/categories/{cat}/", f"{BASE}/es/calculator/categories/{cat}/")
for slug in spanish_tools:
    count += update(f"calculator/{slug}/index.html", f"{BASE}/calculator/{slug}/", f"{BASE}/es/calculator/{slug}/")
if count != 422:
    raise SystemExit(f"Expected 422 updates, got {count}")
print("TOTAL_UPDATED", count)
