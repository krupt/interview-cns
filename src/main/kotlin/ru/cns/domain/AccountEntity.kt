package ru.cns.domain

import javax.persistence.*

@Entity
open class AccountEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Column(length = 20, nullable = false, unique = true, updatable = false)
        val account: String,
        val balance: Double = 0.0
) {
    companion object {
        val systemAccountEntity = AccountEntity(0, "00000000000000000000")
    }

    fun copy(balance: Double) =
            AccountEntity(id, account, balance)
}
