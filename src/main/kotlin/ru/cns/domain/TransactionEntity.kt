package ru.cns.domain

import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime
import javax.persistence.*

@Entity
data class TransactionEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE,
                generator = "TransactionEntityIdSequence")
        @SequenceGenerator(name = "TransactionEntityIdSequence")
        val id: Long? = null,
        val debit: AccountEntity,
        val credit: AccountEntity,
        val amount: Double
) {
    @CreatedDate
    lateinit var timestamp: LocalDateTime
}
