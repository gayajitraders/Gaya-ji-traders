package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- User Profile ---
    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    fun getUserByIdFlow(id: Int): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles")
    fun getAllUserProfilesFlow(): Flow<List<UserProfile>>

    @Query("SELECT * FROM user_profiles WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserProfile?

    @Query("SELECT * FROM user_profiles WHERE role = :role")
    fun getUsersByRoleFlow(role: String): Flow<List<UserProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProfile): Long

    @Query("UPDATE user_profiles SET isLoggedIn = 0")
    suspend fun logoutAllUsers()

    @Query("SELECT * FROM user_profiles WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUser(): UserProfile?

    @Query("SELECT * FROM user_profiles WHERE isLoggedIn = 1 LIMIT 1")
    fun getLoggedInUserFlow(): Flow<UserProfile?>

    // --- Products ---
    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProductsFlow(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY id DESC")
    fun getProductsByCategoryFlow(category: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: Int): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    // --- Cart Items ---
    @Query("SELECT * FROM cart_items")
    fun getCartItemsFlow(): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToCart(cartItem: CartItem)

    @Update
    suspend fun updateCartItem(cartItem: CartItem)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun removeCartItem(productId: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    // --- Orders ---
    @Query("SELECT * FROM orders ORDER BY id DESC")
    fun getAllOrdersFlow(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE customerPhone = :phone ORDER BY id DESC")
    fun getOrdersByCustomerPhoneFlow(phone: String): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE deliveryBoyId = :deliveryBoyId ORDER BY id DESC")
    fun getOrdersForDeliveryBoyFlow(deliveryBoyId: Int): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: Int): Order?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Update
    suspend fun updateOrder(order: Order)

    // --- Coupons ---
    @Query("SELECT * FROM coupons")
    fun getAllCouponsFlow(): Flow<List<Coupon>>

    @Query("SELECT * FROM coupons WHERE code = :code LIMIT 1")
    suspend fun getCouponByCode(code: String): Coupon?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: Coupon)

    @Delete
    suspend fun deleteCoupon(coupon: Coupon)

    // --- Banners ---
    @Query("SELECT * FROM banners")
    fun getAllBannersFlow(): Flow<List<Banner>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanner(banner: Banner)

    @Query("DELETE FROM banners WHERE id = :id")
    suspend fun deleteBanner(id: Int)

    // --- Delivery Boys ---
    @Query("SELECT * FROM delivery_boys")
    fun getAllDeliveryBoysFlow(): Flow<List<DeliveryBoy>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveryBoy(boy: DeliveryBoy)

    @Query("DELETE FROM delivery_boys WHERE id = :id")
    suspend fun deleteDeliveryBoy(id: Int)
}
