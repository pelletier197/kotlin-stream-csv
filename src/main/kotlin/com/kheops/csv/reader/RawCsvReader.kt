package com.kheops.csv.reader

import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Stream


data class RawCsvLine(
    val columns: List<String?>,
    val line: Int
)

class RawCsvReader(
    val separator: String = ",",
    val delimiter: String = "\"",
    val trimEntries: Boolean = false,
    val skipEmptyLines: Boolean = true,
    val emptyStringsAsNull: Boolean = false
) {
    private val regex = buildSplitRegex()

    fun read(url: URL, charset: Charset = StandardCharsets.UTF_8): Stream<RawCsvLine> {
        return read(url.openStream(), charset)
    }

    fun read(input: InputStream, charset: Charset = StandardCharsets.UTF_8): Stream<RawCsvLine> {
        return read(BufferedReader(InputStreamReader(input, charset)).lines())
    }

    fun read(file: File): Stream<RawCsvLine> {
        return read(file.toPath())
    }

    fun read(path: Path): Stream<RawCsvLine> {
        return read(Files.lines(path))
    }

    fun read(lines: List<String>): Stream<RawCsvLine> {
        return read(lines.stream())
    }

    fun read(lines: Stream<String>): Stream<RawCsvLine> {
        val skippedEmptyLines = if (skipEmptyLines) lines.filter { it.isNotBlank() } else lines
        return processLines(skippedEmptyLines)
    }

    private fun processLines(
        lines: Stream<String>,
    ): Stream<RawCsvLine> {
        val index = AtomicInteger(1)
        return lines.map {
            RawCsvLine(
                columns = regex.findAll(it).toList().map { v -> format(v.groupValues.last()) },
                line = index.getAndIncrement(),
            )
        }
    }

    private fun format(value: String): String? {
        var output = removeDelimiters(value)

        if (trimEntries) {
            output = output.trim()
        }

        if (emptyStringsAsNull && output.isEmpty()) {
            return null
        }

        return output
    }

    private fun removeDelimiters(value: String): String {
        val trimmed = value.trim()
        if (trimmed.startsWith(delimiter) && trimmed.endsWith(delimiter)) {
            return trimmed.drop(1).dropLast(1)
        }
        return trimmed
    }

    private fun buildSplitRegex(): Regex {
        val s = separator
        val d = delimiter
        // https://stackoverflow.com/questions/18144431/regex-to-split-a-csv/18147076
        return Regex("(?:$s|\\n|^)($d(?:(?:$d$d)*[^$d]*)*$d|[^$d$s\\n]*|(?:\\n|\$))")
    }
}
