package com.catherine.nursingprep.viewmodel

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.catherine.nursingprep.data.CurriculumData
import com.catherine.nursingprep.data.ErrorEntry
import com.catherine.nursingprep.data.StudyPreferences
import com.catherine.nursingprep.data.StudySession
import com.catherine.nursingprep.data.TimerMode
import com.catherine.nursingprep.data.dateKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

data class UiState(
    val checked: Set<String> = emptySet(),
    val pastQuestions: Int = 0,
    val errors: List<ErrorEntry> = emptyList(),
    val sessions: List<StudySession> = emptyList(),
    val routineDone: Set<String> = emptySet(),
    val timerMode: TimerMode = CurriculumData.DEFAULT_TIMER,
    val remainingSec: Int = CurriculumData.DEFAULT_TIMER.minutes * 60,
    val running: Boolean = false,
)

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = StudyPreferences(application)

    private val _uiState = MutableStateFlow(
        UiState(
            checked = prefs.loadChecked(),
            pastQuestions = prefs.loadPastQuestions(),
            errors = prefs.loadErrors(),
            sessions = prefs.loadSessions(),
            routineDone = prefs.loadRoutineDone(),
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private var timerJob: Job? = null

    /* ── Roadmap topics ────────────────────────────────────────── */

    fun toggleTopic(key: String) {
        _uiState.update { s ->
            val next = if (key in s.checked) s.checked - key else s.checked + key
            prefs.saveChecked(next)
            s.copy(checked = next)
        }
    }

    /* ── Past questions ────────────────────────────────────────── */

    fun addPastQuestions(n: Int) {
        _uiState.update { s ->
            val next = (s.pastQuestions + n).coerceAtLeast(0)
            prefs.savePastQuestions(next)
            s.copy(pastQuestions = next)
        }
        _messages.tryEmit("+$n past questions logged 💪")
    }

    /* ── Error log ─────────────────────────────────────────────── */

    fun addError(
        subjectId: String,
        question: String,
        myAnswer: String,
        correctAnswer: String,
        note: String,
    ) {
        val entry = ErrorEntry(
            id = UUID.randomUUID().toString(),
            subjectId = subjectId,
            question = question.trim(),
            myAnswer = myAnswer.trim(),
            correctAnswer = correctAnswer.trim(),
            note = note.trim(),
            createdAt = System.currentTimeMillis().toString(),
            mastered = false,
        )
        _uiState.update { s ->
            val next = listOf(entry) + s.errors
            prefs.saveErrors(next)
            s.copy(errors = next)
        }
        _messages.tryEmit("Saved to your Error Log 📝")
    }

    fun toggleMastered(id: String) {
        _uiState.update { s ->
            val next = s.errors.map { if (it.id == id) it.copy(mastered = !it.mastered) else it }
            prefs.saveErrors(next)
            s.copy(errors = next)
        }
    }

    fun deleteError(id: String) {
        _uiState.update { s ->
            val next = s.errors.filterNot { it.id == id }
            prefs.saveErrors(next)
            s.copy(errors = next)
        }
        _messages.tryEmit("Entry deleted — keep drilling!")
    }

    /* ── Nightly routine blocks ────────────────────────────────── */

    fun toggleRoutineBlock(blockId: String, done: Boolean) {
        _uiState.update { s ->
            val key = "${dateKey(Calendar.getInstance())}|$blockId"
            val next = if (done) s.routineDone + key else s.routineDone - key
            prefs.saveRoutineDone(next)
            s.copy(routineDone = next)
        }
    }

    /* ── Study sessions ────────────────────────────────────────── */

    fun logSession(label: String, minutes: Int) {
        _uiState.update { s ->
            val session = StudySession(
                id = UUID.randomUUID().toString(),
                label = label,
                minutes = minutes,
                date = dateKey(Calendar.getInstance()),
            )
            val next = listOf(session) + s.sessions
            prefs.saveSessions(next)
            s.copy(sessions = next)
        }
    }

    /* ── Session timer ─────────────────────────────────────────── */

    fun selectTimerMode(mode: TimerMode) {
        timerJob?.cancel()
        _uiState.update { it.copy(timerMode = mode, remainingSec = mode.minutes * 60, running = false) }
    }

    fun toggleRun() {
        if (_uiState.value.running) {
            timerJob?.cancel()
            _uiState.update { it.copy(running = false) }
        } else {
            _uiState.update { it.copy(running = true) }
            timerJob = viewModelScope.launch {
                while (true) {
                    delay(1000)
                    val remaining = _uiState.value.remainingSec
                    if (remaining <= 1) {
                        completeSession()
                        break
                    } else {
                        _uiState.update { it.copy(remainingSec = it.remainingSec - 1) }
                    }
                }
            }
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(running = false, remainingSec = it.timerMode.minutes * 60) }
    }

    private fun completeSession() {
        val mode = _uiState.value.timerMode
        timerJob?.cancel()
        playChime()
        logSession(mode.label, mode.minutes)
        mode.blockId?.let { toggleRoutineBlock(it, true) }
        _uiState.update { it.copy(running = false, remainingSec = mode.minutes * 60) }
        _messages.tryEmit("${mode.label} complete — ${mode.minutes} min logged 🎉")
    }

    private fun playChime() {
        try {
            val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            tone.startTone(ToneGenerator.TONE_PROP_ACK, 600)
            Handler(Looper.getMainLooper()).postDelayed({ tone.release() }, 900)
        } catch (_: Exception) {
            /* audio unavailable — silent success */
        }
    }

    /* ── Reset everything ──────────────────────────────────────── */

    fun resetAll() {
        timerJob?.cancel()
        prefs.clearAll()
        _uiState.update {
            UiState(
                timerMode = it.timerMode,
                remainingSec = it.timerMode.minutes * 60,
            )
        }
        _messages.tryEmit("Fresh start 🌱 — all progress reset")
    }
}
