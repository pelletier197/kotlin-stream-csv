package com.kheops.csv.reader.reflect.converters.implementations

import java.text.DateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

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