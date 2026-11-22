# Where the bitcoin went — forward trace, 12–19 November 2026

## Scope

The 34 outgoing transactions from the 19 addresses were traced forward through the chain. Only movements confirmed on-chain between **12 November 00:00 UTC and 19 November 24:00 UTC** are counted. Addresses were followed one hop further where the coins moved again in that window. At each hop the **largest output** is the main continuation; where a receiving address is a shared/commingled wallet (many depositors), an output can contain coins from other sources — that is flagged below.

## Headline numbers

| Metric | Value |
|---|---|
| Outgoing transactions from the 19 source addresses (hop 1) | **34** |
| Destination addresses at hop 1 | **18** |
| Destination addresses that moved the coins again in the window (hop 2) | **13** |
| Further transactions observed at hop 2 | **30** |
| Destinations still holding the coins untouched at 19 Nov | **5** |

## Where the coins ended up

### A. Still sitting at the first destination (no further movement)

| Address | BTC received from the 19 | Current balance |
|---|---:|---:|
| `bc1q7upzc44p5v9wt5se40xt4grpwzalfe85zrcsd9` | 75.00000978 | 75.00000978 |
| `bc1q8s6khyqw7rarqw0wr9s23mrxjdj2qq40y69gpp` | 74.99999601 | 74.99999934 |
| `bc1q366ast325w0s7rjl4vf57jcwg5vp8nhqnam297` | 18.25998895 | 18.30997790 |
| `bc1qk2ms3wjjuk56hva893lqp23lhmjjnzlfjlguzk` | 3.09900912 | 3.09901245 |
| `bc1qfr0gspw03yw4pudrr9s7609wyf9t3yjyu4gdh2` | 0.76876246 | 0.76876246 |

### B. Moved again — main continuation (largest output at hop 2)

