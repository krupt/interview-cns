package ru.cns.service

import org.springframework.stereotype.Service
import ru.cns.domain.TransactionEntity
import ru.cns.repository.AccountRepository
import ru.cns.repository.TransactionRepository

@Service
class TransactionService(
        val transactionRepository: TransactionRepository,
        val accountRepository: AccountRepository
) {

    fun create(sourceAccountId: Long, targetAccountId: Long, amount: Double) {
        transactionRepository.save(
                TransactionEntity(
                        debit = accountRepository.getOne(sourceAccountId),
                        credit = accountRepository.getOne(targetAccountId),
                        amount = amount
                )
        )
    }
}
