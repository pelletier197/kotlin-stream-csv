package com.kheops.csv.reader

import com.kheops.csv.CsvProperty
import com.kheops.csv.reader.reflect.CsvReflectionCreator
import com.kheops.csv.reader.reflect.InstantiationError
import com.kheops.csv.reader.reflect.converters.convertToClass
import com.kheops.csv.reader.reflect.converters.convertToType
import java.time.Instant
import java.util.stream.Collectors.toList
import java.util.stream.Stream

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

class TypedCsvReader<T>(
    val targetClass: Class<T>,
    val csvReflectionCreator: CsvReflectionCreator<T> = CsvReflectionCreator(targetClass),
    val headerCsvReader: HeaderCsvReader = HeaderCsvReader()
) {
    fun read(lines: Stream<String>): Stream<TypedCsvLine<T>> {
        return readHeaderLines(headerCsvReader.read(lines))
    }

    fun readHeaderLines(lines: Stream<HeaderCsvLine>): Stream<TypedCsvLine<T>> {
        return lines.map {
            val mappedInstance = csvReflectionCreator.createCsvInstance(it.values)
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

fun main() {
    val t: Instant = convertToType(Instant.now().toString(), Instant::class.java)
    println(t)
    val ty = ArrayList<String>()

    val test = convertToClass("TEST", Lolz::class.java)
    val resu =
        convertToClass(" ${Instant.now()}, ${Instant.now().minusMillis(1000)} ", String::class.java)
    println(resu)
    //val resu2 = convert("123", String::class.java)
    //println(resu2)
    val res = TypedCsvReader(Test::class.java).read(listOf("""a,b,"c",d, l""", """e,f,g,h,"","a ", sdsdfsd """).stream())
    println(res.collect(toList()))
}

enum class Lolz {
    TEST
}

data class Test(
    val a: String,
    @CsvProperty("b")
    val second: String?,
    @CsvProperty("c")
    val ce: String,
    val d: String?,
    val l: List<Instant>?
)