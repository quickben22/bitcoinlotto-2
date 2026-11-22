# Largest-output chain for each of the 19 addresses (12–19 November 2026)

For each source address I took its largest outgoing payment and then, at every subsequent address, followed only the **largest output** of the transaction that spent it, until the coins reached an address with no further outgoing movement in the window. Your example reproduces exactly: `1FFQgpEsQwqyf5psi5tayYFj7Yoz6Ru1tC` settles at `bc1qvwz4luueypyhthfz7tg0cq9klkfpq34e923x4t`.

## Summary

| Source | Moves | Final address | Final BTC |
|---|---:|---|---:|
| `1313TARBFzoSbXQUL7yu32p9TjoBhUPx2M` | 0 | `bc1q7upzc44p5v9wt5se40xt4grpwzalfe85zrcsd9` | 75.00000978 |
| `1ALpEWtXApR19hLXvcG8NfkPyGsUMhFPMS` | 0 | `bc1q8s6khyqw7rarqw0wr9s23mrxjdj2qq40y69gpp` | 74.99999601 |
| `1GdmnuPz5h9SsUvtiFWqTpJ7h28i8hTUQG` | 2 | `bc1q3r54h5s3q34k70rqdajdjj5fgykrj6u42wen75` | 5.53129106 |
| `1GdN9v11u8ecZF4USpj6GqKhazTxUvy7aY` | 6 | `bc1q0x9grmn026zpc9hf880amhs048dg5n6c02x7pw6scqs7cpsq8ueskt88f5` | 40.02418139 |
| `1NoWYaupfeKF93AiXkuPM12JjiaMRw5bZF` | 0 | `bc1q366ast325w0s7rjl4vf57jcwg5vp8nhqnam297` | 18.25998895 |
| `1AgiG4fzC2Z6fQ1Ltf6tEz9sUBCY1ay2za` | 27 | `bc1p77rtrsvsrl5nhu44hg7jp5hkz24qx044jgswx7sejpuwqckqcxxq5ejgvr` | 313.66719025 |
| `1BVbQyATQRkN3Y7MX7aspjZsqnrnN2CWbN` | 1 | `bc1qlt5nm3kflne7rht4alsnzdzad878ld5rcu4na0` | 5.00721689 |
| `1CCoK3bUtothK5GN9vos1mWthdjoa4BqYa` | 2 | `1GQdrgqAbkeEPUef1UpiTc4X1mUHMcyuGW` | 10.00000000 |
| `1CA7ysJMPpUav91cHgQ7Aom7suzY9w7YBw` | 22 | `3P4mPNtqyFtRCjZwPDg8hE36xYk76VfEme` | 550.94490000 |
| `1CAPqy7AhPehwvmUPouVbVGJsNnvWjjiWY` | 1 | `bc1q0dkf8x6rnv7mupgq56z9gcr0kpxxyhlxar3yx0` | 0.30813500 |
| `1Car858fC2J1ZsCv5dmvZbHtKP3Nk2RN8R` | 1 | `bc1qcuvxs62p5nllzx9qv9jr0nhj98c3vxv78zzuv2` | 0.89999738 |
| `1CsRw431siXGryywxgvreDU1vbbEiXGC7g` | 2 | `bc1q37d7vqd4ef7z38hypttvtf972hel273tfav6j6xf5tdzcyempa9spp7wkq` | 2.83001507 |
| `1D5ngkYujbuwNw8XDkm5MiLgwRf467o1ne` | 22 | `3P4mPNtqyFtRCjZwPDg8hE36xYk76VfEme` | 550.94490000 |
| `1738b4HYtP312grgHBMXujFwZQnhveYWuj` | 0 | `bc1qfr0gspw03yw4pudrr9s7609wyf9t3yjyu4gdh2` | 0.76876246 |
| `1PAT2UPBB9vDC5VXZTjEHnKa9yu33zo7jC` | 8 | `bc1qr4dl5wa7kl8yu792dceg9z5knl2gkn220lk7a9` | 149.44992461 |
| `1G47pVbJ7M1JSmZiqzLoVUw2FzyfUuDEuF` | 25 | `bc1p77rtrsvsrl5nhu44hg7jp5hkz24qx044jgswx7sejpuwqckqcxxq5ejgvr` | 313.66719025 |
| `1HnNoijUSSNMH7SMGZKKsgwEqjCxxHKeN2` | 1 | `bc1qle99f93vzvr2sn28al7vnwrgews6clw5f2a8zl` | 32.54941540 |
| `1BU7pYQvW6CTaLhU39iDh7keaKbTpK4HXC` | 7 | `bc1qvwz4luueypyhthfz7tg0cq9klkfpq34e923x4t` | 47.96988008 |
| `1FFQgpEsQwqyf5psi5tayYFj7Yoz6Ru1tC` | 7 | `bc1qvwz4luueypyhthfz7tg0cq9klkfpq34e923x4t` | 47.96988008 |

## Full chains

