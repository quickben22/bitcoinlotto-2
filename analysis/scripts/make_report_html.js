// Build a self-contained HTML explainer with inline SVG charts and links to the flow graphics.
// Usage: cd analysis/data && node ../scripts/make_report_html.js
const fs = require('fs');
const path = require('path');
const d = process.cwd();
const repoRoot = path.resolve(d, '..', '..');
const docs = path.join(repoRoot, 'docs');
const chains = JSON.parse(fs.readFileSync(path.join(d, 'chains.json'), 'utf8'));
const dormancy = JSON.parse(fs.readFileSync(path.join(d, 'dormancy.json'), 'utf8'));
const attrib = JSON.parse(fs.readFileSync(path.join(docs, 'chain-attribution.json'), 'utf8'));
const enrich = JSON.parse(fs.readFileSync(path.join(docs, 'chain-enrichment.json'), 'utf8'));

const esc = (s) => String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
const fmtBtc = (n) => n.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 8 });
const labelCounts = {};
for (const e of Object.values(attrib)) {
  const k = e.label || e.status || 'unknown';
  const cat = k.startsWith('unnamed cluster') ? 'Unnamed wallet cluster' : k === 'not in WalletExplorer' ? 'Not listed' : k;
  labelCounts[cat] = (labelCounts[cat] || 0) + 1;
}

function hbar(title, items, color, suffix, maxOverride) {
  const W = 920, left = 118, right = 78, row = 27;
  const H = items.length * row + 66;
  const max = maxOverride || Math.max(...items.map(i => i.v));
  let s = `<svg viewBox="0 0 ${W} ${H}" class="chart" role="img" aria-label="${esc(title)}">`;
  s += `<text x="16" y="26" class="ct">${esc(title)}</text>`;
  items.forEach((it, i) => {
    const y = 46 + i * row;
    const w = Math.max(2, (it.v / max) * (W - left - right));
    s += `<text x="${left - 8}" y="${y + 13}" text-anchor="end" class="cl">${esc(it.k)}</text>`;
    s += `<rect x="${left}" y="${y + 2}" width="${w}" height="15" rx="3" fill="${color}" opacity="${it.hi ? 1 : 0.75}"/>`;
    s += `<text x="${left + w + 7}" y="${y + 14}" class="cv">${esc(it.t != null ? it.t : it.v.toFixed(2))}${suffix || ''}</text>`;
  });
  s += `</svg>`;
  return s;
}

function amountChart() {
  const r = chains.find(x => x.source.startsWith('1AgiG4fz'));
  const vals = r.chain.map(n => n.value / 1e8);
  const W = 920, H = 290, left = 62, bottom = 40, top = 26;
  const min = Math.log10(Math.min(...vals)), max = Math.log10(Math.max(...vals));
  const X = i => left + (i / (vals.length - 1)) * (W - left - 24);
  const Y = v => top + (1 - (Math.log10(v) - min) / (max - min)) * (H - top - bottom);
  let s = `<svg viewBox="0 0 ${W} ${H}" class="chart" role="img" aria-label="Amount at each hop for 1AgiG4fz">`;
  s += `<text x="16" y="20" class="ct">Amount at each hop — 1AgiG4fz… (log scale)</text>`;
  [1, 10, 100, 1000].forEach(t => {
    if (t < 10 ** min || t > 10 ** max) return;
    s += `<line x1="${left}" y1="${Y(t)}" x2="${W - 24}" y2="${Y(t)}" stroke="#1d3140"/>`;
    s += `<text x="${left - 8}" y="${Y(t) + 4}" text-anchor="end" class="cl">${t}</text>`;
  });
  s += `<polyline points="${vals.map((v, i) => `${X(i)},${Y(v)}`).join(' ')}" fill="none" stroke="#00e5ff" stroke-width="2.5"/>`;
  vals.forEach((v, i) => { s += `<circle cx="${X(i)}" cy="${Y(v)}" r="3" fill="${i === vals.length - 1 ? '#39ff88' : '#00e5ff'}"/>`; });
  s += `<text x="${left}" y="${H - 8}" class="cl">source</text><text x="${W - 24}" y="${H - 8}" text-anchor="end" class="cl">final address (hop ${vals.length - 1})</text>`;
  s += `<text x="${X(20) - 10}" y="${Y(vals[20]) - 14}" class="cv">2,461 BTC — commingled wallet</text>`;
  s += `</svg>`;
  return s;
}

