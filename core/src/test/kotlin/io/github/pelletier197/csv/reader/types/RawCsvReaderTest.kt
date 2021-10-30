package io.github.pelletier197.csv.reader.types

import io.github.pelletier197.csv.reader.CsvReader
import io.github.pelletier197.csv.reader.deleteTestFile
import io.github.pelletier197.csv.reader.filePath
import io.github.pelletier197.csv.reader.parser.RawCsvLine
import io.github.pelletier197.csv.reader.writeTestFile
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import java.io.File
import java.nio.file.Paths
import kotlin.streams.toList

class RawCsvReaderTest : ShouldSpec({
    val underTest = CsvReader().rawReader()

    context("on a regular CSV") {
        val csv =
            """
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

        context("parsing CSV from input stream") {
            should("parse and return all CSV lines") {
                underTest.read(csv.byteInputStream()).toList().shouldContainExactly(expectedLines)
            }
        }

        context("parsing CSV from path") {
            beforeTest { writeTestFile(csv) }

            should("parse and return all CSV lines") {
                underTest.read(Paths.get(filePath)).toList().shouldContainExactly(expectedLines)
            }
        }

        context("parsing CSV from file") {
            beforeTest { writeTestFile(csv) }

            should("parse and return all CSV lines") {
                underTest.read(File(filePath)).toList().shouldContainExactly(expectedLines)
            }
        }

        context("parsing CSV from URL") {
            beforeTest { writeTestFile(csv) }

            should("parse and return all CSV lines") {
                @Suppress("BlockingMethodInNonBlockingContext")
                underTest.read(Paths.get(filePath).toUri().toURL()).toList().shouldContainExactly(expectedLines)
            }
        }
    }

    context("when setting custom separator") {
        val csv =
            """
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
                underTest.withSeparator(';').read(csv).toList().shouldContainExactly(
                    RawCsvLine(
                        columns = listOf("a", "b", "c,4"),
                        line = 1
                    )
                )
            }
        }
    }

    context("when setting custom column delimiter") {
        val csv =
            """
                a,"b, c",'c, d'
            """.trimIndent()
        context("with default delimiter") {
            should("use default delimiter for quotes") {
                underTest.read(csv).toList().shouldContainExactly(
                    RawCsvLine(
                        columns = listOf("a", "b, c", "'c", " d'"),
                        line = 1
                    )
                )
            }
        }

        context("with custom delimiter") {
            should("use only custom delimiter to ") {
                underTest.withDelimiter('\'').read(csv).toList().shouldContainExactly(
                    RawCsvLine(
                        columns = listOf("a", "\"b", " c\"", "c, d"),
                        line = 1
                    )
                )
            }
        }
    }

    context("when trimming whitespaces") {
        val csv =
            """
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

        context("activating whitespace trimming") {
            should("trim whitespaces") {
                underTest.withTrimEntries(true).read(csv).toList().shouldContainExactly(
                    RawCsvLine(
                        columns = listOf("a", "b", "c"),
                        line = 1
                    )
                )
            }
        }
    }

    context("when parsing empty strings as null") {
        val csv =
            """
            a,,c
            """.trimIndent()

        context("with default setting") {
            should("return empty strings as empty") {
                underTest.read(csv).toList().shouldContainExactly(
                    RawCsvLine(
                        columns = listOf("a", "", "c"),
                        line = 1
                    )
                )
            }
        }
        context("when activating empty strings as null") {
            should("return empty strings as null") {
                underTest.withEmptyStringsAsNull(true).read(csv).toList().shouldContainExactly(
                    RawCsvLine(
                        columns = listOf("a", null, "c"),
                        line = 1
                    )
                )
            }
        }
    }

    context("when skipping empty lines") {
        val csv =
            """
            
            a,b,c
            
            """.trimIndent()
        context("with default settings") {
            should("skip empty lines") {
                underTest.read(csv).toList().shouldContainExactly(
                    RawCsvLine(
                        columns = listOf("a", "b", "c"),
                        line = 1
                    )
                )
            }
        }
        context("when deactivating empty lines skipping") {
            should("return empty lines results") {
                underTest.withSkipEmptyLines(false).read(csv).toList().shouldContainExactly(
                    RawCsvLine(
                        columns = emptyList(),
                        line = 1
                    ),
                    RawCsvLine(
                        columns = listOf("a", "b", "c"),
                        line = 2
                    ),
                    RawCsvLine(
                        columns = emptyList(),
                        line = 3
                    )
                )
            }
        }
    }

    afterSpec { deleteTestFile() }
})
