package com.example.ui.face

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import com.example.model.GuiColorPreset

@Composable
fun FaceCanvas(
    visualState: FaceVisualState,
    colorPreset: GuiColorPreset,
    customColor: Color = Color(0xFF00D9FF),
    faceBrightness: Float = 1.0f,
    glowIntensity: Float = 1.0f,
    circuitIntensity: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    val primaryColor = if (colorPreset == GuiColorPreset.CUSTOM) customColor else colorPreset.primaryColor
    val highlightColor = if (colorPreset == GuiColorPreset.CUSTOM) Color.White else colorPreset.highlightColor

    // Smooth subtle energy pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "face_energy")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_phase"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Layer 1: Near-black background (#020406)
        drawRect(Color(0xFF020406))

        // Layer 2: Dark graphite outer faceplate (#071015)
        drawOuterFaceplate(w, h, cx, cy)

        // Layer 3: Secondary cheek, temple, forehead, and jaw panels (#0C1A20)
        drawSecondaryArmorPlates(w, h, cx, cy)

        // Circuit paths with animated pulse
        drawCircuitPaths(
            w = w,
            h = h,
            cx = cx,
            cy = cy,
            pulsePhase = pulsePhase,
            primaryColor = primaryColor,
            circuitIntensity = circuitIntensity * visualState.circuitPulse,
            faceBrightness = faceBrightness
        )

        // Central Nose Bridge structure
        drawNoseBridge(w, h, cx, cy, primaryColor, faceBrightness)

        // Heroic Jawline and segmented chin plate
        drawHeroicJawline(
            w = w,
            h = h,
            cx = cx,
            cy = cy,
            jawGlow = visualState.jawGlow,
            primaryColor = primaryColor,
            highlightColor = highlightColor,
            glowIntensity = glowIntensity,
            faceBrightness = faceBrightness
        )

        // Cheek and Temple accent vents
        drawCheekAndTempleAccents(
            w = w,
            h = h,
            cx = cx,
            cy = cy,
            cheekGlow = visualState.cheekGlow,
            templeGlow = visualState.templeGlow,
            primaryColor = primaryColor,
            faceBrightness = faceBrightness
        )

        // Mechanical Eyes with eyelids and brows
        drawMechanicalEyes(
            w = w,
            h = h,
            cx = cx,
            cy = cy,
            visualState = visualState,
            primaryColor = primaryColor,
            highlightColor = highlightColor,
            glowIntensity = glowIntensity,
            faceBrightness = faceBrightness
        )

        // Animated Brows
        drawBrows(
            w = w,
            h = h,
            cx = cx,
            cy = cy,
            visualState = visualState,
            primaryColor = primaryColor,
            highlightColor = highlightColor,
            faceBrightness = faceBrightness
        )

        // Expressive Mouth / Voice Aperture
        drawMouthAperture(
            w = w,
            h = h,
            cx = cx,
            cy = cy,
            visualState = visualState,
            primaryColor = primaryColor,
            highlightColor = highlightColor,
            glowIntensity = glowIntensity,
            faceBrightness = faceBrightness
        )

        // Layer 6: Very faint high-tech scan lines
        drawScanLines(w, h)
    }
}

private fun DrawScope.drawOuterFaceplate(w: Float, h: Float, cx: Float, cy: Float) {
    val facePath = Path().apply {
        // Forehead crest top center
        moveTo(cx, cy - h * 0.45f)
        lineTo(cx + w * 0.28f, cy - h * 0.43f)
        lineTo(cx + w * 0.44f, cy - h * 0.32f) // Upper temple
        lineTo(cx + w * 0.46f, cy - h * 0.10f) // Mid temple
        lineTo(cx + w * 0.42f, cy + h * 0.12f) // Cheek flare
        lineTo(cx + w * 0.30f, cy + h * 0.30f) // Upper jaw
        lineTo(cx + w * 0.16f, cy + h * 0.42f) // Chin taper
        lineTo(cx + w * 0.08f, cy + h * 0.44f) // Chin flat right
        lineTo(cx - w * 0.08f, cy + h * 0.44f) // Chin flat left
        lineTo(cx - w * 0.16f, cy + h * 0.42f) // Chin taper left
        lineTo(cx - w * 0.30f, cy + h * 0.30f) // Upper jaw left
        lineTo(cx - w * 0.42f, cy + h * 0.12f) // Cheek flare left
        lineTo(cx - w * 0.46f, cy - h * 0.10f) // Mid temple left
        lineTo(cx - w * 0.44f, cy - h * 0.32f) // Upper temple left
        lineTo(cx - w * 0.28f, cy - h * 0.43f)
        close()
    }

    // Outer faceplate base
    drawPath(facePath, Color(0xFF071015))
    // Outer metallic bevel border
    drawPath(facePath, Color(0xFF14252E), style = Stroke(width = 3.5f))
}

