package io.github.pelletier197.csv.reader.types

import io.github.pelletier197.csv.reader.parser.CsvLineParser
import io.github.pelletier197.csv.reader.parser.RawCsvLine
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream

data class RawCsvReader(
    val parser: CsvLineParser = CsvLineParser()
) {
    fun read(url: URL): Stream<RawCsvLine> {
        return read(url.openStream())
    }

    fun read(input: InputStream): Stream<RawCsvLine> {
        return parser.parse(input)
    }

    fun read(file: File): Stream<RawCsvLine> {
        return read(file.toPath())
    }

    fun read(path: Path): Stream<RawCsvLine> {
        return read(Files.newInputStream(path))
    }

    fun read(lines: List<String>): Stream<RawCsvLine> {
        return read(lines.joinToString(separator = "\n"))
    }

    fun read(value: String): Stream<RawCsvLine> {
        return read(value.byteInputStream(parser.encoding))
    }

    fun withSeparator(separator: Char): RawCsvReader {
        return copy(parser = parser.withSeparator(separator))
    }

    fun withDelimiter(delimiter: Char): RawCsvReader {
        return copy(parser = parser.withDelimiter(delimiter))
    }

    fun withTrimEntries(trim: Boolean): RawCsvReader {
        return copy(parser = parser.withTrimEntries(trim))
    }

    fun withSkipEmptyLines(skip: Boolean): RawCsvReader {
        return copy(parser = parser.withSkipEmptyLines(skip))
    }

    fun withEmptyStringsAsNull(emptyAsNulls: Boolean): RawCsvReader {
        return copy(parser = parser.withEmptyStringsAsNull(emptyAsNulls))
    }

    fun withEncoding(encoding: Charset): RawCsvReader {
        return copy(parser = parser.withEncoding(encoding))
    }
}
