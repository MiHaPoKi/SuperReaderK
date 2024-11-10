import android.content.Context
import android.os.Environment
import java.io.File
import java.io.IOException

class FileScanner(private val context: Context) {

    fun scanFiles(targetExtensions: List<String>): List<String> {
        val result = mutableListOf<String>()

        // Начинаем сканирование с директории общего доступа
        val rootDirectory = Environment.getExternalStorageDirectory()
        scanDirectory(rootDirectory, targetExtensions, result)

        return result
    }

    private fun scanDirectory(directory: File, extensions: List<String>, result: MutableList<String>) {
        if (!directory.exists() || !directory.isDirectory) return

        directory.listFiles()?.forEach { file ->
            try {
                if (file.isDirectory) {
                    scanDirectory(file, extensions, result)
                } else {
                    if (extensions.any { file.extension.equals(it.removePrefix("."), ignoreCase = true) }) {
                        result.add(file.absolutePath)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }
}