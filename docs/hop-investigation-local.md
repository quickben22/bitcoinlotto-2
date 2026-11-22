# Local investigation: why the 19 traced BTC chains have 7–27 hops

Local, offline analysis only — no network calls. Working directory: repository root.
Data: `analysis/data/chains.json`, `txdetails.json`, `alltrace_txs.json`, `address_txs_cache.json`,
`histories.json`, `zombies.json`, `last_activity_times.json`, `dormancy.json`;
`docs/chain-attribution.json`, `chain-enrichment.json`, `refresh-biggest-chain.{md,csv}`,
`SESSION_2026-11-19.md`. Window: 12–19 Nov 2026.

**Conventions.** In `chains.json` each node is one hop: `address` received `value` sats in `txid`;
`nextOutputs[].v` is in BTC. For the frequency analysis I call the maximum-`v` output of a node the
**largest/continued** output (the tracer follows it) and every other output a **small** output.

---

## Executive summary

- 19 chains contain **153 hop nodes** but only **105 distinct spending txids** and **65 distinct node
  addresses**. Counting all `nextOutputs`, the graph has 780 addresses and 1,222 output entries.
- 29 of the 134 nominal hops are the *same transactions* reused by two chains. Three chain pairs are
  effectively one path: `1AgiG4fz`/`1G47pVbJ` share **21 txids**, `1CA7ysJM`/`1D5ngkYu` share **19**,
  `1BU7pYQ`/`1FFQgpEs` share **8** (all of them).
- **91 of 134 hops (68%)** occur *after* a >3× upward value jump — i.e. inside high-throughput
  custodial/exchange wallets, not in the original coins' path. Only ~43 hops are genuine
  source-side peels, and 14 of those are the duplicated `1BU7pYQ`/`1FFQgpEs` path.
- A small set of relay addresses is reused across nominally unrelated sources:
  `bc1qpralxa3…jjwss` and `352zzNHvs…QB2` each appear in **5 chains**; together with `361mDjTHQ…` they
  belong to one WalletExplorer cluster `000000030ae8727e`.
- Genuine peels use **round fixed amounts** (1, 2, 4 BTC; 0.2, 1.85, 0.522; 0.44, 0.70, 0.33), not
  percentages. Recurring fee/dust destinations are exact-value: `32WaVK3A…dd6` receives only
  **0.01 / 1 / 2 / 4 BTC** (14 payments, 22.04 BTC total, identical in both anomaly chains);
  `3J7cUjBZx…SSU` receives 26 dust payments of 0.000049–0.000328 BTC (0.00371448 BTC total).
- One transaction, `4ae994ad…5001` (block 975632), co-spends UTXOs of **both** `1BU7pYQ` and
  `1FFQgpEs` plus their pooled 50+50 BTC — direct proof of common control.

Assessment: the hop counts are dominated by **(b) exchange/custodian commingling artifacts**; the
genuine core is **(a) a peel chain** from a single operator; mixing (c) is not supported; legitimate
bulk transfer (d) is inconsistent with the dust inputs and round peels. Details below.

---

## 1. Address graph, multi-chain addresses, recurring small/dust addresses

Graph built from every `node.address` and every `nextOutputs[].a` in all 19 chains.

| Metric | Value |
|---|---:|
| chains / hop nodes | 19 / 153 |
| distinct spending txids in node paths | 105 |
| distinct node addresses | 65 |
| distinct addresses incl. all nextOutputs | 780 |
| total nextOutput entries | 1,222 |
| transactions with >2 outputs (batching) | 70 (46% of 153) |
| transactions whose outputs are all <0.01 BTC | 0 |

**Addresses appearing in multiple chains (top of the list):**

