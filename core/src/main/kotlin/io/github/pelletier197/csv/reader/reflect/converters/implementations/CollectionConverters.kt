package io.github.pelletier197.csv.reader.reflect.converters.implementations

import io.github.pelletier197.csv.reader.reflect.converters.ConversionContext
import io.github.pelletier197.csv.reader.reflect.converters.Converter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.LinkedList
import java.util.TreeSet
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

abstract class BaseStringToCollectionConverter<TO> : Converter<String, TO> {
    override val source: Class<String> get() = String::class.java
}

class StringToListConverter : BaseStringToCollectionConverter<List<*>>() {
    override val target: Class<List<*>> get() = List::class.java
    override fun convert(value: String, to: Type, context: ConversionContext): List<*> =
        asList(value, to, context)
}

class StringToArrayListConverter : BaseStringToCollectionConverter<ArrayList<*>>() {
    override val target: Class<ArrayList<*>> get() = ArrayList::class.java
    override fun convert(value: String, to: Type, context: ConversionContext): ArrayList<*> =
        ArrayList(asList(value, to, context))
}

class StringToLinkedListConverter : BaseStringToCollectionConverter<LinkedList<*>>() {
    override val target: Class<LinkedList<*>> get() = LinkedList::class.java
    override fun convert(value: String, to: Type, context: ConversionContext): LinkedList<*> =
        LinkedList(asList(value, to, context))
}

class StringToSetConverter : BaseStringToCollectionConverter<Set<*>>() {
    override val target: Class<Set<*>> get() = Set::class.java
    override fun convert(value: String, to: Type, context: ConversionContext): Set<*> =
        asList(value, to, context).toSet()
}

class StringToHashSetConverter : BaseStringToCollectionConverter<HashSet<*>>() {
    override val target: Class<HashSet<*>> get() = HashSet::class.java
    override fun convert(value: String, to: Type, context: ConversionContext): HashSet<*> =
        HashSet(asList(value, to, context))
}

class StringToTreeSetConverter : BaseStringToCollectionConverter<TreeSet<*>>() {
    override val target: Class<TreeSet<*>> get() = TreeSet::class.java
    override fun convert(value: String, to: Type, context: ConversionContext): TreeSet<*> =
        TreeSet(asList(value, to, context))
}

private fun asList(value: String, to: Type, parameters: ConversionContext): List<*> {
    if (to !is ParameterizedType) throw IllegalArgumentException(
        """
                Not enough information to determine the type of the target list for value '$value' of type '$to'
                To convert a list, it is mandatory to pass the generic type of the list, and not simply the class, otherwise it is impossible 
                to determine the target type
            """
    )
    val settings = parameters.settings
    val targetClass = to.actualTypeArguments[0]
    return value.split(settings.listSeparator)
        .filter { it.isNotBlank() }
        .map { parameters.convert(it, targetClass, parameters) }
}
