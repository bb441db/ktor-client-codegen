package github.bb441db.ktor

import kotlinx.metadata.jvm.JvmFieldSignature
import kotlinx.metadata.jvm.JvmMethodSignature
import javax.lang.model.element.*
import javax.lang.model.type.*
import javax.lang.model.util.AbstractTypeVisitor6
import javax.lang.model.util.Types

internal val Element.internalName: String
    get() = when (this) {
        is TypeElement -> {
            when (nestingKind) {
                NestingKind.TOP_LEVEL ->
                    qualifiedName.toString().replace('.', '/')
                NestingKind.MEMBER ->
                    enclosingElement.internalName + "$" + simpleName
                NestingKind.LOCAL, NestingKind.ANONYMOUS ->
                    error("Unsupported nesting $nestingKind")
                null ->
                    error("Unsupported, nestingKind == null")
            }
        }
        is QualifiedNameable -> qualifiedName.toString().replace('.', '/')
        else -> simpleName.toString()
    }

/**
 * @return the "field descriptor" of this type.
 * @see [JvmDescriptorTypeVisitor]
 */
@Suppress("unused")
internal val NoType.descriptor: String
    get() = "V"

/**
 * @return the "field descriptor" of this type.
 * @see [JvmDescriptorTypeVisitor]
 */
internal val DeclaredType.descriptor: String
    get() = "L" + asElement().internalName + ";"

/**
 * @return the "field descriptor" of this type.
 * @see [JvmDescriptorTypeVisitor]
 */
internal val PrimitiveType.descriptor: String
    get() = when (this.kind) {
        TypeKind.BYTE -> "B"
        TypeKind.CHAR -> "C"
        TypeKind.DOUBLE -> "D"
        TypeKind.FLOAT -> "F"
        TypeKind.INT -> "I"
        TypeKind.LONG -> "J"
        TypeKind.SHORT -> "S"
        TypeKind.BOOLEAN -> "Z"
        else -> error("Unknown primitive type $this")
    }

/**
 * @see [JvmDescriptorTypeVisitor]
 */
internal fun TypeMirror.descriptor(types: Types): String =
    accept(JvmDescriptorTypeVisitor, types)

/**
 * @return the "field descriptor" of this type.
 * @see [JvmDescriptorTypeVisitor]
 */
internal fun WildcardType.descriptor(types: Types): String =
    types.erasure(this).descriptor(types)

/**
 * @return the "field descriptor" of this type.
 * @see [JvmDescriptorTypeVisitor]
 */
internal fun TypeVariable.descriptor(types: Types): String =
    types.erasure(this).descriptor(types)

/**
 * @return the "field descriptor" of this type.
 * @see [JvmDescriptorTypeVisitor]
 */
internal fun ArrayType.descriptor(types: Types): String =
    "[" + componentType.descriptor(types)

/**
 * @return the "method descriptor" of this type.
 * @see [JvmDescriptorTypeVisitor]
 */
internal fun ExecutableType.descriptor(types: Types): String {
    val parameterDescriptors = parameterTypes.joinToString(separator = "") { it.descriptor(types) }
    val returnDescriptor = returnType.descriptor(types)
    return "($parameterDescriptors)$returnDescriptor"
}

/**
 * Returns the JVM signature in the form "$Name$MethodDescriptor", for example: `equals(Ljava/lang/Object;)Z`.
 *
 * Useful for comparing with [JvmMethodSignature].
 *
 * For reference, see the [JVM specification, section 4.3](http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3).
 */

internal fun ExecutableElement.jvmMethodSignature(types: Types): JvmMethodSignature {
    return JvmMethodSignature(simpleName.toString(), asType().descriptor(types))
}

/**
 * Returns the JVM signature in the form "$Name:$FieldDescriptor", for example: `"value:Ljava/lang/String;"`.
 *
 * Useful for comparing with [JvmFieldSignature].
 *
 * For reference, see the [JVM specification, section 4.3](http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3).
 */
internal fun VariableElement.jvmFieldSignature(types: Types): String {
    return "$simpleName:${asType().descriptor(types)}"
}

/**
 * When applied over a type, it returns either:
 * - a "field descriptor", for example: `Ljava/lang/Object;`
 * - a "method descriptor", for example: `(Ljava/lang/Object;)Z`
 *
 * For reference, see the [JVM specification, section 4.3](http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3).
 */
internal object JvmDescriptorTypeVisitor : AbstractTypeVisitor6<String, Types>() {
    override fun visitNoType(t: NoType, types: Types): String = t.descriptor
    override fun visitDeclared(t: DeclaredType, types: Types): String = t.descriptor
    override fun visitPrimitive(t: PrimitiveType, types: Types): String = t.descriptor

    override fun visitArray(t: ArrayType, types: Types): String = t.descriptor(types)
    override fun visitWildcard(t: WildcardType, types: Types): String = t.descriptor(types)
    override fun visitExecutable(t: ExecutableType, types: Types): String = t.descriptor(types)
    override fun visitTypeVariable(t: TypeVariable, types: Types): String = t.descriptor(types)

    override fun visitNull(t: NullType, types: Types): String = visitUnknown(
        t, types
    )
    override fun visitError(t: ErrorType, types: Types): String = visitUnknown(
        t, types
    )

    override fun visitUnknown(t: TypeMirror, types: Types): String = error("Unsupported type $t")
}