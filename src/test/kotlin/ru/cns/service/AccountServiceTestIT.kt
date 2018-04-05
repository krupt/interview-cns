package ru.cns.service

import com.vladmihalcea.sql.SQLStatementCountValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import ru.cns.TestJpaConfiguration
import ru.cns.domain.AccountEntity
import ru.cns.dto.AccountOperationRequest
import ru.cns.dto.CreateAccountRequest
import ru.cns.errors.AccountAlreadyExistsException
import ru.cns.errors.InsufficientFundsException
import ru.cns.repository.AccountRepository
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [TestJpaConfiguration::class, AccountService::class,
    TransactionService::class])
class AccountServiceTestIT {

    @Autowired
    private lateinit var accountService: AccountService

    @Autowired
    private lateinit var accountRepository: AccountRepository

    private val accountNumber = "40817840291234598765"

    @Before
    fun setUp() {
        accountRepository.save(
                accountRepository.findOneByAccount(accountNumber)?.copy(balance = 500.23)
                        ?: AccountEntity(account = accountNumber, balance = 500.23)
        )

        SQLStatementCountValidator.reset()
    }

    @Test(expected = AccountAlreadyExistsException::class)
    fun testCreateAlreadyExistingAccount() {
        accountService.create(
                CreateAccountRequest(accountNumber)
        )
    }

    @Test
    fun testThatWithdrawalDoingInOneTransaction() {
        accountService.withdrawal(
                AccountOperationRequest(accountNumber, 156.0)
        )

        // Select account
        SQLStatementCountValidator.assertSelectCount(1)
        // Update account
        SQLStatementCountValidator.assertUpdateCount(1)
        // Insert transaction
        SQLStatementCountValidator.assertInsertCount(1)
    }

    @Test
    fun testThatDepositDoingInOneTransaction() {
        accountService.deposit(
                AccountOperationRequest(accountNumber, 15.0)
        )

        // Select account
        SQLStatementCountValidator.assertSelectCount(1)
        // Update account
        SQLStatementCountValidator.assertUpdateCount(1)
        // Insert transaction
        SQLStatementCountValidator.assertInsertCount(1)
    }

    @Test
    fun testParallelWithdrawalFailed() {
        val service = Executors.newFixedThreadPool(2)

        val exception = AtomicReference<InsufficientFundsException?>()

        for (i in 1..2) {
            service.submit({
                try {
                    accountService.withdrawal(
                            AccountOperationRequest(accountNumber, 251.0)
                    )
                } catch (e: InsufficientFundsException) {
                    exception.set(e)
                }
            })
        }

        service.shutdown()
        service.awaitTermination(5, TimeUnit.SECONDS)

        val accountBalance = accountService.get(accountNumber)
        assertEquals(249.23, accountBalance.balance, 0.001)
        assertNotNull("InsufficientFundsException must be thrown", exception.get())
    }

    @Test
    fun tesSuccessfulParallelWithdrawal() {
        val service = Executors.newFixedThreadPool(2)

        for (i in 1..2) {
            service.submit({
                accountService.withdrawal(
                        AccountOperationRequest(accountNumber, 249.0)
                )
            })
        }

        service.shutdown()
        service.awaitTermination(5, TimeUnit.SECONDS)

        val accountBalance = accountService.get(accountNumber)
        assertEquals(2.23, accountBalance.balance, 0.001)
    }

    @Test
    fun testSuccessfulParallelDeposit() {
        val service = Executors.newFixedThreadPool(2)

        for (i in 1..2) {
            service.submit({
                accountService.deposit(
                        AccountOperationRequest(accountNumber, 256.64)
                )
            })
        }

        service.shutdown()
        service.awaitTermination(5, TimeUnit.SECONDS)

        val accountBalance = accountService.get(accountNumber)
        assertEquals(1013.51, accountBalance.balance, 0.001)
    }

    @Test
    fun testSuccessfulParallelWithdrawalAndDeposit() {
        val service = Executors.newFixedThreadPool(2)

        service.submit({
            accountService.withdrawal(
                    AccountOperationRequest(accountNumber, 249.0)
            )
        })

        service.submit({
            accountService.deposit(
                    AccountOperationRequest(accountNumber, 256.64)
            )
        })

        service.shutdown()
        service.awaitTermination(5, TimeUnit.SECONDS)

        val accountBalance = accountService.get(accountNumber)
        assertEquals(507.87, accountBalance.balance, 0.001)
    }
}
