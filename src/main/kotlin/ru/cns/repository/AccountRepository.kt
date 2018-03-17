package ru.cns.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import ru.cns.domain.AccountEntity
import javax.persistence.LockModeType

interface AccountRepository : JpaRepository<AccountEntity, Long> {

    fun findOneByAccount(accountNumber: String): AccountEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findOneAndLockByAccount(accountNumber: String): AccountEntity?
}
