# Zombie Bitcoins

### A forensic audit of 92,083 addresses presumed lost — 2.26 million BTC still sitting there, and 245,000 BTC that proved otherwise

**12 November 2026** · Bitcoin Key Hunter research · full data in [`address-audit.csv`](./address-audit.csv)

*Prefer a styled version? Open [`zombie-bitcoins.html`](./zombie-bitcoins.html) in a browser.*

---

## The premise

Every Bitcoin address is a promise that someone, somewhere, holds a key. Most of those promises are never tested. A wallet can sit untouched for a decade and still be perfectly alive, like a house with the lights off.

The opposite is harder to see. An address can be **dead and still look alive**: keys gone, owner gone, coins unreachable — yet the address still sits in the chain with a balance, indistinguishable from a working wallet. Call these **zombie addresses**: wallets that stopped living years ago and never stopped standing. If their coins are truly lost, no transaction will ever move them again.

### Where the list came from

This population was not assembled at random. The **Bitcoin Key Hunter** app ships with a list built in **2018–19** by a single, brutal filter: every legacy `1…` address that had **not moved a single satoshi out for more than seven years**.

Seven years of silence was already an extreme test in 2018. An address that passed it had not spent since roughly 2011 or earlier — before Mt. Gox was famous, before the first halving had happened, when Bitcoin was still a mailing-list curiosity. The working assumption behind such a list is straightforward: if nobody has touched a wallet in that long, **nobody is left to touch it**. The keys are gone. The owner is gone. The coins are lost, and the address is a headstone with a balance on it.

That premise is what this audit tests. The list holds **92,083 addresses** and the balances they carried when it was compiled. Together they account for **2,530,552 BTC** — more than one bitcoin in eight that has ever been mined.

By November 2026, the survivors had been silent for **at least fifteen years**, and the oldest of them for nearly the entire life of the asset. If the "lost coins" theory is right, those balances should sit exactly where they were in 2018, forever.

We checked every single one of them against the live chain. We asked two questions:

1. **Is the money still there?**
2. **If it isn't, when did it leave?**

All 92,083 balances were resolved. Every one of the 7,816 fully emptied addresses was matched to the transaction that emptied it. And then we found something the balance column alone hides: **502 more addresses that are functionally empty but technically "funded"** — the ghosts.

The premise was mostly right, and in one important way completely wrong. The audit can now put a number on each half: **2,256,802.64 BTC is still lost**, and **245,284.56 BTC is not**. Everything that follows is about the line between them.

---

## The ledger

The list held **2,530,552.46 BTC** when it was compiled. Two numbers carry the whole story.

**2,256,802.64 BTC is still lost.**
It sits in **82,086 zombie addresses** that have not spent a single satoshi since the list was built — **89.2%** of the snapshot, silent for at least fifteen years and, for the oldest, nearly the entire life of Bitcoin. If those keys still existed, someone would have used them by now. Nobody has.

**245,284.56 BTC is no longer lost.**
It left, from **9,990 addresses** that turned out to have working keys — **9.7%** of the snapshot, and 10.8% of the addresses. Every one of them is proof that "lost" was never a permanent condition. Somebody, somewhere, could still sign.

The remaining **28,465.27 BTC** — 1.1% — is neither. It still sits in addresses that spent part of their balance, proving they were alive, and then stopped.

| | Addresses | BTC at snapshot | BTC today |
|---|---:|---:|---:|
| **Zombie addresses — never spent** | **82,086** | **2,256,759.10** | **2,256,802.64** |
| **Proved alive — spent something** | **9,990** | **273,793.36** | **28,465.27** |
| — fully emptied | 7,816 | 101,532.01 | 0 |
| — drained to dust (ghost wallets) | 502 | 131,748.86 | 0.0269 |
| — partial exits | 1,672 | 40,512.49 | 28,465.24 |
| Never funded (zero at snapshot) | 7 | 0 | 0 |
| **Total** | **92,083** | **2,530,552.46** | **2,285,267.90** |

<sub>Zombie addresses hold 43.54 BTC more than at the snapshot: incoming change and dust, not accumulation. Gross outflow from the living cohort is 245,328.09 BTC; 43.54 BTC of that returned as change, which is why net outflow is 245,284.56 BTC. Figures are rounded to 0.01 BTC, so rows may not sum exactly.</sub>

That is the first surprise: **89.2% of a list of presumed-lost coins is still exactly where it was**, through two brutal bear markets, a pandemic, a wave of exchange collapses and the largest bull market in the asset's history.

The second surprise is what the 10.8% that proved itself alive actually did.

---

## Two ways to leave

A balance of zero is unambiguous. 7,816 addresses were drained completely, and we have the txid and timestamp for every one of them.

But zero is not the only way out. Consider this address:

```
1f1miYFQWTzdLiCBxtHHnNiW7WAWPUccr
```

At the snapshot it held **10,009.26 BTC**. Today it holds **17,835 satoshis** — about twenty dollars.

