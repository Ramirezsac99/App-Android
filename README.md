# ControlGastos (Android)

App Android (Kotlin, Views XML + View Binding) que usa la libreria `gastos-core` (proyecto Maven aparte).

## Antes de abrir el proyecto
1. Abrir `clase12-gastos-core` en IntelliJ y ejecutar `mvn clean install` (debe terminar en BUILD SUCCESS).
   Esto deja `gastos-core-1.1.0.jar` en `~/.m2`.
2. Abrir esta carpeta en Android Studio (File > Open) y esperar el Gradle Sync.

## Generar el APK
Build > Build Bundle(s) / APK(s) > Build APK(s)
Ruta: `app/build/outputs/apk/debug/app-debug.apk`

## Estructura
- `ServicioApp.kt` - el unico ControlGastos de la app
- `MainActivity.kt` - Registro (categorias, dialogo con teclado, lista)
- `ResumenActivity.kt` - ingreso, gasto y saldo del mes
