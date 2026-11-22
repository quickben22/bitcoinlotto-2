// Render a vertical flow diagram of the largest-output BTC chain for one source address.
// Usage: cd analysis/data && node ../scripts/make_chain_graphic.js [sourceAddress]
// Output: docs/btc-flow-<first8ofsource>.svg  (relative to the repository root)
const fs = require('fs');
const path = require('path');

const dataDir = process.cwd();
const repoRoot = path.resolve(dataDir, '..', '..');
const chains = JSON.parse(fs.readFileSync(path.join(dataDir, 'chains.json'), 'utf8'));
const source = process.argv[2] || '1FFQgpEsQwqyf5psi5tayYFj7Yoz6Ru1tC';
const rec = chains.find((c) => c.source === source);
if (!rec) throw new Error(`No chain found for ${source}`);
const enrichPath = path.join(repoRoot, 'docs', 'chain-enrichment.json');
const enrich = fs.existsSync(enrichPath) ? JSON.parse(fs.readFileSync(enrichPath, 'utf8'))[source] : null;

const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
function fmtDate(iso) {
  const d = new Date(iso);
  const p = (n) => String(n).padStart(2, '0');
  return `${p(d.getUTCDate())} ${MONTHS[d.getUTCMonth()]} ${d.getUTCFullYear()} ${p(d.getUTCHours())}:${p(d.getUTCMinutes())} UTC`;
}
function shortTx(tx) {
  return tx ? `${tx.slice(0, 10)}…${tx.slice(-8)}` : '';
}
function splitAddr(a) {
  const mid = Math.ceil(a.length / 2);
  return [a.slice(0, mid), a.slice(mid)];
}
function esc(s) {
  return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}
function fmtDuration(sec) {
  const days = sec / 86400;
  if (days < 60) return `${days.toFixed(1)} days`;
  const years = days / 365.2425;
  return `${years.toFixed(2)} years (${Math.round(days)} days)`;
}

const nodes = rec.chain;
const first = nodes[0];
const items = [
  {
    label: 'SOURCE',
    address: source,
    btc: first.value / 1e8,
    date: first.date,
    status: 'SOURCE',
    creatingTx: first.txid,
    spendTx: first.spendTxid,
    source: true,
  },
  ...nodes.map((n, i) => ({
    label: i === nodes.length - 1 ? `HOP ${i} · FINAL` : `HOP ${i}`,
    address: n.address,
    btc: n.value / 1e8,
    date: n.date,
    status: n.status,
    creatingTx: n.txid,
    spendTx: n.spendTxid,
    final: i === nodes.length - 1,
  })),
];

const CARD_X = 70;
const CARD_W = 860;
const CARD_H = 104;
const STEP = 176;
const INFO_H = 132; // dormancy + attribution panel below the header
const TOP = 170 + INFO_H;
const W = 1000;
const H = TOP + items.length * STEP + 70;

let svg = '';
svg += `<svg xmlns="http://www.w3.org/2000/svg" width="${W}" height="${H}" viewBox="0 0 ${W} ${H}" font-family="Segoe UI, Helvetica, Arial, sans-serif">\n`;
svg += `<defs>
  <marker id="arrow" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="8" markerHeight="8" orient="auto-start-reverse">
    <path d="M 0 0 L 10 5 L 0 10 z" fill="#00e5ff"/>
  </marker>
  <linearGradient id="hdr" x1="0" y1="0" x2="1" y2="0">
    <stop offset="0" stop-color="#00e5ff"/><stop offset="1" stop-color="#39ff88"/>
  </linearGradient>
</defs>\n`;
svg += `<rect width="${W}" height="${H}" fill="#0a0e13"/>\n`;
svg += `<rect x="0" y="0" width="${W}" height="6" fill="url(#hdr)"/>\n`;

svg += `<text x="${CARD_X}" y="56" fill="#e8f6ff" font-size="29" font-weight="700">BTC FLOW · ${esc(source.slice(0, 10))}…${esc(source.slice(-6))}</text>\n`;
svg += `<text x="${CARD_X}" y="84" fill="#8aa0b0" font-size="15">Largest-output chain · 12–19 Nov 2026 · ${nodes.length - 1} moves · ends UNSPENT</text>\n`;
svg += `<circle cx="${CARD_X + 6}" cy="116" r="6" fill="#f0a020"/><text x="${CARD_X + 20}" y="121" fill="#8aa0b0" font-size="13">source</text>`;
svg += `<circle cx="${CARD_X + 110}" cy="116" r="6" fill="#00e5ff"/><text x="${CARD_X + 124}" y="121" fill="#8aa0b0" font-size="13">intermediate hop</text>`;
svg += `<circle cx="${CARD_X + 290}" cy="116" r="6" fill="#39ff88"/><text x="${CARD_X + 304}" y="121" fill="#8aa0b0" font-size="13">final, unspent output</text>\n`;

