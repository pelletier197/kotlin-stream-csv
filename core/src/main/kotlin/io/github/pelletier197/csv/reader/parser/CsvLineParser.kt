package io.github.pelletier197.csv.reader.parser

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.Comparator
import java.util.Spliterator
import java.util.Spliterator.DISTINCT
import java.util.Spliterator.IMMUTABLE
import java.util.Spliterator.NONNULL
import java.util.Spliterator.ORDERED
import java.util.Spliterator.SORTED
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.Stream
import java.util.stream.StreamSupport

data class RawCsvLine(
    val columns: List<String?>,
    val line: Int
)

data class CsvLineParser(
    val separator: Char = ',',
    val delimiter: Char = '"',
    val escapeCharacter: Char = '\\',
    val trimEntries: Boolean = false,
    val skipEmptyLines: Boolean = true,
    val emptyStringsAsNull: Boolean = false,
    val encoding: Charset = Charset.defaultCharset(),
) {
    fun parse(input: InputStream): Stream<RawCsvLine> {
        return StreamSupport.stream(createSplitIterator(input), false)
    }

    private fun createSplitIterator(input: InputStream): Spliterator<RawCsvLine> {
        val charReader = BufferedReader(InputStreamReader(input, encoding))
        val lineIndex = AtomicInteger(1)

        return object : Spliterator<RawCsvLine> {
            override fun tryAdvance(consumer: Consumer<in RawCsvLine>): Boolean {
                val line = readNextLineColumns(charReader)

                if (line == null) {
                    charReader.close()
                    return false
                }

                val processedLine = processLine(line)
                    ?: return true // Line is skipped, but there may still be remaining elements

                val result = RawCsvLine(
                    columns = processedLine,
                    line = lineIndex.getAndIncrement()
                )

                consumer.accept(result)

                return true
            }

            override fun trySplit(): Spliterator<RawCsvLine>? {
                return null
            }

            override fun estimateSize(): Long {
                return 0
            }

            override fun characteristics(): Int {
                return DISTINCT or SORTED or ORDERED or NONNULL or IMMUTABLE
            }

            override fun getComparator(): Comparator<in RawCsvLine> {
                return Comparator.comparing { it.line }
            }
        }
    }

    private fun readNextLineColumns(reader: BufferedReader): List<String>? {
        var escape = false
        var columnBuilder = StringBuilder()
        var delimiterActive = false
        val columns = ArrayList<String>()

        while (true) {
            val current = reader.read()
            // End of stream
            if (current == -1) {
                if (delimiterActive) {
                    // TODO - this should raise an error - a delimiter was opened but never closed
                }

                val currentLine = columnBuilder.toString()

                if (columns.isEmpty() && currentLine.isEmpty()) {
                    return null
                }

                columns.add(currentLine)
                return columns
            }

            val char = current.toChar()

            if (escape) {
                // The previous character is the escape char, which means that the current character is used literally
                columnBuilder.append(char)
                escape = false
                continue
            }

            if (char == escapeCharacter) {
                escape = true
                continue
            }

            if (char == delimiter) {
                delimiterActive = !delimiterActive
                continue
            }

            if (char == separator) {
                // We are inside a delimiter, which means that everything in there is valid
                if (delimiterActive) {
                    columnBuilder.append(char)
                } else {
                    columns.add(columnBuilder.toString())
                    columnBuilder = StringBuilder()
                }
                continue
            }

            if (char == '\r') {
                // Ignore those
                continue
            }

            if (char == '\n') {
                if (delimiterActive) {
                    columnBuilder.append(char)
                    continue
                } else {
                    columns.add(columnBuilder.toString())
                    return columns
                }
            }

            columnBuilder.append(char)
        }
    }

    private fun processLine(columns: List<String>): List<String?>? {
        if (skipEmptyLines && columns.isEmpty()) {
            return null
        }

        if (columns.size == 1 && columns.first().isBlank()) {
            return if (skipEmptyLines) {
                null
            } else {
                emptyList()
            }
        }

        var columnStream = columns.stream()

        if (trimEntries) {
            columnStream = columnStream.map { it.trim() }
        }

        if (emptyStringsAsNull) {
            columnStream = columnStream.map { it.ifEmpty { null } }
        }

        return columnStream.collect(Collectors.toList())
    }

    fun withSeparator(separator: Char): CsvLineParser {
        return copy(separator = separator)
    }

    fun withDelimiter(delimiter: Char): CsvLineParser {
        return copy(delimiter = delimiter)
    }

    fun withTrimEntries(trim: Boolean): CsvLineParser {
        return copy(trimEntries = trim)
    }

    fun withSkipEmptyLines(skip: Boolean): CsvLineParser {
        return copy(skipEmptyLines = skip)
    }

    fun withEmptyStringsAsNull(emptyAsNulls: Boolean): CsvLineParser {
        return copy(emptyStringsAsNull = emptyAsNulls)
    }

    fun withEncoding(encoding: Charset): CsvLineParser {
        return copy(encoding = encoding)
    }
}