### `1313TARBFzoSbXQUL7yu32p9TjoBhUPx2M` — 0 moves, settles at `bc1q7upzc44p5v9wt5se40xt4grpwzalfe85zrcsd9`

| # | Address | Arrived (UTC) | BTC at that address | Status |
|---:|---|---|---:|---|
| 0 | `bc1q7upzc44p5v9wt5se40xt4grpwzalfe85zrcsd9` | 2026-11-12 22:46:48 | 75.00000978 | UNSPENT |

### `1ALpEWtXApR19hLXvcG8NfkPyGsUMhFPMS` — 0 moves, settles at `bc1q8s6khyqw7rarqw0wr9s23mrxjdj2qq40y69gpp`

| # | Address | Arrived (UTC) | BTC at that address | Status |
|---:|---|---|---:|---|
| 0 | `bc1q8s6khyqw7rarqw0wr9s23mrxjdj2qq40y69gpp` | 2026-11-17 02:52:26 | 74.99999601 | UNSPENT |

### `1GdmnuPz5h9SsUvtiFWqTpJ7h28i8hTUQG` — 2 moves, settles at `bc1q3r54h5s3q34k70rqdajdjj5fgykrj6u42wen75`

| # | Address | Arrived (UTC) | BTC at that address | Status |
|---:|---|---|---:|---|
| 0 | `bc1q4ay3j0h8apx5u5t73dhruqekee523cdsepdzk5` | 2026-11-14 06:03:55 | 24.99997530 | moved |
| 1 | `37d2CQCn6rpq9g9XohxhENjxaHKUgddq4M` | 2026-11-14 06:17:29 | 24.99996930 | moved |
| 2 | `bc1q3r54h5s3q34k70rqdajdjj5fgykrj6u42wen75` | 2026-11-14 08:45:47 | 5.53129106 | UNSPENT |

### `1GdN9v11u8ecZF4USpj6GqKhazTxUvy7aY` — 6 moves, settles at `bc1q0x9grmn026zpc9hf880amhs048dg5n6c02x7pw6scqs7cpsq8ueskt88f5`

| # | Address | Arrived (UTC) | BTC at that address | Status |
|---:|---|---|---:|---|
| 0 | `bc1qr6rxdsk4pyqzuw8sq3kht0ldm3syvx9tv4dycc365g9ffv6vr3lqdtnk32` | 2026-11-16 13:02:43 | 7.00000000 | moved |
| 1 | `bc1qppunwskzl7t3mtmcarlvpxjuzd8jy7l7ga49av2c78xg47fujncsrc76y6` | 2026-11-16 16:22:01 | 119.57374785 | moved |
| 2 | `bc1q36me9m90r9t3uzlez4q4pvvzxu75lwf6dj8w4n8tggjupftdf5tsx0dpyq` | 2026-11-17 09:55:00 | 22.31018478 | moved |
| 3 | `1NBSAB1xtgN5BUbUJJtoe8ejySzmqwBjHG` | 2026-11-17 21:41:36 | 3.94492000 | moved |
| 4 | `34QsfLw2TUeaFkfjA7VFsGEkVg2LR4NjMY` | 2026-11-17 21:49:47 | 11.00000000 | moved |
| 5 | `bc1qppunwskzl7t3mtmcarlvpxjuzd8jy7l7ga49av2c78xg47fujncsrc76y6` | 2026-11-18 00:23:05 | 99.34638286 | moved |
| 6 | `bc1q0x9grmn026zpc9hf880amhs048dg5n6c02x7pw6scqs7cpsq8ueskt88f5` | 2026-11-18 21:05:33 | 40.02418139 | UNSPENT |

### `1NoWYaupfeKF93AiXkuPM12JjiaMRw5bZF` — 0 moves, settles at `bc1q366ast325w0s7rjl4vf57jcwg5vp8nhqnam297`

| # | Address | Arrived (UTC) | BTC at that address | Status |
|---:|---|---|---:|---|
| 0 | `bc1q366ast325w0s7rjl4vf57jcwg5vp8nhqnam297` | 2026-11-12 02:08:34 | 18.25998895 | UNSPENT |

### `1AgiG4fzC2Z6fQ1Ltf6tEz9sUBCY1ay2za` — 27 moves, settles at `bc1p77rtrsvsrl5nhu44hg7jp5hkz24qx044jgswx7sejpuwqckqcxxq5ejgvr`