It is classified `still_funded`. And it is, technically. A naive audit would file it under "untouched" and move on. But economically it is a **ghost**: **99.999998%** of its value left on **4 July 2025**, and what remains is dust.

We defined these as **ghost wallets**: addresses that held at least 1 BTC at the snapshot, still report a positive balance, but now hold less than 0.001 BTC. They are the most misleading rows in any balance-based study, and they turn out to be where most of the money went.

A ghost is not a zombie. A zombie never moved. A ghost moved everything and left a body behind — a balance that still reads as funded.

| Cohort | Addresses | BTC out (gross) | Share of all outflow |
|---|---:|---:|---:|
| Fully emptied | 7,816 | 101,532.01 | 41.4% |
| Drained to dust (ghosts) | 502 | 131,748.83 | 53.7% |
| Genuinely partial exits | 1,672 | 12,047.25 | 4.9% |
| **Total** | **9,990** | **245,328.09** | **100%** |

<sub>Gross outflow. Roughly 43.5 BTC flowed back in as change to addresses that grew, which is why net outflow is 245,284.56 BTC.</sub>

**The ghosts moved more bitcoin than every fully emptied address combined.** 502 addresses — half a percent of the dataset — account for 131,749 BTC, or 5.2% of the entire snapshot on their own. Their combined *current* balance is **0.0269 BTC** — under $3,000 at a $110,000 bitcoin — spread across five hundred wallets that each once held a fortune.

> If you sort this dataset by balance, the ghosts look alive. If you sort it by value, they are the emptiest thing in it.

---

## The 95% rule

Of the **82,086 zombie addresses**, not one has spent a satoshi. The entire 245,284.56 BTC that left came from the other 9,990 — and the concentration inside that group is extreme.

Rank every address that lost value and the concentration is brutal: **1,718 addresses — 1.9% of the dataset — account for 95.0% of everything that left.**

The other 8,272 addresses that moved at all contributed 12,266 BTC between them: 5.0% of the total across nine years.

The exits are also top-heavy within themselves. The 168 emptied addresses that held at least 100 BTC each moved **39,677 BTC** between them — 39.1% of all fully-emptied value, from 2.1% of the emptied addresses. The ten largest emptied addresses alone account for 12,794.21 BTC.

Bitcoin's dormant supply is not a smooth distribution of small holders slowly waking up. It is a small number of very large decisions, and a very long tail of nothing happening — a wall of 82,086 addresses that have not moved at all.

### How long the coins slept

An address can be old without its coins being old. What matters is the age of the specific output that moved. For the biggest single coins, each one can be traced back to the transaction that created it:

| Coins that moved | Dormant for | Moved on | Funded |
|---|---|---:|---:|
| **One 3,962.62 BTC pile** — largest coin 1,300 BTC | **14.5 years** | 2025-07-24 | 15 Jan 2011 |
| **Eight 10,000 BTC wallets** — 80,000 BTC | **14.2–14.3 years** | 2025-07-04 | 2 Apr – 4 May 2011 |
| **Thirty-four 50 BTC wallets** — 1,700 BTC | **13.4–13.6 years** | 2024-03-26 | 7 Aug – 5 Nov 2010 |
| **Seven 500 BTC wallets** — 3,500 BTC | **11.3 years** | 2022-11-11 | 10 Jul 2011 |
| **One 1,188.40 BTC address** — largest coin 1,118.40 BTC | **7.9 years** | 2020-09-04 | 16 Oct 2012 |
| **Four wallets** — 6,400 BTC | **7.5–7.9 years** | 2020-02-10 | 8 Mar – 23 Aug 2012 |
| **Three wallets** — 1,889 BTC | **7.7 years** | 2020-01-24 | 27 May 2012 |
| **Two addresses** — 1,850 BTC on the day (largest coins 200 BTC each) | **7.6 years** | 2018-09-13 | 10–11 Feb 2011 |
| **One address** — 4,154.25 BTC, largest coin 1,201.59 BTC | **7.0 years** | 2019-04-02 | 19 Apr 2012 |
| **One address** — 2,000 BTC | **5.4 years** | 2018-04-25 | 23 Nov 2012 |

<sub>"Dormant for" is measured from the transaction that created the largest single output the address spent to the transaction that spent it. Addresses that spent in several transactions can hold coins of different ages.</sub>

Read by age, the pattern is a cliff. Everything that moved in 2024 and 2025 had slept for **13 to 14.5 years**. Everything earlier was **5.4 to 11.3 years** old, and most of it seven to eight. The list's seven-year filter left a visible floor: most of these wallets were just old enough to qualify, then moved. The two tails are instructive. The 34 fifty-bitcoin wallets were 2010 coins that waited 13.5 years — mining rewards, not a modern position. The 2,000 BTC address that moved in April 2018 held a coin created only 5.4 years earlier; the address may be older, but the coin was not.

