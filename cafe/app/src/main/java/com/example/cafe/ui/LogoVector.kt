package com.example.cafe.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun CafeAromaLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(100.dp)) {
        val w = size.width
        val h = size.height

        // Círculo exterior (Dorado/Marrón claro)
        drawCircle(
            color = Color(0xFFB47B5A),
            radius = w * 0.45f,
            style = Stroke(width = w * 0.02f)
        )

        // Grano de Café (Lado izquierdo oscuro)
        val leftBean = Path().apply {
            moveTo(w * 0.5f, h * 0.2f)
            cubicTo(w * 0.3f, h * 0.2f, w * 0.25f, h * 0.5f, w * 0.35f, h * 0.75f)
            cubicTo(w * 0.4f, h * 0.85f, w * 0.5f, h * 0.8f, w * 0.5f, h * 0.8f)
            cubicTo(w * 0.45f, h * 0.6f, w * 0.55f, h * 0.4f, w * 0.5f, h * 0.2f)
        }
        drawPath(leftBean, color = Color(0xFF3D2C24), style = Fill)

        // Grano de Café (Lado derecho claro)
        val rightBean = Path().apply {
            moveTo(w * 0.5f, h * 0.2f)
            cubicTo(w * 0.7f, h * 0.2f, w * 0.75f, h * 0.5f, w * 0.65f, h * 0.75f)
            cubicTo(w * 0.6f, h * 0.85f, w * 0.5f, h * 0.8f, w * 0.5f, h * 0.8f)
            cubicTo(w * 0.45f, h * 0.6f, w * 0.55f, h * 0.4f, w * 0.5f, h * 0.2f)
        }
        drawPath(rightBean, color = Color(0xFF8B5E3C), style = Fill)

        // Hojas verdes en la base
        val leftLeaf = Path().apply {
            moveTo(w * 0.5f, h * 0.85f)
            quadraticTo(w * 0.2f, h * 0.8f, w * 0.2f, h * 0.6f)
            quadraticTo(w * 0.4f, h * 0.55f, w * 0.5f, h * 0.85f)
        }
        drawPath(leftLeaf, color = Color(0xFF2D4F1E), style = Fill)

        val rightLeaf = Path().apply {
            moveTo(w * 0.5f, h * 0.85f)
            quadraticTo(w * 0.8f, h * 0.8f, w * 0.8f, h * 0.6f)
            quadraticTo(w * 0.6f, h * 0.55f, w * 0.5f, h * 0.85f)
        }
        drawPath(rightLeaf, color = Color(0xFF2D4F1E), style = Fill)
        
        // Venas de las hojas (Detalle dorado)
        drawPath(leftLeaf, color = Color(0xFFB47B5A), style = Stroke(width = 1.dp.toPx()))
        drawPath(rightLeaf, color = Color(0xFFB47B5A), style = Stroke(width = 1.dp.toPx()))
    }
}
