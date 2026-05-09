package com.eventpro.admin.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.eventpro.admin.R
import com.eventpro.admin.ui.navigation.Screen

private data class NavItem(val screen: Screen, val labelRes: Int, val icon: ImageVector)

private val items = listOf(
    NavItem(Screen.Dashboard, R.string.nav_dashboard, Icons.Outlined.Dashboard),
    NavItem(Screen.Events, R.string.nav_events, Icons.Outlined.CalendarToday),
    NavItem(Screen.Clients, R.string.nav_clients, Icons.Outlined.Groups),
    NavItem(Screen.Inventory, R.string.nav_inventory, Icons.Outlined.Inventory2),
    NavItem(Screen.Ledger, R.string.nav_ledger, Icons.Outlined.AccountBalanceWallet)
)

@Composable
fun BottomNavBar(navController: NavController) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    NavigationBar {
        items.forEach { item ->
            val label = stringResource(item.labelRes)
            NavigationBarItem(
                selected = currentRoute == item.screen.route,
                onClick = {
                    if (currentRoute != item.screen.route) {
                        navController.navigate(item.screen.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = label) },
                label = { Text(label) }
            )
        }
    }
}
