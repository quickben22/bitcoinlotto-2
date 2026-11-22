const https = require('https');
const fs = require('fs');
const recent = require('./recent26.json');
const txids = [...new Set(recent.flatMap(r => r.txs.map(t => t.tx)))];
console.error('unique txids', txids.length);

function get(url, retries = 4) {
  return new Promise((resolve, reject) => {
    const go = n => https.get(url, { headers: { 'User-Agent': 'audit/1.0' } }, res => {
      let d = ''; res.on('data', c => d += c);
      res.on('end', () => {
        if (res.statusCode === 200) { try { return resolve(JSON.parse(d)); } catch (e) { return reject(e); } }
        if ((res.statusCode === 429 || res.statusCode >= 500) && n < retries) return setTimeout(() => go(n + 1), 1000 * (n + 1));
        reject(new Error('HTTP ' + res.statusCode + ' ' + d.slice(0, 120)));
      });
    }).on('error', e => n < retries ? setTimeout(() => go(n + 1), 1000 * (n + 1)) : reject(e));
    go(0);
  });
}

(async () => {
  const out = {};
  const queue = txids.slice();
  const workers = Array.from({ length: 4 }, async () => {
    while (queue.length) {
      const id = queue.shift();
      out[id] = await get('https://mempool.space/api/tx/' + id);
    }
  });
  await Promise.all(workers);
  fs.writeFileSync('./txdetails.json', JSON.stringify(out));
  console.error('saved', Object.keys(out).length);
  const dates = out[txids[0]].status.block_time;
  console.log('sample date', new Date(dates * 1000).toISOString());
})().catch(e => { console.error('FATAL', e); process.exit(1); });
