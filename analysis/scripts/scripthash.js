const crypto = require('crypto');
const ALPHABET = '123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz';

function b58decode(str) {
  let num = 0n;
  for (const ch of str) {
    const idx = ALPHABET.indexOf(ch);
    if (idx < 0) throw new Error('bad base58 char ' + ch);
    num = num * 58n + BigInt(idx);
  }
  let hex = num.toString(16);
  if (hex.length % 2) hex = '0' + hex;
  let bytes = Buffer.from(hex, 'hex');
  // leading zeros
  let zeros = 0;
  for (const ch of str) { if (ch === '1') zeros++; else break; }
  return Buffer.concat([Buffer.alloc(zeros), bytes]);
}

function addressToScripthash(addr) {
  const raw = b58decode(addr);
  // raw = version(1) + hash160(20) + checksum(4)
  const h160 = raw.slice(1, 21);
  const script = Buffer.concat([Buffer.from([0x76, 0xa9, 0x14]), h160, Buffer.from([0x88, 0xac])]);
  const sha = crypto.createHash('sha256').update(script).digest();
  return Buffer.from(sha).reverse().toString('hex');
}

module.exports = { addressToScripthash, b58decode };
