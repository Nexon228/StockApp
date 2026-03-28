package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil3.compose.AsyncImage
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.StockDetailsViewModel
import com.example.myapplication.viewmodel.StockListViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                StockNavGraph()
            }
        }
    }
}

@Composable
fun StockNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "list"
    ) {
        composable("list") {
            StockListScreen(
                onStockClick = { symbol ->
                    navController.navigate("details/$symbol")
                }
            )
        }

        composable(
            route = "details/{symbol}",
            arguments = listOf(navArgument("symbol") { type = NavType.StringType })
        ) { backStackEntry ->
            val symbol = backStackEntry.arguments?.getString("symbol").orEmpty()

            StockDetailsScreen(
                symbol = symbol,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun StockListScreen(
    onStockClick: (String) -> Unit,
    viewModel: StockListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, top = 24.dp, end = 24.dp)
            .navigationBarsPadding()
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = "Moja aplikacja giełdowa",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = uiState.tickerText,
            onValueChange = viewModel::onTickerTextChange,
            label = { Text("Wpisz ticker, np. AAPL") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        Button(
            onClick = { viewModel.addStock() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Dodaj")
        }

        if (uiState.lastUpdateTime.isNotEmpty() || uiState.stockList.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.lastUpdateTime.isNotEmpty())
                        "Ostatnia aktualizacja: ${uiState.lastUpdateTime}"
                    else
                        "Brak aktualizacji"
                )

                IconButton(onClick = { viewModel.refreshAll() }) {
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

        uiState.stockList.forEach { stock ->
            StockCard(
                stock = stock,
                onClick = { onStockClick(stock.symbol) },
                onDelete = { viewModel.deleteStock(stock.symbol) }
            )
        }


    }
}

@Composable
fun StockCard(
    stock: Stock,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val changeColor =
        if (stock.changePercent >= 0) Color(0xFF2E7D32)
        else Color(0xFFC62828)

    Card(
        onClick = onClick,
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
                Box(modifier = Modifier.size(64.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailsScreen(
    symbol: String,
    onBack: () -> Unit,
    viewModel: StockDetailsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(symbol) {
        viewModel.loadStock(symbol)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Szczegóły spółki") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Wstecz")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorText.isNotEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(uiState.errorText)
                }
            }

            uiState.stock != null -> {
                val stock = uiState.stock!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    if (stock.logoUrl.isNotBlank()) {
                        AsyncImage(
                            model = stock.logoUrl,
                            contentDescription = stock.name,
                            modifier = Modifier.size(96.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stock.name,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = stock.symbol,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Cena: ${formatPrice(stock.price)} USD")
                    Text("Zmiana: ${formatChangePercent(stock.changePercent)}")
                    Text("Branża: ${stock.industry}")
                    Text("Kraj: ${stock.country}")
                    Text("Giełda: ${stock.exchange}")
                    Text("Waluta: ${stock.currency}")
                    Text("IPO: ${stock.ipo}")
                    Text("Kapitalizacja: ${String.format("%.2f", stock.marketCap)}")
                    Text("Strona: ${stock.webUrl}")

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Wykres porównawczy",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PriceBarsChart(
                        values = listOf(
                            "Open" to stock.open,
                            "High" to stock.high,
                            "Low" to stock.low,
                            "Prev Close" to stock.previousClose,
                            "Current" to stock.price
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun PriceBarsChart(
    values: List<Pair<String, Double>>
) {
    val maxValue = values.maxOfOrNull { it.second } ?: 1.0

    Column {
        values.forEach { (label, value) ->
            Text("$label: ${formatPrice(value)} USD")

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .padding(bottom = 8.dp)
            ) {
                val barWidth = (size.width * (value / maxValue)).toFloat()

                drawLine(
                    color = Color(0xFF1976D2),
                    start = Offset(0f, size.height / 2),
                    end = Offset(barWidth, size.height / 2),
                    strokeWidth = size.height,
                    cap = StrokeCap.Round
                )
            }
        }
    }
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