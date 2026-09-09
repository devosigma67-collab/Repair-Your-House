package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.game.audio.GameAudioEngine
import com.example.game.data.SaveManager
import com.example.game.engine.GameEngine
import com.example.game.ui.ControlsOverlay
import com.example.game.ui.GameRenderer
import com.example.game.ui.GemdexMenu
import com.example.game.ui.HUDOverlay
import com.example.game.ui.HouseMenu
import com.example.game.ui.MainMenu
import com.example.game.ui.SeasonalAndLeaderboardMenu
import com.example.game.ui.SellDialog
import com.example.game.ui.SettingsDialog
import com.example.game.ui.ShopMenu
import com.example.game.ui.TutorialOverlay
import com.example.ui.theme.MyApplicationTheme

enum class GameScreen {
    MAIN_MENU,
    GAMEPLAY
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        enableEdgeToEdge()

        // Hide system bars for immersive landscape gaming
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            MyApplicationTheme {
                RepairYourHouseApp()
            }
        }
    }
}

@Composable
fun RepairYourHouseApp() {
    val context = LocalContext.current
    val saveManager = remember { SaveManager(context) }
    val audioEngine = remember {
        GameAudioEngine(context).apply {
            sfxVolume = saveManager.sfxVolume
            musicVolume = saveManager.musicVolume
            vibrationEnabled = saveManager.vibrationEnabled
        }
    }

    val engine = remember { GameEngine(saveManager, audioEngine) }

    var currentScreen by remember { mutableStateOf(GameScreen.MAIN_MENU) }

    // Dialog & overlay states
    var showHouseMenu by remember { mutableStateOf(false) }
    var showShopMenu by remember { mutableStateOf(false) }
    var showGemdexMenu by remember { mutableStateOf(false) }
    var showSeasonsMenu by remember { mutableStateOf(false) }
    var seasonsInitialTab by remember { mutableStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showSellDialog by remember { mutableStateOf(false) }

    val showSellStandPrompt by engine.showSellStand.collectAsState()
    val tutorialStep by engine.tutorialStep.collectAsState()

    // 60FPS Game Loop using withFrameNanos
    var frameTick by remember { mutableLongStateOf(0L) }
    LaunchedEffect(currentScreen) {
        if (currentScreen == GameScreen.GAMEPLAY) {
            var lastTime = 0L
            while (true) {
                withFrameNanos { time ->
                    if (lastTime != 0L) {
                        val dt = (time - lastTime) / 1_000_000_000f
                        engine.update(dt)
                        frameTick++
                    }
                    lastTime = time
                }
            }
        }
    }

    // Android system back button handler
    BackHandler {
        when {
            showHouseMenu -> showHouseMenu = false
            showShopMenu -> showShopMenu = false
            showGemdexMenu -> showGemdexMenu = false
            showSeasonsMenu -> showSeasonsMenu = false
            showSettingsDialog -> showSettingsDialog = false
            showSellDialog -> showSellDialog = false
            currentScreen == GameScreen.GAMEPLAY -> currentScreen = GameScreen.MAIN_MENU
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        when (currentScreen) {
            GameScreen.MAIN_MENU -> {
                MainMenu(
                    engine = engine,
                    onStartGame = { currentScreen = GameScreen.GAMEPLAY },
                    onOpenHouse = { showHouseMenu = true },
                    onOpenShop = { showShopMenu = true },
                    onOpenGemdex = { showGemdexMenu = true },
                    onOpenSeasons = {
                        seasonsInitialTab = 0
                        showSeasonsMenu = true
                    },
                    onOpenLeaderboard = {
                        seasonsInitialTab = 1
                        showSeasonsMenu = true
                    },
                    onOpenSettings = { showSettingsDialog = true }
                )
            }

            GameScreen.GAMEPLAY -> {
                // 1. GAME RENDERING CANVAS
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Reading frameTick triggers recomposition on every frame
                    val _tick = frameTick
                    GameRenderer.drawWorld(this, engine, size.width, size.height)
                }

                // 2. TUTORIAL BANNER (If in progress)
                if (tutorialStep in 1..10) {
                    TutorialOverlay(
                        engine = engine,
                        step = tutorialStep,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }

                // 3. HUD (Money, Depth, Backpack, House progress, quick action buttons)
                HUDOverlay(
                    engine = engine,
                    onOpenHouse = { showHouseMenu = true },
                    onOpenShop = { showShopMenu = true },
                    onOpenGemdex = { showGemdexMenu = true },
                    onOpenMenu = { currentScreen = GameScreen.MAIN_MENU },
                    onOpenSellDialog = { showSellDialog = true },
                    modifier = Modifier.align(Alignment.TopCenter)
                )

                // 4. TOUCH CONTROLS (D-Pad on left, Action buttons on right)
                ControlsOverlay(
                    engine = engine,
                    opacity = saveManager.controlOpacity,
                    scaleFactor = saveManager.controlScale,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // --- OVERLAYS & MODALS ---
        if (showHouseMenu) {
            HouseMenu(engine = engine, onClose = { showHouseMenu = false })
        }

        if (showShopMenu) {
            ShopMenu(engine = engine, onClose = { showShopMenu = false })
        }

        if (showGemdexMenu) {
            GemdexMenu(engine = engine, onClose = { showGemdexMenu = false })
        }

        if (showSeasonsMenu) {
            SeasonalAndLeaderboardMenu(
                engine = engine,
                initialTab = seasonsInitialTab,
                onClose = { showSeasonsMenu = false }
            )
        }

        if (showSettingsDialog) {
            SettingsDialog(engine = engine, onDismiss = { showSettingsDialog = false })
        }

        if (showSellDialog) {
            SellDialog(engine = engine, onDismiss = { showSellDialog = false })
        }
    }
}
