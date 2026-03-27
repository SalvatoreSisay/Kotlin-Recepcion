# MVP de Gestión de Pacientes en Kotlin

**Trabajo alfa MVP control de pacientes**  
*Primera construcción de aplicación desktop para familiarizarme con Kotlin.*

## 📌 Descripción General
Este repositorio contiene una aplicación de escritorio desarrollada con Kotlin y JavaFX diseñada para la gestión de pacientes. Sirve como un Producto Mínimo Viable (MVP) y un proyecto de aprendizaje para explorar el ecosistema de Kotlin en el desarrollo de escritorio.

### Características Principales
- **Portal Seguro:** Sistema de autenticación de empleados/usuarios.
- **Registro de Pacientes:** Flujos de trabajo para registrar pacientes nuevos y recurrentes.
- **Interfaz Moderna:** Diseños adaptables utilizando FXML, CSS y transiciones fluidas.

## 🛠️ Stack Tecnológico
- **Lenguaje:** Kotlin 2.1.20 (JVM)
- **Frameworks:**
  - [JavaFX 21](https://openjfx.io/) (GUI)
  - [ControlsFX](https://controlsfx.github.io/)
  - [FormsFX](https://github.com/dlsc-software-consulting-gmbh/FormsFX)
  - [Ikonli](https://kordamp.org/ikonli/) (Iconos)
- **Sistema de Construcción:** Gradle (Kotlin DSL)
- **Compatibilidad:** JDK 23 (definido en el toolchain)

## 🚀 Configuración y Ejecución

### Requisitos
- **JDK 23+** instalado y configurado.

### Ejecutar la Aplicación
Para iniciar la aplicación desde la raíz del proyecto:
```bash
./gradlew run
```

### Construir Distribución (jlink)
El proyecto está configurado con el plugin `jlink` para crear imágenes de tiempo de ejecución personalizadas.
```bash
./gradlew jlink
```
*El archivo zip de salida se encontrará en `build/distributions/`.*

## 📜 Scripts Disponibles (Tareas de Gradle)
- `run`: Inicia la aplicación JavaFX.
- `build`: Ensambla y prueba el proyecto.
- `test`: Ejecuta las pruebas de JUnit 5.
- `jlink`: Genera una imagen de tiempo de ejecución modular.
- `jlinkZip`: Empaqueta la imagen de tiempo de ejecución en un archivo ZIP.

## 📁 Estructura del Proyecto
```text
.
├── build.gradle.kts        # Configuración de construcción (Gradle)
├── src/main/java           # module-info.java (Descriptores de módulo)
├── src/main/kotlin         # Código fuente en Kotlin
│   └── .../recepcionui     # Lógica de la aplicación y controladores
├── src/main/resources      # Activos FXML, CSS e imágenes
└── ...
```

## 🔐 Variables de Entorno
*Actualmente no se requieren variables de entorno para este proyecto.*
- [ ] TODO: Definir cadenas de conexión a la base de datos si se migra a almacenamiento externo.

## 🧪 Pruebas
Las pruebas unitarias utilizan JUnit 5.
```bash
./gradlew test
```
- [ ] TODO: Aumentar la cobertura de pruebas para los controladores de UI y la lógica de negocio.

## 📄 Licencia
- [ ] TODO: Agregar un archivo LICENSE.

---
© 2026 ISD Informatica y Servicion de Desarrollo
