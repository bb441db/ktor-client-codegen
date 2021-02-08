package github.bb441db.ktor

import javax.lang.model.element.VariableElement

data class AttributeElement(val name: String, val value: VariableElement) {
    companion object {
        fun from(element: VariableElement, annotation: Attr): AttributeElement {
            val name = annotation.name.ifEmpty { element.simpleName.toString() }
            return AttributeElement(name, element)
        }

        fun from(pair: Pair<VariableElement, Attr>) = from(pair.first, pair.second)
    }
}