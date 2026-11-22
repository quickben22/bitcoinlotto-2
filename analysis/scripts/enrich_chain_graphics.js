// Enhance all chain graphics with:
//   1. dormancy: last on-chain activity before the coins moved, and how long they sat idle
//   2. attribution: public wallet labels from WalletExplorer (wallet cluster + exchange/service name)
//
// Usage: cd analysis/data && node ../scripts/enrich_chain_graphics.js [--skip-labels]
// Data:  docs/chain-attribution.json (cache, safe to delete to refresh labels)
const fs = require('fs');
const path = require('path');
const https = require('https');
const { spawnSync } = require('child_process');

const dataDir = process.cwd();
const repoRoot = path.resolve(dataDir, '..', '..');
const docsDir = path.join(repoRoot, 'docs');
const chains = JSON.parse(fs.readFileSync(path.join(dataDir, 'chains.json'), 'utf8'));
const zombies = JSON.parse(fs.readFileSync(path.join(dataDir, 'zombies.json'), 'utf8'));
const histories = JSON.parse(fs.readFileSync(path.join(dataDir, 'histories.json'), 'utf8'));
const zIndex = new Map(zombies.map((z, i) => [z.a, i]));

const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
function fmtDate(iso) {
  const d = new Date(iso);
  const p = (n) => String(n).padStart(2, '0');
  return `${p(d.getUTCDate())} ${MONTHS[d.getUTCMonth()]} ${d.getUTCFullYear()}`;
}
function fmtSeconds(s) {
  if (s < 3600) return `${Math.round(s / 60)} minutes`;
  const days = s / 86400;
  if (days < 60) return `${days.toFixed(1)} days`;
  const years = days / 365.2425;
  return `${years.toFixed(2)} years (${Math.round(days)} days)`;
}

// Block-height -> timestamp (approximate: 10 min/block from the 2010 genesis). Used only for
// the 2010 "last activity" of the two example addresses; exact block times are unavailable in
// the saved data, and the error there is at most a few days out of ~16 years.
function heightToApproxDate(h) {
  return new Date(Date.UTC(2009, 0, 3, 18, 15) + h * 600000);
}
function lastActivityBefore(address, moveMs) {
  const i = zIndex.get(address);
  if (i === undefined || !histories[i]) return null;
  const prior = histories[i].filter((e) => {
    const t = e.h === 0 ? Date.UTC(2009, 0, 3, 18, 15) : heightToApproxDate(e.h).getTime();
    return t < moveMs - 60000;
  });
  if (!prior.length) return null;
  const e = prior[prior.length - 1];
  return { ms: e.h === 0 ? Date.UTC(2009, 0, 3, 18, 15) : heightToApproxDate(e.h).getTime(),
    height: e.h, tx: e.tx, approx: true };
}

function getJson(url) {
  return new Promise((resolve, reject) => {
    https.get(url, { headers: { 'User-Agent': 'bitcoin-lotto-audit/1.0' } }, (res) => {
      let body = '';
      res.on('data', (c) => body += c);
      res.on('end', () => {
        try { resolve(JSON.parse(body)); } catch (e) { reject(new Error(`bad json: ${body.slice(0, 120)}`)); }
      });
    }).on('error', reject);
  });
}
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

const LABEL_URL = (addr) => `https://www.walletexplorer.com/api/1/address-lookup?address=${addr}&caller=bitcoin-lotto-audit`;
const WALLET_URL = (id) => `https://www.walletexplorer.com/api/1/wallet?wallet=${id}&from=0&count=2&caller=bitcoin-lotto-audit`;

function collectAddresses() {
  const set = new Set();
  for (const r of chains) {
    set.add(r.source);
    for (const n of r.chain) set.add(n.address);
  }
  return [...set];
}

