package com.oyl.frutossecos

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Pantalla principal del proyecto con Room para OYL Frutos Secos.
 *
 * La actividad coordina la interfaz y permite verificar el funcionamiento
 * de la base de datos local SQLite (`frutossecos.db`).
 */
class MainActivity : Activity() {

    private lateinit var baseDatos: AppDatabase
    private lateinit var btnRecrearBaseDatos: Button
    private lateinit var btnVerProductos: Button
    private lateinit var txtResultado: TextView

    // Room no permite acceder al disco desde el hilo de la interfaz. Un solo
    // hilo secundario mantiene las operaciones en orden y evita bloqueos.
    private val ejecutorBaseDatos: ExecutorService = Executors.newSingleThreadExecutor()

    /**
     * Inicializa la pantalla, comprueba si el archivo existía, abre Room e
     * inserta únicamente los registros iniciales que falten.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        aplicarMargenesDelSistema(findViewById(R.id.mainContainer))
        enlazarControles()
        habilitarBotones(false)

        // databaseList contiene las bases privadas creadas previamente.
        val baseExistia = databaseList().contains(AppDatabase.NOMBRE_BD)

        ejecutorBaseDatos.execute {
            try {
                baseDatos = AppDatabase.crear(this)
                poblarDatosIniciales()

                val mensaje = if (baseExistia) {
                    "La base de datos 'frutossecos.db' ya existía y Room validó su estructura."
                } else {
                    "Room creó la base 'frutossecos.db', sus 8 tablas y los datos iniciales de prueba."
                }
                mostrarMensaje(mensaje)
            } catch (error: Exception) {
                mostrarError("No fue posible preparar la base de datos", error)
            }
        }
    }

    /** Obtiene las vistas del diseño y registra las acciones de los botones. */
    private fun enlazarControles() {
        txtResultado = findViewById(R.id.txtResultado)
        btnRecrearBaseDatos = findViewById(R.id.btnRecrearBaseDatos)
        btnVerProductos = findViewById(R.id.btnVerProductos)

        btnRecrearBaseDatos.setOnClickListener { eliminarYCrearBaseDatos() }
        btnVerProductos.setOnClickListener { mostrarProductosYUsuarios() }
    }

    /**
     * Inserta los datos iniciales de prueba según la pauta de OYL Frutos Secos:
     * - Cuenta Administrador demo preconfigurada.
     * - Cliente de prueba.
     * - Productos del catálogo (frutos secos, semillas y deshidratados).
     * - Punto de retiro tienda física.
     */
    private fun poblarDatosIniciales() {
        // Usuarios iniciales
        val usuariosIniciales = listOf(
            Usuario(
                id = 1,
                nombre = "Administrador",
                alias = "admin",
                contrasena = "admin123",
                correo = "admin@admin.cl",
                telefono = "+56 9 1234 1234",
                direccion = "Santiago",
                rol = "administrador",
                activo = 1
            ),
            Usuario(
                id = 2,
                nombre = "Juan Pérez",
                alias = "jperez",
                contrasena = "cliente123",
                correo = "juan.perez@gmail.com",
                telefono = "+56 9 9876 5432",
                direccion = "Av. Providencia 1234, Santiago",
                rol = "cliente",
                activo = 1
            )
        )
        baseDatos.usuarioDao().insertarSiNoExisten(usuariosIniciales)

        // Catálogo sintético de prueba
        val productosIniciales = listOf(
            Producto(
                id = 1,
                nombre = "Nueces Mariposa",
                categoria = "Frutos secos",
                descripcion = "Nueces peladas seleccionadas calibre extra.",
                precio = 8500.0,
                presentacion = "Bolsa 500g",
                stock = 25,
                rutaImagen = "nueces.png",
                activo = 1
            ),
            Producto(
                id = 2,
                nombre = "Almendras Nonpareil",
                categoria = "Frutos secos",
                descripcion = "Almendras naturales sin sal añadidas.",
                precio = 7990.0,
                presentacion = "Bolsa 500g",
                stock = 18,
                rutaImagen = "almendras.png",
                activo = 1
            ),
            Producto(
                id = 3,
                nombre = "Semillas de Chía",
                categoria = "Semillas",
                descripcion = "Semillas de chía orgánica ricas en Omega 3.",
                precio = 3500.0,
                presentacion = "Bolsa 250g",
                stock = 40,
                rutaImagen = "chia.png",
                activo = 1
            ),
            Producto(
                id = 4,
                nombre = "Cranberries Deshidratados",
                categoria = "Deshidratados",
                descripcion = "Arándanos rojos deshidratados ligeramente endulzados.",
                precio = 4800.0,
                presentacion = "Bolsa 300g",
                stock = 12,
                rutaImagen = "cranberries.png",
                activo = 1
            )
        )
        baseDatos.productoDao().insertarSiNoExisten(productosIniciales)

        // Punto de retiro tienda física
        val puntosIniciales = listOf(
            PuntoRetiro(
                id = 1,
                nombre = "Tienda Central OYL Frutos Secos",
                direccion = "Av. Apoquindo 4500, Las Condes, Santiago",
                latitud = -33.4150,
                longitud = -70.5820,
                horario = "Lunes a Viernes 09:00 - 19:00"
            )
        )
        baseDatos.puntoRetiroDao().insertarSiNoExisten(puntosIniciales)
    }

