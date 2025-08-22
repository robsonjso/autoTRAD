package com.example.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import io.autotrad.core.AutoTrad
import io.autotrad.core.MlKitTranslator
import io.autotrad.core.MlKitLanguageDetector
import io.autotrad.core.DetectingTranslator
import io.autotrad.core.GlossaryTranslator
import io.autotrad.core.AutoTradPrewarm
import io.autotrad.core.TextRole
import io.autotrad.core.LocaleManager
import io.autotrad.core.LocalePolicy
import io.autotrad.core.LocaleMode
import io.autotrad.core.AutoTradTelemetry
import io.autotrad.core.TelemetrySink
import io.autotrad.compose.AutoText
import io.autotrad.compose.AutoTradLayout
import io.autotrad.compose.autoContentDescription
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.example.sample.AutoTradPending.zipPending
import java.util.Locale

class MainActivity : ComponentActivity() {
    lateinit var localeManager: LocaleManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa a engine (lib) no sample com glossário e tradução automática
        AutoTrad.init(
            context = applicationContext,
            locale = Locale.getDefault(),
            translators = listOf(
                // Glossário + tradução automática
                GlossaryTranslator(
                    map = mapOf(
                        "Login" to "Enter",
                        "Home" to "Home",
                        "Continuar" to "Continue"
                    ),
                    dontTranslate = setOf("AutoTrad", "ID", "v1.0"),
                    delegate = DetectingTranslator(
                        detector = MlKitLanguageDetector(),
                        delegate = MlKitTranslator(requireWifiForDownload = false),
                        fallbackSource = Locale("pt", "BR")
                    )
                )
            ),
            loadFromAssets = true,
            devCaptureEnabled = true
        )

        // LocaleManager com persistência
        localeManager = LocaleManager(
            context = this,
            policy = LocalePolicy(
                mode = LocaleMode.HYBRID,
                supported = listOf("en", "es", "pt-BR"),
                fallbackChain = listOf("en")
            )
        )
        localeManager.recompute()

        // Telemetria (opcional)
        AutoTradTelemetry.register(object : TelemetrySink {
            override fun onTmHit() {
                println("AutoTrad: TM hit")
            }
            override fun onMtCall(durationMs: Long) {
                println("AutoTrad: MT call took ${durationMs}ms")
            }
            override fun onQualityReject() {
                println("AutoTrad: Quality gate rejected translation")
            }
        })

        // Pré-aquecimento completo
        lifecycleScope.launch {
            // 1) Baixa modelos
            MlKitTranslator().preDownloadLanguages(
                Locale("pt", "BR"), 
                Locale.ENGLISH, 
                Locale("es"),
                requireWifi = false
            )
            
            // 2) Pré-traduz textos críticos
            AutoTradPrewarm.prewarmCriticalUI(Locale.ENGLISH)
        }

        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    // ⬇️ AutoTradLayout aplica RTL automático + Dev Overlay
                    AutoTradLayout {
                        AutoTradDevHost {
                            DemoScreen(
                                onLang = { tag -> localeManager.setUserLanguage(tag) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExportPendingButton() {
    val context = LocalContext.current
    Button(onClick = {
        val zip = zipPending(context)
        if (zip != null) {
            val uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                zip
            )
            val share = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_SUBJECT, "AutoTrad pending")
                putExtra(Intent.EXTRA_TEXT, "Arquivos pending exportados do AutoTrad.")
            }
            context.startActivity(Intent.createChooser(share, "Exportar pending"))
        } else {
            // opcional: Snackbar/Toast avisando que não há pendências
        }
    }) {
        AutoText("Exportar pending", role = TextRole.Button)
    }
}

@Composable
fun DemoScreen(onLang: (String) -> Unit) {
    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AutoText("Entrar")
        Button(onClick = {}) { AutoText("Login", role = TextRole.Button) }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { onLang("en") }) { AutoText("Inglês", role = TextRole.Button) }
            Button(onClick = { onLang("es") }) { AutoText("Espanhol", role = TextRole.Button) }
            Button(onClick = { onLang("pt-BR") }) { AutoText("Português", role = TextRole.Button) }
        }

        // Placeholders
        AutoText("Olá, {name}!", args = mapOf("name" to "Robson"))
        
        // Exemplos de tradução automática (sem strings.xml)
        AutoText("papagaio") // será traduzido automaticamente
        AutoText("computador")
        AutoText("teclado")
        
        // Exemplos de do-not-translate
        AutoText("AutoTrad") // não será traduzido
        AutoText("ID123") // não será traduzido
        AutoText("user@example.com") // email não será traduzido
        
        // Exemplo de acessibilidade
        Button(
            onClick = {},
            modifier = Modifier.autoContentDescription("Botão de exemplo")
        ) {
            AutoText("Exemplo A11y", role = TextRole.Button)
        }
        
        // Botão para exportar pending
        ExportPendingButton()
    }
}
