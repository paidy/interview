package com.example.simplerestaurantapi.routes.v1

import com.example.simplerestaurantapi.handlers.v1.MenuHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

@Configuration
class MenuRouter {
    @Bean
    fun menuRoutes(handler: MenuHandler) = router {
        (accept(MediaType.APPLICATION_JSON) and "/v1/menus").nest {
            GET("").invoke(handler::all)
            POST("").invoke(handler::create)
            GET("/{id}").invoke(handler::findById)
            PUT("/{id}").invoke(handler::updateById)
            DELETE("/{id}").invoke(handler::deleteById)
        }
    }
}
