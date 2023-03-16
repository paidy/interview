package com.example.simplerestaurantapi.repository

import com.example.simplerestaurantapi.entity.Order
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface OrderRepository: ReactiveCrudRepository<Order, Int>
