package dev.roman.kamyshnikov.codegen.generation

import dev.roman.kamyshnikov.codegen.Config
import dev.roman.kamyshnikov.codegen.generation.extensions.requireTypeArgumentsAsTypes
import dev.roman.kamyshnikov.codegen.generation.models.CodegenDir
import org.jetbrains.kotlin.psi.KtNamedFunction

class CodeGenerator(
    private val domainModelGenerator: DomainModelGenerator,
    private val generationQueue: GenerationQueue,
) {

    fun generate(
        targetDir: CodegenDir,
        apiFunction: KtNamedFunction,
    ) {
        val domainDir = targetDir + Config.Output.DOMAIN_PACKAGE
        val modelDir = domainDir + Config.Output.MODEL_PACKAGE

        val parameters = apiFunction.valueParameters.map { dataParameter ->
            dataParameter to domainModelGenerator.deepCloneParameter(parameter = dataParameter, targetDir = modelDir)
        }

        val returnTypes = when (Config.Input.unwrapRetrofitResponse) {
            true -> apiFunction.typeReference?.requireTypeArgumentsAsTypes?.single()!!
            false -> apiFunction.typeReference!!
        }.let { dataReturnType ->
            dataReturnType to domainModelGenerator.deepCloneType(type = dataReturnType, targetDir = modelDir)
        }

        generationQueue.writeAllFiles()
    }
}