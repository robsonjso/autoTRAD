package com.example.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.autotrad.compose.AutoTradDevController
import io.autotrad.compose.LocalAutoTradDevController
import io.autotrad.core.AutoTrad
import java.util.Locale

private data class EditorData(val raw: String, val current: String, val locale: Locale)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoTradDevHost(content: @Composable () -> Unit) {
    val sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var editor by remember { mutableStateOf<EditorData?>(null) }

    val controller = remember {
        object : AutoTradDevController {
            override fun openEditor(raw: String, current: String, locale: Locale) {
                editor = EditorData(raw, current, locale)
            }
        }
    }

    CompositionLocalProvider(LocalAutoTradDevController provides controller) {
        content()

        editor?.let { data ->
            var translated by remember(data) { mutableStateOf(data.current) }

            ModalBottomSheet(
                onDismissRequest = { editor = null },
                sheetState = sheetState
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("AutoTrad – Editor (DEV)", Modifier.padding(bottom = 4.dp))
                    OutlinedTextField(
                        value = data.raw,
                        onValueChange = {},
                        label = { Text("Original (read-only)") },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = translated,
                        onValueChange = { translated = it },
                        label = { Text("Tradução") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = { editor = null }) {
                            Text("Cancelar")
                        }
                        Button(onClick = {
                            // Salva no TM + pending.json (valida placeholders e role)
                            val ok = AutoTrad.upsertTranslation(
                                raw = data.raw,
                                translated = translated,
                                locale = data.locale
                            )
                            // Se falhar (placeholders), apenas mantenha aberto (poderia mostrar um aviso)
                            if (ok) editor = null
                        }) {
                            Text("Salvar")
                        }
                    }
                }
            }
        }
    }
}
