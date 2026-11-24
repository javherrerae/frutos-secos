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

El backend crea la cuenta `admin` únicamente con el perfil `demo`; su contraseña inicial se configura mediante `OYL_ADMIN_PASSWORD` en el `.env` local del backend. Los clientes se registran desde la app.

La guía completa está en `docs/INTEGRACION.md` del backend. Los estados de pedido se administran mediante la API; no se añadió una pantalla nueva para esa tarea.

El backend se abre desde su repositorio independiente `frutos-secos-backend`; consulta allí `docs/INTEGRACION.md`. No vuelvas a aplicar `frontend.patch`: la integración ya está incorporada.

Para README.docx el equipo debe aportar sus nombres y datos reales, enlaces definitivos y capturas propias del emulador, del pedido y de PostgreSQL antes y después del reinicio. No incluir contraseñas ni tokens.
# Pruebas unitarias y cobertura

En PowerShell, con JDK 21 y el SDK de Android configurados:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:koverHtmlReport :app:koverXmlReport
.\gradlew.bat :app:koverVerify
.\gradlew.bat :app:assembleDebug
```

El informe de pruebas debug se genera en `app/build/reports/tests/testDebugUnitTest/index.html` y el de cobertura del módulo en `app/build/reports/kover/html/index.html`. Kover agrega las variantes debug y release y verifica un mínimo del 80 % de líneas. No se han configurado exclusiones de clases, incluidas las generadas. Las pruebas JVM no miden pruebas instrumentadas ejecutadas en dispositivo. El umbral aún no se alcanza; el informe permite identificar las clases pendientes.
