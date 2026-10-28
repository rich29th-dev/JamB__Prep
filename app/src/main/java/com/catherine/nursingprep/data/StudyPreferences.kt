package com.catherine.nursingprep.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/* ────────────────────────────────────────────────────────────────
   Persistence layer — SharedPreferences + Gson
   Stores: checked topics, past-question count, error log,
   study sessions and completed routine blocks.
   ──────────────────────────────────────────────────────────────── */

class StudyPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    /* checked roadmap topics */
    fun loadChecked(): Set<String> =
        prefs.getStringSet(KEY_TOPICS, emptySet())?.toSet() ?: emptySet()

    fun saveChecked(checked: Set<String>) {
        prefs.edit().putStringSet(KEY_TOPICS, checked).apply()
    }

    /* past questions solved */
    fun loadPastQuestions(): Int = prefs.getInt(KEY_PAST_QUESTIONS, 0)

    fun savePastQuestions(count: Int) {
        prefs.edit().putInt(KEY_PAST_QUESTIONS, count).apply()
    }

    /* error log */
    fun loadErrors(): List<ErrorEntry> {
        val json = prefs.getString(KEY_ERRORS, null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<ErrorEntry>>() {}.type)
                ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveErrors(entries: List<ErrorEntry>) {
        prefs.edit().putString(KEY_ERRORS, gson.toJson(entries)).apply()
    }

    /* study sessions */
    fun loadSessions(): List<StudySession> {
        val json = prefs.getString(KEY_SESSIONS, null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<StudySession>>() {}.type)
                ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveSessions(sessions: List<StudySession>) {
        prefs.edit().putString(KEY_SESSIONS, gson.toJson(sessions)).apply()
    }

    /* routine blocks done, keyed "{yyyy-MM-dd}|{blockId}" */
    fun loadRoutineDone(): Set<String> =
        prefs.getStringSet(KEY_ROUTINE_DONE, emptySet())?.toSet() ?: emptySet()

    fun saveRoutineDone(done: Set<String>) {
        prefs.edit().putStringSet(KEY_ROUTINE_DONE, done).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val PREFS_NAME = "cathcare_prefs"
        const val KEY_TOPICS = "checked_topics"
        const val KEY_PAST_QUESTIONS = "past_questions"
        const val KEY_ERRORS = "error_log"
        const val KEY_SESSIONS = "study_sessions"
        const val KEY_ROUTINE_DONE = "routine_done"
    }
}
