package com.quickben22.bitcoinlotto.data

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager

interface CheckpointCipher {
    fun encrypt(plaintext: ByteArray): ByteArray

    fun decrypt(ciphertext: ByteArray): ByteArray
}

class AeadCheckpointCipher(
    private val aead: Aead,
) : CheckpointCipher {
    override fun encrypt(plaintext: ByteArray): ByteArray =
        aead.encrypt(plaintext, ASSOCIATED_DATA)

    override fun decrypt(ciphertext: ByteArray): ByteArray =
        aead.decrypt(ciphertext, ASSOCIATED_DATA)

    private companion object {
        val ASSOCIATED_DATA = "bitcoinlotto-checkpoint".toByteArray(Charsets.UTF_8)
    }
}

class TinkCheckpointCipher(context: Context) : CheckpointCipher {
    private val delegate by lazy {
        AeadConfig.register()
        val keysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context.applicationContext, KEYSET_NAME, PREF_FILE_NAME)
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
        AeadCheckpointCipher(keysetHandle.getPrimitive(Aead::class.java))
    }

    override fun encrypt(plaintext: ByteArray): ByteArray = delegate.encrypt(plaintext)

    override fun decrypt(ciphertext: ByteArray): ByteArray = delegate.decrypt(ciphertext)

    private companion object {
        const val KEYSET_NAME = "checkpoint_keyset"
        const val PREF_FILE_NAME = "checkpoint_keyset_prefs"
        const val MASTER_KEY_URI = "android-keystore://bitcoinlotto_checkpoint_master_key"
    }
}