| # | Address | Arrived (UTC) | BTC at that address | Status |
|---:|---|---|---:|---|
| 0 | `361mDjTHQpo6i3rf95KXXirnF7PCuYSCfH` | 2026-11-14 11:54:18 | 4.00000000 | moved |
| 1 | `bc1qpralxa3ke2vk8yflyjq5z3eefzrqz35snxaazcvq5n8tpug8u8lq6jjwss` | 2026-11-14 12:04:55 | 3.80000000 | moved |
| 2 | `352zzNHvsBTaUUNzqB5xbMo8EYdpvHJQB2` | 2026-11-14 12:04:55 | 3.79999398 | moved |
| 3 | `bc1qpralxa3ke2vk8yflyjq5z3eefzrqz35snxaazcvq5n8tpug8u8lq6jjwss` | 2026-11-14 13:32:26 | 1.94900000 | moved |
| 4 | `352zzNHvsBTaUUNzqB5xbMo8EYdpvHJQB2` | 2026-11-14 13:32:26 | 1.94899700 | moved |
| 5 | `bc1qpralxa3ke2vk8yflyjq5z3eefzrqz35snxaazcvq5n8tpug8u8lq6jjwss` | 2026-11-14 16:51:45 | 1.42698668 | moved |
| 6 | `352zzNHvsBTaUUNzqB5xbMo8EYdpvHJQB2` | 2026-11-14 16:51:45 | 1.42697468 | moved |
| 7 | `15DomY3arDPw5jD658Mbf3AY3wT3YCDmW2` | 2026-11-14 23:34:21 | 400.99999412 | moved |
| 8 | `bc1q9exg34dkmgxu28yektgn929jpjpzukhhl2r3zr` | 2026-11-15 00:36:34 | 356.19996959 | moved |
| 9 | `bc1q9exg34dkmgxu28yektgn929jpjpzukhhl2r3zr` | 2026-11-15 01:08:26 | 244.19995397 | moved |
| 10 | `bc1q9exg34dkmgxu28yektgn929jpjpzukhhl2r3zr` | 2026-11-15 01:36:18 | 196.39993714 | moved |
| 11 | `bc1q9exg34dkmgxu28yektgn929jpjpzukhhl2r3zr` | 2026-11-15 01:59:34 | 172.59992031 | moved |
| 12 | `bc1qj5kz6mz40suxmv5mcntgul0pqsz7vyrj62n8fp` | 2026-11-15 01:59:34 | 92.00000000 | moved |
| 13 | `bc1q0frx45s0ssz243jcycxxvw3k4cmx8hnl35zrnh` | 2026-11-15 02:20:27 | 432.00000000 | moved |
| 14 | `bc1q0frx45s0ssz243jcycxxvw3k4cmx8hnl35zrnh` | 2026-11-15 04:32:22 | 300.81711026 | moved |
| 15 | `3NF4VfgYoBkj5iAsWfbZNLYbRERU8cdWKQ` | 2026-11-15 05:12:32 | 485.00000000 | moved |
| 16 | `bc1qzl6fpgtylqrv6r8x2hgsa77lpn0whqchgs64f7` | 2026-11-15 05:25:07 | 467.41612540 | moved |
| 17 | `bc1qzl6fpgtylqrv6r8x2hgsa77lpn0whqchgs64f7` | 2026-11-15 15:07:44 | 1400.68645168 | moved |
| 18 | `bc1qzl6fpgtylqrv6r8x2hgsa77lpn0whqchgs64f7` | 2026-11-16 02:27:06 | 1210.01187671 | moved |
| 19 | `bc1qzl6fpgtylqrv6r8x2hgsa77lpn0whqchgs64f7` | 2026-11-16 15:27:02 | 2461.81391181 | moved |
| 20 | `bc1qzl6fpgtylqrv6r8x2hgsa77lpn0whqchgs64f7` | 2026-11-16 16:11:12 | 1285.37511909 | moved |
| 21 | `bc1qzl6fpgtylqrv6r8x2hgsa77lpn0whqchgs64f7` | 2026-11-16 16:40:06 | 1035.31906744 | moved |
| 22 | `bc1qzl6fpgtylqrv6r8x2hgsa77lpn0whqchgs64f7` | 2026-11-16 17:12:24 | 648.67075849 | moved |
| 23 | `3MqUP6G1daVS5YTD8fz3QgwjZortWwxXFd` | 2026-11-16 17:20:56 | 648.67075849 | moved |
| 24 | `1GX3XHjdWFCsJ9CLdBG42uC2usxm6TV1bQ` | 2026-11-16 17:25:31 | 814.92530000 | moved |
| 25 | `bc1qcyaupd6ln8lwsyldkkf4pp4s3effyedh53terw` | 2026-11-16 17:36:45 | 815.00000000 | moved |
| 26 | `3H48L34TxY1CXdgL291XnVxn9DT3BZJX3m` | 2026-11-16 19:00:39 | 500.00000000 | moved |
| 27 | `bc1p77rtrsvsrl5nhu44hg7jp5hkz24qx044jgswx7sejpuwqckqcxxq5ejgvr` | 2026-11-16 19:21:36 | 313.66719025 | UNSPENT |

### `1BVbQyATQRkN3Y7MX7aspjZsqnrnN2CWbN` — 1 moves, settles at `bc1qlt5nm3kflne7rht4alsnzdzad878ld5rcu4na0`

