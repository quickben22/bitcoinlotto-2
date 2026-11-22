const fs = require('fs');
const path = require('path');
const chains = JSON.parse(fs.readFileSync(path.join(process.cwd(), 'chains.json'), 'utf8'));
const dests = new Map();
let txs = 0, withSmall = 0;
for (const r of chains) {
  for (const n of r.chain) {
    if (!n.nextOutputs) continue;
    txs++;
    const outs = n.nextOutputs;
    if (outs.length > 1) withSmall++;
    for (const o of outs) {
      const e = dests.get(o.a) || { count: 0, btc: 0, sources: new Set() };
      e.count++; e.btc += o.v; e.sources.add(r.source.slice(0, 8));
      dests.set(o.a, e);
    }
  }
}
console.log('spending txs examined:', txs, 'with >1 output:', withSmall);
console.log('\nmost frequent output addresses (count | BTC | distinct chains):');
[...dests.entries()].sort((a, b) => b[1].count - a[1].count).slice(0, 12).forEach(([a, e]) => {
  console.log(a, '|', e.count, '|', e.btc.toFixed(4), '|', e.sources.size);
});
console.log('\nsmall (<2 BTC) outputs by address:');
const small = new Map();
for (const r of chains) for (const n of r.chain) if (n.nextOutputs)
  for (const o of n.nextOutputs) if (o.v < 2) {
    const e = small.get(o.a) || { count: 0, btc: 0 }; e.count++; e.btc += o.v; small.set(o.a, e);
  }
[...small.entries()].sort((a, b) => b[1].count - a[1].count).slice(0, 12).forEach(([a, e]) =>
  console.log(a, '|', e.count, '|', e.btc.toFixed(6)));
