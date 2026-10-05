package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FarmViewModel
import com.example.ui.game3d.Game3DScreen
import com.example.ui.screens.AgricultureScreen
import com.example.ui.screens.BusinessDashboardScreen
import com.example.ui.screens.LivestockScreen
import com.example.ui.screens.MarketScreen
import com.example.ui.screens.SolarEnergyScreen
import com.example.ui.screens.WorkshopScreen
import com.example.ui.theme.CleanCyan
import com.example.ui.theme.SolarEmerald
import com.example.ui.theme.SolarpunkFarmTheme
import com.example.ui.theme.SunGold

enum class FarmScreen(val title: String, val icon: ImageVector) {
    WORLD_3D("3D Farm", Icons.Default.Public),
    CROPS("Crops", Icons.Default.LocalFlorist),
    ENERGY("Energy", Icons.Default.Bolt),
    LIVESTOCK("Sanctuary", Icons.Default.Pets),
    WORKSHOP("Workshop", Icons.Default.Build),
    MARKET("Market", Icons.Default.Store),
    LEDGER("Eco Ledger", Icons.Default.Park)
}

class MainActivity : ComponentActivity() {

    private val viewModel: FarmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SolarpunkFarmTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.executeFullSaveFlow()
    }

    override fun onStop() {
        super.onStop()
        viewModel.executeFullSaveFlow()
    }
}

@Composable
fun MainAppContent(viewModel: FarmViewModel) {
    var currentScreen by remember { mutableStateOf(FarmScreen.WORLD_3D) }

    if (currentScreen != FarmScreen.WORLD_3D) {
        BackHandler {
            currentScreen = FarmScreen.WORLD_3D
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0A181B),
                contentColor = Color.White,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .height(54.dp)
                    .testTag("main_navigation_bar")
            ) {
                FarmScreen.values().forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF091215),
                            selectedTextColor = SolarEmerald,
                            indicatorColor = SolarEmerald,
                            unselectedIconColor = Color(0xFF90A4AE),
                            unselectedTextColor = Color(0xFF90A4AE)
                        ),
                        modifier = Modifier.testTag("nav_${screen.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF091215))
        ) {
            // Persistent 3D World layer (prevents SurfaceSyncGroup teardowns during tab navigation)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = if (currentScreen == FarmScreen.WORLD_3D) 1f else 0f
                    }
            ) {
                Game3DScreen(viewModel = viewModel)
            }

            // High-performance overlay sub-screens
            if (currentScreen != FarmScreen.WORLD_3D) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF091215))
                ) {
                    when (currentScreen) {
                        FarmScreen.CROPS -> AgricultureScreen(viewModel = viewModel)
                        FarmScreen.ENERGY -> SolarEnergyScreen(viewModel = viewModel)
                        FarmScreen.LIVESTOCK -> LivestockScreen(viewModel = viewModel)
                        FarmScreen.WORKSHOP -> WorkshopScreen(viewModel = viewModel)
                        FarmScreen.MARKET -> MarketScreen(viewModel = viewModel)
                        FarmScreen.LEDGER -> BusinessDashboardScreen(viewModel = viewModel)
                        FarmScreen.WORLD_3D -> Unit
                    }
                }
            }
        }
    }
}
