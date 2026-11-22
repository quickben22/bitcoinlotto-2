const fs = require('fs');
const summary = require('./movements_summary.json');
const bal = require('./current_balances.json');
const z = require('./zombies.json');
const byAddr = new Map(z.map((x, i) => [x.a, { snap: x.snap, audit: x.cur, now: bal[i].confirmed + bal[i].unconfirmed }]));

function fmt(n) { return (n / 1e8).toFixed(8); }
function iso(d) { return d; }

// per-address summary CSV
const head = 'address,snapshot_btc,first_movement_utc,last_movement_utc,moved_btc_gross,returned_as_change_btc,moved_btc_net,remaining_btc,outgoing_txs,destination_addresses';
const rows = summary.map(s => {
  const first = s.events.map(e => e.date).sort()[0];
  const dests = [...new Set(s.events.flatMap(e => e.dests.map(d => d.a)))].join('|');
  return [s.address, fmt(s.snapshot*1e8), first, s.lastSpend, fmt(s.movedGross*1e8), fmt(s.returned*1e8), fmt(s.movedNet*1e8), fmt(s.remaining*1e8), s.events.length, dests].join(',');
});
fs.writeFileSync('D:/gestacijski diabetes/Android Studio/bitcoinLotto/docs/refresh-movement-summary.csv', [head, ...rows].join('\n') + '\n');

// per-event CSV
const ehead = 'address,movement_utc,block_height,txid,spent_from_address_btc,returned_as_change_btc,net_out_btc,balance_after_event_btc,destination_addresses,destination_amounts_btc';
const erows = [];
for (const s of summary) {
  const evs = [...s.events].sort((a,b) => a.date.localeCompare(b.date));
  evs.forEach((e, i) => {
    erows.push([s.address, e.date, e.height, e.txid, fmt(e.spent), fmt(e.back), fmt(e.net), '',
      e.dests.map(d => d.a).join('|'), e.dests.map(d => fmt(d.v)).join('|')].join(','));
  });
}
fs.writeFileSync('D:/gestacijski diabetes/Android Studio/bitcoinLotto/docs/refresh-movement-events.csv', [ehead, ...erows].join('\n') + '\n');

const totalMoved = summary.reduce((a, s) => a + s.movedNet, 0);
const totalRemain = summary.reduce((a, s) => a + s.remaining, 0);
let md = `# Re-check of the refreshed address set (19 November 2026)\n\n`;
md += `## What was checked\n\n`;
md += `The bundled database \`core/data/src/main/assets/blockchain.sqlite\` contains **82,086** addresses. `;
md += `It was refreshed on 12 November 2026 (commit \`d65befd\`) by keeping only rows where `;
md += `\`status == still_funded\` and \`current_sats >= snapshot_sats\` in \`docs/address-audit.csv\`. `;
md += `Every one of those 82,086 addresses was re-checked against the live blockchain via public Electrum servers `;
md += `(\`blockchain.scripthash.get_history\` / \`get_balance\`) and the transaction details were resolved through `;
md += `\`mempool.space/api/tx\`.\n\n`;
md += `## Result\n\n`;
md += `**${summary.length}** of the refreshed addresses have moved bitcoin out of them. `;
md += `Net outflow across them: **${totalMoved.toFixed(8)} BTC**; remaining in those addresses: **${totalRemain.toFixed(8)} BTC**. `;
md += `All movements are dated between **12 and 18 November 2026** — i.e. after the audit snapshot (11 November) `;
md += `and, in 18 of 19 cases, after the refresh itself (12 November 17:32 UTC).\n\n`;
md += `The other addresses in the database show either no transaction after 8 November 2026 or only incoming dust; `;
md += `they have not moved coins out in this window.\n\n`;
md += `## Per-address result\n\n`;
md += `| Address | First movement (UTC) | Last movement (UTC) | Moved out (BTC, net) | Remaining (BTC) | Destination(s) |\n`;
md += `|---|---|---|---:|---:|---|\n`;
for (const s of summary) {
  const first = s.events.map(e => e.date).sort()[0];
  const dests = [...new Set(s.events.flatMap(e => e.dests.map(d => d.a)))];
  const short = dests.map(d => d.length > 14 ? d.slice(0,10) + '…' + d.slice(-6) : d).join('<br>');
  md += `| \`${s.address}\` | ${first} | ${s.lastSpend} | ${s.movedNet.toFixed(8)} | ${s.remaining.toFixed(8)} | ${short} |\n`;
}
md += `\n"Moved out" is the value of inputs spent from the address minus any change returned to it in the same transaction. `;
md += `"Remaining" is the confirmed balance on 19 November 2026. Full per-event detail, including txids and destination `;
md += `amounts, is in \`docs/refresh-movement-events.csv\`; a per-address roll-up is in \`docs/refresh-movement-summary.csv\`.\n\n`;
md += `## Notes\n\n`;
md += `- \`1NoWYaupfeKF93AiXkuPM12JjiaMRw5bZF\` moved on 12 November 02:08 UTC — after the 11 November balance `;
md += `snapshot but a few hours before the refresh commit.\n`;
md += `- Several of the 19 addresses spent in multiple transactions (e.g. \`1GdN9v11…\` in four, \`1AgiG4fz…\` in five); `;
md += `the table shows the first and last, while the CSV lists each transaction separately.\n`;
md += `- This re-check only looks for *outgoing* movement. Incoming dust is ignored.\n`;
fs.writeFileSync('D:/gestacijski diabetes/Android Studio/bitcoinLotto/docs/refresh-movement-audit.md', md);
console.log('addresses', summary.length, 'events', erows.length, 'totalMoved', totalMoved.toFixed(8), 'totalRemain', totalRemain.toFixed(8));
