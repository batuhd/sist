package com.sinop.sist.presentation.assets.add

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sinop.sist.domain.model.AssetType
import com.sinop.sist.presentation.components.SistTopBar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddAssetScreen(
    onBackClick: () -> Unit,
    assetId: Long? = null,
    viewModel: AddAssetViewModel = viewModel(factory = AddAssetViewModel.factory(assetId))
) {
    val state by viewModel.state.collectAsState()
    val event by viewModel.event.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(event) {
        if (event is AddAssetEvent.Saved) {
            Toast.makeText(context, "Kaydedildi", Toast.LENGTH_SHORT).show()
            viewModel.consumeEvent()
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            SistTopBar(
                title = if (state.isEditing) "Varlığı Düzenle" else "Yeni Varlık",
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.symbol,
                onValueChange = viewModel::onSymbolChange,
                label = { Text("Sembol") },
                placeholder = { Text("Örn: THYAO, GARAN, XU100") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Ad (opsiyonel)") },
                placeholder = { Text("Örn: Türk Hava Yolları") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Column {
                Text(
                    text = "Varlık Türü",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssetType.entries.forEach { type ->
                        FilterChip(
                            selected = state.assetType == type,
                            onClick = { viewModel.onAssetTypeChange(type) },
                            label = { Text(assetTypeLabel(type)) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.currentPrice,
                onValueChange = viewModel::onCurrentPriceChange,
                label = { Text("Güncel Fiyat (opsiyonel)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            OutlinedTextField(
                value = state.currencyCode,
                onValueChange = viewModel::onCurrencyChange,
                label = { Text("Para Birimi") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = viewModel::saveAsset,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = if (state.isEditing) "Güncelle" else "Kaydet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun assetTypeLabel(type: AssetType): String = when (type) {
    AssetType.STOCK -> "Hisse"
    AssetType.FUND -> "Fon"
    AssetType.CURRENCY -> "Döviz"
    AssetType.GOLD -> "Altın"
}