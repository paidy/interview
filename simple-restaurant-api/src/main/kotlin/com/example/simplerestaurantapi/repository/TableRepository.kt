package com.example.simplerestaurantapi.repository

import com.example.simplerestaurantapi.entity.Table
import org.springframework.data.repository.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface TableRepository: Repository<Table, Int> {
    fun findAll(): Flux<Table>
    fun findById(id: Int): Mono<Table>
    fun save(table: Table): Mono<Table>
    fun deleteById(id: Int): Mono<Void>
}