The July 2025 giants really were among the oldest large coins in the dataset: **14 years and two to three months**, which is exactly what the press meant by "dormant for 14 years". The 3,962 BTC pile that moved three weeks later was older still. The rest of the big exits were younger than the folklore: the 2022 FTX-day cohort was 11.3 years old, and the 2018–2020 movers had mostly been still for **seven to eight years** — old enough to pass the list's seven-year filter, not old enough to be relics.

Dormancy is a property of the coin, not the address. A wallet can be twenty years old and hold a coin minted last month; the reverse is equally possible. Only the output has an age.

---

## When the lost came back

Combining every address that lost value — fully emptied, drained to dust and partially spent — gives the real timeline of economic exits. The figures cover all 9,990 addresses that spent something, not only the ones that emptied.

```
BTC that left, by year   (█ fully emptied   ▓ left a residue)

2018   19,992  ███████▓▓▓▓
2019   32,182  ██████████▓▓▓▓▓▓▓
2020   24,946  ████████▓▓▓▓▓
2021   23,835  █████████▓▓▓▓
2022   11,490  ███▓▓▓
2023   12,322  ███▓▓▓
2024   15,409  ██████▓▓
2025   95,845  ████▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
2026*   9,307  ███▓▓
```

<sub>*2026 is year-to-date through 11 November.</sub>

| Year | Addresses with outflow | BTC out | Fully emptied | Left a residue | Cumulative |
|---:|---:|---:|---:|---:|---:|
| 2018 | 745 | 19,991.65 | 13,148.09 | 6,843.56 | 19,991.65 |
| 2019 | 862 | 32,181.87 | 19,223.69 | 12,958.18 | 52,173.52 |
| 2020 | 775 | 24,946.16 | 15,103.76 | 9,842.40 | 77,119.68 |
| 2021 | 1,586 | 23,835.03 | 17,056.52 | 6,778.52 | 100,954.71 |
| 2022 | 585 | 11,489.62 | 4,886.29 | 6,603.34 | 112,444.33 |
| 2023 | 732 | 12,322.28 | 6,236.44 | 6,085.84 | 124,766.61 |
| 2024 | 888 | 15,408.78 | 11,761.68 | 3,647.10 | 140,175.39 |
| 2025 | 1,375 | 95,845.20 | 8,154.12 | 87,691.07 | 236,020.59 |
| 2026* | 1,695 | 9,307.14 | 5,961.36 | 3,345.78 | 245,327.73 |

<sub>An address can appear in more than one year. "Left a residue" combines ghosts and partially spent addresses. Gross outflow; 43.54 BTC returned to zombie addresses as change, which is why the net figure in the ledger is 245,284.56 BTC. 0.37 BTC of dust-level outflow is not dated.</sub>

Read the shape of it. For seven years the outflow is a slow, almost geological trickle: eleven to thirty-two thousand bitcoin a year, most of it in small pieces, most of it invisible. Then 2025 happens.

**2025 alone accounts for 95,845 BTC — 39% of all outflow in the entire audit — and 87,240 BTC of it came from just 60 of the 502 ghosts.**

And within 2025, one day stands out so far that it distorts the chart.

---

## 4 July 2025: eighty thousand bitcoin in eleven hours

On 4 July 2025, between 03:39 and 14:54 UTC, **eight separate addresses were drained in sequence**. Each had held roughly **10,000 BTC** at the snapshot — seven of them exactly that, the last 10,009.26. Each was left with a residue of between 14,000 and 43,000 satoshis. Together they moved **80,009 BTC**.

| Time (UTC) | Address | BTC out | Left behind |
|---|---|---:|---:|
| 03:39 | `12tLs9c9RsALt4ockxa1hB4iTCTSmxj2me` | 10,000.01 | 15,105 sats |
| 04:06 | `1KbrSKrT3GeEruTuuYYUSQ35JwKbrAWJYm` | 10,000.01 | 43,162 sats |
| 12:37 | `1P1iThxBH542Gmk1kZNXyji4E4iwpvSbrt` | 10,000.01 | 16,979 sats |
| 13:07 | `1CPaziTqeEixPoSFtJxu74uDGbpEAotZom` | 10,000.01 | 29,368 sats |
| 13:31 | `14YK4mzJGo5NKkNnmVJeuEAQftLt795Gec` | 10,000.01 | 16,289 sats |
| 14:01 | `1ucXXZQSEf4zny2HRwAQKtVpkLPTUKRtt` | 10,000.01 | 14,105 sats |
| 14:30 | `1BAFWQhH9pNkz3mZDQ1tWrtKkSHVCkc3fV` | 10,000.01 | 19,743 sats |
| 14:54 | `1f1miYFQWTzdLiCBxtHHnNiW7WAWPUccr` | 10,009.26 | 17,835 sats |

