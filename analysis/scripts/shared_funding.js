const fs = require('fs');
const path = require('path');
const d = process.cwd();
const chains = JSON.parse(fs.readFileSync(path.join(d, 'chains.json'), 'utf8'));
const zombies = JSON.parse(fs.readFileSync(path.join(d, 'zombies.json'), 'utf8'));
const histories = JSON.parse(fs.readFileSync(path.join(d, 'histories.json'), 'utf8'));
const zIndex = new Map(zombies.map((z, i) => [z.a, i]));
const m = new Map();
for (const r of chains) {
  const i = zIndex.get(r.source);
  for (const e of (histories[i] || [])) {
    if (e.h >= 975000) continue;
    if (!m.has(e.tx)) m.set(e.tx, new Set());
    m.get(e.tx).add(r.source.slice(0, 8));
  }
}
const shared = [...m.entries()].filter(([, s]) => s.size > 1).sort((a, b) => b[1].size - a[1].size);
console.log('shared pre-window txs:', shared.length);
for (const [tx, s] of shared.slice(0, 30)) console.log(tx, [...s].join(' '));
