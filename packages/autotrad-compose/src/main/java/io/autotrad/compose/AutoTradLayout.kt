package io.autotrad.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import io.autotrad.core.AutoTrad
import io.autotrad.core.isRtlLocale

@Composable
fun AutoTradLayout(content: @Composable () -> Unit) {
  val locale = AutoTrad.currentLocale.collectAsState().value
  val dir = if (isRtlLocale(locale)) LayoutDirection.Rtl else LayoutDirection.Ltr
  CompositionLocalProvider(LocalLayoutDirection provides dir) {
    content()
  }
}
