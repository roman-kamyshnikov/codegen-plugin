package dev.roman.kamyshnikov.codegen.generation.extensions

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaTypeNullability
import org.jetbrains.kotlin.idea.base.psi.kotlinFqName
import org.jetbrains.kotlin.psi.KtTypeReference

val KaClassType.requireKotlinFqName: String
    get() = requireNotNull(this.symbol.psi?.kotlinFqName?.asString())

fun KaSession.extractTypeClassFqName(ktTypeReference: KtTypeReference): String = with(this) {
    val kaClassType = ktTypeReference.type as KaClassType

    val fqName = kaClassType.requireKotlinFqName
    val fqTypeArguments = ktTypeReference.typeElement?.typeArgumentsAsTypes
        ?.takeIf { it.isNotEmpty() }
        ?.joinToString(separator = ", ", prefix = "<", postfix = ">") { argument -> extractTypeClassFqName(argument) }
        ?: ""
    val quest = "?".takeUnless { kaClassType.nullability == KaTypeNullability.NON_NULLABLE } ?: ""

    return "$fqName$fqTypeArguments$quest"
}