| Address | chains | as node | as largest | as small | small BTC | chains |
|---|---:|---:|---:|---:|---:|---|
| `bc1qpralxa3ke2vk8yflyjq5z3eefzrqz35snxaazcvq5n8tpug8u8lq6jjwss` | 5 | 9 | 9 | 14 | 8.440945 | 1AgiG4fz, 1CA7ysJM, 1D5ngkYu, 1PAT2UPB, 1G47pVbJ |
| `352zzNHvsBTaUUNzqB5xbMo8EYdpvHJQB2` | 5 | 9 | 9 | 3 | 1.765676 | same 5 |
| `361mDjTHQpo6i3rf95KXXirnF7PCuYSCfH` | 3 | 3 | 0 | 0 | 0 | 1AgiG4fz, 1CA7ysJM, 1D5ngkYu |
| `bc1qppunwskzl7t3mtmcarlvpxjuzd8jy7l7ga49av2c78xg47fujncsrc76y6` | 2 | 3 | 3 | 0 | 0 | 1GdN9v11, 1CCoK3bU |
| `bc1qefcww8zpq9xsqmzh8qxq8cseq7uat8mlskk739` | 2 | 12 | 12 | 2 | 473.465 | 1CA7ysJM, 1D5ngkYu |
| `bc1qzl6fpgtylqrv6r8x2hgsa77lpn0whqchgs64f7` | 2 | 14 | 14 | 0 | 0 | 1AgiG4fz, 1G47pVbJ |
| `bc1q9exg34dkmgxu28yektgn929jpjpzukhhl2r3zr` | 2 | 8 | 8 | 2 | 161.200 | 1AgiG4fz, 1G47pVbJ |
| `3J7cUjBZxvGRCwFBz3q23zAsnhFfZrDSSU` | 4 | 0 | 0 | 26 | 0.003714 | 1AgiG4fz, 1CA7ysJM, 1D5ngkYu, 1G47pVbJ |

The remaining ~140 multi-chain addresses appear exactly twice and are overwhelmingly *batch
recipients* in the same two shared corridors (identical txids), not independent relay reuse.

**Recurring fee-like / dust-like destinations (ranked by number of small outputs):**

- `3J7cUjBZxvGRCwFBz3q23zAsnhFfZrDSSU` — **26** small outputs, values
  0.00004923 / 0.00007389 / 0.00009855 / 0.00010827 / 0.00014103 / 0.00014274 / 0.00020988 /
  0.00028215 / 0.00032805 BTC, total **0.00371448 BTC**. Appears in the 3-output exchange-batch txs
  (`983edf9e…`, `e341b118…`, `b28ce70e…`, `44fbf326…`, etc.); a persistent dust marker/fee.
- `32WaVK3Aghcf1MDc2Dkiq7EHQduajiMdd6` — **14** small outputs, total **22.04 BTC**, values only
  **0.01, 1, 2, 4 BTC** (exact). Identical sequence in both `1BU7pYQ` and `1FFQgpEs` chains.
- `bc1qpralxa3…jjwss` — dual role: 9 times the continued output, 14 times a small output. The small
  outputs split into **0.00000331 BTC dust markers** (e.g. txids `606f40c4…`, `512159b1…`,
  `eedc8819…`, `d46c60c6…` — always nouts=2/4) and round amounts 0.827, 2.104615, 2.34115 BTC in
  24–52-output batch txs.
- `3MqUP6G1daVS5YTD8fz3QgwjZortWwxXFd` and `1LYhaUGjRARNF2nHit3vy9uLKbvxMffzpW` show up as
  "small" outputs of 18–1,176 BTC and 199–599 BTC respectively — these are not fees; they are the
  losing side of the tracer's largest-output choice inside commingled wallets (§6).

---

## 2. Source transaction sequences and relay reuse

`address_txs_cache.json` holds per-source WalletExplorer history ([amount] in BTC). In-window
sequences (12–19 Nov) per source have only 1–5 txs; notable multi-step ones:

- `1GdN9v11…` (5 in-window txs): `06f950d5…` 25 out / 20 in (bh 975756) → `0cc0c62b…` 20 out /
  14 in (975807) → dust-in only (975837) → `f9070e1a…` 14 out / 7 in (976131) → `011d4bce…`
  7 out / 3 in (976478). The pattern "peel to a round retained amount + recurring dust input" repeats.
- `1AgiG4fz…` (5): `3b0a8bfc…` 10→8 (975787), `09343870…` 8→4 (975789), `4971d6c7…` 4→3.9
  (975829), `8a5c204f…` 3.9→0.9 (975830), `68d4411a…` 0.9→dust (975831).
