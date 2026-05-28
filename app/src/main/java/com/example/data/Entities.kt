package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val phone: String,
    val name: String,
    val address: String? = null,
    val latitude: Double = 28.6139, // Default coordinates (e.g., Delhi, India)
    val longitude: Double = 77.2090,
    val isLoggedIn: Boolean = false,
    val role: String = "customer" // customer, admin, delivery
) : Serializable

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val stock: Int,
    val images: String, // Comma-separated strings of image resource descriptors or links
    val offers: String? = null
) : Serializable

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val quantity: Int
) : Serializable

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val customerPhone: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val itemsJson: String, // String representation of CartItems list (name x count -> price)
    val totalAmount: Double,
    val deliveryCharge: Double,
    val discountApplied: Double,
    val couponCode: String?,
    val paymentMethod: String, // "COD" or "Online"
    val paymentStatus: String, // "Pending" or "Paid"
    val orderStatus: String, // "Placed", "Accepted", "Dispatched", "Out for Delivery", "Delivered"
    val deliveryBoyId: Int? = null,
    val deliveryOtp: String = "", // OTP code (e.g. "4829")
    val liveLatitude: Double? = null,
    val liveLongitude: Double? = null,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "coupons")
data class Coupon(
    @PrimaryKey val code: String,
    val discountAmount: Double,
    val minPurchase: Double
) : Serializable

@Entity(tableName = "banners")
data class Banner(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imageUrl: String,
    val title: String? = null
) : Serializable

@Entity(tableName = "delivery_boys")
data class DeliveryBoy(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val active: Boolean = true
) : Serializable
