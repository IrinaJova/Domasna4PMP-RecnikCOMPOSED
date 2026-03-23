package com.example.composedictionary

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Пастелни бои за нежен изглед
            val pastelBackground = Color(0xFFFDF6EC)
            val accentGreen = Color(0xFF81B29A)

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = pastelBackground) {
                    DictionaryApp(accentGreen)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryApp(accentColor: Color) {
    val context = LocalContext.current
    // Трајна меморија на телефонот
    val sharedPrefs = remember { context.getSharedPreferences("dictionary_prefs", Context.MODE_PRIVATE) }

    var word by remember { mutableStateOf("") }
    var translation by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    // Вчитување на податоците од меморијата
    var dictionaryMap by remember {
        mutableStateOf(
            sharedPrefs.all.mapValues { it.value.toString() }.toMutableMap().ifEmpty {
                mutableMapOf("car" to "avtomobil", "baby" to "bebe", "house" to "kukja", "apple" to "jabolko")
            }
        )
    }

    // Филтрирање за пребарување и превод
    val filteredList = dictionaryMap.filter {
        it.key.contains(searchQuery, ignoreCase = true) || it.value.contains(searchQuery, ignoreCase = true)
    }.toList()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Pastel Dictionary", fontWeight = FontWeight.Bold, color = Color(0xFF3D405B)) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {

            // 1. ПОЛЕ ЗА ПРЕБАРАЈ И ПРЕВЕДИ
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Пребарај или Преведи...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                // ПОПРАВКА ЗА BOITE:
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    focusedLabelColor = accentColor,
                    cursorColor = accentColor
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. КАРТИЧКА ЗА ДОДАВАЊЕ НОВИ ЗБОРОВИ
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Додај нов збор:", fontWeight = FontWeight.Bold, color = accentColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = word,
                            onValueChange = { word = it },
                            label = { Text("English") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = translation,
                            onValueChange = { translation = it },
                            label = { Text("MK") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Button(
                        onClick = {
                            if (word.isNotBlank() && translation.isNotBlank()) {
                                val key = word.lowercase().trim()
                                val value = translation.lowercase().trim()

                                // Зачувај трајно во меморијата на телефонот
                                sharedPrefs.edit().putString(key, value).apply()

                                // Ажурирај го интерфејсот
                                val updatedMap = dictionaryMap.toMutableMap()
                                updatedMap[key] = value
                                dictionaryMap = updatedMap

                                word = ""; translation = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text("Зачувај во речникот", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Твоите зборови:", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3D405B))
            Spacer(modifier = Modifier.height(8.dp))

            // 3. ЛИСТА СО АНИМАЦИЈА (LAZY COLUMN)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredList) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth().animateContentSize(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = item.first, fontWeight = FontWeight.Bold, color = accentColor)
                            Text(text = "→", color = Color.Gray)
                            Text(text = item.second, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}