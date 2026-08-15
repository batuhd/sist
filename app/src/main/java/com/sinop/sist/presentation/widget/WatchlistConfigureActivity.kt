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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.sinop.sist.SistApplication
import com.sinop.sist.presentation.components.SistTopBar
import com.sinop.sist.ui.theme.SistRadius
import com.sinop.sist.ui.theme.SistTheme
import com.sinop.sist.ui.theme.SistTypography
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Takip Listesi widget'i icin yapilandirma ekrani.
 * Ilk kurulumda ve widget ustundeki disli ikonundan ayni ekran acilir;
 * kullanici serbest sembol girebilir, bilinen sembollerden secebilir,
 * tema ve yazi boyutunu belirleyebilir (en fazla 5 sembol).
 */
class WatchlistConfigureActivity : ComponentActivity() {

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
                WatchlistConfigureScreen(
                    widgetId = appWidgetId,
                    onSave = { selected ->
                        WidgetDataProvider.saveWatchlistSymbols(this, appWidgetId, selected)
                        finishWithResult(Activity.RESULT_OK)
                    },
                    onUseDefault = {
                        WidgetDataProvider.saveWatchlistSymbols(this, appWidgetId, emptyList())
                        finishWithResult(Activity.RESULT_OK)
                    },
                    onCancel = { finishWithResult(Activity.RESULT_CANCELED) }
                )
            }
        }
    }

    private fun finishWithResult(resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            lifecycleScope.launch {
                WatchlistWidget().updateAllInstances(this@WatchlistConfigureActivity)
            }
        }
        setResult(
            resultCode,
            Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        )
        finish()
    }
}

@Composable
private fun WatchlistConfigureScreen(
    widgetId: Int,
    onSave: (List<String>) -> Unit,
    onUseDefault: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as SistApplication
    val scope = rememberCoroutineScope()

    var knownSymbols by remember { mutableStateOf<List<String>>(emptyList()) }
    var portfolioSymbols by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selected by remember { mutableStateOf<List<String>>(emptyList()) }
    var theme by remember { mutableStateOf(WidgetThemePrefs.themeFor(context, widgetId)) }
    var fontKey by remember { mutableStateOf(WidgetThemePrefs.fontKeyFor(context, widgetId)) }
    var input by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val assets = app.container.assetRepository.getAllAssets().first().map { it.symbol.trim().uppercase() }
        val cached = app.container.priceCacheRepository.getAll().first().map { it.symbol.trim().uppercase() }
        portfolioSymbols = assets.toSet()
        knownSymbols = (assets + cached).distinct().sorted()
        selected = WidgetDataProvider.getWatchlistSymbols(context, widgetId)
        loaded = true
    }

    Scaffold(
        topBar = {
            SistTopBar(title = "Takip Listesi", onBackClick = onCancel)
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        WidgetThemePrefs.saveTheme(context, widgetId, theme)
                        WidgetThemePrefs.saveFontScale(context, widgetId, fontKey)
                        onUseDefault()
                    }) {
                        Text("Otomatik seç")
                    }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = {
                        WidgetThemePrefs.saveTheme(context, widgetId, theme)
                        WidgetThemePrefs.saveFontScale(context, widgetId, fontKey)
                        onSave(selected)
                    }) {
                        Text("Kaydet")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                SectionLabel("Sembol Ekle")
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(SistRadius.lg),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = input,
                                onValueChange = { input = it.uppercase() },
                                label = { Text("Sembol") },
                                placeholder = { Text("örn. THYAO, BTC-USD") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    val symbol = input.trim().uppercase()
                                    if (symbol.isNotBlank() && selected.size < 5) {
                                        if (symbol !in selected) selected = selected + symbol
                                        if (symbol !in knownSymbols) knownSymbols = (knownSymbols + symbol).sorted()
                                        input = ""
                                    }
                                },
                                enabled = input.isNotBlank() && selected.size < 5
                            ) {
                                Text("Ekle")
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "En fazla 5 sembol - ${selected.size}/5",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (selected.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    SectionLabel("Seçilenler")
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selected.forEach { symbol ->
                            InputChip(
                                selected = true,
                                onClick = { selected = selected - symbol },
                                label = { Text(symbol) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Kaldır",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                SectionLabel("Bilinen Semboller")
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(SistRadius.lg),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    if (!loaded) {
                        Text(
                            text = "Yükleniyor...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else if (knownSymbols.isEmpty()) {
                        Text(
                            text = "Portföyünüzde varlık yok. Yukarıdan sembol ekleyin.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        Column {
                            knownSymbols.forEachIndexed { index, symbol ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selected = when {
                                                symbol in selected -> selected - symbol
                                                selected.size < 5 -> selected + symbol
                                                else -> selected
                                            }
                                        }
                                        .padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = symbol in selected,
                                        onCheckedChange = { checked ->
                                            selected = when {
                                                checked && symbol !in selected && selected.size < 5 -> selected + symbol
                                                !checked -> selected - symbol
                                                else -> selected
                                            }
                                        }
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = symbol,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = if (symbol in portfolioSymbols) "Portföyde" else "Fiyat önbelleği",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                app.container.priceCacheRepository.deletePrice(symbol)
                                                knownSymbols = knownSymbols - symbol
                                                selected = selected - symbol
                                                WatchlistWidget().updateAllInstances(context)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Önbellekten sil",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (index < knownSymbols.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                SectionLabel("Görünüm")
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(SistRadius.lg),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tema",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
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

                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Yazı Boyutu",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
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
                }

                Spacer(Modifier.height(24.dp))
            }
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
