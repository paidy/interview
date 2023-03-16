package com.example.simplerestaurantapi.repository

import com.example.simplerestaurantapi.entity.Table
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface TableRepository: ReactiveCrudRepository<Table, Int>
