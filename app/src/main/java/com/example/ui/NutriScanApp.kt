package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AppLanguage
import com.example.model.StringDictionary
import com.example.ui.children.ChildDetailScreen
import com.example.ui.children.ChildRegistrationScreen
import com.example.ui.children.ChildrenListScreen
import com.example.ui.components.TopAppBarHeader
import com.example.ui.followup.FollowUpScreen
import com.example.ui.home.HomeScreen
import com.example.ui.screening.NewScreeningWizard
import com.example.ui.sync.SyncSettingsScreen
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TealPrimary

@Composable
fun NutriScanApp(
    viewModel: NutriScanViewModel,
    modifier: Modifier = Modifier
) {
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val metrics by viewModel.metrics.collectAsStateWithLifecycle()
    val children by viewModel.allChildren.collectAsStateWithLifecycle()
    val recentScreenings by viewModel.recentScreenings.collectAsStateWithLifecycle()
    val allFollowUps by viewModel.allFollowUps.collectAsStateWithLifecycle()
    val selectedChild by viewModel.selectedChild.collectAsStateWithLifecycle()
    val childScreenings by viewModel.childScreenings.collectAsStateWithLifecycle()
    val preselectedChildId by viewModel.preselectedChildIdForScreening.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    val strings = remember(currentLanguage) {
        StringDictionary.get(currentLanguage)
    }

    val childNameMap = remember(children) {
        children.associate { it.id to "${it.firstName} ${it.lastName}" }
    }

    // Back handling for sub-screens
    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        when (currentScreen) {
            AppScreen.CHILD_DETAIL -> viewModel.navigateTo(AppScreen.CHILDREN)
            AppScreen.REGISTER_CHILD -> viewModel.navigateTo(AppScreen.CHILDREN)
            AppScreen.SCREENING -> viewModel.navigateTo(AppScreen.HOME)
            else -> viewModel.navigateTo(AppScreen.HOME)
        }
    }

    Scaffold(
        topBar = {
            TopAppBarHeader(
                currentLanguage = currentLanguage,
                onLanguageChange = { viewModel.setLanguage(it) },
                pendingSyncCount = metrics.pendingSyncCount,
                onSyncClick = { viewModel.syncNow() }
            )
        },
        bottomBar = {
            // Only show bottom navigation on primary top-level tabs
            if (currentScreen in listOf(AppScreen.HOME, AppScreen.CHILDREN, AppScreen.SCREENING, AppScreen.FOLLOW_UPS, AppScreen.SETTINGS)) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.HOME,
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = strings.homeTab) },
                        label = { Text(strings.homeTab, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            selectedTextColor = TealPrimary,
                            indicatorColor = TealContainer
                        ),
                        modifier = Modifier.testTag("nav_home")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.CHILDREN,
                        onClick = { viewModel.navigateTo(AppScreen.CHILDREN) },
                        icon = { Icon(Icons.Default.People, contentDescription = strings.childrenTab) },
                        label = { Text(strings.childrenTab, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            selectedTextColor = TealPrimary,
                            indicatorColor = TealContainer
                        ),
                        modifier = Modifier.testTag("nav_children")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.SCREENING,
                        onClick = { viewModel.startScreeningForChild(null) },
                        icon = { Icon(Icons.Default.PlayArrow, contentDescription = strings.screeningTab) },
                        label = { Text(strings.screeningTab, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            selectedTextColor = TealPrimary,
                            indicatorColor = TealContainer
                        ),
                        modifier = Modifier.testTag("nav_screening")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.FOLLOW_UPS,
                        onClick = { viewModel.navigateTo(AppScreen.FOLLOW_UPS) },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = strings.followUpTab) },
                        label = { Text(strings.followUpTab, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            selectedTextColor = TealPrimary,
                            indicatorColor = TealContainer
                        ),
                        modifier = Modifier.testTag("nav_followups")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.SETTINGS,
                        onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = strings.settingsTab) },
                        label = { Text(strings.settingsTab, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            selectedTextColor = TealPrimary,
                            indicatorColor = TealContainer
                        ),
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> HomeScreen(
                    strings = strings,
                    metrics = metrics,
                    recentScreenings = recentScreenings,
                    childNameMap = childNameMap,
                    onStartScreeningClick = { viewModel.startScreeningForChild(null) },
                    onRegisterChildClick = { viewModel.navigateTo(AppScreen.REGISTER_CHILD) },
                    onViewChildrenClick = { viewModel.navigateTo(AppScreen.CHILDREN) },
                    onViewFollowUpsClick = { viewModel.navigateTo(AppScreen.FOLLOW_UPS) },
                    onSyncNowClick = { viewModel.syncNow() },
                    isSyncing = isSyncing
                )

                AppScreen.CHILDREN -> ChildrenListScreen(
                    strings = strings,
                    children = children,
                    onChildClick = { viewModel.openChildDetail(it) },
                    onRegisterChildClick = { viewModel.navigateTo(AppScreen.REGISTER_CHILD) }
                )

                AppScreen.SCREENING -> NewScreeningWizard(
                    strings = strings,
                    children = children,
                    preselectedChildId = preselectedChildId,
                    onNavigateToRegisterChild = { viewModel.navigateTo(AppScreen.REGISTER_CHILD) },
                    onScreeningFinished = { viewModel.navigateTo(AppScreen.HOME) },
                    onSaveScreening = { childId, name, cat, edema, grade, muac, w, h, ds, dsn, act, reason, fu, img, ovr ->
                        viewModel.saveScreening(
                            childId = childId,
                            childFullName = name,
                            category = cat,
                            hasEdema = edema,
                            edemaGrade = grade,
                            muacCm = muac,
                            weightKg = w,
                            heightCm = h,
                            dangerSignsPresent = ds,
                            dangerSignsNotes = dsn,
                            actionTaken = act,
                            primaryReason = reason,
                            followUpDate = fu,
                            imageVerdict = img,
                            manualOverride = ovr
                        )
                    }
                )

                AppScreen.FOLLOW_UPS -> FollowUpScreen(
                    strings = strings,
                    followUps = allFollowUps,
                    onCompleteFollowUp = { viewModel.completeFollowUp(it) },
                    onStartScreeningForChild = { viewModel.startScreeningForChild(it) }
                )

                AppScreen.SETTINGS -> SyncSettingsScreen(
                    strings = strings,
                    currentLanguage = currentLanguage,
                    onLanguageChange = { viewModel.setLanguage(it) },
                    pendingSyncCount = metrics.pendingSyncCount,
                    onSyncNow = { viewModel.syncNow() },
                    isSyncing = isSyncing
                )

                AppScreen.CHILD_DETAIL -> {
                    selectedChild?.let { child ->
                        ChildDetailScreen(
                            strings = strings,
                            child = child,
                            screenings = childScreenings,
                            onBack = { viewModel.navigateTo(AppScreen.CHILDREN) },
                            onStartScreening = { viewModel.startScreeningForChild(it) }
                        )
                    } ?: run {
                        viewModel.navigateTo(AppScreen.CHILDREN)
                    }
                }

                AppScreen.REGISTER_CHILD -> ChildRegistrationScreen(
                    strings = strings,
                    onBack = { viewModel.navigateTo(AppScreen.CHILDREN) },
                    onChildRegistered = { _, _ -> },
                    onSaveChild = { fName, lName, s, dob, age, cName, cPhone, reg, z, w, k, got, draft, startScreening ->
                        viewModel.registerChild(
                            firstName = fName,
                            lastName = lName,
                            sex = s,
                            dateOfBirth = dob,
                            estimatedAgeMonths = age,
                            caregiverName = cName,
                            caregiverPhone = cPhone,
                            region = reg,
                            zone = z,
                            woreda = w,
                            kebele = k,
                            gotCommunity = got,
                            isDraft = draft,
                            startScreeningImmediately = startScreening
                        )
                    }
                )
            }
        }
    }
}
