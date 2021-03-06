package io.github.pelletier197.csv.reader

enum class CsvErrorType {
    NON_NULL_RESULT_EXPECTED,
    NON_NULLABLE_FIELD_IS_NULL,
    NO_CONVERTER_FOUND_FOR_VALUE,
    CONVERSION_OF_FIELD_FAILED
}

data class CsvError(
    val csvField: String,
    val classField: String,
    val providedValue: String?,
    val type: CsvErrorType,
    val cause: Throwable?,
)
