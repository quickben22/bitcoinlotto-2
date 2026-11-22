const fs = require('fs');
const { EClient } = require('./electrum');
const { addressToScripthash } = require('./scripthash2');
const summary = require('./movements_summary.json');
const txdetails = require('./txdetails.json');
const START = Date.parse('2026-11-12T00:00:00Z') / 1000;
const END = Date.parse('2026-11-20T00:00:00Z') / 1000;
const cache = { hist: {}, tx: {} };
let client;

async function connect() {
  for (const [h, p] of [['electrum2.bluewallet.io', 443], ['electrum3.bluewallet.io', 443], ['electrum.legalise.it', 50002]]) {
    const c = new EClient(h, p, true);
    try { await c.connect(); await c.request('server.version', ['audit/1.0', '1.4']); return c; } catch (e) { console.error('connect fail', h, e.message); }
  }
  throw new Error('no electrum');
}
function timeout(p, ms) { return Promise.race([p, new Promise((_, r) => setTimeout(() => r(new Error('TIMEOUT')), ms))]); }
async function batch(calls) {
  for (let a = 0; a < 6; a++) {
    try { return await timeout(client.batch(calls), 90000); }
    catch (e) { console.error('batch retry', a, e.message); try { client.close(); } catch (_) {} client = await connect(); }
  }
  throw new Error('batch failed');
}
async function history(addr) {
  if (!cache.hist[addr]) cache.hist[addr] = (await client.request('blockchain.scripthash.get_history', [addressToScripthash(addr)])) || [];
  return cache.hist[addr];
}
async function getTxs(ids) {
  const need = ids.filter(id => !cache.tx[id]);
  for (let i = 0; i < need.length; i += 8) {
    const part = need.slice(i, i + 8);
    const res = await batch(part.map(id => ({ method: 'blockchain.transaction.get', params: [id, true] })));
    part.forEach((id, j) => { cache.tx[id] = res[j]; });
  }
  return ids.map(id => cache.tx[id]);
}
const addrOf = o => o.scriptpubkey_address || (o.scriptPubKey && o.scriptPubKey.address);

async function follow(start) {
  const chain = [];
  let cur = { ...start };
  for (let step = 0; step < 25; step++) {
    chain.push({ address: cur.address, txid: cur.txid, n: cur.n, value: cur.value, date: cur.date, status: null });
    const hist = await history(cur.address);
    const cand = hist.filter(h => h.height === 0 || h.height > 974000).map(h => h.tx_hash);
    const txs = await getTxs(cand);
    let spend = null;
    for (const t of txs) {
      if (!t || !t.vin) continue;
      if (t.vin.some(v => v.txid === cur.txid && v.vout === cur.n)) {
        const when = t.blocktime;
        if (!when || when < START || when >= END) { spend = { outside: true, txid: t.txid, when }; }
        else spend = { tx: t, when };
        break;
      }
    }
    if (!spend || spend.outside) {
      chain[chain.length - 1].status = spend && spend.outside ? 'spent_outside_window' : 'UNSPENT';
      return chain;
    }
    const t = spend.tx;
    const outs = t.vout.map((o, i) => ({ address: addrOf(o), value: o.value, n: i })).filter(o => o.address && o.value > 0);
    if (!outs.length) { chain[chain.length - 1].status = 'no_outputs'; return chain; }
    const main = [...outs].sort((a, b) => b.value - a.value)[0];
    chain[chain.length - 1].status = 'moved';
    chain[chain.length - 1].spendTxid = t.txid;
    chain[chain.length - 1].nextOutputs = outs.map(o => ({ a: o.address, v: o.value }));
    cur = { address: main.address, txid: t.txid, n: main.n, value: main.value, date: new Date(spend.when * 1000).toISOString() };
  }
  chain[chain.length - 1].status = 'depth_limit';
  return chain;
}

(async () => {
  client = await connect();
  const starts = [];
  for (const s of summary) {
    let best = null;
    for (const e of s.events) {
      const t = txdetails[e.txid]; if (!t) continue;
      t.vout.forEach((o, i) => {
        if (!o.scriptpubkey_address || o.scriptpubkey_address === s.address || o.value <= 0) return;
        if (!best || o.value > best.value) best = { source: s.address, address: o.scriptpubkey_address,
          txid: e.txid, n: i, value: o.value, date: e.date, netOut: e.net };
      });
    }
    if (best) starts.push(best);
  }
  console.error('starts', starts.length);
  const results = [];
  for (const s of starts) {
    console.error('following', s.source);
    const chain = await follow(s);
    results.push({ source: s.source, initialNetOut: s.netOut, chain });
    console.error('  hops', chain.length, 'final', chain[chain.length - 1].address, chain[chain.length - 1].status);
    fs.writeFileSync('./chains.json', JSON.stringify(results, null, 2));
  }
  client.close();
  console.log('done');
})().catch(e => { console.error('FATAL', e); process.exit(1); });
