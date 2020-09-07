package com.kheops.csv.reader

import com.kheops.csv.CsvProperty
import com.kheops.csv.reader.reflect.CsvReflectionCreator
import com.kheops.csv.reader.reflect.converters.convertToType
import java.time.Instant
import java.util.stream.Collectors.toList
import java.util.stream.Stream
import kotlin.reflect.jvm.javaField

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
                errors = emptyList(),
                line = it.line
            )
        }
    }
}

data class TypedCsvLine<T>(
    val result: T?,
    val line: Int,
    val errors: List<CsvErrorType>? = null
) {
    val hasErrors: Boolean get() = !errors.isNullOrEmpty()

    fun getResultOrThrow(): T {
        return if (hasErrors) throw CsvParsingException(
            message = "error while parsing CSV file",
            errors = errors!!,
            line = line
        ) else result ?: throw CsvParsingException(
            message = "expected a non null CSV result",
            errors = listOf(CsvErrorType.NON_NULL_RESULT_EXPECTED),
            line = line
        )
    }
}

fun main() {
    val t: Instant = convertToType(Instant.now().toString(), Instant::class.java)
    println(t)
    val ty = ArrayList<String>()

    val resu: List<Instant> =
        convertToType(" ${Instant.now()}, ${Instant.now().minusMillis(1000)} ", List::class.java)
    println(resu)
    //val resu2 = convert("123", String::class.java)
    //println(resu2)
    val res = TypedCsvReader(Test::class.java).read(listOf("""a,b,"c",d""", """e,f,g,h,"","a ", sdsdfsd """).stream())
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
    val l: List<Instant>
)