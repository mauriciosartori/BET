package com.sartori.brick

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sartori.brick.feature.earthquakelist.EarthquakeListScreen
import com.sartori.brick.ui.theme.BrickTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrickTheme {
                EarthquakeListScreen()
            }
        }
    }
}
