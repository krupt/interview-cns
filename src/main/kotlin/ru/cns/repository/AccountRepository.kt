package ru.cns.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.cns.domain.AccountEntity

interface AccountRepository : JpaRepository<AccountEntity, Long> {

    fun findOneByAccount(accountNumber: String) : AccountEntity?
}
