package com.sinop.sist

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.sinop.sist.presentation.navigation.MainNavigation
import com.sinop.sist.presentation.terms.TermsScreen
import com.sinop.sist.ui.theme.SistTheme
import com.sinop.sist.util.NotificationHelper
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannel(this)

        val termsRepository = (application as SistApplication).container.termsRepository

        setContent {
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            val isTermsAccepted by termsRepository.isTermsAccepted().collectAsState(initial = false)

            SistTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isTermsAccepted) {
                        MainNavigation()
                    } else {
                        TermsScreen(
                            onAccept = {
                                lifecycleScope.launch {
                                    termsRepository.setTermsAccepted(true)
                                }
                            },
                            onDecline = {
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }
}
