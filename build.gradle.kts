plugins {
    java
    application
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    id("org.javamodularity.moduleplugin") version "1.8.15"
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "2.25.0"
}

group = "com.resdev.AKrecepcion"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val junitVersion = "5.12.1"


tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("com.resdev.akrecepcion.recepcionui")
    // Utiliza un punto de entrada `main()` de Kotlin para evitar casos extremos relacionados con JavaFX o el lanzador de módulos.
    mainClass.set("com.resdev.akrecepcion.recepcionui.LauncherKt")
}
kotlin {
    jvmToolchain(23)
}

javafx {
    version = "21.0.6"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation("org.controlsfx:controlsfx:11.2.1")
    implementation("com.dlsc.formsfx:formsfx-core:11.6.0") {
        exclude(group = "org.openjfx")
    }
    implementation("org.kordamp.ikonli:ikonli-javafx:12.3.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")

    //MariaDB conector
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.8")
    //HikariCP con soporte nativo java 21+
    implementation("com.zaxxer:HikariCP:7.0.2")
    // Logging simple (Hikari usa SLF4J)
    implementation("org.slf4j:slf4j-simple:2.0.13")
    //Corrutinas de kotlin para optimizacion en el fxml
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.8.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Utilidad: verificar DB sin levantar JavaFX.
tasks.register<JavaExec>("dbPing") {
    group = "verification"
    description = "Hace ping a MariaDB usando HikariCP (SELECT 1). Lee config desde .env/env vars."

    // Nota: lo ejecutamos por classpath (no modular) para evitar friccion JPMS en el output mixed Kotlin/Java.
    mainClass.set("com.resdev.akrecepcion.recepcionui.db.DbPingKt")

    classpath = sourceSets.main.get().runtimeClasspath

    // Importante: el proyecto compila con toolchain Java 23 (classfile 67).
    // Forzamos que este JavaExec use el mismo JDK para evitar "Unsupported major.minor version 67.0".
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(23))
        },
    )
}

jlink {
    imageZip.set(layout.buildDirectory.file("/distributions/app-${javafx.platform.classifier}.zip"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "app"
    }
}
