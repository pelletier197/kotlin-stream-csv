package io.github.pelletier197.csv.reader.reflect

import io.github.pelletier197.csv.reader.reflect.converters.ConversionFailedException
import io.github.pelletier197.csv.reader.reflect.converters.ConversionSettings
import io.github.pelletier197.csv.reader.reflect.converters.Converter
import io.github.pelletier197.csv.reader.reflect.converters.NoConverterFoundException
import io.github.pelletier197.csv.reader.reflect.converters.TypeConverter
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KVisibility

class InvalidTargetClass(
    target: Class<*>,
    fields: Collection<InstantiationField>
) : Exception(
    """
    Invalid recipient class: '${target.name}'. Recipient class is expected to have a public constructor for all the public field of the target class and no other parameters.
    
    >> If the constructor exists and is public, it is possible that constructor's fields name are lost at compile time, which makes it impossible to find field's parameter names. With Java 8+, you can enable JVM option to conserve field name values with compile option '-parameters'. 
    
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
    val providedValue: String?,
    val cause: Exception?
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

data class InstantiationField(
    val field: Field,
    private val property: KProperty<*>?,
) {
    val name: String get() = this.field.name
    val isNullable: Boolean get() = property?.returnType?.isMarkedNullable ?: true
}

data class InstantiationArgument(
    val field: InstantiationField,
    val value: String?,
    val originalTargetName: String
)

data class InstanceCreator(
    private val typeConverter: TypeConverter = TypeConverter.getDefault()
) {
    fun withConverter(newConverter: Converter<*, *>): InstanceCreator {
        return copy(typeConverter = typeConverter.withConverter(newConverter))
    }

    fun withConverters(newConverters: List<Converter<*, *>>): InstanceCreator {
        return copy(typeConverter = typeConverter.withConverters(newConverters))
    }

    fun withClearedConverters(): InstanceCreator {
        return copy(typeConverter = typeConverter.withClearedConverters())
    }

    fun <T> createInstance(
        target: Class<T>,
        arguments: List<InstantiationArgument>,
        settings: ConversionSettings
    ): InstantiationWithErrors<T> {
        val constructor = getTargetConstructor<T>(target, arguments.map { it.field })
        val errors = ArrayList<InstantiationError>()
        val args = buildConstructorArguments(constructor, arguments, settings, errors)

        if (errors.isNotEmpty()) {
            return InstantiationWithErrors(
                result = null,
                errors = errors
            )
        }

        return InstantiationWithErrors(
            result = constructor.call(args),
            errors = errors
        )
    }

    private fun <T> buildConstructorArguments(
        constructor: GenericConstructor<T>,
        arguments: List<InstantiationArgument>,
        settings: ConversionSettings,
        errors: MutableList<InstantiationError>
    ): Array<Any?> {
        val instantiationFieldByName = arguments.map { it.field.name to it }.toMap()
        val names = constructor.parameterNames
        return names.map(
            fun(it: String): Any? {
                val instField = instantiationFieldByName[it] ?: error("no field found with name '$it'")
                val fieldValue = instField.value

                if (fieldValue == null && !instField.field.isNullable) {
                    errors.add(createError(instField, InstantiationErrorType.NON_NULLABLE_FIELD_IS_NULL))
                    return null
                }

                try {
                    return fieldValue?.let {
                        typeConverter.convertForField<String, Any?>(
                            fieldValue,
                            instField.field.field,
                            settings
                        )
                    }
                } catch (e: NoConverterFoundException) {
                    errors.add(createError(instField, InstantiationErrorType.NO_CONVERTER_FOUND_FOR_VALUE, e))
                } catch (e: ConversionFailedException) {
                    errors.add(createError(instField, InstantiationErrorType.CONVERSION_OF_FIELD_FAILED, e))
                }
                return null
            }
        ).toTypedArray()
    }

    private fun createError(
        argument: InstantiationArgument,
        type: InstantiationErrorType,
        ex: Exception? = null
    ): InstantiationError {
        return InstantiationError(
            field = argument.field.name,
            originalField = argument.originalTargetName,
            providedValue = argument.value,
            type = type,
            cause = ex
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getTargetConstructor(
        target: Class<*>,
        fields: Collection<InstantiationField>
    ): GenericConstructor<T> {
        val fieldNames = fields.map { it.name }
        return try {
            (
                target.kotlin.constructors.filter { canBeSeen(it) }.find { constructor ->
                    constructor.parameters.map { it.name }.containsAll(fieldNames)
                } as KFunction<T>?
                )?.let { KotlinConstructor(it) }
        } catch (ex: KotlinReflectionNotSupportedError) {
            (
                target.constructors.find { constructor ->
                    constructor.parameters.map { it.name }.containsAll(fieldNames)
                } as Constructor<T>?
                )?.let { JavaConstructor(it) }
        } ?: throw InvalidTargetClass(target, fields)
    }

    private fun canBeSeen(it: KFunction<Any>) =
        it.visibility == KVisibility.PUBLIC || it.visibility == KVisibility.PROTECTED

    private interface GenericConstructor<T> {
        val parameterNames: List<String>
        fun call(args: Array<Any?>): T
    }

    private class JavaConstructor<T>(private val constructor: Constructor<T>) : GenericConstructor<T> {
        override val parameterNames: List<String>
            get() = constructor.parameters.map { it.name }

        override fun call(args: Array<Any?>): T = constructor.newInstance(*args)
    }

    private class KotlinConstructor<T>(private val function: KFunction<T>) : GenericConstructor<T> {
        override val parameterNames: List<String>
            get() = function.parameters.map { it.name ?: error("expected a parameter with a name on $function") }

        override fun call(args: Array<Any?>): T = function.call(*args)
    }
}
