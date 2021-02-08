package github.bb441db.ktor

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

fun ProcessingEnvironment.error(msg: CharSequence, e: Element): Nothing {
    messager.printMessage(Diagnostic.Kind.ERROR, msg, e)
    throw Exception(msg.toString())
}
