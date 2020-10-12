package io.github.pelletier197.csv

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class CsvProperty(
    val name: String,
    val ignoreCase: Boolean = false
)
