package ru.cns.resource

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.cns.dto.AccountOperationRequest
import ru.cns.dto.CreateAccountRequest
import ru.cns.dto.TransferOperationRequest
import ru.cns.service.AccountService
import javax.validation.Valid

@RestController
@RequestMapping("api/account")
class AccountResource(
        val accountService: AccountService
) {

    @PostMapping("create")
    fun createNewAccount(@Valid @RequestBody createRequest: CreateAccountRequest) =
            accountService.create(createRequest)

    @GetMapping("{accountNumber}")
    fun getAccountInfo(@PathVariable("accountNumber")
                       accountNumber: String) =
            accountService.get(accountNumber)

    @PostMapping("withdrawal")
    fun withdrawal(@Valid @RequestBody withdrawalRequest: AccountOperationRequest) =
            accountService.withdrawal(withdrawalRequest)

    @PostMapping("deposit")
    fun deposit(@Valid @RequestBody depositRequest: AccountOperationRequest) =
            accountService.deposit(depositRequest)

    @PostMapping("transfer")
    fun transfer(@Valid
                 @RequestBody
                 transferOperationRequest: TransferOperationRequest): ResponseEntity<String> {
        accountService.transfer(transferOperationRequest)
        return ResponseEntity.ok("Transfer successfully completed")
    }
}
