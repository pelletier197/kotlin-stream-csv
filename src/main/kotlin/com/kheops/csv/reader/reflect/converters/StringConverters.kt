package com.kheops.csv.reader.reflect.converters

import java.text.DateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

abstract class StringConverterBase<TO> : Converter<String, TO> {
    override val source: Class<String> get() = String::class.java
}

class StringInstantConverter() : StringConverterBase<Instant>() {
    override val target: Class<Instant> = Instant::class.java
    override fun convert(value: String?): Instant? = value?.let { Instant.parse(it) }
}

class StringZonedDateTimeConverter() : StringConverterBase<ZonedDateTime>() {
    override val target: Class<ZonedDateTime> get() = ZonedDateTime::class.java
    override fun convert(value: String?): ZonedDateTime? = value?.let { ZonedDateTime.parse(it) }
}

class StringLocalDateConverter() : StringConverterBase<LocalDate>() {
    override val target: Class<LocalDate> get() = LocalDate::class.java
    override fun convert(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }
}

class StringLocalDateTimeConverter() : StringConverterBase<LocalDateTime>() {
    override val target: Class<LocalDateTime> get() = LocalDateTime::class.java
    override fun convert(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it) }
}

class StringDateConverter() : StringConverterBase<Date>() {
    private val dateFormat = DateFormat.getInstance()
    override val target: Class<Date> get() = Date::class.java
    override fun convert(value: String?): Date? = value?.let { dateFormat.parse(it) }
}


class StringLongConverter() : StringConverterBase<Long>() {
    override val target: Class<Long> = Long::class.java
    override fun convert(value: String?): Long? = value?.toLong()
}

class StringIntConverter() : StringConverterBase<Int>() {
    override val target: Class<Int> = Int::class.java
    override fun convert(value: String?): Int? = value?.toInt()
}

class StringFloatConverter() : StringConverterBase<Float>() {
    override val target: Class<Float> = Float::class.java
    override fun convert(value: String?): Float? = value?.toFloat()
}

class StringDoubleConverter() : StringConverterBase<Double>() {
    override val target: Class<Double> get() = Double::class.java
    override fun convert(value: String?): Double? = value?.toDouble()
}

class StringToByteConverter() : StringConverterBase<Byte>() {
    override val target: Class<Byte> get() = Byte::class.java
    override fun convert(value: String?): Byte? = value?.toByte()
}

@Suppress("EXPERIMENTAL_API_USAGE")
class StringUIntConverter() : StringConverterBase<UInt>() {
    override val target: Class<UInt> get() = UInt::class.java
    override fun convert(value: String?): UInt? = value?.toUInt()
}

@Suppress("EXPERIMENTAL_API_USAGE")
class StringULongConverter() : StringConverterBase<ULong>() {
    override val target: Class<ULong> get() = ULong::class.java
    override fun convert(value: String?): ULong? = value?.toULong()
}