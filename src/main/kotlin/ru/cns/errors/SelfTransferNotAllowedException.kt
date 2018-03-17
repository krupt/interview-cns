package ru.cns.errors

class SelfTransferNotAllowedException : BaseSimpleMessageException(
        "Source account and target account must be different")
