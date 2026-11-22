// Generate SVG + PNG flow graphics for every source chain in chains.json.
// Usage: cd analysis/data && node ../scripts/make_all_chain_graphics.js
const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');

const dataDir = process.cwd();
const repoRoot = path.resolve(dataDir, '..', '..');
const docsDir = path.join(repoRoot, 'docs');
const chains = JSON.parse(fs.readFileSync(path.join(dataDir, 'chains.json'), 'utf8'));
const generator = path.join(dataDir, '..', 'scripts', 'make_chain_graphic.js');
const edge = 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe';
const profile = path.join(process.env.TEMP || 'C:\\Temp', 'edge-chain-shots');
const force = process.argv.includes('--force');

const CARD_STEP = 176;
const BASE_H = 372; // TOP (170+132 panel) + footer allowance (70)

for (const rec of chains) {
  const src = rec.source;
  const svg = path.join(docsDir, `btc-flow-${src.slice(0, 8)}.svg`);
  const png = path.join(docsDir, `btc-flow-${src.slice(0, 8)}.png`);
  if (!force && fs.existsSync(svg) && fs.existsSync(png)) {
    console.log(`skip (exists) ${src}`);
    continue;
  }

  const r = spawnSync(process.execPath, [generator, src], { cwd: dataDir, encoding: 'utf8' });
  if (r.status !== 0) throw new Error(`generator failed for ${src}: ${r.stderr}`);

  const height = BASE_H + (rec.chain.length + 1) * CARD_STEP;
  const fileUrl = 'file:///' + svg.replace(/\\/g, '/');
  const s = spawnSync(edge, [
    '--headless=new', '--disable-gpu', '--no-sandbox', '--hide-scrollbars',
    '--force-device-scale-factor=1',
    `--user-data-dir=${profile}`,
    `--screenshot=${png}`,
    `--window-size=1000,${height}`,
    fileUrl,
  ], { encoding: 'utf8', timeout: 120000 });
  if (s.status !== 0) throw new Error(`render failed for ${src}: ${s.stderr}`);
  console.log(`done ${src}  (${rec.chain.length - 1} moves, ${height}px, ${fs.statSync(png).size} bytes)`);
}
