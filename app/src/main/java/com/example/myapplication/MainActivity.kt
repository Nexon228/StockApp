package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import coil3.compose.AsyncImage
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                StockApp()
            }
        }
    }
}

@Composable
fun StockApp() {
    var tickerText by remember { mutableStateOf("") }
    var stockList by remember { mutableStateOf(listOf<Stock>()) }
    var messageText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var lastUpdateTime by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            delay(10000)

            if (stockList.isNotEmpty()) {
                val updatedList = stockList.map { stock ->
                    refreshStockPrice(stock) ?: stock
                }

                stockList = updatedList
                messageText = "Ceny zaktualizowane"
                lastUpdateTime = getCurrentTimeString()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Moja aplikacja giełdowa",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = tickerText,
            onValueChange = { tickerText = it },
            label = { Text("Wpisz ticker, np. AAPL") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        Button(
            onClick = {
                val cleanedTicker = tickerText.trim().uppercase()

                if (cleanedTicker.isEmpty()) {
                    messageText = "Wpisz ticker spółki"
                } else if (stockList.any { it.symbol == cleanedTicker }) {
                    messageText = "Ta spółka jest już na liście"
                } else {
                    coroutineScope.launch {
                        val stock = fetchStockFromApi(cleanedTicker)

                        if (stock != null) {
                            stockList = stockList + stock
                            tickerText = ""
                            messageText = "Dodano spółkę ${stock.symbol}"
                            lastUpdateTime = getCurrentTimeString()
                        } else {
                            messageText = "Nie znaleziono spółki o tickerze $cleanedTicker"
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Dodaj")
        }

        if (lastUpdateTime.isNotEmpty() || stockList.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (lastUpdateTime.isNotEmpty())
                        "Ostatnia aktualizacja: $lastUpdateTime"
                    else
                        "Brak aktualizacji"
                )

                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            if (stockList.isNotEmpty()) {
                                stockList = refreshAllStocks(stockList)
                                messageText = "Ceny zaktualizowane"
                                lastUpdateTime = getCurrentTimeString()
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Odśwież ceny"
                    )
                }
            }

            Text(
                text = "Lista spółek:",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        for (stock in stockList) {
            StockCard(
                stock = stock,
                onDelete = {
                    stockList = stockList.filter { it.symbol != stock.symbol }
                    messageText = "Usunięto spółkę ${stock.symbol}"
                }
            )
        }

    }
}


@Composable
fun StockCard(
    stock: Stock,
    onDelete: () -> Unit
) {
    val changeColor =
        if (stock.changePercent >= 0) Color(0xFF2E7D32)
        else Color(0xFFC62828)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (stock.logoUrl.isNotBlank()) {
                AsyncImage(
                    model = stock.logoUrl,
                    contentDescription = "Logo spółki ${stock.name}",
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier.size(64.dp)
                )
            }

            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(text = "Symbol: ${stock.symbol}")
                Text(text = "Nazwa: ${stock.name}")
                Text(text = "Cena: ${formatPrice(stock.price)} USD")
                Text(
                    text = "Zmiana: ${formatChangePercent(stock.changePercent)}",
                    color = changeColor
                )
                Text(text = "Branża: ${stock.industry}")
                Text(text = "Kraj: ${stock.country}")
            }

            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFC62828)
                )
            ) {
                Text("Usuń")
            }
        }
    }
}
fun getCurrentTimeString(): String {
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return formatter.format(Date())
}
fun formatPrice(price: Double): String {
    return String.format("%.2f", price)
}

fun formatChangePercent(changePercent: Double): String {
    return if (changePercent > 0) {
        "+${String.format("%.2f", changePercent)}%"
    } else {
        "${String.format("%.2f", changePercent)}%"
    }
}

suspend fun refreshAllStocks(stockList: List<Stock>): List<Stock> {
    return stockList.map { stock ->
        refreshStockPrice(stock) ?: stock
    }
}