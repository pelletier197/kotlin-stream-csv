package io.github.pelletier197.csv.reader.reflect.converters

import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Type

class ConversionSettings(
    val listSeparator: Char = ','
)

class NoConverterFoundException(value: Any, target: Type) :
    Exception(
        """
        Could not find a converter for value '$value' of type '${value::class.java.name}' to '${target.typeName}'.
        It is possible the value you are trying to convert is not supported by default. You should create and register your custom converter to handle this value. 
    """.trimMargin()
    )

class ConversionFailedException(value: Any, target: Type, cause: Throwable) :
    Exception(
        "conversion failed for value '$value' of type '${value::class.java.name}' to '${target.typeName}'",
        cause
    )

class TypeConverter private constructor(private val converters: Converters) {
    companion object {
        fun getDefault(): TypeConverter {
            return TypeConverter(converters = Converters())
        }
    }

    fun <S, T> convertForField(
        value: S,
        field: Field,
        settings: ConversionSettings = ConversionSettings()
    ): T {
        return convertToType(value, field.genericType, settings)
    }

    @Suppress("UNNECESSARY_NOT_NULL_ASSERTION", "UNCHECKED_CAST")
    fun <S, T> convertToType(
        value: S,
        to: Type,
        settings: ConversionSettings = ConversionSettings()
    ): T {
        val nonNullValue = value!!
        if (nonNullValue::class.java == to || to == Any::class.java) return value as T
        val converter =
            converters.getConverter(nonNullValue::class.java, to) ?: throw NoConverterFoundException(nonNullValue, to)

        try {
            return converter.convert(nonNullValue, to, settings) { internalValue, internalTo, internalSettings ->
                this.convertToType(
                    internalValue,
                    internalTo,
                    internalSettings.settings,
                )
            }
        } catch (ex: InvocationTargetException) {
            throw ConversionFailedException(nonNullValue, to, ex.cause ?: ex)
        } catch (ex: Exception) {
            throw ConversionFailedException(nonNullValue, to, ex)
        }
    }

    fun withConverter(newConverter: Converter<*, *>): TypeConverter {
        return TypeConverter(converters = converters.withConverter(newConverter))
    }

    fun withConverters(newConverters: List<Converter<*, *>>): TypeConverter {
        return TypeConverter(converters = converters.withConverters(newConverters))
    }

    fun withClearedConverters(): TypeConverter {
        return TypeConverter(converters = converters.withClearedConverters())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TypeConverter

        if (converters != other.converters) return false

        return true
    }

    override fun hashCode(): Int {
        return converters.hashCode()
    }
}
