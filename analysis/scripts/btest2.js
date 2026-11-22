const { EClient } = require('./electrum');
const { addressToScripthash } = require('./scripthash');
const z = require('./zombies.json');

async function withTimeout(p, ms) {
  return Promise.race([p, new Promise((_, rej) => setTimeout(() => rej(new Error('TIMEOUT')), ms))]);
}

(async () => {
  for (const [host, port] of [['electrum.blockstream.info', 50002], ['electrum2.bluewallet.io', 443], ['electrum.legalise.it', 50002]]) {
    let c;
    try { c = new EClient(host, port, true); await c.connect(); }
    catch (e) { console.log(host, 'CONNECT FAIL', e.message); continue; }
    console.log('---', host);
    for (const size of [10, 15, 20, 30, 50]) {
      const shs = z.slice(0, size).map(x => addressToScripthash(x.a));
      const t0 = Date.now();
      try {
        const r = await withTimeout(c.batch(shs.map(sh => ({ method: 'blockchain.scripthash.get_balance', params: [sh] }))), 12000);
        console.log('size', size, 'OK', r.length, 'ms', Date.now() - t0);
      } catch (e) { console.log('size', size, 'FAIL', e.message, 'ms', Date.now() - t0); break; }
    }
    c.close();
  }
})().catch(e => { console.error('FATAL', e); process.exit(1); });
