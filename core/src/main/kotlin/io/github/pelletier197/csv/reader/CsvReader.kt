package io.github.pelletier197.csv.reader

import io.github.pelletier197.csv.reader.types.HeaderCsvReader
import io.github.pelletier197.csv.reader.types.RawCsvReader
import io.github.pelletier197.csv.reader.types.TypedCsvReader

class CsvReader {
    inline fun <reified T> readerForType(): TypedCsvReader<T> {
        return readerForType(T::class.java)
    }

    fun <T> readerForType(targetClass: Class<T>): TypedCsvReader<T> {
        return TypedCsvReader(targetClass = targetClass)
    }

    fun readerWithHeader(): HeaderCsvReader {
        return HeaderCsvReader()
    }

    fun reader(): RawCsvReader {
        return RawCsvReader()
    }
}
