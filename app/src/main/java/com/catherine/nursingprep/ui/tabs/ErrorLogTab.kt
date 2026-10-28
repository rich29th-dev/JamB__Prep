package com.catherine.nursingprep.ui.tabs

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.imePadding
import com.catherine.nursingprep.data.ErrorEntry
import com.catherine.nursingprep.data.Subject
import com.catherine.nursingprep.ui.components.EmptyState
import com.catherine.nursingprep.ui.components.SectionCard
import com.catherine.nursingprep.ui.components.SelectPill
import com.catherine.nursingprep.ui.components.TabHeader
import com.catherine.nursingprep.ui.components.TagPill
import com.catherine.nursingprep.ui.components.subjectVisual
import com.catherine.nursingprep.ui.theme.Coral
import com.catherine.nursingprep.ui.theme.Sage
import com.catherine.nursingprep.ui.theme.SageDeep
import com.catherine.nursingprep.viewmodel.StudyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* ────────────────────────────────────────────────────────────────
   Error Log — Catherine's digital mistakes notebook
   ──────────────────────────────────────────────────────────────── */

@Composable
fun ErrorLogTab(vm: StudyViewModel, modifier: Modifier = Modifier) {
    val ui by vm.uiState.collectAsState()

    var subject by remember { mutableStateOf(Subject.ENGLISH) }
    var question by remember { mutableStateOf("") }
    var myAnswer by remember { mutableStateOf("") }
    var correctAnswer by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf(false) }

    var view by remember { mutableStateOf("pending") } // pending | mastered | all
    var subjectFilter by remember { mutableStateOf<Subject?>(null) }
    var query by remember { mutableStateOf("") }

    val pending = ui.errors.count { !it.mastered }
    val mastered = ui.errors.count { it.mastered }

    Column(
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TabHeader(
            title = "Error Log",
            subtitle = "Every miss logged tonight is a mark you keep in March",
        )

        /* ── New entry form ─────────────────────────────────────── */
        SectionCard(title = "NEW MISTAKE → FUTURE MARK 📝", icon = Icons.Filled.EditNote) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Subject.entries.forEach { s ->
                    val vis = subjectVisual(s)
                    SelectPill(
                        label = s.short,
                        selected = subject == s,
                        onClick = { subject = s },
                        selectedBg = vis.main,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = question,
                onValueChange = { question = it; inputError = false },
                label = { Text("Question you missed *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = myAnswer,
                onValueChange = { myAnswer = it },
                label = { Text("Your answer") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = correctAnswer,
                onValueChange = { correctAnswer = it; inputError = false },
                label = { Text("Correct answer *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Revision note — why was it tricky?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
            if (inputError) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Question and correct answer are required.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    if (question.isBlank() || correctAnswer.isBlank()) {
                        inputError = true
                    } else {
                        vm.addError(subject.id, question, myAnswer, correctAnswer, note)
                        question = ""; myAnswer = ""; correctAnswer = ""; note = ""
                        inputError = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Text("Save to Error Log")
            }
        }

        /* ── Filters ────────────────────────────────────────────── */
        SectionCard(title = "MY ENTRIES", icon = Icons.Filled.EditNote) {
            Text(
                "$pending pending · $mastered mastered",
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SelectPill("Pending ($pending)", view == "pending") { view = "pending" }
                SelectPill("Mastered ($mastered)", view == "mastered") { view = "mastered" }
                SelectPill("All", view == "all") { view = "all" }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SelectPill("All subjects", subjectFilter == null) { subjectFilter = null }
                Subject.entries.forEach { s ->
                    val vis = subjectVisual(s)
                    SelectPill(
                        label = s.short,
                        selected = subjectFilter == s,
                        onClick = { subjectFilter = if (subjectFilter == s) null else s },
                        selectedBg = vis.main,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search entries") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))

            val entries = ui.errors.filter { e ->
                val viewOk = when (view) {
                    "pending" -> !e.mastered
                    "mastered" -> e.mastered
                    else -> true
                }
                val subjectOk = subjectFilter == null || e.subjectId == subjectFilter.id
                val q = query.trim().lowercase()
                val queryOk = q.isEmpty() ||
                    e.question.lowercase().contains(q) ||
                    e.correctAnswer.lowercase().contains(q) ||
                    e.note.lowercase().contains(q)
                viewOk && subjectOk && queryOk
            }

            if (entries.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.CheckCircle,
                    title = if (ui.errors.isEmpty()) "A clean slate!"
                    else "Nothing here",
                    body = if (ui.errors.isEmpty())
                        "No misses logged yet. Drill your past questions tonight and log what trips you up."
                    else "No entries match these filters.",
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    entries.forEach { entry ->
                        EntryCard(
                            entry = entry,
                            onToggleMastered = { vm.toggleMastered(entry.id) },
                            onDelete = { vm.deleteError(entry.id) },
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun EntryCard(
    entry: ErrorEntry,
    onToggleMastered: () -> Unit,
    onDelete: () -> Unit,
) {
    val subject = Subject.fromId(entry.subjectId) ?: Subject.ENGLISH
    val vis = subjectVisual(subject)
    val dateLabel = remember(entry.createdAt) {
        runCatching {
            SimpleDateFormat("MMM d", Locale.US).format(Date(entry.createdAt.toLong()))
        }.getOrDefault("")
    }

    Column(Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TagPill(subject.short, vis.soft, vis.text)
            if (dateLabel.isNotEmpty()) {
                Text(dateLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (entry.mastered) TagPill("Mastered ✓", Sage.copy(alpha = 0.15f), SageDeep)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            entry.question,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            textDecoration = if (entry.mastered) TextDecoration.LineThrough else TextDecoration.None,
        )
        if (entry.myAnswer.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Filled.Cancel, contentDescription = null, tint = Coral, modifier = Modifier.size(14.dp))
                Text(
                    "Your answer: ${entry.myAnswer}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Sage, modifier = Modifier.size(14.dp))
            Text(
                "Correct: ${entry.correctAnswer}",
                style = MaterialTheme.typography.bodySmall,
                color = SageDeep,
            )
        }
        if (entry.note.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                "💡 ${entry.note}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = entry.mastered,
                    onCheckedChange = { onToggleMastered() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Sage,
                        checkmarkColor = Color.White,
                        uncheckedColor = MaterialTheme.colorScheme.outline,
                    ),
                )
                Text(
                    if (entry.mastered) "Mastered" else "Mark as mastered",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete entry",
                    tint = Coral,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    }
}
