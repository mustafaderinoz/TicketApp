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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafaderinoz.core.domain.event.Event
import com.mustafaderinoz.core.domain.event.TicketType
import com.mustafaderinoz.core.domain.purchase.Purchase
import com.mustafaderinoz.core.util.DateTimeUtils
import com.mustafaderinoz.core.util.TicketUtils
import com.mustafaderinoz.ticketapp.viewmodel.EventDetailViewModel
import com.mustafaderinoz.ticketapp.viewmodel.PurchaseViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: EventDetailViewModel = koinViewModel(),
    purchaseViewModel: PurchaseViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val purchaseState by purchaseViewModel.state.collectAsStateWithLifecycle()
    val pullToRefreshState = rememberPullToRefreshState()

    // ── One-shot events (State üzerinden kontrol) ────────────────────────────
    LaunchedEffect(purchaseState.isPurchaseCompleted, purchaseState.shouldRefreshEvent) {
        if (purchaseState.isPurchaseCompleted) {
            onNavigateToHome()
            purchaseViewModel.onNavigationConsumed()
        }
        if (purchaseState.shouldRefreshEvent) {
            viewModel.loadEvent(isRefresh = true)
            purchaseViewModel.onRefreshConsumed()
        }
    }

    // ── Ödeme Onay Diyaloğu ──────────────────────────────────────────────────
    purchaseState.purchaseForConfirmation?.let { purchase ->
        PurchaseConfirmationDialog(
            purchase = purchase,
            ticketTypes = purchaseState.ticketTypes,
            onConfirm  = { purchaseViewModel.pay() },
            onDismiss  = { purchaseViewModel.dismissConfirmation() },
        )
    }

    // ── Hata Diyaloğu (purchase) ─────────────────────────────────────────────
    purchaseState.error?.let { errorMessage ->
        AlertDialog(
            onDismissRequest = { purchaseViewModel.dismissError() },
            confirmButton = {
                TextButton(onClick = { purchaseViewModel.dismissError() }) { Text("Tamam") }
            },
            icon  = { Icon(Icons.Outlined.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Hata") },
            text  = { Text(errorMessage) },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Etkinlik Detayı", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.loadEvent(isRefresh = true) },
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    state.isLoading -> CircularProgressIndicator()

                    state.error != null -> ErrorStateContent(
                        errorMessage = state.error!!,
                        onRetry = { viewModel.loadEvent() },
                    )

                    state.event != null -> {
                        val isPurchaseInFlight = purchaseState.isCreatingPurchase || purchaseState.isPaying

                        EventDetailContent(
                            event        = state.event!!,
                            quantities   = state.quantities,
                            onIncrement  = viewModel::increment,
                            onDecrement  = viewModel::decrement,
                            onRefresh    = { viewModel.loadEvent(isRefresh = true) },
                            isRefreshing = state.isRefreshing,
                            isPurchasing = isPurchaseInFlight,
                            onBuy        = {
                                purchaseViewModel.createPurchase(
                                    ticketTypes = state.event!!.ticketTypes,
                                    quantities  = state.quantities,
                                )
                            },
                            modifier = Modifier.align(Alignment.TopCenter),
                        )
                    }

                    else -> EmptyStateContent()
                }
            }
        }
    }
}

// ─── Onay Diyaloğu ────────────────────────────────────────────────────────────

