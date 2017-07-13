package com.nd.sdp.common.task

import com.github.platan.idea.Coordinate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.PsiTreeUtil
import com.nd.sdp.common.model.Dependency
import com.nd.sdp.common.model.Widget
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrApplicationStatement
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall
import java.io.File

/**
 * 检测依赖任务
 * Created by Young on 2017/7/7.
 */
class CheckGradleTask(private val dependency: Dependency, private val project: Project, private val editor: Editor) : Runnable {

    override fun run() {
        val dependency = dependency
        val document = editor.document
        val file = FileDocumentManager.getInstance().getFile(document)
        val findModuleForFile = ModuleUtilCore.findModuleForFile(file!!, project)
        val moduleFile = findModuleForFile!!.moduleFile
        val buildGradleFile = moduleFile!!.findFileByRelativePath("../build.gradle")
        val gradleFilePsi = PsiManager.getInstance(project).findFile(buildGradleFile!!)
        checkApplyFile(gradleFilePsi)
        val scriptDir = File(buildGradleFile!!.parent.path, "commonUIScript")
        if (!scriptDir.exists()) {
            scriptDir.mkdir()
        }
        val commonDepGradle = File(scriptDir, "commonDep.gradle")
        if (!commonDepGradle.exists()) {
            commonDepGradle.createNewFile()
        }
        VirtualFileManager.getInstance().syncRefresh()
        var commonDepGradleVF = LocalFileSystem.getInstance().findFileByIoFile(commonDepGradle)!!
        var commonDepGradlePsi = PsiManager.getInstance(project).findFile(commonDepGradleVF)!!
        var findDependenciesClosure = findDependenciesClosure(commonDepGradlePsi)
        if (findDependenciesClosure == null) {
            val factory = GroovyPsiElementFactory.getInstance(project)
            val createMethodCallByAppCall = factory.createStatementFromText("dependencies{\n\n}") as GrMethodCall
            commonDepGradlePsi.add(createMethodCallByAppCall)
            commonDepGradleVF = LocalFileSystem.getInstance().findFileByIoFile(commonDepGradle)!!
            commonDepGradlePsi = PsiManager.getInstance(project).findFile(commonDepGradleVF)!!
            findDependenciesClosure = findDependenciesClosure(commonDepGradlePsi)
        }
        if (checkWidget(dependency, commonDepGradlePsi)) {
            return
        }
        val factory = GroovyPsiElementFactory.getInstance(project)
        val createStatementFromText = factory.createStatementFromText("compile '${dependency!!.groupId}:${dependency.artifactId}:${dependency.version}'")
        val addStatementBefore = findDependenciesClosure!!.addStatementBefore(createStatementFromText, null)
        CodeStyleManager.getInstance(project).reformat(commonDepGradlePsi)
    }

    private fun findDependenciesClosure(psiFile: PsiFile): GrClosableBlock? {
        val methodCalls = PsiTreeUtil.getChildrenOfTypeAsList(psiFile.originalElement, GrMethodCall::class.java)
        val dependenciesBlock = methodCalls.find { it.invokedExpression.text == "dependencies" } ?: return null
        return dependenciesBlock.closureArguments.first()
    }

    private fun checkWidget(dependency: Dependency?, psiFile: PsiFile): Boolean {
        val methodCalls = PsiTreeUtil.getChildrenOfTypeAsList(psiFile.originalElement, GrMethodCall::class.java)
        val dependenciesBlock = methodCalls.find { it.invokedExpression.text == "dependencies" } ?: return false
        val allDep = PsiTreeUtil.getChildrenOfTypeAsList(dependenciesBlock, GrClosableBlock::class.java)
        val whiteSpace = allDep[0].children.filter { it is PsiWhiteSpace }
        for (psiElement in whiteSpace) {
            (psiElement as PsiWhiteSpace).delete()
        }
        val compileStatements = allDep[0].children.filter { it is GrApplicationStatement }
        val element = compileStatements.find { it?.text?.contains("${dependency!!.groupId}:${dependency.artifactId}") ?: false } ?: return false
        val statement = element as GrApplicationStatement
        val regex = Regex("(')(.*)(')")
        regex.find(element.text).let { match ->
            val value = match?.groups!![2]?.value
            val parse = Coordinate.parse(value)
            val version = parse.version.get()
            if (version == dependency?.version) {
                return true
            }
            if (version < dependency?.version ?: "") {
                element.removeStatement()
            } else {
                return true
            }
        }
        return false
    }

    private fun checkApplyFile(psiFile: PsiFile?) {
        if (psiFile == null) {
            return
        }
        val methodCalls = PsiTreeUtil.getChildrenOfTypeAsList(psiFile.originalElement, GrApplicationStatement::class.java)
        val dependenciesBlock = methodCalls.find { it.text == "apply from: file('commonUIScript/commonDep.gradle')" }
        if (dependenciesBlock == null) {
            val factory = GroovyPsiElementFactory.getInstance(project)
            val createStatementFromText = factory.createStatementFromText("apply from: file('commonUIScript/commonDep.gradle')")
            psiFile.add(createStatementFromText)
        }
    }
}
