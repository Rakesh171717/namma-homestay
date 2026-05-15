package com.example.namma_homestay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.namma_homestay.screens.DailyMenuScreen
import com.example.namma_homestay.screens.HomeProfileScreen
import com.example.namma_homestay.screens.InquiryBoxScreen
import com.example.namma_homestay.screens.LocalGuideScreen
import com.example.namma_homestay.screens.DashboardScreen
import com.example.namma_homestay.screens.CalendarScreen
import com.example.namma_homestay.screens.auth.LoginScreen
import com.example.namma_homestay.screens.auth.SignUpScreen
import com.example.namma_homestay.screens.user.TravelerHomeScreen
import com.example.namma_homestay.screens.user.HomestayDetailScreen
import androidx.compose.runtime.collectAsState

sealed class Screen(val route: String, val title: String, val icon: ImageVector, val unselectedIcon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
    object Calendar : Screen("calendar", "Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth)
    object Inquiries : Screen("inquiries", "Inbox", Icons.Filled.Chat, Icons.Outlined.Chat)
    object Menu : Screen("menu", "Menu", Icons.Filled.MenuBook, Icons.Outlined.MenuBook)
    object Profile : Screen("profile", "Profile", Icons.Filled.Home, Icons.Outlined.Home)
    object Guide : Screen("guide", "Guide", Icons.Filled.Map, Icons.Outlined.Map)
}

val items = listOf(
    Screen.Dashboard,
    Screen.Calendar,
    Screen.Inquiries,
    Screen.Profile,
    Screen.Menu,
    Screen.Guide
)

@Composable
fun NammaHomestayApp(authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val authUiState by authViewModel.authUiState.collectAsState()

    if (authUiState.isLoading && !isAuthenticated) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (!isAuthenticated) {
        AuthApp(authViewModel)
    } else {
        when (currentUser.role) {
            UserRole.HOST -> HostApp(authViewModel)
            UserRole.TRAVELER -> TravelerApp(authViewModel)
            else -> AuthApp(authViewModel)
        }
    }
}

@Composable
fun AuthApp(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authUiState by authViewModel.authUiState.collectAsState()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                authUiState = authUiState,
                onLoginClick = { email, password -> authViewModel.login(email, password) },
                onNavigateToSignUp = { navController.navigate("signup") },
                onErrorShown = { authViewModel.clearError() }
            )
        }
        composable("signup") {
            SignUpScreen(
                authUiState = authUiState,
                onSignUpClick = { name, email, password, role -> authViewModel.signUp(name, email, password, role) },
                onNavigateToLogin = { navController.navigate("login") { popUpTo("login") { inclusive = true } } },
                onErrorShown = { authViewModel.clearError() }
            )
        }
    }
}

@Composable
fun TravelerApp(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            TravelerHomeScreen(
                onNavigateToDetail = { homestay -> navController.navigate("detail/${homestay.name}") },
                onLogout = { authViewModel.logout() }
            )
        }
        composable("detail/{homestayName}") { backStackEntry ->
            val name = backStackEntry.arguments?.getString("homestayName") ?: ""
            HomestayDetailScreen(
                homestayName = name,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun HostApp(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val haptic = LocalHapticFeedback.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.icon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title, fontWeight = FontWeight.Bold) },
                        selected = isSelected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            scope.launch { drawerState.close() }
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Logout, contentDescription = "Logout") },
                    label = { Text("Logout", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        authViewModel.logout()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    tonalElevation = 8.dp
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    items.forEach { screen ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.icon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                if (isSelected) {
                                    Text(screen.title, fontWeight = FontWeight.Bold)
                                }
                            },
                            alwaysShowLabel = false,
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                val openDrawer = { scope.launch { drawerState.open() } }
                
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        onMenuClick = { openDrawer() },
                        onUpdateMenuClick = { navController.navigate(Screen.Menu.route) },
                        onManageProfileClick = { navController.navigate(Screen.Profile.route) }
                    )
                }
                composable(Screen.Calendar.route) { CalendarScreen(onMenuClick = { openDrawer() }) }
                composable(Screen.Profile.route) { HomeProfileScreen(onMenuClick = { openDrawer() }) }
                composable(Screen.Menu.route) { DailyMenuScreen(onMenuClick = { openDrawer() }) }
                composable(Screen.Inquiries.route) { InquiryBoxScreen(onMenuClick = { openDrawer() }) }
                composable(Screen.Guide.route) { LocalGuideScreen(onMenuClick = { openDrawer() }) }
            }
        }
    }
}