private fun DrawScope.drawSecondaryArmorPlates(w: Float, h: Float, cx: Float, cy: Float) {
    val plateColor = Color(0xFF0C1A20)
    val plateBorder = Color(0xFF1A3340)

    // Forehead V-crest armor plate
    val crestPath = Path().apply {
        moveTo(cx, cy - h * 0.41f)
        lineTo(cx + w * 0.18f, cy - h * 0.39f)
        lineTo(cx + w * 0.12f, cy - h * 0.28f)
        lineTo(cx, cy - h * 0.24f)
        lineTo(cx - w * 0.12f, cy - h * 0.28f)
        lineTo(cx - w * 0.18f, cy - h * 0.39f)
        close()
    }
    drawPath(crestPath, plateColor)
    drawPath(crestPath, plateBorder, style = Stroke(width = 2f))

    // Left and Right Temple Armor Plates
    val leftTemple = Path().apply {
        moveTo(cx - w * 0.42f, cy - h * 0.28f)
        lineTo(cx - w * 0.26f, cy - h * 0.26f)
        lineTo(cx - w * 0.28f, cy - h * 0.12f)
        lineTo(cx - w * 0.43f, cy - h * 0.10f)
        close()
    }
    val rightTemple = Path().apply {
        moveTo(cx + w * 0.42f, cy - h * 0.28f)
        lineTo(cx + w * 0.26f, cy - h * 0.26f)
        lineTo(cx + w * 0.28f, cy - h * 0.12f)
        lineTo(cx + w * 0.43f, cy - h * 0.10f)
        close()
    }
    drawPath(leftTemple, plateColor)
    drawPath(leftTemple, plateBorder, style = Stroke(width = 2f))
    drawPath(rightTemple, plateColor)
    drawPath(rightTemple, plateBorder, style = Stroke(width = 2f))

    // Lower-face cheek side armor panels
    val leftCheekPlate = Path().apply {
        moveTo(cx - w * 0.40f, cy + h * 0.08f)
        lineTo(cx - w * 0.24f, cy + h * 0.12f)
        lineTo(cx - w * 0.22f, cy + h * 0.26f)
        lineTo(cx - w * 0.32f, cy + h * 0.28f)
        close()
    }
    val rightCheekPlate = Path().apply {
        moveTo(cx + w * 0.40f, cy + h * 0.08f)
        lineTo(cx + w * 0.24f, cy + h * 0.12f)
        lineTo(cx + w * 0.22f, cy + h * 0.26f)
        lineTo(cx + w * 0.32f, cy + h * 0.28f)
        close()
    }
    drawPath(leftCheekPlate, plateColor)
    drawPath(leftCheekPlate, plateBorder, style = Stroke(width = 2f))
    drawPath(rightCheekPlate, plateColor)
    drawPath(rightCheekPlate, plateBorder, style = Stroke(width = 2f))
}

private fun DrawScope.drawCircuitPaths(
    w: Float,
    h: Float,
    cx: Float,
    cy: Float,
    pulsePhase: Float,
    primaryColor: Color,
    circuitIntensity: Float,
    faceBrightness: Float
) {
    val alpha = (0.35f * circuitIntensity * faceBrightness).coerceIn(0.08f, 0.9f)
    val glowColor = primaryColor.copy(alpha = alpha)

    // Circuit tracks across forehead
    val leftForeheadCircuit = Path().apply {
        moveTo(cx - w * 0.10f, cy - h * 0.37f)
        lineTo(cx - w * 0.22f, cy - h * 0.37f)
        lineTo(cx - w * 0.28f, cy - h * 0.32f)
        lineTo(cx - w * 0.28f, cy - h * 0.20f)
    }
    val rightForeheadCircuit = Path().apply {
        moveTo(cx + w * 0.10f, cy - h * 0.37f)
        lineTo(cx + w * 0.22f, cy - h * 0.37f)
        lineTo(cx + w * 0.28f, cy - h * 0.32f)
        lineTo(cx + w * 0.28f, cy - h * 0.20f)
    }

    // Lower cheek circuit tracks
    val leftLowerCircuit = Path().apply {
        moveTo(cx - w * 0.22f, cy + h * 0.24f)
        lineTo(cx - w * 0.26f, cy + h * 0.30f)
        lineTo(cx - w * 0.16f, cy + h * 0.36f)
    }
    val rightLowerCircuit = Path().apply {
        moveTo(cx + w * 0.22f, cy + h * 0.24f)
        lineTo(cx + w * 0.26f, cy + h * 0.30f)
        lineTo(cx + w * 0.16f, cy + h * 0.36f)
    }

    val stroke = Stroke(
        width = 2.2f,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 10f), pulsePhase * 28f)
    )

    drawPath(leftForeheadCircuit, glowColor, style = stroke)
    drawPath(rightForeheadCircuit, glowColor, style = stroke)
    drawPath(leftLowerCircuit, glowColor, style = stroke)
    drawPath(rightLowerCircuit, glowColor, style = stroke)
}

