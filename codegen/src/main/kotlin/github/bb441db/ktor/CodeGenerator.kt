package github.bb441db.ktor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.classinspector.elements.ElementsClassInspector
import com.squareup.kotlinpoet.metadata.*
import com.squareup.kotlinpoet.metadata.specs.internal.ClassInspectorUtil
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmVariance
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Name
import javax.lang.model.element.TypeElement
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

@KotlinPoetMetadataPreview
@ExperimentalStdlibApi
class CodeGenerator(
    private val target: TypeElement,
    private val processingEnvironment: ProcessingEnvironment,
) {

    private val nameAllocator = NameAllocator()
    private val clientMemberName = nameAllocator.newName("client")
    private val implementationClassName = nameAllocator.newName("${nameOf(target)}${classImplementationPostfix}")
    private val inspector = ElementsClassInspector.create(processingEnvironment.elementUtils, processingEnvironment.typeUtils) as ElementsClassInspector

    fun generate(): FileSpec {
        val packageName = packageNameOf(target)
        val name = nameOf(target)
        val targetType = typeNameOf(target)

        val typeSpec = TypeSpec
            .classBuilder(implementationClassName)
            .apply {
                if (targetType is ParameterizedTypeName) {
                    val types = targetType.typeArguments.filterIsInstance<TypeVariableName>()
                    addTypeVariables(types)
                }
            }
            .addConstructor()
            .addMethods(target)
            .build()

        return FileSpec
            .builder(packageName.toString(), name.toString())
            .addType(typeSpec)
            .build()
    }

    private fun TypeSpec.Builder.addConstructor(): TypeSpec.Builder {
        return addSuperinterface(typeNameOf(target))
            .primaryConstructor(
                FunSpec
                    .constructorBuilder()
                    .addParameter(ParameterSpec(clientMemberName, typeNameOf<HttpClient>()))
                    .build()
            )
            .addProperty(
                PropertySpec
                    .builder(clientMemberName, typeNameOf<HttpClient>(), KModifier.PRIVATE)
                    .initializer(clientMemberName)
                    .build()
            )
    }

    private fun TypeSpec.Builder.addMethods(typeElement: TypeElement, parentElementConfig: RequestConfig = RequestConfig.Empty): TypeSpec.Builder {
        val immutableClass = typeElement.toImmutableKmClass()
        val typeSpec = typeElement.toTypeSpec(inspector)
        val functions = typeElement.enclosedElements.filterIsInstance<ExecutableElement>()
        val kFunctions = immutableClass.functions
        val typeConfig = parentElementConfig + collect(typeElement, processingEnvironment)


        for (kFunction in kFunctions) {
            val function = functions.first { it.jvmMethodSignature(processingEnvironment.typeUtils) == kFunction.signature }

            if (!kFunction.isSuspend) {
                processingEnvironment.error("Method must be a suspending function", function)
            }

            val config = typeConfig + collect(function, processingEnvironment)

            addFunction(
                typeSpec
                    .funSpecs
                    .first { it.tag(ImmutableKmFunction::class) == kFunction }
                    .overriding()
                    .clearBody()
                    .addCode(
                        CodeBlock
                            .Builder()
                            .apply {
                                if (kFunction.returnType.classifier == KmClassifier.Class("kotlin/Unit")) {
                                    beginControlFlow(
                                        "return this.%L.%M<%T>",
                                        clientMemberName,
                                        MemberName("io.ktor.client.request", config.methodFunctionName()),
                                        UNIT
                                    )
                                } else {
                                    beginControlFlow(
                                        "return this.%L.%M",
                                        clientMemberName,
                                        MemberName("io.ktor.client.request", config.methodFunctionName())
                                    )
                                }
                            }
                            .add(config.method())
                            .add(config.url())
                            .add(config.body())
                            .add(config.headers())
                            .add(config.parameters())
                            .add(config.attributes())
                            .add(config.timeout())
                            .endControlFlow()
                            .build()
                    )
                    .build()
            )
        }

        val superInterfaces = typeElement.interfaces
            .map { processingEnvironment.typeUtils.asElement(it) }
            .filterIsInstance<TypeElement>()

        for (iface in superInterfaces) {
            addMethods(iface, typeConfig)
        }

        return this
    }

    private val ImmutableKmClass.className: ClassName get() = ClassInspectorUtil.createClassName(this.name)
    private val ImmutableKmClass.typeVariables: List<TypeVariableName> get() = this.typeParameters.map {
        val bounds = it.bounds
        if (bounds == null) {
            TypeVariableName.Companion.invoke(it.name, variance = it.variance.asKModifier())
        } else {
            TypeVariableName.Companion.invoke(it.name, bounds, variance = it.variance.asKModifier())
        }
    }

    private val ImmutableKmTypeParameter.bounds: TypeName?
        get() {
            val first = this.upperBounds.firstOrNull() ?: return null
            return when (val classifier = first.classifier) {
                is KmClassifier.Class -> ClassInspectorUtil.createClassName(classifier.name)
                is KmClassifier.TypeParameter -> return null
                is KmClassifier.TypeAlias -> return null
            }
        }

    private fun KmVariance.asKModifier(): KModifier? {
        return when (this) {
            KmVariance.INVARIANT -> null
            KmVariance.IN -> KModifier.IN
            KmVariance.OUT -> KModifier.OUT
        }
    }

    private fun typeNameOf(element: TypeElement): TypeName {
        val immutableClass = element.toImmutableKmClass()
        val typeVariables = immutableClass.typeVariables
        return if (typeVariables.isNotEmpty()) {
            immutableClass.className.parameterizedBy(typeVariables)
        } else {
            immutableClass.className
        }
    }

    private fun nameOf(element: Element): Name {
        return element.simpleName
    }

    private fun packageNameOf(element: Element): Name {
        return processingEnvironment.elementUtils.getPackageOf(element).qualifiedName
    }

    private fun FunSpec.overriding(): FunSpec.Builder {
        return this
            .toBuilder()
            .apply {
                modifiers.remove(KModifier.ABSTRACT)
                addModifiers(KModifier.OVERRIDE)

                // Remove annotations and default values from method parameters
                parameters.replaceAll { parameterSpec ->
                    parameterSpec.toBuilder()
                        .apply {
                            annotations.removeIf { annotationSpec ->
                                when (annotationSpec.typeName) {
                                    Param::class.asTypeName() -> true
                                    Query::class.asTypeName() -> true
                                    Attr::class.asTypeName() -> true
                                    Body::class.asTypeName() -> true
                                    else -> false
                                }
                            }
                            clearDefault()
                        }.build()
                }

                clearBody()
                addCode("TODO(\"not implemented\")")
            }
    }

    private fun RequestConfig.headers(): CodeBlock {
        val distinctHeaders = this.distinctHeaders()
        val builder = CodeBlock.builder()

        if (distinctHeaders.isEmpty()) {
            return builder.build()
        }

        if (distinctHeaders.size > 1) {
            builder.beginControlFlow("%M", MemberName("io.ktor.client.request", "headers"))

            for ((name, value) in distinctHeaders) {
                builder.addStatement("%L(%S, %S)", "append", name, value)
            }

            builder.endControlFlow()
        } else {
            val headerMember = MemberName("io.ktor.client.request", "header")
            val (name, value) = distinctHeaders.first()

            builder.addStatement("%M(%S, %S)", headerMember, name, value)
        }

        return builder.build()
    }

    private fun RequestConfig.Fun.url(): CodeBlock {
        val parts = this.url
        val urlMemberName = MemberName("io.ktor.client.request", "url")

        return buildCodeBlock {
            when {
                parts.isEmpty() -> addStatement("%M(%S)", urlMemberName, "")
                parts.size == 1 -> {
                    when(val first = parts.first()) {
                        is TemplatePart.Constant -> addStatement("%M(%S)", urlMemberName, first.text)
                        is TemplatePart.Variable -> addStatement("%M(%L.%L())", urlMemberName, first.reference, "toString")
                    }
                }
                else -> {
                    add("%M(", urlMemberName)
                    beginControlFlow("%L", "buildString")
                    for (part in parts) {
                        when (part) {
                            is TemplatePart.Constant -> addStatement("%L(%S)", "append", part.text)
                            is TemplatePart.Variable -> addStatement("%L(%L)", "append", part.reference)
                        }
                    }
                    endControlFlow(postfix = ")\n")
                }
            }
        }
    }

    private fun CodeBlock.Builder.endControlFlow(postfix: String = "\n") {
        unindent()
        add("}$postfix")
    }

    private fun RequestConfig.Fun.methodFunctionName(): String {
        return when (method) {
            HttpMethod.Get -> "get"
            HttpMethod.Delete -> "delete"
            HttpMethod.Head -> "head"
            HttpMethod.Options -> "options"
            HttpMethod.Patch -> "patch"
            HttpMethod.Post -> "post"
            HttpMethod.Put -> "put"
            else -> "request"
        }
    }

    private fun RequestConfig.Fun.method(): CodeBlock {
        return CodeBlock
            .builder()
            .apply {
                when (method) {
                    HttpMethod.Get,
                    HttpMethod.Delete,
                    HttpMethod.Head,
                    HttpMethod.Options,
                    HttpMethod.Patch,
                    HttpMethod.Post,
                    HttpMethod.Put -> { /* Already specified when opening request block */ }
                    else -> addStatement("%L = %T(%S)", "method", HttpMethod::class, method.value)
                }
            }
            .build()
    }

    private fun RequestConfig.Fun.attributes(): CodeBlock {
        return CodeBlock
            .builder()
            .apply {
                val distinctAttributes = distinctAttributes()

                if (distinctAttributes.isNotEmpty()) {
                    beginControlFlow("%L", "setAttributes")
                    for (attribute in distinctAttributes) {
                        addStatement(
                            "%L(%T(%S), %L)",
                            "put",
                            AttributeKey::class,
                            attribute.name,
                            attribute.value
                        )
                    }
                    endControlFlow()
                }
            }
            .build()
    }

    private fun RequestConfig.Fun.parameters(): CodeBlock {
        val parameterMember = MemberName("io.ktor.client.request", "parameter")
        return CodeBlock
            .builder()
            .apply {
                for (query in query) {
                    when (query) {
                        is QueryElement.Variable -> addStatement(
                            "%M(%S, %L)",
                            parameterMember,
                            query.name,
                            query.value
                        )
                        is QueryElement.Constant -> addStatement(
                            "%M(%S, %S)",
                            parameterMember,
                            query.name,
                            query.value
                        )
                    }
                }
            }
            .build()
    }

    private fun RequestConfig.timeout(): CodeBlock {
        val builder = CodeBlock.builder()

        if (timeout == TimeoutElement.Empty) {
            return builder.build()
        }

        return builder
            .apply {
                beginControlFlow("%M", MemberName("io.ktor.client.features", "timeout"))

                if (timeout.connect > -1) {
                    addStatement("%N = %L", "connectTimeoutMillis", timeout.connect)
                }

                if (timeout.request > -1) {
                    addStatement("%N = %L", "requestTimeoutMillis", timeout.request)
                }

                if (timeout.socket > -1) {
                    addStatement("%N = %L", "socketTimeoutMillis", timeout.socket)
                }

                endControlFlow()
            }
            .build()
    }

    private fun RequestConfig.Fun.body(): CodeBlock {
        val builder = CodeBlock.builder()

        if (this.body == null) {
            return builder.build()
        }

        return builder
            .apply {
                addStatement("%N = %L", "body", body.value)
            }
            .build()
    }

    private fun ParameterSpec.Builder.clearDefault(): ParameterSpec.Builder {
        val callable = this::class.declaredMemberProperties
            .filterIsInstance<KMutableProperty<*>>()
            .first { it.name == "defaultValue" }

        val wasAccessible = callable.setter.isAccessible
        callable.setter.isAccessible = true
        callable.setter.call(this, null)
        callable.setter.isAccessible = wasAccessible

        return this
    }

    companion object {
        private const val classImplementationPostfix = "Impl"
    }
}
