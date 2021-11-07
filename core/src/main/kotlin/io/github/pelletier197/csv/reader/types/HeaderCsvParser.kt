package io.github.pelletier197.csv.reader.types

import io.github.pelletier197.csv.reader.CsvLine
import io.github.pelletier197.csv.reader.CsvReader
import io.github.pelletier197.csv.reader.CsvReaderConfigurer
import io.github.pelletier197.csv.reader.parser.RawCsvLine
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.charset.Charset
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference
import java.util.stream.Stream

data class HeaderCsvLine(
    val values: Map<String, String?>,
    override val line: Int
) : CsvLine

data class HeaderCsvReader(
    val header: List<String>? = null,
    private val reader: RawCsvReader = RawCsvReader(),
) : CsvReader<HeaderCsvLine>, CsvReaderConfigurer<HeaderCsvReader> {
    fun withHeader(header: List<String>): HeaderCsvReader {
        return copy(header = header)
    }

    override fun withSeparator(separator: Char): HeaderCsvReader {
        return copy(reader = reader.withSeparator(separator))
    }

    override fun withDelimiter(delimiter: Char): HeaderCsvReader {
        return copy(reader = reader.withDelimiter(delimiter))
    }

    override fun withTrimEntries(trim: Boolean): HeaderCsvReader {
        return copy(reader = reader.withTrimEntries(trim))
    }

    override fun withSkipEmptyLines(skip: Boolean): HeaderCsvReader {
        return copy(reader = reader.withSkipEmptyLines(skip))
    }

    override fun withEmptyStringsAsNull(emptyAsNulls: Boolean): HeaderCsvReader {
        return copy(reader = reader.withEmptyStringsAsNull(emptyAsNulls))
    }

    override fun withEncoding(encoding: Charset): HeaderCsvReader {
        return copy(reader = reader.withEncoding(encoding))
    }

    override fun read(url: URL): Stream<HeaderCsvLine> {
        return readRaw(reader.read(url))
    }

    override fun read(input: InputStream): Stream<HeaderCsvLine> {
        return readRaw(reader.read(input))
    }

    override fun read(file: File): Stream<HeaderCsvLine> {
        return readRaw(reader.read(file))
    }

    override fun read(path: Path): Stream<HeaderCsvLine> {
        return readRaw(reader.read(path))
    }

    override fun read(lines: List<String>): Stream<HeaderCsvLine> {
        return readRaw(reader.read(lines))
    }

    override fun read(value: String): Stream<HeaderCsvLine> {
        return readRaw(reader.read(value))
    }

    private fun readRaw(lines: Stream<RawCsvLine>): Stream<HeaderCsvLine> {
        val currentHeader = AtomicReference(this.header)
        return lines
            .filter { setHeaderAndSkip(it, currentHeader) }
            .map {
                HeaderCsvLine(
                    values = currentHeader.get()!!
                        .mapIndexed { index, column -> column to it.columns.getOrElse(index) { if (reader.parser.emptyStringsAsNull) null else "" } }
                        .toMap(),
                    line = it.line,
                )
            }
    }

    private fun setHeaderAndSkip(rawCsvLine: RawCsvLine, currentHeader: AtomicReference<List<String>?>): Boolean {
        if (currentHeader.get() == null) {
            currentHeader.set(rawCsvLine.columns.filterNotNull().map { it.trim() })
            return false
        }
        return true
    }
}