private fun DrawScope.drawNoseBridge(w: Float, h: Float, cx: Float, cy: Float, primaryColor: Color, faceBrightness: Float) {
    val bridgePath = Path().apply {
        moveTo(cx - w * 0.035f, cy - h * 0.08f)
        lineTo(cx + w * 0.035f, cy - h * 0.08f)
        lineTo(cx + w * 0.045f, cy + h * 0.06f)
        lineTo(cx, cy + h * 0.10f)
        lineTo(cx - w * 0.045f, cy + h * 0.06f)
        close()
    }
    drawPath(bridgePath, Color(0xFF0F222B))
    drawPath(bridgePath, Color(0xFF1B3B4B), style = Stroke(width = 1.8f))

    // Nose center glowing indicator line
    val lineAlpha = (0.4f * faceBrightness).coerceIn(0.1f, 0.9f)
    drawLine(
        color = primaryColor.copy(alpha = lineAlpha),
        start = Offset(cx, cy - h * 0.05f),
        end = Offset(cx, cy + h * 0.06f),
        strokeWidth = 2f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawHeroicJawline(
    w: Float,
    h: Float,
    cx: Float,
    cy: Float,
    jawGlow: Float,
    primaryColor: Color,
    highlightColor: Color,
    glowIntensity: Float,
    faceBrightness: Float
) {
    val jawAlpha = (jawGlow * glowIntensity * faceBrightness).coerceIn(0.12f, 1.0f)
    val glowColor = primaryColor.copy(alpha = jawAlpha)
    val highlight = highlightColor.copy(alpha = jawAlpha.coerceAtMost(0.9f))

    // Broad heroic jaw begins beneath cheek structures and tapers to clean angular chin
    val jawPath = Path().apply {
        moveTo(cx - w * 0.36f, cy + h * 0.20f)
        lineTo(cx - w * 0.26f, cy + h * 0.32f)
        lineTo(cx - w * 0.14f, cy + h * 0.41f)
        lineTo(cx - w * 0.07f, cy + h * 0.43f)
        lineTo(cx + w * 0.07f, cy + h * 0.43f)
        lineTo(cx + w * 0.14f, cy + h * 0.41f)
        lineTo(cx + w * 0.26f, cy + h * 0.32f)
        lineTo(cx + w * 0.36f, cy + h * 0.20f)
    }

    // Outer soft glow bloom behind jaw
    if (jawGlow > 0.4f) {
        drawPath(
            jawPath,
            glowColor.copy(alpha = (jawAlpha * 0.4f).coerceIn(0.05f, 0.5f)),
            style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }

    // Main heroic jaw line
    drawPath(
        jawPath,
        glowColor,
        style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Highlight core on chin
    drawLine(
        color = highlight,
        start = Offset(cx - w * 0.08f, cy + h * 0.43f),
        end = Offset(cx + w * 0.08f, cy + h * 0.43f),
        strokeWidth = 3f,
        cap = StrokeCap.Round
    )

    // Segmented central chin plate
    val chinPlate = Path().apply {
        moveTo(cx - w * 0.07f, cy + h * 0.38f)
        lineTo(cx + w * 0.07f, cy + h * 0.38f)
        lineTo(cx + w * 0.05f, cy + h * 0.42f)
        lineTo(cx - w * 0.05f, cy + h * 0.42f)
        close()
    }
    drawPath(chinPlate, Color(0xFF10232C))
    drawPath(chinPlate, glowColor.copy(alpha = jawAlpha * 0.8f), style = Stroke(width = 1.5f))
}

private fun DrawScope.drawCheekAndTempleAccents(
    w: Float,
    h: Float,
    cx: Float,
    cy: Float,
    cheekGlow: Float,
    templeGlow: Float,
    primaryColor: Color,
    faceBrightness: Float
) {
    val cheekAlpha = (cheekGlow * faceBrightness).coerceIn(0.1f, 0.95f)
    val templeAlpha = (templeGlow * faceBrightness).coerceIn(0.1f, 0.95f)

    // Temple vents
    val leftTempleBar = Offset(cx - w * 0.36f, cy - h * 0.18f)
    val rightTempleBar = Offset(cx + w * 0.36f, cy - h * 0.18f)
    val barSize = Size(w * 0.045f, h * 0.015f)

    drawRoundRect(
        color = primaryColor.copy(alpha = templeAlpha),
        topLeft = leftTempleBar,
        size = barSize,
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = primaryColor.copy(alpha = templeAlpha),
        topLeft = rightTempleBar,
        size = barSize,
        cornerRadius = CornerRadius(4f, 4f)
    )

    // Cheek vents (3 subtle horizontal cyber slats)
    for (i in 0..2) {
        val yOffset = cy + h * (0.04f + i * 0.024f)
        val slatW = w * (0.055f - i * 0.008f)
        drawRoundRect(
            color = primaryColor.copy(alpha = cheekAlpha * (0.9f - i * 0.2f)),
            topLeft = Offset(cx - w * 0.34f, yOffset),
            size = Size(slatW, h * 0.006f),
            cornerRadius = CornerRadius(3f, 3f)
        )
        drawRoundRect(
            color = primaryColor.copy(alpha = cheekAlpha * (0.9f - i * 0.2f)),
            topLeft = Offset(cx + w * 0.34f - slatW, yOffset),
            size = Size(slatW, h * 0.006f),
            cornerRadius = CornerRadius(3f, 3f)
        )
    }
}

private fun DrawScope.drawMechanicalEyes(
    w: Float,
    h: Float,
    cx: Float,
    cy: Float,
    visualState: FaceVisualState,
    primaryColor: Color,
    highlightColor: Color,
    glowIntensity: Float,
    faceBrightness: Float
) {
    val eyeWidth = w * 0.26f * visualState.eyeScaleX
    val eyeHeight = h * 0.12f * visualState.eyeScaleY
    val leftEyeCenter = Offset(cx - w * 0.22f, cy - h * 0.06f)
    val rightEyeCenter = Offset(cx + w * 0.22f, cy - h * 0.06f)

    // Draw Left Eye
    drawSingleEye(
        eyeCenter = leftEyeCenter,
        eyeW = eyeWidth,
        eyeH = eyeHeight,
        upperLid = visualState.leftUpperLid,
        lowerLid = visualState.lowerLid,
        lookX = visualState.lookX,
        lookY = visualState.lookY,
        isLoveEyes = visualState.isLoveEyes,
        primaryColor = primaryColor,
        highlightColor = highlightColor,
        glowIntensity = glowIntensity,
        faceBrightness = faceBrightness
    )

    // Draw Right Eye
    drawSingleEye(
        eyeCenter = rightEyeCenter,
        eyeW = eyeWidth,
        eyeH = eyeHeight,
        upperLid = visualState.rightUpperLid,
        lowerLid = visualState.lowerLid,
        lookX = visualState.lookX,
        lookY = visualState.lookY,
        isLoveEyes = visualState.isLoveEyes,
        primaryColor = primaryColor,
        highlightColor = highlightColor,
        glowIntensity = glowIntensity,
        faceBrightness = faceBrightness
    )
}

private fun DrawScope.drawSingleEye(
    eyeCenter: Offset,
    eyeW: Float,
    eyeH: Float,
    upperLid: Float,
    lowerLid: Float,
    lookX: Float,
    lookY: Float,
    isLoveEyes: Boolean,
    primaryColor: Color,
    highlightColor: Color,
    glowIntensity: Float,
    faceBrightness: Float
) {
    val ox = eyeCenter.x
    val oy = eyeCenter.y
    val hw = eyeW / 2f
    val hh = eyeH / 2f

    // Angular hexagonal eye socket
    val socketPath = Path().apply {
        moveTo(ox - hw * 0.85f, oy - hh)
        lineTo(ox + hw * 0.85f, oy - hh)
        lineTo(ox + hw, oy)
        lineTo(ox + hw * 0.75f, oy + hh)
        lineTo(ox - hw * 0.75f, oy + hh)
        lineTo(ox - hw, oy)
        close()
    }

    // Outer dark housing frame
    drawPath(socketPath, Color(0xFF040A0E))
    drawPath(socketPath, Color(0xFF162D38), style = Stroke(width = 3.5f))

    // Clip rendering inside the mechanical socket
    clipPath(socketPath) {
        if (isLoveEyes) {
            // Glowing neon heart for LOVE expression
            val heartPath = Path().apply {
                val scale = eyeW * 0.45f
                moveTo(ox, oy + scale * 0.5f)
                cubicTo(ox - scale, oy, ox - scale, oy - scale * 0.7f, ox, oy - scale * 0.2f)
                cubicTo(ox + scale, oy - scale * 0.7f, ox + scale, oy, ox, oy + scale * 0.5f)
                close()
            }
            drawPath(heartPath, Color(0xFFFF4081))
            drawPath(heartPath, Color.White, style = Stroke(width = 2.5f))
        } else {
            // Pupil & Iris position based on look direction
            val maxShiftX = hw * 0.38f
            val maxShiftY = hh * 0.40f
            val pupilCenter = Offset(ox + lookX * maxShiftX, oy + lookY * maxShiftY)

            // Iris outer glow circle
            val glowRadius = hw * 0.75f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = (0.85f * faceBrightness).coerceIn(0.1f, 1f)),
                        primaryColor.copy(alpha = (0.25f * faceBrightness).coerceIn(0.05f, 0.4f)),
                        Color.Transparent
                    ),
                    center = pupilCenter,
                    radius = glowRadius
                ),
                radius = glowRadius,
                center = pupilCenter
            )

            // Mechanical segmented iris ring
            drawCircle(
                color = primaryColor.copy(alpha = (0.95f * faceBrightness).coerceIn(0.2f, 1f)),
                radius = hw * 0.48f,
                center = pupilCenter,
                style = Stroke(width = 4.5f)
            )

            // High-tech pupil core
            val coreRadius = hw * 0.26f
            drawCircle(
                color = highlightColor.copy(alpha = (0.95f * faceBrightness).coerceIn(0.2f, 1f)),
                radius = coreRadius,
                center = pupilCenter
            )

            // Specular reflection highlight
            drawCircle(
                color = Color.White,
                radius = coreRadius * 0.4f,
                center = Offset(pupilCenter.x - coreRadius * 0.35f, pupilCenter.y - coreRadius * 0.35f)
            )
        }

        // Upper Eyelid armor panel (sweeps down)
        if (upperLid > 0.02f) {
            val lidHeight = (eyeH * 1.4f) * upperLid.coerceIn(0f, 1f)
            val upperLidPath = Path().apply {
                moveTo(ox - hw * 1.1f, oy - hh * 1.2f)
                lineTo(ox + hw * 1.1f, oy - hh * 1.2f)
                lineTo(ox + hw * 1.1f, (oy - hh) + lidHeight)
                lineTo(ox - hw * 1.1f, (oy - hh) + lidHeight)
                close()
            }
            drawPath(upperLidPath, Color(0xFF071015))
            // Eyelid metal edge
            drawLine(
                color = Color(0xFF1E3A47),
                start = Offset(ox - hw * 1.1f, (oy - hh) + lidHeight),
                end = Offset(ox + hw * 1.1f, (oy - hh) + lidHeight),
                strokeWidth = 3f
            )
        }

        // Lower Eyelid armor panel (sweeps up)
        if (lowerLid > 0.05f) {
            val lowerHeight = (eyeH * 0.9f) * lowerLid.coerceIn(0f, 1f)
            val lowerLidPath = Path().apply {
                moveTo(ox - hw * 1.1f, oy + hh * 1.2f)
                lineTo(ox + hw * 1.1f, oy + hh * 1.2f)
                lineTo(ox + hw * 1.1f, (oy + hh) - lowerHeight)
                lineTo(ox - hw * 1.1f, (oy + hh) - lowerHeight)
                close()
            }
            drawPath(lowerLidPath, Color(0xFF071015))
            drawLine(
                color = Color(0xFF1E3A47),
                start = Offset(ox - hw * 1.1f, (oy + hh) - lowerHeight),
                end = Offset(ox + hw * 1.1f, (oy + hh) - lowerHeight),
                strokeWidth = 2.5f
            )
        }
    }
}

