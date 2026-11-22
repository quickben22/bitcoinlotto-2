const https = require('https');
const fs = require('fs');
const cacheTxs = {};
const cacheOut = {};

function get(url, retries = 5) {
  return new Promise((resolve, reject) => {
    const go = n => https.get(url, { headers: { 'User-Agent': 'audit/1.0' } }, res => {
      let d = ''; res.on('data', c => d += c);
      res.on('end', () => {
        if (res.statusCode === 200) { try { return resolve(JSON.parse(d)); } catch (e) { return reject(e); } }
        if ((res.statusCode === 429 || res.statusCode >= 500) && n < retries) return setTimeout(() => go(n + 1), 800 * (n + 1));
        reject(new Error('HTTP ' + res.statusCode + ' ' + d.slice(0, 100)));
      });
    }).on('error', e => n < retries ? setTimeout(() => go(n + 1), 800 * (n + 1)) : reject(e));
    go(0);
  });
}
async function outspends(txid) {
  if (!cacheOut[txid]) cacheOut[txid] = await get('https://mempool.space/api/tx/' + txid + '/outspends');
  return cacheOut[txid];
}
async function tx(txid) {
  if (!cacheTxs[txid]) cacheTxs[txid] = await get('https://mempool.space/api/tx/' + txid);
  return cacheTxs[txid];
}

const START = new Date('2026-11-12T00:00:00Z').getTime() / 1000;
const END = new Date('2026-11-20T00:00:00Z').getTime() / 1000;

async function main() {
  const starts = require('./start_outputs.json');
  const results = [];
  const queue = starts.map(o => ({ ...o, path: [{ address: o.address, value: o.value, date: o.date, txid: o.txid, n: o.n }], depth: o.hop }));
  let processed = 0;
  async function worker() {
    while (queue.length) {
      const item = queue.shift();
      processed++;
      if (processed % 50 === 0) console.error('processed', processed, 'queue', queue.length);
      const os = await outspends(item.txid);
      if (!os[item.n] || !os[item.n].spent) {
        results.push({ source: item.source, path: item.path, endpoint: item.address, endpointValue: item.value, status: 'unspent' });
        continue;
      }
      const spendTxid = os[item.n].txid;
      const t = await tx(spendTxid);
      const when = t.status && t.status.block_time;
      if (!when || when < START || when >= END) {
        results.push({ source: item.source, path: item.path, endpoint: item.address, endpointValue: item.value,
          status: 'spent_outside_window', spendTxid, spendDate: when ? new Date(when * 1000).toISOString() : 'mempool' });
        continue;
      }
      const date = new Date(when * 1000).toISOString();
      const newPath = [...item.path, { address: null, value: item.value, date, txid: spendTxid, n: -1, via: 'spend' }];
      const vouts = t.vout.map((o, i) => ({ ...o, n: i })).filter(o => o.scriptpubkey_address && o.value > 0);
      if (!vouts.length) {
        results.push({ source: item.source, path: newPath, endpoint: null, endpointValue: 0, status: 'all_fee_or_opreturn', spendTxid, spendDate: date });
        continue;
      }
      for (const o of vouts) {
        if (item.depth >= 12) {
          results.push({ source: item.source, path: newPath, endpoint: o.scriptpubkey_address, endpointValue: o.value, status: 'depth_limit', spendTxid, spendDate: date });
        } else {
          queue.push({ source: item.source, address: o.scriptpubkey_address, value: o.value, txid: spendTxid, n: o.n,
            date, path: [...newPath.slice(0, -1), { ...newPath[newPath.length - 1], outputs: vouts.map(v => ({ a: v.scriptpubkey_address, v: v.value })) }], depth: item.depth + 1 });
        }
      }
    }
  }
  await Promise.all(Array.from({ length: 5 }, worker));
  fs.writeFileSync('./trace_results.json', JSON.stringify(results, null, 2));
  console.log('results', results.length, 'endpoints', new Set(results.map(r => r.endpoint)).size);
}
main().catch(e => { console.error('FATAL', e); process.exit(1); });
