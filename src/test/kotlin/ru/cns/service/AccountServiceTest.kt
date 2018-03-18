package ru.cns.service

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner
import ru.cns.domain.AccountEntity
import ru.cns.dto.AccountOperationRequest
import ru.cns.dto.TransferOperationRequest
import ru.cns.errors.AccountNotFoundException
import ru.cns.errors.InsufficientFundsException
import ru.cns.errors.SelfTransferNotAllowedException
import ru.cns.repository.AccountRepository

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [(AccountService::class)])
class AccountServiceTest {

    @Autowired
    private lateinit var accountService: AccountService

    @MockBean
    private lateinit var accountRepository: AccountRepository

    private val testAccountEntity = AccountEntity(123, "40817810401234567890", 11.1)

    @Before
    fun setUp() {
        BDDMockito.given(accountRepository.findOneByAccount("40817810401234567890"))
                .willReturn(testAccountEntity)
        BDDMockito.given(accountRepository.findOneAndLockByAccount("40817810401234567890"))
                .willReturn(testAccountEntity)

        BDDMockito.given(accountRepository.save(any(AccountEntity::class.java)))
                .will { it.arguments[0] }
    }

    @Test(expected = AccountNotFoundException::class)
    fun testGetNonExistingAccount() {
        accountService.get("2312312312312312312")
    }

    @Test(expected = AccountNotFoundException::class)
    fun testWithdrawalOnNonExistingAccount() {
        accountService.withdrawal(
                AccountOperationRequest("1231231233232", 1.0)
        )
    }

    @Test(expected = AccountNotFoundException::class)
    fun testDepositOnNonExistingAccount() {
        accountService.deposit(
                AccountOperationRequest("123123123123", 2.0)
        )
    }

    @Test(expected = InsufficientFundsException::class)
    fun testWithdrawalOnAccountWithInsufficientFunds() {
        accountService.withdrawal(
                AccountOperationRequest("40817810401234567890", 11.11)
        )
    }

    @Test
    fun testSuccessfulGet() {
        val accountBalance = accountService.get("40817810401234567890")
        assertEquals("40817810401234567890", accountBalance.accountNumber)
        assertEquals(11.1, accountBalance.balance, 0.001)
    }

    @Test
    fun testSuccessfulWithdrawal() {
        val accountBalance = accountService.withdrawal(
                AccountOperationRequest("40817810401234567890", 5.0)
        )
        assertEquals("40817810401234567890", accountBalance.accountNumber)
        assertEquals(6.1, accountBalance.balance, 0.001)
    }

    @Test
    fun testSuccessfulDeposit() {
        val accountBalance = accountService.deposit(
                AccountOperationRequest("40817810401234567890", 11.11)
        )
        assertEquals("40817810401234567890", accountBalance.accountNumber)
        assertEquals(22.21, accountBalance.balance, 0.001)
    }

    @Test
    fun testSuccessfulTransfer() {
        val transferSourceAccountEntity = AccountEntity(43412, "40817978071234567123", 578.85)
        BDDMockito.given(accountRepository.findOneAndLockByAccount("40817978071234567123"))
                .willReturn(transferSourceAccountEntity)

        val savedEntities = Array<AccountEntity?>(2, { null })
        BDDMockito.given(accountRepository.save(any(AccountEntity::class.java)))
                .will {
                    val accountEntity = it.arguments[0] as AccountEntity
                    if (accountEntity.account == "40817978071234567123") {
                        savedEntities[0] = accountEntity
                    } else if (accountEntity.account == "40817810401234567890") {
                        savedEntities[1] = accountEntity
                    }
                    return@will it.arguments[0]
                }

        accountService.transfer(
                TransferOperationRequest("40817978071234567123",
                        "40817810401234567890",
                        543.21
                )
        )

        val sourceAccountEntity = savedEntities[0]!!

        assertEquals("40817978071234567123", sourceAccountEntity.account)
        assertEquals(35.64, sourceAccountEntity.balance, 0.001)

        val targetAccountEntity = savedEntities[1]!!
        assertEquals("40817810401234567890", targetAccountEntity.account)
        assertEquals(554.31, targetAccountEntity.balance, 0.001)
    }

    @Test(expected = SelfTransferNotAllowedException::class)
    fun testTransferOnSameAccountFailed() {
        accountService.transfer(
                TransferOperationRequest("40817810401234567890",
                        "40817810401234567890", 7.23)
        )
    }
}
