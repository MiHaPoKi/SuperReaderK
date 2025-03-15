package com.example.superreaderk

//import com.github.mertakdut.Book
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.superreaderk.ui.theme.SuperReaderKTheme
import com.github.mertakdut.Reader
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


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
                        Spacer(modifier = Modifier.height(128.dp))
                        //if(prevMS == maxSections)

                    }
                }
            }
        }
    }
    var book: String = "" //"Lorem ipsum is a dummy text. Not much to know about it. A guy in 1500s or like that invented it to test his fonts. It remains popular to these days." //fileContent
    var pos: Long by mutableStateOf(100)
    var elem: Int = 0
    var isPaused by mutableStateOf(false)
    var maxSections: Int by mutableStateOf(5)
    var prevMS: Int by mutableStateOf(maxSections - 5)

    var fileContent: String by mutableStateOf("")
        private set

    @Composable
    fun EpubPicker(onFileSelected: (Uri?) -> Unit) {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
            onResult = { uri ->
                if (uri != null) {
                    Log.d("DEBUGGIE", "EPUB FILE PICKED: $uri")
                } else {
                    Log.d("DEBUGGIE", "NO FILE SELECTED")
                }
                onFileSelected(uri)
            }
        )

        Button(onClick = {
            try {
                Log.d("DEBUGGIE", "PICK FILE BUTTON CLICKED")
                launcher.launch(arrayOf("application/epub+zip"))
            } catch (e: Exception) {
                Log.e("DEBUGGIE", "File picker crashed: ${e.localizedMessage}", e)
            }
        }) {
            Text("Выбрать .epub файл")
        }
    }

    @Composable
    fun EpubReader() {
        val context = LocalContext.current
        //val cs = rememberCoroutineScope()
        //var fileContent by remember { mutableStateOf("") }  // Состояние для контента книги

        EpubPicker { uri ->
            Log.d("DEBUGGIE", "LOG 1")
            uri?.let {
                Log.d("DEBUGGIE", "LOG 2")
                Log.d("DEBUGGIE", "EPUB URI: $it")

                //cs.launch {
                    // Получаем текст из EPUB по URI и обновляем состояние
                try {
                    Log.d("DEBUGGIE", "EPUB URI: $it")
                    var text = extractTextFromEPUB(context, it)
                    Log.d("DEBUGGIE", "TEXT VARIABLE ASSIGNED, value - $text")
                    book = text
                    Log.d("DEBUGGIE", "CLASS VARIABLE CHANGED, Value - $book")
                } catch (e: Exception) {
                    Log.e("DEBUGGIE", "Error reading EPUB: ${e.localizedMessage}", e)
                }
                //}
            }
        }
    }


    //var bufferVar = ""
    //var counter = 0

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
            var splitted = book.split(' ', '.')//: MutableList<String> = mutableListOf()
//            while(localCounter % 100 != 0 || localCounter == 0){
//                for(x in book){
//                    if (x != ' ') {
//                        bufferWord += x
//                    }
//                    if (x == ' ') {
//                        splitted.add(bufferWord)
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

    fun extractTextFromEPUB(context: Context, epubUri: Uri): String {
        return try {
            Log.d("DEBUGGIE", "Начинаем обработку EPUB: $epubUri")

            // 1. Копируем файл во временную папку
            val tempFile = File.createTempFile("temp_epub", ".epub", context.cacheDir)
            context.contentResolver.openInputStream(epubUri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return "Ошибка: не удалось открыть EPUB"

            Log.d("DEBUGGIE", "Файл скопирован: ${tempFile.absolutePath}")

            // 2. Настраиваем ридер
            val reader = Reader()
            reader.setMaxContentPerSection(5000)  // Больше символов в секции
            reader.setIsIncludingTextContent(true)
            reader.setFullContent(tempFile.absolutePath)

            Log.d("DEBUGGIE", "EPUB загружен, начинаем чтение секций...")

            // 3. Читаем ВСЕ секции, пока не выйдет ошибка
            val textContent = StringBuilder()
            var sectionIndex = 0
            var emptySectionCount = 0  // Считаем подряд идущие пустые секции

            while (true) {
                try {
                    val section = reader.readSection(sectionIndex)  // Читаем секцию
                    val sectionText = section?.sectionTextContent  // Получаем текст

                    if (!sectionText.isNullOrBlank()) {
                        // Если есть текст, добавляем в итоговый результат
                        textContent.append(sectionText).append("\n\n")
                        Log.d("DEBUGGIE", "Секция $sectionIndex загружена, размер: ${sectionText.length} символов")

                        emptySectionCount = 0  // Обнуляем счётчик пустых секций
                    } else {
                        // Если секция пустая, увеличиваем счётчик
                        Log.d("DEBUGGIE", "Секция $sectionIndex пустая, пропускаем...")
                        emptySectionCount++
                    }

                    sectionIndex++

                    // Если встречаем 10 пустых секций подряд — скорее всего, книга закончилась
                    if (emptySectionCount >= 10) {
                        Log.d("DEBUGGIE", "10 пустых секций подряд, вероятно конец книги")
                        break
                    }
                } catch (e: IndexOutOfBoundsException) {
                    Log.d("DEBUGGIE", "Достигнут конец книги на секции $sectionIndex")
                    break
                } catch (e: Exception) {
                    Log.e("DEBUGGIE", "Ошибка при чтении секции $sectionIndex: ${e.localizedMessage}", e)
                    break
                }
            }

            // 4. Возвращаем текст или сообщение об ошибке
            return if (textContent.isNotEmpty()) textContent.toString() else "Ошибка: книга пустая или не распознана"
        } catch (e: Exception) {
            Log.e("DEBUGGIE", "Ошибка при чтении EPUB: ${e.localizedMessage}", e)
            "Ошибка при чтении EPUB"
        }
    }






}