| # | Address | Arrived (UTC) | BTC at that address | Status |
|---:|---|---|---:|---|
| 0 | `3PDJKAUT1BhaWSbaCgKHdGFogfRLuaqNiu` | 2026-11-15 19:58:06 | 4.99999420 | moved |
| 1 | `bc1qlt5nm3kflne7rht4alsnzdzad878ld5rcu4na0` | 2026-11-15 20:08:54 | 5.00721689 | UNSPENT |

### `1CCoK3bUtothK5GN9vos1mWthdjoa4BqYa` — 2 moves, settles at `1GQdrgqAbkeEPUef1UpiTc4X1mUHMcyuGW`

| # | Address | Arrived (UTC) | BTC at that address | Status |
|---:|---|---|---:|---|
| 0 | `39bUFFWPRcXpJViX532hgJSVnkjpeVV9cL` | 2026-11-13 08:23:52 | 2.00000000 | moved |
| 1 | `bc1qppunwskzl7t3mtmcarlvpxjuzd8jy7l7ga49av2c78xg47fujncsrc76y6` | 2026-11-13 12:28:13 | 40.43504771 | moved |
| 2 | `1GQdrgqAbkeEPUef1UpiTc4X1mUHMcyuGW` | 2026-11-15 02:51:07 | 10.00000000 | UNSPENT |

### `1CA7ysJMPpUav91cHgQ7Aom7suzY9w7YBw` — 22 moves, settles at `3P4mPNtqyFtRCjZwPDg8hE36xYk76VfEme`

| # | Address | Arrived (UTC) | BTC at that address | Status |
|---:|---|---|---:|---|
| 0 | `361mDjTHQpo6i3rf95KXXirnF7PCuYSCfH` | 2026-11-14 17:09:34 | 0.99999000 | moved |
| 1 | `bc1qpralxa3ke2vk8yflyjq5z3eefzrqz35snxaazcvq5n8tpug8u8lq6jjwss` | 2026-11-14 19:30:44 | 0.83435463 | moved |
| 2 | `352zzNHvsBTaUUNzqB5xbMo8EYdpvHJQB2` | 2026-11-14 19:30:44 | 0.83434861 | moved |
| 3 | `bc1qefcww8zpq9xsqmzh8qxq8cseq7uat8mlskk739` | 2026-11-15 01:59:34 | 105.74467639 | moved |
| 4 | `bc1qefcww8zpq9xsqmzh8qxq8cseq7uat8mlskk739` | 2026-11-15 02:57:10 | 484.87519854 | moved |
| 5 | `bc1qefcww8zpq9xsqmzh8qxq8cseq7uat8mlskk739` | 2026-11-15 13:31:33 | 1197.44497736 | moved |
| 6 | `bc1qefcww8zpq9xsqmzh8qxq8cseq7uat8mlskk739` | 2026-11-15 15:39:51 | 1175.69650900 | moved |
| 7 | `bc1qefcww8zpq9xsqmzh8qxq8cseq7uat8mlskk739` | 2026-11-15 16:51:03 | 1182.93262673 | moved |
| 8 | `bc1qefcww8zpq9xsqmzh8qxq8cseq7uat8mlskk739` | 2026-11-15 19:58:06 | 682.81610521 | moved |
| 9 | `1LYhaUGjRARNF2nHit3vy9uLKbvxMffzpW` | 2026-11-15 21:40:51 | 596.54477787 | moved |
| 10 | `33UB1zVipqDkzibpKKYFFLTpdw1uCRUG5V` | 2026-11-15 21:49:36 | 823.00098500 | moved |
| 11 | `bc1qx9n80t5q7tfmutzaj0ramzzzsvtveara68zntc` | 2026-11-15 22:16:39 | 832.10105165 | moved |
| 12 | `bc1qhgnyal5330hdckyvze9ws2h3633l9zmekgm7hk` | 2026-11-16 15:32:02 | 829.41070854 | moved |
| 13 | `bc1qhz82yr3erzlvp06jr6mqktunl4zxxrq6670vqw` | 2026-11-16 20:30:05 | 827.37664522 | moved |
| 14 | `bc1qm2pg360p45n64rwzs42ggdxx8u0mavw4eg6nkp` | 2026-11-16 23:55:29 | 826.96560378 | moved |
| 15 | `bc1qjjl6pglsxr87294xumeym948g0jt7ryk0y9tdl` | 2026-11-17 07:31:31 | 826.37914062 | moved |
| 16 | `bc1qjjl6pglsxr87294xumeym948g0jt7ryk0y9tdl` | 2026-11-17 13:13:22 | 825.91927684 | moved |
| 17 | `bc1qjjl6pglsxr87294xumeym948g0jt7ryk0y9tdl` | 2026-11-17 14:17:39 | 823.50964545 | moved |
| 18 | `bc1q9a9vwpw2jtn3lnqr5jv4gelg65gnmzgja0x68c` | 2026-11-17 16:30:13 | 823.15128397 | moved |
| 19 | `bc1q9a9vwpw2jtn3lnqr5jv4gelg65gnmzgja0x68c` | 2026-11-17 18:01:56 | 822.63281103 | moved |
| 20 | `bc1q9a9vwpw2jtn3lnqr5jv4gelg65gnmzgja0x68c` | 2026-11-17 18:01:56 | 822.63277258 | moved |
| 21 | `bc1qmxz7zzmxhkt40l2dysm5pas9cmelmh2artg6fv` | 2026-11-17 19:58:13 | 818.99678879 | moved |
| 22 | `3P4mPNtqyFtRCjZwPDg8hE36xYk76VfEme` | 2026-11-18 04:27:10 | 550.94490000 | UNSPENT |

