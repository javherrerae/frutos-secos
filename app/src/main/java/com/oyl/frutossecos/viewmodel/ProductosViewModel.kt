package com.oyl.frutossecos.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oyl.frutossecos.AppDatabase
import com.oyl.frutossecos.Producto
import com.oyl.frutossecos.data.ProductosDemo
import com.oyl.frutossecos.model.Producto as UiProducto
import com.oyl.frutossecos.repository.ProductoRepository
import com.oyl.frutossecos.repository.ResultadoSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel que gestiona el catálogo de productos de OYL Frutos Secos.
 * Conecta la pantalla del catálogo con el servidor backend mediante Retrofit
 * y utiliza Room (SQLite) como respaldo local cuando no hay conexión.
 */
class ProductosViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.crear(application)
    private val productoDao = db.productoDao()
    private val repository = ProductoRepository(productoDao)

    private val _productos = mutableStateListOf<UiProducto>()
    val productos: List<UiProducto> get() = _productos.toList()

    var cargando by mutableStateOf(false)
        private set

    var mensajeEstado by mutableStateOf<String?>(null)
        private set

    var esModoOffline by mutableStateOf(false)
        private set

    init {
        cargarCatalogo()
    }

    /**
     * Consulta el catálogo en el servidor Spring Boot y actualiza la base local.
     * Si el servidor no responde, utiliza los datos de Room o los de demostración.
     */
    fun cargarCatalogo() {
        cargando = true
        mensajeEstado = null

        viewModelScope.launch(Dispatchers.IO) {
            val resultado = repository.sincronizarCatalogo()

            withContext(Dispatchers.Main) {
                cargando = false
                when (resultado) {
                    is ResultadoSync.Exito -> {
                        esModoOffline = false
                        mensajeEstado = "Catálogo sincronizado con el servidor"
                        _productos.clear()
                        _productos.addAll(resultado.datos.map { it.toUiModel() })
                    }
                    is ResultadoSync.DesdeCacheConError -> {
                        esModoOffline = true
                        mensajeEstado = "Modo local: datos cargados de caché"
                        val items = if (resultado.datos.isNotEmpty()) {
                            resultado.datos.map { it.toUiModel() }
                        } else {
                            sembrarDatosDemo()
                        }
                        _productos.clear()
                        _productos.addAll(items)
                    }
                    is ResultadoSync.Error -> {
                        esModoOffline = true
                        mensajeEstado = "Sin conexión: mostrando productos disponibles"
                        val items = sembrarDatosDemo()
                        _productos.clear()
                        _productos.addAll(items)
                    }
                }
            }
        }
    }

    /**
     * Inicializa los productos en Room si la app se ejecuta por primera vez sin backend.
     */
    private suspend fun sembrarDatosDemo(): List<UiProducto> = withContext(Dispatchers.IO) {
        val entidades = ProductosDemo.productos.map { demo ->
            Producto(
                id = demo.id,
                nombre = demo.nombre,
                categoria = demo.categoria,
                descripcion = demo.descripcion,
                precio = demo.precio.toDouble(),
                presentacion = demo.peso,
                stock = demo.stock,
                rutaImagen = demo.emoji,
                activo = 1
            )
        }
        productoDao.insertarSiNoExisten(entidades)
        ProductosDemo.productos
    }

    /**
     * Busca un producto por ID dentro de la lista actual.
     */
    fun buscarPorId(id: Int): UiProducto? {
        return _productos.find { it.id == id }
    }

    /**
     * Rebaja el stock local de los productos del pedido confirmado
     * y actualiza la lista visual del catálogo inmediatamente.
     */
    fun rebajarStockLocal(items: List<Pair<Int, Int>>) {
        viewModelScope.launch(Dispatchers.IO) {
            items.forEach { (idProducto, cantidad) ->
                productoDao.rebajarStock(idProducto, cantidad)
            }
            // Releer los productos actualizados de Room
            val actualizados = productoDao.obtenerCatalogo().map { it.toUiModel() }
            withContext(Dispatchers.Main) {
                _productos.clear()
                _productos.addAll(actualizados)
            }
        }
    }
}

/**
 * Función de extensión que convierte la entidad Room Producto en el modelo visual UiProducto.
 */
fun Producto.toUiModel(): UiProducto {
    val emoji = when {
        nombre.contains("almendra", ignoreCase = true) -> "🥜"
        nombre.contains("nuez", ignoreCase = true) || nombre.contains("nueces", ignoreCase = true) -> "🌰"
        nombre.contains("zapallo", ignoreCase = true) -> "🌱"
        nombre.contains("semilla", ignoreCase = true) -> "🌻"
        nombre.contains("tropical", ignoreCase = true) -> "🥭"
        nombre.contains("mani", ignoreCase = true) || nombre.contains("maní", ignoreCase = true) -> "🥜"
        categoria.contains("deshidratad", ignoreCase = true) -> "🍇"
        categoria.contains("semilla", ignoreCase = true) -> "🌱"
        else -> "🌿"
    }
    return UiProducto(
        id = id,
        nombre = nombre,
        categoria = categoria,
        descripcion = descripcion,
        peso = presentacion,
        precio = precio.toInt(),
        stock = stock,
        emoji = emoji
    )
}