function timelineChart() {
  const byDay = {};
  for (const r of chains) {
    const day = r.chain[0].date.slice(0, 10);
    byDay[day] = (byDay[day] || 0) + 1;
  }
  const days = Object.keys(byDay).sort();
  const W = 920, H = 200, left = 40, rowH = 20;
  const max = Math.max(...Object.values(byDay));
  let s = `<svg viewBox="0 0 ${W} ${H}" class="chart" role="img" aria-label="First movements by day">`;
  s += `<text x="16" y="20" class="ct">First movement of each address, by day (UTC)</text>`;
  days.forEach((day, i) => {
    const x = left + i * ((W - 90) / days.length);
    const h = (byDay[day] / max) * 105;
    s += `<rect x="${x}" y="${140 - h}" width="${(W - 90) / days.length - 14}" height="${h}" rx="4" fill="#00e5ff" opacity="0.8"/>`;
    s += `<text x="${x + ((W - 90) / days.length - 14) / 2}" y="${134 - h}" text-anchor="middle" class="cv">${byDay[day]}</text>`;
    s += `<text x="${x + ((W - 90) / days.length - 14) / 2}" y="162" text-anchor="middle" class="cl">${day.slice(5)}</text>`;
  });
  s += `</svg>`;
  return s;
}

function attributionChart() {
  const W = 920, H = 120;
  let x = 16;
  let s = `<svg viewBox="0 0 ${W} ${H}" class="chart" role="img" aria-label="Public attribution results">`;
  s += `<text x="16" y="20" class="ct">Public labels for the 84 addresses in the chains</text>`;
  const colors = { 'Unnamed wallet cluster': '#00e5ff', 'Not listed': '#f0a020', 'Kraken.com': '#39ff88' };
  for (const [k, v] of Object.entries(labelCounts)) {
    const w = (v / 84) * (W - 32);
    s += `<rect x="${x}" y="40" width="${w}" height="36" fill="${colors[k] || '#888'}" opacity="0.8"/>`;
    s += `<text x="${x + w / 2}" y="63" text-anchor="middle" class="cw">${v}</text>`;
    s += `<text x="${x + w / 2}" y="96" text-anchor="middle" class="cl">${esc(k)}</text>`;
    x += w;
  }
  s += `</svg>`;
  return s;
}

// 1ALpEWtX received an unsolicited 0.001 BTC payment in Feb 2018 which resets the generic
// "last >= 0.001 BTC tx" metric. Its own 75 BTC had not moved since Feb 2011, so chart the
// real coin dormancy and flag it.
const isNoisy = (src) => src.startsWith('1ALpEWtX');
const dormancyItems = chains.map(r => {
  const raw = (dormancy[r.source].dormantSeconds || 0) / 86400 / 365.2425;
  const v = isNoisy(r.source) ? 15.8 : raw;
  return { k: r.source.slice(0, 10), v, t: v.toFixed(1) + (isNoisy(r.source) ? '†' : ''), hi: v > 15 };
}).sort((a, b) => b.v - a.v);
const hopItems = chains.map(r => ({ k: r.source.slice(0, 10), v: r.chain.length, t: r.chain.length === 1 ? '1 transfer' : r.chain.length + ' transfers', hi: r.chain.length > 15 })).sort((a, b) => b.v - a.v);

