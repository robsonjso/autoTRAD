plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.plugin.compose")
}

import java.io.File

android {
    namespace = "com.example.sample"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.sample"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
  buildTypes { release { isMinifyEnabled = false } }
  buildFeatures { compose = true }
  composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions { jvmTarget = "17" }
}

dependencies {
  implementation(project(":packages:autotrad-compose"))
  implementation(project(":packages:autotrad-core"))
  implementation("androidx.activity:activity-compose:1.9.2")
  implementation("androidx.compose.ui:ui:1.6.8")
  implementation("androidx.compose.material3:material3:1.2.1")
  implementation("androidx.compose.ui:ui-tooling-preview:1.6.8")
  debugImplementation("androidx.compose.ui:ui-tooling:1.6.8")
  implementation("androidx.appcompat:appcompat:1.6.1")
}

tasks.register("mergeAutoTradPending") {
    group = "autotrad"
    description = "Mergeia arquivos pending (./pending/autotrad.pending.<lang>.json) nos assets."
    doLast {
        val pendingDir = File(project.projectDir, "pending")
        if (!pendingDir.exists()) {
            println("Nenhum diretório 'pending' encontrado em ${pendingDir.path}")
            return@doLast
        }
        val assetsDir = File(project.projectDir, "src/main/assets/autotrad").apply { mkdirs() }

        pendingDir.listFiles { f -> f.name.startsWith("autotrad.pending.") && f.extension == "json" }?.forEach { pend ->
            val lang = pend.name.removePrefix("autotrad.pending.").removeSuffix(".json")
            val asset = File(assetsDir, "autotrad.$lang.json")
            
            // Parse JSON manualmente (sem dependência externa)
            val baseText = if (asset.exists()) asset.readText() else "{}"
            val pendText = pend.readText()
            
            // Merge simples: concatena os JSONs (assume formato correto)
            val merged = if (baseText == "{}") {
                pendText
            } else {
                // Remove } do base e { do pending, concatena com vírgula
                val baseWithoutEnd = baseText.removeSuffix("}")
                val pendWithoutStart = pendText.removePrefix("{")
                "$baseWithoutEnd,$pendWithoutStart"
            }
            
            asset.writeText(merged)
            println("Merged ${pend.name} -> ${asset.path}")
        }
    }
}
