package com.kheops.csv.reader

import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.net.URL
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors.toList
import java.util.stream.Stream

class CsvReader {
    fun <T> readerForType(): TypedCsvReader<T> {
        return TypedCsvReader()
    }

    fun readerWithHeader(): HeaderCsvReader {
        return HeaderCsvReader()
    }

    fun readerWithHeader(header: List<String>): HeaderCsvReader {
        return HeaderCsvReader(header = header)
    }

    fun reader(): RawCsvReader {
        return RawCsvReader()
    }
}

class TypedCsvReader<T>(
    val headerCsvReader: HeaderCsvReader = HeaderCsvReader()
)


data class TypedCsvSchema<T>(
    val header: List<String>? = null
) {
    fun withHeader(header: List<String>): TypedCsvSchema<T> {
        return copy(header = header)
    }
}

class RawCsvReader(
    val separator: String = ",",
    val delimiter: String = "\"",
    val trimEntries: Boolean = false,
    val skipEmptyLines: Boolean = true
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

    private fun format(value: String): String {
        var output = removeDelimiters(value)

        if (trimEntries) {
            output = output.trim()
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

data class HeaderCsvReader(
    val header: List<String>? = null,
    val errorOnMissingProperties: Boolean = true,
    val reader: RawCsvReader = RawCsvReader(),
) {
    fun withHeader(header: List<String>): HeaderCsvReader {
        return copy(header = header)
    }
}

data class RawCsvLine(
    val columns: List<String>,
    val line: Int
)

data class CsvLine<T>(
    val result: T?,
    val line: Int,
    val errors: List<CsvError>? = null
) {
    val hasErrors: Boolean get() = !errors.isNullOrEmpty()

    fun getResultOrThrow(): T {
        return result ?: throw CsvParsingException(
            message = "error while parsing CSV file",
            errors = errors!!,
            line = line
        )
    }
}

enum class CsvError {
    TEST
}

data class CsvParsingException(
    override val message: String,
    val errors: List<CsvError>,
    val line: Int
) : Exception(message)

fun main() {
    val res = RawCsvReader().read(listOf("""a,b,"c",d""", """r,e,e,e,"","a ", sdsdfsd """).stream())
    println(res.collect(toList()))
}