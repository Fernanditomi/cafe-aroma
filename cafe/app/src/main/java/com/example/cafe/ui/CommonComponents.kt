package com.example.cafe.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cafe.ui.theme.AccentCinnamon
import com.example.cafe.ui.theme.PrimaryCoffee
import com.example.cafe.ui.theme.White

@Composable
fun HeaderSection(
    userName: String, 
    avatarUrl: String = "", 
    onAvatarClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Parte Izquierda: Perfil y Saludo
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f).clickable { onAvatarClick() }
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(PrimaryCoffee.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                if (avatarUrl.isNotEmpty()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person, 
                        contentDescription = null, 
                        tint = PrimaryCoffee, 
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column {
                Text(
                    text = "Hola, $userName 👋",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryCoffee,
                    lineHeight = 22.sp
                )
                Text(
                    text = "☕ ¿Qué se te antoja hoy?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
            }
        }
        
        // Parte Derecha: Botones de Acción
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.White, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications, 
                    contentDescription = "Notificaciones", 
                    tint = PrimaryCoffee, 
                    modifier = Modifier.size(22.dp)
                )
            }
            
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(42.dp)
                    .background(PrimaryCoffee, RoundedCornerShape(14.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Ajustes",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SearchBarStyled(query: String, onQueryChange: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, Color.Gray.copy(alpha = 0.8f)) // Borde gris como en el dibujo
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { 
                Text(
                    text = "Buscar café, postre...", 
                    color = Color.Gray.copy(alpha = 0.6f),
                    fontSize = 16.sp
                ) 
            },
            leadingIcon = { 
                Box(
                    modifier = Modifier.padding(start = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Círculo dividido inspirado en el grano de café del logo
                    Canvas(modifier = Modifier.size(36.dp)) {
                        // Lado izquierdo (Oscuro)
                        drawArc(
                            color = PrimaryCoffee,
                            startAngle = 90f,
                            sweepAngle = 180f,
                            useCenter = true
                        )
                        // Lado derecho (Claro)
                        drawArc(
                            color = Color(0xFF8B5E3C),
                            startAngle = 270f,
                            sweepAngle = 180f,
                            useCenter = true
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.Search, 
                        contentDescription = null, 
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    ) 
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = PrimaryCoffee
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SectionHeader(title: String, onActionClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Ver todo →",
            fontSize = 14.sp,
            color = AccentCinnamon,
            modifier = Modifier.clickable { onActionClick() }
        )
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "NUEVO" -> Color(0xFF1976D2)
        "CONFIRMADO" -> Color(0xFFFBC02D)
        "PREPARANDO" -> Color(0xFF7B1FA2)
        "LISTO" -> Color(0xFF388E3C)
        "ENTREGADO" -> Color(0xFF388E3C)
        "CANCELADO" -> Color.Red
        else -> Color.Gray
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun UserSection(
    userName: String,
    isLoggedIn: Boolean,
    onLogoutClick: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "User",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = userName,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp
                )
            }
            
            if (isLoggedIn) {
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Ajustes",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    currentTab: String = "Inicio",
    onTabSelected: (String) -> Unit = {}
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(32.dp),
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavButton("Inicio", Icons.Default.Home, isActive = currentTab == "Inicio", onClick = { onTabSelected("Inicio") })
            NavButton("Menú", Icons.Default.Menu, isActive = currentTab == "Menú", onClick = { onTabSelected("Menú") })
            NavButton("Favoritos", Icons.Default.Favorite, isActive = currentTab == "Favoritos", onClick = { onTabSelected("Favoritos") })
            NavButton("Pedidos", Icons.Default.ListAlt, isActive = currentTab == "Pedidos", onClick = { onTabSelected("Pedidos") })
        }
    }
}

@Composable
fun NavButton(label: String, icon: ImageVector, isActive: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
        shape = RoundedCornerShape(24.dp),
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
            if (isActive) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