@Composable
private fun PurchaseConfirmationDialog(
    purchase:Purchase,
    ticketTypes: List<TicketType>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val ticketTypeMap = ticketTypes.associateBy { it.id }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ödeme Onayı", fontWeight = FontWeight.Bold)


                Text(
                    text = purchase.status.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                purchase.items.forEach { item ->
                    val name = ticketTypeMap[item.ticketTypeId]?.name ?: item.ticketTypeId
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$name × ${item.quantity}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            TicketUtils.formatPrice(item.unitPriceCents * item.quantity),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Toplam", fontWeight = FontWeight.Bold)
                    Text(
                        TicketUtils.formatPrice(purchase.totalCents),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Onayla") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        },
    )
}

// ─── Yardımcı State Composable'ları ──────────────────────────────────────────

@Composable
private fun ErrorStateContent(errorMessage: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(Icons.Outlined.Warning, contentDescription = "Hata",
            tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
        Text(errorMessage, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center)
        Button(onClick = onRetry) { Text("Tekrar Dene") }
    }
}

@Composable
private fun EmptyStateContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(Icons.Outlined.Info, contentDescription = "Bilgi",
            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp))
        Text("Bu etkinliğin detayları bulunamadı.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

// ─── İçerik ───────────────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun EventDetailContent(
    event: Event,
    quantities: Map<String, Int>,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
    isPurchasing: Boolean,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalCents = TicketUtils.calculateTotal(event.ticketTypes, quantities)
    val sortedTicketTypes = TicketUtils.sortTickets(event.ticketTypes)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            val initial = event.name.firstOrNull { it.isLetter() }?.uppercaseChar() ?: '?'
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            EventInfoCard(event = event)

            TicketTypesCard(
                ticketTypes = sortedTicketTypes,
                quantities  = quantities,
                onIncrement = onIncrement,
                onDecrement = onDecrement,
                onRefresh   = onRefresh,
                isRefreshing = isRefreshing,
            )
        }

        BottomBar(
            totalCents   = totalCents,
            isPurchasing = isPurchasing,
            onBuy        = onBuy,
            modifier     = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// ─── Bottom Bar ───────────────────────────────────────────────────────────────

@Composable
private fun BottomBar(
    totalCents: Long,
    isPurchasing: Boolean,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier        = modifier.fillMaxWidth(),
        color           = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation  = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Toplam", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    TicketUtils.formatPrice(totalCents),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Button(
                onClick  = onBuy,
                enabled  = totalCents > 0 && !isPurchasing,
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp),
            ) {
                if (isPurchasing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Satın Al", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─── EventInfoCard / TicketTypesCard / TicketTypeRow / InfoRow / SectionCard ──

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun EventInfoCard(event: Event) {
    SectionCard(title = "Etkinlik Bilgileri") {
        Text(event.name, style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

        if (event.description.isNotBlank()) {
            Text(event.description, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        InfoRow(Icons.Outlined.LocationOn, "Mekan", event.venue)

        val startDate = DateTimeUtils.formatDate(event.startsAt)
        val endDate   = DateTimeUtils.formatDate(event.endsAt)
        val dateValue = if (startDate == endDate) startDate else "$startDate – $endDate"
        InfoRow(Icons.Outlined.DateRange, "Tarih", dateValue)

        val startTime = DateTimeUtils.formatTime(event.startsAt)
        val endTime   = DateTimeUtils.formatTime(event.endsAt)
        InfoRow(Icons.Outlined.CheckCircle, "Saat", "$startTime – $endTime")
    }
}

@Composable
private fun TicketTypesCard(
    ticketTypes: List<TicketType>,
    quantities: Map<String, Int>,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        border    = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Biletler", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = onRefresh, enabled = !isRefreshing, modifier = Modifier.size(32.dp)) {
                    if (isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Stokları Yenile",
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    }
                }
            }
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ticketTypes.forEachIndexed { index, tt ->
                    TicketTypeRow(
                        ticketType  = tt,
                        quantity    = quantities[tt.id] ?: 0,
                        onIncrement = { onIncrement(tt.id) },
                        onDecrement = { onDecrement(tt.id) },
                    )
                    if (index < ticketTypes.lastIndex) {
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketTypeRow(
    ticketType: TicketType,
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    val isAvailable = ticketType.remaining > 0
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(ticketType.name, style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text("${ticketType.remaining}/${ticketType.capacity} kaldı",
                style = MaterialTheme.typography.labelSmall,
                color = if (isAvailable) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.error)
            Text(TicketUtils.formatPrice(ticketType.priceCents),
                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.width(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledIconButton(
                onClick  = onDecrement,
                enabled  = quantity > 0,
                modifier = Modifier.size(32.dp),
                colors   = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor   = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = "Azalt",
                    modifier = Modifier.size(18.dp))
            }
            Text(quantity.toString(), style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center, modifier = Modifier.width(32.dp))
            FilledIconButton(
                onClick  = onIncrement,
                enabled  = isAvailable && quantity < ticketType.remaining,
                modifier = Modifier.size(32.dp),
                colors   = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor   = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(Icons.Outlined.AddCircle, contentDescription = "Artır",
                    modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        border    = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    if (value.isBlank()) return
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(36.dp)) {
            Icon(imageVector = icon, contentDescription = null,
                modifier = Modifier.padding(8.dp).size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}