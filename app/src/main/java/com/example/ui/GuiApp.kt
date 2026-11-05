package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.WindowManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.assistant.commands.CommandParser
import com.example.assistant.commands.CommandRouter
import com.example.assistant.conversation.ConversationManager
import com.example.assistant.speech.SpeechInputManager
import com.example.assistant.state.AssistantStateMachine
import com.example.assistant.voice.VoiceOutputManager
import com.example.automation.ActionRegistry
import com.example.automation.AppLauncher
import com.example.automation.CastingManager
import com.example.automation.MediaCommandManager
import com.example.automation.shizuku.ShizukuManager
import com.example.data.AutomationHistoryRepository
import com.example.data.GuiSettings
import com.example.data.SettingsRepository
import com.example.model.AssistantState
import com.example.model.GuiExpression
import com.example.sensors.BatteryMonitor
import com.example.sensors.MotionSensorManager
import com.example.sensors.SoundIntensity
import com.example.sensors.SoundReactionManager
import com.example.sensors.vision.VisionManager
import com.example.ui.commandcenter.CommandCenterSheet
import com.example.ui.console.OwnerConsoleManager
import com.example.ui.console.OwnerConsoleScreen
import com.example.ui.expressionlab.ExpressionLabScreen
import com.example.ui.face.FaceViewModel
import com.example.ui.face.GuiFaceScreen
import com.example.ui.history.AutomationHistoryScreen
import com.example.ui.permissions.PermissionCenterScreen
import com.example.ui.quickcontrols.QuickControlsSheet
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.GuiTheme
import kotlinx.coroutines.launch

enum class ScreenState {
    FACE,
    SETTINGS,
    EXPRESSION_LAB,
    HISTORY,
    PERMISSIONS,
    OWNER_CONSOLE
}

