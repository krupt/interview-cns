package ru.cns.model

import ru.cns.domain.AccountEntity

data class Account(
        val id: Long,
        val accountNumber: String,
        val balance: Double
) {

    companion object Mapper {
        fun fromEntity(accountEntity: AccountEntity) =
                Account(
                        accountEntity.id ?: -1,
                        accountEntity.account,
                        accountEntity.balance
                )
    }
}
