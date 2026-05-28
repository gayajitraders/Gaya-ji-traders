package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = "App Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column {
                            Text(
                                text = "QuickStore",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Instant Local Delivery",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                },
                actions = {
                    if (loggedInUser != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    when (currentRole) {
                                        "customer" -> viewModel.setRole("admin")
                                        "admin" -> viewModel.setRole("delivery")
                                        else -> viewModel.setRole("customer")
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = when (currentRole) {
                                    "admin" -> Icons.Default.AdminPanelSettings
                                    "delivery" -> Icons.Default.DirectionsBike
                                    else -> Icons.Default.Person
                                },
                                contentDescription = "Role info",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentRole.replaceFirstChar { it.uppercase() },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = "Logout",
                                tint = Color.Red.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (loggedInUser != null && currentRole == "customer") {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    NavigationBarItem(
                        selected = currentScreen == "home" || currentScreen == "product_detail",
                        onClick = { viewModel.navigateTo("home") },
                        label = { Text("Shop") },
                        icon = { Icon(Icons.Default.ShoppingBag, "Shop") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "cart",
                        onClick = { viewModel.navigateTo("cart") },
                        label = { Text("Cart") },
                        icon = {
                            val cartItems by viewModel.cartItemsList.collectAsStateWithLifecycle()
                            BadgedBox(badge = {
                                if (cartItems.isNotEmpty()) {
                                    Badge { Text(cartItems.sumOf { it.quantity }.toString()) }
                                }
                            }) {
                                Icon(Icons.Default.ShoppingCart, "Cart")
                            }
                        }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "history" || currentScreen == "tracking",
                        onClick = { viewModel.navigateTo("history") },
                        label = { Text("Orders") },
                        icon = { Icon(Icons.Default.ListAlt, "Orders") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "profile",
                        onClick = { viewModel.navigateTo("profile") },
                        label = { Text("Settings") },
                        icon = { Icon(Icons.Default.LocationOn, "Address") }
                    )
                }
            } else if (loggedInUser != null && currentRole == "admin") {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    NavigationBarItem(
                        selected = currentScreen == "dashboard" || currentScreen == "orders",
                        onClick = { viewModel.navigateTo("dashboard") },
                        label = { Text("Dashboard") },
                        icon = { Icon(Icons.Default.Analytics, "Dashboard") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "products",
                        onClick = { viewModel.navigateTo("products") },
                        label = { Text("Stock") },
                        icon = { Icon(Icons.Default.EditCalendar, "Products") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "coupons",
                        onClick = { viewModel.navigateTo("coupons") },
                        label = { Text("Coupons") },
                        icon = { Icon(Icons.Default.LocalOffer, "Coupons") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == "banners",
                        onClick = { viewModel.navigateTo("banners") },
                        label = { Text("Banners") },
                        icon = { Icon(Icons.Default.ViewCarousel, "Banners") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            if (loggedInUser == null) {
                LoginSignupScreen(viewModel)
            } else {
                when (currentRole) {
                    "customer" -> {
                        when (currentScreen) {
                            "home" -> CustomerHomeScreen(viewModel)
                            "product_detail" -> ProductDetailScreen(viewModel)
                            "cart" -> CustomerCartScreen(viewModel)
                            "checkout" -> CustomerCheckoutScreen(viewModel)
                            "tracking" -> CustomerTrackingScreen(viewModel)
                            "history" -> CustomerOrderHistoryScreen(viewModel)
                            "profile" -> CustomerProfileScreen(viewModel)
                            else -> CustomerHomeScreen(viewModel)
                        }
                    }
                    "admin" -> {
                        when (currentScreen) {
                            "dashboard" -> AdminDashboardScreen(viewModel)
                            "products" -> AdminProductsManagerScreen(viewModel)
                            "coupons" -> AdminCouponsScreen(viewModel)
                            "banners" -> AdminBannersScreen(viewModel)
                            else -> AdminDashboardScreen(viewModel)
                        }
                    }
                    "delivery" -> {
                        DeliveryBoyDashboardScreen(viewModel)
                    }
                }
            }
        }
    }
}

// Helpers for Category Color & Vector mapping
@Composable
fun getCategoryVector(category: String): ImageVector {
    return when (category.trim()) {
        "Kitchen Appliances" -> Icons.Default.Kitchen
        "Home Appliances" -> Icons.Default.Home
        "Cooling Devices" -> Icons.Default.AcUnit
        "Power & Utilities" -> Icons.Default.ElectricBolt
        else -> Icons.Default.Category
    }
}

@Composable
fun getCategoryColor(category: String): Color {
    return when (category.trim()) {
        "Kitchen Appliances" -> Color(0xFFFF9900) // Amazon vibrant orange
        "Home Appliances" -> Color(0xFF2874F0)    // Flipkart electric blue
        "Cooling Devices" -> Color(0xFF00A4CC)    // Cool Ice Cyan
        "Power & Utilities" -> Color(0xFFD50000)   // High-voltage red
        else -> MaterialTheme.colorScheme.primary
    }
}

// ==========================================
// 1. LOGIN & SIGNUP SCREEN (Real Mobile OTP Mock)
// ==========================================
@Composable
fun LoginSignupScreen(viewModel: AppViewModel) {
    var phone by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var stepOtp by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }
    var generatedOtp by remember { mutableStateOf("1234") }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Storefront,
            contentDescription = "Logo",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )
        Text(
            text = "Welcome to QuickStore",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Your 50 KM Express Local Store Link",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (!stepOtp) {
                    Text(
                        text = "Sign In / Register",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { if (it.length <= 10) phone = it },
                        label = { Text("Mobile Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, "Phone Icon") },
                        prefix = { Text("+91 ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Badge, "Name Icon") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (phone.length == 10 && name.isNotBlank()) {
                                // Generate a random 4-digit code
                                generatedOtp = String.format("%04d", (1000..9999).random())
                                stepOtp = true
                                Toast.makeText(context, "SMS Simulated: Verification OTP code is: $generatedOtp", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Please enter valid name & 10-digit mobile number", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Get Mobile OTP", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "💡 TEST PERSONAS PANEL KEYBOARD GUIDE:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "• Admin Login: Phone -> '9999999999'\n• Delivery Boy: Phone -> '8888888888'\n• Customer: Enter any 10 digits number",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else {
                    Text(
                        text = "OTP Verification",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Show a realistic visual SMS notification panel on top of card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sms,
                                contentDescription = "SMS Notification",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "SMS: Your QuickStore OTP is $generatedOtp",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Simulating real mobile integration",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { otpInput = it },
                        label = { Text("Enter 4-Digit OTP") },
                        leadingIcon = { Icon(Icons.Default.LockClock, "Lock Icon") },
                        placeholder = { Text(generatedOtp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (otpInput.trim() == generatedOtp || otpInput.trim() == "1234" || otpInput.trim() == "1111") {
                                viewModel.loginOrSignup(phone, name)
                                val welcomeRole = when (phone) {
                                    "9999999999" -> "Admin"
                                    "8888888888" -> "Delivery Partner"
                                    else -> "Customer"
                                }
                                Toast.makeText(context, "$welcomeRole Logged In Successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Invalid OTP! Try entering '$generatedOtp'", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Verify & Log In", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { stepOtp = false }) {
                        Text("Back to Edit Mobile")
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. CUSTOMER EXPLORE & SHOPPING (HOME)
// ==========================================
@Composable
fun CustomerHomeScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val products by viewModel.productsList.collectAsStateWithLifecycle()
    val banners by viewModel.bannersList.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    val filteredProducts = products.filter {
        val matchesSearch = it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "All" || it.category == selectedCategory
        matchesSearch && matchesCategory
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchProducts(it) },
            placeholder = { Text("Search cooler, mixi, kitchen, power series, home appliances...") },
            leadingIcon = { Icon(Icons.Default.Search, "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchProducts("") }) {
                        Icon(Icons.Default.Clear, "Clear")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                if (banners.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(banners) { banner ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .width(300.dp)
                                    .height(130.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .drawBehind {
                                            drawRect(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(
                                                        Color(0xFF2874F0), // Flipkart blue
                                                        Color(0xFFFF9900), // Amazon orange
                                                        Color(0xFF00A4CC)  // Ice Cyan
                                                    )
                                                )
                                            )
                                        }
                                        .padding(16.dp),
                                    contentAlignment = Alignment.BottomStart
                                ) {
                                    Column {
                                        Icon(
                                            imageVector = Icons.Default.Stars,
                                            contentDescription = "Offer Star",
                                            tint = PureWhite,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = banner.title ?: "Express Flash Deal",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp,
                                            color = PureWhite,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Verified 50 KM Location coverage",
                                            fontSize = 11.sp,
                                            color = PureWhite.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                val categories = listOf("All", "Kitchen Appliances", "Home Appliances", "Cooling Devices", "Power & Utilities")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = category == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectCategory(category) },
                            label = { Text(category) },
                            leadingIcon = {
                                if (category != "All") {
                                    Icon(
                                        imageVector = getCategoryVector(category),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Deals Near You (${filteredProducts.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            if (filteredProducts.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "Empty",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No products found matches query",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            } else {
                val chunked = filteredProducts.chunked(2)
                items(chunked) { pair ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (prod in pair) {
                            Box(modifier = Modifier.weight(1f)) {
                                ProductItemCard(product = prod, onSelect = { viewModel.selectProduct(it) }) {
                                    viewModel.addItemToCart(prod.id)
                                    Toast.makeText(context, "${prod.name} added to cart!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductItemCard(product: Product, onSelect: (Int) -> Unit, onAddToCart: () -> Unit) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(product.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(getCategoryColor(product.category).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryVector(product.category),
                    contentDescription = product.name,
                    tint = getCategoryColor(product.category),
                    modifier = Modifier.size(48.dp)
                )
                if (product.stock == 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .background(Color.Red, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Out of Stock", fontSize = 10.sp, color = PureWhite, fontWeight = FontWeight.Bold)
                    }
                } else if (product.stock < 5) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .background(AccentGold, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Only ${product.stock} Left", fontSize = 10.sp, color = DarkSlate, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.category,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${product.price.roundToInt()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(
                        onClick = { if (product.stock > 0) onAddToCart() else Toast.makeText(context, "Product is currently out of stock", Toast.LENGTH_SHORT).show() },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AddShoppingCart, contentDescription = "Add", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. PRODUCT DETAIL SCREEN
// ==========================================
@Composable
fun ProductDetailScreen(viewModel: AppViewModel) {
    val products by viewModel.productsList.collectAsStateWithLifecycle()
    val pid by viewModel.selectedProductId.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val product = products.find { it.id == pid }
    if (product == null) {
        Box(contentAlignment = Alignment.Center) { Text("Product not found") }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(getCategoryColor(product.category).copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getCategoryVector(product.category),
                contentDescription = product.name,
                tint = getCategoryColor(product.category),
                modifier = Modifier.size(96.dp)
            )

            IconButton(
                onClick = { viewModel.navigateTo("home") },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .background(getCategoryColor(product.category).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = product.category,
                    color = getCategoryColor(product.category),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "₹${product.price.roundToInt()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                if (product.stock > 0) {
                    Text(
                        text = "In Stock (${product.stock} items)",
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                } else {
                    Text(
                        text = "Out of Stock",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = "Description",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = product.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (product.stock > 0) {
                            viewModel.addItemToCart(product.id)
                            Toast.makeText(context, "Added to cart!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Product out of stock", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AddShoppingCart, "Add to cart")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add To Cart")
                }

                Button(
                    onClick = {
                        if (product.stock > 0) {
                            viewModel.addItemToCart(product.id)
                            viewModel.navigateTo("cart")
                        } else {
                            Toast.makeText(context, "Product out of stock", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Buy Now", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 4. CUSTOMER CART & CHECKOUT SYSTEMS
// ==========================================
@Composable
fun CustomerCartScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val cartItems by viewModel.cartItemsList.collectAsStateWithLifecycle()
    val products by viewModel.productsList.collectAsStateWithLifecycle()
    val coupons by viewModel.couponsList.collectAsStateWithLifecycle()
    val appliedCouponCode by viewModel.selectedCouponCode.collectAsStateWithLifecycle()

    var couponInput by remember { mutableStateOf("") }

    if (cartItems.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.RemoveShoppingCart, "Cart Empty", modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Your Cart is Empty", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Add delicious grocery, clothing, and utility deals to proceed.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 4.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { viewModel.navigateTo("home") }) {
                Text("Start Shopping Now")
            }
        }
        return
    }

    var subtotal = 0.0
    val mappedItems = cartItems.mapNotNull { item ->
        val prod = products.find { it.id == item.productId } ?: return@mapNotNull null
        subtotal += prod.price * item.quantity
        Pair(item, prod)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text("Shopping Cart (${cartItems.size} items)", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))
            }

            items(mappedItems) { (cartItem, product) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(getCategoryColor(product.category).copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = getCategoryVector(product.category), contentDescription = null, tint = getCategoryColor(product.category))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("₹${product.price.roundToInt()} per unit", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            Text("Total: ₹${(product.price * cartItem.quantity).roundToInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(18.dp))
                        ) {
                            IconButton(
                                onClick = { viewModel.updateCartItemQuantity(product.id, cartItem.quantity - 1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Remove, "Less", modifier = Modifier.size(16.dp))
                            }
                            Text(cartItem.quantity.toString(), fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 6.dp))
                            IconButton(
                                onClick = {
                                    if (cartItem.quantity + 1 <= product.stock) {
                                        viewModel.updateCartItemQuantity(product.id, cartItem.quantity + 1)
                                    } else {
                                        Toast.makeText(context, "Only ${product.stock} left in stock!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Add, "More", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Offers & Coupons", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = couponInput,
                                onValueChange = { couponInput = it },
                                placeholder = { Text("WELCOME50, SUPERDEAL...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val log = viewModel.applyCoupon(couponInput)
                                    Toast.makeText(context, log, Toast.LENGTH_LONG).show()
                                    couponInput = ""
                                }
                            ) {
                                Text("Apply")
                            }
                        }

                        if (appliedCouponCode != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .background(PrimaryGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Check, "Active", tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Coupon '$appliedCouponCode' Active", color = PrimaryGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Clear", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { viewModel.clearCoupon() })
                            }
                        }
                    }
                }
            }
        }

        Surface(tonalElevation = 8.dp) {
            Column(modifier = Modifier.padding(20.dp)) {
                var calculatedDiscount = 0.0
                if (appliedCouponCode != null) {
                    val actualCop = coupons.find { it.code == appliedCouponCode }
                    if (actualCop != null && subtotal >= actualCop.minPurchase) {
                        calculatedDiscount = actualCop.discountAmount
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal:", color = MaterialTheme.colorScheme.secondary)
                    Text("₹${subtotal.roundToInt()}", fontWeight = FontWeight.Bold)
                }
                if (calculatedDiscount > 0.0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Discount Applied:", color = PrimaryGreen)
                        Text("-₹${calculatedDiscount.roundToInt()}", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { viewModel.navigateTo("checkout") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Proceed to Checkout (₹${(subtotal - calculatedDiscount).roundToInt()})", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Checkout Screen with 50 KM delivery checks, payment options, coordinate setting
@Composable
fun CustomerCheckoutScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userProfile by viewModel.loggedInUser.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItemsList.collectAsStateWithLifecycle()
    val products by viewModel.productsList.collectAsStateWithLifecycle()
    val coupons by viewModel.couponsList.collectAsStateWithLifecycle()
    val appliedCouponCode by viewModel.selectedCouponCode.collectAsStateWithLifecycle()

    var customAddress by remember { mutableStateOf(userProfile?.address ?: "") }
    var selectedDistanceOffset by remember { mutableStateOf(5.0) }

    var paymentMode by remember { mutableStateOf("COD") }
    var paymentStepInProgress by remember { mutableStateOf(false) }

    val subtotal = cartItems.sumOf { item ->
        val prod = products.find { it.id == item.productId } ?: return@sumOf 0.0
        prod.price * item.quantity
    }

    var discount = 0.0
    if (appliedCouponCode != null) {
        val actualCop = coupons.find { it.code == appliedCouponCode }
        if (actualCop != null && subtotal >= actualCop.minPurchase) {
            discount = actualCop.discountAmount
        }
    }

    val centerLat = viewModel.storeLatitude
    val centerLng = viewModel.storeLongitude

    val degreeOffset = selectedDistanceOffset / 111.0
    val userLat = centerLat + degreeOffset
    val userLng = centerLng + degreeOffset

    val distanceCalculated = viewModel.getDistanceKm(centerLat, centerLng, userLat, userLng)
    val deliveryCharge = viewModel.calculateDeliveryCharge(userLat, userLng)
    val isOutOfRange = distanceCalculated > 50.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Order Checkout", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = if (isOutOfRange) Color.Red.copy(alpha = 0.08f) else PrimaryGreen.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, if (isOutOfRange) Color.Red else PrimaryGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Delivery Area & Distance Checker",
                    fontWeight = FontWeight.Bold,
                    color = if (isOutOfRange) DangerRed else PrimaryGreen,
                    fontSize = 15.sp
                )
                Text(
                    text = "Our warehouse delivers orders up to 50 KM radius central.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text("Select Delivery Distance Simulation:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Slider(
                        value = selectedDistanceOffset.toFloat(),
                        onValueChange = { selectedDistanceOffset = it.toDouble() },
                        valueRange = 1.0f..65.0f,
                        modifier = Modifier.weight(1f)
                    )
                    Text("${selectedDistanceOffset.roundToInt()} KM", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(start = 8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Calculated Distance:", fontSize = 13.sp)
                    Text(
                        text = String.format("%.2f KM", distanceCalculated),
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isOutOfRange) DangerRed else PrimaryGreen
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Delivery Charge Calculated:", fontSize = 13.sp)
                    Text(
                        text = if (isOutOfRange) "UNAVAILABLE" else "₹${deliveryCharge.roundToInt()}",
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isOutOfRange) DangerRed else PrimaryGreen
                    )
                }

                if (isOutOfRange) {
                    Text(
                        text = "🚨 Oops! Outside of delivery coverage area (Max limit 50 KM).",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Saved Delivery Address Details", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = customAddress,
            onValueChange = { customAddress = it },
            label = { Text("House, Street, Area Name") },
            leadingIcon = { Icon(Icons.Default.Home, "Address") },
            placeholder = { Text("E.g. Flat 301, Emerald Heights, Sector 62") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Choose Payment Method", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                colors = CardDefaults.cardColors(containerColor = if (paymentMode == "COD") MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, if (paymentMode == "COD") MaterialTheme.colorScheme.primary else Color.LightGray),
                modifier = Modifier
                    .weight(1f)
                    .clickable { paymentMode = "COD" }
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Handshake, "COD", tint = if (paymentMode == "COD") MaterialTheme.colorScheme.primary else Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Cash, Pay on COD", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = if (paymentMode == "Online") MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, if (paymentMode == "Online") MaterialTheme.colorScheme.primary else Color.LightGray),
                modifier = Modifier
                    .weight(1f)
                    .clickable { paymentMode = "Online" }
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.QrCodeScanner, "UPI/Card", tint = if (paymentMode == "Online") MaterialTheme.colorScheme.primary else Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("UPI/Cards Online", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val grandTotal = if (isOutOfRange) 0.0 else subtotal + deliveryCharge - discount
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Cart Subtotal:")
                    Text("₹${subtotal.roundToInt()}", fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Simulated Distance:")
                    Text(String.format("%.2f KM", selectedDistanceOffset))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Delivery Fee Calc:")
                    Text(if (isOutOfRange) "Denied" else "₹${deliveryCharge.roundToInt()}", fontWeight = FontWeight.Bold)
                }
                if (discount > 0.0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Active Offer Applied:")
                        Text("-₹${discount.roundToInt()}", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Grand Total to Pay:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("₹${grandTotal.roundToInt()}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (paymentStepInProgress) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = InfoBlue.copy(alpha = 0.1f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Processing Secure UPI Payment gateway protocol...", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Button(
            onClick = {
                if (isOutOfRange) {
                    Toast.makeText(context, "Cannot place order outside 50 KM area!", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (customAddress.isBlank()) {
                    Toast.makeText(context, "Please save standard delivery address!", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (paymentMode == "Online") {
                    scope.launch {
                        paymentStepInProgress = true
                        delay(2500)
                        paymentStepInProgress = false
                        viewModel.placeOrder(
                            customerName = userProfile?.name ?: "Valued Customer",
                            customerPhone = userProfile?.phone ?: "0000000000",
                            address = customAddress,
                            lat = userLat,
                            lng = userLng,
                            paymentMethod = "Online"
                        ) { ord ->
                            viewModel.startTracking(ord.id)
                            Toast.makeText(context, "COD/Online Order #${ord.id} PLACED!", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    viewModel.placeOrder(
                        customerName = userProfile?.name ?: "Valued Customer",
                        customerPhone = userProfile?.phone ?: "0000000000",
                        address = customAddress,
                        lat = userLat,
                        lng = userLng,
                        paymentMethod = "COD"
                    ) { ord ->
                        viewModel.startTracking(ord.id)
                        Toast.makeText(context, "COD Order #${ord.id} PLACED!", Toast.LENGTH_LONG).show()
                    }
                }
            },
            enabled = !isOutOfRange && !paymentStepInProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Confirm Place Order (₹${grandTotal.roundToInt()})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

// ==========================================
// 5. CUSTOMER TRACKING & ORDER HISTORY
// ==========================================
@Composable
fun CustomerTrackingScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val trackingOrderId by viewModel.trackingOrderId.collectAsStateWithLifecycle()
    val orders by viewModel.ordersList.collectAsStateWithLifecycle()

    val currentOrder = orders.find { it.id == trackingOrderId }
    if (currentOrder == null) {
        Box(contentAlignment = Alignment.Center) { Text("No active tracking session") }
        return
    }

    val liveLat by viewModel.liveDeliveryLat.collectAsStateWithLifecycle()
    val liveLng by viewModel.liveDeliveryLng.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo("history") }) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
            Text("Tracking Order #${currentOrder.id}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Button(
                onClick = {
                    scope.launch {
                        val status = viewModel.saveInvoiceToDownloads(currentOrder)
                        Toast.makeText(context, status, Toast.LENGTH_LONG).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(Icons.Default.FileDownload, "Download", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Get Receipt", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Express Route Map Tracker", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlate)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    drawRect(color = Color(0xFF1E1E1E))

                    val ptStoreX = width * 0.2f
                    val ptStoreY = height * 0.5f

                    val ptCustX = width * 0.8f
                    val ptCustY = height * 0.4f

                    drawLine(
                        color = Color.DarkGray,
                        start = Offset(ptStoreX, ptStoreY),
                        end = Offset(ptCustX, ptCustY),
                        strokeWidth = 4f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    drawCircle(color = PrimaryGreen, radius = 16f, center = Offset(ptStoreX, ptStoreY))
                    drawCircle(color = AccentGold, radius = 20f, center = Offset(ptCustX, ptCustY))

                    val activeLat = liveLat ?: viewModel.storeLatitude
                    val totalLatDiff = currentOrder.latitude - viewModel.storeLatitude
                    val fraction = if (totalLatDiff != 0.0) {
                        (activeLat - viewModel.storeLatitude) / totalLatDiff
                    } else {
                        1.0
                    }

                    val boyX = ptStoreX + (ptCustX - ptStoreX) * fraction.toFloat()
                    val boyY = ptStoreY + (ptCustY - ptStoreY) * fraction.toFloat()

                    drawCircle(
                        color = Color(0xFF1EC36A),
                        radius = 24f,
                        center = Offset(boyX, boyY)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Store Warehouse", fontSize = 10.sp, color = PureWhite, fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Courier Boy Position", fontSize = 10.sp, color = Color.Green, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Delivery OTP Code:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Box(
                        modifier = Modifier
                            .background(AccentGold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = currentOrder.deliveryOtp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB78103),
                            fontSize = 18.sp,
                            letterSpacing = 2.sp
                        )
                    }
                }

                Text(
                    text = "Provide this OTP to delivery agent when securely receivingbasmati packages.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Order Status:", fontSize = 13.sp)
                    val statusColor = when (currentOrder.orderStatus) {
                        "Delivered" -> PrimaryGreen
                        "Out for Delivery" -> Color(0xFF00BCD4)
                        else -> AccentGold
                    }
                    Text(currentOrder.orderStatus.uppercase(), fontWeight = FontWeight.Bold, color = statusColor, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Payment Setup:", fontSize = 13.sp)
                    Text(
                        "${currentOrder.paymentStatus} (${currentOrder.paymentMethod})",
                        fontWeight = FontWeight.Bold,
                        color = if (currentOrder.paymentStatus == "Paid") PrimaryGreen else Color.Red
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        try {
                            val msg = "Hello QuickStore support, I need update on my Order #${currentOrder.id}"
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://api.whatsapp.com/send?phone=919999999999&text=${Uri.encode(msg)}")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "WhatsApp launch URL opened in custom link: +91 9999999999", Toast.LENGTH_LONG).show()
                        }
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "WhatsApp icon",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Support Chat on WhatsApp", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Instant resolution of order issues click here", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

@Composable
fun CustomerOrderHistoryScreen(viewModel: AppViewModel) {
    val orders by viewModel.ordersList.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Your Order History", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))

        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No order history found. Open 'Shop' to place your first order!",
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
            }
            return
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(orders) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.startTracking(order.id) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Order ID: #${order.id}", fontWeight = FontWeight.Bold)
                            val color = when (order.orderStatus) {
                                "Delivered" -> PrimaryGreen
                                "Out for Delivery" -> Color(0xFF1EC36A)
                                else -> AccentGold
                            }
                            Text(order.orderStatus, fontWeight = FontWeight.Bold, color = color)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = order.itemsJson.replace("\n", ", ").removeSuffix(", "),
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Amount paid: ₹${order.totalAmount.roundToInt()}", fontWeight = FontWeight.Bold)

                            Row {
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            val s = viewModel.saveInvoiceToDownloads(order)
                                            Toast.makeText(context, s, Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Icon(Icons.Default.FileDownload, "Download", modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("TXT Invoice", fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = { viewModel.startTracking(order.id) },
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("Track Live", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Coordinate Setting address profile config
@Composable
fun CustomerProfileScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val userProfile by viewModel.loggedInUser.collectAsStateWithLifecycle()

    var addressText by remember { mutableStateOf(userProfile?.address ?: "") }
    var userLat by remember { mutableStateOf(userProfile?.latitude ?: 28.6139) }
    var userLng by remember { mutableStateOf(userProfile?.longitude ?: 77.2090) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Address Configuration", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("Configure coordinate points dynamically below to verify the 50 KM coverage delivery calculations easily.", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))

        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = addressText,
                    onValueChange = { addressText = it },
                    label = { Text("Physical Delivery Address") },
                    leadingIcon = { Icon(Icons.Default.HomeWork, "House") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Simulate GPS Latitudes:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Slider(
                        value = userLat.toFloat(),
                        onValueChange = { userLat = it.toDouble() },
                        valueRange = 28.0f..29.5f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(String.format("%.4f", userLat), modifier = Modifier.padding(start = 8.dp))
                }

                Text("Simulate GPS Longitudes:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Slider(
                        value = userLng.toFloat(),
                        onValueChange = { userLng = it.toDouble() },
                        valueRange = 76.5f..78.0f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(String.format("%.4f", userLng), modifier = Modifier.padding(start = 8.dp))
                }

                Button(
                    onClick = {
                        viewModel.saveAddress(addressText, userLat, userLng)
                        Toast.makeText(context, "Delivery coordinates and address saved!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save GPS Coordinates Setup")
                }
            }
        }
    }
}

// ==========================================
// 6. ADMIN/SHOP OWNER FEATURES (PANEL)
// ==========================================
@Composable
fun AdminDashboardScreen(viewModel: AppViewModel) {
    val orders by viewModel.ordersList.collectAsStateWithLifecycle()
    val products by viewModel.productsList.collectAsStateWithLifecycle()
    val deliveryBoys by viewModel.deliveryBoysList.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Owner Controls", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Total Store sales monitoring console", fontSize = 12.sp, color = Color.Gray)
            }

            Button(
                onClick = {
                    scope.launch {
                        val statusStr = viewModel.saveSalesReportToDownloads()
                        Toast.makeText(context, statusStr, Toast.LENGTH_LONG).show()
                    }
                }
            ) {
                Icon(Icons.Default.FileDownload, "Report")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sales Statement")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = PrimaryGreen.copy(alpha = 0.12f))) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Total Revenue", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground)
                    Text("₹${orders.sumOf { it.totalAmount }.roundToInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                }
            }
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = AccentGold.copy(alpha = 0.12f))) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Total Orders", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground)
                    Text("${orders.size} Placed", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB78103))
                }
            }
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Items listed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground)
                    Text("${products.size} Products", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Text("Order Management Console", fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(bottom = 8.dp))

        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No orders placed yet in database logs.", color = Color.Gray)
            }
            return
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(orders) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ID: #${order.id} (Paid: ${order.paymentMethod})", fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .background(PrimaryGreen.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(order.orderStatus.uppercase(), color = PrimaryGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text("Customer: ${order.customerName} (${order.customerPhone})", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                        Text("Address: ${order.address}", fontSize = 12.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Assign Status / Driver:", fontSize = 11.sp, color = Color.Gray)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Button(
                                        onClick = { viewModel.adminUpdateOrderStatus(order, "Accepted") },
                                        contentPadding = PaddingValues(horizontal = 6.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Accept", fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = { viewModel.adminUpdateOrderStatus(order, "Dispatched") },
                                        contentPadding = PaddingValues(horizontal = 6.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Dispatch", fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = { viewModel.adminUpdateOrderStatus(order, "Out for Delivery") },
                                        contentPadding = PaddingValues(horizontal = 6.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Out", fontSize = 10.sp)
                                    }
                                }
                            }

                            var showBoysDrop by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(
                                    onClick = { showBoysDrop = true },
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    val dbName = deliveryBoys.find { it.id == order.deliveryBoyId }?.name ?: "Assign Driver"
                                    Text(dbName, fontSize = 11.sp)
                                    Icon(Icons.Default.ArrowDropDown, "Assign", modifier = Modifier.size(12.dp))
                                }

                                DropdownMenu(
                                    expanded = showBoysDrop,
                                    onDismissRequest = { showBoysDrop = false }
                                ) {
                                    for (boy in deliveryBoys) {
                                        DropdownMenuItem(
                                            text = { Text(boy.name) },
                                            onClick = {
                                                viewModel.adminAssignDeliveryBoy(order, boy.id)
                                                showBoysDrop = false
                                                Toast.makeText(context, "Assigned order to driver: ${boy.name}", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductsManagerScreen(viewModel: AppViewModel) {
    val products by viewModel.productsList.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAddForm by remember { mutableStateOf(false) }

    var pName by remember { mutableStateOf("") }
    var pPrice by remember { mutableStateOf("") }
    var pDesc by remember { mutableStateOf("") }
    var pCategory by remember { mutableStateOf("Kitchen Appliances") }
    var pStock by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Listed Inventory System", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Button(onClick = { showAddForm = !showAddForm }) {
                Icon(if (showAddForm) Icons.Default.Close else Icons.Default.Add, "Form")
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (showAddForm) "Close Form" else "Add Custom Product")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedVisibility(visible = showAddForm) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Product Upload Form", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = pName,
                        onValueChange = { pName = it },
                        label = { Text("Product Display Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = pPrice,
                            onValueChange = { pPrice = it },
                            label = { Text("Price (INR)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = pStock,
                            onValueChange = { pStock = it },
                            label = { Text("Stock Status") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("Choose Category Tag:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    val categoriesList = listOf("Kitchen Appliances", "Home Appliances", "Cooling Devices", "Power & Utilities")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categoriesList) { cat ->
                            FilterChip(
                                selected = pCategory == cat,
                                onClick = { pCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = pDesc,
                        onValueChange = { pDesc = it },
                        label = { Text("Brief Description detailing premium elements") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val pr = pPrice.toDoubleOrNull()
                            val st = pStock.toIntOrNull()
                            if (pName.isNotBlank() && pr != null && st != null) {
                                viewModel.adminAddProduct(
                                    pName, pDesc, pr, pCategory, st, ""
                                )
                                Toast.makeText(context, "Product Uploaded Successfully!", Toast.LENGTH_SHORT).show()
                                showAddForm = false
                                pName = ""
                                pPrice = ""
                                pDesc = ""
                                pStock = ""
                            } else {
                                Toast.makeText(context, "Fill invalid price or title headers!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Publish Stock Items")
                    }
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(products) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(getCategoryColor(product.category).copy(alpha = 0.1f), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = getCategoryVector(product.category), contentDescription = null, tint = getCategoryColor(product.category))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Price: ₹${product.price.roundToInt()} | Stock: ${product.stock} items", fontSize = 12.sp, color = Color.Gray)
                        }

                        IconButton(onClick = {
                            viewModel.adminDeleteProduct(product)
                            Toast.makeText(context, "Product Deleted Successfully", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.DeleteOutline, "Delete", tint = Color.Red.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCouponsScreen(viewModel: AppViewModel) {
    val coupons by viewModel.couponsList.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var code by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("") }
    var minPurchase by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Offer & Coupons manager", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Generate Special Coupons", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Coupon Code (E.g. SUMMER100)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = discount,
                        onValueChange = { discount = it },
                        label = { Text("Discount ₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minPurchase,
                        onValueChange = { minPurchase = it },
                        label = { Text("Min Order Value ₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = {
                        val d = discount.toDoubleOrNull()
                        val m = minPurchase.toDoubleOrNull()
                        if (code.isNotBlank() && d != null && m != null) {
                            viewModel.adminAddCoupon(code, d, m)
                            Toast.makeText(context, "Coupon Saved!", Toast.LENGTH_SHORT).show()
                            code = ""
                            discount = ""
                            minPurchase = ""
                        } else {
                            Toast.makeText(context, "Please input valid parameters!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Register Offer Coupon Code")
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(coupons) { coupon ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(coupon.code, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text("Discount: ₹${coupon.discountAmount.roundToInt()} on orders >= ₹${coupon.minPurchase.roundToInt()}", fontSize = 12.sp, color = Color.Gray)
                        }

                        IconButton(onClick = { viewModel.adminDeleteCoupon(coupon) }) {
                            Icon(Icons.Default.DeleteOutline, "Delete", tint = Color.Red.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminBannersScreen(viewModel: AppViewModel) {
    val banners by viewModel.bannersList.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var bannerTitle by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Campaign Banners", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Publish Promotional Header Banner", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = bannerTitle,
                    onValueChange = { bannerTitle = it },
                    label = { Text("Banner Campaign Title Headline") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (bannerTitle.isNotBlank()) {
                            viewModel.adminAddBanner(imageUrl = "", title = bannerTitle)
                            Toast.makeText(context, "Promotion Banner Registered!", Toast.LENGTH_SHORT).show()
                            bannerTitle = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Promotion Banner")
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(banners) { banner ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(banner.title ?: "Express Offer Banner ID: ${banner.id}", fontWeight = FontWeight.Medium)
                        IconButton(onClick = { viewModel.adminDeleteBanner(banner.id) }) {
                            Icon(Icons.Default.DeleteOutline, "Delete", tint = Color.Red.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. DELIVERY BOY PANEL (Radius limits, Map Route, OTP verification)
// ==========================================
@Composable
fun DeliveryBoyDashboardScreen(viewModel: AppViewModel) {
    val orders by viewModel.ordersList.collectAsStateWithLifecycle()
    val deliveryBoys by viewModel.deliveryBoysList.collectAsStateWithLifecycle()
    val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val activeBoy = deliveryBoys.find { it.phone == loggedInUser?.phone }
        ?: deliveryBoys.firstOrNull() ?: DeliveryBoy(id = 1, name = "Karan Sharma", phone = "+91 9102938475")

    val assignedOrders = orders.filter { it.deliveryBoyId == activeBoy.id && it.orderStatus != "Delivered" }

    var selectedOrderForTrackingMap by remember { mutableStateOf<Order?>(null) }
    var otpTextFieldState by remember { mutableStateOf("") }

    if (selectedOrderForTrackingMap != null) {
        val mappedOrder = selectedOrderForTrackingMap!!
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { selectedOrderForTrackingMap = null }) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Assigned Delivery Run #${mappedOrder.id}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        drawRect(color = Color(0xFF1C2D37))

                        val startX = w * 0.2f
                        val startY = h * 0.5f
                        val endX = w * 0.8f
                        val endY = h * 0.4f

                        drawLine(
                            color = Color.Green,
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = 6f
                        )

                        drawCircle(color = PrimaryGreen, radius = 12f, center = Offset(startX, startY))
                        drawCircle(color = AccentGold, radius = 18f, center = Offset(endX, endY))
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Active Track Navigation: Store -> Customer", fontSize = 10.sp, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Secure OTP Verification For Completion", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("To verify order completed successfully and cash received securely, ask customer for OTP displayed in their order status page.", fontSize = 12.sp, color = Color.Gray)

                    OutlinedTextField(
                        value = otpTextFieldState,
                        onValueChange = { otpTextFieldState = it },
                        label = { Text("Provide Secure Verification OTP") },
                        placeholder = { Text("E.g. ****") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            viewModel.deliveryVerifyOtpAndComplete(mappedOrder, otpTextFieldState) { success ->
                                if (success) {
                                    Toast.makeText(context, "Order Delivered Successfully! Target Cash finalized.", Toast.LENGTH_LONG).show()
                                    selectedOrderForTrackingMap = null
                                    otpTextFieldState = ""
                                } else {
                                    Toast.makeText(context, "Wrong verification code! Re-input customer code.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text("Verify OTP & Complete Safe Handover", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.deliveryUpdateStatus(mappedOrder, "Out for Delivery") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Set Out for Delivery", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryGreen.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, PrimaryGreen)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DirectionsBike, "Cyclist Icon", tint = PrimaryGreen, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Driver Partner Running Panel", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Active: ${activeBoy.name} (${activeBoy.phone})", fontSize = 13.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("My Assigned Deliveries Area (${assignedOrders.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(10.dp))

        if (assignedOrders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No pending runs to complete! Relax.", color = Color.Gray)
            }
            return
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(assignedOrders) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Run ID: #${order.id}", fontWeight = FontWeight.Bold)
                            val c = when (order.orderStatus) {
                                "Out for Delivery" -> Color(0xFF00BCD4)
                                else -> AccentGold
                            }
                            Text("Status: ${order.orderStatus}", color = c, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Text("Customer: ${order.customerName} (${order.customerPhone})", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                        Text("Address: ${order.address}", fontSize = 12.sp, color = Color.Gray)
                        Text("Total Collectable Value: ₹${order.totalAmount.roundToInt()} (${order.paymentMethod})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { selectedOrderForTrackingMap = order },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Map, "Navigation")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Map Navigation & Handover OTP", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
