package io.github.pelletier197.csv.reader

import java.nio.file.Files
import java.nio.file.Paths

const val filePath = "test.csv"

fun writeTestFile(content: String) {
    Files.writeString(Paths.get(filePath), content)
}

fun deleteTestFile() {
    Files.delete(Paths.get(filePath))
}
