package com.example.model

import androidx.compose.ui.graphics.Color

enum class GuiColorPreset(
    val label: String,
    val primaryColor: Color,
    val highlightColor: Color,
    val hexString: String
) {
    GUI_CYAN("GUI Cyan", Color(0xFF00D9FF), Color(0xFF8CF3FF), "#00D9FF"),
    ELECTRIC_BLUE("Electric Blue", Color(0xFF2979FF), Color(0xFF82B1FF), "#2979FF"),
    REACTOR_GREEN("Reactor Green", Color(0xFF00E676), Color(0xFFB9F6CA), "#00E676"),
    VIOLET("Violet", Color(0xFFD500F9), Color(0xFFEA80FC), "#D500F9"),
    AMBER("Amber", Color(0xFFFFAB00), Color(0xFFFFE57F), "#FFAB00"),
    RED("Red", Color(0xFFFF1744), Color(0xFFFF8A80), "#FF1744"),
    WHITE("White", Color(0xFFECEFF1), Color(0xFFFFFFFF), "#ECEFF1"),
    CUSTOM("Custom Color", Color(0xFF00D9FF), Color(0xFF8CF3FF), "#00D9FF");

    companion object {
        fun fromName(name: String): GuiColorPreset =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: GUI_CYAN
    }
}
