package com.kheops.csv.reader

import java.lang.Exception

data class CsvParsingException(
    override val message: String,
    val errors: List<CsvErrorType>,
    val line: Int
) : Exception(message)
