package ru.cns.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import ru.cns.TestJpaConfiguration
import ru.cns.dto.AccountOperationRequest
import ru.cns.dto.CreateAccountRequest
import ru.cns.errors.InsufficientFundsException
import ru.cns.repository.AccountRepository
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [TestJpaConfiguration::class, AccountService::class])
class AccountServiceConcurrentJpaTestIT {

    @Autowired
    private lateinit var accountService: AccountService

    @Autowired
    private lateinit var accounRepository: AccountRepository

    private val accountNumber = "40817840291234598765"

    @Before
    fun setUp() {
        accounRepository.delete(
                accounRepository.findOneByAccount(accountNumber)!!
        )
        accountService.create(
                CreateAccountRequest(accountNumber)
        )
        accountService.deposit(
                AccountOperationRequest(accountNumber, 500.23)
        )
    }

    @Test
    fun testParallelWithdrawalFailed() {
        val service = Executors.newFixedThreadPool(2)

        // Using lambda hack from Java
        val exception = Array<InsufficientFundsException?>(1, { null })

        for (i in 1..2) {
            service.submit({
                try {
                    accountService.withdrawal(
                            AccountOperationRequest(accountNumber, 251.0)
                    )
                } catch (e: InsufficientFundsException) {
                    exception[0] = e
                }
            })
        }

        service.shutdown()
        service.awaitTermination(1, TimeUnit.SECONDS)

        val accountBalance = accountService.get(accountNumber)
        assertEquals(249.23, accountBalance.balance, 0.0)
        assertNotNull("InsufficientFundsException must be thrown", exception[0])
    }

    @Test
    fun tesSuccessfultParallelWithdrawal() {
        val service = Executors.newFixedThreadPool(2)

        for (i in 1..2) {
            service.submit({
                accountService.withdrawal(
                        AccountOperationRequest(accountNumber, 249.0)
                )
            })

            service.shutdown()
            service.awaitTermination(1, TimeUnit.SECONDS)

            val accountBalance = accountService.get(accountNumber)
            assertEquals(2.23, accountBalance.balance, 0.0)
        }
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
        service.awaitTermination(1, TimeUnit.SECONDS)

        val accountBalance = accountService.get(accountNumber)
        assertEquals(1013.51, accountBalance.balance, 0.0)
    }
}
