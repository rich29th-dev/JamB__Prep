package com.catherine.nursingprep.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/* ────────────────────────────────────────────────────────────────
   Catherine's JAMB Nursing Prep — data models
   139 days · 20 weeks · exam: March 20, 2027
   ──────────────────────────────────────────────────────────────── */

enum class Subject(val id: String, val label: String, val short: String) {
    ENGLISH("english", "Use of English", "English"),
    BIOLOGY("biology", "Biology", "Biology"),
    CHEMISTRY("chemistry", "Chemistry", "Chemistry"),
    PHYSICS("physics", "Physics", "Physics");

    companion object {
        fun fromId(id: String): Subject? = entries.firstOrNull { it.id == id }
    }
}

data class PhaseMeta(
    val id: Int,
    val name: String,
    val days: String,
    val weekFrom: Int,
    val weekTo: Int,
    val blurb: String,
)

data class WeekData(
    val week: Int,
    val phase: Int,
    val topics: Map<Subject, List<String>>,
)

data class NightBlock(
    val id: String,
    val time: String,
    val title: String,
    val minutes: Int,
    val desc: String,
)

data class TimerMode(
    val id: String,
    val label: String,
    val minutes: Int,
    /** when set, completing this timer ticks the matching routine block */
    val blockId: String? = null,
)

data class RotationDay(
    val dow: Int, // 0 = Sunday … 6 = Saturday
    val name: String,
    val title: String,
    val focus: String,
    val subjects: List<Subject>,
    val flow: List<String>,
)

data class ErrorEntry(
    val id: String = "",
    val subjectId: String = Subject.ENGLISH.id,
    val question: String = "",
    val myAnswer: String = "",
    val correctAnswer: String = "",
    val note: String = "",
    val createdAt: String = "",
    val mastered: Boolean = false,
)

data class StudySession(
    val id: String = "",
    val label: String = "",
    val minutes: Int = 0,
    val date: String = "", // yyyy-MM-dd
)

data class SubjectStat(val done: Int = 0, val total: Int = 0, val pct: Int = 0)

data class Stats(
    val pastQuestions: Int,
    val totalTopics: Int,
    val doneTopics: Int,
    val pct: Int,
    val perSubject: Map<Subject, SubjectStat>,
    val planDay: Int,
    val currentWeek: Int,
    /** "upcoming" | "active" | "done" */
    val planStatus: String,
    val daysUntilStart: Int,
    val focusToday: Int,
    val focusWeek: Int,
    val sessionsWeek: Int,
    val streak: Int,
    val totalMinutes: Int,
    val errorsPending: Int,
    val errorsMastered: Int,
)

fun topicKey(week: Int, subject: Subject, index: Int): String = "w$week-${subject.id}-$index"

/* ── Date & plan-time helpers (java.util.Calendar — no desugaring needed) ── */

fun Calendar.toMidnight() {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}

fun dateKey(c: Calendar): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.time)

fun parseDateKey(key: String): Calendar? = try {
    val p = key.split("-")
    Calendar.getInstance().apply {
        clear()
        set(p[0].toInt(), p[1].toInt() - 1, p[2].toInt())
    }
} catch (_: Exception) {
    null
}

/* ── Derived stats ─────────────────────────────────────────────── */

fun computeStats(
    checked: Set<String>,
    pastQuestions: Int,
    errors: List<ErrorEntry>,
    sessions: List<StudySession>,
    now: Calendar,
): Stats {
    /* syllabus topics */
    val totals = mutableMapOf<Subject, Int>()
    val dones = mutableMapOf<Subject, Int>()
    var totalTopics = 0
    var doneTopics = 0
    for (week in CurriculumData.WEEKS) {
        for ((subject, topics) in week.topics) {
            totals[subject] = (totals[subject] ?: 0) + topics.size
            totalTopics += topics.size
            for (i in topics.indices) {
                if (topicKey(week.week, subject, i) in checked) {
                    dones[subject] = (dones[subject] ?: 0) + 1
                    doneTopics += 1
                }
            }
        }
    }
    val perSubject = Subject.entries.associateWith { s ->
        val t = totals[s] ?: 0
        val d = dones[s] ?: 0
        SubjectStat(d, t, if (t == 0) 0 else (d * 100 / t))
    }

    /* plan position */
    val planDay = CurriculumData.planDayFor(now)
    var planStatus = "upcoming"
    var currentWeek = 1
    var daysUntilStart = 0
    if (planDay in 1..CurriculumData.TOTAL_DAYS) {
        planStatus = "active"
        currentWeek = CurriculumData.weekForDay(planDay)
    } else if (planDay > CurriculumData.TOTAL_DAYS) {
        planStatus = "done"
        currentWeek = CurriculumData.TOTAL_WEEKS
    } else {
        daysUntilStart = 1 - planDay
    }

    /* focus sessions */
    val today = dateKey(now)
    val todayMid = (now.clone() as Calendar).apply { toMidnight() }
    val weekAgo = (todayMid.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -6) }
    var focusToday = 0
    var focusWeek = 0
    var sessionsWeek = 0
    var totalMinutes = 0
    for (s in sessions) {
        totalMinutes += s.minutes
        if (s.date == today) focusToday += s.minutes
        val sd = parseDateKey(s.date)
        if (sd != null && !sd.before(weekAgo)) {
            focusWeek += s.minutes
            sessionsWeek += 1
        }
    }

    /* streak: consecutive nights (ending today or yesterday) with ≥1 session */
    var streak = 0
    val sessionDates = sessions.map { it.date }.toSet()
    val cursor = todayMid.clone() as Calendar
    if (dateKey(cursor) !in sessionDates) cursor.add(Calendar.DAY_OF_YEAR, -1)
    while (dateKey(cursor) in sessionDates) {
        streak += 1
        cursor.add(Calendar.DAY_OF_YEAR, -1)
    }

    /* error log */
    val errorsPending = errors.count { !it.mastered }
    val errorsMastered = errors.count { it.mastered }

    return Stats(
        pastQuestions = pastQuestions,
        totalTopics = totalTopics,
        doneTopics = doneTopics,
        pct = if (totalTopics == 0) 0 else (doneTopics * 100 / totalTopics),
        perSubject = perSubject,
        planDay = planDay,
        currentWeek = currentWeek,
        planStatus = planStatus,
        daysUntilStart = daysUntilStart,
        focusToday = focusToday,
        focusWeek = focusWeek,
        sessionsWeek = sessionsWeek,
        streak = streak,
        totalMinutes = totalMinutes,
        errorsPending = errorsPending,
        errorsMastered = errorsMastered,
    )
}