let gallery = '';
for (const r of chains) {
  const pre = r.source.slice(0, 8);
  const e = enrich[r.source] || {};
  const yrs = isNoisy(r.source) ? '15.8' : (e.dormantSeconds ? (e.dormantSeconds / 86400 / 365.2425).toFixed(1) : '?');
  gallery += `<figure><a href="btc-flow-${pre}.svg"><img src="btc-flow-${pre}.png" alt="Flow diagram for ${esc(r.source)}" loading="lazy"></a>`;
  gallery += `<figcaption><b>${esc(r.source.slice(0, 12))}…</b> — ${r.chain.length - 1} hop(s), dormant ${yrs} years, ends with ${fmtBtc(r.chain[r.chain.length - 1].value / 1e8)} BTC. <a href="btc-flow-${pre}.svg">Open vector version</a></figcaption></figure>\n`;
}

const html = `<!doctype html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Dormant Bitcoin wallets that moved — explained</title>
<style>
:root{color-scheme:dark}body{margin:0;background:#0a0e13;color:#dbe9f4;font-family:Segoe UI,Helvetica,Arial,sans-serif;line-height:1.65}
.wrap{max-width:1040px;margin:0 auto;padding:36px 22px 80px}
h1{font-size:2.1rem;margin:0 0 6px;color:#e8f6ff}h2{font-size:1.4rem;margin:44px 0 10px;color:#00e5ff;border-bottom:1px solid #1d3140;padding-bottom:8px}
h3{font-size:1.05rem;color:#7fb2c8;margin:24px 0 6px}p{margin:10px 0}small,.muted{color:#8aa0b0}
a{color:#00e5ff}.lead{font-size:1.08rem;color:#a8c2d4}
.tldr{background:#101922;border:1px solid #274050;border-left:4px solid #39ff88;border-radius:10px;padding:16px 20px;margin:26px 0}
.tldr ul{margin:6px 0;padding-left:20px}
.cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(190px,1fr));gap:12px;margin:22px 0}
.card{background:#101922;border:1px solid #274050;border-radius:10px;padding:14px 16px}.card b{display:block;font-size:1.7rem;color:#39ff88}.card span{color:#8aa0b0;font-size:.9rem}
.chart{width:100%;height:auto;background:#101922;border:1px solid #1d3140;border-radius:10px;margin:12px 0}
.box{background:#0d1a22;border:1px solid #274050;border-left:4px solid #00e5ff;border-radius:10px;padding:14px 18px;margin:16px 0}
.box p{margin:8px 0}.box b.t{color:#7fe6ff}
.diagram{background:#08131a;border:1px solid #1d3140;border-radius:8px;padding:14px;overflow-x:auto;color:#9fe8ff;font:12px/1.55 Consolas,Menlo,monospace;margin:12px 0;white-space:pre}
.eli{background:#101922;border:1px solid #274050;border-radius:10px;padding:16px 20px;margin:18px 0}
.eli ol,.eli ul{margin:8px 0;padding-left:24px}.eli li{margin:5px 0}
.big{font-size:1.25rem;color:#39ff88;font-weight:700;margin:14px 0 4px}
.ct{fill:#a8c2d4;font-size:14px;font-weight:700}.cl{fill:#8aa0b0;font-size:11px}.cv{fill:#39ff88;font-size:11px}.cw{fill:#08131a;font-size:13px;font-weight:700}
figure{margin:18px 0;background:#101922;border:1px solid #1d3140;border-radius:10px;padding:12px}
figure img{max-width:100%;height:auto;border-radius:6px;display:block;margin:0 auto}
figcaption{font-size:.9rem;color:#8aa0b0;margin-top:8px}
.grid2{display:grid;grid-template-columns:repeat(auto-fit,minmax(300px,1fr));gap:18px}
.note{border-left:3px solid #f0a020;background:#1a160d;padding:10px 14px;border-radius:6px;color:#e8d9b5}
code{font-family:Consolas,Menlo,monospace;font-size:.9em;color:#b9e8ff}
table{border-collapse:collapse;width:100%;font-size:.9rem}td,th{border-bottom:1px solid #1d3140;padding:6px 8px;text-align:left}th{color:#7fb2c8}
</style></head><body><div class="wrap">
<h1>Dormant Bitcoin wallets that suddenly moved</h1>
<p class="lead">A plain-language explanation of what happened between 12 and 19 November 2026, why the coin trail has so many hops, and what can — and cannot — be said about who owns these addresses.</p>

<div class="tldr"><b>Short version</b>
<ul>
<li><b>19 wallets</b> that had not spent anything since 2010–2018 moved <b>241.4 BTC</b> between 12 and 19 November 2026. Nearly all of it is still sitting, unspent, at the end of the trail.</li>
<li>They slept for <b>14 to 16 years</b> (median 14.6). Every one was funded between September 2010 and December 2012, and <b>18 of the 19 never sent a transaction at all</b> until November 2026 — they are savings addresses, not wallets that fell idle.</li>
<li>At least <b>six of the 19</b> carry the <b>546-satoshi payment from May 2026</b> that carried a <i>court-ordered legal notice</i> tied to a New York lawsuit over 39,069 dormant addresses. Moving coins was treated as <b>proof of ownership</b>: 339 addresses that responded were dropped from the case.</li>
<li>The long chains are <b>three things mixed together</b>: a one-time move to modern addresses, deliberate peeling, and automated mixing/custodial batching that breaks the public link. Part of the length is an artefact of following the largest output inside one big wallet.</li>
<li><b>Nobody's real identity is publicly known.</b> Of 84 addresses examined, only one cluster has a public name: <b>Kraken.com</b>, appearing mid-chain in two of the trails.</li>
<li><b>The goal, in plain words: keep the treasure, lose the map.</b> Move the coins off addresses that were publicly named and watched, split them up, pass them through busy shared wallets, and let them come out looking ordinary. They are mostly not being sold — just repositioned.</li>
</ul></div>

<div class="cards">
<div class="card"><b>19</b><span>addresses that moved</span></div>
<div class="card"><b>241.4</b><span>BTC moved out</span></div>
<div class="card"><b>14.6 yr</b><span>median dormancy</span></div>
<div class="card"><b>1–28</b><span>transfers per chain</span></div>
<div class="card"><b>1</b><span>named wallet label found</span></div>
</div>

<h2>1. What happened — and why now</h2>
<p>These addresses come from a list compiled in 2018–19 of legacy addresses that had not sent a single satoshi for more than seven years. They were presumed lost. In the week of 12–19 November 2026, 19 of them suddenly spent. Every arrow in the images further down is a real transaction recorded on Bitcoin's public ledger.</p>
${timelineChart()}
<p class="muted">The 19 first movements cluster in the middle of the week, not spread randomly across the year.</p>
<p>There is a strong public explanation for the timing. In March 2026 an anonymous plaintiff ("Noah Doe") filed a New York lawsuit (<code>Index No. 153119/2026</code>) asking the court to declare him owner of <b>39,069 dormant addresses holding about 3.8 million BTC</b>. In May 2026, with the court's permission, the plaintiffs sent <b>546 satoshis</b> to every one of those addresses with an attached message: <code>COURT-ORDERED LEGAL NOTICE: https://www.ilawconotices.com/153119-2026</code>.</p>
<p>Crucially, the plaintiffs' own filings say that addresses whose owners <b>"took onchain action"</b> were removed from the case — 339 of them. On-chain movement was being used as proof that a real owner still existed. <b>Six of the 19 addresses in this set demonstrably received the May 2026 notice</b> (a 546-satoshi input in their history); the others show only the earlier dusting rounds or nothing at all. So the lawsuit is the best-documented trigger for the wave, but it cannot be shown to explain every one of the 19. Two of the exact transactions are reported by The Block on 13 November 2026; press coverage consistently says the owners' identities remain unknown.</p>
<p class="muted">Sources: <a href="https://www.galaxy.com/insights/research/satoshi-bitcoin-salomon-brothers-patoshi-abandoned-property-new-york-noah-doe-lawsuit">Galaxy Research, 27 May 2026</a>; <a href="https://www.coindesk.com/markets/2026/08/28/bitcoin-wallets-untouched-for-10-years-moved-usd40-million-most-avoided-exchanges">CoinDesk, 28 Aug 2026</a>; <a href="https://privatekeyfinder.io/bitcoin-dormant">dormant-wallet tracker</a>.</p>

<h2>2. How long they slept</h2>
<p>Dormancy is measured from the last <i>meaningful</i> transaction (at least 0.001 BTC) before 12 November to the first transaction in that week. Tiny "dust" deposits — a known attack that sends a few thousand satoshis to many wallets — are excluded, because they do not mean the owner did anything.</p>
${hbar('Years dormant before waking', dormancyItems, '#f0a020', ' yr')}
<p class="muted">† This wallet is charted from the last movement of its own coins (February 2011). The generic rule gives 8.8 years because an unsolicited 0.001 BTC payment arrived in February 2018; the owner never touched the wallet.</p>

<h2>3. Why did they stay dormant for so long?</h2>
<p>The surprise is not that they slept — it is that anyone expected them to wake. A Bitcoin address costs nothing to keep and never expires. Doing nothing is the default state, and for the kind of address these are, it is the entire purpose.</p>
<p>The data shows these were not working wallets that fell idle. <b>Eighteen of the 19 have no outgoing transaction at all before November 2026</b>; the nineteenth spent coins once, on the day it was funded in 2012, then went silent. All 19 were funded between September 2010 and December 2012. They are <b>savings addresses</b> — places where early coins were parked and deliberately left alone.</p>
<p>Where the coins came from reinforces that. Seventeen of the 19 were funded by ordinary transactions from large early addresses that each held hundreds of coins — <code>1F8byfFV…</code> with 751.65 BTC, <code>1QBU89LH…</code> with 665.40 BTC, <code>12yQzuV7…</code> with 578.50 BTC, <code>15oRZR2p…</code> with 510.76 BTC — each sending one slice. Only two (<code>1BU7pYQ…</code> and <code>1FFQgpEs…</code>) are direct 50 BTC mining rewards. The clearest example is a 2012 transaction (<code>bc10913df…</code>) that took a single 80 BTC output and split it into <b>24 addresses</b>; two of them are in this set. Someone was deliberately scattering a pile into many quiet places.</p>

<h3>a) The money was trivial at the time</h3>
<p>This is the most practical reason nobody touched them: when the wallets went quiet, the balances were worth almost nothing, and there was no custody fee, statement or reminder to make anyone look. The same coins today are worth millions.</p>
<table>
<tr><th>Address</th><th>Funded</th><th>Value then (approx.)</th><th>Value now</th></tr>
<tr><td><code>1FFQgpEs…</code></td><td>Sep 2010</td><td>50 BTC ≈ $3</td><td>≈ $3.1M</td></tr>
<tr><td><code>1BU7pYQv…</code></td><td>Sep 2010</td><td>50 BTC ≈ $3</td><td>≈ $3.1M</td></tr>
<tr><td><code>1313TARBF…</code></td><td>Feb 2011</td><td>75 BTC ≈ $75</td><td>≈ $4.6M</td></tr>
<tr><td><code>1NoWYaup…</code></td><td>May 2011</td><td>18.26 BTC ≈ $60</td><td>≈ $1.1M</td></tr>
<tr><td><code>1GdmnuPz…</code></td><td>Apr 2012</td><td>25 BTC ≈ $125</td><td>≈ $1.5M</td></tr>
<tr><td><code>1HnNoijUSS…</code></td><td>Apr 2011</td><td>0.1 BTC ≈ $0.10</td><td>≈ $6,200</td></tr>
<tr><td><b>all 19</b></td><td>2010–2012</td><td><b>241.4 BTC ≈ $1,000–2,000</b></td><td><b>≈ $14.9M</b></td></tr>
</table>
<p class="muted">Then-values use approximate historical prices ($0.06 in Sep 2010, about $1 in Feb 2011, about $3.30 in May 2011, about $5 in Apr 2012, about $13 in Dec 2012). Now-value uses $61,609 per BTC. Order-of-magnitude only.</p>

<h3>b) Holding was the rational choice</h3>
<p>Early coins had a near-zero cost basis. There was no tax reason to realise a gain, no yield to chase, and no counterparty to worry about. Every year of waiting kept the full upside. The alternative — selling a few dollars' worth of bitcoin in 2011 — had no practical benefit and forfeited the asymmetry that made holding attractive in the first place.</p>

<h3>c) Moving them was actively risky</h3>
<p>Every spend announces to the world that the keys still exist and someone controls them. For the holder of a large early balance that invites theft, tax attention, and — as 2026 showed — lawsuits. An address nobody ever spends from leaks almost nothing. Leaving it alone was the privacy-preserving and safest option, and it stayed safe for as long as nobody could prove the coins were still reachable.</p>

<h3>d) The legal and tax picture kept changing</h3>
<p>Through the 2010s the treatment of old coins, their provenance and their reporting obligations kept shifting. Coins of uncertain origin became awkward to touch: a single spend can create a taxable event and a permanent public trail. Waiting cost nothing; acting could create problems, so waiting was the default.</p>

<h3>e) For these 19, the keys were never lost — and that is the exception</h3>
<p>Because the coins moved in 2026, we know the keys survived 14–16 years in someone's control. That rules out the most common explanation for long dormancy — a lost or destroyed key — for this particular set. But it should not be generalised. The lawsuit is an accidental natural experiment: 39,069 dormant addresses were served with a legal notice, and only <b>339 responded by moving coins</b> — under one percent. For an address untouched for 15 years, "the key is gone" remains the best default assumption. These 19 are the rare survivors.</p>

<h3>f) A caveat about the sample</h3>
<p>This set was defined as addresses that had already been dormant for seven or more years when the list was compiled in 2018–19. Long dormancy is therefore partly baked into the selection: we are looking at the ones that stayed still, not at a random sample of old wallets. The interesting question is not why these slept, but why they were the ones that woke.</p>

<h2>4. Why so many hops?</h2>
<p>There is no single answer. The evidence points to four layers acting together — and the largest of them is an artefact of how the trail was followed.</p>
<h3>a) First, a change of address format</h3>
<p>Every source is an old legacy address (<code>1…</code>). Almost every first destination is a modern native-SegWit address (<code>bc1q…</code>). Moving coins to a current wallet format is routine when an old wallet is finally opened, and explains hop 1 on its own.</p>
<div class="box"><b class="t">What is a "peel chain"?</b>
<p>A peel chain is a series of transactions where the holder forwards the <i>bulk</i> of the money to a fresh address and peels off a small slice each time.</p>
<pre class="diagram">25 BTC ──┬── 20 BTC ──┬── 14 BTC ──┬── 7 BTC ──┬── 3 BTC
         │            │            │           │
        5 BTC        6 BTC        7 BTC       4 BTC
      (peeled)     (peeled)     (peeled)    (peeled)</pre>
<p>Bitcoin cannot "send part of a balance". Every spend consumes whole coins and creates a change output, so one spend always leaves change on a new address. A peel chain is that happening <b>over and over</b>: the change is never put back, it is spent again immediately. What makes it deliberate rather than ordinary is repetition, a fresh address every time, and round peeled amounts (1, 2, 4 BTC) instead of a fixed percentage.</p>
<p class="muted">It does <i>not</i> hide the money — the amounts stay public and a tracer can walk the whole chain. It creates distance and ambiguity about who is behind the address and how much they still hold.</p>
</div>

<h3>b) A deliberate peel chain, with fixed round amounts</h3>
<p>Several addresses did not simply empty once. They received coins and forwarded a smaller amount, then repeated it: <code>1GdN9v11…</code> went 25 → 20 → 14 → 7 → 3 BTC over four days, and <code>1AgiG4fz…</code> went 10 → 8 → 4 → 0.9 BTC in a few hours. The amounts left behind are round numbers (1, 2, 4 BTC), not percentages. The same handful of relay addresses appears in five different chains, and three pairs of chains actually share 21, 19 and 8 transactions — they are one path, not two. One transaction spends the court-notice dust of <i>both</i> example addresses together with their pooled 50 + 50 BTC, direct proof that one party controlled both.</p>
<h3>c) A per-hop payment to automated infrastructure</h3>
<p>125 of the 134 spending transactions had more than one output. One small-output address, <code>3J7cUjBZxv…</code>, received 26 tiny payments and belongs to a cluster with over 140,000 transactions; another, <code>32WaVK3A…dd6</code>, received 14 payments of exactly 0.01, 1, 2 or 4 BTC. Fifteen consecutive hop pairs have identical block timestamps. This is the fingerprint of a scripted service, not 19 people manually moving savings.</p>
<h3>d) The biggest factor: one wallet can create dozens of "hops"</h3>
<p>Our rule was "always follow the largest output". Once the trail enters a large custodial wallet (an exchange, broker or OTC desk), the largest output keeps bouncing through that wallet's internal addresses. Amounts jump to 1,400 and 2,461 BTC even though the original coins were worth a few dozen — those are other people's deposits. <b>91 of the 134 steps after the first destination (68%) happen after such a jump</b>, so the 28-transfer chain is closer to 7 real transfers. One address, <code>bc1qzl6fpg…</code>, appears 14 times across two chains; another, <code>bc1qefcww8…</code>, 14 times.</p>
${amountChart()}
<p class="muted">The rising sections of this line are not the original coins growing — they are the traced output landing in a commingled wallet.</p>
${hbar('Transfers per chain', hopItems, '#00e5ff', '')}
<p class="muted">Four of the 19 are a single transfer to an address that is still unspent. The long chains are the ones that entered shared infrastructure — which is the point: length is a product of where the coins went, not of how many owners there were.</p>

<h2>5. In plain words: what was the owner trying to achieve?</h2>
<p>Strip away the transaction detail and the story is simple.</p>
<div class="eli">
<p>Imagine your grandpa buried a jar of coins in the garden 15 years ago. Everyone knows <i>where</i> it is buried, because the garden is public.</p>
<p>Then a stranger files a court paper: "If nobody claims this jar, it belongs to me." And to make sure you notice, he drops a note in the garden every year.</p>
<p>You want to keep the coins — but the moment you pick up the jar, everyone watching knows it is you.</p>
<p>So you do not pick it up in one go. You:</p>
<ol>
<li>dig it up quietly and move it to a new spot nobody knows;</li>
<li>split it into smaller jars, so no single jar looks like "the famous treasure";</li>
<li>pass the jars through a busy shop where thousands of people's coins go into the same till;</li>
<li>let the coins come out looking like ordinary money.</li>
</ol>
<p class="big">The goal: keep the treasure, lose the map.</p>
</div>

<h3>Three concrete goals</h3>
<p><b>1. Stop being a target.</b> The address was publicly named, publicly dusted and publicly counted in a lawsuit. Every day it sits there it attracts lawyers, thieves and journalists. A fresh address attracts nobody.</p>
<p><b>2. Prove the coins are still theirs — without saying who they are.</b> Moving coins is exactly how 339 owners were removed from the lawsuit. Silence looked like abandonment; a spend says "someone is here, and it is mine". A spend does not have to say <i>who</i>.</p>
<p><b>3. Make the money usable again without a spotlight.</b> Once the coins sit in a large shared pool, selling a little looks like one more withdrawal from a busy service, not "the 2010 whale is selling".</p>

<h3>Why many small hops instead of one big move?</h3>
<p>Because <b>one big move is a headline and many small moves are noise</b>. A single 50 BTC transfer gets reported by Whale Alert and written up in the press. A 3 BTC hop into a wallet with 140,000 transactions is invisible. The chain is not the goal — the <i>boringness</i> is.</p>

<h3>What it was probably not</h3>
<p>Not a quick cash-out. At the end of every trail the coins are mostly <b>still sitting, unspent</b>. If the plan were "sell it all", we would see it land on an exchange and stop. Instead it lands on fresh addresses and stays. The goal was <b>repositioning, not selling</b>: get off the flagged address, get into the crowd, and wait.</p>
<p class="note"><b>Honest caveat.</b> This is what the evidence is consistent with, not something proven. If the movement is really a custodian or exchange migrating old funds, the goal was much duller: "move these to a new wallet". If it was a thief, the goal was identical to the owner's — keep the money, lose the map.</p>

<h2>6. Who owns them?</h2>
<p>We queried the public WalletExplorer clustering service for all 84 addresses that appear in the chains. It groups addresses that are spent together; occasionally the group has a public name.</p>
${attributionChart()}
<p><b>Result:</b> no real-world owner is identified. Every source address and every final address is either an unnamed cluster or too new to be listed. The only public name anywhere is <b>Kraken.com</b> for <code>33UB1zVipqDkzibpKKYFFLTpdw1uCRUG5V</code>, an intermediate stop in two chains. Press coverage of the 13 November movements likewise says the owners' identities remain unknown. A cluster label is a public observation, not proof of legal ownership.</p>
<p>What <i>can</i> be said is that some of these wallets are controlled together: the 546-satoshi court-notice inputs of both example addresses were spent in the <i>same</i> transaction (<code>4ae994ad…</code>), together with their pooled 50 + 50 BTC. The same signing key controlled both. But that proves common control of those two wallets, not a named person.</p>
<p class="note"><b>Correction.</b> An earlier pass treated three shared old transactions as evidence of common funding. Two of them are mass dusting transactions (hundreds of 546-satoshi outputs), not funding. Only <code>bc10913df…</code> from 2012 is a genuine shared origin, funding both <code>1GdN9v11…</code> (25 BTC) and <code>1CCoK3bU…</code> (5 BTC) among 24 outputs. Suggestive, but not enough on its own.</p>

<h2>7. The flow diagrams</h2>
<p>Each image below shows one source address at the top, every hop in the middle, and the final unspent address at the bottom. Click an image to open the scalable SVG version. The two example addresses carry a warning: their 58.99 BTC first hop was funded mainly by <i>other</i> inputs in a shared transaction; those two addresses contributed only dust. Their own 50 BTC moved in a separate transaction.</p>
<div class="grid2">${gallery}</div>

<h2>8. Caveats</h2>
<p>"Following the largest output" is a heuristic, not a claim of ownership. Where a wallet mixes many deposits, the traced amount can change sharply. The analysis window is 12–19 November 2026; coins may have moved again since. Attribution comes from a public clustering service and is incomplete by design.</p>
<p class="muted">Generated from analysis/data/chains.json, dormancy.json and docs/chain-attribution.json. Deeper notes: <a href="hop-investigation-local.md">docs/hop-investigation-local.md</a> (transaction-level graph analysis), <a href="chain-attribution.md">docs/chain-attribution.md</a> (dormancy and labels), <a href="refresh-biggest-chain.md">docs/refresh-biggest-chain.md</a> (the traced chains) and <a href="SESSION_2026-11-19.md">docs/SESSION_2026-11-19.md</a> (method).</p>
</div></body></html>
`;
const out = path.join(docs, 'btc-movement-explainer.html');
fs.writeFileSync(out, html, 'utf8');
console.log(out);
