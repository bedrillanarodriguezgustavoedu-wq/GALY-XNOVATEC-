package com.galyxnovatec.tienda

import com.galyxnovatec.tienda.data.manager.AuthManager
import com.galyxnovatec.tienda.data.manager.CartManager
import com.galyxnovatec.tienda.data.model.CartItem
import com.galyxnovatec.tienda.data.model.Producto
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CartManagerTest {

    @Before
    fun setUp() {
        CartManager.items.clear()
        CartManager.quitarCupon()
        AuthManager.currentUser = null
    }

    @Test
    fun testSubtotalCalculation() {
        val prod1 = Producto(
            id = 1,
            nombre = "Laptop Gaming",
            marca = "ASUS",
            categoria = "Laptops",
            precioNormal = 3000.0,
            precioOferta = 2500.0
        )
        val prod2 = Producto(
            id = 2,
            nombre = "Mouse Gamer",
            marca = "Logitech",
            categoria = "Periféricos",
            precioNormal = 100.0,
            precioOferta = 80.0
        )

        CartManager.items.add(CartItem(producto = prod1, cantidad = 1, isSelected = true))
        CartManager.items.add(CartItem(producto = prod2, cantidad = 2, isSelected = true))

        val subtotal = CartManager.obtenerSubtotal()

        // 2500 * 1 + 80 * 2 = 2660.0
        assertEquals(2660.0, subtotal, 0.01)
    }

    @Test
    fun testTotalItemsCount() {
        val prod1 = Producto(id = 1, nombre = "Producto Prueba", marca = "Marca", categoria = "Cat", precioNormal = 10.0, precioOferta = 10.0)
        CartManager.items.add(CartItem(producto = prod1, cantidad = 3, isSelected = true))
        assertEquals(3, CartManager.totalItems())
    }

    @Test
    fun testSubtotalIgnoraLosItemsDesmarcados() {
        val prod = Producto(id = 1, nombre = "Audifonos", marca = "HyperX", categoria = "Audio", precioNormal = 300.0, precioOferta = 250.0)
        CartManager.items.add(CartItem(producto = prod, cantidad = 2, isSelected = true))
        CartManager.items.add(CartItem(producto = prod.copy(id = 2), cantidad = 5, isSelected = false))

        assertEquals(500.0, CartManager.obtenerSubtotal(), 0.01)
        assertEquals(7, CartManager.totalItems())
        assertEquals(2, CartManager.totalItemsSeleccionados())
    }

    @Test
    fun testTotalDescuentaCuponYPuntos() {
        val prod = Producto(id = 1, nombre = "Laptop", marca = "HP", categoria = "Laptops", precioNormal = 2000.0, precioOferta = 2000.0)
        CartManager.items.add(CartItem(producto = prod, cantidad = 1, isSelected = true))
        CartManager.descuentoPorCupon = 200.0

        assertEquals(1800.0, CartManager.obtenerTotal(), 0.01)
        assertEquals(1700.0, CartManager.obtenerTotal(puntosAUsar = 100), 0.01)
    }

    @Test
    fun testTotalNuncaQuedaNegativo() {
        val prod = Producto(id = 1, nombre = "Mouse", marca = "Logitech", categoria = "Periféricos", precioNormal = 50.0, precioOferta = 50.0)
        CartManager.items.add(CartItem(producto = prod, cantidad = 1, isSelected = true))
        CartManager.descuentoPorCupon = 50.0

        assertEquals(0.0, CartManager.obtenerTotal(puntosAUsar = 500), 0.01)
    }

    @Test
    fun testQuitarCuponDejaElTotalEnElSubtotal() {
        val prod = Producto(id = 1, nombre = "Monitor", marca = "Samsung", categoria = "Pantallas", precioNormal = 800.0, precioOferta = 800.0)
        CartManager.items.add(CartItem(producto = prod, cantidad = 1, isSelected = true))
        CartManager.descuentoPorCupon = 120.0
        assertEquals(680.0, CartManager.obtenerTotal(), 0.01)

        CartManager.quitarCupon()

        assertEquals(0.0, CartManager.descuentoPorCupon, 0.01)
        assertNull(CartManager.cuponAplicado)
        assertEquals(800.0, CartManager.obtenerTotal(), 0.01)
    }
}
