package com.kheops.csv.reader.reflect

import com.kheops.csv.reader.reflect.converters.ConversionSettings
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlin.reflect.jvm.javaField

class InstanceCreatorTest : ShouldSpec({
    val underTest = InstanceCreator()

    context("creating an instance of kotlin data class with non-nullable field") {
        data class ValidClass(
            val field: String
        )

        val argument = InstantiationArgument(
            field = InstantiationField(
                field = ValidClass::field.javaField!!,
                property = ValidClass::field
            ),
            value = "any",
            originalTargetName = "original_field"
        )

        context("given provided field value is not null") {
            val expected = InstantiationWithErrors(
                result = ValidClass("value"),
                errors = emptyList()
            )

            should("create class instance correctly") {
                underTest.createInstance(
                    ValidClass::class.java,
                    arguments = listOf(argument.copy(value = "value")),
                    settings = ConversionSettings()
                ).shouldBe(expected)
            }
        }

        context("given provided field value is null") {
            val expected = InstantiationWithErrors(
                result = null,
                errors = listOf(
                    InstantiationError(
                        field = argument.field.name,
                        originalField = argument.originalTargetName,
                        type = InstantiationErrorType.NON_NULLABLE_FIELD_IS_NULL,
                        providedValue = null,
                        cause = null
                    )
                )
            )
            should("instantiation fails with an error") {
                underTest.createInstance(
                    ValidClass::class.java,
                    arguments = listOf(argument.copy(value = null)),
                    settings = ConversionSettings()
                ).shouldBe(expected)
            }
        }
    }

    context("creating an instance of kotlin data with nullable field") {
        data class NullableFieldClass(
            val field: String?
        )
        val argument = InstantiationArgument(
            field = InstantiationField(
                field = NullableFieldClass::field.javaField!!,
                property = NullableFieldClass::field
            ),
            value = "any",
            originalTargetName = "original_field"
        )
        context("given provided field value is not null") {
            val expected = InstantiationWithErrors(
                result = NullableFieldClass("value"),
                errors = emptyList()
            )

            should("create class instance correctly") {
                underTest.createInstance(
                    NullableFieldClass::class.java,
                    arguments = listOf(argument.copy(value = "value")),
                    settings = ConversionSettings()
                ).shouldBe(expected)
            }
        }

        context("given provided field value is null") {
            val expected = InstantiationWithErrors(
                result = NullableFieldClass(null),
                errors = emptyList()
            )
            should("create class instance correctly") {
                underTest.createInstance(
                    NullableFieldClass::class.java,
                    arguments = listOf(argument.copy(value = null)),
                    settings = ConversionSettings()
                ).shouldBe(expected)
            }
        }
    }
})