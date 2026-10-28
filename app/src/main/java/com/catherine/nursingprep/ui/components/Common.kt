package com.catherine.nursingprep.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Atom
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catherine.nursingprep.data.Subject
import com.catherine.nursingprep.ui.theme.Coral
import com.catherine.nursingprep.ui.theme.CoralSoft
import com.catherine.nursingprep.ui.theme.GoldSoft
import com.catherine.nursingprep.ui.theme.Sage
import com.catherine.nursingprep.ui.theme.SageDeep
import com.catherine.nursingprep.ui.theme.SageSoft
import com.catherine.nursingprep.ui.theme.Teal
import com.catherine.nursingprep.ui.theme.TealDeep
import com.catherine.nursingprep.ui.theme.TealSoft

/* ────────────────────────────────────────────────────────────────
   Shared UI building blocks — glass-style cards, rings, pills, bars
   ──────────────────────────────────────────────────────────────── */

data class SubjectVisual(val main: Color, val soft: Color, val text: Color, val icon: ImageVector)

@Composable
fun subjectVisual(subject: Subject): SubjectVisual {
    val dark = isSystemInDarkTheme()
    return when (subject) {
        Subject.ENGLISH -> SubjectVisual(
            Color(0xFFF59E0B),
            if (dark) GoldSoft.copy(alpha = 0.16f) else GoldSoft,
            if (dark) Color(0xFFFBBF24) else Color(0xFFB45309),
            Icons.Filled.MenuBook,
        )
        Subject.BIOLOGY -> SubjectVisual(
            Sage,
            if (dark) Sage.copy(alpha = 0.18f) else SageSoft,
            if (dark) Color(0xFF8FD3B0) else SageDeep,
            Icons.Filled.Biotech,
        )
        Subject.CHEMISTRY -> SubjectVisual(
            Color(0xFFFB7185),
            if (dark) Coral.copy(alpha = 0.18f) else CoralSoft,
            if (dark) Color(0xFFFDA4AF) else Color(0xFFE11D48),
            Icons.Filled.Science,
        )
        Subject.PHYSICS -> SubjectVisual(
            Teal,
            if (dark) Teal.copy(alpha = 0.18f) else TealSoft,
            if (dark) Color(0xFF2DD4BF) else TealDeep,
            Icons.Filled.Atom,
        )
    }
}

/* ── Card with optional titled header ─────────────────────────── */

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.secondary,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            if (title != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (icon != null) {
                        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        title,
                        style = MaterialTheme.typography.labelLarge,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
            content()
        }
    }
}

/* ── Circular progress ring ───────────────────────────────────── */

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    stroke: Dp = 12.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    ringColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit = {},
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val strokePx = stroke.toPx()
            val inset = strokePx / 2
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
        }
        content()
    }
}

/* ── Rounded progress bar ─────────────────────────────────────── */

@Composable
fun ProgressBar(
    fill: Float,
    color: Color,
    modifier: Modifier = Modifier,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    height: Dp = 8.dp,
) {
    val animated by animateFloatAsState(
        targetValue = fill.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 400),
        label = "progress",
    )
    Box(
        modifier
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(trackColor),
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .clip(RoundedCornerShape(50))
                .background(color),
        )
    }
}

/* ── Small label pill ─────────────────────────────────────────── */

@Composable
fun TagPill(text: String, bg: Color, fg: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg,
        )
    }
}

/* ── Selectable pill (filters, timer modes, subject pickers) ──── */

@Composable
fun SelectPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedBg: Color = MaterialTheme.colorScheme.primary,
    selectedFg: Color = MaterialTheme.colorScheme.onPrimary,
) {
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) selectedBg else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) selectedFg else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/* ── Number + label stat ──────────────────────────────────────── */

@Composable
fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor,
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

/* ── Screen header for each tab ───────────────────────────────── */

@Composable
fun TabHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(top = 12.dp)) {
        Text(title, style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(2.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/* ── Empty state ──────────────────────────────────────────────── */

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}
