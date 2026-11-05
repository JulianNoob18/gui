package com.example.assistant.hotword

enum class HotwordType(val spokenPhrases: List<String>) {
    GUI(listOf("gui", "guy", "hey gui", "hey guy", "ok gui", "ok guy")),
    DUDE(listOf("dude", "hey dude", "yo dude")),
    BROHAM(listOf("broham", "bro ham", "hey broham", "yo broham"));

    companion object {
        fun matches(text: String, enabledWords: Set<HotwordType>): Pair<HotwordType, String>? {
            val lower = text.trim().lowercase()
            for (type in enabledWords) {
                for (phrase in type.spokenPhrases) {
                    if (lower == phrase) {
                        return Pair(type, "")
                    }
                    if (lower.startsWith("$phrase ") || lower.startsWith("$phrase,") || lower.startsWith("$phrase.")) {
                        val remaining = lower.substring(phrase.length).trimStart(' ', ',', '.')
                        return Pair(type, remaining)
                    }
                }
            }
            return null
        }
    }
}

interface HotwordEngine {
    val isListeningForWakeWords: Boolean
    fun setWakeWordToggles(master: Boolean, gui: Boolean, dude: Boolean, broham: Boolean)
    fun processSpokenText(spokenText: String): Pair<HotwordType?, String>
}

class DefaultHotwordEngine : HotwordEngine {
    private var masterEnabled = true
    private var guiEnabled = true
    private var dudeEnabled = true
    private var brohamEnabled = true

    override val isListeningForWakeWords: Boolean
        get() = masterEnabled && (guiEnabled || dudeEnabled || brohamEnabled)

    override fun setWakeWordToggles(
        master: Boolean,
        gui: Boolean,
        dude: Boolean,
        broham: Boolean
    ) {
        masterEnabled = master
        guiEnabled = gui
        dudeEnabled = dude
        brohamEnabled = broham
    }

    override fun processSpokenText(spokenText: String): Pair<HotwordType?, String> {
        if (!masterEnabled) return Pair(null, spokenText)

        val enabledSet = buildSet {
            if (guiEnabled) add(HotwordType.GUI)
            if (dudeEnabled) add(HotwordType.DUDE)
            if (brohamEnabled) add(HotwordType.BROHAM)
        }

        val match = HotwordType.matches(spokenText, enabledSet)
        return if (match != null) {
            Pair(match.first, match.second)
        } else {
            Pair(null, spokenText)
        }
    }
}
