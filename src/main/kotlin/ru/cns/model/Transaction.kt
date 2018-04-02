package ru.cns.model

import ru.cns.domain.TransactionEntity
import java.time.LocalDateTime

data class Transaction(
        val id: Long,
        val sourceAccount: String,
        val targetAccount: String,
        val amount: Double,
        val timestamp: LocalDateTime
) {

    companion object Mapper {
        fun fromEntity(transactionEntity: TransactionEntity) =
                Transaction(transactionEntity.id ?: 0,
                        transactionEntity.debit.account,
                        transactionEntity.credit.account,
                        transactionEntity.amount,
                        transactionEntity.timestamp)
    }
}