- `1CCoK3bU…` (3): 5→3.5 (`3fb0b415…`), 3.5→1.5 (`bd6d6a76…`), 1.5→0 (`7dda3898…`).
- `1D5ngkYu…` (4): 1→0.9, 0.9→0.6, 0.6→dust, dust→0.
- `1PAT2UPB…` (2): 1→0.9 (`35ca33de…`), 0.9→0.9 (`aebff959…`).
- `1BU7pYQ` / `1FFQgpEs` (2 each): the 50 BTC spend and the dust sweep — see §7.

**Yes — the same small subset of relay addresses is reused across sources.** At hop level:

- `bc1qpralxa3…` and `352zzNHvs…` alternate as the retained hop in **five** chains
  (`1AgiG4fz`, `1CA7ysJM`, `1D5ngkYu`, `1PAT2UPB`, `1G47pVbJ`). E.g. `1AgiG4fz` hops 1–6 are
  3.8 → 3.799994 → 1.949 → 1.948997 → 1.426987 → 1.426975 BTC oscillating between these two
  addresses, and `1CA7ysJM`/`1D5ngkYu` hop 1–2 are the same pair at 0.834/0.561 BTC.
- `361mDjTHQ…` is hop 0 for three chains (`1AgiG4fz` 4 BTC, `1CA7ysJM` 1 BTC, `1D5ngkYu` 0.6 BTC).
- `bc1qppunwsk…rc76y6` is a shared hub for `1GdN9v11` (119.57 BTC at hop 1, 99.35 at hop 5) and
  `1CCoK3bU`.
- `33UB1zVip…RUG5V` — labelled **Kraken.com** in `chain-attribution.json` (wallet
  `00001012b1848923`) — appears at hop 10 of both `1CA7ysJM` and `1D5ngkYu` with 823.001 BTC.

These relays are controlled by one operator/pool, or are a shared custodian's deposit addresses.

---

## 3. Amount patterns — round numbers, not percentages

**Genuine peel phase (before any commingling).** Retained amounts are deliberately round, and the
peeled amounts are fixed round values; the implied % varies widely, so they are not percentage fees:

| Chain | retained sequence (BTC) | peel amounts (BTC) | peel % |
|---|---|---|---|
| `1BU7pYQ`/`1FFQgpEs` | 58.99 → 57.98 → 55.98 → 51.97 → 49.97 → 47.97 | 1, 2, 4, 2, 2 (plus two 0.01) | 1.7%, 3.4%, 7.1%, 3.8%, 4.0% |
| `1AgiG4fz` | 4.00 → 3.80 → 1.949 → 1.426987 | 0.2, 1.85, 0.522 | 5%, 48.7%, 26.8% |
| `1G47pVbJ` | 2.402463 → 1.958842 → 1.254437 → 0.922985 | 0.4436, 0.7044, 0.3315 | 18%, 36%, 26% |
| `1PAT2UPB` | 4.946 → 2.499 → 1.791 → 1.139 → 0.819 → 0.409 | 2.4475, 0.7078, 0.6518, 0.3202, 0.4095 | ~49% each |
| `1CA7ysJM` | 0.99999 → 0.834355 | 0.165635 | 16.6% |
| `1D5ngkYu` | 0.60007 → 0.56107 | 0.039 | 6.5% |

The 0.01 BTC peels in the anomaly chain are exactly the fixed payment to `32WaVK3A…dd6` (§1).

**Commingled phase.** Values jump and then swing non-monotonically with round endpoint values:
`1AgiG4fz` 1.43 → **401.00** (×281) → 356.20 → 244.20 → 196.40 → 172.60 → **92.00** →
**432.00** → 300.82 → **485.00** → 467.42 → **1400.69** → 1210.01 → **2461.81** → 1285.38 →
1035.32 → 648.67 → **815.00** → **500.00** → 313.67. `1CA7ysJM`/`1D5ngkYu` jump 0.83/0.56 →
105.74/126.58 → 484.88 → 1197.44, then end at **550.9449** BTC. These are wallet balances, not the
original coins.

**Small-output values.** 152 of 1,088 small outputs are exact integer BTC; the single most common
value is **10 BTC (38 occurrences)**, plus 2 (8), 3 (6), 1 (4), 4 (2). Fixed round withdrawals from
exchange batches (e.g. tx `77e5e21b…4a` has 52 outputs) are the dominant source of these.

