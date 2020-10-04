package com.kheops.csv.reader.types

import com.kheops.csv.reader.deleteTestFile
import com.kheops.csv.reader.filePath
import com.kheops.csv.reader.writeTestFile
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Paths
import kotlin.streams.toList

class HeaderCsvReaderTest : ShouldSpec({
    val underTest = HeaderCsvReader()

    context("a request to set raw reader parameters") {
        context("configuring separator") {
            should("update raw reader with given separator") {
                underTest.withSeparator('/').shouldBe(HeaderCsvReader(reader = RawCsvReader(separator = '/')))
            }
        }
        context("configuring delimiter") {
            should("update raw reader with given delimiter") {
                underTest.withDelimiter('@').shouldBe(HeaderCsvReader(reader = RawCsvReader(delimiter = '@')))
            }
        }
        context("setting trim entries parameter") {
            should("update raw reader with given parameter") {
                listOf(false, true).forEach {
                    underTest.withTrimEntries(it).shouldBe(HeaderCsvReader(reader = RawCsvReader(trimEntries = it)))
                }
            }
        }
        context("setting skip empty lines parameter") {
            should("update raw reader with given parameter") {
                listOf(false, true).forEach {
                    underTest.withSkipEmptyLines(it)
                        .shouldBe(HeaderCsvReader(reader = RawCsvReader(skipEmptyLines = it)))
                }
            }
        }
        context("setting empty strings as null parameter") {
            should("update raw reader with given parameter") {
                listOf(false, true).forEach {
                    underTest.withEmptyStringsAsNull(it)
                        .shouldBe(HeaderCsvReader(reader = RawCsvReader(emptyStringsAsNull = it)))
                }
            }
        }
    }

    context("on a regular CSV") {
        val csv = """
                a,b,c
                1,2,3
            """.trimIndent()
        val expectedLines = listOf(
            HeaderCsvLine(
                values = mapOf(
                    "a" to "1",
                    "b" to "2",
                    "c" to "3",
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

    context("different header provisioning") {
        val csv = """
            a1,b1,c1
            12,14,67
        """.trimIndent()
        context("header is provided") {
            val underTestWithHeader = underTest.withHeader(listOf("c1", "c2", "c3"))
            val expectedLinesWithHeader = listOf(
                HeaderCsvLine(
                    values = mapOf(
                        "c1" to "a1",
                        "c2" to "b1",
                        "c3" to "c1",
                    ),
                    line = 1
                ),
                HeaderCsvLine(
                    values = mapOf(
                        "c1" to "12",
                        "c2" to "14",
                        "c3" to "67",
                    ),
                    line = 2
                )
            )

            should("use provided header") {
                underTestWithHeader.read(csv).toList().shouldBe(expectedLinesWithHeader)
            }
        }

        context("header is not provided") {
            val expectedLines = listOf(
                HeaderCsvLine(
                    values = mapOf(
                        "a1" to "12",
                        "b1" to "14",
                        "c1" to "67",
                    ),
                    line = 2
                )
            )
            should("use first non empty line as the header provider") {
                underTest.read(csv).toList().shouldBe(expectedLines)
            }
        }
    }

    afterSpec { deleteTestFile() }
})