| Hop-1 destination | Hop-2 date (UTC) | Main next address | Output BTC | Note |
|---|---|---|---:|---|
| `bc1q4ay3j0h8…epdzk5` | 2026-11-14T06:17:29.000Z | `37d2CQCn6rpq…gddq4M` | 24.99996930 |  |
| `bc1qr6rxdsk4…dtnk32` | 2026-11-14T08:45:47.000Z | `bc1qppunwskz…rc76y6` | 5.05379564 | multiple sources converged here (2 outputs) |
| `bc1qr6rxdsk4…dtnk32` | 2026-11-14T16:57:14.000Z | `bc1qppunwskz…rc76y6` | 11.69993903 | multiple sources converged here (2 outputs) |
| `bc1qr6rxdsk4…dtnk32` | 2026-11-16T16:22:01.000Z | `bc1qppunwskz…rc76y6` | 119.57374785 | multiple sources converged here (2 outputs) |
| `bc1qr6rxdsk4…dtnk32` | 2026-11-19T00:20:56.000Z | `bc1qppunwskz…rc76y6` | 11.09558518 | multiple sources converged here (2 outputs) |
| `361mDjTHQpo6…uYSCfH` | 2026-11-13T20:59:47.000Z | `bc1qsef50clf…tam2vl` | 5.91369999 |  |
| `361mDjTHQpo6…uYSCfH` | 2026-11-14T07:56:05.000Z | `bc1qqrkxc5aw…g7fpya` | 0.24718995 |  (19 outputs) |
| `361mDjTHQpo6…uYSCfH` | 2026-11-14T11:24:48.000Z | `bc1qpralxa3k…6jjwss` | 1.96800000 | multiple sources converged here (9 outputs) |
| `361mDjTHQpo6…uYSCfH` | 2026-11-14T11:24:48.000Z | `bc1qpralxa3k…6jjwss` | 0.56107000 | multiple sources converged here (19 outputs) |
| `361mDjTHQpo6…uYSCfH` | 2026-11-14T12:04:55.000Z | `bc1qpralxa3k…6jjwss` | 3.80000000 | multiple sources converged here (18 outputs) |
| `361mDjTHQpo6…uYSCfH` | 2026-11-14T19:30:44.000Z | `bc1qpralxa3k…6jjwss` | 0.83435463 | multiple sources converged here (36 outputs) |
| `361mDjTHQpo6…uYSCfH` | 2026-11-14T22:18:52.000Z | `bc1qpralxa3k…6jjwss` | 0.71406715 | multiple sources converged here (36 outputs) |
| `3PDJKAUT1Bha…uaqNiu` | 2026-11-15T20:08:54.000Z | `bc1qlt5nm3kf…cu4na0` | 5.00721689 |  |
| `3PDJKAUT1Bha…uaqNiu` | 2026-11-17T01:41:17.000Z | `bc1qlt5nm3kf…cu4na0` | 5.00038327 |  |
| `39bUFFWPRcXp…eVV9cL` | 2026-11-13T08:23:52.000Z | `bc1qppunwskz…rc76y6` | 14.81592879 | multiple sources converged here (2 outputs) |
| `39bUFFWPRcXp…eVV9cL` | 2026-11-13T12:28:13.000Z | `bc1qppunwskz…rc76y6` | 40.43504771 | multiple sources converged here (2 outputs) |
| `39bUFFWPRcXp…eVV9cL` | 2026-11-14T00:45:27.000Z | `bc1qppunwskz…rc76y6` | 103.74472614 | multiple sources converged here (2 outputs) |
| `bc1qmdpkdfu9…sa4fls` | 2026-11-14T20:51:55.000Z | `bc1q0dkf8x6r…ar3yx0` | 0.30813500 |  (4 outputs) |
| `bc1qu8xlp3jl…wf54ef` | 2026-11-15T21:52:31.000Z | `bc1qcuvxs62p…8zzuv2` | 0.89999738 |  (2 outputs) |
| `bc1qqwgeqgej…0xwrpd` | 2026-11-14T20:19:53.000Z | `bc1qku4zs8lv…r20kc8` | 0.99999635 |  |
| `33SxuZH3KYUw…PfFY45` | 2026-11-17T21:54:41.000Z | `bc1qmuepnly4…xxk5u2` | 4.94630781 |  |
| `33SxuZH3KYUw…PfFY45` | 2026-11-18T21:54:58.000Z | `bc1q3vc3rlev…yjynu4` | 2.75365388 |  |
| `32CeBmRph2Xt…KSPvwm` | 2026-11-17T22:42:18.000Z | `bc1q5a30r8jd…kl2d9t` | 3.11417450 |  |
| `3H4o9sFVx3RU…WNN35c` | 2026-11-14T19:00:58.000Z | `bc1qm7zhzxwz…etty8p` | 2.31543395 |  |
| `3H4o9sFVx3RU…WNN35c` | 2026-11-14T21:22:13.000Z | `bc1qpralxa3k…6jjwss` | 1.95884154 | multiple sources converged here (46 outputs) |
| `3H4o9sFVx3RU…WNN35c` | 2026-11-18T15:30:10.000Z | `bc1qk6taqkr9…rw76zx` | 2.77024159 |  |
| `bc1qky8jzu28…urzn32` | 2026-11-12T09:38:32.000Z | `bc1qle99f93v…f2a8zl` | 15.90687247 |  (2 outputs) |
| `bc1qky8jzu28…urzn32` | 2026-11-14T05:47:32.000Z | `bc1qle99f93v…f2a8zl` | 15.83268521 |  (3 outputs) |
| `bc1qky8jzu28…urzn32` | 2026-11-17T03:55:56.000Z | `bc1qle99f93v…f2a8zl` | 32.54941540 |  (2 outputs) |
| `bc1qj5hhw38g…0nvzr6` | 2026-11-13T18:12:05.000Z | `bc1qp3pfmfrv…9vn4n2` | 58.97990532 |  (2 outputs) |

### C. Main consolidation hubs seen at hop 2

- `bc1qppunwskzl7t3mtmcarlvpxjuzd8jy7l7ga49av2c78xg47fujncsrc76y6` — 306.41877034 BTC received from 2 distinct hop-1 destinations
- `bc1qle99f93vzvr2sn28al7vnwrgews6clw5f2a8zl` — 64.28897308 BTC received from 1 distinct hop-1 destination
- `bc1qp3pfmfrvu5wtlu3waqn03xe52gzcly799vn4n2` — 58.97990532 BTC received from 1 distinct hop-1 destination
- `37d2CQCn6rpq9g9XohxhENjxaHKUgddq4M` — 24.99996930 BTC received from 1 distinct hop-1 destination
- `bc1qlt5nm3kflne7rht4alsnzdzad878ld5rcu4na0` — 10.00760016 BTC received from 1 distinct hop-1 destination
- `bc1qpralxa3ke2vk8yflyjq5z3eefzrqz35snxaazcvq5n8tpug8u8lq6jjwss` — 9.83633332 BTC received from 2 distinct hop-1 destinations
- `bc1qsef50clfv6xn7l7v8ekfwuawcq2ktgrqtam2vl` — 5.91369999 BTC received from 1 distinct hop-1 destination
- `bc1qmuepnly4lwpw08e4f3cn8r2xg57m3kk5xxk5u2` — 4.94630781 BTC received from 1 distinct hop-1 destination
- `bc1q5a30r8jd2gmtp3r9ds3ugc864f9e9vr4kl2d9t` — 3.11417450 BTC received from 1 distinct hop-1 destination
- `bc1qk6taqkr9u6yshxry2jhhnpdymqj6865srw76zx` — 2.77024159 BTC received from 1 distinct hop-1 destination
- `bc1q3vc3rlevq6gkf6da8apg709m2ppcvxdfyjynu4` — 2.75365388 BTC received from 1 distinct hop-1 destination
- `bc1qm7zhzxwzlpvcthwz8lz8amukmc04py29etty8p` — 2.31543395 BTC received from 1 distinct hop-1 destination

