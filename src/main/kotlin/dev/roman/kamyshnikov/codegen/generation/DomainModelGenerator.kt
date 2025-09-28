package dev.roman.kamyshnikov.codegen.generation

import dev.roman.kamyshnikov.codegen.Config
import dev.roman.kamyshnikov.codegen.generation.extensions.extractTypeClassFqName
import dev.roman.kamyshnikov.codegen.generation.extensions.requireKotlinFqName
import dev.roman.kamyshnikov.codegen.generation.extensions.requireTypeArgumentsAsTypes
import dev.roman.kamyshnikov.codegen.generation.models.CodegenDir
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaTypeNullability
import org.jetbrains.kotlin.analysis.api.types.symbol
import org.jetbrains.kotlin.idea.base.psi.kotlinFqName
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtTypeReference
import java.util.regex.Pattern

class DomainModelGenerator(
    private val generationQueue: GenerationQueue,
) {
    fun deepCloneParameter(
        parameter: KtParameter,
        targetDir: CodegenDir,
    ): KtParameter {
        return run {
            val keyword = parameter.valOrVarKeyword?.text ?: KtTokens.VAL_KEYWORD
            val newName = parameter.name!!.removeSurroundingBackticks().removeVersionSuffix()
            val newTypeReference = deepCloneType(parameter.typeReference!!, targetDir)

            KtPsiFactory(parameter.project, markGenerated = true)
                .createParameter("$keyword $newName: ${newTypeReference.text}")
        }
    }

    fun deepCloneType(
        type: KtTypeReference,
        targetDir: CodegenDir,
    ): KtTypeReference {
        return analyze(type) {
            val kaClassType = type.type as KaClassType

            when {
                kaClassType.isKotlinList -> {
                    val dataListType = type.requireTypeArgumentsAsTypes.single()
                    val domainListType = deepCloneType(dataListType, targetDir)

                    run {
                        val listFqName = kaClassType.requireKotlinFqName
                        val quest = "?".takeUnless { kaClassType.nullability == KaTypeNullability.NON_NULLABLE } ?: ""

                        KtPsiFactory(type.project, markGenerated = true)
                            .createType("$listFqName<${domainListType.text}>$quest")
                    }
                }

                kaClassType.hasTypeArguments -> {
                    val fqName = kaClassType.requireKotlinFqName
                    val arguments = kaClassType.typeArguments.joinToString { it.type?.symbol?.psi?.kotlinFqName?.asString()!! }
                    throw IllegalArgumentException("Trying to clone [$fqName] with type arguments [$arguments].")
                }

                kaClassType.isDataLayerModel -> {
                    val dataLayerKtClassOrObject = kaClassType.symbol.psi as KtClassOrObject

                    val domainLayerKtClassOrObject = when {
                        dataLayerKtClassOrObject.hasModifier(KtTokens.DATA_KEYWORD) -> {
                            dataLayerKtClassOrObject.convertToDomainDataClass(targetDir)
                        }

                        else -> {
                            throw IllegalArgumentException("Cannot convert class ${dataLayerKtClassOrObject.fqName?.asString()}. Only data classes are supported.")
                        }
                    }

                    run {
                        val fqName = domainLayerKtClassOrObject.fqName?.asString()!!
                        val quest = "?".takeUnless { kaClassType.nullability == KaTypeNullability.NON_NULLABLE } ?: ""

                        KtPsiFactory(type.project, markGenerated = true)
                            .createType("$fqName$quest")
                    }
                }

                else -> KtPsiFactory(type.project, markGenerated = true).createType(extractTypeClassFqName(type))
            }
        }
    }

    private fun KtClassOrObject.convertToDomainDataClass(
        targetDir: CodegenDir,
        newClassName: String = this.nameAsSafeName.asString().removeVersionSuffix(),
        kDoc: String? = null,
    ): KtClass {
        val newFile = generationQueue.getOrCreateInMemoryFile(targetDir, "$newClassName.kt")

        val newParameters = this.primaryConstructor?.valueParameters!!
            .map { deepCloneParameter(it, targetDir) }

        return newFile.declarations
            .filterIsInstance<KtClass>()
            .singleOrNull { existingClass -> existingClass.name == newClassName }
            ?.let { existingClass ->
                checkIfParametersMatchOrFallback(
                    existingParameters = existingClass.primaryConstructor?.valueParameters.orEmpty(),
                    newParameters = newParameters,
                    onMatch = { existingClass },
                    onFallback = {
                        convertToDomainDataClass(
                            targetDir = targetDir,
                            newClassName = newClassName + "_NEW",
                            kDoc = """
                                |/**
                                | * TODO: Class [${existingClass.fqName?.asString()}] already exists, but has different constructor parameters! Please merge the files manually.
                                | *  Original class - [${this.fqName?.asString()}]
                                | */
                            """.trimMargin(),
                        )
                    }
                )
            }
            ?: run {
                val params = newParameters.joinToString(separator = ",\n", postfix = ",") { it.text }

                val clazz = KtPsiFactory(this.project, markGenerated = true).createClass(
                    """
                        |$kDoc
                        |data class $newClassName(
                        |    $params
                        |)
                    """.trimMargin()
                )
                val newClass = newFile.add(clazz) as KtClass
                generationQueue.addFile(targetDir.path, newFile)

                return@run newClass
            }
    }

    /**
     * Check if both lists match.
     *
     * The list of new parameters are pending generation and will be in fully-qualified form.
     *
     * For the list of existing parameters, there are two options:
     * - They are in a class in the generation queue (in-memory) and they are also in fully-qualified form;
     * - They are in a class that was already on disk before this generation run was triggered. They are in short form and need to be resolved.
     */
    private fun checkIfParametersMatchOrFallback(
        existingParameters: List<KtParameter>,
        newParameters: List<KtParameter>,
        onMatch: () -> KtClass,
        onFallback: () -> KtClass,
    ): KtClass {
        // fast-track: if lists have different sizes, they don't match!
        if (newParameters.size != existingParameters.size) return onFallback()

        // Create mutable list of existing parameters for comparison
        val existingParametersCheckList = existingParameters.toMutableList()

        val parametersMatch = newParameters.all { newParameter ->
            // Iterate of each remaining existing parameter and try to find a match.
            val matchingExistingParameter = existingParametersCheckList.find { existingParameter ->
                val namesMatch = newParameter.name == existingParameter.name

                // Parameters can be in fully-qualified form if they are in an in-memory file that hasn't been generated yed
                val typesTextMatch = newParameter.typeReference?.text == existingParameter.typeReference?.text

                // Fallback type check in case the name matches, but type texts are different.
                // This can happen in two cases: 1 - the types are actually different or 2 - the parameters come from a class on disk
                val typesViaKotlinAnalysisMatch by lazy(LazyThreadSafetyMode.NONE) {
                    val existingFqName = existingParameter.typeReference?.let { type ->
                        analyze(type) {
                            extractTypeClassFqName(type)
                        }
                    }
                    newParameter.typeReference?.text == existingFqName
                }

                namesMatch && (typesTextMatch || typesViaKotlinAnalysisMatch)
            }

            // If we didn't find a matching existing parameter, `remove` returns false. The lists don't match!
            existingParametersCheckList.remove(matchingExistingParameter)
        }

        return when (parametersMatch) {
            true -> onMatch()
            false -> onFallback()
        }
    }
}

private val KaClassType.isDataLayerModel: Boolean
    get() = this.requireKotlinFqName.startsWith(Config.Input.dataLayerModelFqNamePrefix)

private val KaClassType.isKotlinList: Boolean
    get() = this.requireKotlinFqName == Config.Constants.KOTLIN_LIST_FQ_NAME

private val KaClassType.hasTypeArguments: Boolean
    get() = this.typeArguments.isNotEmpty()

private fun String.removeVersionSuffix(): String {
    return Pattern.compile("V\\d+$").matcher(this).replaceFirst("")
}

private fun String.removeSurroundingBackticks(): String {
    return this.removeSurrounding("`")
}
