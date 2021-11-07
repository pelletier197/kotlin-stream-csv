package io.github.pelletier197.csv

import io.github.pelletier197.csv.reader.CsvReaders
import io.github.pelletier197.csv.reader.reflect.converters.ConversionContext
import io.github.pelletier197.csv.reader.reflect.converters.Converter
import java.lang.reflect.Type
import kotlin.streams.toList

data class CustomCsvPerson(
    @CsvProperty("first_name")
    val firstName: String,
    @CsvProperty("last_name")
    val lastName: String,
    val emails: Set<Email>
)

data class Email(
    val value: String
)

class EmailConverter : Converter<String, Email> {
    override val source: Class<String> get() = String::class.java
    override val target: Class<Email> get() = Email::class.java

    override fun convert(value: String, to: Type, context: ConversionContext): Email {
        return Email(value = value)
    }
}

fun main() {
    val reader = CsvReaders
        .forType<CustomCsvPerson>()
        .withConverter(EmailConverter())

    val people = reader.read(csv).map { it.getResultOrThrow() }.toList()

    println(people.joinToString(separator = "\n"))
    // Output:
    // CustomCsvPerson(firstName=John, lastName= Doe, emails=[Email(value=john.doe.1@test.com), Email(value= john.doe.2@test.com)])
    // CustomCsvPerson(firstName=Alice, lastName= Doe, emails=[Email(value= alice.doe@test.com )])
}
