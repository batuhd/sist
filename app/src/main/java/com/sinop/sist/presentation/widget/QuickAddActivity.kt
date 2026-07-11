package com.sinop.sist.presentation.widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sinop.sist.ui.theme.SistTheme

class QuickAddActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SistTheme {
                // TODO: Implement quick add bottom sheet in Module 4
            }
        }
    }
}
