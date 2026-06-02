package com.mustafaderinoz.ticketapp.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafaderinoz.core.domain.ticket.PurchasedTicketUi
import com.mustafaderinoz.core.util.DateTimeUtils
import com.mustafaderinoz.core.util.TicketUtils
import com.mustafaderinoz.ticketapp.viewmodel.TicketDetailViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TicketDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: TicketDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val ticket = state.ticket

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bilet Detayı",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when {
                // 1. Loading State
                state.isLoading -> {
                    CircularProgressIndicator()
                }

                // 2. Error State
                state.error != null -> {
                    ErrorStateContent(
                        errorMessage = state.error!!,
                        onRetry = { viewModel.loadTicketDetail() }
                    )
                }

                // 3. Content State
                ticket != null -> {
                    TicketDetailContent(
                        ticket = ticket,

                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }

                // 4. Empty State
                else -> {
                    EmptyStateContent()
                }
            }
        }
    }
}

@Composable
private fun ErrorStateContent(errorMessage: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Warning,
            contentDescription = "Hata",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry) {
            Text("Tekrar Dene")
        }
    }
}

@Composable
private fun EmptyStateContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "Bilgi",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = "Bu biletin detayları bulunamadı.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TicketDetailContent(
    ticket: PurchasedTicketUi,
    modifier: Modifier = Modifier,
) {
    val isValid = ticket.status == "VALID"
    val badgeContainerColor = if (isValid) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    val badgeContentColor = if (isValid) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ── QR Kod Alanı ──────────────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Durum Badge
                Surface(
                    shape = CircleShape,
                    color = badgeContainerColor
                ) {
                    Text(
                        text = ticket.status,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = badgeContentColor,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // QR Kod Kutusu (Metin)
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = ticket.qrCode,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            textAlign = TextAlign.Center,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Text(
                    text = "Girişte bu QR kodu okutun",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── Etkinlik Bilgileri Kartı ──────────────────────────────────────────────
        DetailCard(title = "Etkinlik Bilgileri") {
            DetailRow(icon = Icons.Outlined.CheckCircle, label = "Etkinlik", value = ticket.eventName)
            DetailRow(icon = Icons.Outlined.LocationOn, label = "Mekan", value = ticket.eventVenue)

            if (ticket.eventStartsAt.isNotBlank()) {
                DetailRow(
                    icon = Icons.Outlined.DateRange,
                    label = "Tarih",
                    value = DateTimeUtils.formatDate(ticket.eventStartsAt)
                )
                DetailRow(
                    icon = Icons.Outlined.DateRange,
                    label = "Saat",
                    value = DateTimeUtils.formatTime(ticket.eventStartsAt)
                )
            }
        }

        // ── Bilet Bilgileri Kartı ────────────────────────────────────────────────
        DetailCard(title = "Bilet Bilgileri") {
            DetailRow(icon = Icons.Outlined.CheckCircle, label = "Bilet Türü", value = ticket.ticketTypeName)

            if (ticket.ticketTypePriceCents > 0L) {
                DetailRow(
                    icon = Icons.Outlined.CheckCircle,
                    label = "Ücret",
                    value = TicketUtils.formatPrice(ticket.ticketTypePriceCents)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ── Yardımcı UI Bileşenleri ──────────────────────────────────────────────────

@Composable
private fun DetailCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content
            )
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    if (value.isBlank()) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(8.dp)
                    .size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}