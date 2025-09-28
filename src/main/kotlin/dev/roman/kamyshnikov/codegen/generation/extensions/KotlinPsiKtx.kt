package dev.roman.kamyshnikov.codegen.generation.extensions

import org.jetbrains.kotlin.psi.KtTypeReference

val KtTypeReference.requireTypeArgumentsAsTypes: List<KtTypeReference>
    get() = requireNotNull(this.typeElement?.typeArgumentsAsTypes)


