package io.github.pelletier197.csv.reader.reflect.converters.implementations

import com.kheops.csv.reader.reflect.converters.ConversionParameters
import com.kheops.csv.reader.reflect.converters.Converter
import java.lang.reflect.Type
import java.math.BigDecimal
import java.math.BigInteger

abstract class StringConverterBase<TO> : Converter<String, TO> {
    override val source: Class<String> get() = String::class.java

    @Suppress("UNCHECKED_CAST")
    override fun convert(value: String, to: Type, parameters: ConversionParameters): TO? =
        doConvert(value.trim(), to as Class<TO>)

    abstract fun doConvert(value: String, to: Class<TO>): TO?
}

class StringLongConverter : StringConverterBase<Long>() {
    override val target: Class<Long> = Long::class.java
    override fun doConvert(value: String, to: Class<Long>): Long = value.toLong()
}

class StringIntConverter : StringConverterBase<Int>() {
    override val target: Class<Int> = Int::class.java
    override fun doConvert(value: String, to: Class<Int>): Int = value.toInt()
}

class StringFloatConverter : StringConverterBase<Float>() {
    override val target: Class<Float> = Float::class.java
    override fun doConvert(value: String, to: Class<Float>): Float = value.toFloat()
}

class StringDoubleConverter : StringConverterBase<Double>() {
    override val target: Class<Double> get() = Double::class.java
    override fun doConvert(value: String, to: Class<Double>): Double = value.toDouble()
}

class StringToByteConverter : StringConverterBase<Byte>() {
    override val target: Class<Byte> get() = Byte::class.java
    override fun doConvert(value: String, to: Class<Byte>): Byte = value.toByte()
}

class StringToBigDecimalConverter : StringConverterBase<BigDecimal>() {
    override val target: Class<BigDecimal> get() = BigDecimal::class.java
    override fun doConvert(value: String, to: Class<BigDecimal>): BigDecimal = value.toBigDecimal()
}

class StringToBigIntegerConverter : StringConverterBase<BigInteger>() {
    override val target: Class<BigInteger> get() = BigInteger::class.java
    override fun doConvert(value: String, to: Class<BigInteger>): BigInteger = value.toBigInteger()
}

class StringToEnumConverter : StringConverterBase<Enum<*>>() {
    override val target: Class<Enum<*>> get() = Enum::class.java
    override fun doConvert(value: String, to: Class<Enum<*>>): Enum<*> =
        to.enumConstants.first { value.toLowerCase() == it.name.toLowerCase() }
}

class StringToBooleanConverter : StringConverterBase<Boolean>() {
    private val trueValues = setOf("true", "t", "yes", "1", "x")
    override val target: Class<Boolean> get() = Boolean::class.java
    override fun doConvert(value: String, to: Class<Boolean>): Boolean = trueValues.contains(value.toLowerCase())
}

@Suppress("EXPERIMENTAL_API_USAGE")
class StringUIntConverter : StringConverterBase<UInt>() {
    override val target: Class<UInt> get() = UInt::class.java
    override fun doConvert(value: String, to: Class<UInt>): UInt = value.toUInt()
}

@Suppress("EXPERIMENTAL_API_USAGE")
class StringULongConverter : StringConverterBase<ULong>() {
    override val target: Class<ULong> get() = ULong::class.java
    override fun doConvert(value: String, to: Class<ULong>): ULong = value.toULong()
}
