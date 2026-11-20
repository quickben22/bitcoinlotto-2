package com.quickben22.bitcoinlotto

import android.content.Context
import com.quickben22.bitcoinlotto.ads.AdsConsentManager
import com.quickben22.bitcoinlotto.ads.RewardedAdManager
import com.quickben22.bitcoinlotto.data.AddressRepository
import com.quickben22.bitcoinlotto.data.CheckpointCipher
import com.quickben22.bitcoinlotto.data.SearchCheckpointStore
import com.quickben22.bitcoinlotto.data.SearchedKeyRangeStore
import com.quickben22.bitcoinlotto.data.SqliteAddressRepository
import com.quickben22.bitcoinlotto.data.SqliteSearchedKeyRangeStore
import com.quickben22.bitcoinlotto.data.TinkCheckpointCipher
import com.quickben22.bitcoinlotto.domain.usecase.CheckPrivateKeyUseCase
import com.quickben22.bitcoinlotto.domain.usecase.PrivateKeyGenerator
import com.quickben22.bitcoinlotto.domain.usecase.SecureRandomPrivateKeyGenerator
import com.quickben22.bitcoinlotto.search.AddressChecker
import com.quickben22.bitcoinlotto.search.RepositoryAddressChecker
import com.quickben22.bitcoinlotto.search.SearchEngine
import com.quickben22.bitcoinlotto.search.SearchStateHolder
import com.quickben22.bitcoinlotto.search.VisibleSearchController

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val checkpointCipher: CheckpointCipher by lazy { TinkCheckpointCipher(appContext) }

    val addressRepository: AddressRepository by lazy { SqliteAddressRepository.open(appContext) }

    val checkpointStore: SearchCheckpointStore by lazy {
        SearchCheckpointStore(appContext, checkpointCipher)
    }

    val searchedKeyRangeStore: SearchedKeyRangeStore by lazy {
        SqliteSearchedKeyRangeStore(appContext, checkpointCipher)
    }

    val searchState: SearchStateHolder = SearchStateHolder()

    val adsConsentManager: AdsConsentManager by lazy { AdsConsentManager(appContext) }

    val rewardedAdManager: RewardedAdManager by lazy {
        RewardedAdManager(appContext, BuildConfig.ADMOB_REWARDED_UNIT_ID)
    }

    val checkPrivateKey: CheckPrivateKeyUseCase by lazy { CheckPrivateKeyUseCase { addressRepository } }

    val addressChecker: AddressChecker by lazy { RepositoryAddressChecker { addressRepository } }

    val searchEngine: SearchEngine by lazy { SearchEngine(addressChecker) }

    val searchController: VisibleSearchController by lazy {
        VisibleSearchController(
            engine = searchEngine,
            store = checkpointStore,
            state = searchState,
            searchedRanges = searchedKeyRangeStore,
        )
    }

    val privateKeyGenerator: PrivateKeyGenerator by lazy { SecureRandomPrivateKeyGenerator() }
}
