package com.example.cafe.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cafe.ui.theme.AccentCinnamon
import com.example.cafe.ui.theme.PrimaryCoffee
import com.example.cafe.ui.theme.White
import com.example.cafe.utils.emailError
import com.example.cafe.utils.passwordError
import com.example.cafe.utils.passwordStrength
import com.example.cafe.utils.strengthLabel

private const val DEMO_ADMIN_EMAIL = "admin@cafearoma.com"
private const val DEMO_ADMIN_PASSWORD = "admin123"
private const val DEMO_CLIENT_EMAIL = "cliente@cafearoma.com"
private const val DEMO_CLIENT_PASSWORD = "cliente123"

@Composable
fun LoginScreen(
    viewModel: UserViewModel,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }

    val error by viewModel.error.collectAsState()
    val notice by viewModel.notice.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val serverReachable by viewModel.serverReachable.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.refreshServerStatus()
    }

    val emailProblem = if (submitted) emailError(email) else null
    val passwordProblem =
        if (submitted && password.isBlank()) "La contraseña es obligatoria." else null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            CafeAromaLogo(modifier = Modifier.size(110.dp))
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Iniciar Sesión",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryCoffee
            )
            Text(
                text = "Bienvenido de vuelta a Café Aroma.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            ServerStatusChip(state = serverReachable)

            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PremiumTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Correo electrónico",
                    helperText = "Ejemplo: nombre@correo.com",
                    errorText = emailProblem,
                    icon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )

                PremiumTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Contraseña",
                    errorText = passwordProblem,
                    icon = Icons.Default.Lock,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordToggle = { passwordVisible = !passwordVisible },
                    imeAction = ImeAction.Done,
                    onImeAction = { focusManager.clearFocus() },
                    enabled = !isLoading
                )
            }

            StatusBanner(message = error, isError = true)
            StatusBanner(message = notice, isError = false)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    submitted = true
                    if (emailError(email) == null && password.isNotBlank()) {
                        viewModel.login(email.trim(), password) { }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoffee),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Verificando...", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = White)
                } else {
                    Icon(
                        imageVector = Icons.Default.Coffee,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Entrar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            DemoAccountsCard(
                onSelect = { demoEmail, demoPassword ->
                    email = demoEmail
                    password = demoPassword
                    submitted = false
                    viewModel.clearError()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¿No tienes cuenta? ", color = Color.Gray)
                Text(
                    text = "Regístrate aquí",
                    color = PrimaryCoffee,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        viewModel.clearError()
                        onNavigateToRegister()
                    }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            CoffeeIllustrations()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RegisterScreen(
    viewModel: UserViewModel,
    onNavigateToLogin: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }

    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val focusManager = LocalFocusManager.current

    val nameError = if (submitted && step == 1 && name.trim().length < 3) "Ingresa tu nombre completo." else null
    val emailProblem = if (submitted && step == 1) emailError(email) else null
    val passwordProblem = if (submitted && step == 2) passwordError(password) else null
    val confirmProblem =
        if (submitted && step == 2 && confirmPassword != password) "Las contraseñas no coinciden." else null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            CafeAromaLogo(modifier = Modifier.size(90.dp))
            Spacer(modifier = Modifier.height(20.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Paso $step de 2: ${if (step == 1) "Datos básicos" else "Seguridad"}",
                    fontSize = 14.sp,
                    color = PrimaryCoffee,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { if (step == 1) 0.5f else 1.0f },
                    modifier = Modifier
                        .width(200.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = AccentCinnamon,
                    trackColor = AccentCinnamon.copy(alpha = 0.2f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Crear Cuenta",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryCoffee
            )
            Text(
                text = "Regístrate para disfrutar de todos los beneficios.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    }
                }, label = ""
            ) { currentStep ->
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (currentStep == 1) {
                        PremiumTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = "Nombre completo",
                            helperText = "Ejemplo: Juan Pérez",
                            errorText = nameError,
                            icon = Icons.Default.Person,
                            imeAction = ImeAction.Next
                        )

                        PremiumTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Correo electrónico",
                            helperText = "Ejemplo: nombre@correo.com",
                            errorText = emailProblem,
                            icon = Icons.Default.Email,
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )

                        PremiumTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = "Teléfono (opcional)",
                            helperText = "Ejemplo: +503 7000-0000",
                            icon = Icons.Default.Phone,
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done,
                            onImeAction = { focusManager.clearFocus() }
                        )
                    } else {
                        PremiumTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = "Contraseña",
                            helperText = "Mínimo 8 caracteres con letras y números",
                            errorText = passwordProblem,
                            icon = Icons.Default.Lock,
                            isPassword = true,
                            passwordVisible = passwordVisible,
                            onPasswordToggle = { passwordVisible = !passwordVisible },
                            imeAction = ImeAction.Next
                        )

                        PasswordStrengthBar(password = password)

                        PremiumTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = "Confirmar contraseña",
                            errorText = confirmProblem,
                            icon = Icons.Default.Lock,
                            isPassword = true,
                            passwordVisible = passwordVisible,
                            onPasswordToggle = { passwordVisible = !passwordVisible },
                            imeAction = ImeAction.Done,
                            onImeAction = { focusManager.clearFocus() },
                            enabled = !isLoading
                        )
                    }
                }
            }

            StatusBanner(message = error, isError = true)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (step == 1) {
                        submitted = true
                        if (name.trim().length >= 3 && emailError(email) == null) {
                            submitted = false
                            step = 2
                        }
                    } else {
                        submitted = true
                        if (passwordError(password) == null && password == confirmPassword) {
                            focusManager.clearFocus()
                            viewModel.register(name.trim(), email.trim(), phone.trim(), password) { }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoffee),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Creando cuenta...", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = White)
                } else {
                    Icon(
                        imageVector = Icons.Default.Coffee,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (step == 1) "Continuar →" else "Registrarse",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
            }

            if (step == 2) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "← Volver",
                    color = PrimaryCoffee,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        submitted = false
                        viewModel.clearError()
                        step = 1
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¿Ya tienes cuenta? ", color = Color.Gray)
                Text(
                    text = "Inicia sesión aquí",
                    color = PrimaryCoffee,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        viewModel.clearError()
                        onNavigateToLogin()
                    }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            CoffeeIllustrations()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ServerStatusChip(state: Boolean?) {
    val (color, text) = when (state) {
        true -> Color(0xFF2E7D32) to "Servidor en línea"
        false -> Color(0xFFE65100) to "Sin conexión · modo local"
        null -> Color.Gray to "Comprobando servidor..."
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.10f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, fontSize = 12.sp, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun StatusBanner(message: String?, isError: Boolean) {
    if (message.isNullOrBlank()) return
    val color = if (isError) Color(0xFFC62828) else Color(0xFFE65100)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isError) Icons.Default.ErrorOutline else Icons.Default.Info,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(message, fontSize = 13.sp, color = color)
        }
    }
}

