const fs = require('fs');
const { EClient } = require('./electrum');
const { addressToScripthash } = require('./scripthash2');
const starts = require('./start_outputs.json');
const START = Date.parse('2026-11-12T00:00:00Z') / 1000;
const END = Date.parse('2026-11-20T00:00:00Z') / 1000;

async function connect() {
  for (const [h, p] of [['electrum2.bluewallet.io', 443], ['electrum3.bluewallet.io', 443], ['electrum.legalise.it', 50002]]) {
    const c = new EClient(h, p, true);
    try { await c.connect(); await c.request('server.version', ['audit/1.0', '1.4']); return c; } catch (e) { console.error('connect fail', h, e.message); }
  }
  throw new Error('no electrum server');
}
function timeout(p, ms) { return Promise.race([p, new Promise((_, r) => setTimeout(() => r(new Error('TIMEOUT')), ms))]); }

async function batchRetry(clients, calls, idx, n) {
  for (let a = 0; a < 5; a++) {
    try { return await timeout(clients[idx].batch(calls), 60000); }
    catch (e) { console.error('retry', idx, a, e.message); try { clients[idx].close(); } catch (_) {} clients[idx] = await connect(); }
  }
  throw new Error('batch failed');
}

(async () => {
  const clients = [await connect(), await connect(), await connect()];
  const known = new Set(starts.map(o => o.address));
  const allTxs = {};
  const hopMap = {};   // address -> list of outgoing events in window
  let frontier = [...known].map(a => ({ a, sh: addressToScripthash(a), depth: 0 }));
  let round = 0;
  const MAX_ROUNDS = 6;
  while (frontier.length && round < MAX_ROUNDS) {
    round++;
    console.error('round', round, 'frontier', frontier.length);
    const histories = {};
    const tasks = [];
    for (let i = 0; i < frontier.length; i += 100) tasks.push(frontier.slice(i, i + 100));
    const qi = { n: 0 };
    const workers = clients.map((_, k) => (async () => {
      while (true) {
        const t = tasks[qi.n++]; if (!t) break;
        const res = await batchRetry(clients, t.map(x => ({ method: 'blockchain.scripthash.get_history', params: [x.sh] })), k, round);
        t.forEach((x, j) => histories[x.a] = res[j]);
      }
    })());
    await Promise.all(workers);
    // collect txs in window
    const need = new Map();
    for (const x of frontier) {
      for (const h of (histories[x.a] || [])) {
        if (h.height === 0) continue;
        // get block time later; approximate by fetching headers
        if (!need.has(h.tx_hash)) need.set(h.tx_hash, []);
        need.get(h.tx_hash).push(x.a);
      }
    }
    const ids = [...need.keys()];
    console.error('txids to fetch', ids.length);
    const txdata = {};
    const txTasks = [];
    for (let i = 0; i < ids.length; i += 100) txTasks.push(ids.slice(i, i + 100));
    const ti = { n: 0 };
    const workers2 = clients.map((_, k) => (async () => {
      while (true) {
        const t = txTasks[ti.n++]; if (!t) break;
        const res = await batchRetry(clients, t.map(id => ({ method: 'blockchain.transaction.get', params: [id, true] })), k, round);
        t.forEach((id, j) => { if (res[j] && res[j].txid) txdata[id] = res[j]; });
      }
    })());
    await Promise.all(workers2);
    Object.assign(allTxs, txdata);
    const next = [];
    for (const x of frontier) {
      const events = [];
      for (const h of (histories[x.a] || [])) {
        const t = txdata[h.tx_hash]; if (!t) continue;
        const when = t.blocktime; if (!when || when < START || when >= END) continue;
        const ins = (t.vin || []).filter(v => v.prevout && v.prevout.scriptpubkey_address === x.a);
        if (!ins.length) continue; // incoming only
        const inVal = ins.reduce((s, v) => s + (v.prevout.value || 0), 0);
        const outs = (t.vout || []).filter(o => o.scriptpubkey_address && o.value > 0 && o.scriptpubkey_address !== x.a)
          .map(o => ({ a: o.scriptpubkey_address, v: o.value }));
        events.push({ date: new Date(when * 1000).toISOString(), height: h.height, txid: h.tx_hash, spent: inVal, outs });
        for (const o of outs) {
          const nextAddr = o.a;
          if (!known.has(nextAddr)) { known.add(nextAddr); next.push({ a: nextAddr, sh: addressToScripthash(nextAddr), depth: x.depth + 1 }); }
        }
      }
      if (events.length) hopMap[x.a] = events;
    }
    frontier = next;
    fs.writeFileSync('./hopmap.json', JSON.stringify(hopMap, null, 2));
    fs.writeFileSync('./alltrace_txs.json', JSON.stringify(allTxs));
    console.error('round done, events so far', Object.keys(hopMap).length, 'next', next.length);
  }
  clients.forEach(c => { try { c.close(); } catch (_) {} });
  console.log('addresses with outgoing activity in window', Object.keys(hopMap).length, 'rounds', round);
})().catch(e => { console.error('FATAL', e); process.exit(1); });
