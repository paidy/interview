package com.example.simplerestaurantapi.repository

import com.example.simplerestaurantapi.entity.Table
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import java.time.LocalDateTime

class TableRepositoryImpl(
    private val template: R2dbcEntityTemplate
): TableRepository {
    private fun mapRow(row: io.r2dbc.spi.Readable) =
        Table(
            row.get("id")!! as Int,
            row.get("name")!! as String,
            row.get("capacity")!! as Int,
            row.get("created_at")!! as LocalDateTime,
            row.get("updated_at")!! as LocalDateTime
        )

    override fun findAll() =
        template.databaseClient.inConnectionMany { connection ->
            connection.createStatement("SELECT * FROM ${Table.TABLE_NAME}").execute()
                .toFlux()
                .flatMap {
                    it.map(::mapRow).toFlux()
                }
        }

    override fun findById(id: Int) =
        template.databaseClient.inConnection { connection ->
            connection.createStatement("SELECT * FROM ${Table.TABLE_NAME} WHERE id = ?")
                .bind(0, id)
                .execute()
                .toMono()
                .flatMap {
                    it.map(::mapRow).toMono()
                }
        }

    override fun save(table: Table) =
        template.databaseClient.inConnection { connection ->
            connection.createStatement("INSERT INTO ${Table.TABLE_NAME} (name, capacity) VALUES (?, ?)")
                .bind(0, table.name)
                .bind(1, table.capacity)
                .execute()
                .toMono()
                .flatMap {
                    connection.createStatement("SELECT * FROM ${Table.TABLE_NAME} WHERE id = LAST_INSERT_ID()")
                        .execute()
                        .toMono()
                        .flatMap {
                            it.map(::mapRow).toMono()
                        }
                }
        }

    override fun deleteById(id: Int) =
        template.databaseClient.inConnection<Void?> { connection ->
            connection.createStatement("DELETE FROM ${Table.TABLE_NAME} WHERE id = ?")
                .bind(0, id)
                .execute()
                .toMono()
                .then()
        }
}