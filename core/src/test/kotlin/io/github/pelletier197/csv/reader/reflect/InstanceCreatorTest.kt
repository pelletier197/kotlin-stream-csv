package io.github.pelletier197.csv.reader.reflect

import com.kheops.csv.reader.reflect.converters.ConversionSettings
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant
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

    context("given conversion of field fails") {
        data class InstantFieldClass(
            val field: Instant
        )

        val argument = InstantiationArgument(
            field = InstantiationField(
                field = InstantFieldClass::field.javaField!!,
                property = InstantFieldClass::field
            ),
            value = "not_an_instant",
            originalTargetName = "original_field"
        )

        val expected = InstantiationWithErrors(
            result = null,
            errors = listOf(
                InstantiationError(
                    field = argument.field.name,
                    originalField = argument.originalTargetName,
                    type = InstantiationErrorType.CONVERSION_OF_FIELD_FAILED,
                    providedValue = argument.value,
                    cause = null
                )
            )
        )

        should("return an error of conversion failed") {
            val result = underTest.createInstance(
                InstantFieldClass::class.java,
                arguments = listOf(argument),
                settings = ConversionSettings()
            )
            result.result.shouldBeNull()
            result.errors.shouldHaveSize(1)
            result.errors[0].shouldBeEqualToIgnoringFields(
                expected.errors[0],
                InstantiationError::cause
            )
            result.errors[0].cause.shouldNotBeNull()
        }
    }

    context("given an invalid target class") {
        class TestInvalid private constructor(val field: String)

        val argument = InstantiationArgument(
            field = InstantiationField(
                field = TestInvalid::field.javaField!!,
                property = TestInvalid::field
            ),
            value = "any",
            originalTargetName = "original_field"
        )

        should("throw an exception") {
            shouldThrow<InvalidTargetClass> {
                underTest.createInstance(TestInvalid::class.java, listOf(argument), ConversionSettings())
            }
        }
    }
})
