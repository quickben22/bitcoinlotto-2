const fs = require('fs');
const chains = require('./chains.json');
const OUT = 'D:/gestacijski diabetes/Android Studio/bitcoinLotto/docs/';
const f = n => (n / 1e8).toFixed(8);
const short = a => a.length > 20 ? a.slice(0, 14) + '…' + a.slice(-6) : a;

const csv = ['source_address,moves,from_address,to_address,utc,spend_txid,amount_btc,status'];
for (const r of chains) {
  const moves = r.chain.length - 1;
  r.chain.forEach((x, i) => {
    const from = i === 0 ? r.source : r.chain[i - 1].address;
    csv.push([r.source, moves, from, x.address, x.date || '', x.spendTxid || '', f(x.value), x.status].join(','));
  });
}
fs.writeFileSync(OUT + 'refresh-biggest-chain.csv', csv.join('\n') + '\n');

let md = '# Largest-output chain for each of the 19 addresses (12–19 November 2026)\n\n';
md += `For each source address I took its largest outgoing payment and then, at every subsequent address, `;
md += `followed only the **largest output** of the transaction that spent it, until the coins reached an `;
md += `address with no further outgoing movement in the window. Your example reproduces exactly: `;
md += `\`1FFQgpEsQwqyf5psi5tayYFj7Yoz6Ru1tC\` settles at `;
md += `\`bc1qvwz4luueypyhthfz7tg0cq9klkfpq34e923x4t\`.\n\n`;
md += `## Summary\n\n| Source | Moves | Final address | Final BTC |\n|---|---:|---|---:|\n`;
for (const r of chains) {
  const last = r.chain[r.chain.length - 1];
  md += `| \`${r.source}\` | ${r.chain.length - 1} | \`${last.address}\` | ${f(last.value)} |\n`;
}
md += `\n## Full chains\n\n`;
for (const r of chains) {
  const last = r.chain[r.chain.length - 1];
  md += `### \`${r.source}\` — ${r.chain.length - 1} moves, settles at \`${last.address}\`\n\n`;
  md += `| # | Address | Arrived (UTC) | BTC at that address | Status |\n|---:|---|---|---:|---|\n`;
  r.chain.forEach((x, i) => {
    md += `| ${i} | \`${x.address}\` | ${(x.date || '').replace('T', ' ').slice(0, 19)} | ${f(x.value)} | ${x.status} |\n`;
  });
  md += '\n';
}
md += `Every transaction id is recorded in \`docs/refresh-biggest-chain.csv\`.\n\n`;
md += `## Notes\n\n`;
md += `- A "move" is one transaction that spent the tracked output. The starting address itself is hop 0.\n`;
md += `- Where a spending transaction had several outputs, only the largest one was followed; the rest are `;
md += `recorded in \`nextOutputs\` in the underlying data and in the CSV only as the chosen continuation.\n`;
md += `- Some large consolidation wallets pull in value from unrelated depositors. When the largest output of `;
md += `such a transaction belongs to a different pool, the chain can jump to a much larger amount — that is `;
md += `the literal result of "always follow the biggest amount", not an attribution of the original coins.\n`;
md += `- All chains end with status UNSPENT, i.e. the tracked amount was still sitting at that address on 19 November 2026.\n`;
fs.writeFileSync(OUT + 'refresh-biggest-chain.md', md);
console.log('chains', chains.length, 'total moves', chains.reduce((s, r) => s + r.chain.length - 1, 0));
