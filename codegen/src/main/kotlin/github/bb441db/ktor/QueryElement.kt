package github.bb441db.ktor

import javax.lang.model.element.VariableElement

sealed class QueryElement(val name: String) {
    class Constant(name: String, val value: String) : QueryElement(name) {
        companion object {
            fun parse(query: String): Constant {
                val before = query.substringBefore('=', "").trim()
                val after = query.substringAfter('=', "").trim()

                return Constant(before, after)
            }
        }
    }

    class Variable(name: String, val value: VariableElement) : QueryElement(name) {
        companion object {
            fun from(element: VariableElement, annotation: Query): Variable {
                val name = annotation.name.ifEmpty { element.simpleName.toString() }
                return Variable(name, element)
            }

            fun from(pair: Pair<VariableElement, Query>) = from(pair.first, pair.second)
        }
    }
}