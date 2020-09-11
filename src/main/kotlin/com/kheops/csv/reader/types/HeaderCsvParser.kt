package com.kheops.csv.reader.types

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

    fun read(lines: Stream<String>): Stream<HeaderCsvLine> {
        return readRaw(reader.read(lines))
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

    fun readRaw(lines: Stream<RawCsvLine>): Stream<HeaderCsvLine> {
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
