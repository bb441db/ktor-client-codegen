package github.bb441db.ktor

import javax.lang.model.element.VariableElement

data class BodyElement(val value: VariableElement, val contentType: String? = null)
