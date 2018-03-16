package ru.cns.errors

class AccountNotFound(accountNumber: String) : RuntimeException(
        "Account '$accountNumber' not found")
