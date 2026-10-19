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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Administración para entrega con Room/SQLite. No hace peticiones de escritura por Retrofit. */
class AdminProductosViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.crear(application).productoDao()

    /** Habilita solo escritura en SQLite local, nunca en el servidor. */
    val escrituraHabilitada = true
    val esModoLocal = true

    private val _productos = mutableStateListOf<Producto>()
    val productos: List<Producto> get() = _productos.toList()

    var cargando by mutableStateOf(false)
        private set
    var guardando by mutableStateOf(false)
        private set
    var mensaje by mutableStateOf<String?>(null)
        private set

    init { actualizarCatalogo() }

    private fun mostrarDatos(datos: List<Producto>) {
        _productos.clear()
        _productos.addAll(datos)
    }

    fun actualizarCatalogo() {
        if (cargando || guardando) return
        viewModelScope.launch {
            cargando = true
            try {
                val datos = withContext(Dispatchers.IO) {
                    asegurarCatalogoLocal(dao)
                    dao.obtenerTodos()
                }
                mostrarDatos(datos)
                mensaje = "Catálogo guardado en este dispositivo (Room/SQLite)."
            } catch (e: Exception) {
                mensaje = "No se pudo leer el catálogo local: ${e.localizedMessage}"
            } finally {
                cargando = false
            }
        }
    }

    fun guardarProducto(
        id: Int?,
        nombre: String,
        categoria: String,
        descripcion: String,
        presentacion: String,
        precioTexto: String,
        onGuardado: () -> Unit
    ) {
        if (guardando || cargando) return
        val nombreLimpio = nombre.trim()
        val categoriaLimpia = categoria.trim()
        val presentacionLimpia = presentacion.trim()
        val precio = precioTexto.trim().toIntOrNull()
        if (nombreLimpio.length < 2 || categoriaLimpia.isEmpty() ||
            presentacionLimpia.isEmpty() || precio == null || precio !in 1..99999999
        ) {
            mensaje = "Revisa nombre, categoría, presentación y precio (CLP entero mayor a cero)."
            return
        }
        viewModelScope.launch {
            guardando = true
            try {
                val resultado = withContext(Dispatchers.IO) {
                    val guardado = if (id == null) {
                        // La PK existente no tiene autogeneración: asignamos siguiente ID libre.
                        val nuevo = Producto(
                            id = dao.siguienteId(),
                            nombre = nombreLimpio,
                            categoria = categoriaLimpia,
                            descripcion = descripcion.trim(),
                            precio = precio.toDouble(),
                            presentacion = presentacionLimpia,
                            stock = 0,
                            rutaImagen = "🌿",
                            activo = 1
                        )
                        dao.insertarProducto(nuevo) > 0L
                    } else {
                        dao.editarDatos(
                            id, nombreLimpio, categoriaLimpia, descripcion.trim(),
                            precio.toDouble(), presentacionLimpia
                        ) == 1
                    }
                    guardado to dao.obtenerTodos()
                }
                mostrarDatos(resultado.second)
                mensaje = if (resultado.first) {
                    if (id == null) "Producto creado en SQLite; stock inicial: 0."
                    else "Producto actualizado en SQLite."
                } else "No se pudo encontrar el producto para guardar."
                if (resultado.first) onGuardado()
            } catch (e: Exception) {
                mensaje = "No se guardó el producto: ${e.localizedMessage}"
            } finally {
                guardando = false
            }
        }
    }

    fun cambiarActivo(id: Int, activar: Boolean) {
        if (guardando || cargando) return
        viewModelScope.launch {
            guardando = true
            try {
                val resultado = withContext(Dispatchers.IO) {
                    dao.cambiarActivo(id, if (activar) 1 else 0) to dao.obtenerTodos()
                }
                mostrarDatos(resultado.second)
                mensaje = if (resultado.first == 1) {
                    if (activar) "Producto activado en SQLite." else "Producto desactivado en SQLite."
                } else "El producto ya no existe."
            } catch (e: Exception) {
                mensaje = "No se cambió el estado: ${e.localizedMessage}"
            } finally {
                guardando = false
            }
        }
    }

    /** Entrada o salida atómica de existencias, solo en la SQLite de este dispositivo. */
    fun registrarMovimiento(id: Int, entrada: Boolean, cantidad: Int, onGuardado: () -> Unit) {
        if (guardando || cargando) return
        if (cantidad !in 1..100000) {
            mensaje = "La cantidad debe ser entre 1 y 100.000 unidades."
            return
        }
        viewModelScope.launch {
            guardando = true
            try {
                val resultado = withContext(Dispatchers.IO) {
                    val modificados = if (entrada) dao.registrarEntrada(id, cantidad)
                    else dao.registrarSalida(id, cantidad)
                    modificados to dao.obtenerTodos()
                }
                mostrarDatos(resultado.second)
                if (resultado.first == 1) {
                    mensaje = "${if (entrada) "Entrada" else "Salida"} de $cantidad unidades guardada en SQLite."
                    onGuardado()
                } else {
                    mensaje = "No se pudo ajustar: verifica stock suficiente y producto activo."
                }
            } catch (e: Exception) {
                mensaje = "No se guardó el ajuste: ${e.localizedMessage}"
            } finally {
                guardando = false
            }
        }
    }
}
