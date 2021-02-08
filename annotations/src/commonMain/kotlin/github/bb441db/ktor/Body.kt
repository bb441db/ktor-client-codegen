package github.bb441db.ktor

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
/**
 * Sets the method value parameter as the body on the request configuration,
 * @property contentType - When not empty, a "Content-Type" header will also be added to the request configuration.
 */
annotation class Body(val contentType: String = "")