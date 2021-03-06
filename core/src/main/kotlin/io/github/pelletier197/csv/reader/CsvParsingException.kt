package io.github.pelletier197.csv.reader

import java.lang.Exception

data class CsvParsingException(
    override val message: String,
    val errors: List<CsvError>,
    val line: Int
) : Exception(message)
