package com.example.simplerestaurantapi.repository

import com.example.simplerestaurantapi.entity.Order
import org.springframework.data.repository.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface OrderRepository: Repository<Order, Int> {
    fun findAll(): Flux<Order>
    fun findById(orderId: Int): Mono<Order>
    fun findAllByTableId(tableId: Int): Flux<Order>
    fun create(tableId: Int, menuId: Int, quantity: Int): Mono<Order>
    fun updateQuantity(orderId: Int, quantity: Int): Mono<Order>
    fun deleteById(orderId: Int): Mono<Void>
    fun deleteByTableId(tableId: Int): Mono<Void>
}
