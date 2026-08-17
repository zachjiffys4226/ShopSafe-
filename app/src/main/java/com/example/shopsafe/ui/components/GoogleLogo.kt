package com.example.shopsafe.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun GoogleLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(18.dp)) {
        val strokeWidth = size.width * 0.22f
        val radius = (size.width - strokeWidth) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -45f,
            sweepAngle = 135f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 90f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 180f,
            sweepAngle = 45f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 225f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        drawRect(
            color = Color(0xFF4285F4),
            topLeft = Offset(center.x, center.y - strokeWidth / 2f),
            size = Size(radius + strokeWidth / 2f, strokeWidth)
        )
    }
}
