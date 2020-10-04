package com.kheops.csv.reader.types

import io.kotest.core.spec.style.ShouldSpec

private data class TestClass(
    val a: String,
    val b: Int,
    val c: List<String>,
    val d: Set<String>,
)

class TypedCsvReaderTest : ShouldSpec({
    val underTest = TypedCsvReader(TestClass::class.java)
})

