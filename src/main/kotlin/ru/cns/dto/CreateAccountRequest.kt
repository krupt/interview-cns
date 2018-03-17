package ru.cns.dto

import ru.cns.validation.ValidAccountNumber

data class CreateAccountRequest(
        @field:ValidAccountNumber
        val accountNumber: String
)
