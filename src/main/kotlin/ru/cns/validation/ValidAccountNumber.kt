package ru.cns.validation

import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.ReportAsSingleViolation
import javax.validation.constraints.Pattern
import kotlin.reflect.KClass

@Pattern(regexp = "\\d{20}")
@ReportAsSingleViolation
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [])
@MustBeDocumented
annotation class ValidAccountNumber(
        val message: String = "Account number must consist of only 20 digits",
        val groups: Array<KClass<*>> = [],
        val payload: Array<KClass<out Payload>> = []
)
