package com.example.superreaderk

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.superreaderk.ui.theme.SuperReaderKTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.ZipInputStream


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuperReaderKTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isReadingVisible by remember { mutableStateOf(false) }
                    //var isPaused by remember { mutableStateOf(false) } // Состояние паузы
                    // Основной UI
                    fileWriter(data = elem.toString(), fileName = "nigger.txt")
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Slider()
                        // Кнопка для запуска Reading
                        if (isReadingVisible) {
                            Reading(pose = pos)
                            Button(onClick = { isPaused = !isPaused }) {
                                Text(if (isPaused) "Resume" else "Pause")
                            }
                        }
                        Button(onClick = { isReadingVisible = !isReadingVisible }) {
                            if (isReadingVisible) {
                                Text("Stop reading")
                            } else {
                                Text("Start reading")
                            }
                            //isPaused = false
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        EpubReader()
                    }
                }
            }
        }
    }

    var fileContent: String by mutableStateOf("")
        private set

    @Composable
    fun EpubPicker(onFileSelected: (Uri?) -> Unit) {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
            onResult = { uri ->
                // Возвращаем выбранный URI
                onFileSelected(uri)
            }
        )

        Button(onClick = {
            launcher.launch(arrayOf("application/epub+zip"))
        }) {
            Text("Выбрать .epub файл")
        }
    }

    @Composable
    fun EpubReader() {
        val context = LocalContext.current
        val cs = rememberCoroutineScope()
        //var fileContent by remember { mutableStateOf("") }  // Состояние для контента книги

        EpubPicker { uri ->
            uri?.let {itUri ->
                cs.launch {
                    // Получаем текст из EPUB по URI и обновляем состояние
                    val text = extractEPUBTextFromUri(context, itUri).joinToString("\n")
                    fileContent = text
                }
            }
        }
    }

    var book: String = fileContent.toString()//"Lorem ipsum is a dummy text. Not much to know about it. A guy in 1500s or like that invented it to test his fonts. It remains popular to these days." //fileContent
    var pos: Long by mutableStateOf(100)
    var elem: Int = 0
    var isPaused by mutableStateOf(false)
    var bufferVar = ""
    var counter = 0

    @Composable
    fun Slider() {
        var position by remember { mutableStateOf(100f) }
        Slider(
            value = position,
            onValueChange = { position = it },
            valueRange = 100f..2000f,
            steps = ((2000 - 100) / 50).toInt() - 1, // Дискретные шаги
            modifier = Modifier.padding(20.dp)
        )
        Text(text = "${position.toLong()} ms interval")
        pos = position.toLong()
    }


    @Composable
    fun Reading(pose: Long, modifier: Modifier = Modifier) {
        var disp by remember { mutableStateOf("") }
        LaunchedEffect(book) {
            //var localCounter = 0
            //var bufferWord = ""
            var splitted = book.split(' ')
//            while(localCounter % 100 != 0){
//                for(x in book){
//                    if (x != ' ') {
//                        bufferWord += x
//                    }
//                    if (x == ' ') {
//                        bufferWord += x
//                        localCounter++
//                    }
//                }
//            }

            while (elem < splitted.size) {
                if (!isPaused) {
                    disp = splitted[elem]
                    elem++
                }
                delay(pose)
            }
        }

        Surface() {
            Text(
                text = disp,
                fontSize = 36.sp,
                modifier = Modifier
            )
        }
    }

    fun seper(): MutableList<String> {
        var localCounter = 0
        var bufferWord = ""
        var splitter: MutableList<String> = mutableListOf()
        while(localCounter % 100 != 0){
            for(x in book){
                if (x != ' ') {
                    bufferWord += x
                }
                if (x == ' ') {
                    //bufferWord += x
                    localCounter++
                }
            }
            splitter += bufferWord
        }
        return splitter
    }


    @Composable
    fun fileWriter(data: String, fileName: String){
        val context = LocalContext.current

        LaunchedEffect(data) {
            saveToFile(context, fileName, data)
        }
    }
    
    private fun saveToFile(context: Context, fileName: String, data: String) {
        try {
            // Открываем файл для записи
            context.openFileOutput(fileName, MODE_PRIVATE).use { outputStream ->
                outputStream.write(data.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace() // Логирование ошибки
        }
    }

    fun extractEPUBTextFromUri(context: Context, epubUri: Uri): List<String> {
        val texts = mutableListOf<String>()
        //var counter = 0

        // Открываем EPUB через URI
        context.contentResolver.openInputStream(epubUri)?.use { inputStream ->
            // Используем ZipInputStream для распаковки
            val zipStream = ZipInputStream(inputStream)
            var entry = zipStream.nextEntry

            while (entry != null) {
                // Ищем файлы с расширением .xhtml или .html
                if (entry.name.endsWith(".xhtml") || entry.name.endsWith(".html")) {
                    val htmlContent = zipStream.bufferedReader().use { it.readText() }
                    // Извлекаем текст из HTML
                    texts.add(extractTextFromHTML(htmlContent))
                }
                // Переходим к следующему файлу в архиве
                entry = zipStream.nextEntry
            }
        }

        return texts
    }

    // Простейший парсер текста из HTML
    fun extractTextFromHTML(html: String): String {
        return html.replace(Regex("<[^>]*>"), "").trim() // Убираем все теги
    }


}



