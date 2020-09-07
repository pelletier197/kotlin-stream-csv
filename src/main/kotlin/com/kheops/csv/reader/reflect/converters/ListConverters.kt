package com.kheops.csv.reader.reflect.converters

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class StringToListConverter : Converter<String, List<*>> {
    override val source: Class<String> get() = String::class.java
    override val target: Class<List<*>> get() = List::class.java
    override fun convert(value: String, to: Type, settings: ConversionSettings): List<*> {
        if (to !is ParameterizedType) throw IllegalArgumentException(
            """
                Not enough information to determine the type of the target list for value '${value}' for type '${to}'
                To convert a list, it is mandatory to pass the generic type of the list, and not simply the class, otherwise it is impossible 
                to determine the target type
            """
        )
        val targetClass = to.actualTypeArguments[0]
        return value.split(settings.listSeparator).map { it.trim() }
            .map { convertToType<String, Any?>(it, targetClass, settings) }
    }
}