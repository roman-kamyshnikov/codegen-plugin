package dev.roman.kamyshnikov.codegen.selectFunction

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.PsiDirectory
import dev.roman.kamyshnikov.codegen.Config
import dev.roman.kamyshnikov.codegen.di.ServiceLocator
import dev.roman.kamyshnikov.codegen.generation.models.CodegenDir
import dev.roman.kamyshnikov.codegen.res.R

class ShowSelectFunctionDialogAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val targetDirectory = e.getData(CommonDataKeys.PSI_ELEMENT) as? PsiDirectory ?: return

        val serviceLocator = ServiceLocator(project = project)

        val dialog = serviceLocator.getSelectFunctionDialog { apiFunction ->
            val generator = serviceLocator.getCodeGenerator()

            try {
                generator.generate(
                    targetDir = CodegenDir.from(psiDirectory = targetDirectory),
                    apiFunction = apiFunction,
                )
                project.showNotification(message = R.strings.generation_success_message, type = NotificationType.INFORMATION)
            } catch (exception: Exception) {
                project.showNotification(message = R.strings.generation_error_message, type = NotificationType.ERROR)
                throw exception
            }
        }

        dialog.show()
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = project != null && file != null && file.isDirectory
            && ProjectFileIndex.getInstance(project).isInSourceContent(file)
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    private fun Project.showNotification(message: String, type: NotificationType) {
        val notification = Notification(Config.Service.NOTIFICATION_GROUP_ID, R.strings.dialog_title, message, type)
        Notifications.Bus.notify(notification, this)
    }
}