package com.example.simplerestaurantapi.handlers.v1

import com.example.simplerestaurantapi.data.TableUpdateRequest
import com.example.simplerestaurantapi.entity.Table
import com.example.simplerestaurantapi.services.TableService
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
class TableHandler(
    private val tableService: TableService
) {
    fun all(request: ServerRequest): Mono<ServerResponse> = ok().body(tableService.findAll())

    fun create(request: ServerRequest): Mono<ServerResponse> =
        request.bodyToMono<Table>().flatMap(tableService::create)
            .flatMap {
                created(UriComponentsBuilder.fromPath("/" + it.id).build().toUri())
                    .body(Mono.just(it))
            }

    fun findById(request: ServerRequest): Mono<ServerResponse> =
        tableService.findById(request.pathVariable("id").toInt())
            .flatMap { ok().body(Mono.just(it)) }
            .switchIfEmpty(notFound().build())

    fun updateById(request: ServerRequest): Mono<ServerResponse> = request.bodyToMono<TableUpdateRequest>()
        .flatMap { tableService.update(request.pathVariable("id").toInt(), it) }
        .flatMap { ok().body(Mono.just(it)) }
        .switchIfEmpty(notFound().build())

    fun deleteById(request: ServerRequest): Mono<ServerResponse> =
        tableService.deleteById(request.pathVariable("id").toInt())
            .flatMap { ok().build() }

}