(async () => {
  const cacheFile = path.join(docsDir, 'chain-attribution.json');
  const cache = fs.existsSync(cacheFile) ? JSON.parse(fs.readFileSync(cacheFile, 'utf8')) : {};
  const skipLabels = process.argv.includes('--skip-labels');
  const addresses = collectAddresses();
  console.error(`addresses to label: ${addresses.length}`);

  if (!skipLabels) {
    // WalletExplorer asks for at most 1 request/second.
    for (const addr of addresses) {
      if (cache[addr] !== undefined) continue;
      let entry = { source: 'WalletExplorer', status: 'no data' };
      try {
        const lookup = await getJson(LABEL_URL(addr));
        if (lookup.found && lookup.wallet_id) {
          entry.wallet_id = lookup.wallet_id;
          await sleep(1100);
          const w = await getJson(WALLET_URL(lookup.wallet_id));
          if (w.found && w.label) entry.label = w.label;
          else entry.label = `unnamed cluster ${lookup.wallet_id.slice(0, 8)}`;
        } else if (lookup.found === false) {
          entry.status = 'not in WalletExplorer';
        } else if (lookup.error) {
          entry.status = `error: ${lookup.error}`;
        }
      } catch (e) {
        entry.status = `request failed: ${e.message}`;
      }
      cache[addr] = entry;
      fs.writeFileSync(cacheFile, JSON.stringify(cache, null, 2));
      console.error(`  ${addr.slice(0, 10)} -> ${entry.label || entry.status}`);
      await sleep(1100);
    }
  }

  // Build per-chain enrichment and write a report. Dormancy fields come from
  // compute_dormancy.js (analysis/data/dormancy.json) and must not be recomputed here.
  const rows = [];
  const enrichPath = path.join(docsDir, 'chain-enrichment.json');
  const enriched = fs.existsSync(enrichPath) ? JSON.parse(fs.readFileSync(enrichPath, 'utf8')) : {};
  const dormancy = JSON.parse(fs.readFileSync(path.join(dataDir, 'dormancy.json'), 'utf8'));
  for (const r of chains) {
    const first = r.chain[0];
    const srcLabel = cache[r.source];
    const finalLabel = cache[r.chain[r.chain.length - 1].address];
    const dm = dormancy[r.source] || {};
    enriched[r.source] = {
      ...(enriched[r.source] || {}),
      dormantSince: dm.dormantSince || null,
      dormantSeconds: dm.dormantSeconds != null ? dm.dormantSeconds : null,
      dormantApprox: false,
      dormantDefinition: dm.definition,
      sourceLabel: srcLabel && (srcLabel.label || srcLabel.status),
      sourceWalletId: srcLabel && srcLabel.wallet_id,
      finalLabel: finalLabel && (finalLabel.label || finalLabel.status),
    };
    rows.push({
      source: r.source,
      moves: r.chain.length - 1,
      firstMove: first.date,
      lastActivity: dm.dormantSince,
      dormancy: dm.dormantSeconds != null ? fmtSeconds(dm.dormantSeconds) : 'unknown',
      sourceLabel: enriched[r.source].sourceLabel,
      finalLabel: enriched[r.source].finalLabel,
      finalAddress: r.chain[r.chain.length - 1].address,
      finalBtc: (r.chain[r.chain.length - 1].value / 1e8).toFixed(8),
    });
  }
  fs.writeFileSync(enrichPath, JSON.stringify(enriched, null, 2));

  let md = '# Dormancy and public attribution for the 19 chains\n\n';
  md += 'Dormancy = time from the last transaction that moved at least 0.001 BTC before 12 Nov 2026 ';
  md += '(dust deposits excluded) to the first transaction in the 12–19 Nov 2026 window. Several ';
  md += 'addresses were reused as relay hops inside that window, so measuring from the transaction ';
  md += 'immediately before each tracked move would understate dormancy. Block times are from the ';
  md += 'public WalletExplorer and Esplora APIs.\n\n';
  md += 'Attribution is from the public WalletExplorer wallet-clustering API. An "unnamed cluster" ';
  md += 'means the addresses are grouped together on-chain but the group has no public name; a named ';
  md += 'label (exchange, service, etc.) is a public attribution, not proof of legal ownership.\n\n';
  md += '| Source | Dormant since | Dormancy | Source wallet label | Final wallet label | Final BTC |\n';
  md += '|---|---|---|---|---|---:|\n';
  for (const x of rows) {
    md += `| \`${x.source.slice(0, 10)}…\` | ${x.lastActivity ? fmtDate(x.lastActivity) : '?'} | ${x.dormancy} | ${x.sourceLabel || '?'} | ${x.finalLabel || '?'} | ${x.finalBtc} |\n`;
  }
  fs.writeFileSync(path.join(docsDir, 'chain-attribution.md'), md);

  // Regenerate graphics (PNG only) using the updated generator, which reads chain-enrichment.json.
  const gen = path.join(dataDir, '..', 'scripts', 'make_chain_graphic.js');
  const edge = 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe';
  const profile = path.join(process.env.TEMP || 'C:\\Temp', 'edge-chain-shots');
  for (const r of chains) {
    const src = r.source;
    const svg = path.join(docsDir, `btc-flow-${src.slice(0, 8)}.svg`);
    const png = path.join(docsDir, `btc-flow-${src.slice(0, 8)}.png`);
    spawnSync(process.execPath, [gen, src], { cwd: dataDir, encoding: 'utf8' });
    const height = 372 + (r.chain.length + 1) * 176;
    spawnSync(edge, [
      '--headless=new', '--disable-gpu', '--no-sandbox', '--hide-scrollbars',
      '--force-device-scale-factor=1', `--user-data-dir=${profile}`,
      `--screenshot=${png}`, `--window-size=1000,${height}`,
      'file:///' + svg.replace(/\\/g, '/'),
    ], { encoding: 'utf8', timeout: 120000 });
    console.error(`rendered ${src.slice(0, 10)}`);
  }
  console.log(path.join(docsDir, 'chain-attribution.md'));
})().catch((e) => { console.error('FATAL', e); process.exit(1); });
