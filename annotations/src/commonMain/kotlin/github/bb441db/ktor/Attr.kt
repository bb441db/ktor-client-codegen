package github.bb441db.ktor

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
/**
 * Appends the method value parameter to the request configuration.
 * @property name - Will be used instead of the value parameters name when not empty.
 */
annotation class Attr(val name: String = "")