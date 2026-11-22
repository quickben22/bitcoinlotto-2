const { EClient } = require('./electrum');
const { addressToScripthash } = require('./scripthash');
const z = require('./zombies.json');

async function withTimeout(p, ms) {
  return Promise.race([p, new Promise((_, rej) => setTimeout(() => rej(new Error('TIMEOUT')), ms))]);
}

(async () => {
  const c = new EClient('electrum.blockstream.info', 50002, true);
  await c.connect();
  for (const size of [10, 25, 50, 100, 200]) {
    const shs = z.slice(0, size).map(x => addressToScripthash(x.a));
    const t0 = Date.now();
    try {
      const r = await withTimeout(c.batch(shs.map(sh => ({ method: 'blockchain.scripthash.get_balance', params: [sh] }))), 15000);
      console.log('size', size, 'OK', r.length, 'ms', Date.now() - t0, 'first', JSON.stringify(r[0]));
    } catch (e) { console.log('size', size, 'FAIL', e.message, 'ms', Date.now() - t0); break; }
  }
  c.close();
})().catch(e => { console.error('FATAL', e); process.exit(1); });