@Composable
fun GuiApp(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // Core Repositories & Subsystems
    val settingsRepo = remember { SettingsRepository(context) }
    val historyRepo = remember { AutomationHistoryRepository() }
    val stateMachine = remember { AssistantStateMachine(scope) }
    val speechInput = remember { SpeechInputManager(context) }
    val voiceOutput = remember { VoiceOutputManager(context) }
    val appLauncher = remember { AppLauncher(context) }
    val mediaCommandManager = remember { MediaCommandManager(context) }
    val castingManager = remember { CastingManager(context) }
    val actionRegistry = remember { ActionRegistry() }
    val commandParser = remember { CommandParser() }
    val commandRouter = remember {
        CommandRouter(context, appLauncher, mediaCommandManager, castingManager, actionRegistry)
    }

    val conversationManager = remember {
        ConversationManager(
            scope = scope,
            stateMachine = stateMachine,
            speechInput = speechInput,
            voiceOutput = voiceOutput,
            commandParser = commandParser,
            commandRouter = commandRouter,
            historyRepo = historyRepo,
            settingsRepo = settingsRepo
        )
    }

    val shizukuManager = remember { ShizukuManager(context).apply { initialize() } }
    val consoleManager = remember { OwnerConsoleManager(scope, shizukuManager) }

    // Sensors
    val batteryMonitor = remember { BatteryMonitor(context) }
    val motionSensor = remember { MotionSensorManager(context) }
    val soundReactionManager = remember { SoundReactionManager(scope) }
    val visionManager = remember { VisionManager(context) }

    // ViewModel
    val faceViewModel = remember {
        FaceViewModel(stateMachine, conversationManager, settingsRepo)
    }

    // State collections
    val settings by settingsRepo.settingsFlow.collectAsState(initial = GuiSettings())
    val recentHistory by historyRepo.history.collectAsState()
    val isVisionActive by visionManager.isVisionActive.collectAsState()
    val ttsStatus by voiceOutput.currentBackendStatus.collectAsState()

    // Navigation and Sheet visibility
    var currentScreen by remember { mutableStateOf(ScreenState.FACE) }
    var isQuickControlsOpen by remember { mutableStateOf(false) }
    var isCommandCenterOpen by remember { mutableStateOf(false) }

    // Window Flags (Keep Screen Awake for Exo Suit mount)
    DisposableEffect(settings.keepScreenAwake) {
        val activity = context as? Activity
        if (settings.keepScreenAwake) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Sensor Listeners Lifecycle
    DisposableEffect(Unit) {
        batteryMonitor.start()
        motionSensor.start()

        batteryMonitor.onChargingStarted = {
            stateMachine.setTemporaryExpression(GuiExpression.EXCITED, 2500)
            faceViewModel.triggerHeroicJawFlare()
        }
        batteryMonitor.onBatteryLow = {
            stateMachine.setTemporaryExpression(GuiExpression.SLEEPY, 3000)
        }
        batteryMonitor.onBatteryFull = {
            stateMachine.setTemporaryExpression(GuiExpression.HEROIC, 3000)
            faceViewModel.triggerHeroicJawFlare()
        }

        motionSensor.onSuddenMovement = {
            stateMachine.setTemporaryExpression(GuiExpression.SURPRISED, 1500)
        }

        soundReactionManager.onSoundIntensityDetected = { intensity ->
            if (settings.soundReactions && stateMachine.currentState.value == AssistantState.IDLE) {
                when (intensity) {
                    SoundIntensity.SUDDEN -> stateMachine.setTemporaryExpression(GuiExpression.SURPRISED, 1200)
                    SoundIntensity.LOUD -> stateMachine.setTemporaryExpression(GuiExpression.ATTENTIVE, 1000)
                    SoundIntensity.NOTICEABLE -> stateMachine.setTemporaryExpression(GuiExpression.CURIOUS, 900)
                    else -> {}
                }
            }
        }

        conversationManager.onOpenOwnerConsoleRequest = {
            currentScreen = ScreenState.OWNER_CONSOLE
        }

        onDispose {
            batteryMonitor.stop()
            motionSensor.stop()
            soundReactionManager.stop()
            visionManager.stopVision()
            conversationManager.release()
            shizukuManager.release()
        }
    }

    // Sound reaction tracking toggle
    LaunchedEffect(settings.soundReactions) {
        if (settings.soundReactions) {
            soundReactionManager.start(true)
        } else {
            soundReactionManager.stop()
        }
    }

    // Vision mode toggle
    LaunchedEffect(settings.visionMode) {
        if (settings.visionMode) {
            visionManager.startVision(lifecycleOwner, true)
        } else {
            visionManager.stopVision()
        }
    }

    GuiTheme {
        when (currentScreen) {
            ScreenState.FACE -> {
                GuiFaceScreen(
                    viewModel = faceViewModel,
                    settings = settings,
                    isVisionActive = isVisionActive,
                    onOpenQuickControls = { isQuickControlsOpen = true },
                    onOpenSettings = { currentScreen = ScreenState.SETTINGS },
                    onOpenCommandCenter = { isCommandCenterOpen = true },
                    onOpenExpressionLab = { currentScreen = ScreenState.EXPRESSION_LAB },
                    onOpenHistory = { currentScreen = ScreenState.HISTORY },
                    onOpenOwnerConsole = { currentScreen = ScreenState.OWNER_CONSOLE },
                    onFirstRunDone = { scope.launch { settingsRepo.setFirstRunDone() } },
                    modifier = modifier.fillMaxSize()
                )

                // Quick Controls Sheet
                QuickControlsSheet(
                    isOpen = isQuickControlsOpen,
                    settings = settings,
                    onDismiss = { isQuickControlsOpen = false },
                    onListenClick = { conversationManager.triggerManualListening() },
                    onStopClick = { conversationManager.emergencyStop() },
                    onTogglePrivacy = { enabled ->
                        stateMachine.togglePrivacyMode()
                        scope.launch { settingsRepo.updatePrivacyMode(enabled) }
                    },
                    onSleepClick = {
                        stateMachine.transitionTo(AssistantState.SLEEPING)
                    },
                    onBrightnessChange = { scope.launch { settingsRepo.updateBrightness(it) } },
                    onVolumeChange = { scope.launch { settingsRepo.updateVoiceVolume(it) } },
                    onToggleSoundReactions = { scope.launch { settingsRepo.updateSoundReactions(it) } },
                    onToggleVisionMode = { scope.launch { settingsRepo.updateVisionMode(it) } },
                    onOpenFullSettings = { currentScreen = ScreenState.SETTINGS }
                )

                // Command Center Sheet
                CommandCenterSheet(
                    isOpen = isCommandCenterOpen,
                    recentCommands = recentHistory,
                    onDismiss = { isCommandCenterOpen = false },
                    onListenClick = { conversationManager.triggerManualListening() },
                    onOpenAppClick = { appName ->
                        appLauncher.launchAppByName(appName)
                    },
                    onWebSearchClick = { query ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$query")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    },
                    onMediaPlayPauseClick = {
                        mediaCommandManager.handleMediaCommand("play")
                    },
                    onCastingClick = {
                        castingManager.openCastingControls()
                    },
                    onAutomationsClick = {
                        actionRegistry.actions.firstOrNull()?.execute?.invoke(context)
                    },
                    onExpressionLabClick = {
                        currentScreen = ScreenState.EXPRESSION_LAB
                    },
                    onOwnerConsoleClick = {
                        currentScreen = ScreenState.OWNER_CONSOLE
                    }
                )
            }

            ScreenState.SETTINGS -> {
                SettingsScreen(
                    settings = settings,
                    settingsRepo = settingsRepo,
                    ttsBackendStatus = ttsStatus,
                    onBack = { currentScreen = ScreenState.FACE },
                    onOpenPermissionCenter = { currentScreen = ScreenState.PERMISSIONS },
                    onOpenOwnerConsole = { currentScreen = ScreenState.OWNER_CONSOLE },
                    onOpenHistory = { currentScreen = ScreenState.HISTORY },
                    onOpenExpressionLab = { currentScreen = ScreenState.EXPRESSION_LAB }
                )
            }

            ScreenState.EXPRESSION_LAB -> {
                ExpressionLabScreen(
                    viewModel = faceViewModel,
                    onBack = { currentScreen = ScreenState.FACE }
                )
            }

            ScreenState.HISTORY -> {
                AutomationHistoryScreen(
                    historyRepo = historyRepo,
                    onBack = { currentScreen = ScreenState.FACE }
                )
            }

            ScreenState.PERMISSIONS -> {
                PermissionCenterScreen(
                    shizukuManager = shizukuManager,
                    onBack = { currentScreen = ScreenState.SETTINGS }
                )
            }

            ScreenState.OWNER_CONSOLE -> {
                OwnerConsoleScreen(
                    consoleManager = consoleManager,
                    shizukuManager = shizukuManager,
                    onBack = { currentScreen = ScreenState.FACE }
                )
            }
        }
    }
}
