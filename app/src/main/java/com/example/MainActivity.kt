package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.EditRateDialog
import com.example.ui.components.QuickConverterDialog
import com.example.ui.components.RateHeaderBanner
import com.example.ui.components.SaleReceiptDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CounterScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CobroAgilViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CobroAgilApp()
            }
        }
    }
}

@Composable
fun CobroAgilApp(
    viewModel: CobroAgilViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    val activeRate by viewModel.activeRate.collectAsStateWithLifecycle()
    val showEditRate by viewModel.showEditRateDialog.collectAsStateWithLifecycle()
    val showConverter by viewModel.showQuickConverter.collectAsStateWithLifecycle()
    val showScanner by viewModel.showBarcodeScanner.collectAsStateWithLifecycle()
    val showReceipt by viewModel.showReceiptDialog.collectAsStateWithLifecycle()
    val lastReceipt by viewModel.lastSaleReceipt.collectAsStateWithLifecycle()
    val feedbackMsg by viewModel.saleFeedbackMessage.collectAsStateWithLifecycle()

    // Manejo de volver a la pestaña de inicio (Mostrador)
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    LaunchedEffect(feedbackMsg) {
        feedbackMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissFeedback()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            RateHeaderBanner(
                activeRate = activeRate,
                isRateOutdated = viewModel.isRateOutdated,
                onEditRateClick = { viewModel.openEditRate() },
                onQuickConverterClick = { viewModel.openQuickConverter() }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = "Mostrador"
                        )
                    },
                    label = {
                        Text(
                            text = "Mostrador",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("nav_tab_counter")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = "Productos"
                        )
                    },
                    label = {
                        Text(
                            text = "Productos",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("nav_tab_products")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "Historial"
                        )
                    },
                    label = {
                        Text(
                            text = "Historial",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("nav_tab_history")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Analítica"
                        )
                    },
                    label = {
                        Text(
                            text = "Analítica",
                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("nav_tab_analytics")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> CounterScreen(viewModel = viewModel)
                1 -> ProductsScreen(viewModel = viewModel)
                2 -> HistoryScreen(viewModel = viewModel)
                3 -> AnalyticsScreen(viewModel = viewModel)
            }
        }
    }

    // Modales y diálogos globales
    if (showEditRate) {
        EditRateDialog(
            currentRate = viewModel.currentRateValue,
            currentSource = activeRate?.fuente ?: "Manual",
            onDismiss = { viewModel.closeEditRate() },
            onSaveRate = { rate, src -> viewModel.saveRate(rate, src) }
        )
    }

    if (showConverter) {
        QuickConverterDialog(
            currentRate = viewModel.currentRateValue,
            onDismiss = { viewModel.closeQuickConverter() }
        )
    }

    if (showScanner) {
        BarcodeScannerDialog(
            onDismiss = { viewModel.closeBarcodeScanner() },
            onBarcodeScanned = { code -> viewModel.scanBarcode(code) }
        )
    }

    if (showReceipt && lastReceipt != null) {
        SaleReceiptDialog(
            ventaConDetalles = lastReceipt!!,
            onDismiss = { viewModel.closeReceipt() }
        )
    }
}
