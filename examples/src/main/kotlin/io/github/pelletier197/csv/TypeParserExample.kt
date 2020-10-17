package io.github.pelletier197.csv

import io.github.pelletier197.csv.reader.CsvReader
import java.util.stream.Collectors.toList

private val csv =
    """
    first_name, last_name, phone_number, emails
    John, Doe, 1+342-534-2342, "john.doe.1@test.com, john.doe.2@test.com"
    Alice, Doe, 1+423-253-3453, alice.doe@test.com 
""".trimMargin()

data class CsvPerson(
    @CsvProperty("first_name")
    val firstName: String,
    @CsvProperty("last_name")
    val lastName: String,
    @CsvProperty("phone_number")
    val phoneNumber: String,
    val emails: Set<String>
)

fun main() {
    val reader = CsvReader()
        .readerForType<CsvPerson>()
        .withSeparator(',')
        .withDelimiter('"')
        .withEmptyStringsAsNull(false)
        .withListSeparator(',')
        .withSkipEmptyLines(true)
        .withTrimEntries(false)

    val people = reader.read(csv).map { it.getResultOrThrow() }.collect(toList())

    println(people.joinToString(separator = "\n"))
    // Output:
    // CsvPerson(firstName=    John, lastName= Doe, phoneNumber= 1+342-534-2342, emails=[])
    // CsvPerson(firstName=    Alice, lastName= Doe, phoneNumber= 1+423-253-3453, emails=[ alice.doe@test.com ])
}