Those eight addresses were not random. They are **eight of the twelve largest addresses in the entire snapshot**. Only four addresses in the dataset ever held more: 79,957 BTC, 31,000 BTC, 28,151 BTC and 10,771 BTC — and all four are still sitting there, unmoved.

That selectivity matters. A protocol change is not selective: it applies to every address at once, and it is a public, contentious process. No Bitcoin consensus change has ever spent coins on someone else's behalf — the 2016 proposal to fork the chain and recover the 119,756 BTC stolen from Bitfinex was abandoned under exactly that objection.

A third of all the value that left this dataset in nine years left on a single day, from a coordinated set of round-numbered 10,000 BTC positions, in a sequence spread across eleven hours of a Friday.

We cannot say from addresses alone where those coins went, or why. What we can say is what the shape suggests: this was not a market sale. Nobody sells 80,000 BTC by draining eight long-dormant cold wallets to dust one at a time over eleven hours. Round-number positions, sequential timing, dust left behind — that is the signature of a **custody migration or a proof-of-control exercise**, not a liquidation.

Twenty days later, on **24 July 2025**, one more giant was drained: `15MZvKjqeNz4AVz2QrHumQcRJq2JVHjFUz`, **3,962.62 BTC**, again leaving 16,984 satoshis behind. It is one of eighteen ghosts that once held at least 1,000 BTC — and at 3,962 BTC, the second-largest of them all, behind only the 4 July shards.

---

## Other set pieces

### 10 February 2020 — four wallets, four transactions, 6,400 BTC

`1MsgeQMD…` (2,000 BTC), `1MEzdoJi…` (1,500), `1H3JocD1…` (1,500) and `1FhLExX7…` (1,400) did not move together. They left in **four separate transactions** on the afternoon of 10 February 2020, between 14:51 and 16:27 UTC. Each balance was split into two clean round-number outputs:

| Time (UTC) | Wallet | In | Split into |
|---|---:|---:|---|
| 14:51 | `1FhLExX7…` | 1,400 | 900 + 500 |
| 14:54 | `1MEzdoJi…` | 1,500 | 1,000 + 500 |
| 14:54 | `1H3JocD1…` | 1,500 | 1,000 + 500 |
| 16:27 | `1MsgeQMD…` | 2,000 | 1,500 + 500 |

At 08:51 the next morning, one transaction swept the four leftovers: **exactly 1,200 satoshis each** (`0264842ce48933f809327b42736dd166a78d1090aeaf99623e8aaed8d077d800`). The residues are the only thing the four addresses shared on-chain. This is not a consolidation; it is a staged payout — four related 2012 wallets, emptied into clean chunks over 96 minutes, then the dust collected in a single pass. Every coin had been dormant for **7.5 to 7.9 years**.

### 26 March 2024 — thirty-four addresses of exactly 50 BTC

On a single day in March 2024, **34 addresses holding exactly 50 BTC each** were emptied in **one transaction**: 1,700 BTC. Two smaller addresses moved another 1.21 BTC that day, for a day total of **1,701.21 BTC from 36 addresses**. The 50 BTC outputs were created between 7 August and 5 November 2010, so those coins had been dormant for **13.4 to 13.6 years**.

Fifty bitcoin was the block reward until the first halving in 2012, and the list is dominated by single-reward wallets: **32,537 of its addresses held exactly 50 BTC**. Only **723** have a smaller balance today; **629** are empty. Between November 2018 and September 2026 the chain shows **24 separate days** on which batches of eight or more of these wallets moved together — 18 on 24 November 2019, 30 on 1 March 2024, 34 on 26 March 2024, 28 on 22 May 2024, 30 on 10 January 2026. That is not 629 independent decisions. It is a small number of operators working through a large stock of mining-era keys, one batch at a time.

Thirty-four identical outputs in one sweep is a batch operation — the kind of pattern produced by an exchange or custodial service consolidating customer addresses, not by 34 individuals deciding to sell on the same afternoon. The recurrence makes that harder to argue with: this is a standing schedule, not a one-off.

### 24 January 2020 — three wallets, two transactions

`1GW9vm3Q…` (641 BTC), `12th9wa1…` (625) and `1KdhAg4Z…` (623) left on a Friday afternoon, in **two transactions 34 minutes apart**. The 625 BTC slice went first, alone, at 15:48 UTC. The other two went together at 16:22. All three wallets had been funded on **27 May 2012**, so their coins were **7.7 years** old.

Three odd-sized slices — 641, 625, 623 — are not round-number withdrawals. They are what is left of three positions after earlier spending, moved together on one operator's schedule. Two transactions instead of one is just as telling: a single controller can afford to take 34 minutes.

### 11 November 2022 — seven identical 500 BTC wallets

