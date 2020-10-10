package com.kheops.csv.reader.types

import com.kheops.csv.reader.CsvError
import com.kheops.csv.reader.CsvErrorType
import com.kheops.csv.reader.CsvParsingException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.shouldBe

class TypedCsvLineTest : ShouldSpec({
    val result = "Test"
    val underTest = TypedCsvLine<Any>(
        result = result,
        line = 4,
        errors = emptyList()
    )

    context("line does not contain errors") {
        context("getting has error status") {
            should("return there is no error") {
                underTest.hasErrors.shouldBeFalse()
            }
        }
        context("getting result or throw") {
            should("return value") {
                underTest.getResultOrThrow().shouldBe(result)
            }
        }
    }
    context("line contains errors") {
        val underTestWithErrors = underTest.copy(
            result = null,
            errors = listOf(
                CsvError(
                    csvField = "test",
                    classField = "test",
                    providedValue = "a_value",
                    type = CsvErrorType.NO_CONVERTER_FOUND_FOR_VALUE,
                    cause = null
                )
            )
        )
        context("getting has error status") {
            should("return there is no error") {
                underTestWithErrors.hasErrors.shouldBeTrue()
            }
        }
        context("getting result or throw") {
            should("throw") {
                shouldThrow<CsvParsingException> {
                    underTestWithErrors.getResultOrThrow()
                }.shouldBeEqualToIgnoringFields(
                    CsvParsingException(
                        message = "any",
                        errors = underTestWithErrors.errors,
                        line = underTestWithErrors.line
                    ), CsvParsingException::message
                )
            }
        }
    }
})