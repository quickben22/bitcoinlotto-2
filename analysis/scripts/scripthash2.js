const crypto = require('crypto');
const { b58decode } = require('./scripthash');

function sha256r(script) { return Buffer.from(crypto.createHash('sha256').update(script).digest()).reverse().toString('hex'); }

function bech32Decode(str) {
  const CHARSET = 'qpzry9x8gf2tvdw0s3jn54khce6mua7l';
  const pos = str.lastIndexOf('1');
  const hrp = str.slice(0, pos), data = str.slice(pos + 1);
  let values = [];
  for (const c of data) { const i = CHARSET.indexOf(c); if (i < 0) throw new Error('bad bech32 char'); values.push(i); }
  // convert 5-bit to 8-bit, drop checksum (6 chars)
  const version = values[0];
  const payload = values.slice(1, -6);
  let acc = 0, bits = 0, bytes = [];
  for (const v of payload) { acc = (acc << 5) | v; bits += 5; while (bits >= 8) { bits -= 8; bytes.push((acc >> bits) & 0xff); } }
  return { hrp, version, program: Buffer.from(bytes) };
}

function addressToScripthash(addr) {
  if (addr.startsWith('1')) {
    const raw = b58decode(addr), h160 = raw.slice(1, 21);
    return sha256r(Buffer.concat([Buffer.from([0x76, 0xa9, 0x14]), h160, Buffer.from([0x88, 0xac])]));
  }
  if (addr.startsWith('3')) {
    const raw = b58decode(addr), h160 = raw.slice(1, 21);
    return sha256r(Buffer.concat([Buffer.from([0xa9, 0x14]), h160, Buffer.from([0x87])]));
  }
  if (addr.startsWith('bc1')) {
    const d = bech32Decode(addr);
    if (d.version === 0 && d.program.length === 20) return sha256r(Buffer.concat([Buffer.from([0x00, 0x14]), d.program]));
    if (d.version === 0 && d.program.length === 32) return sha256r(Buffer.concat([Buffer.from([0x00, 0x20]), d.program]));
    if (d.version === 1 && d.program.length === 32) return sha256r(Buffer.concat([Buffer.from([0x51, 0x20]), d.program]));
    throw new Error('unsupported bech32 witness ' + d.version + '/' + d.program.length);
  }
  throw new Error('unsupported address ' + addr);
}
module.exports = { addressToScripthash };
