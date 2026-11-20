# OYL Frutos Secos — Android conectado al backend

Aplicación Android con Kotlin, Jetpack Compose, Retrofit y Room.

Esta copia integra el backend Spring Boot del proyecto: registro/login, catálogo, administración, inventario, perfil y pedidos. Room conserva el carrito y la caché; las escrituras se confirman en el servidor.

## Ejecutar

Abrir esta carpeta en Android Studio, sincronizar Gradle y ejecutar en un emulador con el backend iniciado.

La URL predeterminada es `http://10.0.2.2:8080/`. Para un teléfono físico, configurar en `gradle.properties`:

```properties
oylBackendUrl=http://IP_DEL_PC:8080/
```

Usar la IP actual del PC y la misma red Wi-Fi. Se necesita Android SDK 35.

```powershell
.\gradlew.bat :app:assembleDebug
```

La primera vez, borrar los datos anteriores de la app para evitar mezclar las cuentas e IDs de la demostración local con los del servidor.

El backend crea la cuenta `admin / admin123` únicamente al arrancar usuarios con el perfil `demo`. Los clientes se registran desde la app.

La guía completa está en `docs/INTEGRACION.md` del backend. Los estados de pedido se administran mediante la API; no se añadió una pantalla nueva para esa tarea.
