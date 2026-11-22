const fs = require('fs');
const path = require('path');
const c = JSON.parse(fs.readFileSync(path.join(process.cwd(), 'address_txs_cache.json'), 'utf8'));
for (const a of process.argv.slice(2)) {
  console.log('===', a);
  const x = c[a];
  if (!x || !x.txs) { console.log('no data'); continue; }
  for (const t of x.txs) console.log(new Date(t.time * 1000).toISOString(), 'sent', t.amount_sent, 'recv', t.amount_received, t.txid.slice(0, 12));
}
