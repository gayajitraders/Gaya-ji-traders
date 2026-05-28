package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {

    // --- User Profile ---
    fun getUserByIdFlow(id: Int): Flow<UserProfile?> = appDao.getUserByIdFlow(id)
    fun getAllUserProfilesFlow(): Flow<List<UserProfile>> = appDao.getAllUserProfilesFlow()
    fun getUsersByRoleFlow(role: String): Flow<List<UserProfile>> = appDao.getUsersByRoleFlow(role)
    fun getLoggedInUserFlow(): Flow<UserProfile?> = appDao.getLoggedInUserFlow()
    suspend fun getUserByPhone(phone: String): UserProfile? = appDao.getUserByPhone(phone)
    suspend fun insertUser(user: UserProfile): Long = appDao.insertUser(user)
    suspend fun logoutAllUsers() = appDao.logoutAllUsers()
    suspend fun getLoggedInUser(): UserProfile? = appDao.getLoggedInUser()

    // --- Products ---
    val allProductsFlow: Flow<List<Product>> = appDao.getAllProductsFlow()
    fun getProductsByCategoryFlow(category: String): Flow<List<Product>> = appDao.getProductsByCategoryFlow(category)
    suspend fun getProductById(productId: Int): Product? = appDao.getProductById(productId)
    suspend fun insertProduct(product: Product): Long = appDao.insertProduct(product)
    suspend fun updateProduct(product: Product) = appDao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = appDao.deleteProduct(product)

    // --- Cart Items ---
    val cartItemsFlow: Flow<List<CartItem>> = appDao.getCartItemsFlow()
    suspend fun addToCart(cartItem: CartItem) = appDao.addToCart(cartItem)
    suspend fun updateCartItem(cartItem: CartItem) = appDao.updateCartItem(cartItem)
    suspend fun removeCartItem(productId: Int) = appDao.removeCartItem(productId)
    suspend fun clearCart() = appDao.clearCart()

    // --- Orders ---
    val allOrdersFlow: Flow<List<Order>> = appDao.getAllOrdersFlow()
    fun getOrdersByCustomerPhoneFlow(phone: String): Flow<List<Order>> = appDao.getOrdersByCustomerPhoneFlow(phone)
    fun getOrdersForDeliveryBoyFlow(deliveryBoyId: Int): Flow<List<Order>> = appDao.getOrdersForDeliveryBoyFlow(deliveryBoyId)
    suspend fun getOrderById(orderId: Int): Order? = appDao.getOrderById(orderId)
    suspend fun insertOrder(order: Order): Long = appDao.insertOrder(order)
    suspend fun updateOrder(order: Order) = appDao.updateOrder(order)

    // --- Coupons ---
    val allCouponsFlow: Flow<List<Coupon>> = appDao.getAllCouponsFlow()
    suspend fun getCouponByCode(code: String): Coupon? = appDao.getCouponByCode(code)
    suspend fun insertCoupon(coupon: Coupon) = appDao.insertCoupon(coupon)
    suspend fun deleteCoupon(coupon: Coupon) = appDao.deleteCoupon(coupon)

    // --- Banners ---
    val allBannersFlow: Flow<List<Banner>> = appDao.getAllBannersFlow()
    suspend fun insertBanner(banner: Banner) = appDao.insertBanner(banner)
    suspend fun deleteBanner(id: Int) = appDao.deleteBanner(id)

    // --- Delivery Boys ---
    val allDeliveryBoysFlow: Flow<List<DeliveryBoy>> = appDao.getAllDeliveryBoysFlow()
    suspend fun insertDeliveryBoy(boy: DeliveryBoy) = appDao.insertDeliveryBoy(boy)
    suspend fun deleteDeliveryBoy(id: Int) = appDao.deleteDeliveryBoy(id)
}
