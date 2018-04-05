package ru.cns.service

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.notNull
import org.mockito.BDDMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner
import ru.cns.domain.AccountEntity
import ru.cns.domain.TransactionEntity
import ru.cns.repository.AccountRepository
import ru.cns.repository.TransactionRepository
import java.time.LocalDateTime
import java.time.Month

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [TransactionService::class])
class TransactionServiceTest {

    @Autowired
    private lateinit var transactionService: TransactionService

    @MockBean
    private lateinit var transactionRepository: TransactionRepository

    @MockBean
    private lateinit var accountRepository: AccountRepository

    @Test
    fun testCreateTransaction() {
        lateinit var transaction: TransactionEntity

        BDDMockito.given(accountRepository.getOne(anyLong()))
                .will {
                    AccountEntity(it.arguments[0] as Long,
                            (it.arguments[0] as Long).toString().padStart(20, '0'))
                }

        BDDMockito.given(transactionRepository.save(notNull<TransactionEntity>()))
                .will {
                    val transactionEntity = (it.arguments[0] as TransactionEntity).copy(1)
                    transactionEntity.timestamp = LocalDateTime.of(2018, Month.MARCH, 30,
                            18, 36, 40)
                    transaction = transactionEntity
                    transactionEntity
                }

        transactionService.create(123333, 3312, 44.31)

        assertEquals(1L, transaction.id)
        assertEquals(123333L, transaction.debit.id)
        assertEquals(3312L, transaction.credit.id)
        assertEquals(44.31, transaction.amount, 0.001)
        assertEquals(LocalDateTime.of(2018, Month.MARCH, 30,
                18, 36, 40),
                transaction.timestamp
        )
    }
}
