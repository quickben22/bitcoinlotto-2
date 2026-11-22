const fs = require('fs');
const z = require('./zombies.json');
const hist = require('./histories.json');
const bal = require('./current_balances.json');
const recent = [];
z.forEach((x, i) => {
  const txs = hist[i].filter(t => t.h >= 975000);
  if (txs.length) recent.push({ idx: i, address: x.a, snap: x.snap, audit: x.cur, now: bal[i].confirmed + bal[i].unconfirmed, txs });
});
fs.writeFileSync('./recent26.json', JSON.stringify(recent, null, 2));
console.log('count', recent.length);
recent.forEach(r => console.log(r.address, 'snap', (r.snap / 1e8).toFixed(8), 'now', (r.now / 1e8).toFixed(8), 'recent txs', r.txs.map(t => t.h + ':' + t.tx.slice(0, 8)).join(' ')));
