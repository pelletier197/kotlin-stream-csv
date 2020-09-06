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



data class CsvLine<T>(
    val result: T?,
    val line: Int,
    val errors: List<CsvError>? = null
) {
    val hasErrors: Boolean get() = !errors.isNullOrEmpty()

    fun getResultOrThrow(): T {
        return if (hasErrors) throw CsvParsingException(
            message = "error while parsing CSV file",
            errors = errors!!,
            line = line
        ) else result ?: throw CsvParsingException(
            message = "expected a non null CSV result",
            errors = listOf(CsvError.NON_NULL_RESULT_EXPECTED),
            line = line
        )
    }
}

fun main() {
    val res = RawCsvReader().read(listOf("""a,b,"c",d""", """r,e,e,e,"","a ", sdsdfsd """).stream())
    println(res.collect(toList()))
}