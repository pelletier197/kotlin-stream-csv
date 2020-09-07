package com.kheops.csv.reader

import com.kheops.csv.CsvProperty
import com.kheops.csv.reader.reflect.converters.convertToClass
import com.kheops.csv.reader.reflect.converters.convertToType
import java.time.Instant
import java.util.stream.Collectors.toList

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

    fun readerWithHeader(header: List<String>): HeaderCsvReader {
        return HeaderCsvReader(header = header)
    }

    fun reader(): RawCsvReader {
        return RawCsvReader()
    }
}