package com.kheops.csv.reader.reflect.converters

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

abstract class BaseStringToCollectionConverter<TO> : Converter<String, TO> {
    override val source: Class<String> get() = String::class.java

}

class StringToListConverter : BaseStringToCollectionConverter<List<*>>() {
    override val target: Class<List<*>> get() = List::class.java
    override fun convert(value: String, to: Type, settings: ConversionSettings): List<*> = asList(value, to, settings)
}

class StringToArrayListConverter : BaseStringToCollectionConverter<ArrayList<*>>() {
    override val target: Class<ArrayList<*>> get() = ArrayList::class.java
    override fun convert(value: String, to: Type, settings: ConversionSettings): ArrayList<*> =
        ArrayList(asList(value, to, settings))
}

class StringToLinkedListConverter : BaseStringToCollectionConverter<LinkedList<*>>() {
    override val target: Class<LinkedList<*>> get() = LinkedList::class.java
    override fun convert(value: String, to: Type, settings: ConversionSettings): LinkedList<*> =
        LinkedList(asList(value, to, settings))
}

class StringToSetConverter : BaseStringToCollectionConverter<Set<*>>() {
    override val target: Class<Set<*>> get() = Set::class.java
    override fun convert(value: String, to: Type, settings: ConversionSettings): Set<*> =
        asList(value, to, settings).toSet()
}

class StringToHashSetConverter : BaseStringToCollectionConverter<HashSet<*>>() {
    override val target: Class<HashSet<*>> get() = HashSet::class.java
    override fun convert(value: String, to: Type, settings: ConversionSettings): HashSet<*> =
        HashSet(asList(value, to, settings))
}

class StringToTreeSetConverter : BaseStringToCollectionConverter<TreeSet<*>>() {
    override val target: Class<TreeSet<*>> get() = TreeSet::class.java
    override fun convert(value: String, to: Type, settings: ConversionSettings): TreeSet<*> =
        TreeSet(asList(value, to, settings))
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