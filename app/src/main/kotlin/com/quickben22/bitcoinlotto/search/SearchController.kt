package com.quickben22.bitcoinlotto.search

interface SearchController {
    fun start(startKeyHex: String, limit: Long? = null, resume: Boolean = false)

    fun stop()
}
