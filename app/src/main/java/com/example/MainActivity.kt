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
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Landscape
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
import com.example.ui.screens.ManaEnergyScreen
import com.example.ui.screens.MarketScreen
import com.example.ui.screens.WorkshopScreen
import com.example.ui.theme.*

enum class FarmScreen(val title: String, val icon: ImageVector) {
    WORLD_3D("Realm", Icons.Default.Landscape),
    CROPS("Fields", Icons.Default.LocalFlorist),
    ENERGY("Mana", Icons.Default.AutoFixHigh),
    LIVESTOCK("Menagerie", Icons.Default.Pets),
    WORKSHOP("Forge", Icons.Default.Build),
    MARKET("Bazaar", Icons.Default.Store),
    LEDGER("Chronicles", Icons.Default.HistoryEdu)
}

class MainActivity : ComponentActivity() {

    private val viewModel: FarmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedievalIslandTheme {
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
                containerColor = Color(0xFF1A0F0A), // Deep Wood
                contentColor = Parchment,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .height(60.dp)
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
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = MedievalSharpFontFamily,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Parchment,
                            selectedTextColor = AgedGold,
                            indicatorColor = MedievalCrimson,
                            unselectedIconColor = Bronze,
                            unselectedTextColor = Bronze
                        ),
                        modifier = Modifier.testTag("nav_${screen.name.lowercase()}")
                    )
                }
            }
        }
    )
 { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF1A0F0A))
        ) {
            // Persistent 3D World layer
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Game3DScreen(
                    viewModel = viewModel,
                    isVisible = currentScreen == FarmScreen.WORLD_3D
                )
            }

            // High-performance overlay sub-screens
            if (currentScreen != FarmScreen.WORLD_3D) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1A0F0A))
                ) {
                    when (currentScreen) {
                        FarmScreen.CROPS -> AgricultureScreen(viewModel = viewModel)
                        FarmScreen.ENERGY -> ManaEnergyScreen(viewModel = viewModel)
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
