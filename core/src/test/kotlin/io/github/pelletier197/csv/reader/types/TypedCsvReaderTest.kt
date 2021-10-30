package io.github.pelletier197.csv.reader.types

import io.github.pelletier197.csv.reader.CsvError
import io.github.pelletier197.csv.reader.CsvErrorType
import io.github.pelletier197.csv.reader.CsvReader
import io.github.pelletier197.csv.reader.deleteTestFile
import io.github.pelletier197.csv.reader.filePath
import io.github.pelletier197.csv.reader.parser.CsvLineParser
import io.github.pelletier197.csv.reader.reflect.converters.ConversionParameters
import io.github.pelletier197.csv.reader.reflect.converters.Converter
import io.github.pelletier197.csv.reader.writeTestFile
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.shouldBe
import java.io.File
import java.lang.reflect.Type
import java.nio.file.Paths
import java.time.Instant
import kotlin.streams.toList

data class TestClass(
    val str: String,
    val int: Int,
    val list: List<String>,
    val instant: Instant
)

class TypedCsvReaderTest : ShouldSpec({
    val underTest = CsvReader().readerForType<TestClass>()

    afterSpec { deleteTestFile() }

    context("a request to set header reader parameters") {
        context("configuring separator") {
            should("update raw reader with given separator") {
                underTest.withSeparator('/')
                    .shouldBe(
                        TypedCsvReader(
                            targetClass = TestClass::class.java,
                            reader = HeaderCsvReader(reader = RawCsvReader(parser = CsvLineParser(separator = '/')))
                        )
                    )
            }
        }
        context("configuring delimiter") {
            should("update raw reader with given delimiter") {
                underTest.withDelimiter('@').shouldBe(
                    TypedCsvReader(
                        targetClass = TestClass::class.java,
                        reader = HeaderCsvReader(reader = RawCsvReader(parser = CsvLineParser(delimiter = '@')))
                    )
                )
            }
        }
        context("setting trim entries parameter") {
            should("update raw reader with given parameter") {
                listOf(false, true).forEach {
                    underTest.withTrimEntries(it).shouldBe(
                        TypedCsvReader(
                            targetClass = TestClass::class.java,
                            reader = HeaderCsvReader(reader = RawCsvReader(parser = CsvLineParser(trimEntries = it)))
                        )
                    )
                }
            }
        }
        context("setting skip empty lines parameter") {
            should("update raw reader with given parameter") {
                listOf(false, true).forEach {
                    underTest.withSkipEmptyLines(it)
                        .shouldBe(
                            TypedCsvReader(
                                targetClass = TestClass::class.java,
                                reader = HeaderCsvReader(reader = RawCsvReader(parser = CsvLineParser(skipEmptyLines = it)))
                            )
                        )
                }
            }
        }
        context("setting empty strings as null parameter") {
            should("update raw reader with given parameter") {
                listOf(false, true).forEach {
                    underTest.withEmptyStringsAsNull(it)
                        .shouldBe(
                            TypedCsvReader(
                                targetClass = TestClass::class.java,
                                reader = HeaderCsvReader(reader = RawCsvReader(parser = CsvLineParser(emptyStringsAsNull = it)))
                            )
                        )
                }
            }
        }
        context("configuration the header of the reader") {
            should("update header reader with given parameter") {
                val header = listOf("test", "header_2")
                underTest.withHeader(header)
                    .shouldBe(
                        TypedCsvReader(
                            targetClass = TestClass::class.java,
                            reader = HeaderCsvReader(header = header)
                        )
                    )
            }
        }
    }

    context("on a regular CSV") {
        val csv =
            """
                str,int,list,instant
                abc,12,"a,b,c",2019-03-05T05:12:34.000Z
            """.trimIndent()
        val expectedLines = listOf(
            TypedCsvLine(
                result = TestClass(
                    str = "abc",
                    int = 12,
                    list = listOf("a", "b", "c"),
                    instant = Instant.parse("2019-03-05T05:12:34.000Z")
                ),
                line = 2
            )
        )

        context("parsing CSV from string") {
            should("parse and return all CSV lines with their header") {
                underTest.read(csv).toList().shouldContainExactly(expectedLines)
            }
        }

        context("parsing CSV from lines list") {
            should("parse and return all CSV lines with their header") {
                underTest.read(csv.lines()).toList().shouldContainExactly(expectedLines)
            }
        }

        context("parsing CSV from input stream") {
            should("parse and return all CSV lines with their header") {
                underTest.read(csv.byteInputStream()).toList().shouldContainExactly(expectedLines)
            }
        }

        context("parsing CSV from path") {
            beforeTest { writeTestFile(csv) }

            should("parse and return all CSV lines with their header") {
                underTest.read(Paths.get(filePath)).toList().shouldContainExactly(expectedLines)
            }
        }

        context("parsing CSV from file") {
            beforeTest { writeTestFile(csv) }

            should("parse and return all CSV lines with their header") {
                underTest.read(File(filePath)).toList().shouldContainExactly(expectedLines)
            }
        }

        context("parsing CSV from URL") {
            beforeTest { writeTestFile(csv) }

            should("parse and return all CSV lines with their header") {
                @Suppress("BlockingMethodInNonBlockingContext")
                underTest.read(Paths.get(filePath).toUri().toURL()).toList().shouldContainExactly(expectedLines)
            }
        }
    }

    context("settings custom list separator") {
        val csv =
            """
                str,int,list,instant
                abc,12,"a;b,c",2019-03-05T05:12:34.000Z
            """.trimIndent()

        context("using default list separator") {
            val expectedLines = listOf(
                TypedCsvLine(
                    result = TestClass(
                        str = "abc",
                        int = 12,
                        list = listOf("a;b", "c"),
                        instant = Instant.parse("2019-03-05T05:12:34.000Z")
                    ),
                    line = 2
                )
            )
            should("use coma as list separator") {
                underTest.read(csv).toList().shouldBe(expectedLines)
            }
        }
        context("using custom list separator") {
            val expectedLines = listOf(
                TypedCsvLine(
                    result = TestClass(
                        str = "abc",
                        int = 12,
                        list = listOf("a", "b,c"),
                        instant = Instant.parse("2019-03-05T05:12:34.000Z")
                    ),
                    line = 2
                )
            )
            should("use custom separator as list separator") {
                underTest.withListSeparator(';').read(csv).toList().shouldBe(expectedLines)
            }
        }
    }

    context("using a custom converter") {
        data class CustomType(
            val value: String
        )

        data class TestClassCustomConverter(
            val test: String,
            val custom: CustomType
        )

        val underTestCustomConverter = TypedCsvReader(TestClassCustomConverter::class.java)

        val csv =
            """
            test,custom
            test string, custom converter test
            """.trimIndent()

        context("without the custom converter") {
            val expectedLine = TypedCsvLine<TestClassCustomConverter>(
                result = null,
                line = 2,
                errors = listOf(
                    CsvError(
                        csvField = "custom",
                        classField = "custom",
                        providedValue = " custom converter test",
                        type = CsvErrorType.NO_CONVERTER_FOUND_FOR_VALUE,
                        cause = null
                    )
                )
            )

            should("return a csv line with errors") {
                val results = underTestCustomConverter.read(csv).toList()
                verifyErrorTypedLineEqualsExpected(results, expectedLine)
            }
        }

        context("providing a custom converter") {
            class CustomConverter : Converter<String, CustomType> {
                override val source: Class<String> get() = String::class.java
                override val target: Class<CustomType> get() = CustomType::class.java
                override fun convert(value: String, to: Type, parameters: ConversionParameters): CustomType {
                    return CustomType(value)
                }
            }

            val customConverter = CustomConverter()
            val expectedLine = TypedCsvLine(
                result = TestClassCustomConverter(
                    test = "test string",
                    custom = CustomType(" custom converter test"),
                ),
                line = 2,
                errors = emptyList()
            )
            context("provided as a single converter") {
                should("convert using the custom converter") {
                    underTestCustomConverter.withConverter(customConverter).read(csv).toList()
                        .shouldContainExactly(expectedLine)
                }
            }

            context("provided as a converter list") {
                should("convert using the custom converter") {
                    underTestCustomConverter.withConverters(listOf(customConverter)).read(csv).toList()
                        .shouldContainExactly(expectedLine)
                }
            }
        }

        context("clearing converters") {
            data class TestClassNoConverters(
                val test: Int
            )

            val underTestNoConverter = TypedCsvReader(TestClassNoConverters::class.java)
            val csvNoConverter =
                """
                    test
                    12
                """.trimIndent()

            context("with cleared converters") {
                val expectedLineClearedConverters = TypedCsvLine<TestClassNoConverters>(
                    result = null,
                    line = 2,
                    errors = listOf(
                        CsvError(
                            csvField = "test",
                            classField = "test",
                            providedValue = "12",
                            type = CsvErrorType.NO_CONVERTER_FOUND_FOR_VALUE,
                            cause = null
                        )
                    )
                )

                should("fail converting value") {
                    val results = underTestNoConverter.withClearedConverters().read(csvNoConverter).toList()
                    verifyErrorTypedLineEqualsExpected(results, expectedLineClearedConverters)
                }
            }
        }
    }
})

private fun verifyErrorTypedLineEqualsExpected(results: List<TypedCsvLine<*>>, expected: TypedCsvLine<*>) {
    results.shouldHaveSize(1)

    val line = results[0]
    line.shouldBeEqualToIgnoringFields(expected, TypedCsvLine<*>::errors)

    val errors = line.errors
    errors.shouldHaveSize(1)

    val error = errors[0]
    error.shouldBeEqualToIgnoringFields(expected.errors[0], CsvError::cause)
}
