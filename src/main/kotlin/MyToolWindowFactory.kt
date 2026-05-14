package com.example

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class MyToolWindowFactory : ToolWindowFactory {

    private var currentWatcher: LogWatcher? = null

    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow
    ) {

        val panel = JPanel(BorderLayout())

        val topPanel = JPanel(BorderLayout())

        val chooseFileButton =
            JButton("Choose Log File")

        val filterField =
            JTextField("###")

        val textArea = JTextArea()

        textArea.isEditable = false

        val allLogs = mutableListOf<String>()

        fun refreshLogs() {

            val filter =
                filterField.text

            val filteredLogs =
                allLogs.filter {

                    filter.isEmpty() ||
                            it.contains(filter)
                }

            textArea.text =
                filteredLogs.joinToString("\n")

            textArea.caretPosition =
                textArea.document.length
        }

        filterField.document.addDocumentListener(
            object : DocumentListener {

                override fun insertUpdate(
                    e: DocumentEvent?
                ) {
                    refreshLogs()
                }

                override fun removeUpdate(
                    e: DocumentEvent?
                ) {
                    refreshLogs()
                }

                override fun changedUpdate(
                    e: DocumentEvent?
                ) {
                    refreshLogs()
                }
            }
        )

        topPanel.add(
            chooseFileButton,
            BorderLayout.WEST
        )

        topPanel.add(
            filterField,
            BorderLayout.CENTER
        )

        chooseFileButton.addActionListener {

            val descriptor = FileChooserDescriptor(
                true,
                false,
                false,
                false,
                false,
                false
            )

            val virtualFile: VirtualFile? =
                FileChooser.chooseFile(
                    descriptor,
                    project,
                    null
                )

            if (virtualFile != null) {

                currentWatcher?.interrupt()

                allLogs.clear()

                refreshLogs()

                currentWatcher = LogWatcher(
                    filePath = virtualFile.path,

                    onNewLine = { line ->

                        SwingUtilities.invokeLater {

                            allLogs.add(line)

                            refreshLogs()
                        }
                    },

                    onFileReset = {

                        SwingUtilities.invokeLater {

                            allLogs.clear()

                            refreshLogs()
                        }
                    }
                )

                currentWatcher?.start()
            }
        }

        panel.add(topPanel, BorderLayout.NORTH)

        panel.add(
            JScrollPane(textArea),
            BorderLayout.CENTER
        )

        val content = ContentFactory
            .getInstance()
            .createContent(panel, "", false)

        toolWindow.contentManager
            .addContent(content)
    }
}