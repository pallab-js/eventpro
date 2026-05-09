package com.eventpro.admin.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Events : Screen("events")
    object Clients : Screen("clients")
    object Inventory : Screen("inventory")
    object Ledger : Screen("ledger")
    object AddTransaction : Screen("add_transaction")

    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(id: Long) = "event_detail/$id"
        const val ARG = "eventId"
    }
    object AddEditEvent : Screen("add_edit_event?eventId={eventId}") {
        fun createRoute(id: Long? = null) = if (id != null) "add_edit_event?eventId=$id" else "add_edit_event"
    }
    object ClientDetail : Screen("client_detail/{clientId}") {
        fun createRoute(id: Long) = "client_detail/$id"
        const val ARG = "clientId"
    }
    object AddEditClient : Screen("add_edit_client?clientId={clientId}") {
        fun createRoute(id: Long? = null) = if (id != null) "add_edit_client?clientId=$id" else "add_edit_client"
    }
    object AddEditInventory : Screen("add_edit_inventory?itemId={itemId}") {
        fun createRoute(id: Long? = null) = if (id != null) "add_edit_inventory?itemId=$id" else "add_edit_inventory"
    }
}
