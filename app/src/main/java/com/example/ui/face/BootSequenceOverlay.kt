package com.example.ui.face

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GuiExpression
import kotlinx.coroutines.delay

@Composable
fun BootSequenceOverlay(
    isFirstRun: Boolean,
    onBootFinished: () -> Unit,
    setTemporaryExpression: (GuiExpression, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isFirstRun) return

    var bootStep by remember { mutableIntStateOf(0) }
    var showTutorial by remember { mutableStateOf(false) }
    val lineAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Step 1: Black screen
        bootStep = 1
        delay(600)

        // Step 2: Thin cyan power line appears
        bootStep = 2
        lineAlpha.animateTo(1f, animationSpec = tween(700))
        delay(400)

        // Step 3: Faceplate circuits illuminate
        bootStep = 3
        delay(600)

        // Step 4 & 5: Jaw & chin plate illuminate
        bootStep = 4
        delay(700)

        // Step 6 & 7: Left and right eyes power on
        bootStep = 5
        setTemporaryExpression(GuiExpression.FOCUSED, 1200)
        delay(800)

        // Step 8: Brows calibrate
        bootStep = 6
        setTemporaryExpression(GuiExpression.SURPRISED, 800)
        delay(800)

        // Step 9 & 10: Look left then right
        bootStep = 7
        setTemporaryExpression(GuiExpression.CURIOUS, 1200)
        delay(1000)

        // Step 11: Blink
        bootStep = 8
        setTemporaryExpression(GuiExpression.ATTENTIVE, 800)
        delay(800)

        // Step 12 & 13: Mouth aperture activates & ready
        bootStep = 9
        setTemporaryExpression(GuiExpression.HEROIC, 2000)
        delay(1200)

        // Step 14: Gesture tutorial
        showTutorial = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (bootStep < 8) Color.Black.copy(alpha = 0.95f) else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        if (bootStep in 1..8) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "GUI CORE SYSTEM INIT",
                    color = Color(0xFF00D9FF),
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = when (bootStep) {
                        1 -> "Powering main capacitor..."
                        2 -> "Energizing core plasma rail..."
                        3 -> "Illuminating faceplate circuits..."
                        4 -> "Calibrating heroic jawline and chin plate..."
                        5 -> "Activating optical mechanical eyes..."
                        6 -> "Calibrating dynamic brows..."
                        7 -> "Running spatial ocular diagnostics..."
                        8 -> "Voice aperture online."
                        else -> "MOBI Exo Suit link established."
                    },
                    color = Color(0xFF8CF3FF),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            }
        }

        AnimatedVisibility(
            visible = showTutorial,
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(300))
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xEE09141B)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MEET GUI",
                        color = Color(0xFF00D9FF),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Spelled GUI, pronounced \"guy\".\nRobotic companion living inside the MOBI Exo Suit.",
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "• Wake Words: \"GUI\", \"Dude\", \"Broham\"\n" +
                               "• Interruption: Speak anytime or tap to stop\n" +
                               "• Tap Face: Jaw (heroic flare), Eyes (wink), Nose (focus)\n" +
                               "• Gestures:\n" +
                               "  ↓ Top swipe: Quick Controls\n" +
                               "  ↑ Bottom swipe: Command Center\n" +
                               "  ← Left swipe: Expression Lab\n" +
                               "  → Right swipe: History\n" +
                               "  2-Finger Tap: Emergency Stop\n" +
                               "  3-Finger Long Press: Owner Console",
                        color = Color(0xFFB0D0DC),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = onBootFinished,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D9FF)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_gui_button")
                    ) {
                        Text(
                            text = "START GUI",
                            color = Color(0xFF020406),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
