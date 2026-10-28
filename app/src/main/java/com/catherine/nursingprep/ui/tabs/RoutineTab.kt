package com.catherine.nursingprep.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catherine.nursingprep.data.CurriculumData
import com.catherine.nursingprep.data.NightBlock
import com.catherine.nursingprep.data.StudySession
import com.catherine.nursingprep.data.TimerMode
import com.catherine.nursingprep.data.computeStats
import com.catherine.nursingprep.data.dateKey
import com.catherine.nursingprep.ui.components.EmptyState
import com.catherine.nursingprep.ui.components.ProgressRing
import com.catherine.nursingprep.ui.components.SectionCard
import com.catherine.nursingprep.ui.components.SelectPill
import com.catherine.nursingprep.ui.components.StatTile
import com.catherine.nursingprep.ui.components.TabHeader
import com.catherine.nursingprep.ui.components.TagPill
import com.catherine.nursingprep.ui.theme.Coral
import com.catherine.nursingprep.ui.theme.GoldSoft
import com.catherine.nursingprep.ui.theme.GoldDeep
import com.catherine.nursingprep.ui.theme.Navy
import com.catherine.nursingprep.ui.theme.NavySoft
import com.catherine.nursingprep.ui.theme.Sage
import com.catherine.nursingprep.ui.theme.SageDeep
import com.catherine.nursingprep.ui.theme.SageSoft
import com.catherine.nursingprep.viewmodel.StudyViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/* ────────────────────────────────────────────────────────────────
   Night Routine — 10:00 PM – 12:30 AM · 4 blocks + session timer
   ──────────────────────────────────────────────────────────────── */

@Composable
fun RoutineTab(vm: StudyViewModel, modifier: Modifier = Modifier) {
    val ui by vm.uiState.collectAsState()
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            nowMs = System.currentTimeMillis()
        }
    }
    val now = remember(nowMs) { Calendar.getInstance() }
    val stats = remember(ui, nowMs) {
        computeStats(ui.checked, ui.pastQuestions, ui.errors, ui.sessions, now)
    }

    Column(
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TabHeader(
            title = "Night Routine",
            subtitle = "10:00 PM – 12:30 AM · 2.5 focused hours, every night",
        )

        WindowStatusCard(now)
        TimerCard(ui.timerMode, ui.remainingSec, ui.running, onSelectMode = vm::selectTimerMode, onToggleRun = vm::toggleRun, onReset = vm::resetTimer)

        CurriculumData.NIGHT_BLOCKS.forEach { block ->
            NightBlockCard(
                block = block,
                done = "${dateKey(now)}|${block.id}" in ui.routineDone,
                onToggleDone = { vm.toggleRoutineBlock(block.id, it) },
                onStartTimer = {
                    val mode = CurriculumData.TIMER_MODES.first { m -> m.blockId == block.id }
                    vm.selectTimerMode(mode)
                    vm.toggleRun()
                },
            )
        }

        NurseTipCard(now)
        FocusStatsCard(stats.focusToday, stats.focusWeek, stats.streak, stats.totalMinutes)
        RecentSessionsCard(ui.sessions)
        Spacer(Modifier.height(8.dp))
    }
}

/* ── Window status (live vs starts-in) ─────────────────────────── */

