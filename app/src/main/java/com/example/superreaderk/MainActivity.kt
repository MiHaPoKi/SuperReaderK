package com.example.superreaderk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.superreaderk.ui.theme.SuperReaderKTheme
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


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
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Slider()
                        // Кнопка для запуска Reading
                        if (isReadingVisible) {
                            Reading(pose = pos)
                            Button(onClick = { isPaused = !isPaused

                            }) {
                                Text(if (isPaused) "Resume" else "Pause")
                            }
                        }
                        Button(onClick = { isReadingVisible = true }) {
                            Text("Start Reading")
                            //isPaused = false
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                }
            }
        }
    }
}

var book: String = "Lorem ipsum is a dummy text made by some random mf to test his fonts sometime back ago. And it is now very famous."
var pos: Long = 100
var elem: Int = 0
var isPaused by mutableStateOf(false)

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
        var splitted = book.split(' ')
        while(elem < splitted.size){
            if(!isPaused){
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
}}

