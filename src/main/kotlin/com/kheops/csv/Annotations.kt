package com.kheops.csv

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class CsvProperty(
    val name: String
)