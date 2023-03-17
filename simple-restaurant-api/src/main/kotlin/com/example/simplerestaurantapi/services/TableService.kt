package com.example.simplerestaurantapi.services

import com.example.simplerestaurantapi.data.TableUpdateRequest
import com.example.simplerestaurantapi.entity.Table
import com.example.simplerestaurantapi.repository.TableRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class TableService(
    private val tableRepository: TableRepository
) {
    fun findAll() = tableRepository.findAll()
    fun findById(id: Int) = tableRepository.findById(id)
    fun create(table: Table) = tableRepository.save(table)
    fun update(id: Int, request: TableUpdateRequest) = tableRepository.findById(id)
        .flatMap { table ->
            tableRepository.save(table.copy(
                name = request.name ?: table.name,
                capacity = request.capacity ?: table.capacity,
                updatedAt = LocalDateTime.now()
            ))
        }
        .switchIfEmpty(Mono.empty())
    fun deleteById(id: Int) = tableRepository.deleteById(id)

}
