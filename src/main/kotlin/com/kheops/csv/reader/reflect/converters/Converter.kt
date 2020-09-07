package com.kheops.csv.reader.reflect.converters

import java.lang.Exception
import kotlin.reflect.KFunction
import kotlin.reflect.full.functions

interface Converter<FROM, TO> {
    val source: Class<FROM>
    val target: Class<TO>
    fun convert(value: FROM?): TO?
}

private data class ConversionTargets(
    val source: String,
    val target: String
)

private data class ConverterWrapper(
    val converter: Converter<*, *>,
    val function: KFunction<Any?>
) {
    @Suppress("UNCHECKED_CAST")
    fun <S, T> convert(value: S): T {
        return function.call(converter, value) as T
    }
}

private val allConverters: Map<ConversionTargets, ConverterWrapper> = listOf(
    StringFloatConverter(),
    StringLongConverter(),
    StringInstantConverter(),
    StringIntConverter()
).map {
    ConversionTargets(it.source.canonicalName, it.target.canonicalName) to ConverterWrapper(
        converter = it,
        function = Converter::class.functions.find { f -> f.name == "convert" }
            ?: error("expected a function convert inside the converter interface")
    )
}.toMap()

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
    val converter = allConverters[ConversionTargets(nonNullValue::class.java.canonicalName, to.canonicalName)]
        ?: throw NoConverterFoundException(nonNullValue, to)
    
    try {
        return converter.convert(nonNullValue)
    } catch (ex: Exception) {
        throw ConversionFailedException(nonNullValue, to, ex)
    }
}