package io.github.pelletier197.csv.reader

import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.file.Path
import java.util.stream.Stream

interface CsvReader<LINE : CsvLine> {
    fun read(url: URL): Stream<LINE>

    fun read(input: InputStream): Stream<LINE>

    fun read(file: File): Stream<LINE>

    fun read(path: Path): Stream<LINE>

    fun read(lines: List<String>): Stream<LINE>

    fun read(value: String): Stream<LINE>
}

interface CsvLine {
    val line: Int
}
