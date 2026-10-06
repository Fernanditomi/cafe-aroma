package com.example.cafe.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cafe.data.ServerConfig
import com.example.cafe.data.User
import com.example.cafe.ui.theme.AccentCinnamon
import com.example.cafe.ui.theme.PrimaryCoffee

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userViewModel: UserViewModel,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onLogout: () -> Unit,
    onChangeAccount: () -> Unit,
    onPlayGame: () -> Unit,
    onBack: () -> Unit
) {
    val currentUser by userViewModel.currentUser.collectAsState()
    val serverUrl by userViewModel.serverUrl.collectAsState()
    val serverReachable by userViewModel.serverReachable.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }

    var serverUrlInput by remember(serverUrl) { mutableStateOf(serverUrl) }
    var serverUrlError by remember { mutableStateOf<String?>(null) }
    
    var editName by remember(currentUser) { mutableStateOf(currentUser?.name ?: "") }
    var editAvatarUrl by remember(currentUser) { mutableStateOf(currentUser?.avatarUrl ?: "") }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Fondo decorativo sutil
        Canvas(modifier = Modifier.fillMaxSize()) {
            val circleColor = PrimaryCoffee.copy(alpha = 0.015f)
            drawCircle(circleColor, radius = 400f, center = androidx.compose.ui.geometry.Offset(size.width, 0f))
            drawCircle(circleColor, radius = 300f, center = androidx.compose.ui.geometry.Offset(0f, size.height))
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Ajustes", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = PrimaryCoffee) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = PrimaryCoffee)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Perfil con click para editar
                Box(modifier = Modifier.padding(24.dp)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { showEditDialog = true },
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(28.dp),
                        shadowElevation = 8.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryCoffee.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(75.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryCoffee.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (currentUser?.avatarUrl?.isNotEmpty() == true) {
                                    AsyncImage(
                                        model = currentUser?.avatarUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Default.Person, null, modifier = Modifier.size(45.dp), tint = PrimaryCoffee)
                                }
                                // Badge de edición
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .background(PrimaryCoffee, CircleShape)
                                        .size(22.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(20.dp))
                            Column {
                                Text(
                                    text = currentUser?.name ?: "Usuario",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryCoffee
                                )
                                Text(
                                    text = currentUser?.email ?: "",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    color = AccentCinnamon.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (currentUser?.isAdmin == true) "Administrador" else "Miembro Aroma",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentCinnamon
                                    )
                                }
                            }
                        }
                    }
                }

                SettingsSectionHeader("PERSONALIZACIÓN")
                SettingsItemStyled(
                    title = "Modo Oscuro",
                    subtitle = "Cambia el tema de la aplicación",
                    icon = Icons.Default.NightsStay,
                    iconBg = Color(0xFFE1F5FE),
                    iconTint = Color(0xFF0288D1),
                    trailing = {
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { onThemeToggle() },
                            colors = SwitchDefaults.colors(checkedTrackColor = PrimaryCoffee)
                        )
                    }
                )

                SettingsSectionHeader("SERVIDOR")
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    ServerStatusChip(state = serverReachable)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = serverUrlInput,
                        onValueChange = { serverUrlInput = it; serverUrlError = null },
                        label = { Text("URL de la API") },
                        leadingIcon = { Icon(Icons.Default.Cloud, contentDescription = null) },
                        singleLine = true,
                        isError = serverUrlError != null,
                        supportingText = {
                            Text(
                                text = serverUrlError
                                    ?: "Servidor usado por la app para validar el login",
                                fontSize = 12.sp
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                val normalized = ServerConfig.normalize(serverUrlInput)
                                serverUrlError = userViewModel.saveServerUrl(serverUrlInput)
                                if (serverUrlError == null) serverUrlInput = normalized
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoffee),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Guardar y probar")
                        }
                        OutlinedButton(
                            onClick = { userViewModel.refreshServerStatus() },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Probar conexión")
                        }
                    }
                }

                SettingsSectionHeader("CUENTA")
                SettingsItemStyled(
                    title = "Cambiar Cuenta",
                    subtitle = "Inicia con un usuario diferente",
                    icon = Icons.Default.SwitchAccount,
                    iconBg = Color(0xFFF3E5F5),
                    iconTint = Color(0xFF7B1FA2),
                    onClick = onChangeAccount
                )
                SettingsItemStyled(
                    title = "Cerrar Sesión",
                    subtitle = "Salir de Café Aroma de forma segura",
                    icon = Icons.Default.ExitToApp,
                    iconBg = Color(0xFFFFEBEE),
                    iconTint = Color(0xFFD32F2F),
                    onClick = onLogout
                )

                SettingsSectionHeader("APP")
                SettingsItemStyled(
                    title = "Versión",
                    subtitle = "Versión estable instalada",
                    icon = Icons.Default.Info,
                    iconBg = Color(0xFFE8F5E9),
                    iconTint = Color(0xFF388E3C),
                    trailing = { Text("1.0.0", fontWeight = FontWeight.Bold, color = Color.Gray) }
                )

                SettingsSectionHeader("DIVERSIÓN")
                SettingsItemStyled(
                    title = "Dino Coffee Run",
                    subtitle = "Supera tu récord de ${currentUser?.highScore ?: 0} pts",
                    icon = Icons.Default.SportsEsports,
                    iconBg = Color(0xFFFFF3E0),
                    iconTint = Color(0xFFF57C00),
                    onClick = onPlayGame
                )

                Spacer(modifier = Modifier.height(48.dp))
                CoffeeIllustrations()
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Diálogo para editar perfil
        if (showEditDialog) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Editar Perfil", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Nombre de usuario") },
                            leadingIcon = { Icon(Icons.Default.Person, null) },
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = editAvatarUrl,
                            onValueChange = { editAvatarUrl = it },
                            label = { Text("URL de foto de perfil") },
                            leadingIcon = { Icon(Icons.Default.Image, null) },
                            shape = RoundedCornerShape(12.dp),
                            placeholder = { Text("https://...") }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            userViewModel.updateProfile(
                                name = editName,
                                avatarUrl = editAvatarUrl,
                                phone = currentUser?.phone ?: "",
                                address = currentUser?.address ?: ""
                            )
                            showEditDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoffee)
                    ) {
                        Text("Guardar Cambios")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsItemStyled(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(iconBg, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(22.dp), tint = iconTint)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        trailing?.invoke() ?: Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black.copy(alpha = 0.6f)
    )
}
