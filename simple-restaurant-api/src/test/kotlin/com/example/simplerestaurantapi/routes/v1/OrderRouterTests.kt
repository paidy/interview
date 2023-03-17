package com.example.simplerestaurantapi.routes.v1

import com.example.simplerestaurantapi.data.OrderCreateRequest
import com.example.simplerestaurantapi.data.OrderUpdateRequest
import com.example.simplerestaurantapi.entity.Menu
import com.example.simplerestaurantapi.entity.Order
import com.example.simplerestaurantapi.entity.Table
import com.example.simplerestaurantapi.handlers.v1.OrderHandler
import com.example.simplerestaurantapi.services.OrderService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.reactive.server.expectBodyList
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.random.Random

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderRouterTests {
    @MockkBean
    private lateinit var orderService: OrderService

    private lateinit var orderHandler: OrderHandler
    private lateinit var client: WebTestClient

    @BeforeAll
    fun setUp() {
        orderHandler = OrderHandler(orderService)
        client = WebTestClient.bindToRouterFunction(OrderRouter().orderRoutes(orderHandler)).build()
    }

    private final fun dummyTable() = iterator {
        var id = 1
        val name = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        while (true)
            yield(Table(id++, name.random().toString(), Random.nextInt(2, 10)))
    }

    private final fun dummyMenu() = iterator {
        var id = 1
        val name = listOf("Pizza", "Noodle", "Spaghetti", "Burger", "Fried Chicken", "Rice", "Salad")
        while (true)
            yield(Menu(id++, name.random(), Random.nextInt(90, 600)))
    }

    private final fun dummyOrder() = iterator {
        var id = 1
        while (true)
            yield(Order(id++, dummyTable().next(), dummyMenu().next(), Random.nextInt(1, 5)))
    }

    @Test
    fun `Get All Orders should return empty list`() {
        every { orderService.findAll() } returns Flux.empty()
        client.get()
            .uri("/v1/orders")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBodyList<Order>().hasSize(0)
    }

    @Test
    fun `Get All Orders should return items`() {
        every { orderService.findAll() } returns Flux.just(dummyOrder().next(), dummyOrder().next())
        client.get()
            .uri("/v1/orders")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBodyList<Order>().hasSize(2)
    }

    @Test
    fun `Get All Orders should return items by Table ID`() {
        every { orderService.findAll() } returns Flux.just(dummyOrder().next())
        client.get()
            .uri("/v1/orders?table=1")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBodyList<Order>().hasSize(1)
    }

    @Test
    fun `Get Order should return not found`() {
        every { orderService.findById(any()) } returns Mono.empty()
        client.get()
            .uri("/v1/orders/1")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `Get Order should return an item`() {
        val testOrder = dummyOrder().next()
        every { orderService.findById(any()) } returns Mono.just(testOrder)
        client.get()
            .uri("/v1/orders/1")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody<Order>().isEqualTo(testOrder)
    }

    @Test
    fun `Create Order should create a list with an item`() {
        val order = dummyOrder().next()
        val request = OrderCreateRequest(order.table.id, order.menu.id, order.quantity)
        every { orderService.create(request) } returns Mono.just(order)
        client.post()
            .uri("/v1/orders")
            .accept(MediaType.APPLICATION_JSON)
            .body(Flux.just(request), OrderCreateRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBodyList<Order>().hasSize(1)
    }

    @Test
    fun `Create Order should create some items`() {
        val order1 = dummyOrder().next()
        val order2 = dummyOrder().next()
        val request1 = OrderCreateRequest(order1.table.id, order1.menu.id, order1.quantity)
        val request2 = OrderCreateRequest(order2.table.id, order2.menu.id, order2.quantity)
        every { orderService.create(request1) } returns Mono.just(order1)
        every { orderService.create(request2) } returns Mono.just(order2)
        client.post()
            .uri("/v1/orders")
            .accept(MediaType.APPLICATION_JSON)
            .body(Flux.just(request1, request2), OrderCreateRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBodyList<Order>().hasSize(2)
    }

    @Test
    fun `Update Order should not update non-existent item`() {
        val request = OrderUpdateRequest(Random.nextInt(1, 10))
        every { orderService.updateQuantity(any(), any()) } returns Mono.empty()
        client.put()
            .uri("/v1/orders/1")
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(request), OrderUpdateRequest::class.java)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `Update Order should update an item`() {
        val request = OrderUpdateRequest(Random.nextInt(1, 10))
        val order = dummyOrder().next().copy(quantity = request.quantity)
        every { orderService.updateQuantity(any(), any()) } returns Mono.just(order)
        client.put()
            .uri("/v1/orders/1")
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(request), OrderUpdateRequest::class.java)
            .exchange()
            .expectStatus().isOk
            .expectBody<Order>().isEqualTo(order)
    }

    @Test
    fun `Delete Order should delete an item`() {
        every { orderService.deleteById(any()) } returns Mono.empty()
        client.delete()
            .uri("/v1/orders/1")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `Delete Order should delete all items by Table ID`() {
        every { orderService.deleteByTableId(any()) } returns Mono.empty()
        client.delete()
            .uri("/v1/orders?table=1")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
    }
}
