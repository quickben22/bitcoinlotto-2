const fs = require('fs');
const path = require('path');
const d = process.cwd();
const chains = JSON.parse(fs.readFileSync(path.join(d, 'chains.json'), 'utf8'));
const txs = JSON.parse(fs.readFileSync(path.join(d, 'txdetails.json'), 'utf8'));
const all = JSON.parse(fs.readFileSync(path.join(d, 'alltrace_txs.json'), 'utf8'));
let unique = new Set();
for (const r of chains) for (const n of r.chain) unique.add(n.address);
unique.add(chains[0].source);
console.log('unique addresses in all chains:', unique.size);
console.log('');
for (const r of chains) {
  const first = r.chain[0];
  const tx = txs[first.txid] || all[first.txid];
  if (!tx) { console.log(r.source.slice(0, 10), first.txid, 'TX NOT FOUND'); continue; }
  let fromSource = 0, totalIn = 0, nSource = 0;
  for (const v of tx.vin || []) {
    const val = v.prevout ? v.prevout.value / 1e8 : 0;
    totalIn += val;
    if (v.prevout && v.prevout.scriptpubkey_address === r.source) { fromSource += val; nSource++; }
  }
  console.log([
    r.source.slice(0, 10),
    'firstTx=' + first.txid.slice(0, 10),
    'fromSource=' + fromSource.toFixed(8),
    '(' + nSource + ' inputs)',
    'totalIn=' + totalIn.toFixed(8),
    'largestOut=' + first.value / 1e8,
    fromSource < 0.001 ? '<<< DUST ONLY' : '',
  ].join('  '));
}
