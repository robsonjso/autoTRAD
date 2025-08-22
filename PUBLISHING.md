# Publicação do AutoTrad

Este guia mostra como publicar a lib via **JitPack** (imediato) e, depois, no **Maven Central** (Sonatype).

---

## ✅ JitPack (rápido)

1. Garanta que **cada módulo de lib** tem `maven-publish` configurado:
   - `groupId = "com.github.robsonjso"`
   - `artifactId = "autotrad-core"` / `"autotrad-compose"`
   - `version = "0.1.0"`

2. Faça push do repositório público para o GitHub e crie a **tag**:
```bash
git push -u origin main
git tag v0.1.0
git push origin v0.1.0
```

3. No projeto consumidor:
```kotlin
// settings.gradle(.kts)
dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
  }
}

// build.gradle(.kts) do app
dependencies {
  implementation("com.github.robsonjso:autotrad-core:0.1.0")
  implementation("com.github.robsonjso:autotrad-compose:0.1.0")
}
```

Dica: adicione `.jitpack.yml` com `openjdk17` na raiz, se necessário.

---

## 🏛️ Maven Central (depois)

### 1) Pré-requisitos

- Conta no Sonatype OSSRH (Organization: "io.github.robsonjso")
- Ownership do namespace `io.github.robsonjso` (OSSRH valida que você é o dono do GitHub)
- Geração de chaves GPG (para assinar artefatos)

### 2) Plugins no root (`build.gradle.kts`)
```kotlin
plugins {
  id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
  id("signing")
}

nexusPublishing {
  repositories {
    sonatype {
      stagingProfileId.set(System.getenv("OSSRH_STAGING_PROFILE_ID")) // opcional
      username.set(System.getenv("OSSRH_USERNAME"))
      password.set(System.getenv("OSSRH_PASSWORD"))
    }
  }
}
```

### 3) Em cada módulo de lib

Mantenha `maven-publish` com POM completo (name, description, license, scm).

Adicione assinatura:
```kotlin
signing {
  useInMemoryPgpKeys(
    System.getenv("GPG_SIGNING_KEY"),       // conteúdo ASCII-armored
    System.getenv("GPG_SIGNING_PASSWORD")   // senha da chave
  )
  sign(publishing.publications)
}
```

### 4) Publicar manualmente
```bash
# publica no staging
./gradlew publishToSonatype

# fecha e libera o repositório no Sonatype
./gradlew closeAndReleaseSonatypeStagingRepository
```

### 5) Dicas

- Configure secrets no GitHub (`OSSRH_USERNAME`, `OSSRH_PASSWORD`, `GPG_SIGNING_KEY`, `GPG_SIGNING_PASSWORD`)
- Use um workflow separado para publicar na criação de Release
- Documentação oficial do Sonatype/Gradle Nexus Publish plugin será útil quando você for ativar de fato
