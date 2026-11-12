package com.quickben22.bitcoinlotto.search

import android.content.Context

interface SearchController {
    fun start(startKeyHex: String, limit: Long? = null, resume: Boolean = false)

    fun stop()
}

class ServiceSearchController(
    private val context: Context,
) : SearchController {
    override fun start(startKeyHex: String, limit: Long?, resume: Boolean) {
        SearchService.start(context, startKeyHex, limit, resume)
    }

    override fun stop() {
        SearchService.stop(context)
    }
}
