package com.example.simplerestaurantapi.handlers.v1

import com.example.simplerestaurantapi.data.MenuUpdateRequest
import com.example.simplerestaurantapi.data.OrderCreateRequest
import com.example.simplerestaurantapi.data.OrderUpdateRequest
import com.example.simplerestaurantapi.services.OrderService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono

@Component
class OrderHandler(
    private val orderService: OrderService
) {
    fun all(request: ServerRequest): Mono<ServerResponse> =
        ok().body(orderService.findAll())

    fun findByTableId(request: ServerRequest): Mono<ServerResponse> =
        ok().body(
            request.queryParamOrNull("table")?.let { tableId ->
                orderService.findAllByTableId(tableId.toInt())
            } ?: Mono.empty()
        )

    fun findById(request: ServerRequest): Mono<ServerResponse> =
        orderService.findById(request.pathVariable("id").toInt())
            .flatMap { ok().body(Mono.just(it)) }
            .switchIfEmpty(notFound().build())

    fun create(request: ServerRequest): Mono<ServerResponse> =
        ok().body(
            request.bodyToFlux<OrderCreateRequest>()
                .flatMap { orderService.create(it) }
        )

    fun updateById(request: ServerRequest): Mono<ServerResponse> =
        request.bodyToMono<OrderUpdateRequest>()
            .flatMap { orderService.updateQuantity(request.pathVariable("id").toInt(), it.quantity) }
            .flatMap { ok().body(Mono.just(it)) }
            .switchIfEmpty(notFound().build())

    fun deleteById(request: ServerRequest): Mono<ServerResponse> =
        orderService.deleteById(request.pathVariable("id").toInt())
            .flatMap { ok().build() }

    fun deleteByTableId(request: ServerRequest): Mono<ServerResponse> =
        ok().body(
            request.queryParamOrNull("table")?.let { tableId ->
                orderService.deleteByTableId(tableId.toInt())
            } ?: Mono.empty()
        )
}
