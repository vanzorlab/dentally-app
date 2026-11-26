package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.ChatMessageEntity
import com.example.ui.components.DentallyLogoIcon
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DentallyBorder
import com.example.ui.theme.DentallyTintBg
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.SageDark
import com.example.ui.theme.SageMedium
import com.example.ui.theme.SagePill
import com.example.ui.viewmodel.DentallyViewModel

private data class QuickStarterChip(
    val label: String,
    val prompt: String
)

private data class FollowUpOption(
    val label: String,
    val message: String,
    val isQuizChoice: Boolean = false
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DentallyChatScreen(
    viewModel: DentallyViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val learningProfile by viewModel.chatLearningProfile.collectAsStateWithLifecycle()
    val sharedKnowledge by viewModel.sharedAiKnowledge.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val quickStarters = remember {
        listOf(
            QuickStarterChip("Main Kuis", "Yuk main kuis tebak-tebakan seru!"),
            QuickStarterChip("Fakta Unik", "Spill fakta unik yang gokil dong!"),
            QuickStarterChip("Cek Progres", "Gimana progres sama skor gigiku hari ini?"),
            QuickStarterChip("Abis Jajan Manis", "Aku habis minum boba dan makan manis nih, aman gak?"),
            QuickStarterChip("Tips Sikat Gigi", "Gimana cara sikat gigi 2 menit yang santai tapi bersih?"),
            QuickStarterChip("Ngobrol Random", "Lagi gabut nih, ayo ngobrol random yuk!")
        )
    }

    LaunchedEffect(messages.size, isAiLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NaturalBackground)
            .imePadding()
    ) {
        // Chat Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(DentallyTintBg),
                        contentAlignment = Alignment.Center
                    ) {
                        DentallyLogoIcon(sizeDp = 30.dp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Dentally",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SageDark,
                                    fontSize = 16.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Online",
                                tint = AccentGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        val totalLearned = sharedKnowledge.totalCollectiveInteractions
                        val statusSubtitle = if (totalLearned > 0) {
                            "${learningProfile.closenessLevel} • Belajar dari $totalLearned obrolan"
                        } else {
                            "${learningProfile.closenessLevel} • Siap ngobrol apa aja"
                        }
                        Text(
                            text = statusSubtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MintPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }

                IconButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.testTag("clear_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus Riwayat",
                        tint = SageMedium
                    )
                }
            }
        }

        // Minimal playful safety note
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFF9E6))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info Medis",
                    tint = Color(0xFFB78103),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Temen ngobrol & edukasi santai — kalau sakit gigi parah, tetap cek ke dokter gigi ya!",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = Color(0xFF7A5400)
                    )
                )
            }
        }

        // Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(messages, key = { _, msg -> msg.id }) { index, message ->
                val isLastAssistant = !message.isUser && index == messages.lastIndex && !isAiLoading
                Column {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(220)) + slideInVertically(initialOffsetY = { it / 6 })
                    ) {
                        ChatMessageBubble(
                            message = message,
                            userAvatarUrl = userProfile.avatarUrl
                        )
                    }

                    if (isLastAssistant) {
                        val followUps = remember(message.id, message.text) {
                            computeContextualFollowUps(message.text)
                        }
                        if (followUps.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 40.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                followUps.forEachIndexed { fIdx, option ->
                                    val bgColor = if (option.isQuizChoice) DentallyTintBg else Color.White
                                    val borderColor = if (option.isQuizChoice) NaturalPrimary else DentallyBorder
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(bgColor)
                                            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                                            .clickable {
                                                viewModel.sendMessageToDentally(option.message, option.label)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 7.dp)
                                            .testTag("chat_followup_$fIdx")
                                    ) {
                                        Text(
                                            text = option.label,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = SageDark,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isAiLoading) {
                item {
                    BouncingTypingIndicator()
                }
            }
        }

        // Quick Suggestion Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickStarters) { starter ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, DentallyBorder, RoundedCornerShape(16.dp))
                        .clickable(enabled = !isAiLoading) {
                            viewModel.sendMessageToDentally(starter.prompt, starter.label)
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = starter.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SageDark,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        // Input Field
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = SageDark,
                        fontSize = 14.5.sp
                    ),
                    placeholder = {
                        Text(
                            "Ketik apa aja, curhat, atau tanya gigi...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = SageMedium,
                                fontSize = 13.5.sp
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank() && !isAiLoading) {
                                val msg = inputText.trim()
                                inputText = ""
                                viewModel.sendMessageToDentally(msg)
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SageDark,
                        unfocusedTextColor = SageDark,
                        cursorColor = NaturalPrimary,
                        focusedPlaceholderColor = SageMedium,
                        unfocusedPlaceholderColor = SageMedium,
                        focusedBorderColor = NaturalPrimary,
                        unfocusedBorderColor = DentallyBorder,
                        focusedContainerColor = NaturalBackground,
                        unfocusedContainerColor = NaturalBackground
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val msg = inputText.trim()
                            inputText = ""
                            viewModel.sendMessageToDentally(msg)
                        }
                    },
                    enabled = inputText.isNotBlank() && !isAiLoading,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank() && !isAiLoading) NaturalPrimary else DentallyBorder)
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Kirim",
                        tint = if (inputText.isNotBlank() && !isAiLoading) Color.White else SageMedium,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = {
                    Text(
                        text = "Mulai Obrolan Baru?",
                        fontWeight = FontWeight.Bold,
                        color = SageDark
                    )
                },
                text = {
                    Text(
                        text = "Riwayat chat yang lama bakal dibersihkan biar mulai dari awal lagi. Lanjut?",
                        style = MaterialTheme.typography.bodyMedium.copy(color = SageDark)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearChat()
                            showClearDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text("Ya, Bersihkan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) {
                        Text("Batal", color = SageMedium)
                    }
                }
            )
        }
    }
}

