package com.example.simplerestaurantapi.services

import com.example.simplerestaurantapi.data.MenuUpdateRequest
import com.example.simplerestaurantapi.entity.Menu
import com.example.simplerestaurantapi.repository.MenuRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class MenuService(
    private val menuRepository: MenuRepository
) {
    fun findAll() = menuRepository.findAll()
    fun findById(id: Int) = menuRepository.findById(id)
    fun create(menu: Menu) = menuRepository.save(menu)
    fun update(id: Int, request: MenuUpdateRequest) = menuRepository.findById(id)
        .flatMap { menu ->
            menuRepository.save(menu.copy(
                name = request.name ?: menu.name,
                cookingTimeSec = request.cookingTimeSec ?: menu.cookingTimeSec,
                updatedAt = LocalDateTime.now()
            ))
        }
        .switchIfEmpty(Mono.empty())
    fun deleteById(id: Int) = menuRepository.deleteById(id)

}
