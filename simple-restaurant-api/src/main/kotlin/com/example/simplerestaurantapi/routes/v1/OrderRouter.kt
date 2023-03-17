package com.example.simplerestaurantapi.routes.v1

import com.example.simplerestaurantapi.handlers.v1.OrderHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RequestPredicates.all
import org.springframework.web.reactive.function.server.router

@Configuration
class OrderRouter {
    @Bean
    fun orderRoutes(handler: OrderHandler) = router {
        (accept(MediaType.APPLICATION_JSON) and "/v1/orders").nest {
            GET("/").invoke(handler::all)
            GET("/", all()
                .and(queryParam("table") { true }),
                handler::findByTableId)
            GET("/{id}").invoke(handler::findById)
            POST("/").invoke(handler::create)
            PUT("/{id}").invoke(handler::updateById)
            DELETE("/").invoke(handler::deleteByTableId)
            DELETE("/{id}").invoke(handler::deleteById)
        }
    }
}
