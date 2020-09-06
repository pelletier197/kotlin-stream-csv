package com.kheops.csv.reader

import java.util.stream.Stream


data class HeaderCsvLine(
    val values: Map<String, String>,
    val line: Int,
    val errors: List<CsvErrorType>?,
) {
    val hasErrors: Boolean get() = !errors.isNullOrEmpty()
}

data class ColumnCsvError(override val type: CsvErrorType, override val line: Int, val column: String) : CsvError()

data class HeaderCsvReader(
    val header: List<String>? = null,
    val errorOnMissingProperties: Boolean = true,
    val reader: RawCsvReader = RawCsvReader(),
) {
    fun withHeader(header: List<String>): HeaderCsvReader {
        return copy(header = header)
    }

    fun read(lines: Stream<String>): Stream<HeaderCsvLine> {
        return readRaw(reader.read(lines))
    }

    fun readRaw(lines: Stream<RawCsvLine>): Stream<HeaderCsvLine> {
        var currentHeader = this.header
        return lines.filter(fun(line: RawCsvLine): Boolean {
            // Set header and skip
            if (currentHeader == null) {
                currentHeader = line.columns
                return false
            }
            return true
        }).map {

        }
    }
}
