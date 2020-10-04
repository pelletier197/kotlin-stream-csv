package com.kheops.csv.reader.reflect.converters

import java.lang.reflect.Type
import java.math.BigDecimal
import java.math.BigInteger
import java.text.DateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

abstract class StringConverterBase<TO> : Converter<String, TO> {
    override val source: Class<String> get() = String::class.java

    @Suppress("UNCHECKED_CAST")
    override fun convert(value: String, to: Type, parameters: ConversionParameters): TO? =
        doConvert(value.trim(), to as Class<TO>)

    abstract fun doConvert(value: String, to: Class<TO>): TO?
}

class StringInstantConverter : StringConverterBase<Instant>() {
    override val target: Class<Instant> = Instant::class.java
    override fun doConvert(value: String, to: Class<Instant>): Instant = Instant.parse(value)
}

class StringZonedDateTimeConverter : StringConverterBase<ZonedDateTime>() {
    override val target: Class<ZonedDateTime> get() = ZonedDateTime::class.java
    override fun doConvert(value: String, to: Class<ZonedDateTime>): ZonedDateTime = ZonedDateTime.parse(value)
}

class StringLocalDateConverter : StringConverterBase<LocalDate>() {
    override val target: Class<LocalDate> get() = LocalDate::class.java
    override fun doConvert(value: String, to: Class<LocalDate>): LocalDate = LocalDate.parse(value)
}

class StringLocalDateTimeConverter : StringConverterBase<LocalDateTime>() {
    override val target: Class<LocalDateTime> get() = LocalDateTime::class.java
    override fun doConvert(value: String, to: Class<LocalDateTime>): LocalDateTime = LocalDateTime.parse(value)
}

class StringDateConverter : StringConverterBase<Date>() {
    private val dateFormat = DateFormat.getInstance()
    override val target: Class<Date> get() = Date::class.java
    override fun doConvert(value: String, to: Class<Date>): Date = dateFormat.parse(value)
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
    private val trueValues = setOf("true", "t", "yes", "1")
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
