const https = require('https');
const fs = require('fs');
const starts = require('./start_outputs.json');
const START = Date.parse('2026-11-12T00:00:00Z') / 1000;
const END = Date.parse('2026-11-20T00:00:00Z') / 1000;
const cache = {};
function get(path, retries = 4) {
  return new Promise((resolve, reject) => {
    const go = n => https.get('https://blockstream.info/api' + path, { headers: { 'User-Agent': 'Mozilla/5.0' } }, res => {
      let d = ''; res.on('data', c => d += c);
      res.on('end', () => {
        if (res.statusCode === 200) { try { return resolve(JSON.parse(d)); } catch (e) { return reject(e); } }
        if ((res.statusCode === 429 || res.statusCode >= 500) && n < retries) return setTimeout(() => go(n + 1), 3000 * (n + 1));
        reject(new Error('HTTP ' + res.statusCode + ' ' + d.slice(0, 60)));
      });
    }).on('error', e => n < retries ? setTimeout(() => go(n + 1), 3000 * (n + 1)) : reject(e));
    go(0);
  });
}
async function outspends(id) { if (!cache['o'+id]) cache['o'+id] = await get('/tx/' + id + '/outspends'); return cache['o'+id]; }
async function tx(id) { if (!cache['t'+id]) cache['t'+id] = await get('/tx/' + id); return cache['t'+id]; }

(async () => {
  const results = [];
  const queue = starts.map(o => ({ source: o.source, txid: o.txid, n: o.n, address: o.address, amount: o.value,
    date: o.date, depth: 1, hops: [o.address] }));
  let processed = 0;
  const MAX = 200;
  while (queue.length && processed < MAX) {
    const item = queue.shift(); processed++;
    if (processed % 25 === 0) console.error('processed', processed, 'q', queue.length);
    let os; try { os = await outspends(item.txid); } catch (e) { console.error('out err', e.message); continue; }
    if (!os[item.n] || !os[item.n].spent) {
      results.push({ source: item.source, amount: item.amount, hops: item.hops, final: item.address, status: 'unspent', endValue: item.amount });
      continue;
    }
    const sp = os[item.n];
    let t; try { t = await tx(sp.txid); } catch (e) { console.error('tx err', e.message); continue; }
    const when = sp.status && sp.status.block_time;
    if (!when || when < START || when >= END) {
      results.push({ source: item.source, amount: item.amount, hops: item.hops, final: item.address, status: 'spent_outside_window', spendTxid: sp.txid, spendDate: when ? new Date(when*1000).toISOString() : null });
      continue;
    }
    const vouts = t.vout.map((o,i)=>({...o,n:i})).filter(o => o.scriptpubkey_address && o.value > 0 && o.scriptpubkey_address !== item.address);
    if (!vouts.length) { results.push({ source: item.source, amount: item.amount, hops: item.hops, final: null, status: 'no_external_outputs', spendTxid: sp.txid }); continue; }
    // Follow only the largest external output = main continuation
    const main = vouts.sort((a,b)=>b.value-a.value)[0];
    const hops = [...item.hops, main.scriptpubkey_address];
    if (item.depth >= 8) {
      results.push({ source: item.source, amount: item.amount, hops, final: main.scriptpubkey_address, status: 'depth_limit', endValue: main.value, spendTxid: sp.txid });
      continue;
    }
    queue.push({ source: item.source, txid: sp.txid, n: main.n, address: main.scriptpubkey_address, amount: main.value,
      date: new Date(when*1000).toISOString(), depth: item.depth + 1, hops });
  }
  fs.writeFileSync('./trace_main_results.json', JSON.stringify(results, null, 2));
  const st = {}; for (const r of results) st[r.status] = (st[r.status]||0)+1;
  console.log('results', results.length, JSON.stringify(st));
  const bySrc = {};
  for (const r of results) { (bySrc[r.source] = bySrc[r.source] || []).push(r); }
  for (const [s, rs] of Object.entries(bySrc)) {
    console.log('\n' + s);
    for (const r of rs) console.log('  ', r.status, 'hops', r.hops.map(a=>a.slice(0,12)).join(' -> '), 'end', r.endValue ? (r.endValue/1e8).toFixed(8) : '');
  }
})().catch(e => { console.error('FATAL', e); process.exit(1); });
