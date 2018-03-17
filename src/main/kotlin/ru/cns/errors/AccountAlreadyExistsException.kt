package ru.cns.errors

class AccountAlreadyExistsException(accountNumber: String) : BaseSimpleMessageException(
        "Account '$accountNumber' already exists")
