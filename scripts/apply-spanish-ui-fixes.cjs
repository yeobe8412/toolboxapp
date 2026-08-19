const fs = require('fs');
const path = require('path');

const root = process.cwd();
const out = path.join(root, 'build');

function copySite(src, dst, relative = '') {
  fs.mkdirSync(dst, { recursive: true });
  for (const entry of fs.readdirSync(src, { withFileTypes: true })) {
    if (!relative && ['.git', '.github', 'build'].includes(entry.name)) continue;
    const rel = relative ? path.join(relative, entry.name) : entry.name;
    if (rel === 'vercel.json' || rel === path.join('scripts', 'apply-spanish-ui-fixes.cjs')) continue;
    const from = path.join(src, entry.name);
    const to = path.join(dst, entry.name);
    if (entry.isDirectory()) copySite(from, to, rel);
    else if (entry.isFile()) fs.copyFileSync(from, to);
  }
}

fs.rmSync(out, { recursive: true, force: true });
copySite(root, out);

const calcRoot = path.join(out, 'es', 'calculator');
const dirs = fs.readdirSync(calcRoot, { withFileTypes: true })
  .filter(d => d.isDirectory() && d.name !== 'categories' && fs.existsSync(path.join(calcRoot, d.name, 'index.html')));

if (dirs.length !== 3500) {
  throw new Error(`Expected 3500 Spanish calculator pages, found ${dirs.length}`);
}

let pageChanges = 0;
let schemaChanges = 0;
let visibleChanges = 0;

const schemaRoot = /(\"name\"\s*:\s*\"Inicio\"\s*,\s*\"item\"\s*:\s*\")https:\/\/www\.toolboxapp\.cc\/(\"\s*})/g;
const visibleRoot = /(<div class=\"breadcrumb\">\s*)<a href=\"\/\">Inicio<\/a>/g;

for (const dir of dirs) {
  const file = path.join(calcRoot, dir.name, 'index.html');
  const original = fs.readFileSync(file, 'utf8');
  let next = original;
  let nSchema = 0;
  let nVisible = 0;
  next = next.replace(schemaRoot, (...args) => { nSchema += 1; return `${args[1]}https://www.toolboxapp.cc/es/${args[2]}`; });
  next = next.replace(visibleRoot, (...args) => { nVisible += 1; return `${args[1]}<a href=\"/es/\">Inicio</a>`; });
  schemaChanges += nSchema;
  visibleChanges += nVisible;
  if (next !== original) {
    fs.writeFileSync(file, next, 'utf8');
    pageChanges += 1;
  }
}

let correctSchema = 0;
let correctVisible = 0;
let remainingSchema = 0;
let remainingVisible = 0;
for (const dir of dirs) {
  const text = fs.readFileSync(path.join(calcRoot, dir.name, 'index.html'), 'utf8');
  correctSchema += (text.match(/\"name\"\s*:\s*\"Inicio\"\s*,\s*\"item\"\s*:\s*\"https:\/\/www\.toolboxapp\.cc\/es\/\"/g) || []).length;
  correctVisible += (text.match(/<div class=\"breadcrumb\">\s*<a href=\"\/es\/\">Inicio<\/a>/g) || []).length;
  remainingSchema += (text.match(schemaRoot) || []).length;
  remainingVisible += (text.match(visibleRoot) || []).length;
}
if (remainingSchema || remainingVisible || correctSchema !== 3500 || correctVisible !== 3500) {
  throw new Error(`Breadcrumb validation failed: schema=${correctSchema}, visible=${correctVisible}, remainingSchema=${remainingSchema}, remainingVisible=${remainingVisible}`);
}

const homeFile = path.join(out, 'es', 'index.html');
let home = fs.readFileSync(homeFile, 'utf8');
const oldFavorites = `      favoritesList.innerHTML = saved.map(tool => \`\n        <a class=\"favorite-card\" href=\"\${escapeHtml(tool.url)}\">\n          <span class=\"favorite-name\">\${escapeHtml(tool.name)}</span>\n          <span class=\"favorite-star\" aria-hidden=\"true\">★</span>\n        </a>\n      \`).join(\"\");`;
const newFavorites = `      favoritesList.innerHTML = saved.map(tool => {\n        const localizedTool = tools.find(item => item.url === tool.url);\n        const displayName = localizedTool?.name || tool.name;\n        return \`\n          <a class=\"favorite-card\" href=\"\${escapeHtml(tool.url)}\">\n            <span class=\"favorite-name\">\${escapeHtml(displayName)}</span>\n            <span class=\"favorite-star\" aria-hidden=\"true\">★</span>\n          </a>\n        \`;\n      }).join(\"\");`;

if (!home.includes(oldFavorites)) throw new Error('Spanish favorites render block did not match expected source');
home = home.replace(oldFavorites, newFavorites);
if ((home.match(/const localizedTool = tools\.find\(item => item\.url === tool\.url\);/g) || []).length !== 1) {
  throw new Error('Favorites localization validation failed');
}
fs.writeFileSync(homeFile, home, 'utf8');

console.log(`Spanish calculator pages changed: ${pageChanges}`);
console.log(`Breadcrumb schema links changed: ${schemaChanges}`);
console.log(`Breadcrumb visible links changed: ${visibleChanges}`);
console.log('Favorites localized: es/index.html');
