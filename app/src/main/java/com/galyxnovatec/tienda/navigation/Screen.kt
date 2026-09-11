package com.galyxnovatec.tienda.navigation

/**
 * Representa todas las rutas de navegación disponibles en la aplicación GALY.
 *
 * @property route Identificador único de la ruta para el NavHost.
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home?category={category}") {
        fun createRoute(category: String? = null) = if (category != null) "home?category=$category" else "home"
    }
    object Detail : Screen("detail/{productId}") {
        fun createRoute(productId: Int) = "detail/$productId"
    }
    object Cart : Screen("cart")
    object Favorites : Screen("favorites")
    object Profile : Screen("profile")
    object Categories : Screen("categories")
    object Success : Screen("success")
    object Checkout : Screen("checkout")
    object Orders : Screen("orders")
    object TechnicalService : Screen("technical_service")
    object Addresses : Screen("addresses")
    object AdminPanel : Screen("admin_panel")
    object AddProduct : Screen("add_product")
    object AdminBanners : Screen("admin_banners")
    object AdminUsers : Screen("admin_users")
    object AdminCategories : Screen("admin_categories")
    object AdminProducts : Screen("admin_products")
    object EditProduct : Screen("edit_product/{productId}") {
        fun createRoute(productId: Int) = "edit_product/$productId"
    }
    object AiChat : Screen("ai_chat")
    object YapePayment : Screen("yape_payment")
    object UserData : Screen("user_data")
    object AdminOrders : Screen("admin_orders")
}
