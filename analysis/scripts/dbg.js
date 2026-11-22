const { EClient } = require('./electrum');
const { addressToScripthash } = require('./scripthash2');
const a = 'bc1qj5hhw38ge72k8zfsm5akem3t6w2sel3d0nvzr6';
(async () => {
  const c = new EClient('electrum2.bluewallet.io', 443, true); await c.connect();
  const sh = addressToScripthash(a); console.log('sh', sh);
  const bal = await c.request('blockchain.scripthash.get_balance', [sh]); console.log('bal', JSON.stringify(bal));
  const hist = await c.request('blockchain.scripthash.get_history', [sh]); console.log('hist', JSON.stringify(hist));
  const t = await c.request('blockchain.transaction.get', [hist[hist.length-1].tx_hash, true]);
  console.log('tx keys', Object.keys(t), 'blocktime', t.blocktime, 'time', t.time);
  c.close();
})().catch(e => { console.error(e); process.exit(1); });
