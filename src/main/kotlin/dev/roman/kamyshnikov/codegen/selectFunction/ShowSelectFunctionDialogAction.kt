package dev.roman.kamyshnikov.codegen.selectFunction

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.roots.ProjectFileIndex

class ShowSelectFunctionDialogAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        TODO("show dialog")
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = project != null && file != null && file.isDirectory
            && ProjectFileIndex.getInstance(project).isInSourceContent(file)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}