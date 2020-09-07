package com.kheops.csv.reader.reflect.converters

import java.time.Instant

abstract class StringConverterBase<TO> : Converter<String, TO> {
    override val source: Class<String> get() = String::class.java
}

class StringInstantConverter() : StringConverterBase<Instant>() {
    override val target: Class<Instant> = Instant::class.java
    override fun convert(value: String?): Instant? = value?.let { Instant.parse(it) }
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