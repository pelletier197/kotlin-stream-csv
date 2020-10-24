package io.github.pelletier197.csv

import io.github.pelletier197.csv.reader.CsvReader
import kotlin.streams.toList

fun main() {
    val reader = CsvReader()
        .reader()
        .withSeparator(',')
        .withDelimiter('"')
        .withEmptyStringsAsNull(false)
        .withSkipEmptyLines(true)
        .withTrimEntries(false)
    val people = reader.read(csv).map { it }.toList()

    println(people.joinToString(separator = "\n"))
    // Output:
    // RawCsvLine(columns=[first_name,  last_name,  phone_number,  emails], line=1)
    // RawCsvLine(columns=[John,  Doe,  1+342-534-2342, john.doe.1@test.com, john.doe.2@test.com], line=2)
    // RawCsvLine(columns=[Alice,  Doe,  1+423-253-3453,  alice.doe@test.com ], line=3)
}
