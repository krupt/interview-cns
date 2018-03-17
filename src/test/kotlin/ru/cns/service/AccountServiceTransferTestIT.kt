package ru.cns.service

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import ru.cns.TestJpaConfiguration
import ru.cns.dto.AccountOperationRequest
import ru.cns.dto.CreateAccountRequest
import ru.cns.dto.TransferOperationRequest
import ru.cns.errors.AccountNotFoundException
import ru.cns.repository.AccountRepository
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [TestJpaConfiguration::class, AccountService::class])
class AccountServiceTransferTestIT {

    @Autowired
    private lateinit var accountService: AccountService

    @Autowired
    private lateinit var accountRepository: AccountRepository

    private val sourceAccountNumber = "40817840291234598765"
    private val targetAccountNumber = "40817978001234565432"

    @Before
    fun setUp() {
        accountRepository.findOneByAccount(sourceAccountNumber)
                ?.let { accountRepository.delete(it) }

        accountService.create(
                CreateAccountRequest(sourceAccountNumber)
        )
        accountService.deposit(
                AccountOperationRequest(sourceAccountNumber, 500.23)
        )

        accountRepository.findOneByAccount(targetAccountNumber)
                ?.let { accountRepository.delete(it) }

        accountService.create(
                CreateAccountRequest(targetAccountNumber)
        )
        accountService.deposit(
                AccountOperationRequest(targetAccountNumber, 17273.57)
        )
    }

    @Test
    fun testTransferToNonExistingAccountFailed() {
        try {
            accountService.transfer(
                    TransferOperationRequest(
                            sourceAccountNumber, "323344324423", 432.31
                    )
            )
            fail("AccountNotFoundException must be thrown")
        } catch (e: AccountNotFoundException) {
            assertEquals("Account '323344324423' not found", e.message)

            // Check that source account balance didn't change
            val (_, sourceAccountBalance) = accountService.get(sourceAccountNumber)
            assertEquals(500.23, sourceAccountBalance, 0.001)
        }
    }

    @Test
    fun testSuccessfulTransfer() {
        accountService.transfer(
                TransferOperationRequest(
                        sourceAccountNumber, targetAccountNumber, 447.24
                )
        )

        val (_, sourceAccountBalance) = accountService.get(sourceAccountNumber)
        assertEquals(52.99, sourceAccountBalance, 0.001)

        val (_, targetAccountBalance) = accountService.get(targetAccountNumber)
        assertEquals(17720.81, targetAccountBalance, 0.001)
    }

    /**
     * Oracle and Postgres have dead-lock detection
     */
    @Test
    fun testTransferDeadlock() {
        val service = Executors.newFixedThreadPool(2)

        service.submit({
            accountService.transfer(
                    TransferOperationRequest(sourceAccountNumber, targetAccountNumber, 432.12)
            )
        })

        service.submit({
            accountService.transfer(
                    TransferOperationRequest(targetAccountNumber, sourceAccountNumber, 326.87)
            )
        })

        service.shutdown()
        service.awaitTermination(1, TimeUnit.MINUTES)

        TODO("Add some checks")
    }
}
