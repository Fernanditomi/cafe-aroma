package com.example.cafe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cafe.ui.theme.PrimaryCoffee
import com.example.cafe.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopConfigScreen(viewModel: ShopConfigViewModel, onBack: () -> Unit) {
    val config by viewModel.config.collectAsState()
    
    var shopName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    LaunchedEffect(config) {
        config?.let {
            shopName = it.name
            address = it.address
            phone = it.phone
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración de Tienda") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Store, contentDescription = null, tint = PrimaryCoffee, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = shopName,
                onValueChange = { shopName = it },
                label = { Text("Nombre de la Cafetería") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Dirección Física") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Teléfono de Contacto") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { 
                    viewModel.saveConfig(shopName, address, phone)
                    onBack() 
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoffee),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar Cambios", color = White)
            }
        }
    }
}
