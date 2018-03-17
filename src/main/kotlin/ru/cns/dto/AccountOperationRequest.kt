package ru.cns.dto

import ru.cns.validation.ValidAccountNumber
import ru.cns.validation.ValidAmount

data class AccountOperationRequest(
        @field:ValidAccountNumber
        val accountNumber: String,
        @field:ValidAmount
        val amount: Double
)