On the day FTX filed for bankruptcy, **seven addresses holding exactly 500 BTC each** were drained to dust — 3,500 BTC in total, plus 75 BTC from two fully-emptied addresses. Every one of the seven coins had been created on **10 July 2011**, so it had been dormant for **11.3 years** before that morning. Seven identical round-number positions moving on that particular day is the kind of coincidence that invites a story. We can't prove one from addresses alone, and we won't: this audit records what moved and when, not who moved it. But it is the clearest example in the dataset of *why* the dates matter as much as the amounts.

### 7 January – 2 April 2019 — the largest single-address exit

`1FdjFtrBwf9Jc9fsGN2GtHmG2vs5ZcEuWH` held **4,154.25 BTC**, the largest balance in the dataset that was eventually emptied. It did not leave in one transaction. It went out in **five transactions across three dates**: 2,001.71 BTC on 7 January 2019, 950.95 BTC on 13 January, and the final 1,201.59 BTC on 2 April. The largest single coin in the address — the 1,201.59 BTC output created on **19 April 2012** — had been dormant for **7.0 years**.

This is the address that teaches the difference between a spend and an exit. The CSV records its last outgoing transaction on **3 February 2020** — a 0.001 BTC dust sweep, a year after the money had gone. Read the last-spend column alone and you would date the exit to the wrong year. Read the five spends in order and you get a staged liquidation over eleven weeks.

---

## What else was happening

Dating a movement tells you *when*. It does not tell you *why*. But the dates are not random, and several of them land on days the rest of the world remembers.

Here are the audit's twelve biggest days, set against the historical record. The last column is the honest part: whether the event actually explains the move.

| Date (UTC) | BTC out | Addresses | What else happened that day, or that week | Does it explain the move? |
|---|---:|---:|---|---|
| 2025-07-04 | 80,010 | 10 | US Independence Day. Trump signs the One Big Beautiful Bill Act. US markets closed. The transfers themselves become the day's crypto news: eight wallets dormant about 14 years. | **No.** Nothing to react to, and bitcoin barely reacted. |
| 2020-02-10 | 6,402 | 6 | Markets near record highs; the WHO names the disease COVID-19 the following day. | **Not directly.** The COVID crash was still three weeks away. |
| 2025-07-24 | 3,963 | 2 | No major event; bitcoin near its 2025 highs. | **No obvious catalyst.** |
| 2022-11-11 | 3,575 | 9 | FTX files for Chapter 11 bankruptcy. | **Yes, plausibly.** The clearest stress-date in the set. |
| 2019-01-07 | 2,145 | 8 | Bitcoin around $4,000, still in the post-2018 trough; no single event. | **No obvious catalyst.** |
| 2018-04-25 | 2,051 | 4 | Bitcoin around $9,300, recovering from the first-quarter crash; no single event. | **No obvious catalyst.** |
| 2025-12-05 | 2,015 | 10 | Bitcoin in a weeks-long slide from its October high; it would end the year near $87,000. | **Mood, not event.** No single trigger. |
| 2019-07-20 | 2,002 | 5 | Backlash to Facebook's Libra: Senate hearings days earlier, Trump's anti-crypto tweets, bitcoin down to about $10,500. | **Context, not catalyst.** A bad week, not a single event. |
| 2018-05-17 | 2,000 | 4 | Bitcoin around $8,000; no single event. | **No — and the pattern says so.** These wallets spent part of their balance and kept a residual position. A sale does not leave the position behind. |
| 2020-01-24 | 1,892 | 5 | Davos week; bitcoin around $8,400; no single event. | **No obvious link.** |
| 2018-09-13 | 1,853 | 5 | The crypto winter deepens: the 5 September crash hit 95 of the top 100 coins; bitcoin around $6,350. | **Yes.** Bear-market capitulation. |
| 2024-03-26 | 1,701 | 36 | Bitcoin around $70,000; spot ETF inflows resume after a five-day outflow streak. | **Yes — institutional plumbing.** |

<sub>Dates are UTC and day-level; an event in another timezone can fall on an adjacent calendar day. Values are net outflow attributed to the snapshot coins, so an address that also received change appears at its true exit size, not its gross spend.</sub>

### Three kinds of coincidence

Read down the table and the dates fall into three groups.

**Market stress.** The crypto-winter capitulation of September 2018, the FTX filing of November 2022, the tech-stock selloff of September 2020 (which took 1,188 BTC out of this dataset on the 4th) — these are days when a holder with a working key had a reason to move. That is the uncomfortable part. **Coins that are genuinely lost do not wake up on the worst day of a bear market.** Some of the supply the list assumed was gone was never gone; it was waiting, and it chose its moments.

**Institutional plumbing.** The 34 × 50 BTC batch of 26 March 2024 is not a panic. It is a custodian or exchange running a batch job, and it happens to land on the day ETF flows turned positive again. The date does not explain the transaction; the transaction explains the date. Both are just the same institution's calendar.

**Nothing at all.** The largest move in the entire audit — 80,009 BTC, a third of all outflow — happened on 4 July 2025: a US public holiday, with markets closed and no crypto news of any kind. The story that day was the *size* of the transfer, not its cause. That absence is itself evidence. You do not move eighty thousand bitcoin because of a headline when no headline exists. You move it because a custodian changed systems, or because someone finally proved they still held keys that everyone had assumed were gone.

