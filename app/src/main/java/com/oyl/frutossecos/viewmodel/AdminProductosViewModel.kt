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
import com.oyl.frutossecos.repository.ProductoRepository
import com.oyl.frutossecos.repository.ResultadoSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Estado administrativo independiente del catálogo de cliente.
 * Reutiliza el repositorio y los endpoints ya existentes, sin alterar Room ni Retrofit.
 * Solo modifica el stock si el backend acepta la operación; nunca simula un guardado.
 */
class AdminProductosViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.crear(application)
    private val repositorio = ProductoRepository(db.productoDao())

    // Cambiar a true SOLO tras validar permisos y PUT /api/productos/{id}/stock
    // con el responsable del backend y datos de prueba.
    val escrituraHabilitada: Boolean = false

    private val _productos = mutableStateListOf<Producto>()
    val productos: List<Producto> get() = _productos.toList()

    var cargando by mutableStateOf(false)
        private set
    var guardando by mutableStateOf(false)
        private set
    var esModoLocal by mutableStateOf(true)
        private set
    var mensaje by mutableStateOf<String?>(null)
        private set

    init { actualizarCatalogo() }

    fun actualizarCatalogo() {
        if (cargando || guardando) return
        viewModelScope.launch {
            cargando = true
            val resultado = withContext(Dispatchers.IO) {
                repositorio.sincronizarCatalogo()
            }
            when (resultado) {
                is ResultadoSync.Exito -> {
                    _productos.clear()
                    _productos.addAll(resultado.datos)
                    esModoLocal = false
                    mensaje = "Catálogo actualizado desde el servidor."
                }
                is ResultadoSync.DesdeCacheConError -> {
                    _productos.clear()
                    _productos.addAll(resultado.datos)
                    esModoLocal = true
                    mensaje = "Sin conexión: mostrando catálogo local. Los cambios están deshabilitados."
                }
                is ResultadoSync.Error -> {
                    esModoLocal = true
                    mensaje = resultado.mensajeError
                }
            }
            cargando = false
        }
    }

    /** El servidor es la fuente oficial. No se hacen cambios de stock en modo local. */
    fun guardarStock(idProducto: Int, nuevoStock: Int) {
        if (!escrituraHabilitada) {
            mensaje = "Protección activa: edición de stock no habilitada."
            return
        }
        if (guardando || cargando) return
        if (esModoLocal) {
            mensaje = "Conecta con el servidor para modificar existencias."
            return
        }
        val anterior = _productos.firstOrNull { it.id == idProducto }
        if (anterior == null || nuevoStock < 0 || nuevoStock == anterior.stock) {
            mensaje = "Selecciona un producto y una cantidad válida distinta del stock actual."
            return
        }
        viewModelScope.launch {
            guardando = true
            mensaje = "Enviando actualización al servidor…"
            val resultado = withContext(Dispatchers.IO) {
                repositorio.actualizarStock(idProducto, nuevoStock)
            }
            when (resultado) {
                is ResultadoSync.Exito -> {
                    val indice = _productos.indexOfFirst { it.id == idProducto }
                    if (indice >= 0) _productos[indice] = resultado.datos
                    mensaje = "Existencias actualizadas por el servidor."
                }
                is ResultadoSync.DesdeCacheConError -> {
                    mensaje = "No se confirmó la actualización en el servidor."
                }
                is ResultadoSync.Error -> {
                    mensaje = "No se guardaron cambios: ${resultado.mensajeError}"
                }
            }
            guardando = false
        }
    }
}
