package io.github.pelletier197.csv.reader.types

import io.github.pelletier197.csv.reader.CsvError
import io.github.pelletier197.csv.reader.CsvErrorType
import io.github.pelletier197.csv.reader.CsvLine
import io.github.pelletier197.csv.reader.CsvParsingException
import io.github.pelletier197.csv.reader.CsvReader
import io.github.pelletier197.csv.reader.HeaderCsvReaderConfigurer
import io.github.pelletier197.csv.reader.reflect.CsvReflectionCreator
import io.github.pelletier197.csv.reader.reflect.InstantiationError
import io.github.pelletier197.csv.reader.reflect.converters.ConversionSettings
import io.github.pelletier197.csv.reader.reflect.converters.Converter
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.charset.Charset
import java.nio.file.Path
import java.util.stream.Stream

data class TypedCsvReader<T>(
    private val targetClass: Class<T>,
    val listSeparator: Char = ',',
    private val creator: CsvReflectionCreator<T> = CsvReflectionCreator(targetClass),
    private val reader: HeaderCsvReader = HeaderCsvReader()
) : CsvReader<TypedCsvLine<T>>, HeaderCsvReaderConfigurer<TypedCsvReader<T>> {
    private val conversionSettings = ConversionSettings(
        listSeparator = listSeparator
    )

    override fun withSeparator(separator: Char): TypedCsvReader<T> {
        return copy(reader = reader.withSeparator(separator))
    }

    override fun withDelimiter(delimiter: Char): TypedCsvReader<T> {
        return copy(reader = reader.withDelimiter(delimiter))
    }

    override fun withTrimEntries(trim: Boolean): TypedCsvReader<T> {
        return copy(reader = reader.withTrimEntries(trim))
    }

    override fun withSkipEmptyLines(skip: Boolean): TypedCsvReader<T> {
        return copy(reader = reader.withSkipEmptyLines(skip))
    }

    override fun withEmptyStringsAsNull(emptyAsNulls: Boolean): TypedCsvReader<T> {
        return copy(reader = reader.withEmptyStringsAsNull(emptyAsNulls))
    }

    override fun withEncoding(encoding: Charset): TypedCsvReader<T> {
        return copy(reader = reader.withEncoding(encoding))
    }

    override fun withHeader(header: List<String>): TypedCsvReader<T> {
        return copy(reader = reader.withHeader(header))
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

    override fun read(url: URL): Stream<TypedCsvLine<T>> {
        return readHeaderLines(reader.read(url))
    }

    override fun read(input: InputStream): Stream<TypedCsvLine<T>> {
        return readHeaderLines(reader.read(input))
    }

    override fun read(file: File): Stream<TypedCsvLine<T>> {
        return readHeaderLines(reader.read(file))
    }

    override fun read(path: Path): Stream<TypedCsvLine<T>> {
        return readHeaderLines(reader.read(path))
    }

    override fun read(lines: List<String>): Stream<TypedCsvLine<T>> {
        return readHeaderLines(reader.read(lines))
    }

    override fun read(value: String): Stream<TypedCsvLine<T>> {
        return readHeaderLines(reader.read(value))
    }

    private fun readHeaderLines(lines: Stream<HeaderCsvLine>): Stream<TypedCsvLine<T>> {
        return lines.map {
            val mappedInstance = creator.createCsvInstance(
                csvHeadersValues = it.values,
                settings = conversionSettings
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
            providedValue = error.providedValue,
            type = CsvErrorType.valueOf(error.type.toString()),
            cause = error.cause
        )
    }
}

data class TypedCsvLine<T>(
    val result: T?,
    override val line: Int,
    val errors: List<CsvError> = emptyList()
) : CsvLine {
    val hasErrors: Boolean get() = errors.isNotEmpty()

    fun getResultOrThrow(): T {
        return if (hasErrors) throw CsvParsingException(
            message = "error while parsing CSV file",
            errors = errors,
            line = line
        ) else result ?: error("unexpected null result for an entity with no error")
    }
}