### The detail that gives the game away

Press coverage of 4 July 2025 described the eight wallets as **"dormant for 14 years"**, and treated that as astonishing. The chain is more precise: the eight 10,000 BTC coins were created between 2 April and 4 May 2011, so they had been perfectly still for **14 years and two to three months**.

In this dataset, it is not astonishing. It is the entry criterion.

Every address here was already seven-plus years silent when the list was built in 2018–19. An address that moved in 2025 had therefore been quiet for roughly **thirteen to fifteen years** — which is exactly what the headlines were gasping at. The list did not accidentally capture history's most patient holders. It was *designed* to capture them, and then it waited long enough for the rest of the world to notice.

That is also why the dates matter more than the amounts. A 10,000 BTC wallet moving is not news because of the number. It is news because of the silence that preceded it — and because, for the first time in a decade, that silence ended.

---

## The zombies

It would be easy to write this as a story about coins leaving. The larger story is about the coins that stayed — and the 82,086 addresses that have held them, silently, for a decade and a half or more.

- **82,086 addresses have not spent a single satoshi since the snapshot.** They hold **2,256,802.64 BTC** — **89.2%** of everything the list held, untouched for at least fifteen years and, for the oldest, nearly the entire life of Bitcoin. These are the **zombie addresses**.
- **47,262 of them have not changed at all** — same balance, satoshi for satoshi, at **83,623.67 BTC**. The other **34,824** received change or dust and grew by a combined **43.5 BTC**, a rounding error. Growth in this dataset is noise, not accumulation.
- **94 addresses still hold at least 1,000 BTC each**, for a total of **344,864 BTC** — 15.1% of everything in the surviving set.
- **Four addresses still hold more than 10,000 BTC**, and none has moved a meaningful amount:

| Address | BTC at snapshot | BTC today |
|---|---:|---:|
| `1FeexV6bAHb8ybZjqQMjJrcCrHGW9sb6uF` | 79,957.17 | **79,957.27** |
| `12ib7dApVFvg82TXKycWBNpN8kFyiAN1dr` | 31,000.07 | **31,000.08** |
| `12tkqA9xSoowkzoERHMWNKsTey55YEBqkv` | 28,151.05 | **28,151.06** |
| `1F34duy2eeMz5mSrvFepVzy7Y1rBsnAyWC` | 10,770.53 | **10,770.54** |

The deltas of a few hundredths of a bitcoin are incoming dust and small payments, not withdrawals. These four addresses have held 149,879 BTC, unmoved, through everything.

---

## What it means

Three readings, in increasing order of speculation.

**1. The lost pile is still lost.** 2,256,802.64 BTC in 82,086 zombie addresses, not one satoshi out. Nine years, two bear markets, a pandemic, a bull market — and 89.2% of the list has not moved. Whatever the narrative about "old coins about to move" is in any given cycle, this dataset says the default state of an address like this is silence. The distribution is not a coiled spring; it is a graveyard.

**2. The exits are institutional, not individual.** Of the 245,285 BTC that left, 95% came from 1.9% of addresses, and the largest single day was a coordinated sweep of eight 10,000 BTC positions. Batch transactions, round numbers, identical residue, sequential timing — the fingerprints throughout are those of custodians, exchanges and large operators, not retail holders panic-selling. Which means this audit is partly a map of **how professional custody moves**, not of how people spend.

**3. The ghosts are the trap.** Any study that classifies addresses by current balance will systematically misread the last five years. 502 addresses holding 131,749 BTC would be counted as "still dormant" by a balance check. They are not dormant. They are empty, and they have been for a while — and in the case of the July 2025 cohort, the *appearance* of a live 10,000 BTC wallet persisted for years after the coins were gone.

A zombie is a wallet that never moved. A ghost is a wallet that moved and left its body behind.

---

## Caveats

Every number above comes with a caveat, and the honest ones matter more than the clean ones.

