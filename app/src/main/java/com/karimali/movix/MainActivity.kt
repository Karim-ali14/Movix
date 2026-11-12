package com.karimali.movix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.karimali.movix.core.ui.theme.MovieAppTheme
import com.karimali.movix.features.movies.navigation.MovixNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Edge-to-edge: content draws behind status/navigation bars (modern M3 look).
        // Screens must then handle insets (Scaffold does it for you via its padding).
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MovieAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MovixNavHost()
                }
            }
        }
    }
}
