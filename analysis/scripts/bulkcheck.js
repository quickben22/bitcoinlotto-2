const https = require('https');
const fs = require('fs');

function get(url, retries = 4) {
  return new Promise((resolve, reject) => {
    const attempt = (n) => {
      https.get(url, { headers: { 'User-Agent': 'btc-dormancy-recheck/1.0' } }, res => {
        let d = '';
        res.on('data', c => d += c);
        res.on('end', () => {
          if (res.statusCode === 200) {
            try { return resolve(JSON.parse(d)); } catch (e) { return reject(e); }
          }
          if (res.statusCode === 429 && n < retries) {
            console.error('429, retry', n);
            return setTimeout(() => attempt(n + 1), 2000 * (n + 1));
          }
          reject(new Error('HTTP ' + res.statusCode + ' ' + d.slice(0, 150)));
        });
      }).on('error', e => {
        if (n < retries) return setTimeout(() => attempt(n + 1), 1000 * (n + 1));
        reject(e);
      });
    };
    attempt(0);
  });
}

async function main() {
  const z = require('./zombies.json');
  const SIZE = 200;
  const batches = [];
  for (let i = 0; i < z.length; i += SIZE) batches.push(z.slice(i, i + SIZE));
  console.error('batches', batches.length);
  const results = new Map();
  const queue = batches.slice();
  const workers = Array.from({ length: 3 }, async () => {
    while (queue.length) {
      const b = queue.shift();
      const url = 'https://blockchain.info/balance?active=' + b.map(x => x.a).join(',');
      const j = await get(url);
      for (const item of b) {
        const r = j[item.a];
        if (!r) throw new Error('missing address in response: ' + item.a);
        results.set(item.a, { snap: item.snap, final: r.final_balance, recv: r.total_received, ntx: r.n_tx });
      }
    }
  });
  await Promise.all(workers);
  console.error('got', results.size);
  fs.writeFileSync('./balances.json', JSON.stringify([...results]));
  const decreased = [...results].filter(([a, r]) => r.final < r.snap);
  console.log(JSON.stringify({ total: results.size, decreased: decreased.length,
    examples: decreased.slice(0, 20).map(([a, r]) => ({ a, snap: r.snap, final: r.final, delta: r.final - r.snap, ntx: r.ntx })) }, null, 2));
}
main().catch(e => { console.error(e); process.exit(1); });
