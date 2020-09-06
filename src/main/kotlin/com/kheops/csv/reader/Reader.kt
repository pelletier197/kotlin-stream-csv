package com.kheops.csv.reader

import javax.xml.validation.Schema

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

class RawCsvReader {

}

data class HeaderCsvReader(
    val header: List<String>? = null,
    val reader: RawCsvReader = RawCsvReader()
) {
    fun withHeader(header: List<String>): HeaderCsvReader {
        return copy(header = header)
    }
}

class HeaderCsvConfiguration(
    val header: List<String>? = null
) {
}

fun test() {
    val firstReader = CsvReader().reader()
    val secondReader = CsvReader().readerForType<Schema>()
    val third = CsvReader().readerWithHeader(listOf("first", "second"))
}