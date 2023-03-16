package com.example.simplerestaurantapi.routes

import com.example.simplerestaurantapi.handlers.TableHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

@Configuration
class TableRouter {
    @Bean
    fun routes(handler: TableHandler) = router {
        (accept(MediaType.APPLICATION_JSON) and "/menu").nest {
            GET("/").invoke(handler::all)
            POST("/").invoke(handler::create)
            GET("/{id}").invoke(handler::getById)
            PUT("/{id}").invoke(handler::updateById)
            DELETE("/{id}").invoke(handler::deleteById)
        }
    }
}
