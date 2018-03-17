package ru.cns.validation

import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.constraints.DecimalMin
import kotlin.reflect.KClass

@DecimalMin("0", inclusive = false)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [])
@MustBeDocumented
annotation class ValidAmount(
        val message: String = "Invalid operation amount",
        val groups: Array<KClass<*>> = [],
        val payload: Array<KClass<out Payload>> = []
)
