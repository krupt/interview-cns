package ru.cns.service

import org.springframework.stereotype.Service
import ru.cns.dto.AccountOperationRequest
import ru.cns.model.AccountBalance
import ru.cns.repository.AccountRepository

@Service
class AccountService(
        val accountRepository: AccountRepository
) {

    fun withdrawal(withdrawalRequest: AccountOperationRequest) : AccountBalance {
        return AccountBalance("", 0.0)
    }

    fun deposit(depositRequest: AccountOperationRequest) : AccountBalance {
        return AccountBalance("", 0.0)
    }
}
