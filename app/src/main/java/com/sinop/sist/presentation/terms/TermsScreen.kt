package com.sinop.sist.presentation.terms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun TermsScreen(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Kabul Ediyorum",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onDecline,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Kabul Etmiyorum - Uygulamadan Çık",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "⚠️ Yasal Uyarı ve Kullanım Şartları",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            val sections = listOf(
                "Genel Bilgilendirme" to "Bu uygulama yalnizca genel bilgilendirme ve kişisel takip amaciyla geliştirilmiştir. Uygulamada yer alan hisse senedi, kripto varlik, döviz, emtia, endeks ve diğer finansal verilere ilişkin bilgiler; yatirim tavsiyesi, yatirim danişmanliği, alim-satim önerisi veya finansal tavsiye niteliği taşimaz.",
                "Veri Doğruluğu" to "Uygulamada sunulan fiyatlar, grafikler, teknik göstergeler, analizler, istatistikler, tahminler, yapay zeka çiktilari, bildirimler ve diğer tüm içerikler yalnizca bilgi amaçlidir. Bu içeriklerin doğruluğu, güncelliği, eksiksizliği veya belirli bir amaca uygunluğu garanti edilmez. Veri sağlayicilarindan kaynaklanabilecek gecikmeler, eksiklikler veya hatalar nedeniyle oluşabilecek sonuçlardan uygulama geliştiricisi sorumlu tutulamaz.",
                "Yatirim Kararlari" to "Yatirim kararlari kişisel risk profili, finansal durum ve yatirim hedefleri dikkate alinarak verilmelidir. Gerektiğinde, Sermaye Piyasasi Kurulu (SPK) tarafindan yetkilendirilmiş yatirim kuruluşlari veya yatirim danişmanlarindan profesyonel destek alinmasi tavsiye edilir.",
                "Sorumluluk Reddi" to "Bu uygulamanin kullanimi sonucunda doğabilecek doğrudan veya dolayli maddi ya da manevi zararlar, kar kaybi, veri kaybi veya diğer herhangi bir zarardan uygulama geliştiricisi hiçbir şekilde sorumlu değildir.",
                "Kabul Beyani" to "Bu uygulamayi kullanarak yukaridaki şartlari okuduğunuzu, anladiğinizi ve kabul ettiğinizi beyan etmiş olursunuz. Kabul etmeden uygulamayi kullanmaya devam edemezsiniz."
            )

            sections.forEach { (title, content) ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Justify
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
