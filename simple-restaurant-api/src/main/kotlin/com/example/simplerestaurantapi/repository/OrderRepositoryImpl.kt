package com.example.simplerestaurantapi.repository

import com.example.simplerestaurantapi.entity.Menu
import com.example.simplerestaurantapi.entity.Order
import com.example.simplerestaurantapi.entity.Table
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import java.time.LocalDateTime

class OrderRepositoryImpl(private val template: R2dbcEntityTemplate): OrderRepository {
    private fun mapRow(row: io.r2dbc.spi.Readable) =
        Order(
            row.get("id")!! as Int,
            Table(
                row.get("table_id")!! as Int,
                row.get("table_name")!! as String,
                row.get("table_capacity")!! as Int
            ),
            Menu(
                row.get("menu_id")!! as Int,
                row.get("menu_name")!! as String,
                row.get("menu_cooking_time_sec")!! as Int
            ),
            row.get("quantity")!! as Int,
            row.get("created_at")!! as LocalDateTime,
            row.get("updated_at")!! as LocalDateTime
        )

    companion object {
        const val SELECT_JOIN_COLUMNS = """
            orders.*,
                    m.id as menu_id, m.name as menu_name, m.cooking_time_sec as menu_cooking_time_sec,
                    t.id as table_id, t.name as table_name, t.capacity as table_capacity
        """
    }

    override fun findAll(): Flux<Order> =
        template.databaseClient.inConnectionMany { connection ->
            connection.createStatement("""
                SELECT $SELECT_JOIN_COLUMNS
                FROM ${Order.TABLE_NAME}
                JOIN ${Menu.TABLE_NAME} m on orders.menu_id = m.id
                JOIN ${Table.TABLE_NAME} t on orders.table_id = t.id
                """)
                .execute()
                .toFlux()
                .flatMap {
                    it.map(::mapRow).toFlux()
                }
        }

    override fun findById(orderId: Int) =
        template.databaseClient.inConnection { connection ->
            connection.createStatement("""
                SELECT $SELECT_JOIN_COLUMNS
                FROM ${Order.TABLE_NAME}
                JOIN ${Menu.TABLE_NAME} m on orders.menu_id = m.id
                JOIN ${Table.TABLE_NAME} t on orders.table_id = t.id
                WHERE id = ?
                """)
                .bind(0, orderId)
                .execute()
                .toMono()
                .flatMap {
                    it.map(::mapRow).toMono()
                }
        }

    override fun findAllByTableId(tableId: Int) =
        template.databaseClient.inConnectionMany { connection ->
            connection.createStatement("""
                SELECT $SELECT_JOIN_COLUMNS
                FROM ${Order.TABLE_NAME}
                JOIN ${Menu.TABLE_NAME} m on orders.menu_id = m.id
                JOIN ${Table.TABLE_NAME} t on orders.table_id = t.id
                WHERE table_id = ?
            """.trimIndent())
                .bind(0, tableId)
                .execute()
                .toFlux()
                .flatMap {
                    it.map(::mapRow).toFlux()
                }
        }

    override fun create(tableId: Int, menuId: Int, quantity: Int) =
        template.databaseClient.inConnection { connection ->
            connection.createStatement("INSERT INTO ${Order.TABLE_NAME} (table_id, menu_id, quantity) VALUES (?, ?, ?)")
                .bind(0, tableId)
                .bind(1, menuId)
                .bind(2, quantity)
                .execute()
                .toMono()
                .flatMap {
                    connection.createStatement("""
                        SELECT $SELECT_JOIN_COLUMNS
                        FROM ${Order.TABLE_NAME}
                        JOIN ${Menu.TABLE_NAME} m on orders.menu_id = m.id
                        JOIN ${Table.TABLE_NAME} t on orders.table_id = t.id
                        WHERE orders.id = LAST_INSERT_ID()
                        """)
                        .execute()
                        .toMono()
                        .flatMap {
                            it.map(::mapRow).toMono()
                        }
                }
        }

    override fun updateQuantity(orderId: Int, quantity: Int) =
        template.databaseClient.inConnection { connection ->
            connection.createStatement("UPDATE ${Order.TABLE_NAME} SET quantity = ? WHERE id = ?")
                .bind(0, quantity)
                .bind(1, orderId)
                .execute()
                .toMono()
                .flatMap {
                    connection.createStatement("""
                        SELECT $SELECT_JOIN_COLUMNS
                        FROM ${Order.TABLE_NAME}
                        JOIN ${Menu.TABLE_NAME} m on orders.menu_id = m.id
                        JOIN ${Table.TABLE_NAME} t on orders.table_id = t.id
                        WHERE orders.id = ?
                        """)
                        .bind(0, orderId)
                        .execute()
                        .toMono()
                        .flatMap {
                            it.map(::mapRow).toMono()
                        }
                }
        }

    override fun deleteById(orderId: Int) =
        template.databaseClient.inConnection { connection ->
            connection.createStatement("DELETE FROM ${Order.TABLE_NAME} WHERE id = ?")
                .bind(0, orderId)
                .execute()
                .toMono()
                .then()
        }

    override fun deleteByTableId(tableId: Int) =
        template.databaseClient.inConnection { connection ->
            connection.createStatement("DELETE FROM ${Order.TABLE_NAME} WHERE table_id = ?")
                .bind(0, tableId)
                .execute()
                .toMono()
                .then()
        }
}
