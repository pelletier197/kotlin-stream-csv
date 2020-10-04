package com.kheops.csv.reader

import com.kheops.csv.reader.types.HeaderCsvReader
import com.kheops.csv.reader.types.RawCsvReader
import com.kheops.csv.reader.types.TypedCsvReader

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