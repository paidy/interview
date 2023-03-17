package com.example.simplerestaurantapi.repository

import com.example.simplerestaurantapi.entity.Menu
import org.springframework.data.repository.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface MenuRepository: Repository<Menu, Int> {
    fun findAll(): Flux<Menu>
    fun findById(id: Int): Mono<Menu>
    fun save(menu: Menu): Mono<Menu>
    fun deleteById(id: Int): Mono<Void>
}
