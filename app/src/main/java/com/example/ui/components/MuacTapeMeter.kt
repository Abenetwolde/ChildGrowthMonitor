package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MedicalAmber
import com.example.ui.theme.MedicalGreen
import com.example.ui.theme.MedicalRed
import com.example.ui.theme.SlateBorder

@Composable
fun MuacTapeMeter(
    currentValueCm: Double?,
    modifier: Modifier = Modifier
) {
    val minCm = 8.0
    val maxCm = 16.0
    val samCutoff = 11.5
    val mamCutoff = 12.5

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "MUAC Tape Guide",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (currentValueCm != null) "${"%.1f".format(currentValueCm)} cm" else "-- cm",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = when {
                    currentValueCm == null -> MaterialTheme.colorScheme.onSurfaceVariant
                    currentValueCm < 11.5 -> MedicalRed
                    currentValueCm < 12.5 -> MedicalAmber
                    else -> MedicalGreen
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Visual Tape Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val totalWidth = size.width
                val tapeHeight = 22.dp.toPx()
                val topY = 16.dp.toPx()

                // Calculate segment widths based on cm range (8.0 to 16.0 = 8.0 cm total span)
                val totalRange = maxCm - minCm
                val redFraction = ((samCutoff - minCm) / totalRange).toFloat().coerceIn(0f, 1f)
                val yellowFraction = ((mamCutoff - samCutoff) / totalRange).toFloat().coerceIn(0f, 1f)
                val greenFraction = ((maxCm - mamCutoff) / totalRange).toFloat().coerceIn(0f, 1f)

                val redWidth = totalWidth * redFraction
                val yellowWidth = totalWidth * yellowFraction
                val greenWidth = totalWidth * greenFraction

                // Draw Red segment (< 11.5cm)
                drawRect(
                    color = MedicalRed,
                    topLeft = Offset(0f, topY),
                    size = Size(redWidth, tapeHeight)
                )

                // Draw Yellow segment (11.5 - 12.4cm)
                drawRect(
                    color = MedicalAmber,
                    topLeft = Offset(redWidth, topY),
                    size = Size(yellowWidth, tapeHeight)
                )

                // Draw Green segment (>= 12.5cm)
                drawRect(
                    color = MedicalGreen,
                    topLeft = Offset(redWidth + yellowWidth, topY),
                    size = Size(greenWidth, tapeHeight)
                )

                // Draw tick marks
                val cmSteps = 8
                for (i in 0..cmSteps) {
                    val tickX = totalWidth * (i.toFloat() / cmSteps)
                    drawLine(
                        color = Color.White.copy(alpha = 0.6f),
                        start = Offset(tickX, topY),
                        end = Offset(tickX, topY + tapeHeight),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }

                // Draw needle pointer if value is provided
                if (currentValueCm != null) {
                    val clampedVal = currentValueCm.coerceIn(minCm, maxCm)
                    val pointerFraction = ((clampedVal - minCm) / totalRange).toFloat()
                    val pointerX = (totalWidth * pointerFraction).coerceIn(4f, totalWidth - 4f)

                    val needlePath = Path().apply {
                        moveTo(pointerX, 0f)
                        lineTo(pointerX - 6.dp.toPx(), 12.dp.toPx())
                        lineTo(pointerX + 6.dp.toPx(), 12.dp.toPx())
                        close()
                    }
                    drawPath(needlePath, color = Color.Black)
                    drawLine(
                        color = Color.Black,
                        start = Offset(pointerX, 10.dp.toPx()),
                        end = Offset(pointerX, topY + tapeHeight + 4.dp.toPx()),
                        strokeWidth = 2.5.dp.toPx()
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("< 11.5 (SAM)", fontSize = 11.sp, color = MedicalRed, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text("11.5–12.4 (MAM)", fontSize = 11.sp, color = MedicalAmber, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text("≥ 12.5 (Normal)", fontSize = 11.sp, color = MedicalGreen, fontWeight = FontWeight.Bold)
        }
    }
}
