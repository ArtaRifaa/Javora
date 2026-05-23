package com.yarsi.javora.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yarsi.javora.ui.theme.*

@Composable
fun JavoraLogo(size: Dp = 240.dp, showText: Boolean = true) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo part
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            // Background Circle/Arc
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    brush = JavoraGradient,
                    startAngle = -60f,
                    sweepAngle = 280f,
                    useCenter = false,
                    style = Stroke(width = (size / 80).toPx(), cap = StrokeCap.Round),
                    size = Size(this.size.width * 0.75f, this.size.height * 0.75f),
                    topLeft = Offset(this.size.width * 0.125f, this.size.height * 0.125f)
                )

                // Draw stars
                drawStar(Offset(this.size.width * 0.85f, this.size.height * 0.2f), (size / 30).toPx() / 2, JavoraOrange)
                drawStar(Offset(this.size.width * 0.15f, this.size.height * 0.5f), (size / 40).toPx() / 2, JavoraPink)
                drawStar(Offset(this.size.width * 0.88f, this.size.height * 0.65f), (size / 35).toPx() / 2, JavoraPurple)
            }

            // The stylized 'J'
            Text(
                text = "J",
                fontSize = (size.value * 0.58).sp,
                fontWeight = FontWeight.Black,
                style = androidx.compose.ui.text.TextStyle(brush = JavoraGradient),
                modifier = Modifier.offset(x = (size / 24), y = -(size / 24))
            )

            // Icons
            Text(
                text = "</>",
                fontSize = (size.value * 0.11).sp,
                fontWeight = FontWeight.Bold,
                style = androidx.compose.ui.text.TextStyle(brush = JavoraGradient),
                modifier = Modifier.align(Alignment.CenterStart).offset(x = (size / 12), y = -(size / 12))
            )

            // Java Coffee Cup
            Canvas(modifier = Modifier.size(size / 6).align(Alignment.CenterEnd).offset(x = -(size / 6), y = (size / 12))) {
                val cupBrush = Brush.verticalGradient(listOf(JavoraPink, JavoraPurple))
                drawPath(
                    path = Path().apply {
                        moveTo(this@Canvas.size.width * 0.2f, this@Canvas.size.height * 0.4f)
                        lineTo(this@Canvas.size.width * 0.8f, this@Canvas.size.height * 0.4f)
                        quadraticTo(this@Canvas.size.width * 0.8f, this@Canvas.size.height * 0.8f, this@Canvas.size.width * 0.5f, this@Canvas.size.height * 0.8f)
                        quadraticTo(this@Canvas.size.width * 0.2f, this@Canvas.size.height * 0.8f, this@Canvas.size.width * 0.2f, this@Canvas.size.height * 0.4f)
                    },
                    brush = cupBrush
                )
                drawPath(
                    path = Path().apply {
                        moveTo(this@Canvas.size.width * 0.4f, this@Canvas.size.height * 0.3f)
                        quadraticTo(this@Canvas.size.width * 0.5f, this@Canvas.size.height * 0.1f, this@Canvas.size.width * 0.4f, 0f)
                        moveTo(this@Canvas.size.width * 0.6f, this@Canvas.size.height * 0.3f)
                        quadraticTo(this@Canvas.size.width * 0.7f, this@Canvas.size.height * 0.1f, this@Canvas.size.width * 0.6f, 0f)
                    },
                    brush = cupBrush,
                    style = Stroke(width = (size / 120).toPx(), cap = StrokeCap.Round)
                )
            }
        }

        if (showText) {
            Spacer(modifier = Modifier.height(size / 8))
            Text(
                text = "Javora",
                fontSize = (size.value * 0.23).sp,
                fontWeight = FontWeight.Bold,
                style = androidx.compose.ui.text.TextStyle(brush = JavoraGradient)
            )
            Spacer(modifier = Modifier.height(size / 20))
            TaglineRow(size)
        }
    }
}

@Composable
fun TaglineRow(size: Dp) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.width(size / 5).height(2.dp)) {
            drawLine(brush = Brush.linearGradient(listOf(JavoraOrange, JavoraPink)), start = Offset(0f, this.size.height / 2), end = Offset(this.size.width, this.size.height / 2), strokeWidth = 2.dp.toPx())
        }
        Text(text = " Quiz Pintar Java ", fontSize = (size.value * 0.08).sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
        Canvas(modifier = Modifier.width(size / 5).height(2.dp)) {
            drawLine(brush = Brush.linearGradient(listOf(JavoraPink, JavoraPurple)), start = Offset(0f, this.size.height / 2), end = Offset(this.size.width, this.size.height / 2), strokeWidth = 2.dp.toPx())
        }
    }
}

@Composable
fun JavoraSplashScreen() {
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.7f,
        animationSpec = tween(durationMillis = 1000, easing = OvershootInterpolator().toEasing()),
        label = "LogoScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "LogoAlpha"
    )

    LaunchedEffect(Unit) { startAnimation = true }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.scale(scale).alpha(alpha)) { JavoraLogo() }
    }
}

private fun OvershootInterpolator() = android.view.animation.OvershootInterpolator(2f)
private fun android.view.animation.Interpolator.toEasing() = Easing { getInterpolation(it) }

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStar(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        for (i in 0 until 4) {
            val angle = i * Math.PI / 2
            val x = center.x + Math.cos(angle).toFloat() * radius * 2
            val y = center.y + Math.sin(angle).toFloat() * radius * 2
            if (i == 0) moveTo(x, y) else lineTo(x, y)
            val innerAngle = angle + Math.PI / 4
            val ix = center.x + Math.cos(innerAngle).toFloat() * radius
            val iy = center.y + Math.sin(innerAngle).toFloat() * radius
            lineTo(ix, iy)
        }
        close()
    }
    drawPath(path, color)
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun JavoraSplashScreenPreview() {
    JavoraTheme { JavoraSplashScreen() }
}
