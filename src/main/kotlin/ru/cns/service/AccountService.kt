package ru.cns.service

import mu.KLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.cns.domain.AccountEntity
import ru.cns.dto.AccountOperationRequest
import ru.cns.dto.CreateAccountRequest
import ru.cns.errors.AccountAlreadyExistsException
import ru.cns.errors.AccountNotFoundException
import ru.cns.errors.InsufficientFundsException
import ru.cns.model.AccountBalance
import ru.cns.repository.AccountRepository

@Service
class AccountService(
        val accountRepository: AccountRepository
) {

    private companion object : KLogging()

    fun get(accountNumber: String) =
            accountRepository.findOneByAccount(accountNumber)?.let { AccountBalance.fromEntity(it) }
                    ?: throw AccountNotFoundException(accountNumber)

    fun create(createRequest: CreateAccountRequest): AccountBalance {
        logger.info("Creating account '{}'", createRequest.accountNumber)
        try {
            return AccountBalance.fromEntity(
                    accountRepository.save(
                            AccountEntity(account = createRequest.accountNumber)
                    )
            )
        } catch (e: DataIntegrityViolationException) {
            throw AccountAlreadyExistsException(createRequest.accountNumber)
        }
    }

    @Transactional
    fun withdrawal(withdrawalRequest: AccountOperationRequest): AccountBalance {
        logger.info("Withdrawal {} on account '{}'",
                withdrawalRequest.amount, withdrawalRequest.accountNumber)

        val accountEntity = accountRepository.findOneAndLockByAccount(withdrawalRequest.accountNumber)
                ?: throw AccountNotFoundException(withdrawalRequest.accountNumber)

        if (accountEntity.balance < withdrawalRequest.amount) {
            throw InsufficientFundsException(withdrawalRequest.accountNumber)
        }

        val newBalance = accountEntity.balance - withdrawalRequest.amount

        return AccountBalance.fromEntity(
                accountRepository.save(
                        accountEntity.copy(balance = newBalance)
                )
        )
    }

    @Transactional
    fun deposit(depositRequest: AccountOperationRequest): AccountBalance {
        logger.info("Deposit {} to account '{}'", depositRequest.amount,
                depositRequest.accountNumber)

        val accountEntity = accountRepository.findOneAndLockByAccount(depositRequest.accountNumber)
                ?: throw AccountNotFoundException(depositRequest.accountNumber)

        val newBalance = accountEntity.balance + depositRequest.amount

        return AccountBalance.fromEntity(
                accountRepository.save(
                        accountEntity.copy(balance = newBalance)
                )
        )
    }
}
