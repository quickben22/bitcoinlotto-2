const https = require('https');
const summary = require('./movements_summary.json');
function get(url, n = 3) { return new Promise((res, rej) => { const go = k => https.get(url, { headers: { 'User-Agent': 'audit/1.0' } }, r => { let d=''; r.on('data',c=>d+=c); r.on('end',()=>{ if(r.statusCode===200){try{return res(JSON.parse(d))}catch(e){return rej(e)}} if(n>k) return setTimeout(()=>go(k+1),1000*(k+1)); rej(new Error('HTTP '+r.statusCode)); }); }).on('error',e=>k<n?setTimeout(()=>go(k+1),1000*(k+1)):rej(e)); go(0); }); }
(async () => {
  const q = summary.map(s => s.address);
  const out = [];
  for (const a of q) {
    const j = await get('https://mempool.space/api/address/' + a);
    const s = summary.find(x => x.address === a);
    out.push({ a, snap: +(s.snapshot).toFixed(8), funded: +(j.chain_stats.funded_txo_sum/1e8).toFixed(8), spent: +(j.chain_stats.spent_txo_sum/1e8).toFixed(8), realBal: +((j.chain_stats.funded_txo_sum-j.chain_stats.spent_txo_sum)/1e8).toFixed(8), movedNet: +s.movedNet.toFixed(8), remain: +s.remaining.toFixed(8) });
  }
  console.log(JSON.stringify(out, null, 2));
})().catch(e => { console.error(e); process.exit(1); });
