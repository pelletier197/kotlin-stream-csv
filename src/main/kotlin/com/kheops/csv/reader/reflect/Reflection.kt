package com.kheops.csv.reader.reflect

import com.kheops.csv.CsvProperty
import com.kheops.csv.reader.CsvErrorType
import com.kheops.csv.reader.Test
import com.kheops.csv.reader.reflect.converters.ConversionFailedException
import com.kheops.csv.reader.reflect.converters.NoConverterFoundException
import com.kheops.csv.reader.reflect.converters.convertForField
import java.lang.Exception
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.kotlinProperty

class InvalidTargetClass(
    target: Class<*>,
    fields: Collection<InstantiationField>
) : Exception(
    """
    Invalid recipient class for '${target.name}'. Recipient class is expected to have a public constructor for all the public field of the target class and no other parameters.
    Expected constructor:
    ${target.name} (
${fields.joinToString(separator = "\n") { "         ${it.name}: ${it.field.genericType.typeName}" }}
    )
"""
)

data class InstantiationError(
    val field: String,
    val originalField: String,
    val type: InstantiationErrorType,
    val cause: Throwable?
)

enum class InstantiationErrorType {
    NON_NULLABLE_FIELD_IS_NULL,
    NO_CONVERTER_FOUND_FOR_VALUE,
    CONVERSION_OF_FIELD_FAILED
}

data class InstantiationWithErrors<T>(
    val result: T?,
    val errors: List<InstantiationError>
)

class InstantiationField(
    val field: Field,
    val property: KProperty<*>?,
) {
    val name: String get() = this.field.name
    val isNullable: Boolean get() = property?.returnType?.isMarkedNullable ?: true
}

class InstantiationArgument(
    val field: InstantiationField,
    val value: String?,
    val originalTargetName: String
)

fun <T> createInstance(target: Class<T>, arguments: List<InstantiationArgument>): InstantiationWithErrors<T> {
    val constructor = getTargetConstructor<T>(target, arguments.map { it.field })
    val errors = ArrayList<InstantiationError>()
    val args = buildConstructorArguments(constructor, arguments, errors)

    if (errors.isNotEmpty()) {
        return InstantiationWithErrors(
            result = null,
            errors = errors
        )
    }

    return InstantiationWithErrors(
        result = constructor.newInstance(args),
        errors = errors
    )
}

private fun <T> buildConstructorArguments(
    constructor: Constructor<T>,
    arguments: List<InstantiationArgument>,
    errors: MutableList<InstantiationError>
): Array<Any?> {
    val instantiationFieldByName = arguments.map { it.field.name to it }.toMap()
    val names = constructor.parameters.map { it.name }
    return names.map(fun(it: String): Any? {
        val instField = instantiationFieldByName[it] ?: error("no field found with name '${it}'")
        val fieldValue = instField.value

        if (fieldValue == null && !instField.field.isNullable) {
            errors.add(createError(instField, InstantiationErrorType.NON_NULLABLE_FIELD_IS_NULL))
            return null
        }

        try {
            return fieldValue?.let { convertForField<String, Any?>(fieldValue, instField.field.field) }
        } catch (e: NoConverterFoundException) {
            errors.add(createError(instField, InstantiationErrorType.NO_CONVERTER_FOUND_FOR_VALUE, e))
        } catch (e: ConversionFailedException) {
            errors.add(createError(instField, InstantiationErrorType.CONVERSION_OF_FIELD_FAILED, e))
        }
        return null
    }).toTypedArray()
}

private fun createError(
    argument: InstantiationArgument,
    type: InstantiationErrorType,
    ex: Exception? = null
): InstantiationError {
    return InstantiationError(
        field = argument.field.name,
        originalField = argument.originalTargetName,
        type = type,
        cause = ex
    )
}

@Suppress("UNCHECKED_CAST")
private fun <T> getTargetConstructor(
    target: Class<*>,
    fields: Collection<InstantiationField>
): Constructor<T> {
    val fieldNames = fields.map { it.name }
    return try {
        target.kotlin.constructors.find { constructor ->
            constructor.parameters.map { it.name }.containsAll(fieldNames)
        }?.javaConstructor as Constructor<T>? ?: throw InvalidTargetClass(target, fields)
    } catch (ex: Exception) {
        target.constructors.find { constructor ->
            constructor.parameters.map { it.name }.containsAll(fieldNames)
        } as Constructor<T>? ?: throw InvalidTargetClass(target, fields)
    }
}
