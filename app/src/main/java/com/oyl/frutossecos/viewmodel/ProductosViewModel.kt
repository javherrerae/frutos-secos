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
import com.oyl.frutossecos.data.asegurarCatalogoLocal
import com.oyl.frutossecos.model.Producto as UiProducto
import com.oyl.frutossecos.repository.ProductoRepository
import com.oyl.frutossecos.repository.ResultadoSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Catálogo del cliente. Para la entrega Room es la fuente de datos por defecto.
 * El método sincronizarConServidor conserva la integración Retrofit para otra etapa.
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

    init { cargarCatalogo() }

    /** Lectura de Room: respeta altas, bajas lógicas y cambios de stock de admin. */
    fun cargarCatalogo() {
        viewModelScope.launch {
            cargando = true
            try {
                val listado = withContext(Dispatchers.IO) {
                    asegurarCatalogoLocal(productoDao)
                    productoDao.obtenerCatalogo().map { it.toUiModel() }
                }
                _productos.clear()
                _productos.addAll(listado)
                esModoOffline = true
                mensajeEstado = "Catálogo local de este dispositivo"
            } catch (e: Exception) {
                mensajeEstado = "No se pudo cargar el catálogo local: ${e.localizedMessage}"
            } finally {
                cargando = false
            }
        }
    }

    /** Preparado para una futura entrega con backend; NO llamado automáticamente ahora.
     * Puede sobrescribir productos locales si se activa sin diseñar sincronización.
     */
    fun sincronizarConServidor() {
        viewModelScope.launch {
            cargando = true
            try {
                val resultado = withContext(Dispatchers.IO) {
                    repository.sincronizarCatalogo()
                }
                when (resultado) {
                    is ResultadoSync.Exito -> {
                        _productos.clear()
                        _productos.addAll(resultado.datos.map { it.toUiModel() })
                        esModoOffline = false
                        mensajeEstado = "Catálogo actualizado desde el servidor"
                    }
                    is ResultadoSync.DesdeCacheConError -> {
                        cargarCatalogo()
                    }
                    is ResultadoSync.Error -> {
                        mensajeEstado = resultado.mensajeError
                    }
                }
            } catch (e: Exception) {
                mensajeEstado = "Error de sincronización: ${e.localizedMessage}"
            } finally {
                cargando = false
            }
        }
    }

    fun buscarPorId(id: Int): UiProducto? = _productos.find { it.id == id }

    /** Conserva la operación original usada cuando se confirman pedidos. */
    fun rebajarStockLocal(items: List<Pair<Int, Int>>) {
        viewModelScope.launch(Dispatchers.IO) {
            items.forEach { (idProducto, cantidad) ->
                productoDao.rebajarStock(idProducto, cantidad)
            }
            val actualizados = productoDao.obtenerCatalogo().map { it.toUiModel() }
            withContext(Dispatchers.Main) {
                _productos.clear()
                _productos.addAll(actualizados)
            }
        }
    }
}

/** Conversión del modelo Room al formato visual de la tienda. */
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