// Dormancy + attribution panel
if (enrich) {
  const PY = 140;
  svg += `<rect x="${CARD_X}" y="${PY}" width="${CARD_W}" height="112" rx="10" fill="#101922" stroke="#274050"/>\n`;
  const srcLabel = enrich.sourceLabel || 'unknown';
  const finalLabel = enrich.finalLabel || 'unknown';
  const shortLabel = (s) => (s.length > 62 ? s.slice(0, 59) + '…' : s);
  svg += `<text x="${CARD_X + 18}" y="${PY + 26}" fill="#7fb2c8" font-size="11" font-weight="700" letter-spacing="1">DORMANCY</text>\n`;
  svg += `<text x="${CARD_X + 18}" y="${PY + 50}" fill="#e8f6ff" font-size="17" font-weight="700">${esc(fmtDate(enrich.dormantSince) + ' → ' + fmtDate(items[0].date))}</text>\n`;
  svg += `<text x="${CARD_X + 18}" y="${PY + 72}" fill="#39ff88" font-size="16" font-weight="700">${esc(enrich.dormantSeconds != null ? fmtDuration(enrich.dormantSeconds) : 'unknown')}${enrich.dormantApprox ? ' (approx.)' : ''}</text>\n`;
  svg += `<text x="${CARD_X + 18}" y="${PY + 96}" fill="#8aa0b0" font-size="12">last >= 0.001 BTC tx before 12 Nov → first tx in the 12–19 Nov window</text>\n`;
  svg += `<line x1="${CARD_X + 560}" y1="${PY + 10}" x2="${CARD_X + 560}" y2="${PY + 102}" stroke="#274050"/>\n`;
  svg += `<text x="${CARD_X + 578}" y="${PY + 26}" fill="#7fb2c8" font-size="11" font-weight="700" letter-spacing="1">PUBLIC ATTRIBUTION (WalletExplorer)</text>\n`;
  svg += `<text x="${CARD_X + 578}" y="${PY + 52}" fill="#8aa0b0" font-size="12">source:</text>\n`;
  svg += `<text x="${CARD_X + 578}" y="${PY + 72}" fill="#e8f6ff" font-size="13">${esc(shortLabel(srcLabel))}</text>\n`;
  svg += `<text x="${CARD_X + 578}" y="${PY + 96}" fill="#8aa0b0" font-size="12">final: <tspan fill="#39ff88">${esc(shortLabel(finalLabel))}</tspan></text>\n`;
}


