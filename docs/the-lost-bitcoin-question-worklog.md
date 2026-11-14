# The lost-bitcoin article — work log

**Last updated:** 14 November 2026
**Deliverable:** [`the-lost-bitcoin-question.html`](./the-lost-bitcoin-question.html)
**Status:** written, checked, ready to publish. No open blockers.

This file exists so a future session (human or agent) can pick the article up
without rebuilding the research. It records what was produced, which files fed
it, every number that was verified, the editorial decisions, and what is left.

---

## 1. What was produced

A standalone HTML article that folds the New York "lost bitcoin" lawsuit into
the existing 92,083-address audit, and uses it as the hook for a pitch for the
**Bitcoin Key Hunter** Android app.

| Property | Value |
| --- | --- |
| Path | `docs/the-lost-bitcoin-question.html` |
| Size | 42,861 bytes |
| Lines | 771 |
| Prose words (body text only) | 2,811 |
| Template source | `docs/article_final_revised.html` (2,625 words) |
| H1 | *Are lost bitcoins anyone's?* |
| Stylesheet | byte-identical to `article_final_revised.html` |
| Head differences | `<title>` and `<meta name="description">` only |
| Anchors | all resolve; no missing `id` targets |
| CSS | 28 classes used, all 39 defined in the page; no undefined classes |

`docs/article_final_revised.html` was **not modified**. It still contains the
old 82,093 figure and is kept as the style reference.

### Section map (same nine-section arc as the original)

1. **The mystery of lost keys** — lock vs. key framing; the case appears in
   paragraph 3; ends with "ownership by paperwork vs. ownership by signature".
   - *Where the list came from* — provenance of the 92,083-address list.
2. **The ledger** — 82,086 never-spent addresses / 2,256,802.64 BTC; full
   six-row breakdown table totalling 92,083.
3. **When the lost came back** — year-by-year outflow bar chart (2018–2026).
4. **How long the coins slept** — dormancy table of the six biggest single
   exits.
5. **The July 2025 transfers** — 4 July (80,009.33 BTC), 24 July (3,962.62 BTC),
   Galaxy's 25 July sale disclosure.
6. **Other set pieces** — three sequences: 26 Mar 2024 (34 × 50 BTC),
   11 Nov 2022 (7 × 500 BTC), 7 Jan – 2 Apr 2019 (`1FdjFtr…`).
7. **The coins that stayed** — 82,086 addresses, dust growth, 94 ≥ 1,000 BTC,
   four largest balances; `1FeexV6…` identified as John Doe #1.
8. **The lawsuit that says silence means surrender** *(new section)* — case
   table, five-step finder theory, on-chain service, five counters, the $10
   question.
9. **The hunt for a lost key** *(replaced the old closer)* — why random keys
   are hopeless, why low-entropy keys are not, what the app does, odds caveat,
   ownership caveat, Play Store link.

Then **Sources** (6 citations) and a CSV download link.

---

## 2. Editorial decisions

### The question
Title and argument are built on **"are the real lost bitcoins anyone's?"** —
the article sets two claims of ownership against each other:

> A bitcoin address is a lock. A private key is the only key. Everything
> else — a court order, a statute, a claim of ownership — is an argument about
> who is allowed to turn it.

The lawsuit supplies *ownership by paperwork*; the chain supplies *ownership by
signature*. The audit sits between them.

### The case as hook, not subject
It opens the piece, gets one dedicated section, and reappears only where it
earns its place (`1FeexV6…` = John Doe #1; the ownership question in the final
callout). The structure stays the original's nine-section arc.

### Hope, but honest
The closer was rebuilt around **low-entropy keys** — flawed RNGs, passphrases,
weak dice, truncated keys — rather than brute force. That is a real, searchable
space and the only honest justification for a phone-based search. Then an
explicit concession:

> We will not pretend the odds are good. They are not.

followed by the pivot: *9,990 addresses on a list everyone assumed was dead
turned out to have a working key behind them.* "Almost impossible" is not
"impossible".

### The pitch
Every app claim was checked against `docs/IMPLEMENTATION_STATUS.md` first.
The article states the app ships the 82,086 never-spent addresses, walks
private keys sequentially, derives compressed **and** uncompressed P2PKH
addresses, checks against a hash160 index, runs in a foreground service with a
notification, checkpoints every ~2 seconds (encrypted), resumes after process
death, and stops to show a found key.

App identity used in the call to action:

- Name: **Bitcoin Key Hunter**
- Package: `com.quickben22.bitcoinlotto`
- Link: <https://play.google.com/store/apps/details?id=com.quickben22.bitcoinlotto>

### Guardrails kept
- The case is described throughout as **allegations, not findings**.
- Explicit "we are not lawyers; this is research, not legal advice".
- Ownership caveat in the final callout: a key gives control, not a title deed;
  the app is a research tool, not a licence to take someone else's coins.
- Footer repeats "Not legal advice".

### What was cut to keep it shorter
The original carried 2,625 words. Despite *adding* the case and the pitch, the
new piece is 2,811 — meaning the audit prose was cut harder:

