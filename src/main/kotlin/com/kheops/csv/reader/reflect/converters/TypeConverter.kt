package com.kheops.csv.reader.reflect.converters

import java.lang.reflect.Field
import java.lang.reflect.Type

class ConversionSettings(
    val listSeparator: String = ","
)



class NoConverterFoundException(value: Any, target: Type) :
    Exception(
        """
        could not find a converter for value '${value}' of type '${value::class.java.name}' to '${target.typeName}'.
        It is possible the value you are trying to convert is not supported by default. You can add the custom converter yourself by calling
        com.kheops.csv.reader.reflect.converters.registerConverter(com.kheops.csv.reader.reflect.converters.Converter)
    """.trimMargin()
    )

class ConversionFailedException(value: Any, target: Type, exception: Exception) :
    Exception(
        "conversion failed for value '${value}' of type '${value::class.java.name}' to '${target.typeName}'",
        exception
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
            return converter.convert(nonNullValue, to, settings)
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
}

