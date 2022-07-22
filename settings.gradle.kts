rootProject.name = "D4rkBotKt"

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      version("kotlin", "1.7.10")

      plugin("kotlinJVM", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
      plugin("shadowJar", "com.github.johnrengelman.shadow").version("7.1.2")

      // Kotlin
      library("kotlin-stdlib", "org.jetbrains.kotlin", "kotlin-stdlib").versionRef("kotlin")
      library("kotlin.scriptEngine", "org.jetbrains.kotlin", "kotlin-scripting-jsr223").versionRef("kotlin")
      library("kotlinx-coroutines", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").version("1.6.2")
      library("okhttp-coroutines", "ru.gildor.coroutines", "kotlin-coroutines-okhttp").version("1.0")

      // JDA & Lavaplayer
      library("jda-core", "net.dv8tion", "JDA").version("5.0.0-alpha.17")
      library("jda-ktx", "com.github.minndevelopment", "jda-ktx").version("03b07e7")
      library("lavaplayer", "com.github.davidffa", "lavaplayer-fork").version("fad7298")
      library("jda-nas", "com.github.davidffa", "jda-nas-fork").version("1.0.3")

      // Lavaplayer audio filters
      library("lavadsp", "com.github.davidffa", "lavadsp-fork").version("0.7.8")

      // YAML parsing
      library("snakeyaml", "org.yaml", "snakeyaml").version("1.30")

      // MongoDB
      library("kmongo", "org.litote.kmongo", "kmongo-coroutine").version("4.6.1")

      // Native System
      library("oshi", "com.github.oshi", "oshi-core").version("6.2.2")

      // Logger impl
      library("logback", "ch.qos.logback", "logback-classic").version("1.2.11")
    }
  }
}