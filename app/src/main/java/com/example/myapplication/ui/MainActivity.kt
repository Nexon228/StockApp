package com.example.myapplication.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
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
import com.example.myapplication.model.Stock
import com.example.myapplication.model.StockDetails
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
            StockDetailsScreen(
                symbol = backStackEntry.arguments?.getString("symbol").orEmpty(),
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
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "StockApp",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Twoja prosta lista obserwowanych spółek.",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = uiState.tickerText,
            onValueChange = viewModel::onTickerTextChange,
            label = { Text("Ticker, np. AAPL") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = viewModel::addStock,
                enabled = !uiState.isLoading,
                modifier = Modifier.weight(1f)
            ) {
                Text("Dodaj")
            }

            OutlinedButton(
                onClick = viewModel::refreshAll,
                enabled = !uiState.isLoading,
                modifier = Modifier.weight(1f)
            ) {
                Text("Odśwież")
            }
        }

        StatusRow(
            message = uiState.messageText,
            lastUpdateTime = uiState.lastUpdateTime,
            isLoading = uiState.isLoading
        )

        Text(
            text = "Obserwowane spółki",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (uiState.stockList.isEmpty()) {
            EmptyListInfo()
        } else {
            uiState.stockList.forEach { stock ->
                StockCard(
                    stock = stock,
                    onClick = { onStockClick(stock.symbol) },
                    onDelete = { viewModel.deleteStock(stock.symbol) }
                )
            }
        }
    }
}

@Composable
fun StatusRow(
    message: String,
    lastUpdateTime: String,
    isLoading: Boolean
) {
    if (message.isBlank() && lastUpdateTime.isBlank() && !isLoading) {
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        }

        Column(modifier = Modifier.weight(1f)) {
            if (message.isNotBlank()) {
                Text(text = message, style = MaterialTheme.typography.bodyMedium)
            }

            if (lastUpdateTime.isNotBlank()) {
                Text(
                    text = "Ostatnia aktualizacja: $lastUpdateTime",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun EmptyListInfo() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = "Dodaj pierwszy ticker, aby zobaczyć cenę, zmianę i podstawowe dane spółki.",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockCard(
    stock: Stock,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val changeColor = if (stock.changePercent >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CompanyLogo(stock = stock, size = 56)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stock.symbol,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(text = stock.name, style = MaterialTheme.typography.bodyMedium)
                Text(text = "${formatPrice(stock.price)} USD")
                Text(
                    text = formatChangePercent(stock.changePercent),
                    color = changeColor,
                    fontWeight = FontWeight.SemiBold
                )
                Text(text = "${stock.industry} - ${stock.country}", style = MaterialTheme.typography.bodySmall)
            }

            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFC62828))
            ) {
                Text("Usuń")
            }
        }
    }
}

@Composable
fun CompanyLogo(stock: Stock, size: Int) {
    if (stock.logoUrl.isBlank()) {
        Box(modifier = Modifier.size(size.dp))
        return
    }

    AsyncImage(
        model = stock.logoUrl,
        contentDescription = "Logo ${stock.name}",
        modifier = Modifier.size(size.dp),
        contentScale = ContentScale.Fit
    )
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
            uiState.isLoading -> LoadingContent(modifier = Modifier.padding(padding))
            uiState.errorText.isNotBlank() -> ErrorContent(uiState.errorText, Modifier.padding(padding))
            uiState.stock != null -> StockDetailsContent(
                stock = uiState.stock!!,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorContent(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, textAlign = TextAlign.Center)
    }
}

@Composable
fun StockDetailsContent(
    stock: StockDetails,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CompanyLogo(
                stock = Stock(
                    symbol = stock.symbol,
                    name = stock.name,
                    price = stock.price,
                    logoUrl = stock.logoUrl
                ),
                size = 72
            )

            Column {
                Text(
                    text = stock.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(text = stock.symbol, style = MaterialTheme.typography.titleMedium)
            }
        }

        DetailCard(stock = stock)

        Text(
            text = "Porównanie cen",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

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

@Composable
fun DetailCard(stock: StockDetails) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            DetailRow("Cena", "${formatPrice(stock.price)} ${stock.currency}")
            DetailRow("Zmiana", formatChangePercent(stock.changePercent))
            DetailRow("Branża", stock.industry)
            DetailRow("Kraj", stock.country)
            DetailRow("Giełda", stock.exchange)
            DetailRow("IPO", stock.ipo)
            DetailRow("Kapitalizacja", String.format("%.2f", stock.marketCap))
            DetailRow("Strona", stock.webUrl)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold)
        Text(
            text = value,
            modifier = Modifier.padding(start = 12.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun PriceBarsChart(values: List<Pair<String, Double>>) {
    val maxValue = values.maxOfOrNull { it.second }?.takeIf { it > 0.0 } ?: 1.0

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        values.forEach { (label, value) ->
            Column {
                Text("$label: ${formatPrice(value)} USD")

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(22.dp)
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
