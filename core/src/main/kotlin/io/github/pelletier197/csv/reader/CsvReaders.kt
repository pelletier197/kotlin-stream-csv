package io.github.pelletier197.csv.reader

import io.github.pelletier197.csv.reader.types.HeaderCsvReader
import io.github.pelletier197.csv.reader.types.RawCsvReader
import io.github.pelletier197.csv.reader.types.TypedCsvReader

object CsvReaders {
    inline fun <reified T> forType(): TypedCsvReader<T> {
        return forType(T::class.java)
    }

    fun <T> forType(targetClass: Class<T>): TypedCsvReader<T> {
        return TypedCsvReader(targetClass = targetClass)
    }

    fun header(): HeaderCsvReader {
        return HeaderCsvReader()
    }

    fun raw(): RawCsvReader {
        return RawCsvReader()
    }
}
