const fs = require('fs');
const summary = require('./movements_summary.json');
const txs = require('./txdetails.json');
const outs = [];
for (const s of summary) {
  for (const e of s.events) {
    const tx = txs[e.txid];
    tx.vout.forEach((o, idx) => {
      if (o.scriptpubkey_address && o.scriptpubkey_address !== s.address && o.value > 0) {
        outs.push({ txid: e.txid, n: idx, address: o.scriptpubkey_address, value: o.value,
          date: e.date, source: s.address, hop: 1 });
      }
    });
  }
}
fs.writeFileSync('./start_outputs.json', JSON.stringify(outs, null, 2));
console.log('starting outputs', outs.length, 'BTC', (outs.reduce((a, o) => a + o.value, 0) / 1e8).toFixed(8));
console.log('unique destinations', new Set(outs.map(o => o.address)).size);
const byDest = {};
for (const o of outs) byDest[o.address] = (byDest[o.address] || 0) + o.value;
Object.entries(byDest).sort((a,b) => b[1]-a[1]).slice(0,25).forEach(([a,v]) => console.log((v/1e8).toFixed(8), a));
