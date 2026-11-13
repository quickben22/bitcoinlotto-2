package com.quickben22.bitcoinlotto.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.quickben22.bitcoinlotto.BitcoinLottoApp
import com.quickben22.bitcoinlotto.R
import com.quickben22.bitcoinlotto.ads.AdBanner
import com.quickben22.bitcoinlotto.feature.solver.SolverRoute
import com.quickben22.bitcoinlotto.feature.stats.StatsRoute
import com.quickben22.bitcoinlotto.ui.components.AppBackground
import com.quickben22.bitcoinlotto.ui.components.PrivacyDialog
import com.quickben22.bitcoinlotto.ui.components.SecureScreen
import com.quickben22.bitcoinlotto.ui.components.findActivity

private object Routes {
    const val STATS = "stats"
    const val SOLVER = "solver"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BitcoinLottoRoot() {
    SecureScreen()
    val context = LocalContext.current
    val app = context.applicationContext as BitcoinLottoApp
    val factory = remember(app) { AppViewModelFactory(app.container) }
    val adsConsentManager = remember(app) { app.container.adsConsentManager }
    val canRequestAds by adsConsentManager.adRequestsAllowed.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.STATS

    var menuExpanded by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (currentRoute == Routes.SOLVER) R.string.solver else R.string.app_name,
                        ),
                    )
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_more),
                                contentDescription = stringResource(R.string.more_options),
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.privacy_options)) },
                                onClick = {
                                    menuExpanded = false
                                    showPrivacy = true
                                },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
        bottomBar = {
            Column {
                AdBanner(canRequestAds = canRequestAds)
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                ) {
                    NavigationBarItem(
                        selected = currentRoute == Routes.STATS,
                        onClick = {
                            navController.navigate(Routes.STATS) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_stats),
                                contentDescription = stringResource(R.string.stats),
                            )
                        },
                        label = { Text(stringResource(R.string.stats)) },
                        colors = navigationColors(),
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.SOLVER,
                        onClick = {
                            navController.navigate(Routes.SOLVER) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_solver),
                                contentDescription = stringResource(R.string.solver),
                            )
                        },
                        label = { Text(stringResource(R.string.solver)) },
                        colors = navigationColors(),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.STATS,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.STATS) {
                StatsRoute(factory = factory)
            }
            composable(Routes.SOLVER) {
                SolverRoute(factory = factory)
            }
        }
    }

        if (showPrivacy) {
            PrivacyDialog(
                onDismiss = { showPrivacy = false },
                onManagePrivacy = {
                    val activity = context.findActivity()
                    if (activity == null) {
                        showPrivacy = false
                    } else {
                        adsConsentManager.showPrivacyOptions(activity) {
                            showPrivacy = false
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun navigationColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.primary,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
)
