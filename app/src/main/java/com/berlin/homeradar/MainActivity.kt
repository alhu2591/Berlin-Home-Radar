package com.berlin.homeradar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.berlin.homeradar.presentation.navigation.BerlinHomeRadarNavGraph
import com.berlin.homeradar.presentation.theme.BerlinHomeRadarTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BerlinHomeRadarTheme {
                BerlinHomeRadarNavGraph()
            }
        }
    }
}
