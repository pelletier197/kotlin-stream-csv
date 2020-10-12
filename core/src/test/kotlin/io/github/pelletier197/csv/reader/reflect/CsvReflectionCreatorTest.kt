package io.github.pelletier197.csv.reader.reflect

import io.github.pelletier197.csv.CsvProperty
import com.kheops.csv.reader.reflect.converters.ConversionSettings
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

data class CsvReflectionClass(
    val string: String,
    @CsvProperty("just_field")
    val field: String,
    @CsvProperty("just_field_ignore", ignoreCase = true)
    val fieldIgnoreCase: String,
)

internal class CsvReflectionCreatorTest : ShouldSpec({
    val expectedClass = CsvReflectionClass(
        string = "string_value",
        field = "just_field_value",
        fieldIgnoreCase = "just_field_ignore_value"
    )
    val expectedNoError = InstantiationWithErrors(
        result = expectedClass,
        errors = emptyList()
    )

    val settings = ConversionSettings()
    val underTest = CsvReflectionCreator(CsvReflectionClass::class.java)

    context("parsing the entity using provided field names") {

        val headers = mapOf(
            "string" to expectedClass.string,
            "just_field" to expectedClass.field,
            "just_field_ignore" to expectedClass.fieldIgnoreCase,
        )

        should("create the instance properly") {
            underTest.createCsvInstance(headers, settings).shouldBe(expectedNoError)
        }
    }

    context("parsing a field ignore case") {
        context("csv property should ignore case") {
            val headers = mapOf(
                "string" to expectedClass.string,
                "just_field" to expectedClass.field,
                "JusT_field_ignOre" to expectedClass.fieldIgnoreCase,
            )
            should("create the instance property") {
                underTest.createCsvInstance(headers, settings).shouldBe(expectedNoError)
            }
        }
        context("csv property should not ignore case") {
            val headers = mapOf(
                "string" to expectedClass.string,
                "Just_Field" to expectedClass.field,
                "JusT_field_ignOre" to expectedClass.fieldIgnoreCase,
            )
            val expectedError = InstantiationWithErrors(
                result = null,
                errors = listOf(
                    InstantiationError(
                        field = "field",
                        originalField = "just_field",
                        providedValue = null,
                        cause = null,
                        type = InstantiationErrorType.NON_NULLABLE_FIELD_IS_NULL
                    )
                )
            )
            should("fail with an error of missing field value") {
                underTest.createCsvInstance(headers, settings).shouldBe(expectedError)
            }
        }
    }

    context("parsing class using class field name instance of annotation property name") {
        val headers = mapOf(
            "string" to expectedClass.string,
            "field" to expectedClass.field,
            "fieldIgnoreCase" to expectedClass.fieldIgnoreCase,
        )
        val expectedError = InstantiationWithErrors(
            result = null,
            errors = listOf(
                InstantiationError(
                    field = "field",
                    originalField = "just_field",
                    providedValue = null,
                    cause = null,
                    type = InstantiationErrorType.NON_NULLABLE_FIELD_IS_NULL
                ),
                InstantiationError(
                    field = "fieldIgnoreCase",
                    originalField = "just_field_ignore",
                    providedValue = null,
                    cause = null,
                    type = InstantiationErrorType.NON_NULLABLE_FIELD_IS_NULL
                )
            )
        )
        should("fail with an error of missing fields") {
            underTest.createCsvInstance(headers, settings).shouldBe(expectedError)
        }
    }
})
