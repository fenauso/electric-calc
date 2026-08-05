# ⚡ Cálculo Eléctrico para Android

Aplicación Android nativa, sencilla y sin conexión, para comprobar el principio de coordinación:

> **I carga ≤ In magnetotérmico ≤ Iz conductor**

## Funciones

- Calcula la sección mínima del conductor conociendo carga y magnetotérmico.
- Calcula un magnetotérmico comercial conociendo carga y conductor.
- Calcula la carga máxima conociendo magnetotérmico y conductor.
- Admite entrada por potencia o intensidad.
- Sistemas monofásicos y trifásicos.
- Factor de potencia, tensión, longitud y caída de tensión máxima.
- Conductores de cobre o aluminio.
- Aislamiento PVC 70 °C o XLPE 90 °C.
- Corrección orientativa por temperatura ambiente.
- Validación visible de **I ≤ In ≤ Iz**.
- No necesita Internet ni permisos especiales.

## Compilar automáticamente en GitHub

El repositorio incluye el flujo de trabajo:

```text
.github/workflows/build-apk.yml
```

Cada vez que se suben cambios a `main` o `master`, GitHub Actions compila la aplicación y guarda el APK durante 30 días.

### Descargar el APK generado

1. Abre el repositorio en GitHub.
2. Entra en la pestaña **Actions**.
3. Abre la ejecución más reciente llamada **Compilar APK Android**.
4. Baja hasta **Artifacts**.
5. Descarga **CalculadoraElectrica-APK**.
6. Descomprime el ZIP y encontrarás `CalculadoraElectrica-v1.0-debug.apk`.

También se puede ejecutar manualmente desde **Actions → Compilar APK Android → Run workflow**.

## Subir este proyecto a GitHub

### Desde la página web

1. Crea un repositorio nuevo y vacío en GitHub.
2. Descomprime este proyecto.
3. En el repositorio, pulsa **Add file → Upload files**.
4. Sube todo el contenido, incluida la carpeta oculta `.github`.
5. Confirma con **Commit changes**.
6. Abre **Actions** para comprobar la compilación.

### Desde Git

```bash
git init
git add .
git commit -m "Primera versión de la calculadora eléctrica"
git branch -M main
git remote add origin https://github.com/USUARIO/REPOSITORIO.git
git push -u origin main
```

## Compilar localmente

El flujo de GitHub utiliza Java 17, Gradle 8.13, Android SDK 35 y Android Gradle Plugin 8.13.0.

Con Android Studio:

1. Abre esta carpeta como proyecto.
2. Espera a que termine la sincronización.
3. Selecciona **Build → Build APK(s)**.
4. El APK se genera en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Desde una terminal con Gradle 8.13 instalado:

```bash
gradle assembleDebug
```

## Estructura principal

```text
.github/workflows/build-apk.yml   Compilación automática
app/src/main/                     Código y recursos Android
app/build.gradle.kts              Configuración de la aplicación
build.gradle.kts                  Versión del plugin Android
settings.gradle.kts               Repositorios y módulos
```

## Aviso técnico

Las tablas de intensidad admisible incluidas son orientativas para una calculadora inicial. El resultado debe verificarse con el REBT, UNE-HD 60364-5-52, el método real de instalación, número de conductores cargados, agrupamiento, aislamiento térmico y condiciones particulares de la instalación.

## Licencia

MIT. Consulta el archivo [`LICENSE`](LICENSE).
