package com.oyl.frutossecos

import com.oyl.frutossecos.data.ProductosDemo
import com.oyl.frutossecos.data.asegurarCatalogoLocal
import com.oyl.frutossecos.viewmodel.toUiModel
import java.lang.reflect.Proxy
import org.junit.Assert.*
import org.junit.Test

class CatalogoLocalTest {
    @Test fun inicializaSoloUnaBaseVaciaYConservaDatos() {
        var cantidad = 0
        var inserciones = 0
        var guardados = emptyList<Producto>()
        val dao = Proxy.newProxyInstance(ProductoDao::class.java.classLoader, arrayOf(ProductoDao::class.java)) { _, metodo, args ->
            when (metodo.name) {
                "contarTodos" -> cantidad
                "insertarSiNoExisten" -> {
                    @Suppress("UNCHECKED_CAST")
                    guardados = args[0] as List<Producto>
                    inserciones++
                    null
                }
                else -> error("DAO inesperado: ${metodo.name}")
            }
        } as ProductoDao
        asegurarCatalogoLocal(dao)
        assertEquals(ProductosDemo.productos.size, guardados.size)
        assertEquals(ProductosDemo.productos.first().precio.toDouble(), guardados.first().precio, 0.0)
        assertTrue(guardados.all { it.activo == 1 })
        cantidad = 1
        asegurarCatalogoLocal(dao)
        assertEquals(1, inserciones)
    }

    @Test fun presentacionUsaEmojiPorNombreOCategoria() {
        val producto = Producto(1, "Almendras", "Frutos", "", 1500.9, "500 g", 2, "", 1)
        assertEquals("🥜", producto.toUiModel().emoji)
        assertEquals(1500, producto.toUiModel().precio)
        assertEquals("🌰", producto.copy(nombre = "Nueces").toUiModel().emoji)
        assertEquals("🌱", producto.copy(nombre = "Zapallo", categoria = "Semillas").toUiModel().emoji)
        assertEquals("🍇", producto.copy(nombre = "Pasas", categoria = "Deshidratados").toUiModel().emoji)
        assertEquals("🌿", producto.copy(nombre = "Otro", categoria = "Otro").toUiModel().emoji)
    }
}
