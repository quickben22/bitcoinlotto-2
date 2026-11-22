const fs = require('fs');
const recent = require('./recent26.json');
const txs = require('./txdetails.json');
const rows = [];
const summary = [];

for (const r of recent) {
  const addr = r.address;
  let totalOut = 0, totalBack = 0, lastSpend = null;
  const events = [];
  for (const rt of r.txs) {
    const tx = txs[rt.tx];
    const ins = tx.vin.filter(v => v.prevout && v.prevout.scriptpubkey_address === addr);
    const inVal = ins.reduce((s, v) => s + v.prevout.value, 0);
    const outs = tx.vout.filter(o => o.scriptpubkey_address === addr);
    const outVal = outs.reduce((s, o) => s + o.value, 0);
    if (inVal > 0) {
      const date = new Date(tx.status.block_time * 1000).toISOString();
      const dests = tx.vout.filter(o => o.scriptpubkey_address !== addr && o.value > 0)
        .map(o => ({ a: o.scriptpubkey_address, v: o.value }));
      totalOut += inVal; totalBack += outVal;
      events.push({ date, height: tx.status.block_height, txid: rt.tx, spent: inVal, back: outVal, net: inVal - outVal, dests });
      if (!lastSpend || date > lastSpend) lastSpend = date;
      rows.push([addr, date, tx.status.block_height, rt.tx, inVal, outVal, inVal - outVal, dests.map(d => d.a + ':' + (d.v / 1e8).toFixed(8)).join('|')].join(','));
    }
  }
  if (events.length) summary.push({ address: addr, snapshot: r.snap / 1e8, remaining: r.now / 1e8,
    movedGross: totalOut / 1e8, returned: totalBack / 1e8, movedNet: (totalOut - totalBack) / 1e8,
    lastSpend, events });
}

summary.sort((a, b) => b.movedNet - a.movedNet);
fs.writeFileSync('./movements.csv', ['address,utc,height,txid,spent_sats,returned_sats,net_out_sats,destinations', ...rows].join('\n'));
fs.writeFileSync('./movements_summary.json', JSON.stringify(summary, null, 2));
console.log('addresses with outgoing spends:', summary.length);
for (const s of summary) {
  console.log(`${s.address} | last ${s.lastSpend} | net out ${s.movedNet.toFixed(8)} BTC | remain ${s.remaining.toFixed(8)} BTC | events ${s.events.length}`);
  for (const e of s.events) console.log(`    ${e.date} h${e.height} ${e.txid.slice(0,12)} spent ${(e.spent/1e8).toFixed(8)} back ${(e.back/1e8).toFixed(8)} net ${(e.net/1e8).toFixed(8)} dest ${e.dests.map(d=>d.a+':'+ (d.v/1e8).toFixed(8)).join(' | ')}`);
}
