package com.yybb.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.yybb.myapplication.presentation.navigation.RootNavGraph
import com.yybb.myapplication.presentation.ui.theme.MyApplicationTheme
import com.yybb.myapplication.presentation.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val isColorBlindTheme by settingsViewModel.isColorBlindTheme.collectAsState()
            MyApplicationTheme(
                colorBlindMode = isColorBlindTheme
            ) {
                RootNavGraph()
            }
        }
    }
}
