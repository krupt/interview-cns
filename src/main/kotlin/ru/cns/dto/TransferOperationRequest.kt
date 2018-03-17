package ru.cns.dto

import ru.cns.validation.ValidAccountNumber
import ru.cns.validation.ValidAmount

data class TransferOperationRequest(
        @field:ValidAccountNumber
        val sourceAccountNumber: String,
        @field:ValidAccountNumber
        val targetAccountNumber: String,
        @field:ValidAmount
        val amount: Double
)
