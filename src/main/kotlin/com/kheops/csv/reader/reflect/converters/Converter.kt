package com.kheops.csv.reader.reflect.converters

import com.kheops.csv.reader.reflect.converters.Converters.getConverter
import kotlin.reflect.KFunction
import kotlin.reflect.full.functions

interface Converter<FROM, TO> {
    val source: Class<FROM>
    val target: Class<TO>
    fun convert(value: FROM?): TO?
}

private data class ConverterWrapper(
    private val converter: Converter<*, *>,
    private val function: KFunction<Any?>
) {
    @Suppress("UNCHECKED_CAST")
    fun <S, T> convert(value: S): T {
        return function.call(converter, value) as T
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
        ).forEach { registerConverter(it) }
    }

    fun getConverter(from: Class<*>, to: Class<*>): ConverterWrapper? {
        return allConverters[ConversionTargets(from.canonicalName, to.canonicalName)]
    }

    fun registerConverter(converter: Converter<*, *>) {
        allConverters[ConversionTargets(converter.source.canonicalName, converter.target.canonicalName)] =
            ConverterWrapper(
                converter = converter,
                function = Converter::class.functions.find { f -> f.name == "convert" }
                    ?: error("expected a function convert inside the converter interface")
            )
    }


}

class NoConverterFoundException(value: Any, target: Class<*>) :
    Exception("could not find a converter for value '${value}' of type '${value::class.java.name}' to '${target.name}'")

class ConversionFailedException(value: Any, target: Class<*>, exception: Exception) :
    Exception(
        "conversion failed for value '${value}' of type '${value::class.java.name}' to '${target.name}'",
        exception
    )

@Suppress("UNNECESSARY_NOT_NULL_ASSERTION", "UNCHECKED_CAST")
fun <S, T> convert(value: S?, to: Class<T>): T? {
    if (value == null) return null
    val nonNullValue = value!!
    val converter = getConverter(nonNullValue::class.java, to) ?: throw NoConverterFoundException(nonNullValue, to)

    try {
        return converter.convert(nonNullValue)
    } catch (ex: Exception) {
        throw ConversionFailedException(nonNullValue, to, ex)
    }
}

fun registerConverter(converter: Converter<*, *>) {
    Converters.registerConverter(converter)
}