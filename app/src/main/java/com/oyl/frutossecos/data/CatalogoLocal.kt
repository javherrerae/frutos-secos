package com.oyl.frutossecos.data

import com.oyl.frutossecos.Producto
import com.oyl.frutossecos.ProductoDao

/** Sembrar los ejemplos solamente si no existe NI UN producto (activo o inactivo).
 * Nunca restaura productos desactivados ni sobrescribe cambios de inventario.
 * Ejecutar desde Dispatchers.IO.
 */
fun asegurarCatalogoLocal(dao: ProductoDao) {
    if (dao.contarTodos() != 0) return
    val iniciales = ProductosDemo.productos.map { demo ->
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
    // IGNORE también protege frente a inicializaciones simultáneas.
    dao.insertarSiNoExisten(iniciales)
}
