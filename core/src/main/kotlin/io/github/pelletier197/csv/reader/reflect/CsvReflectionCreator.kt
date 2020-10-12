package io.github.pelletier197.csv.reader.reflect

import io.github.pelletier197.csv.CsvProperty
import com.kheops.csv.reader.reflect.converters.ConversionSettings
import com.kheops.csv.reader.reflect.converters.Converter
import java.lang.reflect.Field
import java.util.UUID
import kotlin.reflect.jvm.kotlinProperty

data class CsvReflectionCreator<T>(
    private val target: Class<T>,
    private val instanceCreator: InstanceCreator = InstanceCreator()
) {
    private val fieldTranslation: Map<String, InstantiationField>
    private val ignoreCaseTokenId: String = UUID.randomUUID().toString()

    init {
        fieldTranslation = target.declaredFields.map { createFieldMapping(it) }.toMap()
    }

    private fun createFieldMapping(field: Field): Pair<String, InstantiationField> {
        val annotation =
            field.getAnnotation(CsvProperty::class.java) ?: return field.name to toInstantiationField(field)
        if (annotation.ignoreCase) {
            return toIgnoreCaseToken(annotation.name) to toInstantiationField(field)
        }
        return annotation.name to toInstantiationField(field)
    }

    private fun toInstantiationField(field: Field): InstantiationField {
        return InstantiationField(
            field = field,
            property = field.kotlinProperty,
        )
    }

    private fun toIgnoreCaseToken(value: String): String {
        return ignoreCaseTokenId + value.toUpperCase()
    }

    private fun toCsvHeader(value: String): String {
        if (value.startsWith(ignoreCaseTokenId)) return value.replace(ignoreCaseTokenId, "").toLowerCase()
        return value
    }

    fun createCsvInstance(
        csvHeadersValues: Map<String, String?>,
        settings: ConversionSettings
    ): InstantiationWithErrors<T> {
        val ignoreCaseCsvHeader = csvHeadersValues.map { toIgnoreCaseToken(it.key) to it.value }.toMap()

        val arguments = fieldTranslation.map {
            val targetClassField = it.value
            val csvFieldValue = ignoreCaseCsvHeader[it.key] ?: csvHeadersValues[it.key]
            InstantiationArgument(
                field = targetClassField,
                originalTargetName = toCsvHeader(it.key),
                value = csvFieldValue
            )
        }

        return instanceCreator.createInstance(target = target, arguments = arguments, settings)
    }

    fun withConverter(newConverter: Converter<*, *>): CsvReflectionCreator<T> {
        return copy(instanceCreator = instanceCreator.withConverter(newConverter))
    }

    fun withConverters(newConverters: List<Converter<*, *>>): CsvReflectionCreator<T> {
        return copy(instanceCreator = instanceCreator.withConverters(newConverters))
    }

    fun withClearedConverters(): CsvReflectionCreator<T> {
        return copy(instanceCreator = instanceCreator.withClearedConverters())
    }
}
