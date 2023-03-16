package com.example.simplerestaurantapi.handlers

import com.example.simplerestaurantapi.data.MenuUpdateRequest
import com.example.simplerestaurantapi.entity.Menu
import com.example.simplerestaurantapi.services.MenuService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToMono
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

@Component
class MenuHandler(
    private val menuService: MenuService
) {
    fun all(request: ServerRequest): Mono<ServerResponse> = ok().body(menuService.findAll())
    fun create(request: ServerRequest): Mono<ServerResponse> =
        request.bodyToMono<Menu>().flatMap(menuService::create)
            .flatMap {
                created(UriComponentsBuilder.fromPath("/" + it.id).build().toUri())
                    .body(Mono.just(it))
            }
    fun getById(request: ServerRequest): Mono<ServerResponse> =
        menuService.findById(request.pathVariable("id").toInt())
            .flatMap { ok().body(Mono.just(it)) }
            .switchIfEmpty(notFound().build())
    fun updateById(request: ServerRequest): Mono<ServerResponse> = request.bodyToMono<MenuUpdateRequest>()
        .flatMap { menuService.update(request.pathVariable("id").toInt(), it) }
        .flatMap { ok().body(Mono.just(it)) }
        .switchIfEmpty(notFound().build())
    fun deleteById(request: ServerRequest): Mono<ServerResponse> =
        menuService.deleteById(request.pathVariable("id").toInt())
            .flatMap { ok().build() }

}
