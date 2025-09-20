package dev.roman.kamyshnikov.codegen.selectFunction

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.containers.reverse
import dev.roman.kamyshnikov.codegen.Config
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

class ApiFunctionProvider(
    private val project: Project,
) {
    private val psiManager = PsiManager.getInstance(project)

    private val psiFunctionToDisplayName: Map<KtNamedFunction, String> = runReadAction {
        FileTypeIndex.getFiles(KotlinFileType.INSTANCE, GlobalSearchScope.projectScope(project))
            .filter { it.name.endsWith(Config.Input.apiFileSuffix) }
            .map { requireNotNull(psiManager.findFile(it)) as KtFile }
            .flatMap { ktFile -> ktFile.collectDescendantsOfType<KtClass> { it.isInterface() } }
            .flatMap { ktClass -> ktClass.collectDescendantsOfType<KtNamedFunction>() }
            .associateWith { it.fqName.toString().removePrefix(Config.Input.apiPackagePrefix) }
    }

    fun getVariants(): Collection<KtNamedFunction> = psiFunctionToDisplayName.keys

    fun functionAsString(function: KtNamedFunction): String? = psiFunctionToDisplayName[function]

    fun findFunction(value: String): KtNamedFunction? = psiFunctionToDisplayName.reverse()[value]
}