- the cumulative SVG chart;
- the duplicate full yearly ledger table (the bar chart survives);
- the **"What else was happening"** section and its twelve-row table (its best
  insight survives as one line: the biggest move fell on a US holiday with no
  news to react to);
- the **10 February 2020** set piece (three set pieces remain);
- long caveats folded into inline notes.

---

## 3. Files used

### Primary sources

| File | Role | What was taken |
| --- | --- | --- |
| `docs/article_final_revised.html` | **Template** | Full `<head>`, stylesheet, page shell, nine-section arc, bar-chart data, set-piece structures, four largest balances. Not modified. |
| `docs/address-audit.csv` | **Primary data** | 92,083 rows. Every balance, cohort count and threshold figure in the article was recomputed from it. |
| `docs/zombie-bitcoins.md` | **Original research (407 lines)** | Methodology, seven-year filter, yearly outflow series, set-piece narratives, ghost/partial definitions. |
| `docs/zombie-bitcoins.html` | Long-form HTML edition | Cross-reference for wording and figures. |
| `docs/IMPLEMENTATION_STATUS.md` | **App truth** | The authoritative list of what Bitcoin Key Hunter actually does — every pitch claim traces here. |

### Supporting files

| File | Used for |
| --- | --- |
| `core/data/src/main/assets/blockchain.sqlite` | Confirmed the shipped DB: exactly **82,086** rows, sum **2,256,759.10 BTC**, contains `1Feex…`, excludes ghosts and emptied addresses. |
| `app/src/main/res/values/strings.xml` | App display name: "Bitcoin Key Hunter". |
| `app/build.gradle.kts` | `applicationId = "com.quickben22.bitcoinlotto"` for the Play link. |
| `docs/REWRITE_PLAN.md` | Confirmed the search engine is real (secp256k1, hash160 index), so the pitch could describe actual behaviour. |

### CSV schema

```
address,snapshot_sats,current_sats,status,emptied_at_utc,emptying_txid
```

`status` is one of `still_funded`, `emptied`, `zero_at_snapshot`.

> **Caveat:** only `emptied` rows carry `emptied_at_utc`. Ghosts and partial
> exits sit under `still_funded` with no date. An earlier draft wrongly claimed
> the CSV dates every exit; that claim was removed.

### Research sources (cited in the article)

1. Galaxy Research, "'Salomon Brothers' Client Sues to Claim Legal Ownership of
   Satoshi's Coins," 27 May 2026.
2. Baker McKenzie, "The 'Lost Bitcoin' Litigation," 10 June 2026.
3. Bitcoin.com News, "Bitcoin Policy Institute Fights $293B Wallet Grab…,"
   July 2026.
4. CoinDesk, "Galaxy Confirms Sale of 80,000 Satoshi-Era Bitcoins," 25 July 2025.
5. Alex Thorn / Galaxy Research, on the 44 dropped defendants, 8 July 2026.
6. BitcoinWiki, "1FeexV6… Bitcoin address."
7. Court docket, *ABC Company, XYZ Company, and Noah Doe v. John Does
   1–39,069*, Index No. 153119/2026 (N.Y. Sup. Ct., N.Y. County).
8. Crypto.news, Decrypt/Yahoo, Bitcoin Magazine, Cahill, Brown Rudnick — used
   for corroboration only, not cited.

---

## 4. Verified figures

All recomputed directly from `docs/address-audit.csv`; none inherited unchecked.

### The audit

| Figure | Value |
| --- | --- |
| Rows in the historical list | 92,083 |
| Value at snapshot | 2,530,552.46 BTC |
| Value at the 2026 check | 2,285,267.90 BTC |
| Never spent (no outgoing spend) | 82,086 addresses |
| Value in never-spent group | 2,256,759.10 BTC snapshot → **2,256,802.64 BTC** current |
| Share of the list | 89.2% of addresses and value |
| Dust/change gained by that group | +43.54 BTC |
| Proved able to spend | 9,990 addresses, 273,793.36 BTC snapshot → 28,465.27 BTC |
| — fully emptied | 7,816 addresses, 101,532.01 BTC → 0 |
| — drained to residue ("ghosts") | 502 addresses, 131,748.86 BTC → **0.02691 BTC** at a 0.001 BTC threshold |
| — partial exits | 1,672 addresses, 40,512.49 BTC → 28,465.24 BTC |
| Never funded (zero at snapshot) | 7 addresses (included so the table sums to 92,083) |
| Overall net outflow | 245,284.56 BTC |
| Addresses ≥ 1,000 BTC today | 94, holding 344,863.57 BTC (15.1% of all addresses still holding a balance) |
| — same, restricted to the never-spent group | 91, holding 337,185.36 BTC |
| Addresses holding exactly 50 BTC at snapshot | 32,537; 723 have less today, 629 are empty |

### The case

