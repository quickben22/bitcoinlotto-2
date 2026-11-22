// Dormancy = gap between the last meaningful transaction before the 12-19 Nov 2026 activity
// window and the FIRST transaction in that window. Dust (< 0.001 BTC) does not count as activity.
// Many of these addresses were then reused as relay hops within the window, so measuring from
// the last transaction before the immediately preceding move would badly understate dormancy.
const fs = require('fs');
const path = require('path');
const https = require('https');
const d = process.cwd();
const repoRoot = path.resolve(d, '..', '..');
const chains = JSON.parse(fs.readFileSync(path.join(d, 'chains.json'), 'utf8'));
const exactTimes = JSON.parse(fs.readFileSync(path.join(d, 'last_activity_times.json'), 'utf8'));
const addrCacheFile = path.join(d, 'address_txs_cache.json');
const addrCache = fs.existsSync(addrCacheFile) ? JSON.parse(fs.readFileSync(addrCacheFile, 'utf8')) : {};
const DUST = 0.001;
const WINDOW_START = Date.parse('2026-11-12T00:00:00Z');

function getJson(url) {
  return new Promise((resolve, reject) => {
    https.get(url, { headers: { 'User-Agent': 'bitcoin-lotto-audit/1.0' } }, (res) => {
      let b = ''; res.on('data', (c) => b += c);
      res.on('end', () => { try { resolve(JSON.parse(b)); } catch (e) { reject(new Error(b.slice(0, 100))); } });
    }).on('error', reject);
  });
}
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

(async () => {
  const out = {};
  for (const r of chains) {
    const src = r.source;
    if (addrCache[src] === undefined) {
      let data = null;
      for (let a = 0; a < 4; a++) {
        try {
          data = await getJson(`https://www.walletexplorer.com/api/1/address?address=${src}&from=0&count=100&caller=bitcoin-lotto-audit`);
          break;
        } catch (e) { await sleep(1500 * (a + 1)); }
      }
      addrCache[src] = data;
      fs.writeFileSync(addrCacheFile, JSON.stringify(addrCache, null, 2));
      await sleep(1100);
    }
    const data = addrCache[src];
    let firstInWindow = null, lastBefore = null;
    if (data && data.txs) {
      for (const t of data.txs) {
        const ms = (t.time || 0) * 1000;
        const amt = Math.max(t.amount_sent || 0, t.amount_received || 0);
        if (ms >= WINDOW_START) {
          if (!firstInWindow || ms < firstInWindow.ms) firstInWindow = { ms, txid: t.txid };
        } else if (amt >= DUST) {
          if (!lastBefore || ms > lastBefore.ms) lastBefore = { ms, txid: t.txid, amount: amt, height: t.block_height };
        }
      }
    }
    if (!firstInWindow) firstInWindow = { ms: Date.parse(r.chain[0].date), txid: r.chain[0].txid };
    if (!lastBefore) {
      // fallback: most recent exact block time available from the earlier fetch
      const times = Object.entries(exactTimes).filter(([, t]) => t * 1000 < WINDOW_START);
      if (times.length) {
        times.sort((a, b) => b[1] - a[1]);
        lastBefore = { ms: times[0][1] * 1000, txid: times[0][0], amount: null, height: null };
      }
    }
    out[src] = {
      dormantSince: lastBefore ? new Date(lastBefore.ms).toISOString() : null,
      wokeUp: new Date(firstInWindow.ms).toISOString(),
      dormantSeconds: lastBefore ? (firstInWindow.ms - lastBefore.ms) / 1000 : null,
      lastTxBeforeWindow: lastBefore && lastBefore.txid,
      lastAmountBtc: lastBefore && lastBefore.amount,
      definition: 'last >= 0.001 BTC tx before 12 Nov 2026 -> first tx in the 12-19 Nov window',
    };
    console.log(src.slice(0, 10), out[src].dormantSince, '->', out[src].wokeUp, out[src].dormantSeconds ?
      (out[src].dormantSeconds / 86400 / 365.2425).toFixed(2) + ' years' : 'none');
  }
  fs.writeFileSync(path.join(d, 'dormancy.json'), JSON.stringify(out, null, 2));

  const enrichPath = path.join(repoRoot, 'docs', 'chain-enrichment.json');
  const enrich = JSON.parse(fs.readFileSync(enrichPath, 'utf8'));
  for (const r of chains) {
    const e = enrich[r.source];
    if (!e) continue;
    e.dormantSince = out[r.source].dormantSince;
    e.dormantSeconds = out[r.source].dormantSeconds;
    e.dormantApprox = false;
    e.dormantDefinition = out[r.source].definition;
  }
  fs.writeFileSync(enrichPath, JSON.stringify(enrich, null, 2));
})().catch((e) => { console.error('FATAL', e); process.exit(1); });
