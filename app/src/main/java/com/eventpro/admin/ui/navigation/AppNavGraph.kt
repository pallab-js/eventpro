package com.eventpro.admin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.eventpro.admin.ui.clients.AddEditClientScreen
import com.eventpro.admin.ui.clients.ClientDetailScreen
import com.eventpro.admin.ui.clients.ClientListScreen
import com.eventpro.admin.ui.dashboard.DashboardScreen
import com.eventpro.admin.ui.events.AddEditEventScreen
import com.eventpro.admin.ui.events.EventDetailScreen
import com.eventpro.admin.ui.events.EventListScreen
import com.eventpro.admin.ui.inventory.AddEditInventoryScreen
import com.eventpro.admin.ui.inventory.InventoryScreen
import com.eventpro.admin.ui.ledger.AddTransactionScreen
import com.eventpro.admin.ui.ledger.LedgerScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) { DashboardScreen(navController) }
        composable(Screen.Events.route) { EventListScreen(navController) }
        composable(
            Screen.EventDetail.route,
            arguments = listOf(navArgument(Screen.EventDetail.ARG) { type = NavType.LongType })
        ) { EventDetailScreen(navController, it.arguments!!.getLong(Screen.EventDetail.ARG)) }
        composable(
            Screen.AddEditEvent.route,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType; defaultValue = -1L })
        ) { AddEditEventScreen(navController, it.arguments?.getLong("eventId")?.takeIf { id -> id != -1L }) }
        composable(Screen.Clients.route) { ClientListScreen(navController) }
        composable(
            Screen.ClientDetail.route,
            arguments = listOf(navArgument(Screen.ClientDetail.ARG) { type = NavType.LongType })
        ) { ClientDetailScreen(navController, it.arguments!!.getLong(Screen.ClientDetail.ARG)) }
        composable(
            Screen.AddEditClient.route,
            arguments = listOf(navArgument("clientId") { type = NavType.LongType; defaultValue = -1L })
        ) { AddEditClientScreen(navController, it.arguments?.getLong("clientId")?.takeIf { id -> id != -1L }) }
        composable(Screen.Inventory.route) { InventoryScreen(navController) }
        composable(
            Screen.AddEditInventory.route,
            arguments = listOf(navArgument("itemId") { type = NavType.LongType; defaultValue = -1L })
        ) { AddEditInventoryScreen(navController, it.arguments?.getLong("itemId")?.takeIf { id -> id != -1L }) }
        composable(Screen.Ledger.route) { LedgerScreen(navController) }
        composable(Screen.AddTransaction.route) { AddTransactionScreen(navController) }
    }
}
