package github.bb441db.ktor

import javax.lang.model.element.VariableElement

object URLUtils {
    private val pattern = "(.*?)(:([a-z]+[a-zA-Z0-9_]*)|\$)".toRegex()

    fun compile(template: String, param: (name: String) -> VariableElement): List<TemplatePart> {
        return mutableListOf<TemplatePart>().apply {
            for ((constant, variable, name) in pattern.findAll(template).map { it.destructured }) {
                if (constant.isNotEmpty()) {
                    add(TemplatePart.Constant(constant))
                }
                if (variable.isNotEmpty()) {
                    add(TemplatePart.Variable(name, variable, param(name)))
                }
            }
        }
    }
}