package com.quickben22.bitcoinlotto.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun `private key is split into two monospace friendly lines`() {
        val key = "0000000000000000000000000000000000000000000000000000000000000001"

        assertEquals(
            "00000000000000000000000000000000\n00000000000000000000000000000001",
            formatPrivateKey(key),
        )
    }

    @Test
    fun `empty private key stays empty`() {
        assertEquals("", formatPrivateKey(""))
    }

    @Test
    fun `short private key is not padded`() {
        assertEquals("abcd", formatPrivateKey("abcd"))
    }
}
