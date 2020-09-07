package com.kheops.csv.reader.reflect.converters

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList

abstract class BaseStringToListConverter<TO> : Converter<String, TO> {
    override val source: Class<String> get() = String::class.java

}

class StringToListConverter : BaseStringToListConverter<List<*>>() {
    override val target: Class<List<*>> get() = List::class.java
    override fun convert(value: String, to: Type, settings: ConversionSettings): List<*> = asList(value, to, settings)
}

class StringToArrayListConverter : BaseStringToListConverter<ArrayList<*>>() {
    override val target: Class<ArrayList<*>> get() = ArrayList::class.java
    override fun convert(value: String, to: Type, settings: ConversionSettings): ArrayList<*> =
        ArrayList(asList(value, to, settings))
}

class StringToLinkedListConverter : BaseStringToListConverter<LinkedList<*>>() {
    override val target: Class<LinkedList<*>> get() = LinkedList::class.java
    override fun convert(value: String, to: Type, settings: ConversionSettings): LinkedList<*> =
        LinkedList(asList(value, to, settings))
}

private fun asList(value: String, to: Type, settings: ConversionSettings): List<*> {
    if (to !is ParameterizedType) throw IllegalArgumentException(
        """
                Not enough information to determine the type of the target list for value '${value}' for type '${to}'
                To convert a list, it is mandatory to pass the generic type of the list, and not simply the class, otherwise it is impossible 
                to determine the target type
            """
    )
    val targetClass = to.actualTypeArguments[0]
    return value.split(settings.listSeparator)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { convertToType<String, Any?>(it, targetClass, settings) }
}