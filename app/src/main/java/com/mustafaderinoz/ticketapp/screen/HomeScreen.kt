package com.mustafaderinoz.ticketapp.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafaderinoz.core.domain.event.Event
import com.mustafaderinoz.core.domain.ticket.PurchasedTicketUi
import com.mustafaderinoz.core.util.DateTimeUtils
import com.mustafaderinoz.core.util.TicketUtils
import com.mustafaderinoz.ticketapp.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    onNavigateToTicketDetail: (String) -> Unit = {},
    onNavigateToEventDetail: (String) -> Unit = {},
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
                topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Hoş Geldin 👋",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Çıkış Yap",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp)
        ) {
            SectionHeader(title = "Yaklaşan Etkinlikler")
            Spacer(Modifier.height(16.dp))
            EventsRow(
                isLoading = state.isEventsLoading,
                error = state.eventsError,
                events = state.events,
                onEventClick = onNavigateToEventDetail
            )

            Spacer(Modifier.height(32.dp))

            SectionHeader(title = "Satın Alınmış Biletler")
            Spacer(Modifier.height(16.dp))
            TicketsColumn(
                isLoading = state.isTicketsLoading,
                error = state.ticketsError,
                tickets = state.tickets,
                onTicketClick = onNavigateToTicketDetail
            )

        }
    }
}


// Bölüm başlıklarını (ör. "Yaklaşan Etkinlikler") göstermek için kullanılan özel metin bileşeni.
@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}
// İkon ve metni yan yana, hizalı bir şekilde gösteren yardımcı bileşen (ör. konum bilgisi için).
@Composable
private fun IconTextRow(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
// Olumlu (ör. "Kaldı") veya olumsuz (ör. "Tükendi") durumlara göre renk değiştiren durum etiketi.
@Composable
private fun StatusPill(text: String, isPositive: Boolean) {

    val containerColor = if (isPositive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    val contentColor = if (isPositive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer

    Surface(
        shape = CircleShape,
        color = containerColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

// Etkinlikleri yatayda kaydırılabilir şekilde (LazyRow) gösteren, yüklenme ve hata durumlarını yöneten liste.

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun EventsRow(
    isLoading: Boolean,
    error: String?,
    events: List<Event>,
    onEventClick: (String) -> Unit
) {
    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxWidth().height(250.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        }
        error != null -> {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }
        events.isEmpty() -> {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                Text(
                    text = "Şimdilik hiçbir etkinlik yok.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        else -> {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items = events, key = { it.id }) { event ->
                    EventCard(event = event, onClick = { onEventClick(event.id) })
                }
            }
        }
    }
}
// Tek bir etkinliğin adı, konumu, tarihi ve fiyatı gibi detaylarını gösteren kart tasarımı.
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun EventCard(event: Event,onClick: () -> Unit) {
    val initial = event.name.firstOrNull { it.isLetter() }?.uppercaseChar() ?: '?'
    val remaining = TicketUtils.totalRemaining(event.ticketTypes)
    val isAvailable = remaining > 0

    Card(
        onClick = onClick,
        modifier = Modifier.width(300.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Image/Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconTextRow(icon = Icons.Outlined.LocationOn, text = event.venue)

                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)

                // Dates
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DateBadge(icon = Icons.Outlined.DateRange, label = DateTimeUtils.formatDate(event.startsAt))
                    DateBadge(
                        icon = Icons.Outlined.CheckCircle,
                        label = "${DateTimeUtils.formatTime(event.startsAt)} – ${DateTimeUtils.formatTime(event.endsAt)}"
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Footer (Status & Price)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusPill(
                        text = if (isAvailable) "$remaining kaldı" else "Tükendi",
                        isPositive = isAvailable
                    )

                    TicketUtils.minPriceLabel(event.ticketTypes)?.let { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
// Tarih ve saat gibi verileri vurgulamak için kullanılan, ikonlu küçük rozet.
@Composable
private fun DateBadge(icon: ImageVector, label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Satın alınan biletleri alt alta listeleyen, yüklenme ve hata durumlarını yöneten bileşen.

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TicketsColumn(
    isLoading: Boolean,
    error: String?,
    tickets: List<PurchasedTicketUi>,
    onTicketClick: (String) -> Unit,
) {
    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        }
        error != null -> {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }
        tickets.isEmpty() -> {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                Text(
                    text = "Henüz satın alınmış bilet yok.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        else -> {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                tickets.forEach { ticket ->
                    PurchasedTicketCard(ticket =ticket, onClick = { onTicketClick(ticket.id) })
                }
            }
        }
    }
}
// Kullanıcının satın aldığı tek bir biletin bilgilerini ve geçerlilik durumunu gösteren tıklanabilir kart.
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun PurchasedTicketCard(
    ticket: PurchasedTicketUi,
    onClick: () -> Unit,
) {
    val isValid = ticket.status == "VALID"

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // QR Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Ticket Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = ticket.eventName.ifBlank { "Etkinlik bilgisi yükleniyor..." },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (ticket.ticketTypeName.isNotBlank()) {
                    Text(
                        text = ticket.ticketTypeName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (ticket.eventVenue.isNotBlank()) {
                    IconTextRow(icon = Icons.Outlined.LocationOn, text = ticket.eventVenue)
                }
            }

            // Status & Price
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusPill(text = ticket.status, isPositive = isValid)

                if (ticket.ticketTypePriceCents > 0L) {
                    Text(
                        text = TicketUtils.formatPrice(ticket.ticketTypePriceCents),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}