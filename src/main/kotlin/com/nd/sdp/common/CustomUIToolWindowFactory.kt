package com.nd.sdp.common

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.UIUtil
import com.nd.sdp.common.model.Config
import com.nd.sdp.common.model.Widget
import com.nd.sdp.common.task.Callback
import com.nd.sdp.common.task.CheckGradleTask
import com.nd.sdp.common.task.GetMockConfigTask
import com.nd.sdp.common.task.GetRealConfigTask
import com.nd.sdp.common.utils.ValueExportTransferHandler
import org.jdesktop.swingx.JXImageView
import org.jdesktop.swingx.JXTextField
import java.awt.Cursor
import java.awt.Desktop
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.io.IOException
import java.net.URI
import java.net.URL
import java.util.*
import javax.swing.*
import javax.swing.border.Border
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.event.ListSelectionListener
import javax.swing.plaf.basic.BasicSplitPaneDivider
import javax.swing.plaf.basic.BasicSplitPaneUI

/**
 * 入口Factory
 * Created by Young on 2017/6/8.
 */
class CustomUIToolWindowFactory : ToolWindowFactory {

    private var toolWindowPanel: JPanel? = null
    private var listWidget: JList<Widget>? = null
    private var listCategory: JList<String>? = null
    private var widgetSplitPane: JSplitPane? = null
    private var buttonDragToXML: JButton? = null
    private var previewPane: JPanel? = null
    private var labelRepository: JLabel? = null
    private var infoSplitPane: JSplitPane? = null
    private var textPaneReadme: JTextPane? = null
    private var buttonDragToJava: JButton? = null
    private var tfFeedback: JLabel? = null
    private var tfRefresh: JLabel? = null
    private var categoryListModel: DefaultListModel<String> = DefaultListModel()
    private val widgetMap = HashMap<String, DefaultListModel<Widget>>()
    private var mCurrentWidget: Widget? = null
    private var mConfig: Config? = null
    private var tfSearch: JTextField? = null

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(toolWindowPanel, "", false)
        toolWindow.contentManager.addContent(content)
        widgetSplitPane!!.dividerSize = 2
        flattenJSplitPane(infoSplitPane)
        listCategory!!.selectionMode = ListSelectionModel.SINGLE_INTERVAL_SELECTION
        listCategory!!.layoutOrientation = JList.VERTICAL
        textPaneReadme!!.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
//        val font = labelRepository!!.font
//        val attributes = font.attributes
//        labelRepository!!.font = font.deriveFont(attributes)
        tfFeedback!!.addMouseListener(OpenUrlAction("https://github.com/eyyoung/idea-plugin-custom-ui-panel/issues"))
        labelRepository!!.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        tfRefresh!!.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        tfFeedback!!.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        tfRefresh!!.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                initData()
            }
        })
        listWidget!!.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(evt: MouseEvent?) {
                if (project.isDisposed) {
                    return
                }
                val list = evt!!.source as JList<*>
                val index = list.locationToIndex(evt.point)
                val model = listWidget!!.model as DefaultListModel<Widget>
                val widget = model.get(index)
                if (evt.clickCount == 1) {
                    setCurrentWidget(widget)
                } else if (evt.clickCount == 2) {
                    val selectedTextEditor = FileEditorManager.getInstance(project).selectedTextEditor
                    if (selectedTextEditor != null) {
                        val document = selectedTextEditor.document
                        val file = FileDocumentManager.getInstance().getFile(document)
                        if (file != null) {
                            val fileTypeUpperCase = file.fileType.name.toUpperCase()
                            val code: String?
                            if ("xml" == widget.defaultType) {
                                if ("XML" != fileTypeUpperCase) {
                                    Notifications.Bus.notify(Notification("Custom UI Panel",
                                            "文件类型错误",
                                            "请确保打开的是XML文件",
                                            NotificationType.ERROR))
                                    return
                                }
                                code = widget.xml
                            } else {
                                if ("JAVA" != fileTypeUpperCase) {
                                    Notifications.Bus.notify(Notification("Custom UI Panel",
                                            "文件类型错误",
                                            "请确保打开的是请确保打开的文件是Java文件",
                                            NotificationType.ERROR))
                                    return
                                }
                                code = widget.java
                            }
                            if (code == null) {
                                return
                            }
                            val selectionModel = selectedTextEditor.selectionModel
                            val selectionStart = selectionModel.selectionStart
                            val finalCode = code
                            WriteCommandAction.runWriteCommandAction(project) {
                                document.replaceString(selectionStart, selectionStart, finalCode)
                                val replaceIndex = finalCode.indexOf("\${replace}")
                                if (replaceIndex == -1) {
                                    selectionModel.removeSelection()
                                }
                                selectionModel.removeSelection()
                                selectionModel.setSelection(selectionStart + replaceIndex, selectionStart + replaceIndex + 10)
                                selectedTextEditor.caretModel.moveToOffset(selectionStart + replaceIndex)
                                IdeFocusManager.getInstance(project).requestFocus(selectedTextEditor.contentComponent, true)
                                val manager = PsiDocumentManager.getInstance(project)
                                manager.commitDocument(document)
                                UIUtil.invokeAndWaitIfNeeded(Runnable {
                                    val psiFile = PsiManager.getInstance(project).findFile(file)
                                    CodeStyleManager.getInstance(project).reformat(psiFile!!)
                                })
                            }
                            checkDependency(widget, project, selectedTextEditor, mConfig)
                        }
                    }
                }
            }
        })
        buttonDragToXML!!.addMouseMotionListener(object : MouseAdapter() {
            override fun mouseDragged(e: MouseEvent?) {
                if (project.isDisposed) {
                    return
                }
                if (mCurrentWidget == null) {
                    return
                }
                val button = e!!.source as JButton
                val handle = button.transferHandler
                handle.exportAsDrag(button, e, TransferHandler.COPY)
                checkDependency(mCurrentWidget!!, project, FileEditorManager.getInstance(project).selectedTextEditor, mConfig)
            }
        })
        buttonDragToJava!!.addMouseMotionListener(object : MouseAdapter() {
            override fun mouseDragged(e: MouseEvent?) {
                if (project.isDisposed) {
                    return
                }
                if (mCurrentWidget == null) {
                    return
                }
                val button = e!!.source as JButton
                val handle = button.transferHandler
                handle.exportAsDrag(button, e, TransferHandler.COPY)
                checkDependency(mCurrentWidget!!, project, FileEditorManager.getInstance(project).selectedTextEditor, mConfig)
            }
        })
        initSearch()

        initData()
    }

    private fun initSearch() {
        val document = tfSearch!!.document
        document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(e: DocumentEvent?) {
            }

            override fun insertUpdate(e: DocumentEvent?) {
                val text = tfSearch!!.text
                updateSearchResult(text)
            }

            private fun updateSearchResult(text: String) {
                val allWidgets = widgetMap["All"]
                if (text.isEmpty()) {
                    listCategory!!.selectedIndex = 0
                    listWidget!!.model = allWidgets
                    listWidget!!.selectedIndex = 0
                    setCurrentWidget(allWidgets!!.get(0))
                    return
                }
                val size = allWidgets!!.size
                val results = DefaultListModel<Widget>()
                for (i in 0..size - 1) {
                    val widget = allWidgets.get(i)
                    val searchInfo = widget.getSearchInfo()
                    if (searchInfo.contains(text)) {
                        results.addElement(widget)
                    }
                }
                listWidget!!.model = results
                listWidget!!.selectedIndex = 0
                if (results.isEmpty) {
                    return
                }
                setCurrentWidget(results.get(0))
            }

            override fun removeUpdate(e: DocumentEvent?) {
                val text = tfSearch!!.text
                updateSearchResult(text)
            }
        })
    }

    fun createUIComponents() {
        tfSearch = JXTextField()
        (tfSearch!! as JXTextField).prompt = "搜索"
    }

    private fun checkDependency(widget: Widget, project: Project, selectedTextEditor: Editor?, config: Config?) {
        WriteCommandAction.runWriteCommandAction(project) {
            if (widget.dependencies == null || widget.dependencies!!.dependency == null || widget.dependencies?.dependency?.isEmpty() ?: true) {
                if (widget.dependency?.groupId?.isEmpty() ?: true || widget.dependency?.artifactId?.isEmpty() ?: true || widget.dependency?.version?.isEmpty() ?: true) {
                    if (config?.commonDep == null) {
                        return@runWriteCommandAction
                    }
                    ProgressManager.getInstance().executeNonCancelableSection(CheckGradleTask(config.commonDep!!, project, selectedTextEditor!!))
                    return@runWriteCommandAction
                }
                ProgressManager.getInstance().executeNonCancelableSection(CheckGradleTask(widget.dependency!!, project, selectedTextEditor!!))
            } else {
                for (dependency in widget.dependencies!!.dependency!!) {
                    ProgressManager.getInstance().executeNonCancelableSection(CheckGradleTask(dependency, project, selectedTextEditor!!))
                }
            }
        }
    }

    private var openWikiAction: OpenUrlAction? = null

    private fun setCurrentWidget(widget: Widget) {
        mCurrentWidget = widget
        buttonDragToXML!!.isVisible = !mCurrentWidget!!.xml!!.trim { it <= ' ' }.isEmpty()
        buttonDragToJava!!.isVisible = !mCurrentWidget!!.java!!.trim { it <= ' ' }.isEmpty()
        buttonDragToXML!!.transferHandler = ValueExportTransferHandler(mCurrentWidget!!.xml)
        buttonDragToJava!!.transferHandler = ValueExportTransferHandler(mCurrentWidget!!.java)
        previewPane!!.removeAll()
        val jxImageView = JXImageView()
        try {
            jxImageView.setImage(URL(widget.image!!))
            previewPane!!.add(jxImageView)
            jxImageView.addMouseListener(OpenUrlAction(widget.imageWiki))
        } catch (e: IOException) {
            e.printStackTrace()
        }

        textPaneReadme!!.text = mCurrentWidget!!.readme
        labelRepository!!.text = "<html><font><u>${mCurrentWidget!!.repository}</u></font></html>"
        if (openWikiAction != null) {
            textPaneReadme!!.removeMouseListener(openWikiAction)
        }
        openWikiAction = OpenUrlAction(mCurrentWidget!!.wiki)
        textPaneReadme!!.addMouseListener(openWikiAction)
        val mouseListeners = labelRepository!!.mouseListeners
        if (mouseListeners.isNotEmpty()) {
            labelRepository!!.removeMouseListener(mouseListeners[0])
        }
        labelRepository!!.addMouseListener(OpenUrlAction(mCurrentWidget!!.repository))
    }

    private fun initData() {
        val getMockConfigTask = GetRealConfigTask(Callback { config ->
            mConfig = config;
            afterGetConfig(config?.widgets?.widget)
        })
        ProgressManager.getInstance().executeNonCancelableSection(getMockConfigTask)
    }

    private fun afterGetConfig(widgets: List<Widget>?): Void? {
        if (widgets == null) {
            return null
        }
        categoryListModel.clear()
        widgetMap.clear()
        categoryListModel.addElement("All")
        val allListModel = DefaultListModel<Widget>()
        widgetMap.put("All", allListModel)
        for (widget in widgets) {
            val inMap = widgetMap[widget.category]
            val widgetDefaultListModel = inMap ?: DefaultListModel<Widget>()
            if (!widgetMap.containsKey(widget.category)) {
                widgetMap.put(widget.category!!, widgetDefaultListModel)
                categoryListModel.addElement(widget.category)
            }
            widgetDefaultListModel.addElement(widget)
            allListModel.addElement(widget)
        }
        listCategory!!.model = categoryListModel
        listCategory!!.selectedIndex = 0
        listWidget!!.model = widgetMap["All"]
        listWidget!!.selectedIndex = 0
        setCurrentWidget(allListModel.get(0))
        listCategory!!.addListSelectionListener(ListSelectionListener { e ->
            if (!e!!.valueIsAdjusting) {
                val index = listCategory!!.selectedIndex
                if (index == -1) {
                    return@ListSelectionListener
                }
                val s = categoryListModel.get(index)
                val widgetDefaultListModel = widgetMap[s]
                listWidget!!.model = widgetDefaultListModel
                listWidget!!.selectedIndex = 0
                setCurrentWidget(widgetDefaultListModel!!.get(0))
            }
        })
        return null
    }

    private class OpenUrlAction internal constructor(private val url: String?) : MouseListener {

        override fun mouseClicked(e: MouseEvent) {
            if (url!!.isEmpty()) {
                return
            }
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(URI.create(url))
                } catch (ignored: IOException) {
                }

            }
        }

        override fun mousePressed(e: MouseEvent) {

        }

        override fun mouseReleased(e: MouseEvent) {

        }

        override fun mouseEntered(e: MouseEvent) {

        }

        override fun mouseExited(e: MouseEvent) {

        }
    }

    private fun flattenJSplitPane(splitPane: JSplitPane?) {
        if (splitPane == null) {
            return
        }
        try {
            splitPane.border = BorderFactory.createEmptyBorder(1, 1, 1, 1)
            val flatDividerSplitPaneUI = object : BasicSplitPaneUI() {
                override fun createDefaultDivider(): BasicSplitPaneDivider {
                    return object : BasicSplitPaneDivider(this) {
                        override fun setBorder(b: Border) {}
                    }
                }
            }
            splitPane.ui = flatDividerSplitPaneUI
            splitPane.border = BorderFactory.createEmptyBorder()
        } catch(ignore: Exception) {
        } finally {
        }
    }

}
