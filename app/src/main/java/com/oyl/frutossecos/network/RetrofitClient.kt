package com.oyl.frutossecos.network

import com.oyl.frutossecos.api.AuthApi
import com.oyl.frutossecos.api.PedidoApi
import com.oyl.frutossecos.api.ProductoApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton que configura y provee la instancia única de Retrofit
 * para comunicarse con el backend Spring Boot de OYL Frutos Secos.
 *
 * Configuración de IP:
 * - Emulador Android Studio: 10.0.2.2:8080
 * - Teléfono físico en la misma red Wi-Fi: 192.168.1.94:8080 (IP local de tu PC)
 */
object RetrofitClient {

    /**
     * IP y puerto base del servidor backend.
     * Se configura por defecto con la IP de tu PC en la red local Wi-Fi.
     */
    var BASE_URL = "http://192.168.1.94:8080/"
        private set

    /** Token de sesión activo. Se establece tras un login exitoso. */
    private var tokenSesion: String? = null

    /**
     * Interceptor que agrega el encabezado Authorization a cada petición
     * si existe un token de sesión activo.
     */
    private val interceptorAutenticacion = Interceptor { cadena ->
        val constructorPeticion = cadena.request().newBuilder()
        tokenSesion?.let { token ->
            constructorPeticion.addHeader("Authorization", "Bearer $token")
        }
        cadena.proceed(constructorPeticion.build())
    }

    /**
     * Interceptor de logging para depuración en Logcat.
     */
    private val interceptorLog = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Cliente HTTP configurado con timeout ágil (4 segundos)
     * para cambiar rápidamente a persistencia local (Room) si el servidor está apagado.
     */
    private val clienteHttp: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(interceptorAutenticacion)
        .addInterceptor(interceptorLog)
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .writeTimeout(4, TimeUnit.SECONDS)
        .build()

    private fun construirRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(clienteHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /** API de productos (catálogo e inventario). */
    var productoApi: ProductoApi = construirRetrofit().create(ProductoApi::class.java)
        private set

    /** API de pedidos (transacciones y estados). */
    var pedidoApi: PedidoApi = construirRetrofit().create(PedidoApi::class.java)
        private set

    /** API de autenticación (login y registro). */
    var authApi: AuthApi = construirRetrofit().create(AuthApi::class.java)
        private set

    /**
     * Permite cambiar dinámicamente la IP del servidor si cambia de red.
     */
    fun configurarServidor(ip: String, puerto: Int = 8080) {
        val limpia = ip.trim().removePrefix("http://").removePrefix("https://").removeSuffix("/")
        BASE_URL = "http://$limpia:$puerto/"
        val nuevoRetrofit = construirRetrofit()
        productoApi = nuevoRetrofit.create(ProductoApi::class.java)
        pedidoApi = nuevoRetrofit.create(PedidoApi::class.java)
        authApi = nuevoRetrofit.create(AuthApi::class.java)
    }

    /**
     * Establece el token de sesión que se enviará en cada petición.
     */
    fun establecerToken(token: String) {
        tokenSesion = token
    }

    /**
     * Limpia el token de sesión al cerrar sesión.
     */
    fun limpiarToken() {
        tokenSesion = null
    }
}
