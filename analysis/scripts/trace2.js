const https = require('https');
const fs = require('fs');
const cacheTxs = {}, cacheOut = {};

function get(url, retries = 4) {
  return new Promise((resolve, reject) => {
    const go = n => https.get(url, { headers: { 'User-Agent': 'audit/1.0' } }, res => {
      let d = ''; res.on('data', c => d += c);
      res.on('end', () => {
        if (res.statusCode === 200) { try { return resolve(JSON.parse(d)); } catch (e) { return reject(e); } }
        if ((res.statusCode === 429 || res.statusCode >= 500) && n < retries) return setTimeout(() => go(n + 1), 1000 * (n + 1));
        reject(new Error('HTTP ' + res.statusCode));
      });
    }).on('error', e => n < retries ? setTimeout(() => go(n + 1), 1000 * (n + 1)) : reject(e));
    go(0);
  });
}
async function outspends(txid) { if (!cacheOut[txid]) cacheOut[txid] = await get('https://mempool.space/api/tx/' + txid + '/outspends'); return cacheOut[txid]; }
async function tx(txid) { if (!cacheTxs[txid]) cacheTxs[txid] = await get('https://mempool.space/api/tx/' + txid); return cacheTxs[txid]; }

const START = Date.parse('2026-11-12T00:00:00Z') / 1000;
const END = Date.parse('2026-11-20T00:00:00Z') / 1000;
const MAX_NODES = 400;
let nodes = 0;

async function main() {
  const starts = require('./start_outputs.json');
  const results = [];
  const seen = new Set();
  const queue = starts.map(o => ({ ...o, amount: o.value, path: [{ address: o.address, value: o.value, date: o.date, txid: o.txid, n: o.n }], depth: 1 }));
  let processed = 0;
  async function worker() {
    while (queue.length && nodes < MAX_NODES) {
      const item = queue.shift();
      const key = item.txid + ':' + item.n;
      if (seen.has(key)) continue;
      seen.add(key);
      nodes++; processed++;
      if (processed % 50 === 0) console.error('processed', processed, 'queue', queue.length);
      let os;
      try { os = await outspends(item.txid); } catch (e) { console.error('outspends err', key, e.message); continue; }
      if (!os[item.n] || !os[item.n].spent) {
        results.push({ source: item.source, amount: item.amount, path: item.path, endpoint: item.address, status: 'unspent' });
        continue;
      }
      const sp = os[item.n];
      let t;
      try { t = await tx(sp.txid); } catch (e) { console.error('tx err', sp.txid, e.message); continue; }
      const when = sp.status && sp.status.block_time;
      if (!when || when < START || when >= END) {
        results.push({ source: item.source, amount: item.amount, path: item.path, endpoint: item.address, status: 'spent_outside_window',
          spendTxid: sp.txid, spendDate: when ? new Date(when * 1000).toISOString() : 'mempool' });
        continue;
      }
      const date = new Date(when * 1000).toISOString();
      const vouts = t.vout.map((o, i) => ({ ...o, n: i })).filter(o => o.scriptpubkey_address && o.value > 0);
      const spendNode = { date, txid: sp.txid, spentFrom: item.address, inputValue: item.amount, outputs: vouts.map(v => ({ a: v.scriptpubkey_address, v: v.value })) };
      const path = [...item.path, spendNode];
      // Follow only outputs that plausibly carry our value: top 3 by value, >= 1% of tracked amount
      const candidates = vouts.filter(o => o.value >= Math.max(1000, item.amount * 0.01))
        .sort((a, b) => b.value - a.value).slice(0, 3);
      if (!candidates.length) {
        results.push({ source: item.source, amount: item.amount, path, endpoint: null, status: 'dust_or_fee', spendTxid: sp.txid, spendDate: date });
        continue;
      }
      for (const o of candidates) {
        if (item.depth >= 10 || nodes >= MAX_NODES) {
          results.push({ source: item.source, amount: item.amount, path, endpoint: o.scriptpubkey_address, status: 'limit_reached', spendTxid: sp.txid, spendDate: date });
        } else {
          queue.push({ source: item.source, amount: Math.min(item.amount, o.value), address: o.scriptpubkey_address,
            value: o.value, txid: sp.txid, n: o.n, date, path, depth: item.depth + 1 });
        }
      }
    }
  }
  await Promise.all(Array.from({ length: 3 }, worker));
  fs.writeFileSync('./trace2_results.json', JSON.stringify(results, null, 2));
  const stats = {};
  for (const r of results) stats[r.status] = (stats[r.status] || 0) + 1;
  console.log('results', results.length, 'nodes', nodes, 'stats', JSON.stringify(stats));
}
main().catch(e => { console.error('FATAL', e); process.exit(1); });
