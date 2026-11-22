const fs = require('fs');
const { EClient } = require('./electrum');
const { addressToScripthash } = require('./scripthash');

const SERVERS = [
  ['electrum2.bluewallet.io', 443],
  ['electrum3.bluewallet.io', 443],
  ['electrum1.bluewallet.io', 443],
  ['electrum.legalise.it', 50002],
];
async function connect(s) { const c = new EClient(s[0], s[1], true); await c.connect(); await c.request('server.version', ['audit/1.0', '1.4']); return c; }
function timeout(p, ms) { return Promise.race([p, new Promise((_, rej) => setTimeout(() => rej(new Error('TIMEOUT')), ms))]); }

(async () => {
  const z = require('./zombies.json');
  const items = z.map((x, i) => ({ ...x, idx: i, sh: addressToScripthash(x.a) }));
  const clients = await Promise.all(SERVERS.map(connect));
  const BATCH = 100;
  const tasks = [];
  for (let i = 0; i < items.length; i += BATCH) tasks.push(items.slice(i, i + BATCH));
  const out = new Array(items.length);
  let done = 0;
  const workers = clients.map((_, n) => (async () => {
    while (true) {
      const t = tasks.shift();
      if (!t) break;
      let res;
      for (let attempt = 0; attempt < 6; attempt++) {
        try {
          res = await timeout(clients[n].batch(t.map(x => ({ method: 'blockchain.scripthash.get_history', params: [x.sh] }))), 60000);
          if (!res || res.length !== t.length || res.some(r => !Array.isArray(r))) throw new Error('bad response');
          break;
        } catch (e) {
          console.error('retry', n, attempt, e.message);
          try { clients[n].close(); } catch (_) {}
          await new Promise(r => setTimeout(r, 1500 * (attempt + 1)));
          clients[n] = await connect(SERVERS[(n + attempt + 1) % SERVERS.length]);
        }
      }
      if (!res) throw new Error('gave up');
      t.forEach((x, j) => { out[x.idx] = res[j].map(h => ({ h: h.height, tx: h.tx_hash })); });
      done += t.length;
      if (done % 10000 < BATCH) console.error('progress', done, '/', items.length);
    }
  })());
  await Promise.all(workers);
  clients.forEach(c => { try { c.close(); } catch (_) {} });
  if (out.some(x => !x)) throw new Error('missing');
  fs.writeFileSync('./histories.json', JSON.stringify(out));
  const withRecent = out.filter(h => h.some(t => t.h >= 975000));
  console.log(JSON.stringify({ total: out.length, withTxAfterHeight975000: withRecent.length,
    maxHeight: Math.max(...out.map(h => h.reduce((m, t) => Math.max(m, t.h), 0))) }, null, 2));
})().catch(e => { console.error('FATAL', e); process.exit(1); });
