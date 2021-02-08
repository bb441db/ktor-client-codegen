package github.bb441db.ktor

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
/**
 * Override a method value parameter's name when compiling a template URL,
 * Value parameters marked with [Param] have priority over unmarked parameters.
 */
annotation class Param(val name: String)