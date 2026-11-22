# Handoff — dormant bitcoin investigation

**Last updated:** 19 November 2026
**Status:** investigation complete for the current scope; deliverable built; nothing committed.

This note is the entry point for resuming. It supersedes `SESSION_2026-11-19.md` where the two disagree.

---

## 1. The deliverable

`docs/btc-movement-explainer.html` — a self-contained dark-theme explainer (no external scripts or
fonts). It has eight sections:

1. What happened — and why now (timeline chart + New York lawsuit context)
2. How long they slept (dormancy bar chart)
3. Why did they stay dormant for so long? (then-vs-now value table, five reasons, selection caveat)
4. Why so many hops? (peel-chain definition box + diagram, then four causes)
5. In plain words: what was the owner trying to achieve? (buried-jar analogy, three goals)
6. Who owns them? (WalletExplorer attribution chart)
7. The flow diagrams (gallery of all 19, links to SVG)
8. Caveats

Supporting assets: `docs/btc-flow-<prefix>.svg` and `.png` for all 19 sources (38 files).
Deeper notes: `docs/hop-investigation-local.md`, `docs/chain-attribution.md`,
`docs/refresh-biggest-chain.md`, `docs/SESSION_2026-11-19.md`.

Regenerate the HTML (from `analysis/data`):

```
node ../scripts/make_report_html.js
```

---

## 2. The question, and the current answer

**Question:** why do the traced chains have so many hops?

**Answer — four layers, largest last:**

1. **Address-format migration.** Legacy `1…` sources → native SegWit `bc1q…` destinations. Explains hop 1.
2. **A deliberate peel chain.** Round fixed amounts peeled off (1, 2, 4 BTC), sequential forwarding to
   fresh addresses. `1GdN9v11…` 25→20→14→7→3 BTC; `1AgiG4fz…` 10→8→4→0.9 BTC.
3. **Automated infrastructure.** 125 of 134 spending txs have >1 output. `3J7cUjBZ…` got 26 tiny
   payments; `32WaVK3A…` got 14 payments of exactly 0.01/1/2/4 BTC. 15 consecutive hop pairs share
   identical block timestamps. Unnamed clusters with 66k–144k txs.
4. **A tracing artefact (biggest factor).** "Follow the largest output" keeps bouncing inside
   commingled custodial wallets. 91 of the 134 steps after the first destination (68%) happen after a
   value jump; amounts reach 1,400 and 2,461 BTC. The 28-transfer chain is ~7 real transfers.

**Why the long dormancy:** they are savings addresses, not wallets that fell idle. All 19 were funded
Sep 2010 – Dec 2012; 18 of 19 never sent a tx before Nov 2026. Most were funded from large early
addresses holding hundreds of BTC (e.g. `1F8byfFV…` 751.65 BTC, `1QBU89LH…` 665.40 BTC,
`12yQzuV7…` 578.50 BTC), a few from smaller ones. 17 of 19 were funded by normal txs, 2 are direct
50 BTC coinbase rewards. A 2012 tx (`bc10913df…`) split one 80 BTC output into 24 addresses, two of
which are in the set. At funding, the whole 241 BTC set was worth roughly $1,000–2,000.

**Why they woke:** a New York lawsuit (`Index No. 153119/2026`, "Noah Doe") over 39,069 dormant
addresses (~3.8M BTC). In May 2026 the plaintiffs sent 546 sats with an OP_RETURN court notice to every
defendant address; 339 owners who moved coins were removed from the case. Six of the 19 provably carry
that May-2026 notice. Press (The Block, 13 Nov 2026) names two of the exact transactions and says
owners' identities remain unknown.

**Attribution:** 84 addresses queried against WalletExplorer. Only one public label anywhere:
`Kraken.com` for `33UB1zVipqDkzibpKKYFFLTpdw1uCRUG5V`, mid-chain in two trails. No real-world owner.

---

## 3. Key numbers

| Item | Value |
|---|---|
| Sources that moved | 19 |
| Net outflow | 241.42905423 BTC |
| Remaining at sources | 3.80094587 BTC |
| Dormancy | 13.9 – 16.2 years (median 14.6) |
| Transfers per chain | 1 – 28 (four chains are a single transfer) |
| Distinct hop nodes / txs / addresses | 153 / 105 / 65 |
| Value today (at $61,609/BTC) | ≈ $14.9M |
| Named wallet labels | 1 (Kraken.com) |

---

## 4. Corrections already made — do not regress

1. **`1FFQgpEs…` and `1BU7pYQ…` each held 50 BTC**, as 2010 P2PK coinbase rewards. The old claim that
   they "never held more than dust" was wrong and has been fixed in `SESSION_2026-11-19.md`.
   What *is* true: they contributed only ~0.00002 BTC to the 58.99 BTC consolidation
   (`4ae994ad…`); their own 50 BTC each moved separately (`8e762476…`, `86b1ad86…`, two minutes apart).
2. **The two other "shared funding" txs were mass dusting, not common ownership.** Only
   `bc10913df…` (2012, 80 BTC split into 24) is a genuine shared origin.
3. **The "8.8 years" dormancy was an artefact.** `1ALpEWtX…` received an unsolicited 0.001 BTC payment
   on 2018-02-12; its own coins had not moved since Feb 2011 (15.8 years). The report charts 15.8†.