@Composable
fun DemoAccountsCard(onSelect: (String, String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = PrimaryCoffee.copy(alpha = 0.06f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Cuentas de prueba",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryCoffee
            )
            Spacer(modifier = Modifier.height(8.dp))
            DemoAccountRow("Administrador", DEMO_ADMIN_EMAIL, DEMO_ADMIN_PASSWORD, onSelect)
            Spacer(modifier = Modifier.height(6.dp))
            DemoAccountRow("Cliente", DEMO_CLIENT_EMAIL, DEMO_CLIENT_PASSWORD, onSelect)
        }
    }
}

@Composable
private fun DemoAccountRow(
    role: String,
    email: String,
    password: String,
    onSelect: (String, String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(email, password) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            tint = PrimaryCoffee,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(role, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
            Text("$email · $password", fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun PasswordStrengthBar(password: String) {
    val strength = passwordStrength(password)
    if (password.isEmpty()) return
    val color = when (strength) {
        1 -> Color(0xFFC62828)
        2 -> Color(0xFFF9A825)
        3 -> Color(0xFF2E7D32)
        else -> Color(0xFF1565C0)
    }
    Column(modifier = Modifier.padding(start = 4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .background(
                            if (index < strength) color else color.copy(alpha = 0.15f),
                            RoundedCornerShape(3.dp)
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Seguridad: ${strengthLabel(strength)}",
            fontSize = 11.sp,
            color = color
        )
    }
}

@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isFocused: Boolean = false,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    helperText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true
) {
    val focusManager = LocalFocusManager.current
    val isError = errorText != null

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isFocused) PrimaryCoffee.copy(alpha = 0.02f) else Color.Transparent,
                RoundedCornerShape(16.dp)
            ),
        label = { Text(label, fontSize = 14.sp) },
        supportingText = {
            when {
                isError -> Text(errorText!!, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                helperText != null -> Text(helperText, fontSize = 12.sp, color = Color.Gray)
            }
        },
        isError = isError,
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp)) },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = onPasswordToggle) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            }
        },
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
            onDone = {
                focusManager.clearFocus()
                onImeAction()
            }
        ),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryCoffee,
            unfocusedBorderColor = if (isFocused) PrimaryCoffee.copy(alpha = 0.5f) else Color.LightGray,
            focusedLabelColor = PrimaryCoffee,
            cursorColor = PrimaryCoffee
        ),
        singleLine = true
    )
}

@Composable
fun CoffeeIllustrations() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.2f),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        Icon(Icons.Default.CoffeeMaker, null, modifier = Modifier.size(40.dp))
        Icon(Icons.Default.LocalCafe, null, modifier = Modifier.size(30.dp))
        Icon(Icons.Default.Coffee, null, modifier = Modifier.size(35.dp))
        Icon(Icons.Default.Restaurant, null, modifier = Modifier.size(30.dp))
    }
}

// Extension para simplificar colores
fun Color.withAlpha(alpha: Float): Color = this.copy(alpha = alpha)
