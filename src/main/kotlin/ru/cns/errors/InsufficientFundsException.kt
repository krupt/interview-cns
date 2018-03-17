package ru.cns.errors

class InsufficientFundsException(accountNumber: String) : BaseSimpleMessageException(
        "Insufficient funds on account '$accountNumber'")
