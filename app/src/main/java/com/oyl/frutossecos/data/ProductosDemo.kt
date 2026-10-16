
package com.oyl.frutossecos.data

import com.oyl.frutossecos.model.Producto

object ProductosDemo {

    val productos = listOf(
        Producto(
            id = 1,
            nombre = "Almendras naturales",
            categoria = "Frutos secos",
            descripcion = "Crujientes, sin sal añadida y llenas de sabor.",
            peso = "Bolsa 250 g",
            precio = 3990,
            stock = 24,
            emoji = "🥜"
        ),
        Producto(
            id = 2,
            nombre = "Nueces mariposa",
            categoria = "Frutos secos",
            descripcion = "Nueces seleccionadas, ideales para tus recetas.",
            peso = "Bolsa 250 g",
            precio = 4490,
            stock = 18,
            emoji = "🌰"
        ),
        Producto(
            id = 3,
            nombre = "Semillas de zapallo",
            categoria = "Semillas",
            descripcion = "Semillas naturales para complementar tus comidas.",
            peso = "Bolsa 200 g",
            precio = 2990,
            stock = 30,
            emoji = "🌱"
        ),
        Producto(
            id = 4,
            nombre = "Mix tropical",
            categoria = "Frutas deshidratadas",
            descripcion = "Una mezcla de frutas y frutos secos.",
            peso = "Bolsa 250 g",
            precio = 3490,
            stock = 15,
            emoji = "🥭"
        ),
        Producto(
            id = 5,
            nombre = "Maní tostado",
            categoria = "Frutos secos",
            descripcion = "Maní tostado para disfrutar en cualquier momento.",
            peso = "Bolsa 250 g",
            precio = 2490,
            stock = 40,
            emoji = "🥜"
        ),
        Producto(
            id = 6,
            nombre = "Mix de semillas",
            categoria = "Semillas",
            descripcion = "Selección de semillas para una alimentación variada.",
            peso = "Bolsa 200 g",
            precio = 2790,
            stock = 22,
            emoji = "🌻"
        )
    )
}