items.forEach((it, i) => {
  const y = TOP + i * STEP;
  const accent = it.final ? '#39ff88' : it.source ? '#f0a020' : '#00e5ff';
  const band = it.final ? '#0f2e22' : it.source ? '#3a2a10' : '#162632';
  const [a1, a2] = splitAddr(it.address);

  // connector from previous node
  if (i > 0) {
    const prevY = TOP + (i - 1) * STEP + CARD_H;
    const moveTx = items[i].creatingTx; // the tx that created this node = the move from the previous one
    svg += `<line x1="${CARD_X + CARD_W / 2}" y1="${prevY + 4}" x2="${CARD_X + CARD_W / 2}" y2="${y - 10}" stroke="#00e5ff" stroke-width="2" marker-end="url(#arrow)"/>\n`;
    if (moveTx) {
      svg += `<text x="${CARD_X + CARD_W / 2 + 16}" y="${prevY + 42}" fill="#6b8798" font-size="11" font-family="Consolas, Menlo, monospace">tx ${esc(shortTx(moveTx))}</text>\n`;
    }
  }

  svg += `<rect x="${CARD_X}" y="${y}" width="${CARD_W}" height="${CARD_H}" rx="10" fill="#121b24" stroke="${accent}" stroke-width="${it.final ? 2 : 1}"/>\n`;
  svg += `<path d="M ${CARD_X + 118} ${y} L ${CARD_X + 118} ${y + CARD_H}" stroke="${accent}" stroke-width="1" opacity="0.5"/>\n`;
  svg += `<rect x="${CARD_X}" y="${y}" width="118" height="${CARD_H}" rx="10" fill="${band}"/>\n`;
  svg += `<rect x="${CARD_X + 108}" y="${y}" width="10" height="${CARD_H}" fill="${band}"/>\n`;
  svg += `<text x="${CARD_X + 59}" y="${y + 48}" text-anchor="middle" fill="${accent}" font-size="12" font-weight="700">${esc(it.label.split(' · ')[0])}</text>\n`;
  if (it.label.includes('FINAL')) {
    svg += `<text x="${CARD_X + 59}" y="${y + 66}" text-anchor="middle" fill="${accent}" font-size="10">FINAL</text>\n`;
  }

  svg += `<text x="${CARD_X + 138}" y="${y + 42}" fill="#dbe9f4" font-size="15" font-family="Consolas, Menlo, monospace">${esc(a1)}</text>\n`;
  svg += `<text x="${CARD_X + 138}" y="${y + 62}" fill="#dbe9f4" font-size="15" font-family="Consolas, Menlo, monospace">${esc(a2)}</text>\n`;
  svg += `<rect x="${CARD_X + 137}" y="${y + 73}" width="${it.final ? 84 : 70}" height="21" rx="10" fill="${it.final ? '#0f2e22' : it.source ? '#3a2a10' : '#162632'}" stroke="${accent}"/>\n`;
  svg += `<text x="${CARD_X + 147}" y="${y + 88}" fill="${accent}" font-size="11" font-weight="700">${esc(it.status.toUpperCase())}</text>\n`;
  svg += `<text x="${CARD_X + (it.final ? 232 : 218)}" y="${y + 88}" fill="#5f7686" font-size="11" font-family="Consolas, Menlo, monospace">tx ${esc(shortTx(it.creatingTx))}</text>\n`;

  svg += `<text x="${CARD_X + CARD_W - 18}" y="${y + 44}" text-anchor="end" fill="${accent}" font-size="22" font-weight="700">${it.btc.toFixed(8)} BTC</text>\n`;
  svg += `<text x="${CARD_X + CARD_W - 18}" y="${y + 70}" text-anchor="end" fill="#8aa0b0" font-size="12">${fmtDate(it.date)}</text>\n`;
  svg += `<text x="${CARD_X + CARD_W - 18}" y="${y + 90}" text-anchor="end" fill="#5f7686" font-size="11">${it.source ? 'first outgoing payment' : 'at this address'}</text>\n`;
});

const BAD_DB_BALANCE = new Set([
  '1BU7pYQvW6CTaLhU39iDh7keaKbTpK4HXC',
  '1FFQgpEsQwqyf5psi5tayYFj7Yoz6Ru1tC',
]);
const notes = [
  'Each arrow is one transaction that spent the previous output; only the largest output was followed.',
];
if (BAD_DB_BALANCE.has(source)) {
  notes.push('This source held 50 BTC from 2010 until 13 Nov 2026 (the DB balance was correct); the traced 58.99 BTC output came from co-spent inputs.');
}
notes.push('Final output was still UNSPENT on 19 Nov 2026. Source: docs/refresh-biggest-chain.csv · analysis/data/chains.json');

const BAD_DB_BALANCE_FIRST_TX = new Set([
  '1BU7pYQvW6CTaLhU39iDh7keaKbTpK4HXC',
  '1FFQgpEsQwqyf5psi5tayYFj7Yoz6Ru1tC',
]);
if (BAD_DB_BALANCE_FIRST_TX.has(source)) {
  notes.push('WARNING: this address contributed only ~0.00002 BTC to the first transaction; the ~58.99 BTC came from other inputs.');
}
notes.push('Attribution = public wallet cluster label, not proof of legal ownership.');

const fy = TOP + items.length * STEP + 14;
notes.forEach((note, i) => {
  svg += `<text x="${CARD_X}" y="${fy + i * 20}" fill="${note.startsWith('WARNING') ? '#f0a020' : '#6b8798'}" font-size="12">${esc(note)}</text>\n`;
});
svg += `</svg>\n`;

const outFile = path.join(repoRoot, 'docs', `btc-flow-${source.slice(0, 8)}.svg`);
fs.writeFileSync(outFile, svg, 'utf8');
console.log(outFile);
