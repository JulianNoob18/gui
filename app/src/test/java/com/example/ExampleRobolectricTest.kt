package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.assistant.commands.CommandParser
import com.example.assistant.hotword.DefaultHotwordEngine
import com.example.assistant.hotword.HotwordType
import com.example.assistant.state.AssistantStateMachine
import com.example.model.AssistantState
import com.example.model.CommandType
import com.example.model.GuiExpression
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("GUI", appName)
    }

    @Test
    fun hotwordEngineRecognizesAllThreeWakeWords() {
        val engine = DefaultHotwordEngine()
        engine.setWakeWordToggles(master = true, gui = true, dude = true, broham = true)

        val (guiType, guiCmd) = engine.processSpokenText("gui open spotify")
        assertEquals(HotwordType.GUI, guiType)
        assertEquals("open spotify", guiCmd)

        val (dudeType, dudeCmd) = engine.processSpokenText("dude search for 3d printed phone stand")
        assertEquals(HotwordType.DUDE, dudeType)
        assertEquals("search for 3d printed phone stand", dudeCmd)

        val (brohamType, brohamCmd) = engine.processSpokenText("broham open casting")
        assertEquals(HotwordType.BROHAM, brohamType)
        assertEquals("open casting", brohamCmd)
    }

    @Test
    fun commandParserResolvesAppAndWebSearch() {
        val parser = CommandParser()

        val appCmd = parser.parse("open Spotify", "GUI")
        assertEquals(CommandType.OPEN_APP, appCmd.type)
        assertEquals("Spotify", appCmd.target)

        val searchCmd = parser.parse("search for futuristic robot faces", "DUDE")
        assertEquals(CommandType.WEB_SEARCH, searchCmd.type)
        assertEquals("futuristic robot faces", searchCmd.query)

        val castCmd = parser.parse("cast to screen", "BROHAM")
        assertEquals(CommandType.CAST, castCmd.type)
    }

    @Test
    fun assistantStateMachineTransitionsAndInterruption() {
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val stateMachine = AssistantStateMachine(scope)

        assertEquals(AssistantState.IDLE, stateMachine.currentState.value)
        assertEquals(GuiExpression.NEUTRAL, stateMachine.currentExpression.value)

        stateMachine.transitionTo(AssistantState.LISTENING)
        assertEquals(AssistantState.LISTENING, stateMachine.currentState.value)
        assertEquals(GuiExpression.LISTENING, stateMachine.currentExpression.value)
        assertTrue(stateMachine.isMicrophoneActive.value)

        var interrupted = false
        stateMachine.onInterruptRequest = { interrupted = true }
        stateMachine.interrupt()

        assertTrue(interrupted)
        assertEquals(AssistantState.ATTENTIVE, stateMachine.currentState.value)
    }
}
