# Re-check of the refreshed address set (19 November 2026)

## What was checked

The bundled database `core/data/src/main/assets/blockchain.sqlite` contains **82,086** addresses. It was refreshed on 12 November 2026 (commit `d65befd`) by keeping only rows where `status == still_funded` and `current_sats >= snapshot_sats` in `docs/address-audit.csv`. Every one of those 82,086 addresses was re-checked against the live blockchain via public Electrum servers (`blockchain.scripthash.get_history` / `get_balance`) and the transaction details were resolved through `mempool.space/api/tx`.

## Result

**19** of the refreshed addresses have moved bitcoin out of them. Net outflow across them: **241.42905423 BTC**; remaining in those addresses: **3.80094587 BTC**. All movements are dated between **12 and 18 November 2026** — i.e. after the audit snapshot (11 November) and, in 18 of 19 cases, after the refresh itself (12 November 17:32 UTC).

The other addresses in the database show either no transaction after 8 November 2026 or only incoming dust; they have not moved coins out in this window.

## Per-address result

| Address | First movement (UTC) | Last movement (UTC) | Moved out (BTC, net) | Remaining (BTC) | Destination(s) |
|---|---|---|---:|---:|---|
| `1313TARBFzoSbXQUL7yu32p9TjoBhUPx2M` | 2026-11-12T22:46:48.000Z | 2026-11-12T22:46:48.000Z | 75.00002187 | 0.00000000 | bc1q7upzc4…zrcsd9 |
| `1ALpEWtXApR19hLXvcG8NfkPyGsUMhFPMS` | 2026-11-17T02:52:26.000Z | 2026-11-17T02:52:26.000Z | 75.00000000 | 0.00102187 | bc1q8s6khy…y69gpp |
| `1GdmnuPz5h9SsUvtiFWqTpJ7h28i8hTUQG` | 2026-11-14T06:03:55.000Z | 2026-11-14T06:03:55.000Z | 25.00001640 | 0.00000000 | bc1q4ay3j0…epdzk5 |
| `1GdN9v11u8ecZF4USpj6GqKhazTxUvy7aY` | 2026-11-14T07:05:24.000Z | 2026-11-18T20:37:49.000Z | 22.00005097 | 3.00007100 | bc1qr6rxds…dtnk32 |
| `1NoWYaupfeKF93AiXkuPM12JjiaMRw5bZF` | 2026-11-12T02:08:34.000Z | 2026-11-12T02:08:34.000Z | 18.26000000 | 0.00001500 | bc1q366ast…nam297 |
| `1AgiG4fzC2Z6fQ1Ltf6tEz9sUBCY1ay2za` | 2026-11-14T11:13:34.000Z | 2026-11-14T19:17:52.000Z | 9.99999400 | 0.00000600 | 361mDjTHQp…uYSCfH<br>bc1qk2ms3w…jlguzk |
| `1BVbQyATQRkN3Y7MX7aspjZsqnrnN2CWbN` | 2026-11-15T19:58:06.000Z | 2026-11-15T19:58:06.000Z | 5.00000002 | 0.00000000 | 3PDJKAUT1B…uaqNiu |
| `1CCoK3bUtothK5GN9vos1mWthdjoa4BqYa` | 2026-11-13T06:45:02.000Z | 2026-11-13T18:33:06.000Z | 5.00000000 | 0.00000000 | 39bUFFWPRc…eVV9cL |
| `1CA7ysJMPpUav91cHgQ7Aom7suzY9w7YBw` | 2026-11-14T17:09:34.000Z | 2026-11-14T19:34:22.000Z | 1.00000000 | 0.00000000 | 361mDjTHQp…uYSCfH<br>bc1qk2ms3w…jlguzk |
| `1CAPqy7AhPehwvmUPouVbVGJsNnvWjjiWY` | 2026-11-14T20:51:55.000Z | 2026-11-14T20:51:55.000Z | 1.00000000 | 0.00000000 | bc1qmdpkdf…sa4fls |
| `1Car858fC2J1ZsCv5dmvZbHtKP3Nk2RN8R` | 2026-11-14T19:41:02.000Z | 2026-11-14T19:41:02.000Z | 1.00000000 | 0.00000000 | bc1qu8xlp3…wf54ef |
| `1CsRw431siXGryywxgvreDU1vbbEiXGC7g` | 2026-11-14T20:19:53.000Z | 2026-11-14T20:19:53.000Z | 1.00000000 | 0.00000000 | bc1qqwgeqg…0xwrpd |
| `1D5ngkYujbuwNw8XDkm5MiLgwRf467o1ne` | 2026-11-13T18:33:06.000Z | 2026-11-14T19:41:02.000Z | 1.00000000 | 0.00000000 | 361mDjTHQp…uYSCfH<br>bc1qk2ms3w…jlguzk |
| `1738b4HYtP312grgHBMXujFwZQnhveYWuj` | 2026-11-18T11:01:11.000Z | 2026-11-18T11:01:11.000Z | 0.76876467 | 0.00000000 | bc1qfr0gsp…u4gdh2 |
| `1PAT2UPBB9vDC5VXZTjEHnKa9yu33zo7jC` | 2026-11-17T21:38:45.000Z | 2026-11-18T21:15:40.000Z | 0.20016800 | 0.79983200 | 33SxuZH3KY…PfFY45<br>32CeBmRph2…KSPvwm |
| `1G47pVbJ7M1JSmZiqzLoVUw2FzyfUuDEuF` | 2026-11-14T19:53:51.000Z | 2026-11-14T19:53:51.000Z | 0.10000001 | 0.00000000 | 3H4o9sFVx3…WNN35c |
| `1HnNoijUSSNMH7SMGZKKsgwEqjCxxHKeN2` | 2026-11-17T03:42:41.000Z | 2026-11-17T03:42:41.000Z | 0.10000000 | 0.00000000 | bc1qky8jzu…urzn32 |
| `1BU7pYQvW6CTaLhU39iDh7keaKbTpK4HXC` | 2026-11-13T04:48:31.000Z | 2026-11-13T04:48:31.000Z | 0.00002188 | 0.00000000 | bc1qj5hhw3…0nvzr6 |
| `1FFQgpEsQwqyf5psi5tayYFj7Yoz6Ru1tC` | 2026-11-13T04:48:31.000Z | 2026-11-13T04:48:31.000Z | 0.00001641 | 0.00000000 | bc1qj5hhw3…0nvzr6 |

"Moved out" is the value of inputs spent from the address minus any change returned to it in the same transaction. "Remaining" is the confirmed balance on 19 November 2026. Full per-event detail, including txids and destination amounts, is in `docs/refresh-movement-events.csv`; a per-address roll-up is in `docs/refresh-movement-summary.csv`.

## Notes

- `1NoWYaupfeKF93AiXkuPM12JjiaMRw5bZF` moved on 12 November 02:08 UTC — after the 11 November balance snapshot but a few hours before the refresh commit.
- Several of the 19 addresses spent in multiple transactions (e.g. `1GdN9v11…` in four, `1AgiG4fz…` in five); the table shows the first and last, while the CSV lists each transaction separately.
- This re-check only looks for *outgoing* movement. Incoming dust is ignored.
- Two rows (`1BU7pYQ…` and `1FFQgpEs…`) carry 50 BTC each in the database but have never held more than a few thousand satoshis on-chain; their only outgoing movement is dust (0.00002188 and 0.00001641 BTC). Their database balances appear to be wrong, independent of the movement check.
