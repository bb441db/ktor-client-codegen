package github.bb441db.ktor

import javax.lang.model.element.VariableElement

sealed class TemplatePart {
    data class Variable(val name: String, val match: String, val reference: VariableElement) : TemplatePart()
    data class Constant(val text: String) : TemplatePart()
}