- **An address is not a person, a wallet, or an entity.** One operator can control thousands of addresses; one address can be shared by a service. "Addresses that moved" is not "people who sold."
- **A spend proves a key, not an owner.** Every emptying here is a transaction that consumed the address's coins, and the network accepted it under the same signature rules it has always used. No protocol upgrade can move coins out of an address: P2SH (2012), SegWit (2017) and Taproot (2021) changed what *new* outputs can look like, not who controls old ones. So each exit proves someone could sign — not that it was the original owner, and not who they are. A recovered backup, a custodian, an heir, a thief or a guessed weak key would all look identical on-chain.
- **"Zombie" is a status, not proof of death.** We use it for addresses that have not spent since the list was compiled. That is strong evidence the keys are gone, not proof: a living owner can simply choose not to move coins. The 2,256,802.64 BTC figure means "still lost to us", not "provably destroyed".
- **Change outputs distort everything.** When a large address spends, most of the value often returns to a new address in the same transaction. We measure *value that left this address*, not *value that was sold*.
- **The CSV and this article date exits differently, on purpose.** The CSV records each address's *most recent* outgoing transaction. The article dates each address's exit to the day of its **largest net outflow**: spends are walked in order and attributed only up to the decline from snapshot to current balance, so a later dust sweep cannot masquerade as the exit. For a single-spend emptying the two agree. For a staged one they do not. `1Fdj…` last touched the chain on 3 February 2020, when a 0.001 BTC residue was swept, but its 4,154.25 BTC left between 7 January and 2 April 2019. The article's dating is the honest one for economic outflow; the CSV is the literal last-touch record.
- **The oldest outputs are P2PK, and some explorers cannot see them.** Several 2010–2011 coins are pay-to-public-key outputs, which Esplora-based APIs do not map to an address at all. Address histories here were resolved through `blockchain.info/multiaddr`, which does map them. A sweep that silently skipped P2PK inputs would undercount the oldest spending — which is precisely the spending this audit is about.
- **Dust is a definition, not a fact.** We set the ghost threshold at 0.001 BTC (100,000 sats). At 0.0001 BTC the cohort is 441 addresses holding 34,478 BTC; at 0.00001 BTC it is 250 addresses holding 9,561 BTC. The 502 / 131,749 figure is the 0.001 cut, and it is the one that best separates "residue" from "a small but real balance."
- **The seven-year rule is the list's origin, not this audit's measurement.** Every address here had already gone more than seven years without an outgoing spend when it was selected in 2018–19. An address that moved in 2025 had therefore been silent for roughly **13–15 years** at the moment it moved, not seven.
- **A date is not a motive.** Several big moves coincide with market stress or institutional news; several coincide with nothing at all. This audit records when coins moved, not who moved them or why.
- **The list is legacy `1…` addresses only.** No bech32, no P2SH. Modern movement — especially migration to `bc1…` — is therefore undercounted. Some of the 2025 activity may be exactly that: coins moving to a new address format and leaving this dataset's view entirely.
- **2026 is year-to-date** through 11 November 2026.
- **Balances were read in November 2026** and are a snapshot of that moment; the chain keeps moving. The newest exit in the file happened on **11 November 2026 at 14:49 UTC**, roughly eighteen hours before the final CSV was written. The oldest was **15 March 2018**.
- **Six addresses** had no transaction history at all and turned out to hold zero at the snapshot, so they never had funds to lose; all 7,816 genuinely emptied addresses are dated.

---

## Appendix: the fifteen largest exits

| # | BTC out | Main exit (UTC) | Kind | Address |
|---:|---:|---|---|---|
| 1 | 10,009.26 | 2025-07-04 | drained to dust | `1f1miYFQWTzdLiCBxtHHnNiW7WAWPUccr` |
| 2 | 10,000.01 | 2025-07-04 | drained to dust | `14YK4mzJGo5NKkNnmVJeuEAQftLt795Gec` |
| 3 | 10,000.01 | 2025-07-04 | drained to dust | `12tLs9c9RsALt4ockxa1hB4iTCTSmxj2me` |
| 4 | 10,000.01 | 2025-07-04 | drained to dust | `1P1iThxBH542Gmk1kZNXyji4E4iwpvSbrt` |
| 5 | 10,000.01 | 2025-07-04 | drained to dust | `1KbrSKrT3GeEruTuuYYUSQ35JwKbrAWJYm` |
| 6 | 10,000.01 | 2025-07-04 | drained to dust | `1ucXXZQSEf4zny2HRwAQKtVpkLPTUKRtt` |
| 7 | 10,000.01 | 2025-07-04 | drained to dust | `1BAFWQhH9pNkz3mZDQ1tWrtKkSHVCkc3fV` |
| 8 | 10,000.01 | 2025-07-04 | drained to dust | `1CPaziTqeEixPoSFtJxu74uDGbpEAotZom` |
| 9 | 4,154.25 | 2019-01-07 → 2019-04-02 | fully emptied | `1FdjFtrBwf9Jc9fsGN2GtHmG2vs5ZcEuWH` |
| 10 | 3,962.62 | 2025-07-24 | drained to dust | `15MZvKjqeNz4AVz2QrHumQcRJq2JVHjFUz` |
| 11 | 2,000.00 | 2020-02-10 | drained to dust | `1MsgeQMDKCqoPRfKT1tw8gGci6w7fCBtKe` |
| 12 | 2,000.00 | 2018-04-25 | partially spent | `1HZH6zHri1qc68s34MmE5MwG9xstbkFavo` |
| 13 | 1,500.00 | 2020-02-10 | drained to dust | `1MEzdoJiJG4USgJo7Q5N9w9i8VMkaJnMQX` |
| 14 | 1,500.00 | 2020-02-10 | drained to dust | `1H3JocD18dS2iPQvVF1p9NNYhX3Vd21dbL` |
| 15 | 1,400.00 | 2020-02-10 | drained to dust | `1FhLExX7gBeq3v6qiMSe5KXrKVwKxZSkbJ` |

