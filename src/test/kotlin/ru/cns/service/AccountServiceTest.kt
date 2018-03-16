package ru.cns.service

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.eq
import org.mockito.BDDMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner
import ru.cns.domain.AccountEntity
import ru.cns.dto.AccountOperationRequest
import ru.cns.errors.AccountNotFound
import ru.cns.errors.InsufficientFunds
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
    }

    @Test(expected = AccountNotFound::class)
    fun testWithdrawalOnNonExistingAccount() {
        accountService.withdrawal(
                AccountOperationRequest("1231231233232", 1.0)
        )
    }

    @Test(expected = AccountNotFound::class)
    fun testDepositOnNonExistingAccount() {
        accountService.deposit(
                AccountOperationRequest("123123123123", 2.0)
        )
    }

    @Test(expected = InsufficientFunds::class)
    fun testWithdrawalOnAccountWithInsufficientFunds() {
        accountService.withdrawal(
                AccountOperationRequest("40817810401234567890", 11.11)
        )
    }

    @Test
    fun testSuccessfulWithdrawal() {
        val accountBalance = accountService.withdrawal(
                AccountOperationRequest("40817810401234567890", 5.0)
        )
        assertEquals("40817810401234567890", accountBalance.accountNumber)
        assertEquals(6.1, accountBalance.balance, 0.0)

        BDDMockito.verify(accountRepository.save(eq(testAccountEntity.copy(balance = 6.1))))
    }

    @Test
    fun testSuccessfulDeposit() {
        val accountBalance = accountService.deposit(
                AccountOperationRequest("40817810401234567890", 11.1)
        )
        assertEquals("40817810401234567890", accountBalance.accountNumber)
        assertEquals(22.21, accountBalance.balance, 0.0)

        BDDMockito.verify(accountRepository.save(eq(testAccountEntity.copy(balance = 22.21))))
    }
}
