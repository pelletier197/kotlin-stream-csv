package com.kheops.csv.reader.reflect

import com.kheops.csv.CsvProperty
import com.kheops.csv.reader.Test
import java.lang.reflect.Field
import java.util.*
import kotlin.collections.HashMap
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.kotlinProperty

data class InstantiationError(
    val field: String,
    val type: InstantiationErrorType
)

enum class InstantiationErrorType {
    MISSING_FIELD_VALUE
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

fun <T> createInstance(target: Class<T>, arguments: Map<InstantiationField, String?>): InstantiationWithErrors<T> {
    TODO()
}