<sub>"Main exit" is the day on which the largest share of the address's net outflow occurred; where an address spent in several stages, the full span is shown. `1HZH…` left 0.00153 BTC behind in 2018, so it is not quite a ghost, but 99.9999% of its value was gone that morning. The 1,188.40 BTC address that used to appear at #15 is now #16.</sub>

## Appendix: busiest days by value

| Date | Addresses | BTC out | What it looks like |
|---|---:|---:|---|
| 2025-07-04 | 10 | 80,010.33 | Eight 10,000 BTC wallets drained over 11 hours |
| 2020-02-10 | 6 | 6,402.00 | Four 2012 wallets, four txs, 6,400 BTC; dust swept next day |
| 2025-07-24 | 2 | 3,962.92 | One 3,962 BTC ghost plus a 0.30 BTC emptying |
| 2022-11-11 | 9 | 3,575.00 | Seven 500 BTC ghosts drained on the day FTX filed |
| 2019-01-07 | 8 | 2,144.68 | First 2,001.71 BTC chunk of the largest exit |
| 2018-04-25 | 4 | 2,051.07 | The 2,000 BTC partial exit, plus 51 BTC from three others |
| 2025-12-05 | 10 | 2,015.45 | 1,000 BTC emptied + 1,000 BTC ghost on the day |
| 2019-07-20 | 5 | 2,001.73 | Two 1,000 BTC ghosts; a third had moved on 17 July |
| 2018-05-17 | 4 | 2,000.00 | 600 + 500 + 500 + 400 spent, balances retained |
| 2020-01-24 | 5 | 1,891.81 | Three 2012 wallets, two txs, 34 minutes apart |

<sub>Day totals are net outflow attributed to the snapshot coins, so they differ from the gross spend in the transaction. The 2024-03-26 batch now ranks thirteenth: 36 addresses, 1,701.21 BTC.</sub>

---

## How this was produced

1. Read all 92,083 addresses and their snapshot balances from the app's bundled `blockchain.sqlite`, and checked the total — 2,530,552.46 BTC — against the sum of the three ledger lines.
2. Resolved the current balance of every address via the `blockchain.info` balance API.
3. Selected every address whose balance had changed: **9,990 addresses**. Pulled each one's full transaction history — **13,058 outgoing spend events** in total — through `blockchain.info/multiaddr`, with `mempool.space` as a second source for the largest cases.
4. Walked each address's spends in chronological order and attributed value only up to its decline from snapshot to current balance. This prevents post-snapshot deposits that were later spent from being counted, and yields **245,327.72 BTC** of gross outflow, which reconciles with the CSV to within 0.07 BTC.
5. Dated each address's **main exit** to the day carrying the largest share of that attributed outflow. The yearly timeline, the twelve biggest days and the fifteen-largest-exits table are built from these attributions, not from the CSV's last-spend column.
6. Measured dormancy per coin: found the largest single output each address spent, traced it to the transaction that created it, and compared timestamps. Address age is never used as a proxy for coin age.
7. Re-checked the 34 × exactly-50 BTC batch individually. All 34 were 50 BTC coinbase rewards created between 7 August and 5 November 2010 — **1,700 BTC** in total — and the batch is one of **24 days** between November 2018 and September 2026 on which eight or more of the 723 spent 50 BTC wallets moved together.
8. Identified ghost wallets as funded addresses with a snapshot of ≥ 1 BTC and a current balance above zero but below 0.001 BTC: exactly **502**, holding 131,748.86 BTC at the snapshot and 0.02691 BTC today.
9. Re-ran the sweep for undated addresses until every emptied address had a date. The six that stayed dateless had no transaction history at all and turned out to hold zero at the snapshot.
10. Wrote the per-address result to `address-audit.csv` and computed the cohort statistics above from it.

**Output:** `docs/address-audit.csv` — 92,083 rows, 7.0 MB, columns `address, snapshot_sats, current_sats, status, emptied_at_utc, emptying_txid`.

**Statuses:** `still_funded` (84,260) · `emptied` (7,816) · `zero_at_snapshot` (7).

<sub>The audit ran against the 92,083-row asset. The asset shipped with the app has since been narrowed to the 82,086 still-dormant addresses, so reading it today yields that subset; `docs/address-audit.csv` remains the full per-address record of all 92,083.</sub>

*The CSV is the literal truth of the chain: an address with 17,835 sats is still funded. This article is the interpretation — that a wallet with 0.00018 BTC where 10,009 BTC used to be is, for every practical purpose, a ghost; and that the 2.26 million bitcoin which has not moved in fifteen years is what we mean by a zombie.*