---

## 4. Timing, batching, co-spending

**Timing.** Consecutive-hop medians: `1AgiG4fz` 0.48 h, `1G47pVbJ` 0.48 h, `1PAT2UPB` 0.33 h,
`1CA7ysJM` 2.21 h, `1D5ngkYu` 2.14 h, `1BU7pYQ`/`1FFQgpEs` 13.39 h (max 69.9 h). Some hops are
effectively instantaneous: **15 consecutive hop pairs have identical second-resolution timestamps**
(Electrum block time is constant per block, so these are same-block/same-batch moves), e.g.:

- `1AgiG4fz` hops 2, 4, 6, 12: `606f40c4…`→`43cfc7d3…` at 2026-11-14T12:04:55Z,
  `512159b1…`→`046a42ea…` at 13:32:26Z, `c479d3b4…`→`6d3c2c29…` at 16:51:45Z,
  `3fc04423…`→`a27f1ea7…` at 2026-11-15T01:59:34Z.
- `1CA7ysJM`/`1D5ngkYu` hop 2: `eedc8819…`→`3e5763f4…` (19:30:44Z) and `d46c60c6…`→`684c31dc…`
  (11:24:48Z); shared hop 20 `d8e2fea2…`→`099ecff4…` at 2026-11-17T18:01:56Z.
- `1G47pVbJ` hop 10 shares the exact pair/timestamp `3fc04423…`→`a27f1ea7…` with `1AgiG4fz` hop 12.

A same-second pair is two txs in one block: the tracer's "largest output" jumps from one output to
another output of the *same* custodial batch, manufacturing an extra hop.

**Batching.** 70/153 hop txs have >2 outputs; out-degree reaches 52 (`1AgiG4fz` hop 26,
`77e5e21b…4a`) and 50/51 (`1AgiG4fz` hop 4 `046a42ea…`; `1PAT2UPB` hop 2 `f818e175…`). This is
exchange-style batch withdrawal behaviour, not one-to-one peer transfers.

**Co-spending / common ownership.** The clearest case is tx `4ae994ad836e2c5f985201e1e8322543cb7ca2b80fb37ac1289b024af2015001`
(block 975632, 2026-11-13T04:48:31Z). Its **9 inputs** include:

- four dust UTXOs of `1BU7pYQ…` (0.00000546/47/48) and four of `1FFQgpEs…` (0.00000546/47/48);
- `bc1quzuptf8j3ywlp88xpj9m0kd9zlk5q040ngx4kv` — 19.98998044 BTC;
- `bc1qem75307spjv7m8x4t7qxc855yn6nxxja4lryvz` — 38.99998044 BTC.

These two large inputs are the proceeds of the two 50 BTC spends (§7), pooled and swept with both
sources' dust into a **single output** `bc1qj5hhw38…3d0nvzr6` (58.98991325 BTC) — hop 0 of both
chains. A single transaction spending two "independent" sources is strong evidence of one owner.

No tx in `txdetails.json` (43 txs) / `alltrace_txs.json` (121 txs) spends inputs from two *different*
source clusters other than the above; coverage is partial, but chain convergence is decisive:

- `1AgiG4fz` & `1G47pVbJ` share 21 txids (from `b9ffda51…` onward).
- `1CA7ysJM` & `1D5ngkYu` share 19 txids (from `e341b118…` onward).
- `1BU7pYQ` & `1FFQgpEs` share all 8.
- `1GdN9v11` & `1CCoK3bU` share the `bc1qppunwsk…` hub with jump-up amounts.

---

## 5. WalletExplorer cluster groups (`docs/chain-attribution.json`)

All chain addresses are "no data" in WalletExplorer but carry cluster IDs from co-spend heuristics.
Clusters with >1 chain address:

