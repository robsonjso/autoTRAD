plugins {
  id("com.android.library")
  id("org.jetbrains.kotlin.android")
  id("maven-publish")
}

android {
  namespace = "io.autotrad.core"
  compileSdk = 34

  defaultConfig {
    minSdk = 21
    consumerProguardFiles("consumer-rules.pro")
  }
  buildTypes {
    release { isMinifyEnabled = false }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions {
    jvmTarget = "17"
  }
}

dependencies {
  implementation("androidx.core:core-ktx:1.13.1")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
  implementation("com.google.mlkit:translate:17.0.2")
  implementation("com.google.mlkit:language-id:17.0.5")
  implementation("androidx.datastore:datastore-preferences:1.1.1")
  
  testImplementation("junit:junit:4.13.2")
  testImplementation("io.mockk:mockk:1.13.8")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}

afterEvaluate {
  publishing {
    publications {
      create<MavenPublication>("release") {
        groupId = "com.github.robsonjso"
        artifactId = "autotrad-core"
        version = "0.1.0"
        from(components["release"])
        pom {
          name.set("AutoTrad Core")
          description.set("Runtime i18n engine sem strings.xml")
          licenses { license { name.set("Apache-2.0") } }
          scm { url.set("https://github.com/robsonjso/autotrad") }
        }
      }
    }
  }
}