    /**
     * Cierra Room, elimina el archivo SQLite y crea una base nueva.
     * Room reconstruye las 8 tablas y se vuelven a poblar los datos iniciales.
     */
    private fun eliminarYCrearBaseDatos() {
        habilitarBotones(false)
        txtResultado.text = "Recreando la base de datos 'frutossecos.db'..."

        ejecutorBaseDatos.execute {
            try {
                baseDatos.close()
                val eliminada = deleteDatabase(AppDatabase.NOMBRE_BD)
                baseDatos = AppDatabase.crear(this)
                poblarDatosIniciales()

                val mensaje = if (eliminada) {
                    "Base 'frutossecos.db' eliminada y recreada correctamente con Room."
                } else {
                    "La base no existía; Room la creó y pobló correctamente."
                }
                mostrarMensaje(mensaje, mostrarToast = true)
            } catch (error: Exception) {
                mostrarError("No fue posible recrear la base de datos", error)
            }
        }
    }

    /** Consulta productos y usuarios registrados y los muestra en pantalla. */
    private fun mostrarProductosYUsuarios() {
        habilitarBotones(false)
        txtResultado.text = "Consultando catálogo y usuarios locales..."

        ejecutorBaseDatos.execute {
            try {
                val usuarios = baseDatos.usuarioDao().obtenerTodos()
                val productos = baseDatos.productoDao().obtenerCatalogo()

                val builder = StringBuilder()
                builder.append("=== USUARIOS REGISTRADOS (${usuarios.size}) ===\n")
                if (usuarios.isEmpty()) {
                    builder.append("No hay usuarios.\n")
                } else {
                    usuarios.forEach { u ->
                        builder.append("• [${u.rol.uppercase()}] ID: ${u.id} | ${u.nombre} (@${u.alias}) | ${u.correo}\n")
                    }
                }

                builder.append("\n=== CATÁLOGO EN CACHÉ (${productos.size}) ===\n")
                if (productos.isEmpty()) {
                    builder.append("No hay productos.\n")
                } else {
                    productos.forEach { p ->
                        builder.append("• ID: ${p.id} | ${p.nombre} (${p.categoria})\n")
                        builder.append("  Precio: \$${p.precio} | Stock: ${p.stock} | Presentación: ${p.presentacion}\n\n")
                    }
                }

                mostrarMensaje(builder.toString().trimEnd())
            } catch (error: Exception) {
                mostrarError("No fue posible consultar los datos", error)
            }
        }
    }

    /** Actualiza la interfaz desde el hilo principal al terminar una tarea. */
    private fun mostrarMensaje(mensaje: String, mostrarToast: Boolean = false) {
        runOnUiThread {
            txtResultado.text = mensaje
            habilitarBotones(true)
            if (mostrarToast) Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        }
    }

    /** Presenta un error comprensible y vuelve a habilitar la pantalla. */
    private fun mostrarError(descripcion: String, error: Exception) {
        val detalle = error.localizedMessage ?: "error desconocido"
        val mensaje = "$descripcion: $detalle."
        runOnUiThread {
            txtResultado.text = mensaje
            habilitarBotones(true)
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
        }
    }

    /** Evita que el usuario inicie dos operaciones simultáneas. */
    private fun habilitarBotones(habilitados: Boolean) {
        btnRecrearBaseDatos.isEnabled = habilitados
        btnVerProductos.isEnabled = habilitados
    }

    /**
     * Mantiene el contenido fuera de las barras del sistema en Oreo y en el
     * modo de presentación de borde a borde de Android reciente.
     */
    @Suppress("DEPRECATION")
    private fun aplicarMargenesDelSistema(contenedor: View) {
        val izquierda = contenedor.paddingLeft
        val arriba = contenedor.paddingTop
        val derecha = contenedor.paddingRight
        val abajo = contenedor.paddingBottom

        contenedor.setOnApplyWindowInsetsListener { vista, insets ->
            vista.setPadding(
                izquierda + insets.systemWindowInsetLeft,
                arriba + insets.systemWindowInsetTop,
                derecha + insets.systemWindowInsetRight,
                abajo + insets.systemWindowInsetBottom
            )
            insets
        }
        contenedor.requestApplyInsets()
    }

    /** Cierra Room y el hilo auxiliar cuando Android destruye la actividad. */
    override fun onDestroy() {
        ejecutorBaseDatos.execute {
            if (::baseDatos.isInitialized) baseDatos.close()
        }
        ejecutorBaseDatos.shutdown()
        super.onDestroy()
    }
}
