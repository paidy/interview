package com.example.simplerestaurantapi.repository

import com.example.simplerestaurantapi.entity.Menu
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import java.time.LocalDateTime

class MenuRepositoryImpl(private val template: R2dbcEntityTemplate): MenuRepository {
    private fun mapRow(row: io.r2dbc.spi.Readable) =
        Menu(
            row.get("id")!! as Int,
            row.get("name")!! as String,
            row.get("cooking_time_sec")!! as Int,
            row.get("created_at")!! as LocalDateTime,
            row.get("updated_at")!! as LocalDateTime
        )

    override fun findAll(): Flux<Menu> =
        template.databaseClient.inConnectionMany { connection ->
            connection.createStatement("SELECT * FROM ${Menu.TABLE_NAME}").execute()
                .toFlux()
                .flatMap {
                    it.map(::mapRow).toFlux()
                }
        }

    override fun findById(id: Int) =
        template.databaseClient.inConnection { connection ->
            connection.createStatement("SELECT * FROM ${Menu.TABLE_NAME} WHERE id = ?")
                .bind(0, id)
                .execute()
                .toMono()
                .flatMap {
                    it.map(::mapRow).toMono()
                }
        }

    override fun save(menu: Menu) =
        template.databaseClient.inConnection { connection ->
            connection.createStatement("INSERT INTO ${Menu.TABLE_NAME} (name, cooking_time_sec) VALUES (?, ?)")
                .bind(0, menu.name)
                .bind(1, menu.cookingTimeSec)
                .execute()
                .toMono()
                .flatMap {
                    connection.createStatement("SELECT * FROM ${Menu.TABLE_NAME} WHERE id = LAST_INSERT_ID()")
                        .execute()
                        .toMono()
                        .flatMap {
                            it.map(::mapRow).toMono()
                        }
                }
        }

    override fun deleteById(id: Int) =
        template.databaseClient.inConnection<Void?> { connection ->
            connection.createStatement("DELETE FROM ${Menu.TABLE_NAME} WHERE id = ?")
                .bind(0, id)
                .execute()
                .toMono()
                .then()
        }
}
