// Fetch the exact block time of the last on-chain transaction before the first move,
// for each of the 19 source addresses. Uses the public Esplora API at blockstream.info.
const fs = require('fs');
const path = require('path');
const https = require('https');
const d = process.cwd();
const chains = JSON.parse(fs.readFileSync(path.join(d, 'chains.json'), 'utf8'));
const zombies = JSON.parse(fs.readFileSync(path.join(d, 'zombies.json'), 'utf8'));
const histories = JSON.parse(fs.readFileSync(path.join(d, 'histories.json'), 'utf8'));
const zIndex = new Map(zombies.map((z, i) => [z.a, i]));
const outFile = path.join(d, 'last_activity_times.json');
const cache = fs.existsSync(outFile) ? JSON.parse(fs.readFileSync(outFile, 'utf8')) : {};

function getJson(url) {
  return new Promise((resolve, reject) => {
    https.get(url, { headers: { 'User-Agent': 'bitcoin-lotto-audit/1.0' } }, (res) => {
      let b = '';
      res.on('data', (c) => b += c);
      res.on('end', () => { try { resolve(JSON.parse(b)); } catch (e) { reject(new Error(b.slice(0, 100))); } });
    }).on('error', reject);
  });
}
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

(async () => {
  for (const r of chains) {
    const moveMs = Date.parse(r.chain[0].date);
    const i = zIndex.get(r.source);
    const prior = (histories[i] || []).filter((e) => {
      // block height estimate is only used to pre-filter; exact time is fetched below
      const est = Date.UTC(2009, 0, 3) + e.h * 600000;
      return est < moveMs;
    });
    // take the most recent few candidates by height; fetch their real times and pick the latest
    const cand = prior.slice(-4);
    let best = null;
    for (const e of cand) {
      if (cache[e.tx] !== undefined) {
        const t = cache[e.tx];
        if (t && (!best || t > best.time)) best = { tx: e.tx, time: t, height: e.h };
        continue;
      }
      let time = null;
      for (let a = 0; a < 4; a++) {
        try {
          const tx = await getJson(`https://blockstream.info/api/tx/${e.tx}`);
          time = tx.status && tx.status.block_time;
          break;
        } catch (err) { await sleep(1500 * (a + 1)); }
      }
      cache[e.tx] = time;
      fs.writeFileSync(outFile, JSON.stringify(cache, null, 2));
      if (time && (!best || time > best.time)) best = { tx: e.tx, time, height: e.h };
      await sleep(250);
    }
    console.log(r.source.slice(0, 10), best ? new Date(best.time * 1000).toISOString() + ' h' + best.height : 'none');
  }
})().catch((e) => { console.error('FATAL', e); process.exit(1); });
