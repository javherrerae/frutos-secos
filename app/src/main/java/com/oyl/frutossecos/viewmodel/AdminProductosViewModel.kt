package com.oyl.frutossecos.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oyl.frutossecos.AppDatabase
import com.oyl.frutossecos.Producto
import com.oyl.frutossecos.dto.*
import com.oyl.frutossecos.network.RetrofitClient
import com.oyl.frutossecos.repository.mensajeError
import com.oyl.frutossecos.repository.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

interface AdministracionProductos {
    val productos: List<Producto>
    val cargando: Boolean
    val guardando: Boolean
    val mensaje: String?
    fun actualizarCatalogo()
    fun guardarProducto(id: Int?, nombre: String, categoria: String, descripcion: String,
        presentacion: String, precioTexto: String, onGuardado: () -> Unit)
    fun cambiarActivo(id: Int, activar: Boolean)
    fun registrarMovimiento(id: Int, entrada: Boolean, cantidad: Int, onGuardado: () -> Unit)
}

/** Las escrituras se confirman en el backend; Room es una caché de lectura. */
class AdminProductosViewModel(application: Application) : AndroidViewModel(application), AdministracionProductos {
    private val dao = AppDatabase.crear(application).productoDao()
    private val api get() = RetrofitClient.productoApi
    val escrituraHabilitada = true
    val esModoLocal = false
    private val _productos = mutableStateListOf<Producto>()
    override val productos: List<Producto> get() = _productos.toList()
    override var cargando by mutableStateOf(false)
        private set
    override var guardando by mutableStateOf(false)
        private set
    override var mensaje by mutableStateOf<String?>(null)
        private set

    init { actualizarCatalogo() }

    private suspend fun cargarDesdeServidor() {
        val respuesta = api.obtenerAdministracion()
        val datos = respuesta.body()
        if (!respuesta.isSuccessful || datos == null) error(respuesta.mensajeError())
        val locales = datos.map { it.toEntity() }
        withContext(Dispatchers.IO) { dao.reemplazarCatalogo(locales) }
        _productos.clear()
        _productos.addAll(locales)
    }

    override fun actualizarCatalogo() {
        if (cargando || guardando) return
        viewModelScope.launch {
            cargando = true
            try {
                cargarDesdeServidor()
                mensaje = "Catálogo actualizado desde el servidor."
            } catch (e: Exception) {
                mensaje = e.message ?: "No se pudo consultar el servidor."
            } finally { cargando = false }
        }
    }

    private fun escribir(accion: suspend () -> Response<ProductoDto>, onGuardado: () -> Unit = {}) {
        if (guardando || cargando) return
        viewModelScope.launch {
            guardando = true
            try {
                val respuesta = accion()
                if (!respuesta.isSuccessful) error(respuesta.mensajeError())
                mensaje = "Cambio confirmado por el servidor."
                onGuardado()
                cargarDesdeServidor()
            } catch (e: Exception) {
                mensaje = e.message ?: "No se pudo guardar en el servidor."
            } finally { guardando = false }
        }
    }

    override fun guardarProducto(id: Int?, nombre: String, categoria: String, descripcion: String,
            presentacion: String, precioTexto: String, onGuardado: () -> Unit) {
        val precio = precioTexto.trim().toIntOrNull()
        if (nombre.trim().length < 2 || categoria.isBlank() || descripcion.isBlank() ||
                presentacion.isBlank() || precio == null || precio !in 1..99999999) {
            mensaje = "Revisa nombre, descripción, categoría, presentación y precio."
            return
        }
        val actual = productos.find { it.id == id }
        if (id != null && actual?.activo != 1) {
            mensaje = "Activa el producto antes de editarlo."
            return
        }
        val solicitud = ProductoRequest(nombre.trim(), categoria.trim(), descripcion.trim(),
            precio.toDouble(), presentacion.trim(), actual?.stock ?: 0, actual?.rutaImagen ?: "🌿")
        escribir({ if (id == null) api.crear(solicitud) else api.editar(id, solicitud) }, onGuardado)
    }

    override fun cambiarActivo(id: Int, activar: Boolean) {
        escribir({ api.cambiarActivo(id, EstadoProductoRequest(activar)) })
    }

    override fun registrarMovimiento(id: Int, entrada: Boolean, cantidad: Int, onGuardado: () -> Unit) {
        val actual = productos.find { it.id == id } ?: return
        val nuevoStock = actual.stock.toLong() + if (entrada) cantidad.toLong() else -cantidad.toLong()
        if (cantidad !in 1..100000 || actual.activo != 1 || nuevoStock !in 0..Int.MAX_VALUE.toLong()) {
            mensaje = "Verifica la cantidad y el stock disponible."
            return
        }
        escribir({ api.registrarMovimiento(id, MovimientoStockRequest(entrada, cantidad)) }, onGuardado)
    }
}
