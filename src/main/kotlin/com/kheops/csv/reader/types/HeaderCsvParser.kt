package com.kheops.csv.reader.types

import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference
import java.util.stream.Stream

data class HeaderCsvLine(
    val values: Map<String, String?>,
    val line: Int
)

data class HeaderCsvReader(
    val header: List<String>? = null,
    private val reader: RawCsvReader = RawCsvReader(),
) {
    fun withHeader(header: List<String>): HeaderCsvReader {
        return copy(header = header)
    }

    fun withSeparator(separator: String): HeaderCsvReader {
        return copy(reader = reader.withSeparator(separator))
    }

    fun withDelimiter(delimiter: String): HeaderCsvReader {
        return copy(reader = reader.withDelimiter(delimiter))
    }

    fun withTrimEntries(trim: Boolean): HeaderCsvReader {
        return copy(reader = reader.withTrimEntries(trim))
    }

    fun withSkipEmptyLines(skip: Boolean): HeaderCsvReader {
        return copy(reader = reader.withSkipEmptyLines(skip))
    }

    fun withEmptyStringsAsNull(emptyAsNulls: Boolean): HeaderCsvReader {
        return copy(reader = reader.withEmptyStringsAsNull(emptyAsNulls))
    }

    fun read(url: URL, charset: Charset = StandardCharsets.UTF_8): Stream<HeaderCsvLine> {
        return readRaw(reader.read(url, charset))
    }

    fun read(input: InputStream, charset: Charset = StandardCharsets.UTF_8): Stream<HeaderCsvLine> {
        return readRaw(reader.read(input, charset))
    }

    fun read(file: File): Stream<HeaderCsvLine> {
        return readRaw(reader.read(file))
    }

    fun read(path: Path): Stream<HeaderCsvLine> {
        return readRaw(reader.read(path))
    }

    fun read(lines: List<String>): Stream<HeaderCsvLine> {
        return readRaw(reader.read(lines))
    }

    fun read(value: String): Stream<HeaderCsvLine> {
        return readRaw(reader.read(value))
    }

    fun read(lines: Stream<String>): Stream<HeaderCsvLine> {
        return readRaw(reader.read(lines))
    }

    private fun readRaw(lines: Stream<RawCsvLine>): Stream<HeaderCsvLine> {
        val currentHeader = AtomicReference(this.header)
        return lines.filter { setHeaderAndSkip(it, currentHeader) }.map {
            HeaderCsvLine(
                values = currentHeader.get()!!.mapIndexed { index, column -> column to it.columns[index] }.toMap(),
                line = it.line,
            )
        }
    }

    private fun setHeaderAndSkip(rawCsvLine: RawCsvLine, currentHeader: AtomicReference<List<String>?>): Boolean {
        if (currentHeader.get() == null) {
            currentHeader.set(rawCsvLine.columns.filterNotNull())
            return false
        }
        return true
    }
}
