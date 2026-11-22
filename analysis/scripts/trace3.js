const https = require('https');
const fs = require('fs');
const { EClient } = require('./electrum');
const { addressToScripthash } = require('./scripthash2');
const starts = require('./start_outputs.json');
const START = Date.parse('2026-11-12T00:00:00Z') / 1000;
const END = Date.parse('2026-11-20T00:00:00Z') / 1000;
const txCache = {};

function get(url, retries = 5) {
  return new Promise((resolve, reject) => {
    const go = n => https.get(url, { headers: { 'User-Agent': 'Mozilla/5.0 audit' } }, res => {
      let d = ''; res.on('data', c => d += c);
      res.on('end', () => {
        if (res.statusCode === 200) { try { return resolve(JSON.parse(d)); } catch (e) { return reject(e); } }
        if ((res.statusCode === 429 || res.statusCode >= 500) && n < retries) return setTimeout(() => go(n + 1), 2000 * (n + 1));
        reject(new Error('HTTP ' + res.statusCode + ' ' + d.slice(0, 80)));
      });
    }).on('error', e => n < retries ? setTimeout(() => go(n + 1), 2000 * (n + 1)) : reject(e));
    go(0);
  });
}
async function getTx(id) {
  if (!txCache[id]) txCache[id] = await get('https://blockstream.info/api/tx/' + id);
  return txCache[id];
}
async function connect() {
  for (const [h, p] of [['electrum2.bluewallet.io', 443], ['electrum3.bluewallet.io', 443], ['electrum.legalise.it', 50002]]) {
    const c = new EClient(h, p, true);
    try { await c.connect(); await c.request('server.version', ['audit/1.0', '1.4']); return c; } catch (e) { console.error('connect fail', h, e.message); }
  }
  throw new Error('no electrum');
}
function timeout(p, ms) { return Promise.race([p, new Promise((_, r) => setTimeout(() => r(new Error('TIMEOUT')), ms))]); }
async function batchRetry(clients, calls, k) {
  for (let a = 0; a < 5; a++) {
    try { return await timeout(clients[k].batch(calls), 60000); }
    catch (e) { console.error('retry', k, a, e.message); try { clients[k].close(); } catch (_) {} clients[k] = await connect(); }
  }
  throw new Error('batch failed');
}

(async () => {
  const clients = [await connect(), await connect()];
  const known = new Set(starts.map(o => o.address));
  const hopmap = {}; // address -> events
  let frontier = [...known].map(a => ({ a, sh: addressToScripthash(a), depth: 0 }));
  const MAX_ROUNDS = 8;
  for (let round = 1; round <= MAX_ROUNDS && frontier.length; round++) {
    console.error('ROUND', round, 'frontier', frontier.length);
    const histories = {};
    const tasks = []; for (let i = 0; i < frontier.length; i += 100) tasks.push(frontier.slice(i, i + 100));
    let qi = 0;
    await Promise.all(clients.map(async (_, k) => {
      while (true) { const t = tasks[qi++]; if (!t) break; const r = await batchRetry(clients, t.map(x => ({ method: 'blockchain.scripthash.get_history', params: [x.sh] })), k); t.forEach((x, j) => histories[x.a] = r[j]); }
    }));
    const need = new Set();
    for (const x of frontier) for (const h of (histories[x.a] || [])) if (h.height > 975000) need.add(h.tx_hash);
    const ids = [...need];
    console.error(' txids', ids.length);
    const txs = {}; let ti = 0;
    await Promise.all(Array.from({ length: 3 }, async () => {
      while (true) { const id = ids[ti++]; if (!id) break; try { txs[id] = await getTx(id); } catch (e) { console.error('tx err', id.slice(0,10), e.message); } }
    }));
    const next = [];
    for (const x of frontier) {
      const evs = [];
      for (const h of (histories[x.a] || [])) {
        const t = txs[h.tx_hash]; if (!t || !t.status || !t.status.block_time) continue;
        if (t.status.block_time < START || t.status.block_time >= END) continue;
        const ins = (t.vin || []).filter(v => v.prevout && v.prevout.scriptpubkey_address === x.a);
        if (!ins.length) continue;
        const spent = ins.reduce((s, v) => s + (v.prevout.value || 0), 0);
        const outs = (t.vout || []).filter(o => o.scriptpubkey_address && o.value > 0 && o.scriptpubkey_address !== x.a)
          .map(o => ({ address: o.scriptpubkey_address, value: o.value }));
        evs.push({ date: new Date(t.status.block_time * 1000).toISOString(), height: t.status.block_height,
          txid: h.tx_hash, spent, outs });
        for (const o of outs) if (!known.has(o.address)) { known.add(o.address); next.push({ a: o.address, sh: addressToScripthash(o.address), depth: x.depth + 1 }); }
      }
      if (evs.length) hopmap[x.a] = evs;
    }
    fs.writeFileSync('./hopmap3.json', JSON.stringify(hopmap, null, 2));
    console.error(' events so far', Object.keys(hopmap).length, 'next', next.length);
    frontier = next;
  }
  clients.forEach(c => { try { c.close(); } catch (_) {} });
  console.log('DONE addresses with outgoing in window:', Object.keys(hopmap).length);
})().catch(e => { console.error('FATAL', e); process.exit(1); });