private fun DrawScope.drawBrows(
    w: Float,
    h: Float,
    cx: Float,
    cy: Float,
    visualState: FaceVisualState,
    primaryColor: Color,
    highlightColor: Color,
    faceBrightness: Float
) {
    val browWidth = w * 0.24f
    val baseBrowY = cy - h * 0.16f
    val leftCenterX = cx - w * 0.22f
    val rightCenterX = cx + w * 0.22f

    // Left Brow
    val leftY = baseBrowY - (visualState.leftBrowHeight * h * 0.045f)
    val leftTiltShift = visualState.leftBrowTilt * h * 0.025f

    val leftBrowPath = Path().apply {
        // Inner brow tip to outer brow tip
        val innerX = leftCenterX + browWidth * 0.48f
        val innerY = leftY - leftTiltShift
        val outerX = leftCenterX - browWidth * 0.48f
        val outerY = leftY + leftTiltShift

        moveTo(outerX, outerY)
        lineTo(leftCenterX, leftY - h * 0.008f)
        lineTo(innerX, innerY)
        lineTo(innerX, innerY - h * 0.012f)
        lineTo(leftCenterX, leftY - h * 0.020f)
        lineTo(outerX, outerY - h * 0.012f)
        close()
    }

    // Right Brow
    val rightY = baseBrowY - (visualState.rightBrowHeight * h * 0.045f)
    val rightTiltShift = visualState.rightBrowTilt * h * 0.025f

    val rightBrowPath = Path().apply {
        val innerX = rightCenterX - browWidth * 0.48f
        val innerY = rightY - rightTiltShift
        val outerX = rightCenterX + browWidth * 0.48f
        val outerY = rightY + rightTiltShift

        moveTo(innerX, innerY)
        lineTo(rightCenterX, rightY - h * 0.008f)
        lineTo(outerX, outerY)
        lineTo(outerX, outerY - h * 0.012f)
        lineTo(rightCenterX, rightY - h * 0.020f)
        lineTo(innerX, innerY - h * 0.012f)
        close()
    }

    val browAlpha = (0.95f * faceBrightness).coerceIn(0.2f, 1f)
    drawPath(leftBrowPath, primaryColor.copy(alpha = browAlpha))
    drawPath(leftBrowPath, highlightColor.copy(alpha = browAlpha), style = Stroke(width = 1.5f))

    drawPath(rightBrowPath, primaryColor.copy(alpha = browAlpha))
    drawPath(rightBrowPath, highlightColor.copy(alpha = browAlpha), style = Stroke(width = 1.5f))
}

