package ru.cns.errors

class TransferFromSameAccountsToEachOtherException(
        sourceAccountNumber: String,
        targetAccountNumber: String) : BaseSimpleMessageException(
        "A transfer from the same accounts ('$sourceAccountNumber', '$targetAccountNumber')" +
                " to each other has been detected. Please try again later")
