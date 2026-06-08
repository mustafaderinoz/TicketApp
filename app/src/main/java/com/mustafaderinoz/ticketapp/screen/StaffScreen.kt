package com.mustafaderinoz.ticketapp.screen

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.mustafaderinoz.core.domain.checkin.ScanResult
import com.mustafaderinoz.core.domain.event.Event
import com.mustafaderinoz.ticketapp.viewmodel.StaffViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffScreen(
    viewModel: StaffViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val pullToRefreshState = rememberPullToRefreshState()

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val contents = result.contents
        if (contents != null) {
            viewModel.onQrScanned(contents)
        } else {
            Toast.makeText(context, "QR okunamadı.", Toast.LENGTH_SHORT).show()
        }
    }

    fun startCameraScan() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("QR Kodu Çerçeveye Getir")
            setTimeout(15000)
            setBeepEnabled(true)
            setOrientationLocked(true)
            setBarcodeImageEnabled(false)
        }
        scanLauncher.launch(options)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCameraScan()
    }

    fun onScanClick() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (granted) startCameraScan()
        else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Görevli QR Check-in", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Çıkış Yap",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        // YENİ: PullToRefreshBox ile ana ekran sarıldı
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.loadAssignedEvents(isRefresh = true) },
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Atandığım Etkinlikler",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                if (state.isEventsLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (state.eventsError != null) {
                    Text(
                        text = state.eventsError!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                } else if (state.assignedEvents.isEmpty()) {
                    Text(
                        text = "Henüz atandığınız bir etkinlik bulunmuyor.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.assignedEvents, key = { it.id }) { event ->
                            AssignedEventCard(event)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Button(
                        onClick = { onScanClick() },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("QR Oku", style = MaterialTheme.typography.titleMedium)
                    }

                    if (state.isScanning) {
                        CircularProgressIndicator()
                    }

                    state.scanResult?.let { result ->
                        SuccessResultCard(result, viewModel::clearResult)
                    }

                    state.scanError?.let { errorMessage ->
                        ErrorResultCard(errorMessage, viewModel::clearResult)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun AssignedEventCard(event: Event) {
    Card(
        modifier = Modifier.width(200.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = event.name,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = event.venue,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SuccessResultCard(result: ScanResult, onClear: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
            Text("Geçiş Başarılı!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("${result.event.name} - ${result.ticketType}", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onClear, modifier = Modifier.fillMaxWidth()) { Text("Yeni Tarama Yap") }
        }
    }
}

@Composable
private fun ErrorResultCard(errorMessage: String, onClear: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Outlined.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
            Text("Hata", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            Text(errorMessage, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onClear, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text("Kapat")
            }
        }
    }
}