package com.example.ui

import android.app.Application
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.*
import kotlin.random.Random

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = AppRepository(db.appDao)

    // Current app state / navigation role
    private val _currentRole = MutableStateFlow("customer") // customer, admin, delivery
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    // Screen navigation within current role
    private val _currentScreen = MutableStateFlow("home") // customer: home, product_detail, cart, checkout, tracking, history / admin: dashboard, products, orders, coupons, banners / delivery: dashboard, map
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Active tracking order ID
    private val _trackingOrderId = MutableStateFlow<Int?>(null)
    val trackingOrderId: StateFlow<Int?> = _trackingOrderId.asStateFlow()

    // Selected product detail ID
    private val _selectedProductId = MutableStateFlow<Int?>(null)
    val selectedProductId: StateFlow<Int?> = _selectedProductId.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Selected Category
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Selected Coupon Code
    private val _selectedCouponCode = MutableStateFlow<String?>(null)
    val selectedCouponCode: StateFlow<String?> = _selectedCouponCode.asStateFlow()

    // Authentication States
    val loggedInUser: StateFlow<UserProfile?> = repository.getLoggedInUserFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Store coordinate (Central Warehouse/HQ - Delhi Node)
    val storeLatitude = 28.6139
    val storeLongitude = 77.2090

    // Database Flows
    val productsList: StateFlow<List<Product>> = repository.allProductsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItemsList: StateFlow<List<CartItem>> = repository.cartItemsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ordersList: StateFlow<List<Order>> = repository.allOrdersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val couponsList: StateFlow<List<Coupon>> = repository.allCouponsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bannersList: StateFlow<List<Banner>> = repository.allBannersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deliveryBoysList: StateFlow<List<DeliveryBoy>> = repository.allDeliveryBoysFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulated active delivery tracker position
    private val _liveDeliveryLat = MutableStateFlow<Double?>(null)
    val liveDeliveryLat: StateFlow<Double?> = _liveDeliveryLat.asStateFlow()

    private val _liveDeliveryLng = MutableStateFlow<Double?>(null)
    val liveDeliveryLng: StateFlow<Double?> = _liveDeliveryLng.asStateFlow()

    // Live Location update simulated loop
    private var trackingJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            seedInitialDatabaseIfEmpty()
        }
    }

    fun setRole(role: String) {
        _currentRole.value = role
        when (role) {
            "customer" -> _currentScreen.value = "home"
            "admin" -> _currentScreen.value = "dashboard"
            "delivery" -> _currentScreen.value = "dashboard"
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun searchProducts(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectProduct(productId: Int?) {
        _selectedProductId.value = productId
        if (productId != null) {
            _currentScreen.value = "product_detail"
        }
    }

    fun startTracking(orderId: Int) {
        _trackingOrderId.value = orderId
        _currentScreen.value = "tracking"
        simulateDeliveryMovement(orderId)
    }

    // --- Distance & Delivery Charge Calculator ---
    // Haversine formula calculation
    fun getDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Radius of earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    // Auto-calculates charges: Flat ₹30 up to 3km, + ₹6 per KM thereafter. Max delivery 50KM limit.
    fun calculateDeliveryCharge(lat: Double, lng: Double): Double {
        val distance = getDistanceKm(storeLatitude, storeLongitude, lat, lng)
        if (distance > 50.0) return -1.0 // Out of bounds
        return if (distance <= 3.0) {
            30.0
        } else {
            30.0 + ((distance - 3.0) * 6.0).roundToInt()
        }
    }

    // --- Cart Actions ---
    fun addItemToCart(productId: Int, quantity: Int = 1) {
        viewModelScope.launch {
            val existing = cartItemsList.value.find { it.productId == productId }
            if (existing != null) {
                repository.addToCart(existing.copy(quantity = existing.quantity + quantity))
            } else {
                repository.addToCart(CartItem(productId = productId, quantity = quantity))
            }
        }
    }

    fun updateCartItemQuantity(productId: Int, quantity: Int) {
        viewModelScope.launch {
            if (quantity <= 0) {
                repository.removeCartItem(productId)
            } else {
                val existing = cartItemsList.value.find { it.productId == productId }
                if (existing != null) {
                    repository.addToCart(existing.copy(quantity = quantity))
                }
            }
        }
    }

    fun removeCartItem(productId: Int) {
        viewModelScope.launch {
            repository.removeCartItem(productId)
        }
    }

    fun applyCoupon(code: String): String {
        var status = "No coupon applied"
        viewModelScope.launch {
            val coupon = repository.getCouponByCode(code.trim().uppercase())
            if (coupon != null) {
                _selectedCouponCode.value = coupon.code
                status = "Coupon Applied: Save ₹${coupon.discountAmount}"
            } else {
                status = "Invalid Coupon Code"
            }
        }
        return status
    }

    fun clearCoupon() {
        _selectedCouponCode.value = null
    }

    // --- Order Placement ---
    fun placeOrder(
        customerName: String,
        customerPhone: String,
        address: String,
        lat: Double,
        lng: Double,
        paymentMethod: String,
        onSuccess: (Order) -> Unit
    ) {
        viewModelScope.launch {
            val cartList = cartItemsList.value
            if (cartList.isEmpty()) return@launch

            // Compile visual items descriptions
            val itemListStringBuilder = StringBuilder()
            var subtotal = 0.0
            for (item in cartList) {
                val prod = productsList.value.find { it.id == item.productId } ?: continue
                val lineTotal = prod.price * item.quantity
                subtotal += lineTotal
                itemListStringBuilder.append("${prod.name} (x${item.quantity}) - ₹${lineTotal}\n")

                // Update standard stock
                val updatedStock = maxOf(0, prod.stock - item.quantity)
                repository.updateProduct(prod.copy(stock = updatedStock))
            }

            val distance = getDistanceKm(storeLatitude, storeLongitude, lat, lng)
            val deliveryCost = if (distance <= 3.0) 30.0 else 30.0 + ((distance - 3.0) * 6.0).roundToInt()

            // Coupon reduction
            var discount = 0.0
            val couponCodeVal = _selectedCouponCode.value
            if (couponCodeVal != null) {
                val cop = repository.getCouponByCode(couponCodeVal)
                if (cop != null && subtotal >= cop.minPurchase) {
                    discount = cop.discountAmount
                }
            }

            val finalAmount = maxOf(0.0, subtotal + deliveryCost - discount)
            val generatedOtp = String.format("%04d", Random.nextInt(1000, 9999))

            val order = Order(
                customerName = customerName,
                customerPhone = customerPhone,
                address = address,
                latitude = lat,
                longitude = lng,
                itemsJson = itemListStringBuilder.toString(),
                totalAmount = finalAmount,
                deliveryCharge = deliveryCost,
                discountApplied = discount,
                couponCode = couponCodeVal,
                paymentMethod = paymentMethod,
                paymentStatus = if (paymentMethod == "Online") "Paid" else "Pending",
                orderStatus = "Placed",
                deliveryOtp = generatedOtp
            )

            val orderId = repository.insertOrder(order)
            val placedOrder = repository.getOrderById(orderId.toInt())
            if (placedOrder != null) {
                repository.clearCart()
                _selectedCouponCode.value = null
                onSuccess(placedOrder)
            }
        }
    }

    // --- Authentication ---
    fun loginOrSignup(phone: String, name: String, address: String = "") {
        viewModelScope.launch {
            repository.logoutAllUsers()
            val existing = repository.getUserByPhone(phone)
            val role = if (phone == "9999999999") "admin" else if (phone == "8888888888") "delivery" else "customer"
            if (existing != null) {
                repository.insertUser(existing.copy(isLoggedIn = true, name = name, address = address, role = role))
            } else {
                repository.insertUser(
                    UserProfile(
                        phone = phone,
                        name = name,
                        address = address,
                        isLoggedIn = true,
                        role = role
                    )
                )
            }
        }
    }

    fun saveAddress(address: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            val user = loggedInUser.value
            if (user != null) {
                repository.insertUser(user.copy(address = address, latitude = lat, longitude = lng))
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logoutAllUsers()
            _trackingOrderId.value = null
            _selectedProductId.value = null
            _selectedCouponCode.value = null
            _currentScreen.value = "home"
        }
    }

    // --- Shop Owner panel Admin actions ---
    fun adminAddProduct(name: String, desc: String, price: Double, category: String, stock: Int, images: String) {
        viewModelScope.launch {
            repository.insertProduct(Product(name = name, description = desc, price = price, category = category, stock = stock, images = images))
        }
    }

    fun adminUpdateProduct(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun adminDeleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun adminAddCoupon(code: String, discount: Double, minPurchase: Double) {
        viewModelScope.launch {
            repository.insertCoupon(Coupon(code = code.trim().uppercase(), discountAmount = discount, minPurchase = minPurchase))
        }
    }

    fun adminDeleteCoupon(coupon: Coupon) {
        viewModelScope.launch {
            repository.deleteCoupon(coupon)
        }
    }

    fun adminUpdateOrderStatus(order: Order, status: String) {
        viewModelScope.launch {
            repository.updateOrder(order.copy(orderStatus = status))
        }
    }

    fun adminAssignDeliveryBoy(order: Order, deliveryBoyId: Int) {
        viewModelScope.launch {
            repository.updateOrder(order.copy(deliveryBoyId = deliveryBoyId, orderStatus = "Accepted"))
        }
    }

    fun adminAddBanner(imageUrl: String, title: String? = null) {
        viewModelScope.launch {
            repository.insertBanner(Banner(imageUrl = imageUrl, title = title))
        }
    }

    fun adminDeleteBanner(bannerId: Int) {
        viewModelScope.launch {
            repository.deleteBanner(bannerId)
        }
    }

    // --- Delivery Panel Actions ---
    fun deliveryUpdateStatus(order: Order, status: String) {
        viewModelScope.launch {
            repository.updateOrder(order.copy(orderStatus = status))
        }
    }

    fun deliveryVerifyOtpAndComplete(order: Order, inputOtp: String, onCompleted: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (order.deliveryOtp == inputOtp.trim()) {
                repository.updateOrder(
                    order.copy(
                        orderStatus = "Delivered",
                        paymentStatus = "Paid" // If COD, marked paid on OTP complete
                    )
                )
                onCompleted(true)
            } else {
                onCompleted(false)
            }
        }
    }

    // --- Live Tracking Location Simulation ---
    private fun simulateDeliveryMovement(orderId: Int) {
        trackingJob?.cancel()
        trackingJob = viewModelScope.launch(Dispatchers.Default) {
            val order = repository.getOrderById(orderId) ?: return@launch
            val destLat = order.latitude
            val destLng = order.longitude

            // Start delivery boy at central warehouse
            var currLat = storeLatitude
            var currLng = storeLongitude

            _liveDeliveryLat.value = currLat
            _liveDeliveryLng.value = currLng

            val steps = 15
            for (i in 1..steps) {
                delay(2500) // update coordinates every 2.5 seconds
                val fraction = i.toDouble() / steps
                val nextLat = storeLatitude + (destLat - storeLatitude) * fraction
                val nextLng = storeLongitude + (destLng - storeLongitude) * fraction

                _liveDeliveryLat.value = nextLat
                _liveDeliveryLng.value = nextLng

                // Update location info inside order for delivery boy view syncing
                val currentOrderVal = repository.getOrderById(orderId)
                if (currentOrderVal != null && currentOrderVal.orderStatus == "Out for Delivery") {
                    repository.updateOrder(currentOrderVal.copy(liveLatitude = nextLat, liveLongitude = nextLng))
                }
            }
        }
    }

    // --- File Manager Downloads: Save Invoice ---
    // Beautiful human-readable statement printed into device Downloads folder
    suspend fun saveInvoiceToDownloads(order: Order): String = withContext(Dispatchers.IO) {
        try {
            val state = Environment.getExternalStorageState()
            if (Environment.MEDIA_MOUNTED != state) {
                return@withContext "Downloads Folder unavailable/not mounted"
            }

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val fileName = "Invoice_Order_#${order.id}.txt"
            val file = File(downloadsDir, fileName)
            val fileOutputStream = FileOutputStream(file)

            val invoiceContent = """
                =========================================
                             QUICKSTORE INVOICE
                =========================================
                Order Reference ID: #${order.id}
                Date/Time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(order.timestamp))}
                
                CUSTOMER DETAILS
                ----------------
                Name: ${order.customerName}
                Phone: ${order.customerPhone}
                Delivery Address: ${order.address}
                Coordinates: [${order.latitude}, ${order.longitude}]
                
                PURCHASED PRODUCTS & SERVICES
                ----------------
                ${order.itemsJson}
                
                SUMMARY DETAILS
                ---------------
                Delivery Charge: ₹${order.deliveryCharge}
                Discount Applied: -₹${order.discountApplied} (${order.couponCode ?: "No Coupon"})
                Total Paid/Due: ₹${order.totalAmount}
                Payment Mode: ${order.paymentMethod}
                Payment Status: ${order.paymentStatus}
                Delivery Status: ${order.orderStatus}
                Secure Handover Verification OTP: ${order.deliveryOtp}
                
                Thank you for shopping with us!
                For details support, call us on WhatsApp.
                =========================================
            """.trimIndent()

            fileOutputStream.write(invoiceContent.toByteArray())
            fileOutputStream.flush()
            fileOutputStream.close()

            "Invoice saved successfully: Downloads/$fileName"
        } catch (e: Exception) {
            Log.e("InvoiceDownload", "Error saving invoice", e)
            "Download failed: ${e.message}"
        }
    }

    // Save master delivery sales logs reports for store owners
    suspend fun saveSalesReportToDownloads(): String = withContext(Dispatchers.IO) {
        try {
            val size = ordersList.value.size
            if (size == 0) return@withContext "No orders available to generate report"

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val fileName = "QuickStore_Sales_Report.txt"
            val file = File(downloadsDir, fileName)
            val fileOutputStream = FileOutputStream(file)

            val summaryHeader = """
                ===================================================
                         QUICKSTORE - STORE OWNER SALES REPORT
                ===================================================
                Generated: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}
                Total Registered Orders: $size
                Total Revenue generated: ₹${ordersList.value.sumOf { it.totalAmount }}
                Total Delivery Charges: ₹${ordersList.value.sumOf { it.deliveryCharge }}
                Total Discount Redeemed: ₹${ordersList.value.sumOf { it.discountApplied }}
                
                BREAKDOWN BY ORDER:
                ---------------------------------------------------
            """.trimIndent()

            val csvContentBuilder = StringBuilder(summaryHeader)
            ordersList.value.forEach { order ->
                csvContentBuilder.append("\nOrder #${order.id} | Amount: ₹${order.totalAmount} | Cust: ${order.customerName} (${order.customerPhone}) | Status: ${order.orderStatus} | Method: ${order.paymentMethod}")
            }

            fileOutputStream.write(csvContentBuilder.toString().toByteArray())
            fileOutputStream.flush()
            fileOutputStream.close()

            "Sales report saved successfully: Downloads/$fileName"
        } catch (e: Exception) {
            Log.e("SalesReportDownload", "Error saving sales report", e)
            "Download failed: ${e.message}"
        }
    }

    // Seeds standard items, banners, coupons, and delivery boys if the database starts empty
    private suspend fun seedInitialDatabaseIfEmpty() {
        // Prepopulate products
        val prods = db.appDao.getAllProductsFlow().first()
        if (prods.isEmpty()) {
            val initial = listOf(
                Product(
                    name = "Symphony Tower Air Cooler",
                    description = "High efficiency heavy-duty tower body cooler with 3-speed fan control, 40L high water capacity, and automated swing louvers. Highly silent and powerful airflow.",
                    price = 6499.0,
                    category = "Cooling Devices",
                    stock = 25,
                    images = "cooler"
                ),
                Product(
                    name = "Bajaj GX8 750W Mixer Grinder (Mixi)",
                    description = "Heavy-duty 750W 100% copper motor mixer grinder with 3 premium stainless steel jars, durable flow breaker design and multiple speed settings.",
                    price = 3499.0,
                    category = "Kitchen Appliances",
                    stock = 18,
                    images = "mixer"
                ),
                Product(
                    name = "Multi-Socket Series Testing Board",
                    description = "Professional series testing board with clear dual indicator lamps, copper fuse line wire protection, high density sockets, and 5-meter power lead.",
                    price = 499.0,
                    category = "Power & Utilities",
                    stock = 40,
                    images = "series_board"
                ),
                Product(
                    name = "Philips Dry Iron Glide HP",
                    description = "Ultra lightweight dry iron with dynamic gold coating linisole plate, automatic LED thermal regulation, and comfortable premium grip.",
                    price = 999.0,
                    category = "Home Appliances",
                    stock = 35,
                    images = "iron"
                ),
                Product(
                    name = "Prestige Electric Kettle 1.5L",
                    description = "1.5 Liter quick boiling heating kettle with premium food-safe stainless steel interior body, cool touch outer matte finish and automatic auto shut-off.",
                    price = 1299.0,
                    category = "Kitchen Appliances",
                    stock = 50,
                    images = "kettle"
                ),
                Product(
                    name = "Havells Pedestal Dynamic Fan",
                    description = "High performance pedestal wind fan with steady heavy-weight base plate, telescopic vertical height adjusters, and thermal safety overload protector.",
                    price = 2699.0,
                    category = "Cooling Devices",
                    stock = 15,
                    images = "fan"
                ),
                Product(
                    name = "Pigeon Sandwich Toaster Grill",
                    description = "Equipped with high grade non-stick grill plates, heat-resistant cool touch safety handle click lock, power on indicators, and fast uniform heating.",
                    price = 1199.0,
                    category = "Kitchen Appliances",
                    stock = 20,
                    images = "toaster"
                ),
                Product(
                    name = "Spike Guard 6-Way Extension Board",
                    description = "Heavy-duty surge protector power strip with automatic surge suppressor safety fuse, master indicator toggle switch, and copper brass points.",
                    price = 599.0,
                    category = "Power & Utilities",
                    stock = 55,
                    images = "extension"
                ),
                Product(
                    name = "Kent Elegant RO+UV Purifier",
                    description = "8 Liters holding capacity premium kitchen water filter using advanced RO and UV treatment lamps to deliver 100% pure mineral-rich water.",
                    price = 14999.0,
                    category = "Kitchen Appliances",
                    stock = 10,
                    images = "purifier"
                )
            )
            initial.forEach { repository.insertProduct(it) }
        }

        // Prepopulate coupons
        val coupons = db.appDao.getAllCouponsFlow().first()
        if (coupons.isEmpty()) {
            repository.insertCoupon(Coupon("WELCOME100", 100.0, 499.0))
            repository.insertCoupon(Coupon("MEGAELECTRIC", 500.0, 2999.0))
            repository.insertCoupon(Coupon("KITCHENSAVE", 1000.0, 4999.0))
        }

        // Prepopulate banners
        val banners = db.appDao.getAllBannersFlow().first()
        if (banners.isEmpty()) {
            repository.insertBanner(Banner(imageUrl = "banner_cooling", title = "Vivid Summer Cooling Air Cooler Deals Live!"))
            repository.insertBanner(Banner(imageUrl = "banner_kitchen", title = "Unbeatable Offers on Modern Mixie & Kitchen sets!"))
            repository.insertBanner(Banner(imageUrl = "banner_power", title = "Premium Series Boards & Utilities, Flat 30% Off!"))
        }

        // Prepopulate delivery boys
        val boys = db.appDao.getAllDeliveryBoysFlow().first()
        if (boys.isEmpty()) {
            repository.insertDeliveryBoy(DeliveryBoy(name = "Karan Sharma", phone = "+91 9102938475", active = true))
            repository.insertDeliveryBoy(DeliveryBoy(name = "Ravi Teja", phone = "+91 8877665544", active = true))
            repository.insertDeliveryBoy(DeliveryBoy(name = "Amit Yadav", phone = "+91 7766123456", active = true))
        }
    }
}
