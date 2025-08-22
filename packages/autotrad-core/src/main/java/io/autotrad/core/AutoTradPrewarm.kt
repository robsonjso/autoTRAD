package io.autotrad.core

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.Locale

/**
 * Utilitários para pré-aquecimento de traduções e modelos.
 * Evita lag na primeira tela do app.
 */
object AutoTradPrewarm {

    /**
     * Pré-traduz uma lista de literais para o idioma alvo.
     * Executa em paralelo para máxima velocidade.
     */
    suspend fun prewarm(
        literals: List<String>,
        tgt: Locale = AutoTrad.currentLocale.value
    ) {
        coroutineScope {
            literals.map { literal ->
                async {
                    runCatching { 
                        AutoTrad.translate(literal, tgt = tgt) 
                    }
                }
            }.awaitAll()
        }
    }

    /**
     * Pré-aquece textos críticos da UI (botões, labels principais).
     * Use no Splash ou no primeiro ViewModel.
     */
    suspend fun prewarmCriticalUI(
        tgt: Locale = AutoTrad.currentLocale.value
    ) {
        val criticalTexts = listOf(
            "Entrar", "Login", "Continuar", "Cancelar", "Salvar",
            "Sim", "Não", "OK", "Voltar", "Próximo", "Anterior"
        )
        prewarm(criticalTexts, tgt)
    }
}
