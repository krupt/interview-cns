package ru.cns.model

import ru.cns.domain.AccountEntity

data class AccountBalance(
        val accountNumber: String,
        val balance: Double
) {

    companion object Mapper {
        fun fromEntity(accountEntity: AccountEntity) =
                AccountBalance(
                        accountEntity.account,
                        accountEntity.balance
                )
    }
}
