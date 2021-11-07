package io.github.pelletier197.csv.reader.types

import io.github.pelletier197.csv.reader.CsvReader
import io.github.pelletier197.csv.reader.CsvReaderConfigurer
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
) : CsvReader<RawCsvLine>, CsvReaderConfigurer<RawCsvReader> {
    override fun read(url: URL): Stream<RawCsvLine> {
        return read(url.openStream())
    }

    override fun read(input: InputStream): Stream<RawCsvLine> {
        return parser.parse(input)
    }

    override fun read(file: File): Stream<RawCsvLine> {
        return read(file.toPath())
    }

    override fun read(path: Path): Stream<RawCsvLine> {
        return read(Files.newInputStream(path))
    }

    override fun read(lines: List<String>): Stream<RawCsvLine> {
        return read(lines.joinToString(separator = "\n"))
    }

    override fun read(value: String): Stream<RawCsvLine> {
        return read(value.byteInputStream(parser.encoding))
    }

    override fun withSeparator(separator: Char): RawCsvReader {
        return copy(parser = parser.withSeparator(separator))
    }

    override fun withDelimiter(delimiter: Char): RawCsvReader {
        return copy(parser = parser.withDelimiter(delimiter))
    }

    override fun withTrimEntries(trim: Boolean): RawCsvReader {
        return copy(parser = parser.withTrimEntries(trim))
    }

    override fun withSkipEmptyLines(skip: Boolean): RawCsvReader {
        return copy(parser = parser.withSkipEmptyLines(skip))
    }

    override fun withEmptyStringsAsNull(emptyAsNulls: Boolean): RawCsvReader {
        return copy(parser = parser.withEmptyStringsAsNull(emptyAsNulls))
    }

    override fun withEncoding(encoding: Charset): RawCsvReader {
        return copy(parser = parser.withEncoding(encoding))
    }
}
