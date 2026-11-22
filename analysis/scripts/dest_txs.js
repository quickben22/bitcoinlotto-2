const https = require('https');
const fs = require('fs');
const starts = require('./start_outputs.json');
const addrs = [...new Set(starts.map(o => o.address))];
function get(url, n = 5) { return new Promise((res, rej) => { const go = k => https.get(url, { headers: { 'User-Agent': 'audit/1.0' } }, r => { let d=''; r.on('data',c=>d+=c); r.on('end',()=>{ if(r.statusCode===200){try{return res(JSON.parse(d))}catch(e){return rej(e)}} if(k<n) return setTimeout(()=>go(k+1),1500*(k+1)); rej(new Error('HTTP '+r.statusCode+' '+d.slice(0,80))); }); }).on('error',e=>k<n?setTimeout(()=>go(k+1),1500*(k+1)):rej(e)); go(0); }); }
(async () => {
  const out = {};
  for (const a of addrs) {
    try { out[a] = await get('https://mempool.space/api/address/' + a + '/txs'); }
    catch (e) { console.error('ERR', a, e.message); out[a] = []; }
  }
  fs.writeFileSync('./dest_txs.json', JSON.stringify(out, null, 2));
  console.log('addresses', Object.keys(out).length);
  for (const [a, txs] of Object.entries(out)) {
    console.log(a, 'txs', txs.length, txs.map(t => new Date(t.status.block_time*1000).toISOString().slice(0,16)).join(' '));
  }
})().catch(e => { console.error('FATAL', e); process.exit(1); });
