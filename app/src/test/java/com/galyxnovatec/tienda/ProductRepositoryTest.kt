package com.galyxnovatec.tienda

import com.galyxnovatec.tienda.data.model.Producto
import com.galyxnovatec.tienda.data.repository.ProductRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ProductRepositoryTest {

    private val p1 = Producto(
        id = 1,
        nombre = "Laptop Gaming ASUS ROG",
        marca = "ASUS",
        categoria = "Laptops",
        precioNormal = 4500.0,
        precioOferta = 4000.0,
        stock = 5
    )

    private val p2 = Producto(
        id = 2,
        nombre = "Mouse Inalámbrico Logitech",
        marca = "Logitech",
        categoria = "Mouses",
        precioNormal = 150.0,
        precioOferta = 100.0,
        stock = 0
    )

    private val p3 = Producto(
        id = 3,
        nombre = "Monitor Curvo Samsung 27''",
        marca = "Samsung",
        categoria = "Monitores",
        precioNormal = 900.0,
        precioOferta = 0.0,
        stock = 10
    )

    @Before
    fun setUp() {
        ProductRepository.productos.clear()
        ProductRepository.productos.addAll(listOf(p1, p2, p3))
    }

    @Test
    fun testBuscarPorNombreMarcaOCategoria() {
        assertEquals(listOf(1), ProductRepository.buscar("laptop").map { it.id })
        assertEquals(listOf(3), ProductRepository.buscar("samsung").map { it.id })
        assertEquals(listOf(2), ProductRepository.buscar("  mouses  ").map { it.id })
        assertEquals(3, ProductRepository.buscar("").size)
        assertTrue(ProductRepository.buscar("teclado").isEmpty())
    }

    @Test
    fun testFiltroDeStock() {
        val enStock = ProductRepository.obtenerProductosFiltrados(soloEnStock = true)
        assertEquals(listOf(1, 3), enStock.map { it.id }.sorted())
    }

    @Test
    fun testOrdenPorPrecioTrataElCeroComoSinOferta() {
        // p3 no tiene oferta: su precio de venta es precioNormal (900), no 0.
        assertEquals(
            listOf(2, 3, 1),
            ProductRepository.obtenerProductosFiltrados(orden = "PRECIO_MENOR").map { it.id }
        )
        assertEquals(
            listOf(1, 3, 2),
            ProductRepository.obtenerProductosFiltrados(orden = "PRECIO_MAYOR").map { it.id }
        )
    }

    @Test
    fun testRangoDePrecioSeComparaConElPrecioDeVenta() {
        val resultado = ProductRepository.obtenerProductosFiltrados(precioMin = 200.0, precioMax = 4200.0)
        assertEquals(listOf(1, 3), resultado.map { it.id }.sorted())
    }

    @Test
    fun testCategoriaYBusquedaSeCombinan() {
        assertEquals(listOf(1), ProductRepository.obtenerProductosFiltrados(categoria = "Laptops").map { it.id })
        assertEquals(3, ProductRepository.obtenerProductosFiltrados(categoria = "TODOS").size)
        assertTrue(ProductRepository.obtenerProductosFiltrados(categoria = "Laptops", busqueda = "dell").isEmpty())
    }

    @Test
    fun testObtenerPorCategoriaEIgnoreMayusculas() {
        assertEquals(listOf(2), ProductRepository.obtenerPorCategoria("mouses").map { it.id })
        assertEquals(p2, ProductRepository.obtenerPorId(2))
        assertNull(ProductRepository.obtenerPorId(999))
    }
}
