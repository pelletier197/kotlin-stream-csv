package com.kheops.csv.reader.types

import com.kheops.csv.reader.filePath
import com.kheops.csv.reader.writeTestFile
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.io.File
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
    val underTest = TypedCsvReader(TestClass::class.java)

    context("a request to set header reader parameters") {
        context("configuring separator") {
            should("update raw reader with given separator") {
                underTest.withSeparator('/')
                    .shouldBe(
                        TypedCsvReader(
                            targetClass = TestClass::class.java,
                            reader = HeaderCsvReader(reader = RawCsvReader(separator = '/'))
                        )
                    )
            }
        }
        context("configuring delimiter") {
            should("update raw reader with given delimiter") {
                underTest.withDelimiter('@').shouldBe(
                    TypedCsvReader(
                        targetClass = TestClass::class.java,
                        reader = HeaderCsvReader(reader = RawCsvReader(delimiter = '@'))
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
                            reader = HeaderCsvReader(reader = RawCsvReader(trimEntries = it))
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
                                reader = HeaderCsvReader(reader = RawCsvReader(skipEmptyLines = it))
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
                                reader = HeaderCsvReader(reader = RawCsvReader(emptyStringsAsNull = it))
                            )
                        )
                }
            }
        }
    }

    context("on a regular CSV") {
        val csv = """
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

        context("parsing CSV from lines stream") {
            should("parse and return all CSV lines with their header") {
                underTest.read(csv.lines().stream()).toList().shouldContainExactly(expectedLines)
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
})

