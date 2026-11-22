const fs = require('fs');
const path = require('path');
const d = process.cwd();
const chains = JSON.parse(fs.readFileSync(path.join(d, 'chains.json'), 'utf8'));
const zombies = JSON.parse(fs.readFileSync(path.join(d, 'zombies.json'), 'utf8'));
const histories = JSON.parse(fs.readFileSync(path.join(d, 'histories.json'), 'utf8'));
const zIndex = new Map(zombies.map((z, i) => [z.a, i]));
const txIds = new Map();
for (const r of chains) {
  const i = zIndex.get(r.source);
  const hs = (histories[i] || []).slice().sort((a, b) => a.h - b.h);
  console.log('===', r.source, 'first txs:', hs.slice(0, 4).map(x => `h${x.h}:${x.tx.slice(0, 10)}`).join(' '));
  for (const h of hs.slice(0, 3)) {
    if (!txIds.has(h.tx)) txIds.set(h.tx, new Set());
    txIds.get(h.tx).add(r.source.slice(0, 8));
  }
}
console.log('\nShared early funding txs between sources:');
for (const [tx, srcs] of txIds) if (srcs.size > 1) console.log(tx, [...srcs].join(', '));
fs.writeFileSync(path.join(d, 'early_funding_txs.json'), JSON.stringify([...txIds.keys()]));
