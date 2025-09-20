package dev.roman.kamyshnikov.codegen.selectFunction

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.PsiDirectory
import dev.roman.kamyshnikov.codegen.di.ServiceLocator

class ShowSelectFunctionDialogAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val targetDirectory = e.getData(CommonDataKeys.PSI_ELEMENT) as? PsiDirectory ?: return

        ServiceLocator.init(project)
        ServiceLocator.getSelectFunctionDialog { apiFunction ->
            TODO("Generate code for $apiFunction in $targetDirectory")
        }.show()
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = project != null && file != null && file.isDirectory
            && ProjectFileIndex.getInstance(project).isInSourceContent(file)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}