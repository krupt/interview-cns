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
import ru.cns.domain.AccountEntity
import ru.cns.dto.TransferOperationRequest
import ru.cns.errors.AccountNotFoundException
import ru.cns.errors.TransferFromSameAccountsToEachOtherException
import ru.cns.repository.AccountRepository
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [TestJpaConfiguration::class, AccountService::class,
    TransactionService::class])
class AccountServiceTransferTestIT {

    @Autowired
    private lateinit var accountService: AccountService

    @Autowired
    private lateinit var accountRepository: AccountRepository

    private val sourceAccountNumber = "40817840291234598765"
    private val targetAccountNumber = "40817978001234565432"

    @Before
    fun setUp() {
        accountRepository.save(
                accountRepository.findOneByAccount(sourceAccountNumber)?.copy(balance = 500.23)
                        ?: AccountEntity(account = sourceAccountNumber, balance = 500.23)
        )

        accountRepository.save(
                accountRepository.findOneByAccount(targetAccountNumber)?.copy(balance = 17273.57)
                        ?: AccountEntity(account = targetAccountNumber, balance = 17273.57)
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
            val (_, _, sourceAccountBalance) = accountService.get(sourceAccountNumber)
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

        val (_, _, sourceAccountBalance) = accountService.get(sourceAccountNumber)
        assertEquals(52.99, sourceAccountBalance, 0.001)

        val (_, _, targetAccountBalance) = accountService.get(targetAccountNumber)
        assertEquals(17720.81, targetAccountBalance, 0.001)
    }

    @Test
    fun testSuccessfulParallelTransfer() {
        val service = Executors.newFixedThreadPool(2)

        service.submit({
            accountService.transfer(
                    TransferOperationRequest(
                            sourceAccountNumber, targetAccountNumber, 147.24
                    )
            )
        })

        service.submit({
            accountService.transfer(
                    TransferOperationRequest(
                            sourceAccountNumber, targetAccountNumber, 341.47
                    )
            )
        })

        service.shutdown()
        service.awaitTermination(5, TimeUnit.SECONDS)

        val (_, _, sourceAccountBalance) = accountService.get(sourceAccountNumber)
        assertEquals(11.52, sourceAccountBalance, 0.001)

        val (_, _, targetAccountBalance) = accountService.get(targetAccountNumber)
        assertEquals(17762.28, targetAccountBalance, 0.001)
    }

    /**
     * It's very complicated test that check correct cases parallel 'transfer' with potential
     * dead-lock.
     * Case 1: first transfer failed, but second transfer successful
     * Case 2: second transfer failed, but first transfer successful
     * Case 3: both transfers failed
     * Case 4: both transfers successfully completed
     */
    @Test
    fun testTransferDeadlock() {
        val service = Executors.newFixedThreadPool(2)

        val firstTransferAmount = 432.12
        val secondTransferAmount = 326.87

        val firstTransferFailed = AtomicBoolean(false)
        val secondTransferFailed = AtomicBoolean(false)

        service.submit({
            try {
                accountService.transfer(
                        TransferOperationRequest(sourceAccountNumber, targetAccountNumber,
                                firstTransferAmount)
                )
            } catch (e: TransferFromSameAccountsToEachOtherException) {
                println("!!! Source -> Target failed")
                firstTransferFailed.set(true)
            }
        })

        service.submit({
            try {
                accountService.transfer(
                        TransferOperationRequest(targetAccountNumber, sourceAccountNumber,
                                secondTransferAmount)
                )
            } catch (e: TransferFromSameAccountsToEachOtherException) {
                println("!!! Target -> Source failed")
                secondTransferFailed.set(true)
            }
        })

        service.shutdown()
        service.awaitTermination(10, TimeUnit.SECONDS)

        var sourceAccountTransferAmount = 0.0
        var targetAccountTransferAmount = 0.0

        if (!firstTransferFailed.get()) {
            sourceAccountTransferAmount += -firstTransferAmount
            targetAccountTransferAmount += firstTransferAmount
        }

        if (!secondTransferFailed.get()) {
            sourceAccountTransferAmount += secondTransferAmount
            targetAccountTransferAmount += -secondTransferAmount
        }

        val (_, _, sourceAccountBalance) = accountService.get(sourceAccountNumber)
        assertEquals(500.23 + sourceAccountTransferAmount,
                sourceAccountBalance, 0.001)

        val (_, _, targetAccountBalance) = accountService.get(targetAccountNumber)
        assertEquals(17273.57 + targetAccountTransferAmount,
                targetAccountBalance, 0.001)
    }
}