4. **The range is 1–28 transfers, not 7–27 hops.** The old chart counted `chain.length - 1` and showed
   "0 hops" for four single-transfer chains. It now counts `chain.length` and says "transfers".
5. **Only 6 of 19 provably received the May-2026 court notice**, not all 19. Eleven show no dust at
   all in their WalletExplorer history; two show only a 2014 one-satoshi payment.

---

## 5. Data and scripts

Working directory for all scripts: `analysis/data`.

- `chains.json` — 19 largest-output chains. Node 0 is the **first destination**, not the source;
  `chain.length` = number of transfers.
- `dormancy.json` — generic dormancy metric (`last >=0.001 BTC tx` → first Nov-2026 tx).
- `address_txs_cache.json` — WalletExplorer per-address history (amounts, heights, times).
- `histories.json`, `zombies.json`, `current_balances.json` — full 82,086-address scan.
- `docs/chain-attribution.json`, `docs/chain-enrichment.json` — labels + dormancy merged for graphics.

Scripts used in this phase:

- `compute_dormancy.js` — dormancy values
- `analyze_hops.js` — hop/output aggregation (the 125/134 and 91/134 figures)
- `funding_origins.js`, `shared_funding.js` — early funding checks
- `make_chain_graphic.js` — writes `docs/btc-flow-*.svg`
- `enrich_chain_graphics.js` — rebuilds attribution report + graphics
- `make_report_html.js` — **the HTML explainer**
- `check_split_outputs.js` — examined the 24-way 2012 split

---

## 6. Open items

- **Nothing is committed.** `git status` shows `analysis/` and the new `docs/` files as untracked;
  some data files are large (`histories.json` ≈ 43 MB). Decide what belongs in git before committing.
- **Graphics are still slightly off-by-one.** The SVGs subtitle the 28-node chain as "27 moves" and
  label destinations from `HOP 0`. The HTML now says "28 transfers". Either regenerate the graphics
  with the new convention or accept the difference.
- **Unverified:** whether any final address (e.g. `3P4mPNtq…` with one 550.9 BTC UTXO) has moved or been
  sold since 19 Nov 2026.
- **Unexplained:** the other 13 sources moved in the same week without a visible court notice. One
  operator sweeping many addresses, or publicity-driven, remains speculation.
- **Always unproven:** that the movement is the owner rather than a custodian or a thief. The
  co-spend of both court notices with the pooled 50+50 BTC proves common control of those two wallets,
  not a named person.

---

## 7. Session log — 19 November 2026

Chronological record of what was actually done, so a later session can tell what is settled from
what is merely noted.

1. Re-checked the bundled 82,086-address database against the live chain. 19 addresses had outgoing
   spends in 12–19 Nov 2026; 241.42905423 BTC out, 3.80094587 BTC left.
2. Traced every source with "follow the largest output". Wrote `analysis/data/chains.json`.
3. Rendered 19 flow diagrams (`docs/btc-flow-*.svg` + `.png`) with dormancy and public-attribution
   panels.
4. Question raised: *why do the chains have so many hops?* Two parallel investigations ran:
   - local transaction-graph analysis → `docs/hop-investigation-local.md`
   - internet research → lawsuit context, press coverage, tracker data
5. Found the trigger: the "Noah Doe" New York lawsuit (Index 153119/2026) and its May-2026
   546-satoshi court notices. Confirmed 6 of the 19 carry the notice on-chain.
6. Built `docs/btc-movement-explainer.html`: inline SVG charts, attribution chart, gallery of all 19,
   plain-language text.
7. Error found and fixed: the 58.99 BTC consolidation was funded mainly by *other* inputs;
   `1FFQgpEs…` and `1BU7pYQ…` each held their own 50 BTC and moved it separately. Warning added to
   those diagrams and to `SESSION_2026-11-19.md`.
8. Question raised: *why dormant so long?* Reverse-engineered funding: all 19 funded Sep 2010 –
   Dec 2012, 18 of 19 never spent before 2026, 2 are direct coinbase rewards, one 2012 tx split
   80 BTC into 24 addresses. Added section 3 with a then-vs-now value table.
9. Found the "8.8 years" outlier was an unsolicited 0.001 BTC payment in Feb 2018; real range is
   13.9–16.2 years. Corrected the chart (15.8†) and the summary text.
10. Question raised: *what is a peel chain?* Added a definition box with diagram inside section 4.
11. Question raised: *what was the owner's goal?* Added section 5 (buried-jar analogy, three goals).
12. Discovered the hop chart counted `chain.length - 1`, showing "0 hops" for four single-transfer
    chains and contradicting the "7–27" card. Changed to `chain.length` = **1–28 transfers**.
13. Wrote this handoff; corrected `SESSION_2026-11-19.md`.

### If you resume

1. Read §2 (the answer) and §4 (corrections) before changing any numbers.
2. Open `docs/btc-movement-explainer.html` and confirm it still matches `chains.json`.
3. If you extend the window: change `START`/`END` in `analysis/scripts/chain_follow3.js`, delete
   `analysis/data/chains.json`, re-run the chain follower, then `make_report_html.js`.
4. Decide whether to regenerate the SVGs with the "transfers" convention (see §6).
5. Nothing has been committed yet; git scope is still an open decision.
