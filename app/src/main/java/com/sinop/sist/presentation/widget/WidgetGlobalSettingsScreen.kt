package com.sinop.sist.presentation.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.AppWidgetId
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.sinop.sist.presentation.components.SistTopBar
import com.sinop.sist.ui.theme.SistRadius
import com.sinop.sist.ui.theme.SistTheme
import com.sinop.sist.ui.theme.SistTypography
import kotlinx.coroutines.launch

/**
 * Tum widget'larin rengini ve yazi boyutunu tek seferde ayarlayan ekran.
 * Ayarlar > Widget Ayarlari uzerinden erisilir.
 */
@Composable
fun WidgetGlobalSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var theme by remember { mutableStateOf(WidgetThemePrefs.THEME_BRAND) }
    var fontKey by remember { mutableStateOf(WidgetThemePrefs.FONT_NORMAL) }
    var applying by remember { mutableStateOf(false) }
    var applied by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SistTopBar(title = "Widget Ayarları", onBackClick = onBackClick)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(SistRadius.lg),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = "Buradaki renk ve yazı boyutu seçimi, ana ekranınızdaki tüm Sist widget'larına aynı anda uygulanır.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(Modifier.height(20.dp))
            SectionLabel("Renk Teması")
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(SistRadius.lg),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                FlowRow(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        WidgetThemePrefs.THEME_BRAND,
                        WidgetThemePrefs.THEME_DARK,
                        WidgetThemePrefs.THEME_LIGHT,
                        WidgetThemePrefs.THEME_SYSTEM
                    ).forEach { option ->
                        FilterChip(
                            selected = theme == option,
                            onClick = { theme = option },
                            label = { Text(WidgetThemePrefs.labelFor(option)) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionLabel("Yazı Boyutu")
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(SistRadius.lg),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                FlowRow(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        WidgetThemePrefs.FONT_NORMAL,
                        WidgetThemePrefs.FONT_BIG,
                        WidgetThemePrefs.FONT_HUGE
                    ).forEach { option ->
                        FilterChip(
                            selected = fontKey == option,
                            onClick = { fontKey = option },
                            label = { Text(WidgetThemePrefs.labelForFont(option)) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    applying = true
                    applied = false
                    scope.launch {
                        try {
                            val manager = GlanceAppWidgetManager(context)
                            val widgetClasses = listOf(
                                PortfolioWidget::class.java,
                                BudgetWidget::class.java,
                                NetWorthWidget::class.java,
                                QuickAddWidget::class.java,
                                DistributionWidget::class.java,
                                WatchlistWidget::class.java
                            )
                            widgetClasses.forEach { clazz ->
                                manager.getGlanceIds(clazz).forEach { glanceId ->
                                    val widgetId = (glanceId as? AppWidgetId)?.appWidgetId ?: return@forEach
                                    WidgetThemePrefs.saveTheme(context, widgetId, theme)
                                    WidgetThemePrefs.saveFontScale(context, widgetId, fontKey)
                                }
                            }
                            PortfolioWidget().updateAllInstances(context)
                            BudgetWidget().updateAllInstances(context)
                            NetWorthWidget().updateAllInstances(context)
                            QuickAddWidget().updateAllInstances(context)
                            DistributionWidget().updateAllInstances(context)
                            WatchlistWidget().updateAllInstances(context)
                            applied = true
                        } catch (_: Exception) {
                        } finally {
                            applying = false
                        }
                    }
                },
                enabled = !applying,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (applying) "Uygulanıyor..." else "Tüm Widget'lara Uygula")
            }

            if (applied) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Tüm widget'lara uygulandı",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(SistRadius.lg),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Widget boyutları",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Widget boyutları Android ana ekranı tarafından yönetilir; parmağınızı widget'a basılı tutup kenarlarından sürükleyerek büyütebilir veya küçültebilirsiniz.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = SistTypography.sectionLabel,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