@Composable
private fun WindowStatusCard(now: Calendar) {
    val minutesOfDay = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    val live = CurriculumData.routineLive(minutesOfDay)
    val bg = if (live) Sage else Navy
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                Icons.Filled.Nightlight,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
            Text(
                if (live) "Live now — make it count! 🌙" else buildString {
                    append("Study window closed — next block starts at 10:00 PM")
                    if (minutesOfDay in 30 until 22 * 60) {
                        val until = 22 * 60 - minutesOfDay
                        append(" (in ${until / 60}h ${until % 60}m)")
                    }
                },
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            if (live) "Phone on DND, water nearby — you've got this, nurse-to-be. 🩺"
            else "Rest well. Future Catherine thanks you for showing up tonight. 💚",
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

/* ── Session timer ─────────────────────────────────────────────── */

private fun timerIcon(mode: TimerMode) = when (mode.id) {
    "warmup" -> Icons.Filled.WbTwilight
    "core" -> Icons.Filled.MenuBook
    "drill" -> Icons.Filled.Quiz
    "log" -> Icons.Filled.History
    "break" -> Icons.Filled.LocalCafe
    else -> Icons.Filled.Timer
}

@Composable
private fun TimerCard(
    mode: TimerMode,
    remainingSec: Int,
    running: Boolean,
    onSelectMode: (TimerMode) -> Unit,
    onToggleRun: () -> Unit,
    onReset: () -> Unit,
) {
    val totalSec = mode.minutes * 60
    SectionCard(title = "SESSION TIMER", icon = Icons.Filled.Timer) {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CurriculumData.TIMER_MODES.forEach { m ->
                SelectPill(
                    label = m.label,
                    selected = m.id == mode.id,
                    onClick = { onSelectMode(m) },
                    selectedBg = Navy,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            ProgressRing(progress = remainingSec.toFloat() / totalSec, size = 180.dp, stroke = 14.dp, ringColor = Sage) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        String.format(Locale.US, "%02d:%02d", remainingSec / 60, remainingSec % 60),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        mode.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onToggleRun,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Sage, contentColor = Color.White),
            ) {
                Icon(
                    if (running) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(6.dp))
                Text(if (running) "Pause" else "Start")
            }
            OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text("Reset")
            }
        }
        Text(
            "Completing a block timer ticks that routine block automatically.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

/* ── Night block cards ─────────────────────────────────────────── */

@Composable
private fun NightBlockCard(
    block: NightBlock,
    done: Boolean,
    onToggleDone: (Boolean) -> Unit,
    onStartTimer: () -> Unit,
) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TagPill(block.time, NavySoft, MaterialTheme.colorScheme.primary)
            TagPill("${block.minutes} min", SageSoft, SageDeep)
        }
        Spacer(Modifier.height(8.dp))
        Text(block.title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            block.desc,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = done,
                    onCheckedChange = onToggleDone,
                    colors = CheckboxDefaults.colors(
                        checkedColor = Sage,
                        checkmarkColor = Color.White,
                        uncheckedColor = MaterialTheme.colorScheme.outline,
                    ),
                )
                Text(
                    if (done) "Done for tonight ✓" else "Mark done",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (done) SageDeep else MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                "Start ${block.minutes}-min timer",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Sage,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onStartTimer)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

/* ── Nurse tip ─────────────────────────────────────────────────── */

@Composable
private fun NurseTipCard(now: Calendar) {
    SectionCard(title = "NURSE TIP", icon = Icons.Filled.AutoAwesome, iconTint = Coral) {
        Text(CurriculumData.tipFor(now), style = MaterialTheme.typography.bodyMedium)
    }
}

/* ── Focus stats ───────────────────────────────────────────────── */

@Composable
private fun FocusStatsCard(focusToday: Int, focusWeek: Int, streak: Int, totalMinutes: Int) {
    SectionCard(title = "FOCUS STATS", icon = Icons.Filled.Timer) {
        Row(Modifier.fillMaxWidth()) {
            StatTile("$focusToday", "min today", Modifier.weight(1f))
            StatTile("$focusWeek", "min / 7 days", Modifier.weight(1f))
            StatTile("$streak", "night streak", Modifier.weight(1f), valueColor = Coral)
            StatTile("${totalMinutes / 60}h", "all time", Modifier.weight(1f))
        }
    }
}

/* ── Recent sessions ───────────────────────────────────────────── */

@Composable
private fun RecentSessionsCard(sessions: List<StudySession>) {
    SectionCard(title = "RECENT SESSIONS", icon = Icons.Filled.History) {
        if (sessions.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Timer,
                title = "No sessions yet",
                body = "Your first 15-minute warm-up starts tonight at 10:00 PM.",
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sessions.take(5).forEach { s ->
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Sage),
                            )
                            Column {
                                Text(s.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text(
                                    s.date,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        TagPill("+${s.minutes} min", GoldSoft, GoldDeep)
                    }
                }
            }
        }
    }
}
