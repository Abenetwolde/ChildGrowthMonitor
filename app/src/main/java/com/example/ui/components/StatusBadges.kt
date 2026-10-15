package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MedicalAmber
import com.example.ui.theme.MedicalAmberContainer
import com.example.ui.theme.MedicalAmberDark
import com.example.ui.theme.MedicalGreen
import com.example.ui.theme.MedicalGreenContainer
import com.example.ui.theme.MedicalGreenDark
import com.example.ui.theme.MedicalRed
import com.example.ui.theme.MedicalRedContainer
import com.example.ui.theme.MedicalRedDark

@Composable
fun MalnutritionBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    val (bg, textCol, label) = when (category.uppercase()) {
        "SAM" -> Triple(MedicalRedContainer, MedicalRedDark, "SAM (Severe)")
        "MAM" -> Triple(MedicalAmberContainer, MedicalAmberDark, "MAM (Moderate)")
        "NORMAL" -> Triple(MedicalGreenContainer, MedicalGreenDark, "Normal (Healthy)")
        else -> Triple(Color(0xFFF1F5F9), Color(0xFF64748B), "Unscreened")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.dp, textCol.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(textCol)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = textCol,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SyncStatusBadge(
    syncStatus: String,
    modifier: Modifier = Modifier
) {
    val isPending = syncStatus.contains("pending", ignoreCase = true)
    val isSynced = syncStatus.contains("synced", ignoreCase = true)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSynced) MedicalGreenContainer.copy(alpha = 0.5f) else MedicalAmberContainer.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Icon(
            imageVector = if (isSynced) Icons.Default.CloudDone else Icons.Default.CloudQueue,
            contentDescription = null,
            tint = if (isSynced) MedicalGreenDark else MedicalAmberDark,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (isSynced) "Synced" else "Queued Offline",
            color = if (isSynced) MedicalGreenDark else MedicalAmberDark,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
