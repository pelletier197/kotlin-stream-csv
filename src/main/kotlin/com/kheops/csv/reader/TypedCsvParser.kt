package com.kheops.csv.reader

import com.kheops.csv.reader.reflect.CsvReflectionCreator
import com.kheops.csv.reader.reflect.InstantiationError
import com.kheops.csv.reader.reflect.converters.ConversionSettings
import java.util.stream.Stream


class TypedCsvReader<T>(
    val targetClass: Class<T>,
    val listSeparator: String = ",",
    val csvReflectionCreator: CsvReflectionCreator<T> = CsvReflectionCreator(targetClass),
    val headerCsvReader: HeaderCsvReader = HeaderCsvReader()
) {
    private val conversionSettings = ConversionSettings(
        listSeparator = listSeparator
    )

    fun read(lines: Stream<String>): Stream<TypedCsvLine<T>> {
        return readHeaderLines(headerCsvReader.read(lines))
    }

    fun readHeaderLines(lines: Stream<HeaderCsvLine>): Stream<TypedCsvLine<T>> {
        return lines.map {
            val mappedInstance = csvReflectionCreator.createCsvInstance(
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
    val errors: List<CsvError>? = null
) {
    val hasErrors: Boolean get() = !errors.isNullOrEmpty()

    fun getResultOrThrow(): T {
        return if (hasErrors) throw CsvParsingException(
            message = "error while parsing CSV file",
            errors = errors!!,
            line = line
        ) else result ?: error("unexpected null result for an entity with no error")
    }
}