| wallet_id | label | addresses (role in chains) |
|---|---|---|
| `000000030ae8727e` | unnamed | `361mDjTHQ…` (node×3), `352zzNHvs…` (node×9, largest×9), `1LYhaUGj…` (node×2, small×10), `33SxuZH3…`, `3H4o9sFV…` — **the recurring relay family** |
| `36c7ee5fc7bf5a50` | unnamed | `1BU7pYQ…`, `1FFQgpEs…` — **both anomaly sources** |
| `070578d534ab2cd0` | unnamed | `15DomY3ar…` (the 401 BTC jump node) and `bc1q9exg34…` (big wallet, node×8) |
| `00149af03337c7b7` | unnamed | `3NF4Vfg…`, `3MqUP6G1…` |
| `003a7c27faed1e1f` | unnamed | `bc1qx9n80…`, `bc1qjjl6p…` (node×6), `bc1q9a9vw…` (node×6), `bc1qmxz7z…` — **second relay family** in the CA7/D5 corridor |
| `00029416c6e8d7af` | unnamed | `bc1qr6rxd…`, `34QsfLw2T…`, `39bUFFWP…` (all in `1GdN9v11`) |
| `00000240db175755` | unnamed | `3PDJKAUT…`, `3JZ3byZy…` |
| `3e33d62f7037ccec` | unnamed | `bc1qhgnyal…`, `bc1qhz82yr…` (CA7/D5 corridor) |
| `00001012b1848923` | **Kraken.com** | `33UB1zVip…` (823 BTC node in CA7/D5) |

The two relay families coincide exactly with the two commingled corridors; the anomaly pair is one
cluster.

---

## 6. Big wallets and hop inflation

Node repetitions (how often an address is the traced hop):

`bc1qzl6fpg…` **14**, `bc1qefcww8…` **12**, `bc1qpralxa3…` **9**, `352zzNHvs…` **9**,
`bc1q9exg34…` **8**, `bc1qjjl6p…` **6**, `bc1q9a9vw…` **6**, `bc1q0frx45s…` 4.

Per high-hop chain, the share of nodes that are exchange/relay wallets *after* the first >3× jump:

| Chain | nominal hops | first >3× jump at hop | hops after (commingled) | genuine hops |
|---|---:|---:|---:|---:|
| `1AgiG4fz` | 27 | 7 (1.43→401.00) | 20 | 7 |
| `1G47pVbJ` | 25 | 5 (0.92→401.00) | 20 | 5 |
| `1CA7ysJM` | 22 | 3 (0.83→105.74) | 19 | 3 |
| `1D5ngkYu` | 22 | 3 (0.56→126.58) | 19 | 3 |
| `1PAT2UPB` | 8 | 1 (0.10→4.95) | 7 | 1 |
| `1GdN9v11` | 6 | 1 (7.00→119.57) | 5 | 1 |
| `1BU7pYQ` / `1FFQgpEs` | 7 each | none | 0 | 7 each (same path) |

Totals: **91/134 hops (68%)** are post-jump commingled hops; only **43** are genuine peels, and
7 of those are duplicated between `1BU7pYQ` and `1FFQgpEs`. The 4 largest chains (96 nominal hops,
72% of all hops) collapse to two shared exchange corridors of ~20 hops each. The 22–27 counts are
therefore mostly an artifact of following the largest output *through* a commingled wallet: the
wallet re-emerges with a new balance (up or down), which the tracer records as another "hop".
`SESSION_2026-11-19.md` §6 already flags this: "sharp increases in the BTC column … come from that,
not from the original coins."

Quantitatively: amounts jump ×281, ×435, ×126.7, ×225.6 when entering these wallets; a genuine
peel never increases. `bc1qzl6fpg…` alone appears 7 consecutive times in `1AgiG4fz` with values
46.74 → 140.07 → 121.00 → 246.18 → 128.54 → 103.53 → 64.87 BTC — impossible for a single UTXO
chain; it is one address's changing aggregate balance.

---

## 7. The `1FFQgpEs` / `1BU7pYQ` anomaly and the real first spend

Both addresses are P2PKH, funded with **exactly 50 BTC in 2010** (`1FFQ` in block 78671 / tx
`04716b87…`; `1BU7` in block 80723 / tx `1200f922…`) and received only tiny 0.00000546–0.00000548
BTC "dust" deposits in 2020, 2021, 2025 and May 2026. Both have WalletExplorer cluster
`36c7ee5fc7bf5a50`. The DB's 50 BTC figure is in fact correct; the "dust" is additive tracking
dust, not their balance.

