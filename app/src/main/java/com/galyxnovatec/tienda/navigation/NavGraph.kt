package com.galyxnovatec.tienda.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.galyxnovatec.tienda.data.manager.AuthManager
import com.galyxnovatec.tienda.data.manager.CartManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.galyxnovatec.tienda.data.repository.ProductRepository
import com.galyxnovatec.tienda.ui.screens.*

/**
 * Grafo de navegación principal de la aplicación.
 *
 * @param navController Controlador de navegación para gestionar las transiciones.
 */
@Composable
fun GalyNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
    ) {
        composable(Screen.Splash.route) {
            SplashScreen {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
            )
        }

        composable(Screen.Register.route) {
            val context = LocalContext.current
            RegisterScreen(
                onRegisterSuccess = {
                    CoroutineScope(Dispatchers.Main).launch {
                        CartManager.sincronizarConServidor(context)
                    }
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                },
            )
        }

        composable(
            route = Screen.Home.route,
            arguments = listOf(
                navArgument("category") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { backStackEntry ->
            val rawCategory = backStackEntry.arguments?.getString("category")
            val cleanCategory = if (rawCategory.isNullOrBlank() || rawCategory == "{category}" || rawCategory.startsWith("{")) null else rawCategory
            HomeScreen(
                categoriaInicial = cleanCategory,
                alSeleccionarProducto = { producto ->
                    navController.navigate(Screen.Detail.createRoute(producto.id))
                },
                alIrAlCarrito = {
                    navController.navigate(Screen.Cart.route)
                },
                alIrAFavoritos = {
                    navController.navigate(Screen.Favorites.route)
                },
                alIrACategorias = {
                    navController.navigate(Screen.Categories.route)
                },
                alIrAPerfil = {
                    navController.navigate(Screen.Profile.route)
                },
                alIrAPanelAdmin = {
                    navController.navigate(Screen.AdminPanel.route)
                },
                alIrAServicioTecnico = {
                    navController.navigate(Screen.TechnicalService.route)
                },
                alIrAChatAi = {
                    navController.navigate(Screen.AiChat.route)
                },
            )
        }
        
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("productId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val context = LocalContext.current
            val productId = backStackEntry.arguments?.getInt("productId")
            productId?.let { ProductRepository.obtenerPorId(it) }?.let { producto ->
                DetailScreen(
                    producto = producto,
                    alVolver = { navController.popBackStack() },
                    alIrAlCarrito = { navController.navigate(Screen.Cart.route) },
                    alAgregarAlCarrito = { p, cant ->
                        CartManager.agregarProducto(context, p, cant)
                    },
                    alComprarAhora = { p, cant ->
                        CartManager.agregarProducto(context, p, cant)
                        if (AuthManager.currentUser == null) {
                            Toast.makeText(context, "Inicia sesión antes de finalizar tu compra.", Toast.LENGTH_LONG).show()
                            navController.navigate(Screen.Login.route)
                        } else {
                            navController.navigate(Screen.Checkout.route)
                        }
                    },
                    alReservar = { p, cant ->
                        val userId = AuthManager.currentUser?.id
                        if (userId == null) {
                            Toast.makeText(context, "Inicia sesión para apartar un producto.", Toast.LENGTH_LONG).show()
                            navController.navigate(Screen.Login.route)
                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                val error = ProductRepository.crearReserva(userId, p.id, cant)
                                Toast.makeText(
                                    context,
                                    error ?: "Producto apartado por 24 horas.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    },
                    alSeleccionarProductoRelacionado = { rel ->
                        navController.navigate(Screen.Detail.createRoute(rel.id))
                    },
                )
            }
        }
        
        composable(Screen.Cart.route) {
            val context = LocalContext.current
            CartScreen(
                alVolver = { navController.popBackStack() },
                alIrACheckout = {
                    if (AuthManager.currentUser == null) {
                        Toast.makeText(context, "Inicia sesión antes de finalizar tu compra.", Toast.LENGTH_LONG).show()
                        navController.navigate(Screen.Login.route)
                    } else {
                        navController.navigate(Screen.Checkout.route)
                    }
                },
            )
        }
        
        composable(Screen.Checkout.route) {
            CheckoutScreen(
                alVolver = { navController.popBackStack() },
                alIrADirecciones = { navController.navigate(Screen.Addresses.route) },
                alIrAPagoWeb = { url -> navController.navigate(Screen.PaymentWebView.createRoute(url)) },
                alFinalizarCompra = {
                    navController.navigate(Screen.Success.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                alSeleccionarProducto = { producto ->
                    navController.navigate(Screen.Detail.createRoute(producto.id))
                },
                alVolver = { navController.popBackStack() },
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                alIrAMisDatos = {
                    navController.navigate(Screen.UserData.route)
                },
                alIrAMisPedidos = {
                    navController.navigate(Screen.Orders.route)
                },
                alIrAMisFavoritos = {
                    navController.navigate(Screen.Favorites.route)
                },
                alIrAComprasRecientes = {
                    navController.navigate(Screen.Orders.route) // Reutilizamos pedidos para compras recientes
                },
                alIrAMisDirecciones = {
                    navController.navigate(Screen.Addresses.route)
                },
                alIrAMetodosPago = {
                    navController.navigate(Screen.PaymentMethods.route)
                },
                alIrACupones = { navController.navigate(Screen.Cupones.route) },
                alIrANotificaciones = { navController.navigate(Screen.Notificaciones.route) },
                alIrAServicioTecnico = {
                    navController.navigate(Screen.TechnicalService.route)
                },
                alIrANosotros = {
                    navController.navigate(Screen.About.route)
                },
                alIrAReservas = {
                    navController.navigate(Screen.Reservations.route)
                },
                alIrAPanelAdmin = {
                    navController.navigate(Screen.AdminPanel.route)
                },
                alCerrarSesion = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                alVolver = { navController.popBackStack() },
            )
        }
        
        composable(Screen.Addresses.route) {
            AddressesScreen(
                alSeleccionar = { dir ->
                    CartManager.direccionSeleccionada = dir
                    navController.popBackStack()
                },
                alVolver = { navController.popBackStack() },
            )
        }
        
        composable(Screen.Categories.route) {
            CategoriesScreen(
                alSeleccionarCategoria = { cat ->
                    navController.navigate(Screen.Home.createRoute(cat)) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                alVolver = { navController.popBackStack() },
            )
        }
        
        composable(Screen.Orders.route) {
            OrdersScreen(
                alVerSeguimiento = { orderId ->
                    navController.navigate(Screen.Tracking.createRoute(orderId))
                },
                alVolver = { navController.popBackStack() },
            )
        }
        
        composable(Screen.TechnicalService.route) {
            TechnicalServiceScreen(
                alVolver = { navController.popBackStack() },
            )
        }
        
        composable(Screen.AdminPanel.route) {
            AdminPanelScreen(
                alVolver = { navController.popBackStack() },
                alIrAAgregarProducto = { navController.navigate(Screen.AddProduct.route) },
                alIrACategorias = {
                    navController.navigate(Screen.Categories.route)
                },
                alIrAPedidos = { navController.navigate(Screen.AdminOrders.route) },
                alIrABanners = { navController.navigate(Screen.AdminBanners.route) },
                alIrAUsuarios = { navController.navigate(Screen.AdminUsers.route) },
                alIrAOfertas = { navController.navigate(Screen.AdminOffers.route) },
                alIrACupones = { navController.navigate(Screen.AdminCoupons.route) },
                alIrANotificaciones = { navController.navigate(Screen.AdminNotifications.route) },
                alIrAGestionarCategorias = { navController.navigate(Screen.AdminCategories.route) },
                alIrAGestionarMarcas = { navController.navigate(Screen.AdminBrands.route) },
                alIrAContacto = { navController.navigate(Screen.AdminContact.route) },
                alIrANosotros = { navController.navigate(Screen.AdminNosotros.route) },
                alIrAGestionarProductos = { navController.navigate(Screen.AdminProducts.route) },
                alIrAGestionarTickets = { navController.navigate(Screen.AdminTickets.route) }
            )
        }

        composable(Screen.AdminCoupons.route) {
            AdminCouponsScreen(alVolver = { navController.popBackStack() })
        }

        composable(Screen.AdminOffers.route) {
            AdminOffersScreen(alVolver = { navController.popBackStack() })
        }

        composable(Screen.AdminNotifications.route) {
            AdminNotificationsScreen(alVolver = { navController.popBackStack() })
        }

        composable(Screen.AdminTickets.route) {
            AdminTicketsScreen(alVolver = { navController.popBackStack() })
        }

        composable(Screen.AdminProducts.route) {
            AdminProductsScreen(
                alVolver = { navController.popBackStack() },
                alIrAEditarProducto = { productId ->
                    navController.navigate(Screen.EditProduct.createRoute(productId))
                }
            )
        }

        composable(
            route = Screen.EditProduct.route,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: -1
            EditProductScreen(
                productId = productId,
                alVolver = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminCategories.route) {
            AdminCategoriesScreen(alVolver = { navController.popBackStack() })
        }

        composable(Screen.AdminBrands.route) {
            AdminBrandsScreen(alVolver = { navController.popBackStack() })
        }

        composable(Screen.AdminContact.route) {
            AdminContactScreen(alVolver = { navController.popBackStack() })
        }

        composable(Screen.AdminNosotros.route) {
            AdminNosotrosScreen(alVolver = { navController.popBackStack() })
        }

        composable(Screen.About.route) {
            AboutScreen(alVolver = { navController.popBackStack() })
        }

        composable(Screen.Reservations.route) {
            ReservationsScreen(alVolver = { navController.popBackStack() })
        }

        composable(Screen.AiChat.route) {
            GalyAiChatScreen(
                alVolver = { navController.popBackStack() },
                alSeleccionarProducto = { productId ->
                    navController.navigate(Screen.Detail.createRoute(productId))
                }
            )
        }

        composable(Screen.AdminBanners.route) {
            AdminBannersScreen(alVolver = { navController.popBackStack() })
        }

        composable(Screen.AdminUsers.route) {
            AdminUsersScreen(alVolver = { navController.popBackStack() })
        }

        composable(Screen.AddProduct.route) {
            AddProductScreen(
                alVolver = { navController.popBackStack() },
            )
        }

        composable(Screen.UserData.route) {
            UserDataScreen(alVolver = { navController.popBackStack() })
        }

        composable(Screen.PaymentMethods.route) {
            PaymentMethodsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.AdminOrders.route) {
            AdminOrdersScreen(alVolver = { navController.popBackStack() })
        }

        composable(
            route = Screen.Tracking.route,
            arguments = listOf(navArgument("orderId") { type = NavType.IntType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getInt("orderId") ?: -1
            TrackingScreen(
                orderId = orderId,
                alVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PaymentWebView.route,
            arguments = listOf(navArgument("url") { type = NavType.StringType })
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: ""
            PaymentWebViewScreen(
                url = url,
                onSuccess = {
                    navController.navigate(Screen.Success.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onFailure = {
                    Toast.makeText(navController.context, "El pago no pudo completarse", Toast.LENGTH_LONG).show()
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Cupones.route) {
            CuponesScreen(
                alVolver = { navController.popBackStack() },
                alIrAlCarrito = { navController.navigate(Screen.Cart.route) }
            )
        }

        composable(Screen.Notificaciones.route) {
            NotificacionesScreen(alVolver = { navController.popBackStack() })
        }

        composable(Screen.Success.route) {
            SuccessScreen(
                alTerminar = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
            )
        }
    }
}
