const fs = require('fs');
const path = require('path');
const d = process.cwd();
const zombies = JSON.parse(fs.readFileSync(path.join(d, 'zombies.json'), 'utf8'));
const histories = JSON.parse(fs.readFileSync(path.join(d, 'histories.json'), 'utf8'));
const zIndex = new Map(zombies.map((z, i) => [z.a, i]));
const addrs = [
  '1CCoK3bUtothK5GN9vos1mWthdjoa4BqYa', '1Cs7NoB7GkWdrff1Bt3gkLGNX6pfa8mqLs',
  '1CS8cKyDzp5vAA8ncCiq5wuUVtPNQYS8PC', '1CsaTpSG8VktZH6b9JH4KjuLyEiq6oLp8V',
  '1CsbS4eAi9diQiFJRnBA3P1sJksU3jDksz', '1Csd9ZuydT1T1fX1Fb5LZrUYBAHPPMqCV5',
  '1CseiwczaMjNg3G358YQUdX42tffaKwDmR', '1CSETpkLi8zNgGJZNSiAiqNb6YTS6LggJX',
  '1CSeZUGkjH4g4FTHnYZUMpPr9vNjHwh3cS', '1CsJ9DEWoqBVECJ5oBs56PQntrq6LBSu7x',
  '1CSogpuH983iMeiaX8yoeoWsjA5hL8GCX6', '1CSPNJcQGFSsJrn6xa4u4xWpCVHbV3FjoS',
  '1CSsHAFYJ54S4e1Nvj2oxWebj36CFvC4Tc', '1CSU2pMg7kEWbrEfWwJTSmJp6gTyaQ3qj',
  '1CSuGxQnACcNBrSFBq4fSy61vva6Xqoq9n', '1CsuZLpqRh9XXMbNTfYoywxTDSdpCJiG9b',
  '1CSv7BZWRxVQ44jyqmB4ZDwEX74RFU5KbA', '1CsVibz4wDk6AzhRHBp5MCVdjrY5v4s4XU',
  '1CSwrciQba7fARQqGn9e7GwTTbioJBQTWe', '1CsY58r5mw1ZL7GaQSvDVLRtPdqxYoTvut',
  '1CSyFHMnKEvbTtLdyANXwoA41i2t9d9qHP', '1Csyx2E93LS5dcByL9oWsx7U7aymEmkEgv',
  '1GdN9v11u8ecZF4USpj6GqKhazTxUvy7aY', '1GLJB9xkYd5T2sXY2JgwHUXis8fNVyGV5e',
];
for (const a of addrs) {
  const i = zIndex.get(a);
  const hs = i === undefined ? null : histories[i];
  const recent = hs ? hs.filter(x => x.h >= 975000).map(x => x.h + ':' + x.tx.slice(0, 8)) : [];
  console.log(a, i === undefined ? 'NOT IN AUDIT' : `idx${i}`, 'recent:', recent.length ? recent.join(' ') : '-');
}
