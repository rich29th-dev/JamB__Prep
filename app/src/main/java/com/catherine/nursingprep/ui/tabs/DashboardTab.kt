package com.catherine.nursingprep.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Atom
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catherine.nursingprep.Tab
import com.catherine.nursingprep.data.CurriculumData
import com.catherine.nursingprep.data.RotationDay
import com.catherine.nursingprep.data.Stats
import com.catherine.nursingprep.data.Subject
import com.catherine.nursingprep.data.computeStats
import com.catherine.nursingprep.data.topicKey
import com.catherine.nursingprep.ui.components.ProgressBar
import com.catherine.nursingprep.ui.components.ProgressRing
import com.catherine.nursingprep.ui.components.SectionCard
import com.catherine.nursingprep.ui.components.StatTile
import com.catherine.nursingprep.ui.components.TagPill
import com.catherine.nursingprep.ui.components.subjectVisual
import com.catherine.nursingprep.ui.theme.Coral
import com.catherine.nursingprep.ui.theme.CoralSoft
import com.catherine.nursingprep.ui.theme.GoldDeep
import com.catherine.nursingprep.ui.theme.GoldSoft
import com.catherine.nursingprep.ui.theme.Sage
import com.catherine.nursingprep.ui.theme.SageDeep
import com.catherine.nursingprep.ui.theme.SageSoft
import com.catherine.nursingprep.viewmodel.StudyViewModel
import kotlinx.coroutines.delay
import java.util.Calendar

/* ────────────────────────────────────────────────────────────────
   Dashboard — greeting, live countdown, mastery, quick actions
   ──────────────────────────────────────────────────────────────── */

@Composable
fun DashboardTab(
    vm: StudyViewModel,
    onNavigate: (Tab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ui by vm.uiState.collectAsState()
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            nowMs = System.currentTimeMillis()
        }
    }
    val now = remember(nowMs) { Calendar.getInstance() }
    val stats = remember(ui, nowMs) {
        computeStats(ui.checked, ui.pastQuestions, ui.errors, ui.sessions, now)
    }
    val rotation = remember(nowMs) { CurriculumData.todayRotation(now) }

    var showReset by remember { mutableStateOf(false) }
    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            title = { Text("Reset all progress?") },
            text = {
                Text("This clears every checkbox, session and error-log entry saved on this device. It cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReset = false
                        vm.resetAll()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showReset = false }) { Text("Cancel") }
            },
        )
    }

    Column(
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        GreetingHeader(stats, rotation, now)
        QuoteCard(nowMs)
        CountdownCard(nowMs)
        MasteryCard(stats)
        PastQuestionsCard(stats.pastQuestions, onAdd = { vm.addPastQuestions(it) })
        NightFocusCard(stats)
        ErrorLogCard(stats, onOpen = { onNavigate(Tab.ERRORS) })
        TonightFocusCard(rotation, onOpenRoutine = { onNavigate(Tab.ROUTINE) })
        ThisWeekCard(stats, ui.checked, onOpenRoadmap = { onNavigate(Tab.ROADMAP) })
        SubjectMasteryCard(stats)
        ResetCard(onRequestReset = { showReset = true })
        Spacer(Modifier.height(8.dp))
    }
}

/* ── Greeting ──────────────────────────────────────────────────── */

