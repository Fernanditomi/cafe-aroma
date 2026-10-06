package com.example.cafe.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cafe.ui.theme.AccentCinnamon
import com.example.cafe.ui.theme.CafeTheme
import com.example.cafe.ui.theme.PrimaryCoffee
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

data class Obstacle(
    val x: Float,
    val width: Float = 40f,
    val height: Float = 50f,
    val isAerial: Boolean = false,
    val type: Int = Random.nextInt(3) // Different visuals
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float, // 1.0 to 0.0
    val color: Color,
    val size: Float,
    val isBean: Boolean = Random.nextBoolean()
)

data class Cloud(
    var x: Float,
    var y: Float,
    val speed: Float,
    val scale: Float
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun DinoRunScreen(
    userRecord: Int,
    initialDarkTheme: Boolean,
    onUpdateRecord: (Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    
    var isPlaying by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var highScore by remember { mutableStateOf(userRecord) }
    var dinoY by remember { mutableStateOf(0f) }
    var velocityY by remember { mutableStateOf(0f) }
    val obstacles = remember { mutableStateListOf<Obstacle>() }
    val particles = remember { mutableStateListOf<Particle>() }
    val clouds = remember { mutableStateListOf<Cloud>() }
    var gameOver by remember { mutableStateOf(false) }
    var isNewRecord by remember { mutableStateOf(false) }
    
    // Animation states
    val scoreScale = remember { Animatable(1f) }
    val screenShake = remember { Animatable(0f) }
    
    // Progresión
    val level = (score / 50) + 1
    val progressInLevel = (score % 50) / 50f
    
    val gravity = 1.8f
    val jumpStrength = -28f
    val groundY = 0f

    var isDarkState by remember { mutableStateOf(initialDarkTheme) }
    
    // Cambio automático cada 500 puntos
    val currentThreshold = score / 500
    LaunchedEffect(currentThreshold) {
        if (score > 0) {
            isDarkState = !isDarkState
        }
    }

    // Animación de transición de colores
    val animatedBgStart by animateColorAsState(
        targetValue = if (isDarkState) Color(0xFF1B1714) else Color(0xFFFDFBF7),
        animationSpec = tween(1000),
        label = "bgStart"
    )
    val animatedBgEnd by animateColorAsState(
        targetValue = if (isDarkState) Color(0xFF2B2420) else Color(0xFFEFEBE4),
        animationSpec = tween(1000),
        label = "bgEnd"
    )
    val animatedSurfaceColor by animateColorAsState(
        targetValue = if (isDarkState) Color(0xFF3D342F) else Color(0xFFFFFFFF),
        animationSpec = tween(1000),
        label = "surface"
    )

    // Sync record
    LaunchedEffect(userRecord) { highScore = userRecord }

    // Init clouds
    if (clouds.isEmpty()) {
        repeat(5) {
            clouds.add(Cloud(Random.nextFloat() * 1200f, 50f + Random.nextFloat() * 150f, 1f + Random.nextFloat() * 2f, 0.5f + Random.nextFloat()))
        }
    }

    @Suppress("MissingPermission")
    fun triggerVibration(duration: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(duration)
            }
        } catch (e: Exception) { }
    }

    LaunchedEffect(isPlaying, gameOver) {
        if (!isPlaying || gameOver) return@LaunchedEffect
        
        if (obstacles.isEmpty()) obstacles.add(Obstacle(1200f))
        
        var frameCounter = 0
        while (isPlaying && !gameOver) {
            withFrameNanos { _ ->
                // Physics
                dinoY += velocityY
                velocityY += gravity
                if (dinoY >= groundY) {
                    dinoY = groundY
                    velocityY = 0f
                }

                val difficultyLevel = score / 50
                val speedMultiplier = (1f + difficultyLevel * 0.04f).coerceAtMost(5f)
                val currentSpeed = 12f * speedMultiplier
                
                frameCounter++
                if (frameCounter >= 10) {
                    score += 1
                    if (score > highScore) {
                        if (!isNewRecord && highScore > 0) isNewRecord = true
                        highScore = score
                    }
                    frameCounter = 0
                }

                // Clouds parallax
                clouds.forEachIndexed { i, cloud ->
                    cloud.x -= cloud.speed
                    if (cloud.x < -200f) {
                        clouds[i] = cloud.copy(x = 1300f, y = 50f + Random.nextFloat() * 150f)
                    }
                }

                // Particles logic
                for (i in particles.indices.reversed()) {
                    val p = particles[i]
                    p.x += p.vx
                    p.y += p.vy
                    p.vy += 0.1f // small gravity
                    p.life -= 0.02f
                    if (p.life <= 0) particles.removeAt(i)
                }

                // Obstacles
                for (i in obstacles.indices.reversed()) {
                    val obs = obstacles[i]
                    val newX = obs.x - currentSpeed
                    
                    // Collision
                    val isCollisionX = newX in 80f..140f
                    val isCollisionY = if (obs.isAerial) {
                        dinoY < -40f && dinoY > -160f 
                    } else {
                        dinoY > -obs.height + 10f
                    }

                    if (isCollisionX && isCollisionY) {
                        gameOver = true
                        isPlaying = false
                        onUpdateRecord(highScore)
                        triggerVibration(150)
                        // Shake screen
                        launch {
                            repeat(5) {
                                screenShake.animateTo(15f, tween(50))
                                screenShake.animateTo(-15f, tween(50))
                            }
                            screenShake.animateTo(0f, tween(50))
                        }
                        break
                    }
                    
                    if (newX < -100f) {
                        obstacles.removeAt(i)
                    } else {
                        obstacles[i] = obs.copy(x = newX)
                    }
                }

                if (!gameOver) {
                    val lastX = obstacles.lastOrNull()?.x ?: 0f
                    val minGap = 500f - (difficultyLevel * 10f).coerceAtLeast(250f)
                    if (lastX < 1200f - minGap) {
                        if (Random.nextFloat() > 0.98f - (difficultyLevel * 0.002f)) {
                            val isAerial = score > 150 && Random.nextFloat() > 0.8f
                            obstacles.add(Obstacle(1200f, isAerial = isAerial))
                        }
                    }
                }
            }
        }
    }

    CafeTheme(darkTheme = isDarkState) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val gameTitle = remember(score) {
                            val level = (score / 50) + 1
                            when {
                                level < 3 -> "Dino Coffee Run"
                                level < 5 -> "Espresso Dash"
                                else -> "Dino Latte"
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Coffee, null, tint = PrimaryCoffee, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(gameTitle, fontWeight = FontWeight.ExtraBold)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    actions = {
                        IconButton(onClick = { isDarkState = !isDarkState }) {
                            Icon(if (isDarkState) Icons.Default.LightMode else Icons.Default.DarkMode, null)
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Brush.verticalGradient(listOf(animatedBgStart, animatedBgEnd)))
                    .offset(x = screenShake.value.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { _, dragAmount ->
                                if (dragAmount.y < -15f && dinoY == groundY && isPlaying && !gameOver) {
                                    velocityY = jumpStrength
                                    triggerVibration(20)
                                } else if (dragAmount.y > 10f && dinoY < groundY) {
                                    velocityY += 8f
                                }
                            }
                        )
                    }
                    .clickable {
                        if (gameOver) {
                            gameOver = false
                            isNewRecord = false
                            score = 0
                            obstacles.clear()
                            particles.clear()
                            dinoY = 0f
                            velocityY = 0f
                            isPlaying = true
                        } else if (!isPlaying) {
                            isPlaying = true
                        } else if (dinoY == groundY) {
                            velocityY = jumpStrength
                        }
                    }
            ) {
                // UI Elements
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Score Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ScoreCard(
                            label = "PUNTAJE",
                            value = score,
                            icon = Icons.Default.Coffee,
                            color = if (isDarkState) Color.White else PrimaryCoffee,
                            modifier = Modifier.weight(1f)
                        )
                        ScoreCard(
                            label = "RÉCORD",
                            value = highScore,
                            icon = Icons.Default.EmojiEvents,
                            color = Color(0xFFFFD700),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Game Area (Subido al quitar la barra y reducir el spacer)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp) // Aumentado un poco el alto del área de juego
                            .clip(RoundedCornerShape(24.dp))
                            .background(animatedSurfaceColor.copy(alpha = 0.6f))
                    ) {
                        GameCanvas(
                            dinoY = dinoY,
                            obstacles = obstacles,
                            particles = particles,
                            clouds = clouds,
                            currentTheme = isDarkState,
                            score = score,
                            isPlaying = isPlaying,
                            gameOver = gameOver
                        )
                        
                        if (!isPlaying && !gameOver) {
                            StartOverlay()
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "¡Desliza arriba para saltar!",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(Modifier.height(48.dp))
                }

                // Game Over Overlay
                if (gameOver) {
                    GameOverScreen(
                        score = score,
                        record = highScore,
                        isNewRecord = isNewRecord,
                        onRetry = {
                            gameOver = false
                            isNewRecord = false
                            score = 0
                            obstacles.clear()
                            particles.clear()
                            dinoY = 0f
                            velocityY = 0f
                            isPlaying = true
                        },
                        onShare = {
                            val shareMessage = "☕🦕 ¡Hice $score puntos en Dino Latte!\n🏆 Mi récord es $highScore.\n¿Puedes superarme? #CafeAroma"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareMessage)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartir récord"))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ScoreCard(label: String, value: Int, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Text(text = value.toString(), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        }
    }
}

@Composable
fun DifficultyIndicator(level: Int, progress: Float, currentTheme: Boolean) {
    Column(modifier = Modifier.fillMaxWidth(0.8f), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "☕ NIVEL $level",
                fontWeight = FontWeight.ExtraBold,
                color = if (currentTheme) Color.White else PrimaryCoffee,
                fontSize = 14.sp
            )
            Text(
                text = "Next: ${(50 - (progress * 50)).toInt()} pts",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
            color = AccentCinnamon,
            trackColor = AccentCinnamon.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun GameCanvas(
    dinoY: Float,
    obstacles: List<Obstacle>,
    particles: List<Particle>,
    clouds: List<Cloud>,
    currentTheme: Boolean,
    score: Int,
    isPlaying: Boolean,
    gameOver: Boolean
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val groundLevel = height * 0.85f

        // Draw Clouds
        val cloudColor = if (currentTheme) Color.White.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.15f)
        clouds.forEach { cloud ->
            withTransform({
                translate(cloud.x, cloud.y)
                scale(cloud.scale, cloud.scale, Offset.Zero)
            }) {
                drawCircle(cloudColor, radius = 40f, center = Offset(0f, 0f))
                drawCircle(cloudColor, radius = 35f, center = Offset(30f, -10f))
                drawCircle(cloudColor, radius = 35f, center = Offset(-30f, -10f))
            }
        }

        // Draw ground decoration
        drawLine(
            color = if (currentTheme) Color.Gray else Color.LightGray,
            start = Offset(0f, groundLevel),
            end = Offset(width, groundLevel),
            strokeWidth = 4f
        )
        
        // Dino position
        val dinoX = 100f
        val dinoRealY = groundLevel + dinoY
        
        // Draw Particles
        particles.forEach { p ->
            if (p.isBean) {
                drawOval(p.color.copy(alpha = p.life), Offset(p.x, p.y), Size(p.size, p.size * 1.4f))
            } else {
                drawCircle(p.color.copy(alpha = p.life), radius = p.size / 2, center = Offset(p.x, p.y))
            }
        }

        // Draw "Coffee Dino"
        drawCoffeeDino(dinoX, dinoRealY, currentTheme, dinoY < 0f, gameOver)

        // Draw Obstacles
        obstacles.forEach { obs ->
            val obsY = if (obs.isAerial) groundLevel - 150f else groundLevel - obs.height
            drawCoffeeObstacle(obs.x, obsY, obs)
        }
    }
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCoffeeDino(
    x: Float, y: Float, isDark: Boolean, isJumping: Boolean, isDead: Boolean
) {
    val beanWidth = 50f
    val beanHeight = 65f
    val beanX = x
    val beanY = y - beanHeight

    // Grano de Café (Lado izquierdo oscuro)
    val leftBean = Path().apply {
        moveTo(beanX + beanWidth * 0.5f, beanY + beanHeight * 0.05f)
        cubicTo(beanX + beanWidth * 0.1f, beanY + beanHeight * 0.1f, beanX + beanWidth * 0.05f, beanY + beanHeight * 0.5f, beanX + beanWidth * 0.2f, beanY + beanHeight * 0.95f)
        cubicTo(beanX + beanWidth * 0.35f, beanY + beanHeight * 1.05f, beanX + beanWidth * 0.5f, beanY + beanHeight * 0.95f, beanX + beanWidth * 0.5f, beanY + beanHeight * 0.95f)
        cubicTo(beanX + beanWidth * 0.45f, beanY + beanHeight * 0.6f, beanX + beanWidth * 0.55f, beanY + beanHeight * 0.4f, beanX + beanWidth * 0.5f, beanY + beanHeight * 0.05f)
    }
    drawPath(leftBean, color = if(isDead) Color.DarkGray else Color(0xFF3D2C24), style = Fill)

    // Grano de Café (Lado derecho claro)
    val rightBean = Path().apply {
        moveTo(beanX + beanWidth * 0.5f, beanY + beanHeight * 0.05f)
        cubicTo(beanX + beanWidth * 0.9f, beanY + beanHeight * 0.1f, beanX + beanWidth * 0.95f, beanY + beanHeight * 0.5f, beanX + beanWidth * 0.8f, beanY + beanHeight * 0.95f)
        cubicTo(beanX + beanWidth * 0.65f, beanY + beanHeight * 1.05f, beanX + beanWidth * 0.5f, beanY + beanHeight * 0.95f, beanX + beanWidth * 0.5f, beanY + beanHeight * 0.95f)
        cubicTo(beanX + beanWidth * 0.45f, beanY + beanHeight * 0.6f, beanX + beanWidth * 0.55f, beanY + beanHeight * 0.4f, beanX + beanWidth * 0.5f, beanY + beanHeight * 0.05f)
    }
    drawPath(rightBean, color = if(isDead) Color.Gray else Color(0xFF8B5E3C), style = Fill)

    // Eyes
    if (isDead) {
        // X eyes
        val eyeY = beanY + 30f
        drawLine(Color.White, Offset(beanX + 15f, eyeY - 5f), Offset(beanX + 23f, eyeY + 5f), 3f)
        drawLine(Color.White, Offset(beanX + 23f, eyeY - 5f), Offset(beanX + 15f, eyeY + 5f), 3f)
        
        drawLine(Color.White, Offset(beanX + 32f, eyeY - 5f), Offset(beanX + 40f, eyeY + 5f), 3f)
        drawLine(Color.White, Offset(beanX + 40f, eyeY - 5f), Offset(beanX + 32f, eyeY + 5f), 3f)
    } else {
        // Happy eyes
        drawCircle(Color.White, 4f, Offset(beanX + 18f, beanY + 35f))
        drawCircle(Color.White, 4f, Offset(beanX + 35f, beanY + 35f))
        
        // Small legs (running animation simplified)
        val legOffset = if (!isJumping && (System.currentTimeMillis() / 150 % 2 == 0L)) 5f else 0f
        drawLine(Color(0xFF3D2C24), Offset(beanX + 15f, beanY + beanHeight), Offset(beanX + 15f - legOffset, beanY + beanHeight + 10f), 4f)
        drawLine(Color(0xFF3D2C24), Offset(beanX + 35f, beanY + beanHeight), Offset(beanX + 35f + legOffset, beanY + beanHeight + 10f), 4f)
    }
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCoffeeObstacle(
    x: Float, y: Float, obs: Obstacle
) {
    if (obs.isAerial) {
        // A "flying donut" or "sugar cube"
        drawCircle(Color(0xFFE91E63), 20f, Offset(x + 20f, y + 20f))
        drawCircle(Color.White, 8f, Offset(x + 20f, y + 20f)) // Donut hole
    } else {
        // Cacti with coffee colors or "Coffee Bags"
        val color = when(obs.type) {
            0 -> Color(0xFF388E3C) // Green cactus
            1 -> Color(0xFF795548) // Brown coffee bag
            else -> Color(0xFFBCAAA4) // Beige bag
        }
        drawRoundRect(color, Offset(x, y), Size(obs.width, obs.height), androidx.compose.ui.geometry.CornerRadius(8f))
        // Detail on bag/cactus
        if (obs.type == 1) {
            drawCircle(Color.White.copy(0.3f), 5f, Offset(x + obs.width/2, y + 15f))
        }
    }
}

@Composable
fun StartOverlay() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Surface(
            color = Color.Black.copy(alpha = 0.4f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "¡TOCA PARA EMPEZAR!",
                modifier = Modifier.padding(24.dp),
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun GameOverScreen(
    score: Int,
    record: Int,
    isNewRecord: Boolean,
    onRetry: () -> Unit,
    onShare: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.85f).padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(32.dp),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "── GAME OVER ──",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Red
                )
                
                Spacer(Modifier.height(24.dp))
                
                Text("☕😢", fontSize = 48.sp)
                
                Spacer(Modifier.height(24.dp))
                
                if (isNewRecord) {
                    Text(
                        text = "🎉 ¡NUEVO RÉCORD! 🎉",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                    Spacer(Modifier.height(8.dp))
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PUNTAJE", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("$score", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryCoffee)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("RÉCORD", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("$record", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFD700))
                    }
                }
                
                Spacer(Modifier.height(40.dp))
                
                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoffee),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, null)
                        Spacer(Modifier.width(12.dp))
                        Text("REINTENTAR", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                TextButton(onClick = onShare) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Compartir Récord", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
