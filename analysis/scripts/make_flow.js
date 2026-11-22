const fs = require('fs');
const summary = require('./movements_summary.json');
const hopmap = require('./hopmap3.json');
const destStats = require('./dest_stats.json');
const starts = require('./start_outputs.json');
const OUT = 'D:/gestacijski diabetes/Android Studio/bitcoinLotto/docs/';
const f = n => (n / 1e8).toFixed(8);
const short = a => a.length > 16 ? a.slice(0, 12) + '…' + a.slice(-6) : a;

// Build CSV of all hops (hop1 from summary events, hop2 from hopmap)
const csv = ['hop,from_address,utc,block_height,txid,amount_btc,to_address,is_final'];
for (const s of summary) {
  for (const e of [...s.events].sort((a,b)=>a.date.localeCompare(b.date))) {
    csv.push([1, s.address, e.date, e.height, e.txid, f(e.net), e.dests.map(d=>d.a).join('|'), 'no'].join(','));
  }
}
for (const [a, evs] of Object.entries(hopmap)) {
  for (const e of evs) {
    const main = [...e.outs].sort((x,y)=>y.value-x.value)[0];
    csv.push([2, a, e.date, e.height, e.txid, f(e.spent), e.outs.map(o=>o.a).join('|'), 'no'].join(','));
  }
}
fs.writeFileSync(OUT + 'refresh-movement-flow.csv', csv.join('\n') + '\n');

// Final endpoints: destinations with no hop2 events (still holding)
const movedAgain = new Set(Object.keys(hopmap));
const finals = destStats.filter(d => !movedAgain.has(d.address));
console.log('final untouched destinations', finals.length, 'BTC', finals.reduce((s,d)=>s+d.fromUsBTC,0).toFixed(8));
console.log('hop2 events', Object.values(hopmap).reduce((s,e)=>s+e.length,0));

// Main hubs from hop2 outputs
const hub = {}, hubSources = {};
for (const [a, evs] of Object.entries(hopmap)) for (const e of evs) for (const o of e.outs) {
  hub[o.address] = (hub[o.address] || 0) + o.value;
  (hubSources[o.address] = hubSources[o.address] || new Set()).add(a);
}
const hubs = Object.entries(hub).sort((a,b)=>b[1]-a[1]).slice(0,12);
console.log('top hubs', hubs.map(([a,v])=>[short(a),f(v)]));

let md = '# Where the bitcoin went — forward trace, 12–19 November 2026\n\n';
md += `## Scope\n\nThe 34 outgoing transactions from the 19 addresses were traced forward through the chain. `;
md += `Only movements confirmed on-chain between **12 November 00:00 UTC and 19 November 24:00 UTC** are counted. `;
md += `Addresses were followed one hop further where the coins moved again in that window. `;
md += `At each hop the **largest output** is the main continuation; where a receiving address is a shared/commingled `;
md += `wallet (many depositors), an output can contain coins from other sources — that is flagged below.\n\n`;
md += `## Headline numbers\n\n`;
md += `| Metric | Value |\n|---|---|\n`;
md += `| Outgoing transactions from the 19 source addresses (hop 1) | **34** |\n`;
md += `| Destination addresses at hop 1 | **18** |\n`;
md += `| Destination addresses that moved the coins again in the window (hop 2) | **${movedAgain.size}** |\n`;
md += `| Further transactions observed at hop 2 | **${Object.values(hopmap).reduce((s,e)=>s+e.length,0)}** |\n`;
md += `| Destinations still holding the coins untouched at 19 Nov | **${finals.length}** |\n\n`;
md += `## Where the coins ended up\n\n`;
md += `### A. Still sitting at the first destination (no further movement)\n\n`;
md += `| Address | BTC received from the 19 | Current balance |\n|---|---:|---:|\n`;
for (const d of finals) md += `| \`${d.address}\` | ${d.fromUsBTC.toFixed(8)} | ${d.balance.toFixed(8)} |\n`;
md += `\n### B. Moved again — main continuation (largest output at hop 2)\n\n`;
md += `| Hop-1 destination | Hop-2 date (UTC) | Main next address | Output BTC | Note |\n|---|---|---|---:|---|\n`;
for (const [a, evs] of Object.entries(hopmap)) {
  for (const e of evs) {
    const outs = [...e.outs].sort((x,y)=>y.value-x.value);
    if (!outs.length) continue;
    const m = outs[0];
    const commingled = (hubSources[m.address] || new Set()).size > 1;
    md += `| \`${short(a)}\` | ${e.date} | \`${short(m.address)}\` | ${f(m.value)} | ${commingled ? 'multiple sources converged here' : ''}${outs.length > 1 ? ` (${outs.length} outputs)` : ''} |\n`;
  }
}
md += `\n### C. Main consolidation hubs seen at hop 2\n\n`;
for (const [a, v] of hubs) md += `- \`${a}\` — ${f(v)} BTC received from ${hubSources[a].size} distinct hop-1 destination${hubSources[a].size > 1 ? 's' : ''}\n`;
md += `\nThese hub addresses receive from several of the hop-1 destinations in the same window, so they look like `;
md += `exchange or custodial consolidation wallets. Once coins arrive there they are mixed with other funds and can no `;
md += `longer be attributed to a specific source address from public data alone.\n\n`;
md += `## Per-source summary\n\n`;
md += `| Source address | Times moved (hop 1) | Destinations | BTC moved (net) |\n|---|---:|---|---:|\n`;
for (const s of summary) {
  md += `| \`${s.address}\` | ${s.events.length} | ${[...new Set(s.events.flatMap(e=>e.dests.map(d=>d.a)))].map(short).join('<br>')} | ${s.movedNet.toFixed(8)} |\n`;
}
md += `\nFull row-by-row detail (every txid, timestamp, amount and destination at hops 1 and 2) is in `;
md += `\`docs/refresh-movement-flow.csv\`.\n\n`;
md += `## Caveats\n\n`;
md += `- Hop-2 amounts are the full value spent by the destination address; if that address pooled coins from other `;
md += `depositors, only part of the amount belongs to the 19 original addresses.\n`;
md += `- Tracing beyond hop 2 was not continued: several hop-2 outputs land in high-throughput consolidation wallets `;
md += `where the public chain cannot separate individual deposits.\n`;
md += `- Five destination addresses (section A) have never spent the received coins — for those the answer is `;
md += `unambiguous: the bitcoin is still at that address.\n`;
fs.writeFileSync(OUT + 'refresh-movement-flow.md', md);
console.log('report written');
