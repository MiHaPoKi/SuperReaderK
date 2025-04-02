package com.example.superreaderk


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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuperReaderKTheme {
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isReadingVisible by remember { mutableStateOf(false) }
                    
                    
                    fileWriter(data = elem.toString(), fileName = "nigger.txt")
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Slider()
                        
                        if (isReadingVisible) {
                            Reading(pose = pos)
                            Button(onClick = { isPaused = !isPaused }) {
                                Text(if (isPaused) "Продолжить" else "Пауза")
                            }
                        }
                        Button(onClick = { isReadingVisible = !isReadingVisible }) {
                            if (isReadingVisible) {
                                Text("Приостановить чтение")
                            } else {
                                Text("Начать чтение")
                            }
                            
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        EpubReader()
                        Spacer(modifier = Modifier.height(128.dp))
                        

                    }
                }
            }
        }
    }
    var book: String = "" 
    var pos: Long by mutableStateOf(100)
    var elem: Int = 0
    var isPaused by mutableStateOf(false)
    


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
        
        

        EpubPicker { uri ->
            Log.d("DEBUGGIE", "LOG 1")
            uri?.let {
                Log.d("DEBUGGIE", "LOG 2")
                Log.d("DEBUGGIE", "EPUB URI: $it")

                
                    
                try {
                    Log.d("DEBUGGIE", "EPUB URI: $it")
                    var text = extractTextFromEPUB(context, it)
                    Log.d("DEBUGGIE", "TEXT VARIABLE ASSIGNED, value - $text")
                    book = text
                    Log.d("DEBUGGIE", "CLASS VARIABLE CHANGED, Value - $book")
                } catch (e: Exception) {
                    Log.e("DEBUGGIE", "Error reading EPUB: ${e.localizedMessage}", e)
                }
                
            }
        }
    }


    
    

    @Composable
    fun Slider() {
        var position by remember { mutableStateOf(100f) }
        Slider(
            value = position,
            onValueChange = { position = it },
            valueRange = 100f..2000f,
            steps = ((2000 - 100) / 50).toInt() - 1, 
            modifier = Modifier.padding(20.dp)
        )
        Text(text = "Интервал: ${position.toLong()} мс")
        pos = position.toLong()
    }


    @Composable
    fun Reading(pose: Long, modifier: Modifier = Modifier) {
        var disp by remember { mutableStateOf("") }
        var currentSectionIndex by remember { mutableStateOf(0) } 
        val splittedSections = book.split("\n\n") 

        LaunchedEffect(currentSectionIndex) {
            val words = splittedSections.getOrNull(currentSectionIndex)?.split(' ', '.') ?: listOf()
            var localIndex = 0

            while (localIndex < words.size) {
                if (!isPaused) {
                    disp = words[localIndex]
                    localIndex++
                    elem = localIndex 
                }
                delay(pose)
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface {
                Text(
                    text = disp,
                    fontSize = 36.sp,
                    modifier = Modifier
                )
            }

            NavigationButtons(
                onPrevious = {
                    if (currentSectionIndex > 0) {
                        currentSectionIndex-- 
                    }
                },
                onNext = {
                    if (currentSectionIndex < splittedSections.size - 1) {
                        currentSectionIndex++ 
                    }
                }
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
            
            context.openFileOutput(fileName, MODE_PRIVATE).use { outputStream ->
                outputStream.write(data.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace() 
        }
    }
    

    fun extractTextFromEPUB(context: Context, epubUri: Uri): String {
        return try {
            Log.d("DEBUGGIE", "Начинаем обработку EPUB: $epubUri")

            val tempFile = File.createTempFile("temp_epub", ".epub", context.cacheDir)
            context.contentResolver.openInputStream(epubUri)?.use { input ->
                FileOutputStream(tempFile).use { output -> input.copyTo(output) }
            } ?: return "Ошибка: не удалось открыть EPUB"

            Log.d("DEBUGGIE", "Файл скопирован: ${tempFile.absolutePath}")

            val reader = Reader()
            reader.setMaxContentPerSection(5000)
            reader.setIsIncludingTextContent(true)
            reader.setFullContent(tempFile.absolutePath)

            val textContent = mutableListOf<String>()
            var sectionIndex = 0
            var prevText = ""

            while (true) {
                try {
                    val section = reader.readSection(sectionIndex)
                    val sectionText = section?.sectionTextContent
                        ?.split(Regex("\\s+"))  
                        ?.joinToString(" ")     
                        ?.trim() ?: ""
 

                    if (sectionText.isNotBlank() && sectionText != prevText) {
                        textContent.add(sectionText)
                        prevText = sectionText  
                        Log.d("DEBUGGIE", "Секция $sectionIndex загружена, длина: ${sectionText.length}")
                    } else {
                        Log.d("DEBUGGIE", "Секция $sectionIndex дублируется или пустая")
                    }

                    sectionIndex++
                    if (sectionIndex >= 100) break  
                } catch (e: IndexOutOfBoundsException) {
                    Log.d("DEBUGGIE", "Конец книги на секции $sectionIndex")
                    break
                } catch (e: Exception) {
                    Log.e("DEBUGGIE", "Ошибка при чтении секции $sectionIndex: ${e.localizedMessage}", e)
                    break
                }
            }

            return if (textContent.isNotEmpty()) textContent.joinToString("\n\n") else "Ошибка: книга пустая"
        } catch (e: Exception) {
            Log.e("DEBUGGIE", "Ошибка при чтении EPUB: ${e.localizedMessage}", e)
            "Ошибка при чтении EPUB"
        }
    }

    @Composable
    fun NavigationButtons(
        onPrevious: () -> Unit,
        onNext: () -> Unit
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onPrevious) {
                Text("Назад")
            }
            Button(onClick = onNext) {
                Text("Вперёд")
            }
        }
    }





}



