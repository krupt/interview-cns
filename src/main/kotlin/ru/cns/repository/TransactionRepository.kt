package ru.cns.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.cns.domain.TransactionEntity

interface TransactionRepository : JpaRepository<TransactionEntity, Long>
