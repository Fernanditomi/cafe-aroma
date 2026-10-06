package com.example.cafe.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.cafe.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CafeAromaApp() {
    val navController = rememberNavController()
    val userViewModel: UserViewModel = viewModel()
    val cartViewModel: CartViewModel = viewModel()
    val menuViewModel: MenuViewModel = viewModel()
    val adminViewModel: AdminViewModel = viewModel()
    val shopConfigViewModel: ShopConfigViewModel = viewModel()
    val currentUser by userViewModel.currentUser.collectAsState()
    val sessionReady by userViewModel.sessionReady.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val database = remember { com.example.cafe.data.AppDatabase.getDatabase(context) }

    var isDarkTheme by remember { mutableStateOf(false) }

    // La navegacion se deriva del estado de sesion: no hay sesion -> login,
    // hay sesion y estas en login/registro -> panel correspondiente.
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    LaunchedEffect(sessionReady, currentUser, currentRoute) {
        if (!sessionReady || currentRoute == null) return@LaunchedEffect
        val onAuthRoute = currentRoute == "login" || currentRoute == "register"
        when {
            currentUser == null && !onAuthRoute -> {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
            currentUser != null && onAuthRoute -> {
                val target = if (currentUser?.isAdmin == true) "admin_dashboard" else "home"
                navController.navigate(target) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    CafeTheme(darkTheme = isDarkTheme) {
        NavHost(navController = navController, startDestination = "login") {
            composable("login") {
                if (!sessionReady) {
                    SessionSplash()
                } else {
                    LoginScreen(
                        viewModel = userViewModel,
                        onNavigateToRegister = {
                            userViewModel.clearError()
                            navController.navigate("register")
                        }
                    )
                }
            }

            composable("register") {
                if (!sessionReady) {
                    SessionSplash()
                } else {
                    RegisterScreen(
                        viewModel = userViewModel,
                        onNavigateToLogin = {
                            userViewModel.clearError()
                            navController.navigate("login") {
                                popUpTo("register") { inclusive = true }
                            }
                        }
                    )
                }
            }

            composable("home") {
                var showExitDialog by remember { mutableStateOf(false) }
                BackHandler { showExitDialog = true }
                val user by userViewModel.currentUser.collectAsState()
                LaunchedEffect(user) {
                    user?.name?.let { cartViewModel.fetchUserOrders(it) }
                }

                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    MainScreen(
                        userViewModel = userViewModel,
                        menuViewModel = menuViewModel,
                        onExploreMenu = { navController.navigate("menu") },
                        onNavigateToSettings = { navController.navigate("settings") },
                        onNavigateToNotifications = { navController.navigate("notifications") },
                        onNavigateToProfile = { navController.navigate("profile") }
                    )
                    NavigationBar(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
                        currentTab = "Inicio",
                        onTabSelected = { tab ->
                            if (tab == "Menú") navController.navigate("menu")
                            if (tab == "Favoritos") navController.navigate("favorites")
                            if (tab == "Pedidos") navController.navigate("user_orders")
                        }
                    )
                }

                if (showExitDialog) {
                    ExitDialog(onConfirm = { userViewModel.logout() }, onDismiss = { showExitDialog = false })
                }
            }

            composable("menu") {
                val user by userViewModel.currentUser.collectAsState()
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    MenuScreen(
                        userId = user?.id ?: 0,
                        cartViewModel = cartViewModel,
                        menuViewModel = menuViewModel,
                        onNavigateToCart = { navController.navigate("cart") }
                    )
                    NavigationBar(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
                        currentTab = "Menú",
                        onTabSelected = { tab ->
                            if (tab == "Inicio") navController.navigate("home")
                            if (tab == "Favoritos") navController.navigate("favorites")
                            if (tab == "Pedidos") navController.navigate("user_orders")
                        }
                    )
                }
            }

            composable("favorites") {
                val user by userViewModel.currentUser.collectAsState()
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    FavoritesScreen(
                        userId = user?.id ?: 0,
                        viewModel = menuViewModel,
                        cartViewModel = cartViewModel
                    )
                    NavigationBar(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
                        currentTab = "Favoritos",
                        onTabSelected = { tab ->
                            if (tab == "Inicio") navController.navigate("home")
                            if (tab == "Menú") navController.navigate("menu")
                            if (tab == "Pedidos") navController.navigate("user_orders")
                        }
                    )
                }
            }

            composable("cart") {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    CartScreen(
                        cartViewModel = cartViewModel,
                        onCheckout = { navController.navigate("payment") }
                    )
                    NavigationBar(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
                        currentTab = "Carrito",
                        onTabSelected = { tab ->
                            if (tab == "Inicio") navController.navigate("home")
                            if (tab == "Menú") navController.navigate("menu")
                            if (tab == "Favoritos") navController.navigate("favorites")
                            if (tab == "Pedidos") navController.navigate("user_orders")
                        }
                    )
                }
            }

            composable("user_orders") {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    UserOrdersScreen(cartViewModel = cartViewModel)
                    NavigationBar(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
                        currentTab = "Pedidos",
                        onTabSelected = { tab ->
                            if (tab == "Inicio") navController.navigate("home")
                            if (tab == "Menú") navController.navigate("menu")
                            if (tab == "Favoritos") navController.navigate("favorites")
                        }
                    )
                }
            }

            composable(
                route = "profile",
                enterTransition = { slideInVertically(initialOffsetY = { it }) },
                exitTransition = { slideOutVertically(targetOffsetY = { it }) }
            ) {
                ProfileScreen(
                    userViewModel = userViewModel,
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToOrders = { navController.navigate("user_orders") },
                    onNavigateToFavorites = { navController.navigate("favorites") },
                    onNavigateToRewards = { navController.navigate("rewards") },
                    onNavigateToAddresses = { navController.navigate("addresses") },
                    onNavigateToPayments = { navController.navigate("payment_methods") }
                )
            }

            composable("rewards") {
                RewardsScreen(userViewModel = userViewModel, onBack = { navController.popBackStack() })
            }

            composable("addresses") {
                AddressesScreen(onBack = { navController.popBackStack() })
            }

            composable("payment_methods") {
                PaymentMethodsScreen(onBack = { navController.popBackStack() })
            }

            composable("settings") {
                var showLogoutDialog by remember { mutableStateOf(false) }

                SettingsScreen(
                    userViewModel = userViewModel,
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = { isDarkTheme = !isDarkTheme },
                    onLogout = { showLogoutDialog = true },
                    onChangeAccount = { 
                        userViewModel.logout()
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    },
                    onPlayGame = { navController.navigate("dino_run") },
                    onBack = { navController.popBackStack() }
                )

                if (showLogoutDialog) {
                    LogoutDialog(onConfirm = { userViewModel.logout() }, onDismiss = { showLogoutDialog = false })
                }
            }

            // ADMIN ROUTES
            composable("admin_dashboard") {
                val user by userViewModel.currentUser.collectAsState()
                var showLogoutDialog by remember { mutableStateOf(false) }

                AdminDashboardScreen(
                    viewModel = adminViewModel,
                    userName = user?.name ?: "Administrador",
                    onNavigateToProducts = { navController.navigate("admin_products") },
                    onNavigateToOrders = { navController.navigate("admin_orders") },
                    onNavigateToReports = { navController.navigate("admin_reports") },
                    onNavigateToPromotions = { navController.navigate("admin_promotions") },
                    onNavigateToConfig = { navController.navigate("admin_config") },
                    onNavigateToActivity = { navController.navigate("admin_activity") },
                    onNavigateToUsers = { navController.navigate("admin_users") },
                    onNavigateToSettings = { navController.navigate("settings") },
                    onLogout = { showLogoutDialog = true }
                )

                if (showLogoutDialog) {
                    LogoutDialog(onConfirm = { userViewModel.logout() }, onDismiss = { showLogoutDialog = false })
                }
            }

            composable("admin_products") {
                val user by userViewModel.currentUser.collectAsState()
                AdminScreen(
                    viewModel = menuViewModel, 
                    adminName = user?.name ?: "Admin",
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable("admin_orders") {
                OrderManagementScreen(viewModel = adminViewModel, onBack = { navController.popBackStack() })
            }

            composable("admin_reports") {
                ReportsScreen(viewModel = adminViewModel, onBack = { navController.popBackStack() })
            }

            composable("admin_promotions") {
                val user by userViewModel.currentUser.collectAsState()
                PromotionsScreen(
                    viewModel = menuViewModel, 
                    adminName = user?.name ?: "Admin",
                    onBack = { navController.popBackStack() }
                )
            }

            composable("admin_config") {
                ShopConfigScreen(viewModel = shopConfigViewModel, onBack = { navController.popBackStack() })
            }

            composable("admin_activity") {
                ActivityLogScreen(database = database, onBack = { navController.popBackStack() })
            }

            composable("admin_users") {
                UserManagementScreen(database = database, onBack = { navController.popBackStack() })
            }

            composable("payment") {
                val total by cartViewModel.totalAmount.collectAsState()
                PaymentScreen(
                    totalAmount = total,
                    onPaymentConfirmed = { method -> navController.navigate("factura/$method") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("factura/{method}") { backStackEntry ->
                val method = backStackEntry.arguments?.getString("method") ?: "Efectivo"
                val user by userViewModel.currentUser.collectAsState()
                FacturaScreen(
                    userViewModel = userViewModel,
                    cartViewModel = cartViewModel,
                    userName = user?.name ?: "Invitado",
                    userPhone = user?.phone ?: "",
                    paymentMethod = method,
                    onBack = { 
                        navController.navigate("user_orders") { popUpTo("home") { inclusive = false } }
                    }
                )
            }

            composable("dino_run") {
                val user by userViewModel.currentUser.collectAsState()
                DinoRunScreen(
                    userRecord = user?.highScore ?: 0,
                    initialDarkTheme = isDarkTheme,
                    onUpdateRecord = { newRecord -> userViewModel.updateHighScore(newRecord) },
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable("notifications") {
                val user by userViewModel.currentUser.collectAsState()
                NotificationsScreen(
                    userId = user?.id ?: 0,
                    database = database,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    userViewModel: UserViewModel,
    menuViewModel: MenuViewModel,
    onExploreMenu: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val user by userViewModel.currentUser.collectAsState()
    val products by menuViewModel.products.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            HeaderSection(
                userName = user?.name ?: "Invitado",
                avatarUrl = user?.avatarUrl ?: "",
                onAvatarClick = onNavigateToProfile,
                onNotificationClick = onNavigateToNotifications,
                onSettingsClick = onNavigateToSettings
            )
            
            // Promo Banner
            PromoBanner(onExploreMenu)
            CategorySection()

            SectionHeader("Más vendidos", onActionClick = onExploreMenu)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val popularProducts = products.filter { it.isPopular }
                items(popularProducts) { product ->
                    ProductStatCard(product)
                }
            }
        }
    }
}

@Composable
fun PromoBanner(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(HeroGradientStart, HeroGradientEnd)
                    )
                )
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("¡20% OFF!", color = White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Text("En tu primer pedido del día", color = White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(containerColor = White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Ver menú", color = PrimaryCoffee)
                    }
                }
                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = White.copy(alpha = 0.3f), modifier = Modifier.size(80.dp))
            }
        }
    }
}

@Composable
fun CategorySection() {
    val categories = listOf(
        "Cafés" to Icons.Default.Coffee,
        "Comida" to Icons.Default.Restaurant,
        "Postres" to Icons.Default.Cake,
        "Fríos" to Icons.Default.AcUnit
    )
    
    Column {
        SectionHeader("Categorías")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { pair ->
                CategoryChip(pair.first, pair.second)
            }
        }
    }
}

@Composable
fun CategoryChip(name: String, icon: ImageVector) {
    Surface(
        modifier = Modifier.clickable { },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryCoffee, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ProductStatCard(product: com.example.cafe.data.Product) {
    Card(
        modifier = Modifier.width(160.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(product.name, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("$${product.price}", color = AccentCinnamon, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun ExitDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Deseas salir?") },
        text = { Text("Si sales de esta pantalla se cerrará tu sesión por seguridad.") },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                Text("Salir y Cerrar Sesión", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Mantenerse") }
        }
    )
}

@Composable
fun LogoutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cerrar Sesión") },
        text = { Text("¿Estás seguro de que deseas salir de Café Aroma?") },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                Text("Cerrar Sesión", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun SessionSplash() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CafeAromaLogo(modifier = Modifier.size(120.dp))
            Spacer(modifier = Modifier.height(28.dp))
            CircularProgressIndicator(color = PrimaryCoffee)
            Spacer(modifier = Modifier.height(14.dp))
            Text("Restaurando sesión...", color = Color.Gray, fontSize = 14.sp)
        }
    }
}
