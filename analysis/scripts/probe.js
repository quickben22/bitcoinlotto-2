const https = require('https');

function get(url) {
  return new Promise((resolve, reject) => {
    https.get(url, { headers: { 'User-Agent': 'btc-audit/1.0' } }, res => {
      let d = '';
      res.on('data', c => d += c);
      res.on('end', () => {
        if (res.statusCode !== 200) return reject(new Error('HTTP ' + res.statusCode + ' ' + d.slice(0, 100)));
        try { resolve(JSON.parse(d)); } catch (e) { reject(e); }
      });
    }).on('error', reject);
  });
}

async function main() {
  const z = require('./zombies.json');
  const n = parseInt(process.argv[2] || '200', 10);
  const step = Math.floor(z.length / n);
  const sample = [];
  for (let i = 0; i < n; i++) sample.push(z[i * step]);
  let spent = 0, mempoolSpent = 0, errors = 0;
  const examples = [];
  const queue = sample.slice();
  const workers = Array.from({ length: 8 }, async () => {
    while (queue.length) {
      const item = queue.shift();
      try {
        const j = await get('https://mempool.space/api/address/' + item.a);
        if (j.chain_stats.spent_txo_count > 0) { spent++; if (examples.length < 15) examples.push([item.a, j.chain_stats.spent_txo_count, j.chain_stats.spent_txo_sum]); }
        if (j.mempool_stats && j.mempool_stats.spent_txo_count > 0) mempoolSpent++;
      } catch (e) { errors++; }
    }
  });
  await Promise.all(workers);
  console.log(JSON.stringify({ sampled: sample.length, withSpentTxos: spent, withMempoolSpent: mempoolSpent, errors, examples }, null, 2));
}
main().catch(e => { console.error(e); process.exit(1); });