### `1CAPqy7AhPehwvmUPouVbVGJsNnvWjjiWY` — 1 moves, settles at `bc1q0dkf8x6rnv7mupgq56z9gcr0kpxxyhlxar3yx0`

| # | Address | Arrived (UTC) | BTC at that address | Status |
|---:|---|---|---:|---|
| 0 | `bc1qmdpkdfu9dzfk5vtjyy3grs2m8yf29a7msa4fls` | 2026-11-14 20:51:55 | 0.99998895 | moved |
| 1 | `bc1q0dkf8x6rnv7mupgq56z9gcr0kpxxyhlxar3yx0` | 2026-11-14 20:51:55 | 0.30813500 | UNSPENT |

### `1Car858fC2J1ZsCv5dmvZbHtKP3Nk2RN8R` — 1 moves, settles at `bc1qcuvxs62p5nllzx9qv9jr0nhj98c3vxv78zzuv2`

| # | Address | Arrived (UTC) | BTC at that address | Status |
|---:|---|---|---:|---|
| 0 | `bc1qu8xlp3jlk3cn8c9jv8zlyader2en573cwf54ef` | 2026-11-14 19:41:02 | 0.99999756 | moved |
| 1 | `bc1qcuvxs62p5nllzx9qv9jr0nhj98c3vxv78zzuv2` | 2026-11-15 21:52:31 | 0.89999738 | UNSPENT |

### `1CsRw431siXGryywxgvreDU1vbbEiXGC7g` — 2 moves, settles at `bc1q37d7vqd4ef7z38hypttvtf972hel273tfav6j6xf5tdzcyempa9spp7wkq`

| # | Address | Arrived (UTC) | BTC at that address | Status |
|---:|---|---|---:|---|
| 0 | `bc1qqwgeqgeja3j9cn2ltvvygnxmzphkq2yn0xwrpd` | 2026-11-14 20:19:53 | 0.99999756 | moved |
| 1 | `bc1qku4zs8lvnsp7u3lxfak6q8ud0agwgj8qr20kc8` | 2026-11-14 20:19:53 | 0.99999635 | moved |
| 2 | `bc1q37d7vqd4ef7z38hypttvtf972hel273tfav6j6xf5tdzcyempa9spp7wkq` | 2026-11-15 07:20:57 | 2.83001507 | UNSPENT |

### `1D5ngkYujbuwNw8XDkm5MiLgwRf467o1ne` — 22 moves, settles at `3P4mPNtqyFtRCjZwPDg8hE36xYk76VfEme`

