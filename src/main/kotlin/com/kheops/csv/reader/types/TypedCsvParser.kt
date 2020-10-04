package com.kheops.csv.reader.types

import com.kheops.csv.reader.CsvError
import com.kheops.csv.reader.CsvErrorType
import com.kheops.csv.reader.CsvParsingException
import com.kheops.csv.reader.reflect.CsvReflectionCreator
import com.kheops.csv.reader.reflect.InstantiationError
import com.kheops.csv.reader.reflect.converters.ConversionSettings
import com.kheops.csv.reader.reflect.converters.Converter
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.stream.Stream


data class TypedCsvReader<T>(
    val targetClass: Class<T>,
    val listSeparator: Char = ',',
    private val creator: CsvReflectionCreator<T> = CsvReflectionCreator(targetClass),
    private val reader: HeaderCsvReader = HeaderCsvReader()
) {
    private val conversionSettings = ConversionSettings(
        listSeparator = listSeparator
    )

    fun withSeparator(separator: Char): TypedCsvReader<T> {
        return copy(reader = reader.withSeparator(separator))
    }

    fun withDelimiter(delimiter: Char): TypedCsvReader<T> {
        return copy(reader = reader.withDelimiter(delimiter))
    }

    fun withTrimEntries(trim: Boolean): TypedCsvReader<T> {
        return copy(reader = reader.withTrimEntries(trim))
    }

    fun withSkipEmptyLines(skip: Boolean): TypedCsvReader<T> {
        return copy(reader = reader.withSkipEmptyLines(skip))
    }

    fun withEmptyStringsAsNull(emptyAsNulls: Boolean): TypedCsvReader<T> {
        return copy(reader = reader.withEmptyStringsAsNull(emptyAsNulls))
    }

    fun withListSeparator(listSeparator: Char): TypedCsvReader<T> {
        return copy(listSeparator = listSeparator)
    }

    fun withConverter(newConverter: Converter<*, *>): TypedCsvReader<T> {
        return copy(creator = creator.withConverter(newConverter))
    }

    fun withConverters(newConverters: List<Converter<*, *>>): TypedCsvReader<T> {
        return copy(creator = creator.withConverters(newConverters))
    }

    fun withClearedConverters(): TypedCsvReader<T> {
        return copy(creator = creator.withClearedConverters())
    }

    fun read(url: URL, charset: Charset = StandardCharsets.UTF_8): Stream<TypedCsvLine<T>> {
        return readHeaderLines(reader.read(url, charset))
    }

    fun read(input: InputStream, charset: Charset = StandardCharsets.UTF_8): Stream<TypedCsvLine<T>> {
        return readHeaderLines(reader.read(input, charset))
    }

    fun read(file: File): Stream<TypedCsvLine<T>> {
        return readHeaderLines(reader.read(file))
    }

    fun read(path: Path): Stream<TypedCsvLine<T>> {
        return readHeaderLines(reader.read(path))
    }

    fun read(lines: List<String>): Stream<TypedCsvLine<T>> {
        return readHeaderLines(reader.read(lines))
    }

    fun read(value: String): Stream<TypedCsvLine<T>> {
        return readHeaderLines(reader.read(value))
    }

    fun read(lines: Stream<String>): Stream<TypedCsvLine<T>> {
        return readHeaderLines(reader.read(lines))
    }

    private fun readHeaderLines(lines: Stream<HeaderCsvLine>): Stream<TypedCsvLine<T>> {
        return lines.map {
            val mappedInstance = creator.createCsvInstance(
                csvHeadersValues = it.values, settings = conversionSettings
            )
            TypedCsvLine(
                result = mappedInstance.result,
                errors = mappedInstance.errors.map { error -> mapInstantiationError(error) },
                line = it.line
            )
        }
    }

    private fun mapInstantiationError(error: InstantiationError): CsvError {
        return CsvError(
            csvField = error.originalField,
            classField = error.field,
            type = CsvErrorType.valueOf(error.type.toString()),
            cause = error.cause
        )
    }
}

data class TypedCsvLine<T>(
    val result: T?,
    val line: Int,
    val errors: List<CsvError> = emptyList()
) {
    val hasErrors: Boolean get() = errors.isNotEmpty()

    fun getResultOrThrow(): T {
        return if (hasErrors) throw CsvParsingException(
            message = "error while parsing CSV file",
            errors = errors,
            line = line
        ) else result ?: error("unexpected null result for an entity with no error")
    }
}