package ru.cns.service

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import ru.cns.TestJpaConfiguration
import ru.cns.dto.CreateAccountRequest
import ru.cns.errors.AccountAlreadyExistsException

@RunWith(SpringRunner::class)
@DataJpaTest
@SpringBootTest(classes = [TestJpaConfiguration::class, AccountService::class])
class AccountServiceJpaTest {

    @Autowired
    private lateinit var accountService: AccountService

    @Test
    fun testSuccessfulCreate() {
        accountService.create(
                CreateAccountRequest("40817810505432112345")
        )
        val accountBalance = accountService.get("40817810505432112345")
        Assert.assertEquals("40817810505432112345", accountBalance.accountNumber)
        Assert.assertEquals(0.0, accountBalance.balance, 0.0)
    }

    @Test
    fun testCreateAlreadyExistingAccount() {
        accountService.create(
                CreateAccountRequest("4081781079876512345")
        )
        try {
            accountService.create(
                    CreateAccountRequest("4081781079876512345")
            )
            fail("AccountAlreadyExistsException must be thrown")
        } catch (e: AccountAlreadyExistsException) {
            assertEquals("Account '4081781079876512345' already exists", e.message)
        }
    }
}
