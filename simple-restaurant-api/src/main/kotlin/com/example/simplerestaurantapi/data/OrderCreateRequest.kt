package com.example.simplerestaurantapi.data

data class OrderCreateRequest(
    val tableId: Int,
    val menuId: Int,
    val quantity: Int
)
