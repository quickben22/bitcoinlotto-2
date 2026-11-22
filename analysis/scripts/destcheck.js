const https = require('https');
const fs = require('fs');
const starts = require('./start_outputs.json');
const addrs = [...new Set(starts.map(o => o.address))];
function get(url, n = 4) { return new Promise((res, rej) => { const go = k => https.get(url, { headers: { 'User-Agent': 'audit/1.0' } }, r => { let d=''; r.on('data',c=>d+=c); r.on('end',()=>{ if(r.statusCode===200){try{return res(JSON.parse(d))}catch(e){return rej(e)}} if(k<n) return setTimeout(()=>go(k+1),800*(k+1)); rej(new Error('HTTP '+r.statusCode)); }); }).on('error',e=>k<n?setTimeout(()=>go(k+1),800*(k+1)):rej(e)); go(0); }); }
(async () => {
  const out = [];
  const q = addrs.slice();
  const workers = Array.from({ length: 4 }, async () => {
    while (q.length) {
      const a = q.shift();
      const j = await get('https://mempool.space/api/address/' + a);
      const fromUs = starts.filter(o => o.address === a).reduce((s, o) => s + o.value, 0);
      out.push({ address: a, fromUsBTC: fromUs / 1e8, funded: j.chain_stats.funded_txo_sum / 1e8,
        spent: j.chain_stats.spent_txo_sum / 1e8, balance: (j.chain_stats.funded_txo_sum - j.chain_stats.spent_txo_sum) / 1e8,
        spentTxos: j.chain_stats.spent_txo_count, txCount: j.chain_stats.tx_count });
    }
  });
  await Promise.all(workers);
  out.sort((a,b) => b.fromUsBTC - a.fromUsBTC);
  fs.writeFileSync('./dest_stats.json', JSON.stringify(out, null, 2));
  console.log(JSON.stringify(out, null, 2));
})().catch(e => { console.error(e); process.exit(1); });
