package com.example

import java.io.RandomAccessFile

class LogWatcher(
    private val filePath: String,
    private val onNewLine: (String) -> Unit,
    private val onFileReset: () -> Unit
) : Thread() {

    override fun run() {

        val file = RandomAccessFile(filePath, "r")

        var pointer = 0L

        var buffer = ""

        while (!isInterrupted) {

            val length = file.length()

            // File truncated / cleared
            if (length < pointer) {

                pointer = 0L

                buffer = ""

                onFileReset()
            }

            if (length > pointer) {

                file.seek(pointer)

                val bytes =
                    ByteArray((length - pointer).toInt())

                file.readFully(bytes)

                val chunk = String(bytes)

                buffer += chunk

                val lines =
                    buffer.split("\n")

                // Process only COMPLETE lines
                for (i in 0 until lines.size - 1) {

                    val line = lines[i]

                    if (line.isNotBlank()) {

                        onNewLine(line)
                    }
                }

                // Save incomplete line
                buffer = lines.last()

                pointer = file.filePointer
            }

            sleep(300)
        }

        file.close()
    }
}