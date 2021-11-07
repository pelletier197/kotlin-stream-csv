package io.github.pelletier197.csv.reader

import java.nio.charset.Charset

interface CsvReaderConfigurer<READER : CsvReader<*>> {
    fun withSeparator(separator: Char): READER

    fun withDelimiter(delimiter: Char): READER

    fun withTrimEntries(trim: Boolean): READER

    fun withSkipEmptyLines(skip: Boolean): READER

    fun withEmptyStringsAsNull(emptyAsNulls: Boolean): READER

    fun withEncoding(encoding: Charset): READER
}

interface HeaderCsvReaderConfigurer<READER : CsvReader<*>> : CsvReaderConfigurer<READER> {
    fun withHeader(header: List<String>): READER
}
