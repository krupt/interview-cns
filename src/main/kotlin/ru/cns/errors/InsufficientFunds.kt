package ru.cns.errors

class InsufficientFunds(accountNumber: String) : RuntimeException(
        "Insufficient funds on account '$accountNumber'")
