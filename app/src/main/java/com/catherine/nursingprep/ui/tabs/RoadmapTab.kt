package com.catherine.nursingprep.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.catherine.nursingprep.data.CurriculumData
import com.catherine.nursingprep.data.Subject
import com.catherine.nursingprep.data.WeekData
import com.catherine.nursingprep.data.topicKey
import com.catherine.nursingprep.ui.components.ProgressRing
import com.catherine.nursingprep.ui.components.SectionCard
import com.catherine.nursingprep.ui.components.SelectPill
import com.catherine.nursingprep.ui.components.TabHeader
import com.catherine.nursingprep.ui.components.TagPill
import com.catherine.nursingprep.ui.components.subjectVisual
import com.catherine.nursingprep.ui.theme.Sage
import com.catherine.nursingprep.ui.theme.SageDeep
import com.catherine.nursingprep.ui.theme.SageSoft
import com.catherine.nursingprep.viewmodel.StudyViewModel
import java.util.Calendar

/* ────────────────────────────────────────────────────────────────
   139-Day Roadmap — 5 phases · 20 weeks · every syllabus topic
   ──────────────────────────────────────────────────────────────── */

@Composable
fun RoadmapTab(vm: StudyViewModel, modifier: Modifier = Modifier) {
    val ui by vm.uiState.collectAsState()
    val now = remember { Calendar.getInstance() }
    val currentWeek = CurriculumData.weekForDay(CurriculumData.planDayFor(now))
    val currentPhase = CurriculumData.phaseForWeek(currentWeek)

    var filter by remember { mutableStateOf<Subject?>(null) }
    var expandedPhases by remember { mutableStateOf(setOf(currentPhase)) }
    var expandedWeeks by remember { mutableStateOf(setOf(currentWeek)) }

    var doneCount = 0
    var totalCount = 0
    for (w in CurriculumData.WEEKS) {
        for (s in Subject.entries) {
            val topics = w.topics[s] ?: continue
            totalCount += topics.size
            doneCount += topics.indices.count { topicKey(w.week, s, it) in ui.checked }
        }
    }

    Column(
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TabHeader(
            title = "139-Day Roadmap",
            subtitle = "20 weeks · 5 phases · 4 subjects — tick topics as you master them",
        )

        /* overall progress */
        SectionCard {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ProgressRing(progress = if (totalCount == 0) 0f else doneCount.toFloat() / totalCount, size = 84.dp, stroke = 8.dp, ringColor = Sage) {
                    Text(
                        "${if (totalCount == 0) 0 else doneCount * 100 / totalCount}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Column {
                    Text(
                        "$doneCount/$totalCount topics completed",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        "The current week is opened for you automatically.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        /* subject filter */
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SelectPill("All subjects", filter == null) { filter = null }
            Subject.entries.forEach { s ->
                val vis = subjectVisual(s)
                SelectPill(
                    label = s.short,
                    selected = filter == s,
                    onClick = { filter = if (filter == s) null else s },
                    selectedBg = vis.main,
                )
            }
        }

        /* expand / collapse all */
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = {
                expandedPhases = setOf(1, 2, 3, 4, 5)
                expandedWeeks = (1..CurriculumData.TOTAL_WEEKS).toSet()
            }) { Text("Expand all") }
            TextButton(onClick = {
                expandedPhases = emptySet()
                expandedWeeks = emptySet()
            }) { Text("Collapse all") }
        }

        /* phases */
        CurriculumData.PHASES.forEach { phase ->
            val phaseOpen = phase.id in expandedPhases
            val phaseWeeks = CurriculumData.WEEKS.filter { it.week in phase.weekFrom..phase.weekTo }

            SectionCard {
                Row(
                    Modifier.fillMaxWidth().clickable {
                        expandedPhases = if (phaseOpen) expandedPhases - phase.id else expandedPhases + phase.id
                    },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        "Phase ${phase.id}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SageDeep,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(SageSoft)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                    Column(Modifier.weight(1f)) {
                        Text(phase.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${phase.days} · Weeks ${phase.weekFrom}–${phase.weekTo}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        if (phaseOpen) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (phaseOpen) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (phaseOpen) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        phase.blurb,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))

                    phaseWeeks.forEach { week ->
                        WeekCard(
                            week = week,
                            isCurrent = week.week == currentWeek,
                            checked = ui.checked,
                            filter = filter,
                            expanded = week.week in expandedWeeks,
                            onToggleExpand = {
                                expandedWeeks =
                                    if (week.week in expandedWeeks) expandedWeeks - week.week
                                    else expandedWeeks + week.week
                            },
                            onToggleTopic = { vm.toggleTopic(it) },
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun WeekCard(
    week: WeekData,
    isCurrent: Boolean,
    checked: Set<String>,
    filter: Subject?,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleTopic: (String) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onToggleExpand),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Week ${week.week}", style = MaterialTheme.typography.titleSmall)
                    if (isCurrent) TagPill("Current", SageSoft, SageDeep)
                }
                Text(
                    "Days ${CurriculumData.weekDayRange(week.week).first}–${CurriculumData.weekDayRange(week.week).second} · ${CurriculumData.dateRangeLabel(week.week)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (expanded) "Collapse week" else "Expand week",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }

        if (expanded) {
            Spacer(Modifier.height(8.dp))
            Subject.entries.forEach { s ->
                if (filter != null && s != filter) return@forEach
                val topics = week.topics[s] ?: return@forEach
                val vis = subjectVisual(s)
                Column(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(vis.icon, contentDescription = null, tint = vis.main, modifier = Modifier.size(14.dp))
                        Text(
                            s.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = vis.text,
                        )
                    }
                    topics.forEachIndexed { index, topic ->
                        val key = topicKey(week.week, s, index)
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = key in checked,
                                onCheckedChange = { onToggleTopic(key) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = vis.main,
                                    checkmarkColor = Color.White,
                                    uncheckedColor = MaterialTheme.colorScheme.outline,
                                ),
                            )
                            Text(topic, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
