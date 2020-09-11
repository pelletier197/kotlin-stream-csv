package com.kheops.csv.reader.types

import com.kheops.csv.reader.CsvReader
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList

private const val filePath = "test.csv"

class RawCsvReaderTest : ShouldSpec({
    val underTest = CsvReader().reader()

    context("with default parameters") {
        context("on a regular CSV") {
            val csv = """
                a,b,c
                1,2,3
            """.trimIndent()
            val expectedLines = listOf(
                RawCsvLine(
                    columns = listOf("a", "b", "c"),
                    line = 1
                ),
                RawCsvLine(
                    columns = listOf("1", "2", "3"),
                    line = 2
                )
            )

            context("parsing CSV from string") {
                should("parse and return all CSV lines") {
                    underTest.read(csv).toList().shouldContainExactly(expectedLines)

                }
            }

            context("parsing CSV from lines list") {
                should("parse and return all CSV lines") {
                    underTest.read(csv.lines()).toList().shouldContainExactly(expectedLines)
                }
            }

            context("parsing CSV from lines stream") {
                should("parse and return all CSV lines") {
                    underTest.read(csv.lines().stream()).toList().shouldContainExactly(expectedLines)
                }
            }

            context("parsing CSV from input stream") {
                should("parse and return all CSV lines") {
                    underTest.read(csv.byteInputStream()).toList().shouldContainExactly(expectedLines)
                }
            }

            context("parsing CSV from path") {
                beforeTest { writeFile(csv) }

                should("parse and return all CSV lines") {
                    underTest.read(Paths.get(filePath)).toList().shouldContainExactly(expectedLines)
                }
            }

            context("parsing CSV from file") {
                beforeTest { writeFile(csv) }

                should("parse and return all CSV lines") {
                    underTest.read(File(filePath)).toList().shouldContainExactly(expectedLines)
                }
            }

            context("parsing CSV from URL") {
                beforeTest { writeFile(csv) }

                should("parse and return all CSV lines") {
                    @Suppress("BlockingMethodInNonBlockingContext")
                    underTest.read(Paths.get(filePath).toUri().toURL()).toList().shouldContainExactly(expectedLines)
                }
            }
        }

        context("when setting custom separator") {
            val csv = """
                a;b;c,4
            """.trimIndent()
            context("with custom separator") {
                context("with default separator") {
                    should("use default separator to split") {
                        underTest.read(csv).toList().shouldContainExactly(
                            RawCsvLine(
                                columns = listOf("a;b;c", "4"),
                                line = 1
                            )
                        )
                    }
                }

                should("use only the custom separator to split") {
                    underTest.withSeparator(";").read(csv).toList().shouldContainExactly(
                        RawCsvLine(
                            columns = listOf("a", "b", "c,4"),
                            line = 1
                        )
                    )
                }
            }
        }

        context("when setting custom column delimiter") {
            val csv = """
                a,"b, c",'c, d'
            """.trimIndent()
            context("with default delimiter") {
                should("use default delimiter for quotes") {
                    underTest.read(csv).toList().shouldContainExactly(
                        RawCsvLine(
                            columns = listOf("a", "b, c", "'c",  " d'"),
                            line = 1
                        )
                    )
                }
            }

            context("with custom delimiter") {
                should("use only custom delimiter to ") {
                    underTest.withDelimiter("'").read(csv).toList().shouldContainExactly(
                        RawCsvLine(
                            columns = listOf("a", "\"b", " c\"", "c, d"),
                            line = 1
                        )
                    )
                }
            }
        }

        context("when trimming whitespaces") {
            val csv = """
                a  , b , c
            """.trimIndent()
            context("with default setting") {
                should("not trim whitespaces") {
                    underTest.read(csv).toList().shouldContainExactly(
                        RawCsvLine(
                            columns = listOf("a  ", " b ", " c"),
                            line = 1
                        )
                    )
                }
            }
        }

    }

})

private fun writeFile(content: String) {
    Files.writeString(Paths.get(filePath), content)
}
