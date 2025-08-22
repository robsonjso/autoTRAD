plugins {
  id("com.android.library")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.plugin.compose")
  id("maven-publish")
}

android {
  namespace = "io.autotrad.compose"
  compileSdk = 34

  defaultConfig { minSdk = 21 }

  buildFeatures { compose = true }
  composeOptions {
    kotlinCompilerExtensionVersion = "1.5.14"
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions { jvmTarget = "17" }
}

dependencies {
  implementation(project(":packages:autotrad-core"))
  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.compose.ui:ui:1.6.8")
  implementation("androidx.compose.material3:material3:1.2.1")
  implementation("androidx.compose.runtime:runtime:1.6.8")
}

afterEvaluate {
  publishing {
    publications {
      create<MavenPublication>("release") {
        groupId = "com.github.robsonjso"
        artifactId = "autotrad-compose"
        version = "0.1.0"
        from(components["release"])
        pom {
          name.set("AutoTrad Compose")
          description.set("UI Compose helpers para AutoTrad")
          licenses { license { name.set("Apache-2.0") } }
          scm { url.set("https://github.com/robsonjso/autotrad") }
        }
      }
    }
  }
}
