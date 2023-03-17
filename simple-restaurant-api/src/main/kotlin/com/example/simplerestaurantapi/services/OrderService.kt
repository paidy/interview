package com.example.simplerestaurantapi.services

import com.example.simplerestaurantapi.data.OrderCreateRequest
import com.example.simplerestaurantapi.repository.OrderRepository
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val orderRepository: OrderRepository
) {
    fun findAll() = orderRepository.findAll()
    fun findById(orderId: Int) = orderRepository.findById(orderId)
    fun findAllByTableId(tableId: Int) = orderRepository.findAllByTableId(tableId)
    fun create(request: OrderCreateRequest) = orderRepository.create(request.tableId, request.menuId, request.quantity)
    fun updateQuantity(orderId: Int, quantity: Int) = orderRepository.updateQuantity(orderId, quantity)
    fun deleteById(orderId: Int) = orderRepository.deleteById(orderId)
    fun deleteByTableId(tableId: Int) = orderRepository.deleteByTableId(tableId)
}