| # | Address | Arrived (UTC) | BTC at that address | Status |
|---:|---|---|---:|---|
| 0 | `361mDjTHQpo6i3rf95KXXirnF7PCuYSCfH` | 2026-11-14 11:05:58 | 0.60007000 | moved |
| 1 | `bc1qpralxa3ke2vk8yflyjq5z3eefzrqz35snxaazcvq5n8tpug8u8lq6jjwss` | 2026-11-14 11:24:48 | 0.56107000 | moved |
| 2 | `352zzNHvsBTaUUNzqB5xbMo8EYdpvHJQB2` | 2026-11-14 11:24:48 | 0.56106398 | moved |
| 3 | `bc1qefcww8zpq9xsqmzh8qxq8cseq7uat8mlskk739` | 2026-11-15 02:28:23 | 126.57614057 | moved |
| 4 | `bc1qefcww8zpq9xsqmzh8qxq8cseq7uat8mlskk739` | 2026-11-15 02:57:10 | 484.87519854 | moved |
| 5 | `bc1qefcww8zpq9xsqmzh8qxq8cseq7uat8mlskk739` | 2026-11-15 13:31:33 | 1197.44497736 | moved |
| 6 | `bc1qefcww8zpq9xsqmzh8qxq8cseq7uat8mlskk739` | 2026-11-15 15:39:51 | 1175.69650900 | moved |
| 7 | `bc1qefcww8zpq9xsqmzh8qxq8cseq7uat8mlskk739` | 2026-11-15 16:51:03 | 1182.93262673 | moved |
| 8 | `bc1qefcww8zpq9xsqmzh8qxq8cseq7uat8mlskk739` | 2026-11-15 19:58:06 | 682.81610521 | moved |
| 9 | `1LYhaUGjRARNF2nHit3vy9uLKbvxMffzpW` | 2026-11-15 21:40:51 | 596.54477787 | moved |
| 10 | `33UB1zVipqDkzibpKKYFFLTpdw1uCRUG5V` | 2026-11-15 21:49:36 | 823.00098500 | moved |
| 11 | `bc1qx9n80t5q7tfmutzaj0ramzzzsvtveara68zntc` | 2026-11-15 22:16:39 | 832.10105165 | moved |
| 12 | `bc1qhgnyal5330hdckyvze9ws2h3633l9zmekgm7hk` | 2026-11-16 15:32:02 | 829.41070854 | moved |
| 13 | `bc1qhz82yr3erzlvp06jr6mqktunl4zxxrq6670vqw` | 2026-11-16 20:30:05 | 827.37664522 | moved |
| 14 | `bc1qm2pg360p45n64rwzs42ggdxx8u0mavw4eg6nkp` | 2026-11-16 23:55:29 | 826.96560378 | moved |
| 15 | `bc1qjjl6pglsxr87294xumeym948g0jt7ryk0y9tdl` | 2026-11-17 07:31:31 | 826.37914062 | moved |
| 16 | `bc1qjjl6pglsxr87294xumeym948g0jt7ryk0y9tdl` | 2026-11-17 13:13:22 | 825.91927684 | moved |
| 17 | `bc1qjjl6pglsxr87294xumeym948g0jt7ryk0y9tdl` | 2026-11-17 14:17:39 | 823.50964545 | moved |
| 18 | `bc1q9a9vwpw2jtn3lnqr5jv4gelg65gnmzgja0x68c` | 2026-11-17 16:30:13 | 823.15128397 | moved |
| 19 | `bc1q9a9vwpw2jtn3lnqr5jv4gelg65gnmzgja0x68c` | 2026-11-17 18:01:56 | 822.63281103 | moved |
| 20 | `bc1q9a9vwpw2jtn3lnqr5jv4gelg65gnmzgja0x68c` | 2026-11-17 18:01:56 | 822.63277258 | moved |
| 21 | `bc1qmxz7zzmxhkt40l2dysm5pas9cmelmh2artg6fv` | 2026-11-17 19:58:13 | 818.99678879 | moved |
| 22 | `3P4mPNtqyFtRCjZwPDg8hE36xYk76VfEme` | 2026-11-18 04:27:10 | 550.94490000 | UNSPENT |

### `1738b4HYtP312grgHBMXujFwZQnhveYWuj` — 0 moves, settles at `bc1qfr0gspw03yw4pudrr9s7609wyf9t3yjyu4gdh2`

| # | Address | Arrived (UTC) | BTC at that address | Status |
|---:|---|---|---:|---|
| 0 | `bc1qfr0gspw03yw4pudrr9s7609wyf9t3yjyu4gdh2` | 2026-11-18 11:01:11 | 0.76876246 | UNSPENT |

### `1PAT2UPBB9vDC5VXZTjEHnKa9yu33zo7jC` — 8 moves, settles at `bc1qr4dl5wa7kl8yu792dceg9z5knl2gkn220lk7a9`

| # | Address | Arrived (UTC) | BTC at that address | Status |
|---:|---|---|---:|---|
| 0 | `33SxuZH3KYUw4sVHpP8mw573grZHPfFY45` | 2026-11-17 21:38:45 | 0.10000000 | moved |
| 1 | `bc1qmuepnly4lwpw08e4f3cn8r2xg57m3kk5xxk5u2` | 2026-11-17 21:54:41 | 4.94630781 | moved |
| 2 | `bc1qu88mjweplq9efewzng7xcry9p9vdp9ju5j2u33` | 2026-11-17 22:14:23 | 2.49880791 | moved |
| 3 | `bc1qpralxa3ke2vk8yflyjq5z3eefzrqz35snxaazcvq5n8tpug8u8lq6jjwss` | 2026-11-17 22:15:16 | 1.79100000 | moved |
| 4 | `352zzNHvsBTaUUNzqB5xbMo8EYdpvHJQB2` | 2026-11-17 22:15:16 | 1.13920724 | moved |
| 5 | `bc1qpralxa3ke2vk8yflyjq5z3eefzrqz35snxaazcvq5n8tpug8u8lq6jjwss` | 2026-11-17 23:24:11 | 0.81899821 | moved |
| 6 | `352zzNHvsBTaUUNzqB5xbMo8EYdpvHJQB2` | 2026-11-17 23:24:11 | 0.40949580 | moved |
| 7 | `3JZ3byZy7TTrfy8zdxrm9iqJrdsaZ5CrNw` | 2026-11-18 13:28:02 | 147.73750000 | moved |
| 8 | `bc1qr4dl5wa7kl8yu792dceg9z5knl2gkn220lk7a9` | 2026-11-18 17:45:02 | 149.44992461 | UNSPENT |

