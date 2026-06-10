package com.example.dotory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.dotory.ui.map.MapNavigationEvent
import com.example.dotory.ui.map.MapScreen
import com.example.dotory.ui.map.MapViewModel
import com.example.dotory.ui.theme.DotoryTheme
import com.example.dotory.ui.write.WriteDotScreen
import com.example.dotory.ui.write.WriteDotViewModel
import com.example.dotory.ui.write.WriteDotViewModelFactory

enum class AppScreen {
    Map, WriteDot
}

class MainActivity : ComponentActivity() {
    private val mapViewModel: MapViewModel by viewModels()
    private val writeDotViewModel: WriteDotViewModel by viewModels {
        WriteDotViewModelFactory((application as DotoryApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DotoryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf(AppScreen.Map) }

                    // MapViewModel로부터 화면 전환 이벤트를 관찰
                    LaunchedEffect(mapViewModel) {
                        mapViewModel.navigationEvent.collect { event ->
                            when (event) {
                                is MapNavigationEvent.NavigateToWriteDot -> {
                                    writeDotViewModel.initCoordinates(event.latitude, event.longitude)
                                    currentScreen = AppScreen.WriteDot
                                }
                            }
                        }
                    }

                    when (currentScreen) {
                        AppScreen.Map -> {
                            MapScreen(viewModel = mapViewModel)
                        }
                        AppScreen.WriteDot -> {
                            WriteDotScreen(
                                viewModel = writeDotViewModel,
                                onBackClick = { currentScreen = AppScreen.Map }
                            )
                        }
                    }
                }
            }
        }
    }
}