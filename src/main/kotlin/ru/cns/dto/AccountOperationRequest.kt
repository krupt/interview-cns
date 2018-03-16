package ru.cns.dto

import ru.cns.validation.ValidAccountNumber
import ru.cns.validation.ValidAmount

data class AccountOperationRequest(
        @ValidAccountNumber
        val accountNumber: String,
        @ValidAmount
        val amount: Double
)
