package ru.cns.domain

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@EntityListeners(AuditingEntityListener::class)
data class TransactionEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE,
                generator = "TransactionEntityIdGenerator")
        @SequenceGenerator(name = "TransactionEntityIdGenerator",
                sequenceName = "TransactionEntityIdSequence")
        val id: Long? = null,
        @ManyToOne(fetch = FetchType.LAZY)
        val debit: AccountEntity,
        @ManyToOne(fetch = FetchType.LAZY)
        val credit: AccountEntity,
        val amount: Double
) {
    @CreatedDate
    lateinit var timestamp: LocalDateTime
}
