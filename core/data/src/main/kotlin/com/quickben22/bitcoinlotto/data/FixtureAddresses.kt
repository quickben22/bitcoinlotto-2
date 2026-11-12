package com.quickben22.bitcoinlotto.data

object FixtureAddresses {
    const val KEY_1_COMPRESSED = "185iZWp5JuBnK2tUWMB39WagHuPmJvRBRW"
    const val KEY_1_UNCOMPRESSED = "19MoUVhGQ7kKCoWk8UuYAqqqnCYdw96ASi"
    const val KEY_2_COMPRESSED = "1g4yqipc6uTfrdn14AfLhxdUePexjgvnq"
    const val KEY_2_UNCOMPRESSED = "1DotngYyJFzqdZ43DS1ZLFHB59sLYRDNVM"
    const val KEY_3_COMPRESSED = "1Q5o8PCPxKEwPPV2MzWWjSRgQYgnohnsmF"
    const val KEY_3_UNCOMPRESSED = "14v2rFnEYpjz4gpUTR7dMifMfpgEZ5Vg6s"

    val all: Set<String> = linkedSetOf(
        KEY_1_COMPRESSED,
        KEY_1_UNCOMPRESSED,
        KEY_2_COMPRESSED,
        KEY_2_UNCOMPRESSED,
        KEY_3_COMPRESSED,
        KEY_3_UNCOMPRESSED,
    )
}
