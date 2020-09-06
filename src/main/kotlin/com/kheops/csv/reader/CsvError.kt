package com.kheops.csv.reader

import javax.lang.model.type.ErrorType

enum class CsvErrorType {
    NON_NULL_RESULT_EXPECTED
}

abstract class CsvError {
    abstract val line: Int
    abstract val type: CsvErrorType
}

