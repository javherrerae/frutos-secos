package com.oyl.frutossecos.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oyl.frutossecos.AppDatabase
import com.oyl.frutossecos.Producto
import com.oyl.frutossecos.model.Producto as UiProducto
import com.oyl.frutossecos.repository.ProductoRepository
import com.oyl.frutossecos.repository.ResultadoSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductosViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProductoRepository(AppDatabase.crear(application).productoDao())
    private val _productos = mutableStateListOf<UiProducto>()
    val productos: List<UiProducto> get() = _productos.toList()
    var cargando by mutableStateOf(false)
        private set
    var mensajeEstado by mutableStateOf<String?>(null)
        private set
    var esModoOffline by mutableStateOf(false)
        private set

    init { cargarCatalogo() }

    fun cargarCatalogo() {
        if (cargando) return
        viewModelScope.launch {
            cargando = true
            try {
                when (val resultado = withContext(Dispatchers.IO) { repository.sincronizarCatalogo() }) {
                    is ResultadoSync.Exito -> {
                        _productos.clear()
                        _productos.addAll(resultado.datos.map { it.toUiModel() })
                        esModoOffline = false
                        mensajeEstado = "Catálogo actualizado desde el servidor"
                    }
                    is ResultadoSync.DesdeCacheConError -> {
                        _productos.clear()
                        _productos.addAll(resultado.datos.map { it.toUiModel() })
                        esModoOffline = true
                        mensajeEstado = "Sin conexión: mostrando la última copia del catálogo"
                    }
                    is ResultadoSync.Error -> mensajeEstado = resultado.mensajeError
                }
            } finally { cargando = false }
        }
    }

    fun sincronizarConServidor() = cargarCatalogo()
    fun buscarPorId(id: Int): UiProducto? = _productos.find { it.id == id }
}

fun Producto.toUiModel(): UiProducto {
    val emoji = when {
        nombre.contains("almendra", true) || nombre.contains("maní", true) -> "🥜"
        nombre.contains("nuez", true) || nombre.contains("nueces", true) -> "🌰"
        categoria.contains("semilla", true) -> "🌱"
        categoria.contains("deshidratad", true) -> "🍇"
        else -> "🌿"
    }
    return UiProducto(id, nombre, categoria, descripcion, presentacion, precio.toInt(), stock, emoji)
}
