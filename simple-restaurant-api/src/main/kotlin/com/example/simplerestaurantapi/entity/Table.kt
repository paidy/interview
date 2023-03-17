package com.example.simplerestaurantapi.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table(com.example.simplerestaurantapi.entity.Table.TABLE_NAME)
data class Table (
    @Id
    var id: Int = 0,
    @Column("name")
    var name: String = "",
    @Column("capacity")
    var capacity: Int = 0,
    @CreatedDate
    @Column("created_at")
    var createdAt: LocalDateTime = LocalDateTime.now(),
    @LastModifiedDate
    @Column("updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        const val TABLE_NAME = "tables"
    }
}