| Figure | Value |
| --- | --- |
| Filed | 11 March 2026 |
| First Amended Complaint | 1 May 2026 |
| Defendants | 39,069 John Does (the addresses, not persons) |
| Value at filing snapshot | 3,799,629 BTC on 25 May 2026 at $77,245 ≈ **$293.5bn** |
| Patoshi addresses named | 21,923 (≈1,096,134 BTC) |
| John Doe #1 | `1FeexV6…` — Mt. Gox coins, 79,957 BTC |
| John Doe #104 | Counterparty burn address, 2,131 BTC (provably unspendable) |
| Service on chain | 98 batches × 546 sats, blocks 950,446–950,576, 21–22 May 2026 |
| Median defendant | 50 BTC ≈ $3.86m |
| Mean defendant | 97.25 BTC ≈ $7.5m |
| Worth > $10 today | 39,046 of 39,069 |
| Worth > $10 at last-move value | 7,108; the other 28,962 fail only because coins last moved 2009–10 |
| Stay entered | 5 June 2026 |
| Defendants dropped | 44, early July 2026 |
| BPI intervention | 10 July 2026; hearing 14 July 2026 |
| Status | pending; no final ruling reported as of 14 Nov 2026 |

### The July 2025 transfers

- **4 July 2025**, 03:39–14:54 UTC: eight ~10,000 BTC wallets drained,
  **80,009.33 BTC**, residues of 14,000–43,000 sats, funded 2 Apr – 4 May 2011
  (14.2–14.3 years dormant).
- **24 July 2025**: `15MZvKjqeNz4AVz2QrHumQcRJq2JVHjFUz` sent 3,962.62 BTC,
  kept 16,984 sats.
- **25 July 2025**: Galaxy Digital disclosed selling >80,000 BTC (>$9bn) for a
  "Satoshi-era investor" as estate planning. CoinDesk linked it to the
  transfers; Galaxy did not identify the addresses.

### Corrections applied

1. **82,093 → 82,086.** The inherited count included seven addresses that never
   held funds. Fixed in prose, table, hero metrics, and a visible correction
   note. The table now lists the seven so it sums to 92,083.
2. **"Nine years later" → "Eight years later."** The list was built in 2018.
3. **CSV dating claim removed** — see caveat above.
4. **"First serious attempt" → "most ambitious attempt yet"** (other claims to
   abandoned BTC exist).
5. **Galaxy's $10 figures** no longer imply they partition all 39,069.
6. **94 vs. 91** — the ≥1,000 BTC line now says "15.1% of every address in the
   list that still holds a balance" instead of "of the survivors", which read as
   the 82,086 group where the correct number is 91.

---

## 5. How the file was built

The body was written separately, then spliced into the template so the
stylesheet could not drift:

1. Read `docs/article_final_revised.html`, split at `<body>`.
2. Keep everything before `<body>` (the `<head>` and stylesheet) verbatim.
3. Replace only `<title>` and `<meta name="description">`.
4. Concatenate the new body.
5. Write `docs/the-lost-bitcoin-question.html`.

Verification afterwards:

- `<style>` block byte-identical to the template → **true**
- head identical once title/description are stripped → **true**
- balanced tags; no missing anchors; no undefined CSS classes
- word count and file size

### Re-running the data checks

The figures in §4 come from small Node scripts run against the CSV. Any one of
them can be reproduced without dependencies:

```js
const fs = require("fs");
const rows = fs.readFileSync("docs/address-audit.csv", "utf8")
  .trim().split(/\r?\n/).slice(1).map(l => l.split(","));
const btc = n => n / 1e8;
// status is column 3, snapshot 1, current 2
const zombies = rows.filter(r => r[3] === "still_funded" && +r[2] >= +r[1]);
console.log(zombies.length, btc(zombies.reduce((a, r) => a + +r[1], 0)));
// → 82086 2256759.10
```

Useful invariants when editing:

- 7,816 + 502 + 1,672 = 9,990 spenders
- 82,086 + 9,990 + 7 = 92,083 rows
- 2,256,759.10 + 273,793.36 = 2,530,552.46 snapshot
- 2,256,802.64 + 28,465.27 = 2,285,267.90 current

---

## 6. Open items

None blocking. Options if the article is revisited:

1. **Trim ~300 more words** — the remaining fat is in "Other set pieces" and the
   lawsuit's bullet list. Target would be ≤ 2,625 words to match the original.
2. **Replace the old article** — `docs/article_final_revised.html` still carries
   the 82,093 error and has no case coverage. It has been left untouched by
   design; the new file supersedes it.
3. **Responsive check** — the layout was validated structurally, not visually.
   Open locally and check narrow widths, the bar chart, and the two new tables.
4. **Older docs** — `docs/zombie-bitcoins.md` and `.html` are consistent with
   each other but predate the correction; they still describe the audit without
   the seven never-funded addresses being called out.
5. **Publish** — the file is self-contained (inline CSS, no assets, no JS), so it
   can be dropped on any static host as-is. The CSV link is relative
   (`./address-audit.csv`), so `docs/` must be served as a directory.

---

## 7. Git context

- Branch: `master`
- `github` → `https://github.com/quickben22/bitcoinlotto-2.git`
- `origin` → Bitbucket (`Frisk/bitcoinlotto`) — not used for this work
- Publishing this work goes to **`github`**
