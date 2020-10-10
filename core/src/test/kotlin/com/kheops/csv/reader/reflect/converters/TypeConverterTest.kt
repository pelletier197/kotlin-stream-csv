package com.kheops.csv.reader.reflect.converters

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import java.lang.reflect.ParameterizedType
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.Date
import java.util.LinkedList
import java.util.TreeSet
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.reflect.KClass

private data class TestInstance(
    val to: KClass<*>,
    val value: Any?,
    val expected: Any?,
)

internal enum class EnumTest {
    FIRST_ENUM,
    ANOTHER_ENUM_VALUE
}

@ExperimentalUnsignedTypes
class TypeConverterTest : ShouldSpec({
    val settings = ConversionSettings()
    val underTest = TypeConverter.getDefault()

    context("converting all default supported typed") {
        val date = Date()
        val df = Date()

        val testInstances = listOf(
            TestInstance(String::class, "test", "test"),
            TestInstance(Long::class, "12", 12L),
            TestInstance(Int::class, "13", 13),
            TestInstance(Byte::class, "14", 14.toByte()),
            TestInstance(BigDecimal::class, "2374912346.234323", "2374912346.234323".toBigDecimal()),
            TestInstance(BigInteger::class, "2434982374912346", "2434982374912346".toBigInteger()),
            TestInstance(Double::class, "12.64", 12.64),
            TestInstance(Float::class, "12.596", 12.596f),
            TestInstance(EnumTest::class, EnumTest.ANOTHER_ENUM_VALUE.toString(), EnumTest.ANOTHER_ENUM_VALUE),
            TestInstance(EnumTest::class, EnumTest.ANOTHER_ENUM_VALUE.toString().toLowerCase(), EnumTest.ANOTHER_ENUM_VALUE),
            TestInstance(Boolean::class, "t", true),
            TestInstance(Boolean::class, "true", true),
            TestInstance(Boolean::class, "yes", true),
            TestInstance(Boolean::class, "1", true),
            TestInstance(Boolean::class, "x", true),
            TestInstance(Boolean::class, "f", false),
            TestInstance(UInt::class, "123", 123.toUInt()),
            TestInstance(ULong::class, "123", 123.toULong()),
            TestInstance(Instant::class, "2019-03-04T00:00:00.000Z", Instant.parse("2019-03-04T00:00:00.000Z")),
            TestInstance(
                ZonedDateTime::class,
                "2019-03-04T00:00:00.000+01:00",
                ZonedDateTime.parse("2019-03-04T00:00:00.000+01:00")
            ),
            TestInstance(LocalDate::class, "2019-03-12", LocalDate.parse("2019-03-12")),
            TestInstance(
                LocalDateTime::class,
                "2019-03-12T00:00:00.000",
                LocalDateTime.parse("2019-03-12T00:00:00.000")
            ),
        )

        should("convert all values properly") {
            testInstances.forAll {
                underTest.convertToType<Any?, Any?>(it.value, it.to.java, settings).shouldBe(it.expected)
            }
        }
    }

    context("converting a collection") {
        val testValue = "1,2,3, ,"
        val expected = listOf(1, 2, 3)

        val testInstances = listOf(
            TestInstance(List::class, testValue, expected),
            TestInstance(ArrayList::class, testValue, ArrayList(expected)),
            TestInstance(LinkedList::class, testValue, LinkedList(expected)),
            TestInstance(Set::class, testValue, expected.toSet()),
            TestInstance(TreeSet::class, testValue, TreeSet(expected)),
            TestInstance(HashSet::class, testValue, HashSet(expected)),
        )
        context("without generic type specification") {
            should("should throw that generic type is lost") {
                testInstances.forAll {
                    shouldThrow<ConversionFailedException> {
                        underTest.convertToType<Any?, Any?>(it.value, it.to.java, settings).shouldBe(it.expected)
                    }.cause.shouldBeInstanceOf<IllegalArgumentException>()
                }
            }
        }

        context("with generic type specification") {
            should("convert list with generic type target") {
                testInstances.forAll {
                    val parameterized = mockk<ParameterizedType>()
                    every { parameterized.actualTypeArguments } returns arrayOf(Int::class.java)
                    every { parameterized.rawType } returns it.to.java
                    underTest.convertToType<Any?, Any?>(it.value, parameterized, settings).shouldBe(it.expected)
                }
            }
        }
    }

    context("converting an enum") {
        context("enum value is valid") {
            val testInstances = listOf(
                TestInstance(EnumTest::class, EnumTest.ANOTHER_ENUM_VALUE.toString(), EnumTest.ANOTHER_ENUM_VALUE),
                TestInstance(EnumTest::class, EnumTest.ANOTHER_ENUM_VALUE.toString().toLowerCase(), EnumTest.ANOTHER_ENUM_VALUE),
            )

            should("convert value properly") {
                testInstances.forAll {
                    underTest.convertToType<Any?, Any?>(it.value, it.to.java, settings).shouldBe(it.expected)
                }
            }
        }

        context("enum value is invalid") {
            val testInstances = listOf(
                TestInstance(EnumTest::class, "FAKE_VALUE", EnumTest.ANOTHER_ENUM_VALUE),
            )

            should("convert value properly") {
                testInstances.forAll {
                    shouldThrow<ConversionFailedException> {
                        underTest.convertToType<Any?, Any?>("FAKE_VALUE", EnumTest::class.java, settings).shouldBe(it.expected)
                    }
                }
            }
        }
    }
})
