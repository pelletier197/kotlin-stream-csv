package com.kheops.csv.reader.reflect.converters

import com.kheops.csv.reader.reflect.converters.implementations.StringDoubleConverter
import com.kheops.csv.reader.reflect.converters.implementations.StringFloatConverter
import com.kheops.csv.reader.reflect.converters.implementations.StringInstantConverter
import com.kheops.csv.reader.reflect.converters.implementations.StringIntConverter
import com.kheops.csv.reader.reflect.converters.implementations.StringLocalDateConverter
import com.kheops.csv.reader.reflect.converters.implementations.StringLocalDateTimeConverter
import com.kheops.csv.reader.reflect.converters.implementations.StringLongConverter
import com.kheops.csv.reader.reflect.converters.implementations.StringToArrayListConverter
import com.kheops.csv.reader.reflect.converters.implementations.StringToBigDecimalConverter
import com.kheops.csv.reader.reflect.converters.implementations.StringToBigIntegerConverter
import com.kheops.csv.reader.reflect.converters.implementations.StringToBooleanConverter
import com.kheops.csv.reader.reflect.converters.implementations.StringToByteConverter
import com.kheops.csv.reader.reflect.converters.implementations.StringToEnumConverter
import com.kheops.csv.reader.reflect.converters.implementations.StringToHashSetConverter
import com.kheops.csv.reader.reflect.converters.implementations.StringToLinkedListConverter
import com.kheops.csv.reader.reflect.converters.implementations.StringToListConverter
import com.kheops.csv.reader.reflect.converters.implementations.StringToSetConverter
import com.kheops.csv.reader.reflect.converters.implementations.StringToTreeSetConverter
import com.kheops.csv.reader.reflect.converters.implementations.StringUIntConverter
import com.kheops.csv.reader.reflect.converters.implementations.StringULongConverter
import com.kheops.csv.reader.reflect.converters.implementations.StringZonedDateTimeConverter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KFunction
import kotlin.reflect.full.functions

private data class ConversionTargets(
    private val source: String,
    private val target: String
)

typealias ConvertFunction<S, T> = (value: S, to: Type, parameters: ConversionParameters) -> T

data class ConversionParameters(
    val settings: ConversionSettings,
    val convert: ConvertFunction<Any, *>
)

interface Converter<FROM, TO> {
    val source: Class<FROM>
    val target: Class<TO>
    fun convert(value: FROM, to: Type, parameters: ConversionParameters): TO?
}

internal data class ConverterWrapper(
    val converter: Converter<*, *>,
    val function: KFunction<Any?>
) {
    @Suppress("UNCHECKED_CAST")
    fun <S, T> convert(
        value: S,
        to: Type,
        settings: ConversionSettings,
        internalConvertFunction: ConvertFunction<Any, *>
    ): T {
        return convert(
            value,
            to,
            ConversionParameters(
                settings = settings,
                convert = internalConvertFunction,
            )
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun <S, T> convert(value: S, to: Type, parameters: ConversionParameters): T {
        val nonNullValue = value!!
        if (nonNullValue::class.java == to || to == Any::class.java) return value as T
        return function.call(converter, value, to, parameters) as T
    }
}

private val defaultConverters = listOf(
    StringFloatConverter(),
    StringLongConverter(),
    StringInstantConverter(),
    StringZonedDateTimeConverter(),
    StringLocalDateConverter(),
    StringLocalDateTimeConverter(),
    StringDoubleConverter(),
    StringToByteConverter(),
    StringIntConverter(),
    StringToBigDecimalConverter(),
    StringToBigIntegerConverter(),
    StringUIntConverter(),
    StringULongConverter(),
    StringToEnumConverter(),
    StringToBooleanConverter(),
    StringToListConverter(),
    StringToArrayListConverter(),
    StringToLinkedListConverter(),
    StringToSetConverter(),
    StringToHashSetConverter(),
    StringToTreeSetConverter(),
)

internal class Converters(converters: List<Converter<*, *>> = defaultConverters) {
    private val allConverters: Map<ConversionTargets, ConverterWrapper>

    init {
        allConverters = converters.map { converter ->
            ConversionTargets(converter.source.name, converter.target.name) to ConverterWrapper(
                converter = converter,
                function = Converter::class.functions.find { f -> f.name == "convert" }
                    ?: error("expected a function convert inside the converter interface")
            )
        }.toMap()
    }

    private val converters: List<Converter<*, *>> get() = allConverters.values.map { it.converter }

    fun getConverter(from: Class<*>, to: Type): ConverterWrapper? {
        val typeName = getConverterTypeName(to) ?: return null
        val result = allConverters[ConversionTargets(from.name, typeName)]

        if (result == null && to is Class<*> && to.isEnum) {
            // Special converter for enums
            return allConverters[ConversionTargets(from.name, Enum::class.java.name)]
        }

        return result
    }

    fun withConverter(newConverter: Converter<*, *>): Converters {
        return Converters(
            converters = converters + newConverter
        )
    }

    fun withConverters(newConverters: List<Converter<*, *>>): Converters {
        return Converters(
            converters = converters + newConverters
        )
    }

    fun withClearedConverters(): Converters {
        return Converters(
            converters = emptyList()
        )
    }

    private fun getConverterTypeName(type: Type): String? {
        if (type is Class<*>) return type.name
        if (type is ParameterizedType) return type.rawType.typeName
        return null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Converters

        if (allConverters != other.allConverters) return false

        return true
    }

    override fun hashCode(): Int {
        return allConverters.hashCode()
    }
}