@Composable
private fun GreetingHeader(stats: Stats, rotation: RotationDay, now: Calendar) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            when (stats.planStatus) {
                "active" -> TagPill("Day ${stats.planDay} of ${CurriculumData.TOTAL_DAYS}", SageSoft, SageDeep)
                "upcoming" -> TagPill("Plan starts Nov 2, 2026 · in ${stats.daysUntilStart} days", GoldSoft, GoldDeep)
                else -> TagPill("Plan complete — exam day! 🎉", SageSoft, SageDeep)
            }
            TagPill(rotation.title, CoralSoft, Color(0xFFB4434C))
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "${CurriculumData.greeting(now.get(Calendar.HOUR_OF_DAY))}, Catherine! 🩺✨",
            style = MaterialTheme.typography.headlineLarge,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            CurriculumData.fullDateLabel(now),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun QuoteCard(nowMs: Long) {
    val now = remember(nowMs) { Calendar.getInstance() }
    val quote = CurriculumData.quoteFor(now)
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Filled.FormatQuote, contentDescription = null, tint = Color.White.copy(alpha = 0.65f))
            Text(
                quote,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/* ── Countdown ─────────────────────────────────────────────────── */

@Composable
private fun CountdownCard(nowMs: Long) {
    val diff = (CurriculumData.EXAM_MILLIS - nowMs).coerceAtLeast(0)
    val days = (diff / 86_400_000L).toInt()
    val hours = (diff % 86_400_000L / 3_600_000L).toInt()
    val minutes = (diff % 3_600_000L / 60_000L).toInt()
    val seconds = (diff % 60_000L / 1000L).toInt()

    SectionCard {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "JAMB UTME BEGINS IN",
                style = MaterialTheme.typography.labelLarge,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CountdownBox(days, "days", Modifier.weight(1f))
                CountdownBox(hours, "hours", Modifier.weight(1f))
                CountdownBox(minutes, "mins", Modifier.weight(1f))
                CountdownBox(seconds, "secs", Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "${CurriculumData.examDateLabel()} · your white coat is waiting 🩺",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun CountdownBox(value: Int, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(vertical = 12.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("$value", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
    }
}

/* ── Syllabus mastery ring ─────────────────────────────────────── */

@Composable
private fun MasteryCard(stats: Stats) {
    SectionCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ProgressRing(progress = stats.pct / 100f, size = 110.dp, stroke = 10.dp, ringColor = Sage) {
                Text(
                    "${stats.pct}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "SYLLABUS MASTERY",
                    style = MaterialTheme.typography.labelLarge,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "${stats.doneTopics}/${stats.totalTopics} topics completed",
                    style = MaterialTheme.typography.titleMedium,
                )
                ProgressBar(fill = stats.pct / 100f, color = Sage)
                Text(
                    "Every checkbox moves the 139-day needle.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/* ── Past questions quick add ──────────────────────────────────── */

@Composable
private fun PastQuestionsCard(count: Int, onAdd: (Int) -> Unit) {
    SectionCard(title = "PAST QUESTIONS", icon = Icons.Filled.Quiz) {
        Text("$count", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Text(
            "solved & counting — target 280+ on result day",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(10, 25, 50).forEach { n ->
                FilledTonalButton(onClick = { onAdd(n) }, modifier = Modifier.weight(1f)) {
                    Text("+$n")
                }
            }
        }
    }
}

/* ── Night focus stats ─────────────────────────────────────────── */

@Composable
private fun NightFocusCard(stats: Stats) {
    SectionCard(title = "NIGHT FOCUS", icon = Icons.Filled.LocalFireDepartment, iconTint = Coral) {
        Row(Modifier.fillMaxWidth()) {
            StatTile("${stats.focusToday}", "min today", Modifier.weight(1f))
            StatTile("${stats.focusWeek}", "min / 7 days", Modifier.weight(1f))
            StatTile("${stats.streak}", "night streak", Modifier.weight(1f), valueColor = Coral)
        }
    }
}

/* ── Error log teaser ──────────────────────────────────────────── */

@Composable
private fun ErrorLogCard(stats: Stats, onOpen: () -> Unit) {
    SectionCard(title = "ERROR LOG", icon = Icons.Filled.EditNote) {
        Text(
            "${stats.errorsPending} pending · ${stats.errorsMastered} mastered",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Every miss you log tonight is a mark you won't lose in March.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = onOpen, modifier = Modifier.fillMaxWidth()) {
            Text("Review mistakes")
            Spacer(Modifier.size(6.dp))
            Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    }
}

/* ── Tonight's rotation focus ──────────────────────────────────── */

private fun rotationIcon(rotation: RotationDay): ImageVector = when (rotation.dow) {
    1 -> Icons.Filled.Biotech
    2 -> Icons.Filled.Science
    3 -> Icons.Filled.Atom
    4 -> Icons.Filled.MenuBook
    5 -> Icons.Filled.Calculate
    6 -> Icons.Filled.FactCheck
    else -> Icons.Filled.EmojiEvents
}

@Composable
private fun TonightFocusCard(rotation: RotationDay, onOpenRoutine: () -> Unit) {
    val icon = rotationIcon(rotation)
    SectionCard(title = "TONIGHT'S FOCUS — ${rotation.name.uppercase()}", icon = icon, iconTint = Sage) {
        Text(rotation.title, style = MaterialTheme.typography.titleLarge)
        Text(
            rotation.focus,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            rotation.subjects.forEach { s ->
                val vis = subjectVisual(s)
                TagPill(s.short, vis.soft, vis.text)
            }
        }
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            rotation.flow.forEach { line ->
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Sage, modifier = Modifier.size(14.dp))
                    Text(line, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onOpenRoutine,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Sage, contentColor = Color.White),
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.size(6.dp))
            Text("Open Night Routine · 10 PM – 12:30 AM")
        }
    }
}

/* ── This week in the roadmap ──────────────────────────────────── */

@Composable
private fun ThisWeekCard(stats: Stats, checked: Set<String>, onOpenRoadmap: () -> Unit) {
    val week = CurriculumData.WEEKS[stats.currentWeek - 1]
    SectionCard(title = "THIS WEEK IN THE ROADMAP", icon = Icons.Filled.Event) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("Week ${week.week}", style = MaterialTheme.typography.titleLarge)
                Text(
                    CurriculumData.dateRangeLabel(week.week),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TagPill("Current week", SageSoft, SageDeep)
        }
        Spacer(Modifier.height(12.dp))
        Subject.entries.forEach { s ->
            val topics = week.topics[s] ?: return@forEach
            val done = topics.indices.count { topicKey(week.week, s, it) in checked }
            val vis = subjectVisual(s)
            Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(vis.icon, contentDescription = null, tint = vis.main, modifier = Modifier.size(16.dp))
                    Text(
                        s.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "$done/${topics.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(4.dp))
                ProgressBar(
                    fill = if (topics.isEmpty()) 0f else done.toFloat() / topics.size,
                    color = vis.main,
                    height = 5.dp,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onOpenRoadmap, modifier = Modifier.fillMaxWidth()) {
            Text("Open the 139-Day Roadmap")
            Spacer(Modifier.size(6.dp))
            Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    }
}

/* ── Subject mastery bars ──────────────────────────────────────── */

@Composable
private fun SubjectMasteryCard(stats: Stats) {
    SectionCard(title = "SUBJECT MASTERY", icon = Icons.Filled.School) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "Progress across the four official JAMB syllabuses",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            TagPill("${stats.pct}% overall", SageSoft, SageDeep)
        }
        Spacer(Modifier.height(12.dp))
        Subject.entries.forEach { s ->
            val stat = stats.perSubject[s] ?: return@forEach
            val vis = subjectVisual(s)
            Row(
                Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(vis.soft),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(vis.icon, contentDescription = null, tint = vis.main, modifier = Modifier.size(18.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(s.label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    ProgressBar(fill = stat.pct / 100f, color = vis.main, height = 6.dp)
                }
                Text(
                    "${stat.done}/${stat.total}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/* ── Reset ─────────────────────────────────────────────────────── */

@Composable
private fun ResetCard(onRequestReset: () -> Unit) {
    SectionCard {
        Text(
            "Progress is stored on this device only.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        TextButton(onClick = onRequestReset) {
            Text("Reset all data", color = MaterialTheme.colorScheme.error)
        }
    }
}