These hub addresses receive from several of the hop-1 destinations in the same window, so they look like exchange or custodial consolidation wallets. Once coins arrive there they are mixed with other funds and can no longer be attributed to a specific source address from public data alone.

## Per-source summary

| Source address | Times moved (hop 1) | Destinations | BTC moved (net) |
|---|---:|---|---:|
| `1313TARBFzoSbXQUL7yu32p9TjoBhUPx2M` | 1 | bc1q7upzc44p…zrcsd9 | 75.00002187 |
| `1ALpEWtXApR19hLXvcG8NfkPyGsUMhFPMS` | 1 | bc1q8s6khyqw…y69gpp | 75.00000000 |
| `1GdmnuPz5h9SsUvtiFWqTpJ7h28i8hTUQG` | 1 | bc1q4ay3j0h8…epdzk5 | 25.00001640 |
| `1GdN9v11u8ecZF4USpj6GqKhazTxUvy7aY` | 4 | bc1qr6rxdsk4…dtnk32 | 22.00005097 |
| `1NoWYaupfeKF93AiXkuPM12JjiaMRw5bZF` | 1 | bc1q366ast32…nam297 | 18.26000000 |
| `1AgiG4fzC2Z6fQ1Ltf6tEz9sUBCY1ay2za` | 5 | 361mDjTHQpo6…uYSCfH<br>bc1qk2ms3wjj…jlguzk | 9.99999400 |
| `1BVbQyATQRkN3Y7MX7aspjZsqnrnN2CWbN` | 1 | 3PDJKAUT1Bha…uaqNiu | 5.00000002 |
| `1CCoK3bUtothK5GN9vos1mWthdjoa4BqYa` | 3 | 39bUFFWPRcXp…eVV9cL | 5.00000000 |
| `1CA7ysJMPpUav91cHgQ7Aom7suzY9w7YBw` | 2 | 361mDjTHQpo6…uYSCfH<br>bc1qk2ms3wjj…jlguzk | 1.00000000 |
| `1CAPqy7AhPehwvmUPouVbVGJsNnvWjjiWY` | 1 | bc1qmdpkdfu9…sa4fls | 1.00000000 |
| `1Car858fC2J1ZsCv5dmvZbHtKP3Nk2RN8R` | 1 | bc1qu8xlp3jl…wf54ef | 1.00000000 |
| `1CsRw431siXGryywxgvreDU1vbbEiXGC7g` | 1 | bc1qqwgeqgej…0xwrpd | 1.00000000 |
| `1D5ngkYujbuwNw8XDkm5MiLgwRf467o1ne` | 4 | 361mDjTHQpo6…uYSCfH<br>bc1qk2ms3wjj…jlguzk | 1.00000000 |
| `1738b4HYtP312grgHBMXujFwZQnhveYWuj` | 1 | bc1qfr0gspw0…u4gdh2 | 0.76876467 |
| `1PAT2UPBB9vDC5VXZTjEHnKa9yu33zo7jC` | 3 | 33SxuZH3KYUw…PfFY45<br>32CeBmRph2Xt…KSPvwm | 0.20016800 |
| `1G47pVbJ7M1JSmZiqzLoVUw2FzyfUuDEuF` | 1 | 3H4o9sFVx3RU…WNN35c | 0.10000001 |
| `1HnNoijUSSNMH7SMGZKKsgwEqjCxxHKeN2` | 1 | bc1qky8jzu28…urzn32 | 0.10000000 |
| `1BU7pYQvW6CTaLhU39iDh7keaKbTpK4HXC` | 1 | bc1qj5hhw38g…0nvzr6 | 0.00002188 |
| `1FFQgpEsQwqyf5psi5tayYFj7Yoz6Ru1tC` | 1 | bc1qj5hhw38g…0nvzr6 | 0.00001641 |

Full row-by-row detail (every txid, timestamp, amount and destination at hops 1 and 2) is in `docs/refresh-movement-flow.csv`.

## Caveats

- Hop-2 amounts are the full value spent by the destination address; if that address pooled coins from other depositors, only part of the amount belongs to the 19 original addresses.
- Tracing beyond hop 2 was not continued: several hop-2 outputs land in high-throughput consolidation wallets where the public chain cannot separate individual deposits.
- Five destination addresses (section A) have never spent the received coins — for those the answer is unambiguous: the bitcoin is still at that address.
