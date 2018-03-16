package ru.cns.domain

import javax.persistence.*

@Entity
data class AccountEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long?,
        @Column(length = 20, nullable = false, unique = true, updatable = false)
        val account: String,
        val balance: Double = 0.0
)
