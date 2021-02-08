package github.bb441db.ktor

import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import io.ktor.http.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import kotlin.reflect.KClass

private inline fun<reified A: Annotation> Element.getAnnotation(): A? {
    return this.getAnnotation(A::class.java)
}

private fun<A: Annotation> ExecutableElement.parametersAnnotatedWith(annotationClass: KClass<A>): List<Pair<VariableElement, A>> {
    return this.parameters.mapNotNull {
        val annotation = it.getAnnotation(annotationClass.java) ?: return@mapNotNull null
        it to annotation
    }
}

private inline fun<reified A: Annotation> ExecutableElement.parametersAnnotatedWith() = parametersAnnotatedWith(A::class)

@KotlinPoetMetadataPreview
private fun getNamedParameter(element: ExecutableElement, name: String): VariableElement? {
    val withParamAnnotation = element
        .parametersAnnotatedWith<Param>()
        .firstOrNull { (_, annotation) -> annotation.name === name }
        ?.first

    if (withParamAnnotation != null) {
        return withParamAnnotation
    }

    return element.parameters
        .firstOrNull { it.simpleName.contentEquals(name) }
}

private fun getRequest(element: ExecutableElement): Pair<String, HttpMethod>? {
    return listOfNotNull(
        element.getAnnotation<Get>()?.url?.to(HttpMethod.Get),
        element.getAnnotation<Post>()?.url?.to(HttpMethod.Post),
        element.getAnnotation<Put>()?.url?.to(HttpMethod.Put),
        element.getAnnotation<Patch>()?.url?.to(HttpMethod.Patch),
        element.getAnnotation<Delete>()?.url?.to(HttpMethod.Delete),
        element.getAnnotation<Head>()?.url?.to(HttpMethod.Head),
        element.getAnnotation<Options>()?.url?.to(HttpMethod.Options),
        element.getAnnotation<Request>()?.let { it.url to HttpMethod.parse(it.method) }
    ).firstOrNull()
}

@KotlinPoetMetadataPreview
private fun getURLTemplate(element: ExecutableElement, env: ProcessingEnvironment): List<TemplatePart> {
    val (url, _) = getRequest(element) ?: env.error("Methods in interface must be annotated with @Request or one of the @HttpMethod annotations", element)
    return URLUtils.compile(url) { name ->
        getNamedParameter(element, name) ?: env.error("Did not find named parameter for URL part: \":$name\"", element)
    }
}

private fun getMethod(element: ExecutableElement, env: ProcessingEnvironment): HttpMethod {
    val (_, method) = getRequest(element) ?: env.error("Methods in interface must be annotated with @Request or one of the @HttpMethod annotations", element)
    return method
}

private fun getTimeout(element: Element): TimeoutElement {
    return element.getAnnotation<Timeout>()?.let(TimeoutElement::from) ?: TimeoutElement.Empty
}

private fun getAttributes(element: ExecutableElement): List<AttributeElement> {
    return element.parametersAnnotatedWith<Attr>().map(AttributeElement::from)
}

@KotlinPoetMetadataPreview
private fun getHeaders(element: Element): List<HeaderElement> {
    val header = element.getAnnotation<Header>()
    val headers = element.getAnnotation<Headers>()

    return mutableListOf<HeaderElement>().apply {
        if (header != null) {
            add(HeaderElement(header.name, header.value))
        }

        if (headers != null) {
            addAll(headers.values.map(HeaderElement::parse))
        }
    }
}

private fun getQueryElements(element: ExecutableElement): List<QueryElement> {
    return mutableListOf<QueryElement>().apply {
        val constantQuery = element.getAnnotation<Query>()
        val constantQueries = element.getAnnotation<Queries>()

        if (constantQuery != null) {
            add(QueryElement.Constant(constantQuery.name, constantQuery.value))
        }

        if (constantQueries != null) {
            addAll(constantQueries.values.map(QueryElement.Constant::parse))
        }

        addAll(element.parametersAnnotatedWith<Query>().map(QueryElement.Variable::from))
    }
}

fun getBody(element: ExecutableElement): BodyElement? {
    val (variable, annotation) = element.parametersAnnotatedWith<Body>().firstOrNull() ?: return null
    return if (annotation.contentType.isEmpty()) {
        BodyElement(variable)
    } else {
        BodyElement(variable, annotation.contentType)
    }
}

@KotlinPoetMetadataPreview
fun collect(element: ExecutableElement, env: ProcessingEnvironment): RequestConfig.Fun {
    return RequestConfig.Fun(
        url = getURLTemplate(element, env),
        method = getMethod(element, env),
        body = getBody(element),
        headers = getHeaders(element),
        attributes = getAttributes(element),
        query = getQueryElements(element),
        timeout = getTimeout(element)
    )
}

@KotlinPoetMetadataPreview
fun collect(element: TypeElement, env: ProcessingEnvironment): RequestConfig.Class {
    val base = RequestConfig.Class(
        headers = getHeaders(element),
        timeout = getTimeout(element)
    )

    val superInterfaces = element.interfaces
        .map { env.typeUtils.asElement(it) }
        .filterIsInstance<TypeElement>()
        .map { collect(it, env) }

    return superInterfaces.fold(base) { acc, config -> acc + config }
}