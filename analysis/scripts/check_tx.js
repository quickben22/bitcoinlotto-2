const fs = require('fs');
const path = require('path');
const d = process.cwd();
const txs = JSON.parse(fs.readFileSync(path.join(d, 'txdetails.json'), 'utf8'));
const all = JSON.parse(fs.readFileSync(path.join(d, 'alltrace_txs.json'), 'utf8'));
const ids = process.argv.slice(2);
for (const id of ids) {
  const tx = txs[id] || all[id];
  console.log('===', id, tx ? 'FOUND' : 'not in local tx files');
  if (!tx) continue;
  console.log('height', tx.status && tx.status.block_height, 'time', tx.status && tx.status.block_time);
  console.log('VIN:');
  for (const v of tx.vin || []) {
    console.log('  ', v.txid + ':' + v.vout, v.prevout && v.prevout.scriptpubkey_address, v.prevout ? (v.prevout.value / 1e8).toFixed(8) : '?');
  }
  console.log('VOUT:');
  for (const v of tx.vout || []) {
    console.log('  ', v.n, v.scriptpubkey_address, (v.value / 1e8).toFixed(8));
  }
}