- The chain tracer anchors hop 0 at tx `4ae994ad…5001` (block 975632), where each source appears
  **only as dust inputs** (0.00001641 / 0.00002188 BTC swept). That is why they look like "dust-only
  contributors".
- **Real first meaningful spends:** `8e7624765602de63f41867b08e402230d23ac9b2dda614b58b0dd9faffd188c7`
  for `1BU7pYQ` (2026-11-13T04:08:09Z, block **975628**, 50.00000000 BTC) and
  `86b1ad86ea540d5da424bbb351dce0a38406ec5b5b7f11f34058265fed1af3b2` for `1FFQgpEs`
  (2026-11-13T04:10:31Z, block **975629**, 50.00000000 BTC) — found in `address_txs_cache.json`.
- Those two 50 BTC outputs reappear as the 19.98998044 + 38.99998044 BTC inputs to `4ae994ad…`
  (block 975632), which is exactly hop 0 of both chains. The two spends are 2 minutes / 4 blocks
  apart, and both dust UTXOs are swept in the same 9-input tx.
- **Is the real spend part of the same pattern? Yes.** From `4ae994ad…` onward both chains follow one
  clean 7-hop peel: 58.99 → 57.98 → 55.98 → 51.97 → 49.97 → 47.97 BTC, with the fixed round
  side-payments to `32WaVK3A…dd6` of 0.01, 1, 2, 4, 2, 2 BTC (14 identical payments across the two
  chains). No jump, no batch outputs: nouts=2 at every hop. This is the clearest, least-ambiguous
  peel-chain evidence in the dataset; the only problem is the tracer's starting point.

---

## 8. Evidence-based ranking of hypotheses

**1. (b) Exchange/custodian commingling producing tracing artifacts — strongest, explains the hop
counts.** 91/134 hops (68%) occur after a >3× value jump; 29 of 134 are literally the same txs
counted twice; three pairs of chains share 21/19/8 txids; 70 hop txs have >2 outputs (up to 52) with
38 exact-10-BTC withdrawals; a Kraken-labelled cluster (`33UB1zVip…`) sits on the shared path;
identical-second timestamps show multiple hops in one batch. Removing commingled segments reduces
the "27-hop" chain to ~7 real hops and the four big chains to ~18 real hops total.

**2. (a) Peel-chain / single-operator laundering — strong for the genuine core.** Peels are round
fixed amounts (1/2/4 BTC; 0.2/1.85/0.522; 0.44/0.70/0.33) with 0.01/1/2/4 BTC side-payments to a
fixed address and 0.00000331/0.00005–0.00033 BTC dust markers; the same two relays
(`bc1qpralxa3…`/`352zzNHvs…`, cluster `000000030ae8727e`) carry 5 chains; the anomaly pair is
co-spent in one tx. The 19 sources are all 2010–2012 P2PKH addresses dormant for 14–16 years
(`dormancy.json`) that wake within a 6-day window — consistent with one actor sweeping a wallet set.

**3. (c) Mixing service — weak.** The many-output batches and dust outputs superficially resemble a
mixer, but the retained-value path is deterministic "largest output", the batch side-payments are
round exchange-style amounts (10 BTC ×38, 20 BTC, etc.), and a named exchange cluster appears on
the path. No equal-value pool, no fixed mixing fee, and no CoinJoin-like equal outputs were found.
The dust deposits to the two 2010 addresses look like ownership markers, not mixing.

**4. (d) Legitimate bulk transfer — weakest.** Does not explain dust-only UTXOs swept together with
50 BTC, repeated relay addresses across nominally independent sources, non-monotonic 200–2,500 BTC
swings, or fully shared txid segments between different "sources".

**Bottom line:** the 7–27 hops are not 7–27 distinct custodians in a laundering chain. The real
economic activity is a short peel (typically 1–7 hops) by one operator, inflated to 22–27 by
re-entering a few exchange/custodial wallets whose largest output is followed mechanically. The
`1BU7pYQ`/`1FFQgpEs` "7 hops" are one 7-hop peel from two addresses controlled by the same wallet.