### `1G47pVbJ7M1JSmZiqzLoVUw2FzyfUuDEuF` — 25 moves, settles at `bc1p77rtrsvsrl5nhu44hg7jp5hkz24qx044jgswx7sejpuwqckqcxxq5ejgvr`

| # | Address | Arrived (UTC) | BTC at that address | Status |
|---:|---|---|---:|---|
| 0 | `3H4o9sFVx3RUvDhYscJaKwvP3KU1WNN35c` | 2026-11-14 19:53:51 | 2.40246333 | moved |
| 1 | `bc1qpralxa3ke2vk8yflyjq5z3eefzrqz35snxaazcvq5n8tpug8u8lq6jjwss` | 2026-11-14 21:22:13 | 1.95884154 | moved |
| 2 | `352zzNHvsBTaUUNzqB5xbMo8EYdpvHJQB2` | 2026-11-14 21:22:13 | 1.25443680 | moved |
| 3 | `bc1qpralxa3ke2vk8yflyjq5z3eefzrqz35snxaazcvq5n8tpug8u8lq6jjwss` | 2026-11-14 23:07:53 | 0.92298500 | moved |
| 4 | `352zzNHvsBTaUUNzqB5xbMo8EYdpvHJQB2` | 2026-11-14 23:07:53 | 0.92297300 | moved |
| 5 | `15DomY3arDPw5jD658Mbf3AY3wT3YCDmW2` | 2026-11-14 23:34:21 | 400.99999412 | moved |
| 6 | `bc1q9exg34dkmgxu28yektgn929jpjpzukhhl2r3zr` | 2026-11-15 00:36:34 | 356.19996959 | moved |
| 7 | `bc1q9exg34dkmgxu28yektgn929jpjpzukhhl2r3zr` | 2026-11-15 01:08:26 | 244.19995397 | moved |
| 8 | `bc1q9exg34dkmgxu28yektgn929jpjpzukhhl2r3zr` | 2026-11-15 01:36:18 | 196.39993714 | moved |
| 9 | `bc1q9exg34dkmgxu28yektgn929jpjpzukhhl2r3zr` | 2026-11-15 01:59:34 | 172.59992031 | moved |
| 10 | `bc1qj5kz6mz40suxmv5mcntgul0pqsz7vyrj62n8fp` | 2026-11-15 01:59:34 | 92.00000000 | moved |
| 11 | `bc1q0frx45s0ssz243jcycxxvw3k4cmx8hnl35zrnh` | 2026-11-15 02:20:27 | 432.00000000 | moved |
| 12 | `bc1q0frx45s0ssz243jcycxxvw3k4cmx8hnl35zrnh` | 2026-11-15 04:32:22 | 300.81711026 | moved |
| 13 | `3NF4VfgYoBkj5iAsWfbZNLYbRERU8cdWKQ` | 2026-11-15 05:12:32 | 485.00000000 | moved |
| 14 | `bc1qzl6fpgtylqrv6r8x2hgsa77lpn0whqchgs64f7` | 2026-11-15 05:25:07 | 467.41612540 | moved |
| 15 | `bc1qzl6fpgtylqrv6r8x2hgsa77lpn0whqchgs64f7` | 2026-11-15 15:07:44 | 1400.68645168 | moved |
| 16 | `bc1qzl6fpgtylqrv6r8x2hgsa77lpn0whqchgs64f7` | 2026-11-16 02:27:06 | 1210.01187671 | moved |
| 17 | `bc1qzl6fpgtylqrv6r8x2hgsa77lpn0whqchgs64f7` | 2026-11-16 15:27:02 | 2461.81391181 | moved |
| 18 | `bc1qzl6fpgtylqrv6r8x2hgsa77lpn0whqchgs64f7` | 2026-11-16 16:11:12 | 1285.37511909 | moved |
| 19 | `bc1qzl6fpgtylqrv6r8x2hgsa77lpn0whqchgs64f7` | 2026-11-16 16:40:06 | 1035.31906744 | moved |
| 20 | `bc1qzl6fpgtylqrv6r8x2hgsa77lpn0whqchgs64f7` | 2026-11-16 17:12:24 | 648.67075849 | moved |
| 21 | `3MqUP6G1daVS5YTD8fz3QgwjZortWwxXFd` | 2026-11-16 17:20:56 | 648.67075849 | moved |
| 22 | `1GX3XHjdWFCsJ9CLdBG42uC2usxm6TV1bQ` | 2026-11-16 17:25:31 | 814.92530000 | moved |
| 23 | `bc1qcyaupd6ln8lwsyldkkf4pp4s3effyedh53terw` | 2026-11-16 17:36:45 | 815.00000000 | moved |
| 24 | `3H48L34TxY1CXdgL291XnVxn9DT3BZJX3m` | 2026-11-16 19:00:39 | 500.00000000 | moved |
| 25 | `bc1p77rtrsvsrl5nhu44hg7jp5hkz24qx044jgswx7sejpuwqckqcxxq5ejgvr` | 2026-11-16 19:21:36 | 313.66719025 | UNSPENT |