@Composable
private fun BouncingTypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing_dots")
    val dot1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(350),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, delayMillis = 120),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, delayMillis = 240),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(DentallyTintBg),
            contentAlignment = Alignment.Center
        ) {
            DentallyLogoIcon(sizeDp = 22.dp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                listOf(dot1Offset, dot2Offset, dot3Offset).forEachIndexed { idx, offsetY ->
                    Box(
                        modifier = Modifier
                            .offset(y = offsetY.dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(MintPrimary)
                    )
                    if (idx < 2) Spacer(modifier = Modifier.width(4.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Dentally lagi ngetik...",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SageMedium,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessageEntity,
    userAvatarUrl: String = ""
) {
    val isUser = message.isUser
    val formattedText = remember(message.text) {
        // Hide internal quiz marker tag like "[Kuis #1]" from visual bubble while keeping it in Room history for answer checking
        val display = message.text.replace(Regex("""\[Kuis #\d+\]\s*"""), "")
        buildFormattedChatText(display)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(DentallyTintBg),
                contentAlignment = Alignment.Center
            ) {
                DentallyLogoIcon(sizeDp = 22.dp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 310.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .background(if (isUser) NaturalPrimary else Color.White)
                .padding(horizontal = 15.dp, vertical = 11.dp)
        ) {
            Text(
                text = formattedText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isUser) Color.White else SageDark,
                    lineHeight = 21.sp,
                    fontSize = 13.5.sp
                )
            )
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFE4E7)),
                contentAlignment = Alignment.Center
            ) {
                if (userAvatarUrl.isNotBlank()) {
                    AsyncImage(
                        model = userAvatarUrl,
                        contentDescription = "Foto Profil Pengguna",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Pengguna",
                        tint = SageDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun computeContextualFollowUps(botMessage: String): List<FollowUpOption> {
    val hasQuizChoices = botMessage.contains("A)") && botMessage.contains("B)") && botMessage.contains("C)")
    if (hasQuizChoices) {
        return listOf(
            FollowUpOption("Pilih A", "A", isQuizChoice = true),
            FollowUpOption("Pilih B", "B", isQuizChoice = true),
            FollowUpOption("Pilih C", "C", isQuizChoice = true)
        )
    }

    val lower = botMessage.lowercase()
    return when {
        lower.contains("kuis") || lower.contains("benar banget") || lower.contains("hampir kena") -> listOf(
            FollowUpOption("Kuis Lagi", "Gas kasih kuis berikutnya dong!"),
            FollowUpOption("Fakta Unik", "Spill fakta unik yang gokil dong!"),
            FollowUpOption("Topik Bebas", "Kita ngobrol topik random yuk!")
        )
        lower.contains("fun fact") || lower.contains("fakta") -> listOf(
            FollowUpOption("Fakta Lain", "Spill fakta unik lainnya dong!"),
            FollowUpOption("Tes Kuis", "Yuk main kuis tebak-tebakan seru!"),
            FollowUpOption("Pantun / Joke", "Kasih joke atau pantun receh dong!")
        )
        lower.contains("boba") || lower.contains("jajanan") || lower.contains("manis") -> listOf(
            FollowUpOption("Aman gak tiap hari?", "Kalau minum manis tiap hari aman gak?"),
            FollowUpOption("Main Kuis", "Yuk main kuis tebak-tebakan seru!"),
            FollowUpOption("Cek Progres", "Gimana progres sama skor gigiku hari ini?")
        )
        else -> listOf(
            FollowUpOption("Main Kuis", "Yuk main kuis tebak-tebakan seru!"),
            FollowUpOption("Fakta Unik", "Spill fakta unik yang gokil dong!"),
            FollowUpOption("Joke Receh", "Kasih joke atau pantun receh dong!")
        )
    }
}

/**
 * Parses lightweight **bold** and *italic* markdown spans cleanly for mobile chat bubbles.
 */
internal fun buildFormattedChatText(raw: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < raw.length) {
            if (i + 1 < raw.length && raw[i] == '*' && raw[i + 1] == '*') {
                val end = raw.indexOf("**", startIndex = i + 2)
                if (end > i + 2) {
                    val startPos = length
                    append(raw.substring(i + 2, end))
                    addStyle(SpanStyle(fontWeight = FontWeight.Bold), startPos, length)
                    i = end + 2
                    continue
                }
            }
            if (raw[i] == '*') {
                val end = raw.indexOf('*', startIndex = i + 1)
                if (end > i + 1 && !raw.substring(i + 1, end).contains('\n')) {
                    val startPos = length
                    append(raw.substring(i + 1, end))
                    addStyle(SpanStyle(fontStyle = FontStyle.Italic), startPos, length)
                    i = end + 1
                    continue
                }
            }
            append(raw[i])
            i++
        }
    }
}
