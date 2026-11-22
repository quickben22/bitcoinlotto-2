const fs = require('fs');
const { EClient } = require('./electrum');
const { addressToScripthash } = require('./scripthash');

const SERVERS = [
  ['electrum2.bluewallet.io', 443, 300],
  ['electrum3.bluewallet.io', 443, 300],
  ['electrum1.bluewallet.io', 443, 300],
  ['electrum.legalise.it', 50002, 200],
];

async function connect(server) {
  const [host, port] = server;
  const c = new EClient(host, port, true);
  await c.connect();
  await c.request('server.version', ['audit/1.0', '1.4']);
  return c;
}
function timeout(p, ms) {
  return Promise.race([p, new Promise((_, rej) => setTimeout(() => rej(new Error('TIMEOUT')), ms))]);
}

(async () => {
  const z = require('./zombies.json');
  console.error('addresses', z.length);
  const items = z.map((x, i) => ({ ...x, idx: i, sh: addressToScripthash(x.a) }));
  const clients = await Promise.all(SERVERS.map(connect));
  console.error('connected', clients.length);
  const out = new Array(items.length);
  let cursor = 0, done = 0;
  const tasks = [];
  while (true) {
    const start = cursor; cursor += 200;
    if (start >= items.length) break;
    tasks.push(items.slice(start, start + 200));
  }
  console.error('tasks', tasks.length);
  const workers = clients.map((_, n) => (async () => {
    while (true) {
      const t = tasks.shift();
      if (!t) break;
      const serverIdx = n;
      let res;
      for (let attempt = 0; attempt < 6; attempt++) {
        try {
          res = await timeout(clients[serverIdx].batch(t.map(x => ({ method: 'blockchain.scripthash.get_balance', params: [x.sh] }))), 45000);
          if (!res || res.length !== t.length || res.some(r => !r || typeof r.confirmed !== 'number')) throw new Error('bad response');
          break;
        } catch (e) {
          console.error('retry worker', n, 'attempt', attempt, e.message);
          try { clients[serverIdx].close(); } catch (_) {}
          await new Promise(r => setTimeout(r, 1500 * (attempt + 1)));
          clients[serverIdx] = await connect(SERVERS[(serverIdx + attempt + 1) % SERVERS.length]);
        }
      }
      if (!res) throw new Error('gave up on task');
      t.forEach((x, j) => { out[x.idx] = { a: x.a, snap: x.snap, cur: x.cur, confirmed: res[j].confirmed, unconfirmed: res[j].unconfirmed }; });
      done += t.length;
      if (done % 10000 < 200) console.error('progress', done, '/', items.length);
    }
  })());
  await Promise.all(workers);
  clients.forEach(c => { try { c.close(); } catch (_) {} });
  if (out.some(x => !x)) throw new Error('missing results: ' + out.filter(x => !x).length);
  fs.writeFileSync('./current_balances.json', JSON.stringify(out));
  const decreased = out.filter(x => x.confirmed + x.unconfirmed < x.cur);
  const unconf = out.filter(x => x.unconfirmed !== 0);
  const belowSnap = out.filter(x => x.confirmed + x.unconfirmed < x.snap);
  console.log(JSON.stringify({
    total: out.length,
    decreasedVsAudit: decreased.length,
    belowSnapshot: belowSnap.length,
    withUnconfirmed: unconf.length,
    examples: decreased.slice(0, 30).map(x => ({ a: x.a, snap: x.snap, audit: x.cur, now: x.confirmed + x.unconfirmed, delta: x.confirmed + x.unconfirmed - x.cur }))
  }, null, 2));
})().catch(e => { console.error('FATAL', e); process.exit(1); });
