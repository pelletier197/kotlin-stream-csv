package io.github.pelletier197.csv

import io.github.pelletier197.csv.reader.CsvReader
import kotlin.streams.toList

fun main() {
    val reader = CsvReader()
        .readerWithHeader()
        // .withHeader("first_name", "last_name", "phone_number", "emails") // If you wish to provide the header yourself
        .withSeparator(',')
        .withDelimiter('"')
        .withEmptyStringsAsNull(false)
        .withSkipEmptyLines(true)
        .withTrimEntries(false)
    val people = reader.read(csv).map { it }.toList()

    println(people.joinToString(separator = "\n"))
    // Output:
    // HeaderCsvLine(values={first_name=    John, last_name= Doe, phone_number= 1+342-534-2342, emails=john.doe.1@test.com, john.doe.2@test.com}, line=2)
    // HeaderCsvLine(values={first_name=    Alice, last_name= Doe, phone_number= 1+423-253-3453, emails= alice.doe@test.com }, line=3)
}
