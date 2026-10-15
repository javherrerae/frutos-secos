package com.oyl.frutossecos

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Punto de entrada de Room para la base SQLite `frutossecos.db`.
 * Room crea el archivo y las tablas si no existen, y valida su estructura al abrir.
 * Registra las ocho entidades de persistencia local de la aplicación:
 * usuarios, sesiones, productos (caché), carrito, pedidos, detalle de pedidos,
 * notificaciones y puntos de retiro.
 */
@Database(
    entities = [
        Usuario::class,
        Sesion::class,
        Producto::class,
        CarritoItem::class,
        Pedido::class,
        PedidoItem::class,
        Notificacion::class,
        PuntoRetiro::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /** Proporciona las operaciones permitidas sobre la tabla `usuarios`. */
    abstract fun usuarioDao(): UsuarioDao

    /** Proporciona las operaciones permitidas sobre la tabla `sesiones`. */
    abstract fun sesionDao(): SesionDao

    /** Proporciona las operaciones permitidas sobre la tabla `productos`. */
    abstract fun productoDao(): ProductoDao

    /** Proporciona las operaciones permitidas sobre la tabla `carrito_items`. */
    abstract fun carritoItemDao(): CarritoItemDao

    /** Proporciona las operaciones permitidas sobre la tabla `pedidos`. */
    abstract fun pedidoDao(): PedidoDao

    /** Proporciona las operaciones permitidas sobre la tabla `pedido_items`. */
    abstract fun pedidoItemDao(): PedidoItemDao

    /** Proporciona las operaciones permitidas sobre la tabla `notificaciones`. */
    abstract fun notificacionDao(): NotificacionDao

    /** Proporciona las operaciones permitidas sobre la tabla `puntos_retiro`. */
    abstract fun puntoRetiroDao(): PuntoRetiroDao

    companion object {
        const val NOMBRE_BD = "frutossecos.db"

        /**
         * Construye una conexión usando el contexto global de la aplicación
         * para no conservar accidentalmente una referencia a la pantalla.
         */
        fun crear(context: Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                NOMBRE_BD
            ).build()
    }
}
