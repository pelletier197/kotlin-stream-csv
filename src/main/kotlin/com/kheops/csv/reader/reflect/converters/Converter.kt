package com.kheops.csv.reader.reflect.converters

import com.kheops.csv.reader.reflect.converters.Converters.getConverter
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KFunction
import kotlin.reflect.full.functions

class ConversionSettings(
    val listSeparator: String = ","
)

interface Converter<FROM, TO> {
    val source: Class<FROM>
    val target: Class<TO>
    fun convert(value: FROM, to: Type, settings: ConversionSettings): TO?
}

private data class ConverterWrapper(
    private val converter: Converter<*, *>,
    private val function: KFunction<Any?>
) {
    @Suppress("UNCHECKED_CAST")
    fun <S, T> convert(value: S, to: Type, settings: ConversionSettings): T {
        return function.call(converter, value, to, settings) as T
    }
}

private object Converters {
    private val allConverters = HashMap<ConversionTargets, ConverterWrapper>()

    private data class ConversionTargets(
        val source: String,
        val target: String
    )

    init {
        listOf(
            StringFloatConverter(),
            StringLongConverter(),
            StringInstantConverter(),
            StringZonedDateTimeConverter(),
            StringLocalDateConverter(),
            StringLocalDateTimeConverter(),
            StringDateConverter(),
            StringDoubleConverter(),
            StringToByteConverter(),
            StringIntConverter(),
            StringToBigDecimalConverter(),
            StringToBigIntegerConverter(),
            StringUIntConverter(),
            StringULongConverter(),
            StringToEnumConverter(),
            StringToBooleanConverter(),
            StringToListConverter()
        ).forEach { registerConverter(it) }
    }

    fun getConverter(from: Class<*>, to: Type): ConverterWrapper? {
        val typeName = getConverterTypeName(to) ?: return null
        val result = allConverters[ConversionTargets(from.canonicalName, typeName)]

        if (result == null && to is Class<*> && to.superclass == Enum::class.java) {
            // Special converter for enums
            return allConverters[ConversionTargets(from.canonicalName, Enum::class.java.canonicalName)]
        }

        return result
    }

    fun registerConverter(converter: Converter<*, *>) {
        allConverters[ConversionTargets(converter.source.canonicalName, converter.target.canonicalName)] =
            ConverterWrapper(
                converter = converter,
                function = Converter::class.functions.find { f -> f.name == "convert" }
                    ?: error("expected a function convert inside the converter interface")
            )
    }

    private fun getConverterTypeName(type: Type): String? {
        if (type is Class<*>) return type.canonicalName
        if (type is ParameterizedType) return type.rawType.typeName
        return null
    }
}

class NoConverterFoundException(value: Any, target: Type) :
    Exception("could not find a converter for value '${value}' of type '${value::class.java.name}' to '${target.typeName}'")

class ConversionFailedException(value: Any, target: Type, exception: Exception) :
    Exception(
        "conversion failed for value '${value}' of type '${value::class.java.name}' to '${target.typeName}'",
        exception
    )

fun <S, T> convertToClass(
    value: S,
    to: Class<T>,
    settings: ConversionSettings = ConversionSettings()
): T {
    return convertToType(value, to, settings)
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

    val converter = getConverter(nonNullValue::class.java, to) ?: throw NoConverterFoundException(nonNullValue, to)

    try {
        return converter.convert(nonNullValue, to, settings)
    } catch (ex: Exception) {
        throw ConversionFailedException(nonNullValue, to, ex)
    }
}

fun registerConverter(converter: Converter<*, *>) {
    Converters.registerConverter(converter)
}