private fun DrawScope.drawMouthAperture(
    w: Float,
    h: Float,
    cx: Float,
    cy: Float,
    visualState: FaceVisualState,
    primaryColor: Color,
    highlightColor: Color,
    glowIntensity: Float,
    faceBrightness: Float
) {
    val mouthY = cy + h * 0.22f
    val baseW = w * 0.28f * visualState.mouthWidth.coerceIn(0.2f, 1.2f)
    val baseH = h * 0.045f * visualState.mouthHeight.coerceIn(0.1f, 1.5f)

    val halfW = baseW / 2f
    val halfH = baseH / 2f

    // Outer aperture metallic recess
    val apertureRect = RoundRect(
        rect = Rect(
            left = cx - halfW,
            top = mouthY - halfH,
            right = cx + halfW,
            bottom = mouthY + halfH
        ),
        cornerRadius = CornerRadius(halfH * 0.8f, halfH * 0.8f)
    )

    val recessPath = Path().apply { addRoundRect(apertureRect) }
    drawPath(recessPath, Color(0xFF03070A))
    drawPath(recessPath, Color(0xFF14242D), style = Stroke(width = 2.5f))

    // Internal illuminated audio ribs / voice core
    val mouthAlpha = (0.9f * glowIntensity * faceBrightness).coerceIn(0.2f, 1f)
    val ribCount = 7
    val ribSpacing = baseW / (ribCount + 1)

    for (i in 1..ribCount) {
        val rx = (cx - halfW) + i * ribSpacing
        val centerDist = Math.abs(i - (ribCount + 1) / 2f) / ((ribCount + 1) / 2f)
        val ribH = (halfH * 1.5f * (1f - centerDist * 0.45f)).coerceAtLeast(3f)

        drawLine(
            color = if (i == (ribCount + 1) / 2) highlightColor else primaryColor.copy(alpha = mouthAlpha),
            start = Offset(rx, mouthY - ribH / 2f),
            end = Offset(rx, mouthY + ribH / 2f),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawScanLines(w: Float, h: Float) {
    val scanColor = Color(0x0800D9FF)
    val step = 10f
    var y = 0f
    while (y < h) {
        drawLine(
            color = scanColor,
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 1f
        )
        y += step
    }
}
