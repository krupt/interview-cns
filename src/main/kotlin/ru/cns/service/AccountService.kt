package ru.cns.service

import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.PessimisticLockingFailureException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.cns.domain.AccountEntity
import ru.cns.dto.AccountOperationRequest
import ru.cns.dto.CreateAccountRequest
import ru.cns.dto.TransferOperationRequest
import ru.cns.errors.*
import ru.cns.model.Account
import ru.cns.repository.AccountRepository

@Service
class AccountService(
        val accountRepository: AccountRepository,
        val transactionService: TransactionService
) {

    private companion object : KLogging()

    @Autowired
    private lateinit var self: AccountService

    fun get(accountNumber: String) =
            accountRepository.findOneByAccount(accountNumber)?.let { Account.fromEntity(it) }
                    ?: throw AccountNotFoundException(accountNumber)

    fun create(createRequest: CreateAccountRequest): Account {
        logger.info("Creating account '{}'", createRequest.accountNumber)
        try {
            return Account.fromEntity(
                    accountRepository.save(
                            AccountEntity(account = createRequest.accountNumber)
                    )
            )
        } catch (e: DataIntegrityViolationException) {
            throw AccountAlreadyExistsException(createRequest.accountNumber)
        }
    }

    @Transactional
    fun withdrawal(withdrawalRequest: AccountOperationRequest,
                   needCreateTransaction: Boolean = true): Account {
        logger.info("Withdrawal {} on account '{}'",
                withdrawalRequest.amount, withdrawalRequest.accountNumber)

        val accountEntity = accountRepository.findOneAndLockByAccount(withdrawalRequest.accountNumber)
                ?: throw AccountNotFoundException(withdrawalRequest.accountNumber)

        if (accountEntity.balance < withdrawalRequest.amount) {
            throw InsufficientFundsException(withdrawalRequest.accountNumber)
        }

        val newBalance = accountEntity.balance - withdrawalRequest.amount

        if (needCreateTransaction) {
            createTransaction(accountEntity.id, AccountEntity.systemAccountEntity.id,
                    withdrawalRequest.amount)
        }

        return Account.fromEntity(
                accountRepository.save(
                        accountEntity.copy(balance = newBalance)
                )
        )
    }

    @Transactional
    fun deposit(depositRequest: AccountOperationRequest,
                needCreateTransaction: Boolean = true): Account {
        logger.info("Deposit {} to account '{}'", depositRequest.amount,
                depositRequest.accountNumber)

        val accountEntity = accountRepository.findOneAndLockByAccount(depositRequest.accountNumber)
                ?: throw AccountNotFoundException(depositRequest.accountNumber)

        val newBalance = accountEntity.balance + depositRequest.amount

        if (needCreateTransaction) {
            createTransaction(AccountEntity.systemAccountEntity.id, accountEntity.id,
                    depositRequest.amount)
        }

        return Account.fromEntity(
                accountRepository.save(
                        accountEntity.copy(balance = newBalance)
                )
        )
    }

    @Transactional
    fun transfer(transferOperationRequest: TransferOperationRequest) {
        logger.info("Transfer {} from '{}' to '{}'", transferOperationRequest.amount,
                transferOperationRequest.sourceAccountNumber,
                transferOperationRequest.targetAccountNumber)

        if (transferOperationRequest.sourceAccountNumber == transferOperationRequest
                        .targetAccountNumber) {
            throw SelfTransferNotAllowedException()
        }

        try {
            val (sourceAccountId, _, _) = self.withdrawal(
                    AccountOperationRequest(transferOperationRequest.sourceAccountNumber,
                            transferOperationRequest.amount),
                    false
            )

            val (targetAccountId, _, _) = self.deposit(
                    AccountOperationRequest(transferOperationRequest.targetAccountNumber,
                            transferOperationRequest.amount),
                    false
            )

            createTransaction(sourceAccountId, targetAccountId, transferOperationRequest.amount)
        } catch (e: PessimisticLockingFailureException) {
            throw TransferFromSameAccountsToEachOtherException(
                    transferOperationRequest.sourceAccountNumber,
                    transferOperationRequest.targetAccountNumber)
        }
    }

    private fun createTransaction(sourceAccountId: Long?, targetAccountId: Long?, amount: Double) {
        transactionService.create(sourceAccountId!!, targetAccountId!!, amount)
    }
}