### `1HnNoijUSSNMH7SMGZKKsgwEqjCxxHKeN2` — 1 moves, settles at `bc1qle99f93vzvr2sn28al7vnwrgews6clw5f2a8zl`

| # | Address | Arrived (UTC) | BTC at that address | Status |
|---:|---|---|---:|---|
| 0 | `bc1qky8jzu285sk5l4azdexkpwfzlc7p9xnwurzn32` | 2026-11-17 03:42:41 | 28.20774506 | moved |
| 1 | `bc1qle99f93vzvr2sn28al7vnwrgews6clw5f2a8zl` | 2026-11-17 03:55:56 | 32.54941540 | UNSPENT |

### `1BU7pYQvW6CTaLhU39iDh7keaKbTpK4HXC` — 7 moves, settles at `bc1qvwz4luueypyhthfz7tg0cq9klkfpq34e923x4t`

| # | Address | Arrived (UTC) | BTC at that address | Status |
|---:|---|---|---:|---|
| 0 | `bc1qj5hhw38ge72k8zfsm5akem3t6w2sel3d0nvzr6` | 2026-11-13 04:48:31 | 58.98991325 | moved |
| 1 | `bc1qp3pfmfrvu5wtlu3waqn03xe52gzcly799vn4n2` | 2026-11-13 18:12:05 | 58.97990532 | moved |
| 2 | `bc1qd9pja7mlgehlg2dkaa2e4l5f6w4uphxvnc4ns3` | 2026-11-13 18:30:56 | 57.97989746 | moved |
| 3 | `bc1q8ff3aq5a2xyjegd0nj2qyrz4kwlfhdfhgnc9ay` | 2026-11-16 16:22:01 | 55.97989462 | moved |
| 4 | `bc1qsqfnvtdfesdr8zex6dvsy9qdl55h7vx52djxeu` | 2026-11-17 17:44:29 | 55.96989136 | moved |
| 5 | `bc1q9va0jjy0dsmd8d7a7e8ee508wlzqvs7xzu3rzv` | 2026-11-17 18:00:57 | 51.96988851 | moved |
| 6 | `bc1qnf583et0v8uf6km72hnweav6g60trd9lrcdh2w` | 2026-11-18 16:53:14 | 49.96988292 | moved |
| 7 | `bc1qvwz4luueypyhthfz7tg0cq9klkfpq34e923x4t` | 2026-11-18 21:54:58 | 47.96988008 | UNSPENT |

### `1FFQgpEsQwqyf5psi5tayYFj7Yoz6Ru1tC` — 7 moves, settles at `bc1qvwz4luueypyhthfz7tg0cq9klkfpq34e923x4t`

| # | Address | Arrived (UTC) | BTC at that address | Status |
|---:|---|---|---:|---|
| 0 | `bc1qj5hhw38ge72k8zfsm5akem3t6w2sel3d0nvzr6` | 2026-11-13 04:48:31 | 58.98991325 | moved |
| 1 | `bc1qp3pfmfrvu5wtlu3waqn03xe52gzcly799vn4n2` | 2026-11-13 18:12:05 | 58.97990532 | moved |
| 2 | `bc1qd9pja7mlgehlg2dkaa2e4l5f6w4uphxvnc4ns3` | 2026-11-13 18:30:56 | 57.97989746 | moved |
| 3 | `bc1q8ff3aq5a2xyjegd0nj2qyrz4kwlfhdfhgnc9ay` | 2026-11-16 16:22:01 | 55.97989462 | moved |
| 4 | `bc1qsqfnvtdfesdr8zex6dvsy9qdl55h7vx52djxeu` | 2026-11-17 17:44:29 | 55.96989136 | moved |
| 5 | `bc1q9va0jjy0dsmd8d7a7e8ee508wlzqvs7xzu3rzv` | 2026-11-17 18:00:57 | 51.96988851 | moved |
| 6 | `bc1qnf583et0v8uf6km72hnweav6g60trd9lrcdh2w` | 2026-11-18 16:53:14 | 49.96988292 | moved |
| 7 | `bc1qvwz4luueypyhthfz7tg0cq9klkfpq34e923x4t` | 2026-11-18 21:54:58 | 47.96988008 | UNSPENT |

Every transaction id is recorded in `docs/refresh-biggest-chain.csv`.

## Notes

- A "move" is one transaction that spent the tracked output. The starting address itself is hop 0.
- Where a spending transaction had several outputs, only the largest one was followed; the rest are recorded in `nextOutputs` in the underlying data and in the CSV only as the chosen continuation.
- Some large consolidation wallets pull in value from unrelated depositors. When the largest output of such a transaction belongs to a different pool, the chain can jump to a much larger amount — that is the literal result of "always follow the biggest amount", not an attribution of the original coins.
- All chains end with status UNSPENT, i.e. the tracked amount was still sitting at that address on 19 November 2026.
