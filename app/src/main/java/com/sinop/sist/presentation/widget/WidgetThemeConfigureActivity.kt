package com.sinop.sist.presentation.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.sinop.sist.presentation.components.SistTopBar
import com.sinop.sist.ui.theme.SistTheme
import com.sinop.sist.ui.theme.SistTypography
import kotlinx.coroutines.launch

/**
 * Ortak widget renk temasi yapilandirma ekrani.
 * Tum widget'lar bu ekrani android:configure olarak kullanir.
 */
class WidgetThemeConfigureActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        setContent {
            SistTheme {
                ThemeConfigureContent(
                    initialTheme = WidgetThemePrefs.themeFor(this, appWidgetId),
                    initialFont = WidgetThemePrefs.fontKeyFor(this, appWidgetId),
                    onSave = { theme, fontKey ->
                        WidgetThemePrefs.saveTheme(this, appWidgetId, theme)
                        WidgetThemePrefs.saveFontScale(this, appWidgetId, fontKey)
                        lifecycleScope.launch {
                            PortfolioWidget().updateAllInstances(this@WidgetThemeConfigureActivity)
                            BudgetWidget().updateAllInstances(this@WidgetThemeConfigureActivity)
                            NetWorthWidget().updateAllInstances(this@WidgetThemeConfigureActivity)
                            QuickAddWidget().updateAllInstances(this@WidgetThemeConfigureActivity)
                            DistributionWidget().updateAllInstances(this@WidgetThemeConfigureActivity)
                            WatchlistWidget().updateAllInstances(this@WidgetThemeConfigureActivity)
                        }
                        setResult(
                            Activity.RESULT_OK,
                            Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        )
                        finish()
                    },
                    onCancel = {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemeConfigureContent(
    initialTheme: String,
    initialFont: String,
    onSave: (String, String) -> Unit,
    onCancel: () -> Unit
) {
    var selected by remember { mutableStateOf(initialTheme) }
    var fontKey by remember { mutableStateOf(initialFont) }

    val options = listOf(
        WidgetThemePrefs.THEME_BRAND,
        WidgetThemePrefs.THEME_DARK,
        WidgetThemePrefs.THEME_LIGHT,
        WidgetThemePrefs.THEME_SYSTEM
    )
    val fontOptions = listOf(
        WidgetThemePrefs.FONT_NORMAL,
        WidgetThemePrefs.FONT_BIG,
        WidgetThemePrefs.FONT_HUGE
    )

    Scaffold(
        topBar = {
            SistTopBar(title = "Widget Teması", onBackClick = onCancel)
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Button(
                    onClick = { onSave(selected, fontKey) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Kaydet")
                }
            }
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
            SectionLabel("Renk Teması")
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    options.forEachIndexed { index, theme ->
                        val isSelected = selected == theme
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selected = theme }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ThemeSwatch(palette = WidgetThemePrefs.paletteFor(theme))
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = WidgetThemePrefs.labelFor(theme),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                if (theme == WidgetThemePrefs.THEME_BRAND) {
                                    Text(
                                        text = "Uygulama kimliğiyle uyumlu",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = if (isSelected) "Seçili" else "Seç",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (index < options.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionLabel("Yazı Boyutu")
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Widget metinlerinin büyüklüğünü belirler",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        fontOptions.forEach { option ->
                            FilterChip(
                                selected = fontKey == option,
                                onClick = { fontKey = option },
                                label = { Text(WidgetThemePrefs.labelForFont(option)) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
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

@Composable
private fun ThemeSwatch(palette: WidgetPalette) {
    // Arka plan rengini temsil eden sabit renkler: palet onHero/hero degerlerinin
    // yaklasik gorseli (config ekrani tam Compose oldugu icin statik kullanilir).
    val background = when (palette) {
        BrandWidgetPalette -> Color(0xFF06281C)
        DarkWidgetPalette -> Color(0xFF0F1512)
        LightWidgetPalette -> Color(0xFFF6FBF8)
        else -> Color(0xFF06281C)
    }
    val accent = when (palette) {
        BrandWidgetPalette, SystemWidgetPalette -> Color(0xFFE8C15C)
        DarkWidgetPalette -> Color(0xFF4EDC9D)
        else -> Color(0xFF0E7A55)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(12.dp),
            color = background
        ) {
            Surface(
                modifier = Modifier
                    .padding(9.dp)
                    .size(22.dp),
                shape = CircleShape,
                color = accent
            ) {}
        }
    }
}
