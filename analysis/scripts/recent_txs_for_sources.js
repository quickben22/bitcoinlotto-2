const fs = require('fs');
const path = require('path');
const d = process.cwd();
const z = JSON.parse(fs.readFileSync(path.join(d, 'zombies.json'), 'utf8'));
const h = JSON.parse(fs.readFileSync(path.join(d, 'histories.json'), 'utf8'));
const targets = process.argv.slice(2);
for (const t of targets) {
  const i = z.findIndex(x => x.a === t);
  console.log('===', t, 'index', i);
  console.log((h[i] || []).filter(x => x.h >= 974000).map(x => x.h + ' ' + x.tx).join('\n'));
}
