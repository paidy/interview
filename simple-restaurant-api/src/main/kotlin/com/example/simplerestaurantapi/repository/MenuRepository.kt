package com.example.simplerestaurantapi.repository

import com.example.simplerestaurantapi.entity.Menu
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface MenuRepository: ReactiveCrudRepository<Menu, Int>
