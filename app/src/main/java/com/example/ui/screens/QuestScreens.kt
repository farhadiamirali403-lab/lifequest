package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.*
import com.example.ui.QuestViewModel
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.Translations
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// Interactive RPG Sub-Tabs
enum class HomeSubTab(val icon: String, val labelEn: String, val labelFa: String, val labelDe: String) {
    QUESTS("📜", "Quests", "ماموریت‌ها", "Quests"),
    HABITS("🌱", "Habits", "عادت‌ها", "Gewohnheiten"),
    GOALS("🎯", "Goals", "اهداف", "Ziele"),
    MAP("🗺️", "World Map", "نقشه دنیا", "Weltkarte"),
    ORACLE("🔮", "AI Oracle", "اوراکل هوش مصنوعی", "AI Orakel"),
    LEADERBOARD("👑", "Rankings", "رتبه‌بندی", "Rangliste"),
    BATTLE("⚔️", "Arena", "میدان مبارزه", "Kampfarena"),
    CHALLENGES("📜", "Challenges", "چالش‌ها", "Herausforderung"),
    SKILLS("✨", "Skills", "مهارت‌ها", "Fertigkeiten"),
    GUILD("🏰", "Guild", "صنف (Guild)", "Gilde"),
    INSIGHTS("📊", "Insights", "آمارها", "Einblicke")
}

// ==========================================
// 1. CHARACTER STATUS HEADER PANEL
// ==========================================
@Composable
fun HeroHeader(
    profile: PlayerProfile,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val xpNeeded = profile.level * 100
    val progress = if (xpNeeded > 0) profile.xp.toFloat() / xpNeeded else 0f
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(16.dp), ambientColor = GlowingCyan, spotColor = GlowingCyan),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(GlowingCyan, LegendaryGold)))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Large Avatar circle selector
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, LegendaryGold, CircleShape)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                // Determine Avatar visual type
                when (profile.avatarUrl) {
                    "ic_rpg_hero" -> {
                        Image(
                            painter = painterResource(id = R.drawable.ic_rpg_hero),
                            contentDescription = "Hero Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    "🧙‍♂️" -> Text("🧙‍♂️", fontSize = 42.sp)
                    "⚔️" -> Text("⚔️", fontSize = 42.sp)
                    "🗡️" -> Text("🗡️", fontSize = 42.sp)
                    "👑" -> Text("👑", fontSize = 42.sp)
                    "🏹" -> Text("🏹", fontSize = 42.sp)
                    else -> {
                        Image(
                            painter = painterResource(id = R.drawable.ic_rpg_hero),
                            contentDescription = "Hero Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = profile.heroName,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(QuestRed.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                            .border(1.dp, QuestRed.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🔥 ${profile.streak} ${Translations.getString("days", language)}",
                            color = QuestRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${Translations.getString("level", language)} ${profile.level} (${profile.characterClass})",
                        color = LegendaryGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    if (profile.talentPoints > 0) {
                        Text(
                            text = "⭐ ${profile.talentPoints} TP",
                            color = GlowingEmerald,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(7.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .background(Brush.horizontalGradient(listOf(GlowingCyan, GlowingEmerald)))
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "${profile.xp}/$xpNeeded",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. QUEST CARDS (INTERACTIVE & RIPPLE)
// ==========================================
@Composable
fun ScrollQuestCard(
    quest: Quest,
    language: AppLanguage,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderGlow = when (quest.difficulty) {
        "Easy", "آسان", "Einfach" -> Color.Gray
        "Medium", "متوسط", "Mittel" -> GlowingCyan
        "Hard", "سخت", "Schwer" -> QuestRed
        "Epic", "حماسی", "Epos" -> LegendaryGold
        else -> BorderSilver
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, borderGlow.copy(alpha = 0.8f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = quest.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = quest.description,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
                
                IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_quest_item")) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = QuestRed.copy(alpha = 0.8f))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Tag
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(borderGlow.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .border(1.dp, borderGlow.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        val displayCat = if (language == AppLanguage.FA) {
                            when (quest.category) {
                                "Strength" -> "قدرت"
                                "Discipline" -> "انضباط"
                                "Learning" -> "یادگیری"
                                "Health" -> "سلامت"
                                else -> quest.category
                            }
                        } else quest.category

                        val displayDiff = if (language == AppLanguage.FA) {
                            when (quest.difficulty) {
                                "Easy", "آسان" -> "آسان"
                                "Medium", "متوسط" -> "متوسط"
                                "Hard", "سخت" -> "سخت"
                                "Epic", "حماسی" -> "حماسی"
                                else -> quest.difficulty
                            }
                        } else quest.difficulty

                        Text(
                            text = String.format("%s • %s", displayCat, displayDiff),
                            color = if (quest.difficulty == "Epic") LegendaryGold else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                if (!quest.isCompleted) {
                    Button(
                        onClick = onComplete,
                        colors = ButtonDefaults.buttonColors(containerColor = GlowingCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("complete_quest_item")
                    ) {
                        Text(
                            text = Translations.getString("complete_quest", language),
                            color = DarkVoid,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = "Completed", tint = GlowingEmerald)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = Translations.getString("status_completed", language),
                            color = GlowingEmerald,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. HOME SCREEN GRID CENTRAL CONTROL
// ==========================================
@Composable
fun HomeScreen(
    viewModel: QuestViewModel,
    language: AppLanguage,
    onNavigateToAdd: () -> Unit
) {
    val quests by viewModel.allQuests.collectAsState()
    val habits by viewModel.allHabits.collectAsState()
    val goals by viewModel.allGoals.collectAsState()
    val boss by viewModel.activeBoss.collectAsState()
    val challenges by viewModel.allChallenges.collectAsState()
    val profile by viewModel.playerProfile.collectAsState()
    
    val notifications by viewModel.notifications.collectAsState()
    val guildChatMsg by viewModel.guildChatMessages.collectAsState()

    var activeSubTab by remember { mutableStateOf(HomeSubTab.QUESTS) }
    var showNotifBox by remember { mutableStateOf(false) }
    var showBountyRewardGrid by remember { mutableStateOf(true) }

    // Habit Inputs
    var habitTitle by remember { mutableStateOf("") }
    var habitDiff by remember { mutableStateOf("Medium") }
    var habitCat by remember { mutableStateOf("Health") }

    // Goal Inputs
    var goalTitle by remember { mutableStateOf("") }
    var goalSteps by remember { mutableStateOf("5") }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Character Status Header Panel
        HeroHeader(profile = profile, language = language)
        
        Spacer(modifier = Modifier.height(14.dp))

        // Advanced expandable notification box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showNotifBox = !showNotifBox },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔔", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language.code == "fa") "دفترچه هشدارهای قهرمان (آخرین رویدادها)" else "Ledger of Heroic Logs",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(if (showNotifBox) "▲" else "▼", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                }

                if (showNotifBox) {
                    Spacer(modifier = Modifier.height(10.dp))
                    if (notifications.isEmpty()) {
                        Text(
                            text = if (language == AppLanguage.FA) "هیچ لاگ و هشداری در دسترس نیست." else "No logs available.", 
                            fontSize = 12.sp, 
                            color = Color.Gray
                        )
                    } else {
                        notifications.forEach { text ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("• ", color = LegendaryGold, fontWeight = FontWeight.Bold)
                                Text(
                                    text = text,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Story progress info bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, GlowingCyan.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = viewModel.getStoryChapter(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = GlowingEmerald
                    )
                    Text(
                        text = if (language == AppLanguage.FA) "${(viewModel.getStoryProgress() * 100).toInt()}% انجام شده" else "${(viewModel.getStoryProgress() * 100).toInt()}% Done",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = viewModel.getStoryProgress(),
                    color = glowingPurpleLightMode(profile.isDarkMode),
                    trackColor = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // RETENTION & DAILY LOGIN REWARDS CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.2.dp, LegendaryGold.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showBountyRewardGrid = !showBountyRewardGrid },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📅", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language.code == "fa") "جوایز حضور روزانه (۷ روز پیاپی)" else "7-Day Daily Login Bounty Track",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = LegendaryGold
                        )
                    }
                    Text(if (showBountyRewardGrid) "▲" else "▼", fontSize = 11.sp, color = LegendaryGold)
                }

                if (showBountyRewardGrid) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (language.code == "fa") "هر روز برای ادعای پاداش ضربه بزنید! روند صعود شما طلا، جواهر و صندوقچه‌ها را باز می کند." else "Unlock premium items, gold, gems, and legendary bounty packs everyday!",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        lineHeight = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val bountyDays = listOf(
                        Triple(1, "Day 1", "🪙 50 / 📦"),
                        Triple(2, "Day 2", "💎 10"),
                        Triple(3, "Day 3", "🪙 100 / 📦"),
                        Triple(4, "Day 4", "🪙 150 / 🟡"),
                        Triple(5, "Day 5", "💎 25"),
                        Triple(6, "Day 6", "🪙 200 / 🟡"),
                        Triple(7, "Day 7", "💎 50 / 🔮")
                    )

                    val claimProgress = profile.dailyRewardsClaimProgress

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        bountyDays.forEach { (dayIdx, title, gift) ->
                            val isClaimed = dayIdx <= claimProgress
                            val isCurrentTab = dayIdx == claimProgress + 1
                            val boxBorderColor = if (isCurrentTab) GlowingCyan else if (isClaimed) GlowingEmerald.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.2f)
                            
                            Card(
                                modifier = Modifier
                                    .width(76.dp)
                                    .clickable(enabled = isCurrentTab) {
                                        viewModel.claimDailyBountyTrack(dayIdx)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCurrentTab) GlowingCyan.copy(alpha = 0.1f) else if (isClaimed) GlowingEmerald.copy(alpha = 0.05f) else MaterialTheme.colorScheme.background
                                ),
                                border = BorderStroke(1.2.dp, boxBorderColor)
                            ) {
                                Column(
                                    modifier = Modifier.padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (language.code == "fa") "روز $dayIdx" else title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isCurrentTab) GlowingCyan else if (isClaimed) GlowingEmerald else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = gift,
                                        fontSize = 9.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    if (isClaimed) {
                                        Text("✓", color = GlowingEmerald, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    } else if (isCurrentTab) {
                                        Text("CLAIM", color = GlowingCyan, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                                    } else {
                                        Text("🔒", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.surfaceVariant))
                    Spacer(modifier = Modifier.height(12.dp))

                    // WEEKLY AND MONTHLY CHESTS PROGRESS
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Weekly Progress Card
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                            border = BorderStroke(1.dp, AmethystPurple.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (language.code == "fa") "🎁 مأموریت هفتگی" else "🎁 Weekly Chest",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmethystPurple
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (language.code == "fa") "تکمیل ۵ فعالیت هفته" else "Complete 5 tasks/habits done",
                                    fontSize = 9.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                val progressWeeklyRatio = (profile.totalQuestsCompleted % 5).toFloat() / 5f
                                LinearProgressIndicator(
                                    progress = progressWeeklyRatio,
                                    color = AmethystPurple,
                                    trackColor = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                val isWeeklyClaimable = (profile.totalQuestsCompleted >= 5) && !profile.weeklyChestClaimed
                                Button(
                                    onClick = { viewModel.claimWeeklyProgressChest() },
                                    enabled = isWeeklyClaimable,
                                    colors = ButtonDefaults.buttonColors(containerColor = AmethystPurple),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.fillMaxWidth().height(26.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = if (profile.weeklyChestClaimed) "CLAIMED" else if (isWeeklyClaimable) "CLAIM NOW" else "${(profile.totalQuestsCompleted % 5)}/5 DONE",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Monthly Progress Card
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                            border = BorderStroke(1.dp, LegendaryGold.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (language.code == "fa") "👑 مأموریت ماهانه" else "👑 Monthly Vault",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LegendaryGold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (language.code == "fa") "تکمیل ۱۵ فعالیت ماه" else "Complete 15 tasks/habits done",
                                    fontSize = 9.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                val progressMonthlyRatio = (profile.totalQuestsCompleted % 15).toFloat() / 15f
                                LinearProgressIndicator(
                                    progress = progressMonthlyRatio,
                                    color = LegendaryGold,
                                    trackColor = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                val isMonthlyClaimable = (profile.totalQuestsCompleted >= 15) && !profile.monthlyChestClaimed
                                Button(
                                    onClick = { viewModel.claimMonthlyProgressChest() },
                                    enabled = isMonthlyClaimable,
                                    colors = ButtonDefaults.buttonColors(containerColor = LegendaryGold),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.fillMaxWidth().height(26.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = if (profile.monthlyChestClaimed) "CLAIMED" else if (isMonthlyClaimable) "CLAIM NOW" else "${(profile.totalQuestsCompleted % 15)}/15 DONE",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal scrolling tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HomeSubTab.values().forEach { sub ->
                val tabSelected = activeSubTab == sub
                val labelText = when (language.code) {
                    "fa" -> sub.labelFa
                    "de" -> sub.labelDe
                    else -> sub.labelEn
                }
                
                FilterChip(
                    selected = tabSelected,
                    onClick = { activeSubTab = sub },
                    label = {
                        Text(
                            text = "${sub.icon} $labelText",
                            fontWeight = if (tabSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GlowingCyan.copy(alpha = 0.15f),
                        selectedLabelColor = GlowingCyan
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content Router
        when (activeSubTab) {
            HomeSubTab.MAP -> {
                WorldMapScreen(viewModel = viewModel, language = language)
            }
            HomeSubTab.LEADERBOARD -> {
                LeaderboardScreen(viewModel = viewModel, language = language)
            }
            HomeSubTab.QUESTS -> {
                // Core Quests tab
                val activeQuests = quests.filter { !it.isCompleted }
                val completedQuests = quests.filter { it.isCompleted }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📜 ${Translations.getString("status_active", language)} (${activeQuests.size})",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Button(
                        onClick = onNavigateToAdd,
                        colors = ButtonDefaults.buttonColors(containerColor = GlowingCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(Translations.getString("add_tab", language), color = DarkVoid, fontWeight = FontWeight.Bold, fontSize = 12.dp.value.sp)
                    }
                }

                if (activeQuests.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Refresh, contentDescription = "Empty", tint = Color.Gray, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = Translations.getString("empty_quests", language),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }
                } else {
                    activeQuests.forEach { quest ->
                        ScrollQuestCard(
                            quest = quest,
                            language = language,
                            onComplete = { viewModel.completeQuest(quest) },
                            onDelete = { viewModel.deleteQuest(quest) }
                        )
                    }
                }

                if (completedQuests.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "🏆 ${Translations.getString("status_completed", language)} (${completedQuests.size})",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    completedQuests.forEach { quest ->
                        ScrollQuestCard(
                            quest = quest,
                            language = language,
                            onComplete = {},
                            onDelete = { viewModel.deleteQuest(quest) }
                        )
                    }
                }
            }

            HomeSubTab.HABITS -> {
                // Habits tracking tab
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (language == AppLanguage.FA) "ساخت عادت جدید" else "Structure New Habit", 
                            fontWeight = FontWeight.Bold, 
                            color = LegendaryGold, 
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        OutlinedTextField(
                            value = habitTitle,
                            onValueChange = { habitTitle = it },
                            placeholder = { Text(if (language == AppLanguage.FA) "به عنوان مثال: نوشیدن ۲ لیتر آب / ورزش روزانه" else "E.g. Drink 2L water / Avoid sugary drinks", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GlowingCyan,
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Difficulty Dropdowns simulation
                            Column(modifier = Modifier.weight(1f)) {
                                Text(if (language == AppLanguage.FA) "درجه سختی" else "Difficulty", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                        .clickable {
                                            habitDiff = if (habitDiff == "Easy") "Medium" else if (habitDiff == "Medium") "Hard" else "Easy"
                                        }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val diffDisp = if (language == AppLanguage.FA) {
                                        when (habitDiff) {
                                            "Easy" -> "آسان"
                                            "Medium" -> "متوسط"
                                            "Hard" -> "سخت"
                                            else -> habitDiff
                                        }
                                    } else habitDiff
                                    Text(diffDisp, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("↕", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(if (language == AppLanguage.FA) "تمرکز ویژگی" else "Attribute", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                        .clickable {
                                            habitCat = when (habitCat) {
                                                "Health" -> "Fitness"
                                                "Fitness" -> "Learning"
                                                "Learning" -> "Finance"
                                                "Finance" -> "Career"
                                                "Career" -> "Relationships"
                                                else -> "Health"
                                            }
                                        }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val catDisp = if (language == AppLanguage.FA) {
                                        when (habitCat) {
                                            "Health" -> "تندرستی و سلامت"
                                            "Fitness" -> "تناسب اندام ورزشی"
                                            "Learning" -> "آموزش و یادگیری"
                                            "Finance" -> "مدیریت امور مالی"
                                            "Career" -> "صنف و مسیر حرفه‌ای"
                                            "Relationships" -> "خانواده و روابط"
                                            else -> habitCat
                                        }
                                    } else habitCat
                                    Text(catDisp, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("↕", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (habitTitle.isNotBlank()) {
                                    viewModel.addHabit(habitTitle, habitCat, habitDiff)
                                    habitTitle = ""
                                } else {
                                    val toastMsg = if (language == AppLanguage.FA) "لطفا عنوان عادت را وارد کنید!" else "Please enter habit title!"
                                    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GlowingCyan),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (language == AppLanguage.FA) "ثبت عادت رفتاری" else "Forge Neural Routine", fontWeight = FontWeight.Bold, color = DarkVoid)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (language == AppLanguage.FA) "عادت‌های رفتاری فعال شما" else "Your Active Behavioral Habits", 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 16.sp, 
                    color = MaterialTheme.colorScheme.onSurface, 
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (habits.isEmpty()) {
                    Text(
                        text = if (language == AppLanguage.FA) "هنوز عادتی ثبت نشده است. روتین‌های روزانه بسازید تا تجربه غیرفعال کسب کنید!" else "No habits formulated yet. Build routines to grind passive XP daily!", 
                        fontSize = 13.sp, 
                        color = Color.Gray, 
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                } else {
                    habits.forEach { b ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(b.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    
                                    val catDisp = if (language == AppLanguage.FA) {
                                        when (b.category) {
                                            "Health" -> "تندرستی و سلامت"
                                            "Fitness" -> "تناسب اندام ورزشی"
                                            "Learning" -> "آموزش و یادگیری"
                                            "Finance" -> "مدیریت امور مالی"
                                            "Career" -> "صنف و مسیر حرفه‌ای"
                                            "Relationships" -> "خانواده و روابط"
                                            else -> b.category
                                        }
                                    } else b.category

                                    val diffDisp = if (language == AppLanguage.FA) {
                                        when (b.difficulty) {
                                            "Easy" -> "آسان"
                                            "Medium" -> "متوسط"
                                            "Hard" -> "سخت"
                                            else -> b.difficulty
                                        }
                                    } else b.difficulty

                                    Text("$catDisp • ${if (language == AppLanguage.FA) "درجه" else "Diff"}: $diffDisp", fontSize = 12.sp, color = Color.Gray)
                                    Text(
                                        text = if (language == AppLanguage.FA) "🟢 مثبت: ${b.positiveCount}  |  🔴 منفی: ${b.negativeCount}" else "🟢 Positive: ${b.positiveCount}  |  🔴 Negative: ${b.negativeCount}", 
                                        fontSize = 11.sp, 
                                        color = GlowingCyan, 
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // Plus Action
                                    IconButton(
                                        onClick = { viewModel.tickHabit(b, true) },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(GlowingEmerald.copy(alpha = 0.2f), CircleShape)
                                            .border(1.dp, GlowingEmerald, CircleShape)
                                    ) {
                                        Text("+", color = GlowingEmerald, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                    }

                                    // Minus Action
                                    IconButton(
                                        onClick = { viewModel.tickHabit(b, false) },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(QuestRed.copy(alpha = 0.2f), CircleShape)
                                            .border(1.dp, QuestRed, CircleShape)
                                    ) {
                                        Text("-", color = QuestRed, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                    }

                                    // Clear habit
                                    IconButton(onClick = { viewModel.deleteHabit(b) }, modifier = Modifier.size(34.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HomeSubTab.GOALS -> {
                // Goals tracking tab
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (language == AppLanguage.FA) "تعریف نقشه هدف بلندمدت" else "Initialize Long Term Goal Blueprint", 
                            fontWeight = FontWeight.Bold, 
                            color = LegendaryGold, 
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        OutlinedTextField(
                            value = goalTitle,
                            onValueChange = { goalTitle = it },
                            placeholder = { Text(if (language == AppLanguage.FA) "مثال: خواندن ۵ کتاب / ساخت وبسایت پورتفولیو" else "E.g. Read 5 Books / Build Portfolio Website", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GlowingCyan,
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(if (language == AppLanguage.FA) "تعداد گام‌های موردنیاز" else "Total steps required", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                OutlinedTextField(
                                    value = goalSteps,
                                    onValueChange = { goalSteps = it },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GlowingCyan,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val steps = goalSteps.toIntOrNull() ?: 5
                                if (goalTitle.isNotBlank()) {
                                    viewModel.addGoal(goalTitle, steps, "Personal Growth")
                                    goalTitle = ""
                                } else {
                                    val errGoal = if (language == AppLanguage.FA) "لطفا عنوان نقشه هدف را وارد کنید!" else "Please enter goal blueprint title!"
                                    Toast.makeText(context, errGoal, Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GlowingCyan),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (language == AppLanguage.FA) "کتیبه‌نویسی نقشه اهداف حماسی" else "Chronicle Epic Milestone Blueprint", fontWeight = FontWeight.Bold, color = DarkVoid)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (language == AppLanguage.FA) "نقاط عطف بزرگ زندگی شما" else "Your Grand Life Milestones", 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 16.sp, 
                    color = MaterialTheme.colorScheme.onSurface, 
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (goals.isEmpty()) {
                    Text(
                        text = if (language == AppLanguage.FA) "هنوز دستاوردی ثبت نشده است. برای دریافت غنائم حماسی، اهداف بالاتری تنظیم کنید!" else "No massive goals charted yet. Aim higher to receive epic loot bounty!", 
                        fontSize = 13.sp, 
                        color = Color.Gray, 
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                } else {
                    goals.forEach { g ->
                        val pct = if (g.targetValue > 0) g.currentValue.toFloat() / g.targetValue else 0f
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(g.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        
                                        val catDisp = if (language == AppLanguage.FA) {
                                            if (g.category == "Personal Growth") "رشد فردی" else g.category
                                        } else g.category
                                        val progressDisp = if (language == AppLanguage.FA) "پیشرفت" else "Progress"

                                        Text("$catDisp • $progressDisp: ${g.currentValue}/${g.targetValue}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                    
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (!g.completed) {
                                            IconButton(
                                                onClick = { viewModel.incrementGoalStep(g) },
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .background(GlowingCyan.copy(alpha = 0.2f), CircleShape)
                                                    .border(1.dp, GlowingCyan, CircleShape)
                                            ) {
                                                Text("+1", color = GlowingCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            }
                                        } else {
                                            Text(if (language == AppLanguage.FA) "تکمیل شده 🏆" else "Completed 🏆", color = LegendaryGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        IconButton(onClick = { viewModel.deleteGoal(g) }, modifier = Modifier.size(34.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                LinearProgressIndicator(
                                    progress = pct,
                                    color = GlowingCyan,
                                    trackColor = MaterialTheme.colorScheme.background,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            HomeSubTab.BATTLE -> {
                // Battle Boss battles active arena
                if (boss == null) {
                    Text(if (language == AppLanguage.FA) "در حال حاضر هیچ هیولای فعالی مرزهای سرزمین را تهدید نمی‌کند." else "No active monster threatens the realm right now.", color = Color.Gray)
                } else {
                    val b = boss!!
                    val bossProgress = b.currentHp.toFloat() / b.maxHp
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(2.dp, QuestRed)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (language == AppLanguage.FA) "👹 غول مرحله کنونی میدان مبارزه 👹" else "👹 ACTIVE ARENA BOSS DECK 👹",
                                color = QuestRed,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .background(MaterialTheme.colorScheme.background, CircleShape)
                                    .border(2.dp, QuestRed, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (b.name.contains("Sloth")) "🦥" else "💀",
                                    fontSize = 48.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val showBossName = if (language == AppLanguage.FA) {
                                if (b.name == "Sloth Demon") "دیو تنبلی و سستی" else b.name
                            } else b.name

                            Text(showBossName, fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                            Text(if (language == AppLanguage.FA) "سطح ${b.level} هیولای غول‌پیکر" else "Level ${b.level} Behemoth Monster", fontSize = 12.sp, color = Color.Gray)

                            Spacer(modifier = Modifier.height(12.dp))

                            // HP gauge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(if (language == AppLanguage.FA) "جان: ${b.currentHp}/${b.maxHp}" else "HP: ${b.currentHp}/${b.maxHp}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = QuestRed)
                                Text(if (language == AppLanguage.FA) "باقیمانده: ${(bossProgress * 100).toInt()}%" else "${(bossProgress * 100).toInt()}% Remaining", fontSize = 11.sp, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = bossProgress,
                                color = QuestRed,
                                trackColor = MaterialTheme.colorScheme.background,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(CircleShape)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(if (language == AppLanguage.FA) "جایزه تجربه" else "XP Bounty", fontSize = 11.sp, color = Color.Gray)
                                        Text("+${b.rewardXp} XP", fontWeight = FontWeight.Bold, color = GlowingCyan, fontSize = 14.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(if (language == AppLanguage.FA) "سکه طلای غنیمت" else "Gold Slay", fontSize = 11.sp, color = Color.Gray)
                                        Text("+${b.rewardGold} G", fontWeight = FontWeight.Bold, color = LegendaryGold, fontSize = 14.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = if (language == AppLanguage.FA) "⚔️ با انجام واقعی عادات و ماموریت‌های روزانه خود، ضربات مهلکی به این غول وارد کنید!" else "⚔️ Completing your Quests and Habits deals massive damage to this Behemoth!",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                        }
                    }
                }
            }

            HomeSubTab.CHALLENGES -> {
                // Interactive progressive challenges board
                Text(
                    text = if (language == AppLanguage.FA) "پیشخوان چالش‌ها و حملات فصلی قهرمان" else "Quest Board Challenges & Seasonal Raid Events", 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 15.sp, 
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                challenges.forEach { challenge ->
                    val cPct = if (challenge.targetValue > 0) challenge.progress.toFloat() / challenge.targetValue else 0f
                    val headerColors = when (challenge.type) {
                        "Weekly" -> GlowingCyan
                        "Monthly" -> AmethystPurple
                        else -> LegendaryGold
                    }
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, headerColors.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(headerColors.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        val typeDisp = if (language == AppLanguage.FA) {
                                            when (challenge.type) {
                                                "Weekly" -> "هفتگی"
                                                "Monthly" -> "ماهانه"
                                                else -> "فصلی"
                                            }
                                        } else challenge.type
                                        Text(typeDisp, color = headerColors, fontWeight = FontWeight.Black, fontSize = 10.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))

                                    val dispTitle = if (language == AppLanguage.FA) {
                                        when (challenge.title) {
                                            "Morning Routine Devotion" -> "پایبندی به روتین فجر"
                                            "Elite Scholar Ritual" -> "آیین دانش‌پژوه نخبه"
                                            "Master of Health Goals" -> "برتری در اهداف تندرستی"
                                            else -> challenge.title
                                        }
                                    } else challenge.title

                                    Text(dispTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                
                                if (challenge.completed) {
                                    Text(if (language == AppLanguage.FA) "فتح شد! 🎉" else "Conquered! 🎉", color = GlowingEmerald, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            val dispReq = if (language == AppLanguage.FA) {
                                when {
                                    challenge.requirement.contains("health", ignoreCase = true) -> "تکمیل موفقیت‌آمیز ۵ ماموریت ورزشی یا انضباطی در این دوره."
                                    challenge.requirement.contains("skills", ignoreCase = true) -> "ارتقاء مهارت‌های فعال و افزایش غلبه بر عادات غیرمفید."
                                    else -> challenge.requirement
                                }
                            } else challenge.requirement

                            Text(dispReq, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(if (language == AppLanguage.FA) "پیشرفت: ${challenge.progress}/${challenge.targetValue}" else "Progress: ${challenge.progress}/${challenge.targetValue}", fontSize = 11.sp, color = Color.Gray)
                                Text(if (language == AppLanguage.FA) "جوایز: 🪙 ${challenge.rewardGold} سکه | ✨ ${challenge.rewardXp} تجربه" else "Rewards: 🪙 ${challenge.rewardGold}G | ✨ ${challenge.rewardXp}XP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LegendaryGold)
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = cPct,
                                color = headerColors,
                                trackColor = MaterialTheme.colorScheme.background,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                }
            }

            HomeSubTab.SKILLS -> {
                // Skill tree and talent system
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, LegendaryGold.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (language == AppLanguage.FA) "✨ درخت مهارتی اساطیری ✨" else "✨ ARCANE TALENT TREE SYSTEM ✨",
                            fontWeight = FontWeight.Black,
                            color = LegendaryGold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (language == AppLanguage.FA) "با هر بار ارتقاء سطح قهرمان، ١ امتیاز استعداد (TP) کسب کنید. آن‌ها را در اینجا خرج کنید تا قدرت‌های غیرفعال همیشگی باز کنید!" else "Earn 1 Talent Point (TP) on each character level up. Invest them here to unlock structural passive buffs!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Skill Tree Items
                val skillNodes = if (language == AppLanguage.FA) {
                    listOf(
                        Triple("sage_legacy", "میراث گرانبهای فرزانه ✨ (١ امتیاز)", "امتیاز تجربه (XP) تمامی ماموريت‌های تکمیل شده را ٢٠٪ افزایش می‌دهد."),
                        Triple("gilded_fortune", "ثروت بادآورده سرکش 🪙 (١ امتیاز)", "طلاهای کسب شده از تمامی ماموریت‌های تکمیل‌شده را ٢٠٪ افزایش می‌دهد."),
                        Triple("double_damage", "غول‌کش با قدرت دو برابر ⚔️ (٢ امتیاز)", "به هنگام انجام ماموریت‌ها و عادت‌ها، ۲ برابر آسیب به غول مرحله کنونی وارد می‌کند."),
                        Triple("phoenix_blessing", "برکت خدای ققنوس 🔥 (٢ امتیاز)", "تمام جوایز خریدهای دکان فانتزی و پاداش‌های روزانه را دو برابر می‌کند.")
                    )
                } else {
                    listOf(
                        Triple("sage_legacy", "Sage's Secret Legacy ✨ (1 TP)", "Increases all Quest completed XP rewards dynamically by +20%."),
                        Triple("gilded_fortune", "Rogue's Gilded Fortune 🪙 (1 TP)", "Increases all Quest completed Gold rewards dynamically by +20%."),
                        Triple("double_damage", "Giant Slayer Double Damage ⚔️ (2 TP)", "Deals x2 damage automatically to active Arena Bosses when quests/habits are done."),
                        Triple("phoenix_blessing", "Phoenix Lord Blessing 🔥 (2 TP)", "All claims in Fantasy Shop and Daily bounty are doubled.")
                    )
                }

                skillNodes.forEach { (nid, title, desc) ->
                    val unlocked = profile.unlockedSkills.contains(nid)
                    val costTp = if (nid == "sage_legacy" || nid == "gilded_fortune") 1 else 2
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (unlocked) GlowingCyan.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.2.dp, if (unlocked) GlowingCyan else MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (unlocked) GlowingCyan else MaterialTheme.colorScheme.onSurface
                                )
                                Text(desc, fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp)
                            }

                            Button(
                                onClick = { viewModel.unlockSkillNode(nid, costTp) },
                                enabled = !unlocked && profile.talentPoints >= costTp,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (unlocked) Color.Gray else GlowingCyan
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .height(34.dp)
                            ) {
                                Text(
                                    text = if (unlocked) {
                                        if (language == AppLanguage.FA) "باز شده ✓" else "Unlocked"
                                    } else {
                                        if (language == AppLanguage.FA) "یادگیری" else "Learn Node"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (unlocked) Color.White else DarkVoid
                                )
                            }
                        }
                    }
                }
            }

            HomeSubTab.GUILD -> {
                // Guild Hall simulation
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (language == AppLanguage.FA) "🏰 انجمن شهسواران بیدارگر" else "🏰 Mindful Sovereign Knights", 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 16.sp, 
                                    color = LegendaryGold
                                )
                                Text(
                                    text = if (language == AppLanguage.FA) "اتحادیه چندکاربره سطح ۳" else "Level 3 Multi-User Guild Setup", 
                                    fontSize = 11.sp, 
                                    color = Color.Gray
                                )
                            }
                            
                            Button(
                                onClick = { viewModel.performGuildCheckIn() },
                                colors = ButtonDefaults.buttonColors(containerColor = GlowingCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = if (language == AppLanguage.FA) "ثبت حضور" else "Check-In", 
                                    color = DarkVoid, 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (language == AppLanguage.FA) "هم‌رزم گرامی؛ گره همگام‌سازی چندکاربره پیشرفته هم‌اینک آماده است. به تالار آفلاین ملحق شوید و پاداش‌های قهرمان خود را به حالت پویا بررسی کنید." else "Future Multiplayed Sync Node: READY. Join offline channels and review your hero stats achievements dynamically.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Guild checklist status
                        Text(
                            text = if (language == AppLanguage.FA) "اعضای فعال قبیله (۳ نفر آنلاین)" else "Active Clan Members (3 Online)", 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 12.sp, 
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val membersList = if (language == AppLanguage.FA) {
                            listOf("Merlin_Prog (جادوگر بزرگ)", "Galahad_99 (مبارز سنگین)", "SelfCareQueen (طبیب هم‌پیمان)")
                        } else {
                            listOf("Merlin_Prog (Archmage)", "Galahad_99 (Champion)", "SelfCareQueen (Priest)")
                        }
                        membersList.forEach { m ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(m, fontSize = 12.sp, color = Color.Gray)
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(GlowingEmerald)
                                        .align(Alignment.CenterVertically)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Chat dynamic congrats feed
                Text(
                    text = if (language == AppLanguage.FA) "💬 فید گفتگوی زنده تبریک و صمیمیت هم‌رزمان" else "💬 Guild Live Chat Congratulation Feed", 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        guildChatMsg.forEach { (sender, text) ->
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                val dispSender = if (language == AppLanguage.FA) {
                                    when(sender) {
                                        "Merlin_Prog" -> "مرلین_فرزانه"
                                        "Galahad_99" -> "گالاهاد_قهرمان"
                                        "SelfCareQueen" -> "ملکه_تندرستی"
                                        else -> sender
                                    }
                                } else sender

                                val dispText = if (language == AppLanguage.FA) {
                                    text
                                        .replace("Cleared quest:", "ماموریت را با موفقیت تمام کرد:")
                                        .replace("Cleared quest", "ماموریت را با موفقیت تمام کرد:")
                                        .replace("Epic victory! Gold & XP distributed to allies!", "پیروزی بزرگ حماسی! غنائم طلا و تجربه به هم‌پیمانان تعلق گرفت!")
                                        .replace("Prestige Ascended!", "صعود پرستیژ یافت و نامش در زمره جاودانگان حک شد!")
                                        .replace("Leveled up to level", "به سطح والاتری از توانایی‌ها ترفیع یافت! سطح:")
                                        .replace("Unlocked skill node", "مهارت جدیدی را آموخت:")
                                        .replace("Checked in for daily quest bonuses!", "حضور روزانه را ثبت کرد و پاداش گرفت!")
                                } else text

                                Text("<$dispSender>: ", fontWeight = FontWeight.Black, color = GlowingCyan, fontSize = 12.sp)
                                Text(dispText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f))
                            }
                        }
                    }
                }
            }

            HomeSubTab.INSIGHTS -> {
                // Statistics analytics view
                Text(
                    text = if (language == AppLanguage.FA) "کتیبه تقویم عادات و داشبورد سنجه‌ها" else "Chronicle Calendar & Metrics Dashboard", 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.onSurface, 
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Calendar widget
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (language == AppLanguage.FA) "📅 شبکه تقویم تکمیل فعالیت‌ها" else "📅 Activity Completion Calendar Grid", 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 13.sp, 
                            color = LegendaryGold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val wkDays = if (language == AppLanguage.FA) {
                                listOf("شنبه", "۱ش", "۲ش", "۳ش", "۴ش", "۵ش", "جمعه")
                            } else {
                                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                            }
                            wkDays.forEachIndexed { i, d ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(d, fontSize = 10.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (i % 2 == 0) GlowingEmerald.copy(alpha = 0.25f) else MaterialTheme.colorScheme.background
                                            )
                                            .border(
                                                1.5.dp,
                                                if (i % 2 == 0) GlowingEmerald else Color.Gray.copy(alpha = 0.4f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (i % 2 == 0) {
                                            Text("✔", color = GlowingEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        } else {
                                            Text("${i + 12}", color = Color.Gray, fontSize = 9.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Draw XP Growth Trend chart on a custom Compose Canvas
                Card(
                    modifier = Modifier.fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (language == AppLanguage.FA) "📈 کتیبه روند انباشت تجربه قهرمان (XP Trend)" else "📈 Experience (XP) Growth Trend Line",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = LegendaryGold
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            val strokeWidthVal = 4f
                            val listPoints = listOf(30f, 60f, 45f, 95f, 130f, 150f, 210f)
                            val maxValP = 250f
                            val dx = size.width / (listPoints.size - 1)
                            
                            // Draw background grids
                            val gridLines = 4
                            for (gl in 0..gridLines) {
                                val yGrid = size.height * gl / gridLines
                                drawLine(
                                    color = Color.Gray.copy(alpha = 0.15f),
                                    start = androidx.compose.ui.geometry.Offset(0f, yGrid),
                                    end = androidx.compose.ui.geometry.Offset(size.width, yGrid),
                                    strokeWidth = 2f
                                )
                            }
                            
                            // Plot connection paths
                            for (ptIdx in 0 until listPoints.size - 1) {
                                val x1 = ptIdx * dx
                                val x2 = (ptIdx + 1) * dx
                                val y1 = size.height - (listPoints[ptIdx] / maxValP * size.height)
                                val y2 = size.height - (listPoints[ptIdx + 1] / maxValP * size.height)
                                drawLine(
                                    color = GlowingCyan,
                                    start = androidx.compose.ui.geometry.Offset(x1, y1),
                                    end = androidx.compose.ui.geometry.Offset(x2, y2),
                                    strokeWidth = strokeWidthVal
                                )
                                drawCircle(
                                    color = AmethystPurple,
                                    radius = 6f,
                                    center = androidx.compose.ui.geometry.Offset(x1, y1)
                                )
                            }
                            drawCircle(
                                color = AmethystPurple,
                                radius = 6f,
                                center = androidx.compose.ui.geometry.Offset(
                                    (listPoints.size - 1) * dx,
                                    size.height - (listPoints.last() / maxValP * size.height)
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val daysOfWeekStr = if (language == AppLanguage.FA) {
                                listOf("شنبه", "۱شنبه", "۲شنبه", "۳شنبه", "۴شنبه", "۵شنبه", "جمعه")
                            } else {
                                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                            }
                            daysOfWeekStr.forEach { daySym ->
                                Text(daySym, fontSize = 9.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Completion Rate bar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (language == AppLanguage.FA) "📊 نرخ آنالیز عملکرد قهرمان" else "📊 Performance Analytics Rate", 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 13.sp
                        )
                        val total = quests.size
                        val done = quests.filter { it.isCompleted }.size
                        val rate = if (total > 0) done.toFloat() / total else 0.8f
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = if (language == AppLanguage.FA) "نرخ تکمیل ماموریت‌ها" else "Quest Completion Rate", 
                                fontSize = 12.sp, 
                                color = Color.Gray
                            )
                            Text(
                                text = if (language == AppLanguage.FA) "کامل شده: ${(rate * 100).toInt()}%" else "${(rate * 100).toInt()}% Done", 
                                fontWeight = FontWeight.Bold, 
                                color = GlowingCyan, 
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = rate,
                            color = GlowingCyan,
                            trackColor = MaterialTheme.colorScheme.background,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats counters
                        Text(
                            text = if (language == AppLanguage.FA) "توزیع سنجه‌های صعود ویژگی‌های آرپی‌جی قهرمان" else "RPG Attenuation Metric distribution", 
                            fontSize = 11.sp, 
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val statsMapped = if (language == AppLanguage.FA) {
                            listOf(
                                "ویژگی قدرت بدنی ⚔️" to profile.strength,
                                "ویژگی انضباط فردی 🛡️" to profile.discipline,
                                "ویژگی دانش و هوش 📖" to profile.intelligence,
                                "ویژگی بقا و حیات 🩸" to profile.vitality
                            )
                        } else {
                            listOf(
                                "Strength Attribute ⚔️" to profile.strength,
                                "Discipline Attribute 🛡️" to profile.discipline,
                                "Intelligence Attribute 📖" to profile.intelligence,
                                "Vitality Attribute 🩸" to profile.vitality
                            )
                        }

                        statsMapped.forEach { (lbl, valState) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(lbl, fontSize = 13.sp)
                                Text(
                                    text = if (language == AppLanguage.FA) "$valState امتیاز" else "$valState PTS", 
                                    fontWeight = FontWeight.Bold, 
                                    color = LegendaryGold, 
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
            HomeSubTab.ORACLE -> {
                // Glorious AI Oracle panel
                val isFarsi = language == AppLanguage.FA
                val aiQuests by viewModel.aiGeneratedQuests.collectAsState()
                val isGenQuests by viewModel.isGeneratingQuests.collectAsState()
                val coachAdvice by viewModel.aiCoachAdvice.collectAsState()
                val isCoachConsult by viewModel.isConsultingCoach.collectAsState()
                val diffRec by viewModel.aiDifficultyRecommendation.collectAsState()
                val isAnalyzingDiff by viewModel.isAnalyzingDifficulty.collectAsState()

                var oracleCat by remember { mutableStateOf("Health") }
                var oracleDiff by remember { mutableStateOf("Medium") }
                var coachPromptInput by remember { mutableStateOf("") }

                val completedQuestsCount = quests.count { it.isCompleted }
                val pendingQuestsCount = quests.count { !it.isCompleted }

                Text(
                    text = if (isFarsi) "🔮 معبد الهام‌بخش اوراکل هوش مصنوعی قلمرو" else "🔮 Sanctuary of the AI Oracle",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlowingCyan,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = if (isFarsi) "با تکیه بر خرد جاودان، مسیر رشد حماسی و سختی فعالیت‌های خود را تنظیم کنید." else "Harness celestial AI wisdom to balance your quests, routines, and challenges.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 1. AI QUEST GENERATOR & RECOMMENDATIONS CARD
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isFarsi) "📜 تولیدکننده ماموریت‌های شخصی‌سازی شده" else "📜 Smart Personalized Quest Scribe",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = LegendaryGold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isFarsi) "یک موضوع و درجه سختی انتخاب کنید تا اوراکل ۳ ماموریت فانتزی متناسب بسازد!" else "Choose category and intensity. Oracle will forge 3 bespoke quests!",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Category Selection filter
                        val categoriesList = listOf("Health", "Fitness", "Learning", "Finance", "Career", "Relationships")
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categoriesList.forEach { c ->
                                val selected = oracleCat == c
                                val label = if (isFarsi) {
                                    when (c) {
                                        "Health" -> "سلامت"
                                        "Fitness" -> "تناسب اندام"
                                        "Learning" -> "یادگیری"
                                        "Finance" -> "مالی"
                                        "Career" -> "شغل"
                                        "Relationships" -> "روابط"
                                        else -> c
                                    }
                                } else c
                                FilterChip(
                                    selected = selected,
                                    onClick = { oracleCat = c },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GlowingCyan.copy(alpha = 0.2f),
                                        selectedLabelColor = GlowingCyan
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Difficulty Selection filters
                        val diffList = listOf("Easy", "Medium", "Hard")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            diffList.forEach { d ->
                                val selected = oracleDiff == d
                                val label = if (isFarsi) {
                                    when (d) {
                                        "Easy" -> "آسان"
                                        "Medium" -> "متوسط"
                                        "Hard" -> "سخت"
                                        else -> d
                                    }
                                } else d
                                FilterChip(
                                    selected = selected,
                                    onClick = { oracleDiff = d },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GlowingCyan.copy(alpha = 0.2f),
                                        selectedLabelColor = GlowingCyan
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.generateAIQuests(oracleCat, oracleDiff, isFarsi) },
                            enabled = !isGenQuests,
                            colors = ButtonDefaults.buttonColors(containerColor = GlowingCyan),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isGenQuests) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DarkVoid, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isFarsi) "در حال تلاوت اوراد کیهانی..." else "Communing with Mind Cosmos...", color = DarkVoid, fontSize = 13.sp)
                            } else {
                                Text(if (isFarsi) "🔮 احضار ماموریت‌های الهام‌بخش" else "🔮 Conjure Smart Quest Proposal", color = DarkVoid, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        if (aiQuests.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (isFarsi) "مقام پیش‌نویس ماموریت‌های اوراکل:" else "Oracle Proposals Panel:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlowingCyan
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            aiQuests.forEach { (titleText, descText) ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(titleText, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = LegendaryGold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(descText, fontSize = 11.sp, color = Color.Gray, lineHeight = 14.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                viewModel.addQuest(titleText, descText, oracleDiff, oracleCat)
                                                Toast.makeText(context, if (isFarsi) "ماموریت در دفترچه فعال شما قید شد!" else "Accepted! Embarked on Quest successfully.", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = GlowingCyan.copy(alpha = 0.15f)),
                                            modifier = Modifier.align(Alignment.End),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text(if (isFarsi) "🛡️ پذیرش ماموریت" else "🛡️ Embark on Quest", color = GlowingCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. AI HABIT COACH SECTION CARD
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isFarsi) "🌱 مشاور و مربی هوش مصنوعی عادات عمیق" else "🌱 Smart AI Habit Coach & Oracle",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = LegendaryGold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isFarsi) "هدف یا عادت مد نظر خود را بنویسید تا مربی توصیه‌های تاکتیکی در اختیارتان بگذارد." else "Describe your target habits below to receive bespoke behavioral adjustments.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = coachPromptInput,
                            onValueChange = { coachPromptInput = it },
                            placeholder = { Text(if (isFarsi) "مثال: منظم ورزش کردن سه بار در هفته یا دوری از گوشی" else "E.g. Sleep routine at 10 PM or budget tracking daily", color = Color.Gray, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (coachPromptInput.isNotBlank()) {
                                    viewModel.consultAIHabitCoach(coachPromptInput, isFarsi)
                                } else {
                                    Toast.makeText(context, if (isFarsi) "لطفا متنی برای مشاوره قید کنید" else "Please type a routine goal first", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isCoachConsult,
                            colors = ButtonDefaults.buttonColors(containerColor = GlowingCyan),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isCoachConsult) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DarkVoid, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isFarsi) "در حال اسکن عادات عصبی..." else "Decoding neural synapses...", color = DarkVoid, fontSize = 12.sp)
                            } else {
                                Text(if (isFarsi) "💬 مشورت با حکیم گیلد عادات" else "💬 Consult Guild Mind Coach", color = DarkVoid, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        if (coachAdvice.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                border = BorderStroke(1.dp, GlowingCyan.copy(alpha = 0.25f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = if (isFarsi) "📜 توصیه‌ی راهبردی مربی عادات:" else "📜 Scroll of Habit Counsel:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GlowingCyan
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = coachAdvice,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. PERSONALIZED DIFFICULTY ADJUSTER CARD
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isFarsi) "⚖️ ترازوی سنجش و آنالیز سختی ماموریت‌ها" else "⚖️ AI Game Balance & Difficulty Metrics",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = LegendaryGold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isFarsi) "اوراکل بر اساس میزان کامیابی و کارهای عقب‌مانده سختی کارهای قبلی را تحلیل می‌کند." else "The game architect gauges your pending vs completed flows to suggest optimal weight adjustments.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (isFarsi) "کامل شده" else "Completed", fontSize = 11.sp, color = Color.Gray)
                                Text("$completedQuestsCount ✔", fontWeight = FontWeight.Bold, color = GlowingCyan, fontSize = 14.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (isFarsi) "پیش‌رو" else "Pending Act", fontSize = 11.sp, color = Color.Gray)
                                Text("$pendingQuestsCount ⌛", fontWeight = FontWeight.Bold, color = LegendaryGold, fontSize = 14.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.analyzePersonalDifficulty(completedQuestsCount, pendingQuestsCount, isFarsi) },
                            enabled = !isAnalyzingDiff,
                            colors = ButtonDefaults.buttonColors(containerColor = GlowingCyan),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isAnalyzingDiff) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DarkVoid, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isFarsi) "در حال بازخوانی کارنامه اعمال..." else "Rebalancing difficulty index...", color = DarkVoid, fontSize = 12.sp)
                            } else {
                                Text(if (isFarsi) "⚖️ تحلیل و مهار سختی فعالیت‌ها" else "⚖️ Audit Realm Homeostasis Balance", color = DarkVoid, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        if (diffRec.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = if (isFarsi) "📈 گزارش توازن بازی عادات قلمرو:" else "📈 Homeostasis Balance Assessment:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LegendaryGold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = diffRec,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// Function to adapt colors nicely to Light Mode vs Dark Mode for Material Theme
fun glowingPurpleLightMode(isDarkMode: Boolean): Color {
    return if (isDarkMode) GlowingCyan else Color(0xFF720D93)
}

// ==========================================
// 4. ADD NEW QUEST SCREEN
// ==========================================
@Composable
fun AddQuestScreen(
    viewModel: QuestViewModel,
    language: AppLanguage,
    onSuccess: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("Medium") }
    var category by remember { mutableStateOf("Health") }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "✨ ${Translations.getString("create_quest", language)}",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Quest Title input
                Text(
                    text = Translations.getString("quest_title", language),
                    color = LegendaryGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text(text = if (language == AppLanguage.FA) "مثال: گریند در لیت‌کد یا تمرین ورزشی" else "E.g. Grind LeetCode study or Workout", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quest_title_tf"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = GlowingCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Quest objective
                Text(
                    text = Translations.getString("quest_desc", language),
                    color = LegendaryGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text(text = if (language == AppLanguage.FA) "هدف و خروجی اصلی این ماموریت چیست؟" else "What is the primary target objective here?", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .testTag("quest_desc_tf"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = GlowingCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Difficulty row
                Text(
                    text = Translations.getString("difficulty", language),
                    color = LegendaryGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val difficulties = listOf("Easy", "Medium", "Hard", "Epic")
                    difficulties.forEach { diff ->
                        val selected = difficulty == diff
                        val borderCol = when(diff) {
                            "Easy" -> Color.Gray
                            "Medium" -> GlowingCyan
                            "Hard" -> QuestRed
                            else -> LegendaryGold
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) borderCol.copy(alpha = 0.2f) else MaterialTheme.colorScheme.background)
                                .border(
                                    BorderStroke(if (selected) 2.dp else 1.dp, if (selected) borderCol else MaterialTheme.colorScheme.surfaceVariant),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { difficulty = diff }
                                .testTag("quest_difficulty_${diff.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            val diffText = if (language == AppLanguage.FA) {
                                when(diff) {
                                    "Easy" -> "آسان"
                                    "Medium" -> "متوسط"
                                    "Hard" -> "سخت"
                                    else -> "حماسی"
                                }
                            } else diff
                            Text(
                                text = diffText,
                                color = if (selected) borderCol else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Category options
                Text(
                    text = Translations.getString("attribute", language),
                    color = LegendaryGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                val categories = listOf("Health", "Fitness", "Learning", "Finance", "Career", "Relationships")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val selected = category == cat
                        val catLabel = if (language == AppLanguage.FA) {
                            when (cat) {
                                "Health" -> "تندرستی و سلامت"
                                "Fitness" -> "تناسب اندام ورزشی"
                                "Learning" -> "آموزش و یادگیری"
                                "Finance" -> "مدیریت امور مالی"
                                "Career" -> "صنف و مسیر حرفه‌ای"
                                "Relationships" -> "خانواده و روابط"
                                else -> cat
                            }
                        } else cat

                        FilterChip(
                            selected = selected,
                            onClick = { category = cat },
                            label = { Text(catLabel) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GlowingCyan.copy(alpha = 0.15f),
                                selectedLabelColor = GlowingCyan
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Embark save button
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            Toast.makeText(context, Translations.getString("enter_title", language), Toast.LENGTH_SHORT).show()
                        } else if (description.isBlank()) {
                            Toast.makeText(context, Translations.getString("enter_desc", language), Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addQuest(title, description, difficulty, category)
                            Toast.makeText(context, Translations.getString("quest_created", language), Toast.LENGTH_LONG).show()
                            onSuccess()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowingCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_quest_btn")
                ) {
                    Text(
                        text = "⚔️ ${Translations.getString("save_quest", language)} ⚔️",
                        color = DarkVoid,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// 5. PROFILE EXTRA EXPANSIVE SETTING
// ==========================================
@Composable
fun ProfileScreen(
    viewModel: QuestViewModel,
    language: AppLanguage
) {
    val profile by viewModel.playerProfile.collectAsState()
    var isEditingName by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    
    // RPG Profile tabs: 0 = Class / Character Setup, 1 = Summon Companion, 2 = Fantasy Shop, 3 = Achievements Hall
    var subTabSelector by remember { mutableStateOf(0) }
    
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🛡️ ${Translations.getString("profile_tab", language)}",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Gold balance badge top right
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, LegendaryGold)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🪙 ", fontSize = 16.sp)
                    val goldWord = if (language == AppLanguage.FA) "سکه طلا" else "Gold"
                    Text(
                        text = "${profile.gold} $goldWord",
                        color = LegendaryGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Dynamic character shield frame
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.5.dp, GlowingCyan)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Avatar with visual selection fallback
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(3.dp, GlowingCyan, CircleShape)
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    when (profile.avatarUrl) {
                        "ic_rpg_hero" -> {
                            Image(
                                painter = painterResource(id = R.drawable.ic_rpg_hero),
                                contentDescription = "Major Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        "🧙‍♂️" -> Text("🧙‍♂️", fontSize = 52.sp)
                        "⚔️" -> Text("⚔️", fontSize = 52.sp)
                        "🗡️" -> Text("🗡️", fontSize = 52.sp)
                        "👑" -> Text("👑", fontSize = 52.sp)
                        "🏹" -> Text("🏹", fontSize = 52.sp)
                        else -> {
                            Image(
                                painter = painterResource(id = R.drawable.ic_rpg_hero),
                                contentDescription = "Major Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Editable Hero name input field
                if (isEditingName) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            placeholder = { Text(text = profile.heroName, color = Color.Gray) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = GlowingCyan,
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                        
                        IconButton(
                            onClick = {
                                if (nameInput.isNotBlank()) {
                                    viewModel.updateHeroName(nameInput)
                                }
                                isEditingName = false
                                nameInput = ""
                            },
                            modifier = Modifier.testTag("save_hero_name_btn")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Save", tint = GlowingEmerald)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.clickable { isEditingName = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = profile.heroName,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit name",
                            tint = LegendaryGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                val prestigeVal by viewModel.prestigeRank.collectAsState()
                if (prestigeVal > 0) {
                    val tierWord = if (language == AppLanguage.FA) "رتبه پرستیژ $prestigeVal ⭐" else "PRESTIGE TIER $prestigeVal ⭐"
                    Text("🛡️ $tierWord", fontWeight = FontWeight.Black, color = LegendaryGold, fontSize = 13.sp, modifier = Modifier.padding(bottom = 2.dp))
                }
                val ascendantClsWord = if (language == AppLanguage.FA) {
                    val clsFA = when(profile.characterClass) {
                        "Warrior" -> "رزم‌آور (مبارز سرسخت)"
                        "Mage" -> "کیمیاگر (مربی دانایی)"
                        "Rogue" -> "سرکش (خنجر طلا)"
                        else -> profile.characterClass
                    }
                    "کلاس صعودکننده: $clsFA"
                } else "Ascendant Class: ${profile.characterClass}"
                Text(ascendantClsWord, fontWeight = FontWeight.Bold, color = LegGlassPurpleStyle(profile.characterClass), fontSize = 13.sp)
                
                Spacer(modifier = Modifier.height(10.dp))
                if (profile.level >= 20) {
                    Button(
                        onClick = { viewModel.triggerPrestigeReset() },
                        colors = ButtonDefaults.buttonColors(containerColor = LegendaryGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(if (language == AppLanguage.FA) "✨ صعود و نوزایی پرستیژ ✨" else "✨ PRESTIGE ASCENSION ✨", color = DarkVoid, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1B122E), RoundedCornerShape(6.dp))
                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.FA) "چرخه پرستیژ: رسیدن به سطح ۲۰ (پیشرفت: ${profile.level}/۲۰)" else "Prestige Loop: Reach Lvl 20 (Progress: ${profile.level}/20)",
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal selections for tabs (scrollable for mobile density)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val sections = if (language == AppLanguage.FA) {
                listOf("🛡️ قهرمان", "🐾 یار همراه", "🛒 دکان", "🏆 مدال‌ها", "📚 کلکسیون", "🌀 خلوتگاه")
            } else {
                listOf("🛡️ Hero", "🐾 Summon", "🛒 Shop", "🏆 Medal", "📚 Collection", "🌀 Sanctum")
            }
            sections.forEachIndexed { idx, s ->
                val active = subTabSelector == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { subTabSelector = idx }
                        .border(
                            1.dp,
                            if (active) GlowingCyan else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(6.dp)
                        )
                        .background(
                            if (active) GlowingCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                        )
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(s, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (active) GlowingCyan else MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Sub-tab router
        when (subTabSelector) {
            0 -> {
                // Class and Character selections
                Text(
                    text = if (language == AppLanguage.FA) "کلاس قهرمانی ارشد خود را انتخاب کنید" else "Select Master Character Class", 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                val classes = if (language == AppLanguage.FA) {
                    listOf(
                        Triple("Warrior", "🗡️ رزم‌آور (محافظ سپر حلیم)", "+۱۵٪ امتیاز تجربه در ماموریت‌های ورزشی و چالش‌های فیزیکی."),
                        Triple("Mage", "🧙‍♂️ کیمیاگر (پناهگاه مربی دانایی)", "+۱۵٪ امتیاز تجربه در ماموریت‌های دانشی، مطالعه و تمرکز."),
                        Triple("Rogue", "🗡️ سرکش (خنجر طلاخواه)", "+۱۵٪ ضریب طلا به هنگام تکمیل هرگونه ماموریت.")
                    )
                } else {
                    listOf(
                        Triple("Warrior", "🗡️ Warrior (Shield Master)", "+15% Experience bonuses on athletic/strength category quests."),
                        Triple("Mage", "🧙‍♂️ Mage (Spellwarden)", "+15% Experience rewards on mental learning/discipline quests."),
                        Triple("Rogue", "🗡️ Rogue (Golden Blade)", "+15% Gold multiplier on completing quests of any type.")
                    )
                }

                classes.forEach { (nid, title, bonusText) ->
                    val activeCls = profile.characterClass == nid
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { viewModel.selectCharacterClass(nid) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (activeCls) GlowingCyan.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.5.dp, if (activeCls) GlowingCyan else MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(title, fontWeight = FontWeight.Bold, color = if (activeCls) GlowingCyan else MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(bonusText, fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Avatar Grid Change
                Text(
                    text = if (language == AppLanguage.FA) "تجهیز نمودن چهره پویا آواتار" else "Equip Dynamic Avatar Icon", 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val avatars = listOf("ic_rpg_hero", "🧙‍♂️", "⚔️", "🗡️", "👑", "🏹")
                    avatars.forEach { av ->
                        val equipped = profile.avatarUrl == av
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (equipped) GlowingCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                                .border(
                                    1.5.dp,
                                    if (equipped) GlowingCyan else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.changeAvatar(av) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (av == "ic_rpg_hero") {
                                Text("👤", fontSize = 18.sp)
                            } else {
                                Text(av, fontSize = 24.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                
                // PREMIUM CHARACTER CUSTOMIZATION: Hairstyles, Armor Skins, Character Portraits
                Text(
                    text = if (language == AppLanguage.FA) "💇‍♂️ پیرایشگاه قهرمان و تجهیز پوسته" else "💇‍♂️ Legend Barber Shop & Skins", 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 15.sp,
                    color = LegendaryGold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.FA) "جلوه ظاهری، شنیون مو و جوشن‌های اسطوره‌ای آواتار خود را سفارشی‌سازی کنید." else "Personalize your display hairstyles, armor skins, and status portraits.", 
                    fontSize = 11.sp, 
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Hairstyles Selection
                val hairstyles = listOf("Noble Spikes 💇‍♂️", "Archmage Locks 🧑‍🦳", "Rogue Ponytail 💇", "Dreadlock Crest 🧑‍🎤")
                val activeHair by viewModel.currentHairstyle.collectAsState()
                Text(
                    text = if (language == AppLanguage.FA) "شنیون موی فعال: $activeHair" else "Active Hairstyle: $activeHair",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    hairstyles.forEach { hs ->
                        val isSel = activeHair == hs
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) LegendaryGold.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                                .border(1.dp, if (isSel) LegendaryGold else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                .clickable { viewModel.changeHairstyle(hs) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(hs.split(" ").firstOrNull() ?: hs, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Armor Skins Selection
                val armorSkins = listOf("Cloth Tunic", "Molten Obsidian 🌋", "Royal Plate 👑", "Stealth Hood 🖤")
                val activeSkin by viewModel.currentArmorSkin.collectAsState()
                Text(
                    text = if (language == AppLanguage.FA) "پوسته جوشن مجهز: $activeSkin" else "Equipped Armor Skin: $activeSkin",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    armorSkins.forEach { askin ->
                        val isSel = activeSkin == askin
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) LegendaryGold.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                                .border(1.dp, if (isSel) LegendaryGold else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                .clickable { viewModel.changeArmorSkin(askin) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(askin.split(" ").firstOrNull() ?: askin, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Portraits Selection
                val portraits = listOf("Default Knight", "Archmage Sage 🔮", "Void Assassin 🌌", "Dragon Vanguard 🐲")
                val activePortrait by viewModel.currentPortrait.collectAsState()
                Text(
                    text = if (language == AppLanguage.FA) "تمثال مفاخر قهرمان: $activePortrait" else "Vocal Hero Portrait: $activePortrait",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    portraits.forEach { port ->
                        val isSel = activePortrait == port
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) LegendaryGold.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                                .border(1.dp, if (isSel) LegendaryGold else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                .clickable { viewModel.changePortrait(port) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val dispPort = when(port) {
                                "Default Knight" -> "🛡️"
                                "Archmage Sage 🔮" -> "🧙‍♂️"
                                "Void Assassin 🌌" -> "👥"
                                "Dragon Vanguard 🐲" -> "🐲"
                                else -> "👤"
                            }
                            Text(dispPort, fontSize = 16.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.surfaceVariant))
                Spacer(modifier = Modifier.height(16.dp))

                // RPG SPECIALIZATION SYSTEM
                Text(
                    text = if (language == AppLanguage.FA) "🔥 تخصص‌سازی ارشد کلاس قهرمان (پیشرفته)" else "🔥 Master RPG Specialist Ascension", 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 15.sp,
                    color = LegendaryGold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.FA) "مسیر تخصصی خود را برای باز کردن پاداش‌های غیرفعال افسانه‌ای انتخاب کنید (نیازمند سطح ۵)" else "Ascend to a specialized subclass to obtain mythic passive perks! (Requires LVL 5)", 
                    fontSize = 11.sp, 
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (profile.level < 5) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🔒", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (language == AppLanguage.FA) "قفل است! برای تخصص‌سازی لازم است به سطح ۵ برسید." else "Ascension Path Locked! Grasp Level 5 to specialize.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    val specializations = if (language == AppLanguage.FA) {
                        listOf(
                            Triple("Paladin", "🛡️ شوالیه پالادین (Paladin Specialty)", "کلاس مبارز صعودیافته. دریافت ۲۰٪ تجربه بیشتر دائمی روی تمامی ماموریت‌ها."),
                            Triple("Berserker", "🪓 جنگجوی دیوانه (Berserker Specialty)", "تمرکز بر انهدام و قدرت بدنی. دریافت ۲۵٪ طلای بیشتر روی ماموریت‌های تندرستی و ورزش."),
                            Triple("Assassin", "🗡️ سایه کشنده (Assassin Specialty)", "سرعت و کشندگی. دریافت ۲۵٪ طلای اضافه برروی تمامی ماموریت‌های انجام شده."),
                            Triple("Savant", "🎓 فرزانه دانشمند (Savant Specialty)", "کیمیاگری و دانش پژوهی. دریافت ۲۰٪ تجربه اضافه دائمی روی تمامی حوزه ها."),
                            Triple("Battlemage", "⚡ جادوگر جنگی (Battlemage Specialty)", "کنترل جادویی و تمرکز انضباطی. دریافت ۲۰٪ سکه اضافه روی کارهای روزمره.")
                        )
                    } else {
                        listOf(
                            Triple("Paladin", "🛡️ Paladin Ascent", "Holy guardian of Solis. Earn +20% permanent XP bonuses on all completed tasks."),
                            Triple("Berserker", "🪓 Berserker Fury", "Physical and athletic dominance. Recieve +25% Gold gains on Strength/Fitness categories."),
                            Triple("Assassin", "🗡️ Shadow Assassin", "Deadly precision. Receives +25% Gold bonuses on absolutely all quests completed."),
                            Triple("Savant", "🎓 Sage Savant", "Ultimate academic wisdom. Grasp +20% permanent XP bonuses on everything completed."),
                            Triple("Battlemage", "⚡ Arcanist Battlemage", "Combat-mage precision. Yields +20% permanent Gold bonuses on disciplines.")
                        )
                    }

                    specializations.forEach { (specKey, specTitle, specDesc) ->
                        val isActiveSpec = profile.activeSpecialization == specKey
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clickable { viewModel.specializeCharacterClass(specKey) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isActiveSpec) LegendaryGold.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.5.dp, if (isActiveSpec) LegendaryGold else MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = specTitle, 
                                        fontWeight = FontWeight.Bold, 
                                        color = if (isActiveSpec) LegendaryGold else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 13.sp
                                    )
                                    if (isActiveSpec) {
                                        Text(
                                            text = if (language == AppLanguage.FA) "فعال 👑" else "ACTIVE 👑", 
                                            fontWeight = FontWeight.ExtraBold, 
                                            fontSize = 9.sp, 
                                            color = LegendaryGold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(specDesc, fontSize = 11.sp, color = Color.Gray, lineHeight = 15.sp)
                            }
                        }
                    }
                }
            }

            1 -> {
                // Companions Summon Deck
                Text(
                    text = if (language == AppLanguage.FA) "🐾 احضار پیشکار و حیوان همراه اساطیری" else "🐾 Familiar Companions Summon Deck", 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.FA) "همراهانی وفادار را احضار کنید که دوشادوش شما در مسیر حقیقت قوی‌تر می‌شوند!" else "Summon legendary companions that advance loyalty alongside you!", 
                    fontSize = 12.sp, 
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Current Familiar status
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.2.dp, LegendaryGold)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (profile.companionName == "Slime Buddy") "🟢" else if (profile.companionName == "Iron Golem") "🤖" else "🔥",
                                fontSize = 32.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                val compFA = if (language == AppLanguage.FA) {
                                    when(profile.companionName) {
                                        "Slime Buddy" -> "یار اسلایمی مایه"
                                        "Iron Golem" -> "انسان‌نمای آهنین غول"
                                        "Baby Phoenix" -> "جوجه ققنوس آتشین"
                                        else -> profile.companionName
                                    }
                                } else profile.companionName
                                Text(
                                    text = if (language == AppLanguage.FA) "همراه بیدار شده: $compFA" else "Active Companion: ${profile.companionName}", 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (language == AppLanguage.FA) "پیشکار وفادار سطح ${profile.companionLevel}" else "Level ${profile.companionLevel} Familiar Guard", 
                                    fontSize = 12.sp, 
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Familiar EXP Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val fPct = profile.companionXp.toFloat() / 100f
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(10.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.background)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fPct)
                                        .background(LegendaryGold)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${profile.companionXp}/100 XP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        // COMPANION TREATS TRAINING BUTTON (Active Pet Levelup)
                        Button(
                            onClick = { viewModel.trainCompanionPet() },
                            enabled = profile.gold >= 35,
                            colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.FA) "🍬 خورانیدن آب‌نبات جادویی (هزینه ۳۵ سکه | +25 Companion XP)" else "🍬 Feed Companion Treat (35 Gold | +25 Pet XP)",
                                fontSize = 11.sp,
                                color = DarkVoid,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (language == AppLanguage.FA) "پیشکاران در دسترس در دکان معابد" else "Available Familiars in Shop", 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                val monstersAvailable = if (language == AppLanguage.FA) {
                    listOf(
                        Triple("Baby Phoenix", 100, "به طور فعال +۱۵٪ پاداش امتیاز تجربه بر کل فعالیت‌ها به شما اهدا می‌دارد."),
                        Triple("Slime Buddy", 50, "به طور فعال +۵٪ طلای اضافه برای ماموریت‌های انجام شده به ارمغان می‌آورد."),
                        Triple("Iron Golem", 200, "دفاع کلی و مروت را تقویت کرده و ظرفیت سنجه‌های پویای بقا را افزایش می‌دهد.")
                    )
                } else {
                    listOf(
                        Triple("Baby Phoenix", 100, "Instantly awards passive +15% Experience reward multipliers on all activities completed."),
                        Triple("Slime Buddy", 50, "Instantly earns slight +5% extra Gold multipliers on any completed quests."),
                        Triple("Iron Golem", 200, "Increases defense & grants dynamic local resilience metrics.")
                    )
                }

                monstersAvailable.forEach { (mname, goldCost, mdesc) ->
                    val owned = profile.companionName == mname
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                val showMName = if (language == AppLanguage.FA) {
                                    when(mname) {
                                        "Baby Phoenix" -> "جوجه ققنوس آتشین"
                                        "Slime Buddy" -> "یار اسلایمی مایه"
                                        "Iron Golem" -> "انسان‌نمای آهنین غول"
                                        else -> mname
                                    }
                                } else mname
                                Text(showMName, fontWeight = FontWeight.Bold)
                                Text(mdesc, fontSize = 11.sp, color = Color.Gray, lineHeight = 15.sp)
                            }

                            Button(
                                onClick = { viewModel.selectCompanion(mname, goldCost) },
                                enabled = !owned && profile.gold >= goldCost,
                                colors = ButtonDefaults.buttonColors(containerColor = LegendaryGold),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                val btnText = if (owned) {
                                    if (language == AppLanguage.FA) "مجهز شده" else "Equipped"
                                } else {
                                    if (language == AppLanguage.FA) "احضار ($goldCost سکه)" else "Summon ($goldCost G)"
                                }
                                Text(btnText, fontSize = 10.sp, color = DarkVoid, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (language == AppLanguage.FA) "🛡️ دکان کلاهخود و زره پوش پیشکار" else "🛡️ Companion Equipment Bazaar", 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 14.sp,
                    color = LegendaryGold
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                val compEqState by viewModel.companionEquipment.collectAsState()
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, GlowingCyan.copy(alpha = 0.4f))
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🛡️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (language == AppLanguage.FA) "تجهیزات فعلی پیشکار: ${if (compEqState.isEmpty()) "بند چرمی ساده" else compEqState}" else "Equipped Companion Item: ${if (compEqState.isEmpty()) "Simple Leather Collar" else compEqState}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlowingCyan
                        )
                    }
                }

                val petGear = listOf(
                    Triple("Spike Collar 🧣", 80, if (language == AppLanguage.FA) "+۱۰٪ به کارایی حمله قهرمان در جنگ با هیولاها" else "+10% Companion Attack contribution"),
                    Triple("Chitin Helm 🪖", 120, if (language == AppLanguage.FA) "+۱۵٪ مقاومت و دفاع پیشکار در طول روز" else "+15% Companion Defense armor"),
                    Triple("Bell of Solitude 🔔", 150, if (language == AppLanguage.FA) "+۱۵٪ افزایش کسب تجربه پیشکار در تمرینات" else "+15% Loyalty training EXP booster"),
                    Triple("Gold-Plated Wings 💸", 190, if (language == AppLanguage.FA) "+۲۰٪ ضریب طلای به دست آمده در کل سناریوها" else "+20% Companion quest gold multiplier")
                )

                petGear.forEach { (item, cost, descGear) ->
                    val isEquipped = compEqState == item
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, if (isEquipped) GlowingEmerald else MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(descGear, fontSize = 11.sp, color = Color.Gray)
                            }
                            Button(
                                onClick = { viewModel.equipCompanionItem(item, cost) },
                                enabled = !isEquipped && profile.gold >= cost,
                                colors = ButtonDefaults.buttonColors(containerColor = if (isEquipped) GlowingEmerald else LegendaryGold),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(
                                    text = if (isEquipped) {
                                        if (language == AppLanguage.FA) "مجهز" else "Equipped"
                                    } else {
                                        if (language == AppLanguage.FA) "خرید ($cost سکه)" else "Buy ($cost G)"
                                    },
                                    fontSize = 10.sp,
                                    color = DarkVoid,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                // Fantasy Shop & Boosters
                Text(text = "🛒 ${Translations.getString("shop_title", language)}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))

                // Daily Reward Claim Box
                val dailyClaimTime = profile.lastLoginRewardTime
                val canClaim = System.currentTimeMillis() - dailyClaimTime >= 24 * 60 * 60 * 1000L || dailyClaimTime == 0L
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassSurface),
                    border = BorderStroke(1.2.dp, LegendaryGold.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = Translations.getString("daily_reward_title", language), fontWeight = FontWeight.Bold, color = LegendaryGold)
                        Text(text = Translations.getString("daily_reward_desc", language), fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.claimDailyReward() },
                            enabled = canClaim,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = LegendaryGold),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                if (canClaim) Translations.getString("claim_btn", language) else Translations.getString("claimed_btn", language),
                                fontWeight = FontWeight.Bold,
                                color = DarkVoid
                            )
                        }
                    }
                }

                // Equipments lists available in Shop
                val equipments = listOf(
                    Triple("Flame Sword", 120, "Weapon"),
                    Triple("Iron Plate Mail", 160, "Armor"),
                    Triple("Ruby Ring", 80, "Ring"),
                    Triple("XP Elixir Booster", 30, "Booster")
                )

                equipments.forEach { (name, cost, typeState) ->
                    val isOwned = profile.inventory.contains(name)
                    val isEquipped = when (typeState) {
                        "Weapon" -> profile.weapon == name
                        "Armor" -> profile.armor == name
                        "Ring" -> profile.ring == name
                        else -> false
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                val nameDisp = if (language == AppLanguage.FA) {
                                    when(name) {
                                        "Flame Sword" -> "شمشیر شعله‌ور سوزان"
                                        "Iron Plate Mail" -> "زره غول‌آسا فلزی"
                                        "Ruby Ring" -> "انگشتر یاقوت سرخ"
                                        "XP Elixir Booster" -> "اکسیر افزایش‌دهنده تجربه"
                                        else -> name
                                    }
                                } else name
                                Text(nameDisp, fontWeight = FontWeight.Bold)

                                val typeDisp = if (language == AppLanguage.FA) {
                                    when(typeState) {
                                        "Weapon" -> "سلاح جنگی"
                                        "Armor" -> "پوشش زرهی"
                                        "Ring" -> "انگشتر جادویی"
                                        else -> "تقویت‌کننده اکسیر"
                                    }
                                } else "Class Type: $typeState"
                                Text(typeDisp, fontSize = 12.sp, color = Color.Gray)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (typeState == "Booster") {
                                    Button(
                                        onClick = { viewModel.purchaseShopItem(name, cost, typeState) },
                                        enabled = profile.gold >= cost,
                                        colors = ButtonDefaults.buttonColors(containerColor = GlowingCyan),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(if (language == AppLanguage.FA) "نوشیدن ($cost سکه)" else "Drink ($cost Gold)", fontSize = 11.sp, color = DarkVoid, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    if (!isOwned) {
                                        Button(
                                            onClick = { viewModel.purchaseShopItem(name, cost, typeState) },
                                            enabled = profile.gold >= cost,
                                            colors = ButtonDefaults.buttonColors(containerColor = LegendaryGold),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(if (language == AppLanguage.FA) "خرید ($cost سکه)" else "Buy ($cost Gold)", fontSize = 11.sp, color = DarkVoid)
                                        }
                                    } else {
                                        Button(
                                            onClick = { viewModel.equipItem(name, typeState) },
                                            enabled = !isEquipped,
                                            colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald)
                                        ) {
                                            val bStateText = if (isEquipped) {
                                                if (language == AppLanguage.FA) "مجهز شده ✓" else "Equipped ✓"
                                            } else {
                                                if (language == AppLanguage.FA) "تجهیز کردن" else "Equip"
                                            }
                                            Text(bStateText, fontSize = 11.sp, color = DarkVoid)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    border = BorderStroke(1.5.dp, LegendaryGold)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔮", fontSize = 32.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.FA) "صندوقچه اسطوره‌ای اساطیری" else "Mythic Premium Loot Box",
                                    color = LegendaryGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                val mythicChestsState by viewModel.mythicChests.collectAsState()
                                Text(
                                    text = if (language == AppLanguage.FA) "صندوق‌های اسطوره‌ای موجود: $mythicChestsState عدد" else "Your Mythic Chests: $mythicChestsState owned",
                                    fontSize = 12.sp,
                                    color = Color.LightGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (language == AppLanguage.FA) "شامل شانس کشف آیتم‌های لژندری بسیار کمیاب آلبوم کلکسیون!" else "Guaranteed rare drops! Chance to unlock Legendary Relics for the Collection book.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.buyMythicChestWithGold() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C223E)),
                                border = BorderStroke(1.dp, LegendaryGold.copy(alpha = 0.5f)),
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (language == AppLanguage.FA) "خرید با سکه 🪙۲۵۰" else "Buy: 🪙250 Gold",
                                    fontSize = 11.sp,
                                    color = LegendaryGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = { viewModel.buyMythicChestWithGems() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2F3E)),
                                border = BorderStroke(1.dp, GlowingCyan.copy(alpha = 0.5f)),
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (language == AppLanguage.FA) "خرید با الماس 💎۱۵" else "Buy: 💎15 Gems",
                                    fontSize = 11.sp,
                                    color = GlowingCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val mChestsVal by viewModel.mythicChests.collectAsState()
                        Button(
                            onClick = { viewModel.openMythicChest() },
                            enabled = mChestsVal > 0,
                            colors = ButtonDefaults.buttonColors(containerColor = LegendaryGold),
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.FA) "✨ گشودن صندوق اسطوره‌ای ✨" else "✨ OPEN MYTHIC LOOT BOX ✨",
                                color = DarkVoid,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            3 -> {
                // Hall of Achievements Medal board
                Text(
                    text = if (language == AppLanguage.FA) "🏆 تالار افتخارات قهرمانی و مدال‌های حماسی" else "🏆 Hall of Heroic Achievement Medals", 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 16.sp, 
                    color = LegendaryGold
                )
                Spacer(modifier = Modifier.height(10.dp))

                val achievements = if (language == AppLanguage.FA) {
                    listOf(
                        Triple("first_quest", "صعود نخستین 🏔️", "ثبت و اولین پاکسازی موفقیت‌آمیز ماموریت روزانه در زندگی حقیقی."),
                        Triple("streak_7", "غیرت ناگسستنی 🔥", "پایداری بی‌وقفه و انجام مداوم ماموریت‌ها به مدت ۷ روز پیاپی."),
                        Triple("level_10", "سرور صعودکننده ⭐", "پیشروی قدرتمندانه تا رسیدن به سطح ارتقاء یافته سطح ۱۰ قهرمان."),
                        Triple("quests_100", "پیشکسوت کتیبه‌ها 📜", "انجام و تکمیل بیش از ۱۰ فعالیت مختلف روی این حساب کاربری."),
                        Triple("gold_hoarder", "ذخیره‌کننده گنج اژدها 🪙", "جمع‌آوری و پس‌انداز بیش از ۲۰۰ سکه طلا در صندوق مخازن معابد.")
                    )
                } else {
                    listOf(
                        Triple("first_quest", "First Ascent 🏔️", "Logged and cleared your very first life quest successfully."),
                        Triple("streak_7", "Unbroken Zealot 🔥", "Maintained an unbroken questing streak of 7 entire consecutive days."),
                        Triple("level_10", "Ascendant Overlord ⭐", "Grew dominant enough to reach Player Level 10 successfully."),
                        Triple("quests_100", "Chronicle Veteran 📜", "Completed over 10 activities on this account."),
                        Triple("gold_hoarder", "Hoarder of Dragon Loot 🪙", "Amassed over 200 Gold inside the Vault bank.")
                    )
                }

                achievements.forEach { (aid, title, notesdesc) ->
                    val unlocked = profile.unlockedAchievements.contains(aid)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (unlocked) LegendaryGold.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.2.dp, if (unlocked) LegendaryGold else MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(if (unlocked) LegendaryGold.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (unlocked) "🏅" else "🔒", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(title, fontWeight = FontWeight.Bold, color = if (unlocked) LegendaryGold else MaterialTheme.colorScheme.onSurface)
                                Text(notesdesc, fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp)
                            }
                        }
                    }
                }
            }

            4 -> {
                // ITEM COLLECTION BOOK & RARE ARCHIVES
                val collectionItems = listOf(
                    Triple("Flame Sword", if (language == AppLanguage.FA) "🗡️ شمشیر شعله‌ور سوزان" else "🗡️ Flame Sword", if (language == AppLanguage.FA) "تیغه‌ای افسانه‌ای آغشته به جادوی ابدی سولیس." else "A mystical heavy blade forged within the burning peaks of Solis."),
                    Triple("Shadow Blade", if (language == AppLanguage.FA) "🗡️ تیغه سایه‌گون" else "🗡️ Shadow Blade", if (language == AppLanguage.FA) "تیغه تیز و مخفی که به مالکان خود چابکی اضافه می‌دهد." else "A swift dagger saturated with absolute void essence."),
                    Triple("Iron Plate Mail", if (language == AppLanguage.FA) "🛡️ زره غول‌آسا فلزی" else "🛡️ Iron Plate Mail", if (language == AppLanguage.FA) "زره پلاتینیوم سنگین که از جنگجو در برابر هر ضربه‌ای محافظت می‌کند." else "Heavy steel forged plate armor crafted by royal dwarven smiths."),
                    Triple("Assassin Leather", if (language == AppLanguage.FA) "🛡️ زره چرمی آدم‌کش" else "🛡️ Assassin Leather", if (language == AppLanguage.FA) "روپوش چرمی مستحکم جهت استتار در تاریکی شب." else "Flexible, reinforced night stalker armor for stealth scouts."),
                    Triple("Ruby Ring", if (language == AppLanguage.FA) "💍 انگشتر یاقوت سرخ" else "💍 Ruby Ring", if (language == AppLanguage.FA) "نگین سرخ درخشان که قدرت‌های ضربتی قهرمان را بالا می‌برد." else "Enchanted with physical power. Boosts offensive attributes."),
                    Triple("Sapphire Ring", if (language == AppLanguage.FA) "💍 انگشتر یاقوت کبود" else "💍 Sapphire Ring", if (language == AppLanguage.FA) "نگین آبی جادویی فشرده‌شده با قدرت تمرکز هوش بالا." else "Crystalline sapphire offering deep mystical intelligence enhancements."),
                    Triple("Baby Phoenix", if (language == AppLanguage.FA) "🐣 ققنوس کوچک" else "🐣 Baby Phoenix", if (language == AppLanguage.FA) "جمع‌آوری تجربه بیشتر قهرمان." else "A celestial bird that multiplies experience rates periodically."),
                    Triple("Gorgon Hatchling", if (language == AppLanguage.FA) "🐍 گورگون نوباوه" else "🐍 Gorgon Hatchling", if (language == AppLanguage.FA) "همراه کوچک سمی با توانایی فلج کردن حریفان و حشرات." else "Sly toxic beast from Highcliff. Boosts combat and speed attributes.")
                )

                val unlockedCount = collectionItems.count { profile.inventory.contains(it.first) }
                val progressPct = unlockedCount.toFloat() / collectionItems.size

                Text(
                    text = if (language == AppLanguage.FA) "📖 منشور تالار کلکسیون عتیقه و دارایی‌ها" else "📖 Antiquities Item Collection Book", 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 16.sp,
                    color = LegendaryGold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (language == AppLanguage.FA) "آیتم‌های جدید بخرید و صندوقچه‌ها را باز کنید تا هر هشت عتیقه تاریخی حماسی آلبوم خود را کشف و کامل نمایید." else "Earn gold and open mythic chests to unlock all 8 historical relics!", 
                    fontSize = 11.sp, 
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Progress ratio bar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, LegendaryGold.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (language == AppLanguage.FA) "جمع‌آوری شده در آلبوم عتیقه" else "Collection Discoveries Rate",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$unlockedCount / ${collectionItems.size} (${(progressPct*100).toInt()}%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = LegendaryGold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = progressPct,
                            color = LegendaryGold,
                            trackColor = MaterialTheme.colorScheme.background,
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                collectionItems.forEach { (rawKey, title, descText) ->
                    val isCollected = profile.inventory.contains(rawKey)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCollected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isCollected) LegendaryGold.copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.2f)
                        )
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (isCollected) LegendaryGold.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.1f), 
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (isCollected) "✨" else "🔒", fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title, 
                                    fontWeight = FontWeight.Bold, 
                                    color = if (isCollected) MaterialTheme.colorScheme.onSurface else Color.Gray
                                )
                                Text(
                                    text = descText, 
                                    fontSize = 11.sp, 
                                    color = if (isCollected) Color.Gray else Color.Gray.copy(alpha = 0.6f),
                                    lineHeight = 14.sp
                                )
                            }
                            if (isCollected) {
                                Text(
                                    text = if (language == AppLanguage.FA) "کشف شد 🔮" else "DISCOVERED 🔮",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LegendaryGold
                                )
                            }
                        }
                    }
                }
            }
            5 -> {
                // Case 5: SANCTUM (Weather, Special seasonal quests / Winter solstice holiday achievements, simulated offline notifications logs)
                Text(
                    text = if (language == AppLanguage.FA) "🌀 معبد آیین باد و رویدادهای فصلی" else "🌀 Dynamic Sanctum Shrine & Seasons", 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 16.sp,
                    color = LegendaryGold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.FA) "هواشناسی قلمرو را مانیتور کنید، نذر نمایید، رویدادهای فصلی حماسی را باز کنید و اعلان‌های شبیه‌سازی را بیازمایید." else "Pray for weather, review active solstice events, and inspect simulated offline logs.", 
                    fontSize = 11.sp, 
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 1: Weather shrine
                val weatherType by viewModel.currentWeather.collectAsState()
                val weatherDesc = when(weatherType) {
                    "Sunny ☀️" -> if (language == AppLanguage.FA) "روز آفتابی مطبوع و ملایم. بازدهی ماموریت‌ها بدون تغییر است." else "Clear skies. Quests and duties grant normal standard yields."
                    "Thunderstorm ⚡" -> if (language == AppLanguage.FA) "طوفان رعد و برق شدید! آسیب‌های ضربتی شما به هولناکان ۱.۵ برابر می‌شود!" else "Violent skies. Your strike damage against Bad Habit Bosses is multiplied by 1.5x!"
                    "Snowy ❄️" -> if (language == AppLanguage.FA) "بارش برف جادویی سولستیس! خورانیدن جادویی پیشکاران به پاداش تعهد مضاعف می‌انجامد." else "Frost solstice. Pet treat feedings grant increased loyalty levels."
                    "Mist 🌫️" -> if (language == AppLanguage.FA) "مه غلیظ اسرارآمیز! برای دوری از گم شدن، تمرکز عجیبی لازم است." else "Mystic aura. Requires high attention and steady focus on quests."
                    else -> if (language == AppLanguage.FA) "هوای خنک سولیس" else "Breezy winds"
                }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.2.dp, GlowingCyan)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when(weatherType) {
                                    "Sunny ☀️" -> "☀️"
                                    "Thunderstorm ⚡" -> "⚡"
                                    "Snowy ❄️" -> "❄️"
                                    "Mist 🌫️" -> "🌫️"
                                    else -> "🌈"
                                },
                                fontSize = 32.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.FA) "آب و هوای فعلی قلمرو: $weatherType" else "Current Realm Weather: $weatherType",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = GlowingCyan
                                )
                                Text(
                                    text = weatherDesc,
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.prayForWeatherChange() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(1.dp, GlowingCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.FA) "🕯️ نذر در معبد معراج باد (۱۰ سکه)" else "🕯️ Offer Weather Prayer (10 Gold)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlowingCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // SECTION 2: Seasonal Holiday Event Quests
                Text(
                    text = if (language == AppLanguage.FA) "❄️ چالش‌ها و افتخارات ویژه رویداد زمستانه" else "❄️ Special Solstice Seasonal Event Quests",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(8.dp))

                val eventQuests by viewModel.eventQuests.collectAsState()
                eventQuests.forEach { eq ->
                    val isDone = eq.currentCount >= eq.requiredCount
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDone) Color(0xFF0C2417) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isDone) GlowingEmerald else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = eq.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isDone) GlowingEmerald else MaterialTheme.colorScheme.onSurface
                                )
                                if (isDone) {
                                    Text(
                                        text = if (language == AppLanguage.FA) "تکمیل شد 🌟" else "COMPLETED 🌟",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = LegendaryGold
                                    )
                                } else {
                                    Text(
                                        text = "${eq.currentCount} / ${eq.requiredCount}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = LegendaryGold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(eq.details, fontSize = 11.sp, color = Color.Gray, lineHeight = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (language == AppLanguage.FA) "🎁 جایزه اسطوره‌ای: ${eq.rewardItem}" else "🎁 Mythic Reward: ${eq.rewardItem}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = LegendaryGold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 3: Offline Notification Logs Simulator (with lovely RPG-Themed messages)
                Text(
                    text = if (language == AppLanguage.FA) "📟 شبیه‌ساز آفلاین پیام صعود قهرمان" else "📟 Simulated Offline RPG Notifications Log",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.FA) "پیام‌های هوشمند و فانتزی که هنگام دوری شما فرستاده می‌شوند را در زیر بیازمایید:" else "Test tailored immersive Push alert notifications generated dynamically when you are offline:",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                val notifyState by viewModel.simulatedNotificationsLog.collectAsState()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Button(
                            onClick = { viewModel.triggerSimulatedOfflineNotification() },
                            colors = ButtonDefaults.buttonColors(containerColor = LegendaryGold),
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (language == AppLanguage.FA) "🔔 ارسال اعلان آفلاین تستی فانتزی" else "🔔 Fire Simulated Offline Notification", color = DarkVoid, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (language == AppLanguage.FA) "پیام‌های ثبت شده در سیستم آفلاین:" else "Simulated Log Records:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (notifyState.isEmpty()) {
                            Text(
                                text = if (language == AppLanguage.FA) "پیامی ثبت نشده. پیام تست را بزنید." else "No offline logs. Click above to trigger system simulated warnings!",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        } else {
                            notifyState.forEach { logItem ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("📟", fontSize = 11.sp)
                                    Text(logItem, fontSize = 11.sp, color = Color.LightGray, lineHeight = 15.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

fun LegGlassPurpleStyle(cls: String): Color {
    return when(cls) {
        "Warrior" -> QuestRed
        "Mage" -> AmethystPurple
        else -> GlowingCyan
    }
}

// ==========================================
// 6. SETTINGS SCREEN (PORTAL PREFERENCE & DATA BACKUP)
// ==========================================
@Composable
fun SettingsScreen(
    viewModel: QuestViewModel,
    language: AppLanguage
) {
    val profile by viewModel.playerProfile.collectAsState()
    var backupStringInput by remember { mutableStateOf("") }
    
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "⚙️ ${Translations.getString("settings_tab", language)}",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Multi language switcher panel card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🌐 ${Translations.getString("select_language", language)}",
                    color = LegendaryGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                AppLanguage.values().forEach { lang ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.updateLanguage(lang.code) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = lang.label,
                            color = if (profile.languageCode == lang.code) GlowingCyan else MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = if (profile.languageCode == lang.code) FontWeight.Bold else FontWeight.Normal
                        )
                        
                        RadioButton(
                            selected = profile.languageCode == lang.code,
                            onClick = { viewModel.updateLanguage(lang.code) },
                            colors = RadioButtonDefaults.colors(selectedColor = GlowingCyan)
                        )
                    }
                }
            }
        }

        // Toggles for dynamic theme modes Dark Mode and Light Mode
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == AppLanguage.FA) "🌓 چیدمان حس‌های سیستم (حالت زمینه)" else "🌓 System Senses Layout (Theme Mode)", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 15.sp, 
                        color = LegendaryGold
                    )
                    Text(
                        text = if (language == AppLanguage.FA) "جابجایی بین تم جادوییِ تاریک حماسی و طرح پاکیزهٔ روشن با آسودگی چشم." else "Toggle between Dark Void magical ambient and clean pure light layout.", 
                        fontSize = 12.sp, 
                        color = Color.Gray, 
                        lineHeight = 16.sp
                    )
                }

                Switch(
                    checked = profile.isDarkMode,
                    onCheckedChange = { viewModel.toggleDarkMode() },
                    colors = SwitchDefaults.colors(checkedThumbColor = GlowingCyan, checkedTrackColor = GlowingCyan.copy(alpha = 0.4f))
                )
            }
        }

        // Cloud sync prepare module
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (language == AppLanguage.FA) "☁️ همگام‌ساز ذخیره‌سازی ابری چندنفره" else "☁️ Prepared Multiplayed Cloud Sync Engine", 
                    fontWeight = FontWeight.Bold, 
                    color = LegendaryGold
                )
                Text(
                    text = if (language == AppLanguage.FA) "کانفیگ دستی گره‌های معابد جهت همگام‌سازی ابری اطلاعات قهرمانان به سرور مرکزی خطوط بازی." else "Configure parameters to prepare local game credentials for online node networks.", 
                    fontSize = 12.sp, 
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.FA) "وضعیت نشانه همگام‌سازی:" else "Synchronization Token Status:", 
                        fontSize = 12.sp
                    )
                    Box(
                        modifier = Modifier
                            .background(GlowingEmerald.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.FA) "گره برخط نسخه v0.4" else "Node Online v0.4", 
                            color = GlowingEmerald, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Copy Paste Backup & Restore panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (language == AppLanguage.FA) "🗄️ دفترچه پشتیبان‌گیری و بازیابی قهرمان محلی" else "🗄️ Local Champion Backup & Restore Ledger", 
                    fontWeight = FontWeight.Bold, 
                    color = LegendaryGold, 
                    fontSize = 15.sp
                )
                Text(
                    text = if (language == AppLanguage.FA) "رشته‌های پشتیبان متنی آفلاین و کاملاً قوی تولید و صادر کنید، یا آنها را برای بازگشت فوری به حافظه بچسبانید!" else "Generate and export completely robust offline text backup strings, or paste them returning into chronological memory instantly!", 
                    fontSize = 12.sp, 
                    color = Color.Gray, 
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val plain = viewModel.exportBackupToString()
                            clipboardManager.setText(AnnotatedString(plain))
                            val succText = if (language == AppLanguage.FA) "پشتیبان در حافظه موقت کپی شد! پیش خود حفظ کنید." else "Backup copied to clipboard! Keep it safe."
                            Toast.makeText(context, succText, Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GlowingCyan),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.FA) "استخراج کد ذخیره‌سازی آفلاین" else "Export Save String", 
                            fontSize = 11.sp, 
                            color = DarkVoid, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = backupStringInput,
                    onValueChange = { backupStringInput = it },
                    placeholder = { 
                        Text(
                            text = if (language == AppLanguage.FA) "کد رشته پشتیبان LifeQuestBackup معتبر را اینجا بچسبانید..." else "Paste valid LifeQuestBackup string here...", 
                            color = Color.Gray, 
                            fontSize = 12.sp
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = GlowingCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (backupStringInput.isNotBlank()) {
                            val success = viewModel.importBackupFromString(backupStringInput)
                            if (success) {
                                val succRestore = if (language == AppLanguage.FA) "وضعیت ماجراجویی با موفقیت بازیابی شد!" else "State Restored Successfully!"
                                Toast.makeText(context, succRestore, Toast.LENGTH_LONG).show()
                                backupStringInput = ""
                            } else {
                                val failRestore = if (language == AppLanguage.FA) "خطا: رشته وضعیت بازیابی نامعتبر یا فاسد است!" else "Failed: State String Corrupt!"
                                Toast.makeText(context, failRestore, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowingEmerald),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.FA) "وارد کردن و بازنشانی همه‌ جانبه" else "Import & Restore State", 
                        fontSize = 12.sp, 
                        color = DarkVoid, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Reset journey wipe button
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GlassSurface),
            border = BorderStroke(1.dp, QuestRed.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "💀 ${Translations.getString("reset_data", language)}",
                    color = QuestRed,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = Translations.getString("reset_warning", language),
                    color = TextLight,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Button(
                    onClick = { showResetDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = QuestRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reset_progress_button")
                ) {
                    Text(
                        text = Translations.getString("reset_button", language),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "💀 WARNING / هشدار",
                    color = QuestRed,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to end your hero's chronicle and wipe your memory?\n\nآیا مطمئن هستید که می‌خواهید ماجراجویی خود را پاک کنید؟",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetProgress()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = QuestRed)
                ) {
                    Text(text = "Reset Journey", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(text = "Cancel", color = Color.Gray)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

// ==========================================
// 7. FLOATING COMBAT XP TICKER ALERT
// ==========================================
@Composable
fun FloatingXpGainIndicator(
    xpGainedValue: Int?,
    language: AppLanguage
) {
    AnimatedVisibility(
        visible = xpGainedValue != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        if (xpGainedValue != null) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    modifier = Modifier.shadow(16.dp, RoundedCornerShape(24.dp), ambientColor = LegendaryGold, spotColor = LegendaryGold),
                    colors = CardDefaults.cardColors(containerColor = DarkVoid),
                    border = BorderStroke(2.dp, LegendaryGold),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚔️ ",
                            fontSize = 18.sp
                        )
                        Text(
                            text = String.format(Translations.getString("quest_xp_gain", language), xpGainedValue),
                            color = LegendaryGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. CRITICAL GLOWING LEVEL UP HUD DIALOG
// ==========================================
@Composable
fun LevelUpOverlayDialog(
    newLevel: Int?,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    if (newLevel != null) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = LegendaryGold, ambientColor = LegendaryGold)
                        .border(3.dp, Brush.radialGradient(listOf(LegendaryGold, GlowingCyan)), RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = GlassSurface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .border(4.dp, LegendaryGold, CircleShape)
                                .background(DarkVoid)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_rpg_hero),
                                contentDescription = "Hero Crest",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = Translations.getString("level_up", language),
                            color = LegendaryGold,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Serif,
                            modifier = Modifier.shadow(4.dp, shape = RoundedCornerShape(4.dp))
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = String.format(Translations.getString("level_up_congrats", language), newLevel),
                            color = TextLight,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkVoid.copy(alpha = 0.5f)),
                            border = BorderStroke(1.dp, BorderSilver)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val lvlBonusText = if (language == AppLanguage.FA) {
                                    "🌟 پاداش ویژگی آزاد شد +۱ 🌟\n⭐ امتیاز استعداد (TP) را در درخت مهارت خرج کنید!"
                                } else {
                                    "🌟 attributes bonus unlocked +1 🌟\n⭐ Spend Talent Point (TP) on Arcane Skill Tree!"
                                }
                                Text(
                                    text = lvlBonusText,
                                    color = GlowingCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .shadow(8.dp, RoundedCornerShape(8.dp), spotColor = GlowingCyan),
                            colors = ButtonDefaults.buttonColors(containerColor = GlowingCyan),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = Translations.getString("close", language),
                                color = DarkVoid,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

class RegionInfo(
    val id: Int,
    val name: String,
    val nameFa: String,
    val nameDe: String,
    val icon: String,
    val minLvl: Int,
    val categories: String,
    val rLvl: Int,
    val rXp: Int,
    val rReq: Int
)

@Composable
fun WorldMapScreen(viewModel: QuestViewModel, language: AppLanguage) {
    val selectedRegion by viewModel.selectedRegionIndex.collectAsState()
    val prestigeVal by viewModel.prestigeRank.collectAsState()
    val minorC by viewModel.minorChests.collectAsState()
    val goldC by viewModel.goldChests.collectAsState()
    val legendaryC by viewModel.legendaryChests.collectAsState()
    val activeStory by viewModel.activeStoryQuest.collectAsState()
    val npcDialog by viewModel.activeNpcDialog.collectAsState()
    val chestOpenResult by viewModel.chestOpenResult.collectAsState()
    val profile by viewModel.playerProfile.collectAsState()

    val solisLvl by viewModel.solisLvl.collectAsState()
    val solisXp by viewModel.solisXp.collectAsState()
    
    val sagesLvl by viewModel.sagesLvl.collectAsState()
    val sagesXp by viewModel.sagesXp.collectAsState()

    val rogueLvl by viewModel.rogueLvl.collectAsState()
    val rogueXp by viewModel.rogueXp.collectAsState()

    val healthTempleLevel by viewModel.healthTempleLevel.collectAsState()
    val fitnessArenaLevel by viewModel.fitnessArenaLevel.collectAsState()
    val learningAcademyLevel by viewModel.learningAcademyLevel.collectAsState()
    val financeVaultLevel by viewModel.financeVaultLevel.collectAsState()
    val careerCitadelLevel by viewModel.careerCitadelLevel.collectAsState()
    val relationshipGardenLevel by viewModel.relationshipGardenLevel.collectAsState()
    val quests by viewModel.allQuests.collectAsState(initial = emptyList())

    val regions = remember(solisLvl, solisXp, sagesLvl, sagesXp, rogueLvl, rogueXp) {
        listOf(
            RegionInfo(0, "Sovereignty of Solis", "قلمرو سولیس", "Herrschaft von Solis", "☀️", 1, "Strength, Health, Fitness", solisLvl, solisXp, solisLvl * 150),
            RegionInfo(1, "Sages of Highcliff", "فرزانگان هایکلیف", "Weisen von Highcliff", "🧙‍♀️", 5, "Learning, Academic, Intelligence", sagesLvl, sagesXp, sagesLvl * 150),
            RegionInfo(2, "Rogue's Refuge", "پناهگاه یاغی", "Refugium des Schurken", "🗡️", 10, "Discipline, Work, Routines", rogueLvl, rogueXp, rogueLvl * 150),
            RegionInfo(3, "Amethyst Spires", "مناره‌های آمیتیس", "Amethysttürme", "✨", 15, "Mythical Focus & Apotheosis", 1, 0, 1000)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Atmospheric World Map Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF140E24)),
            border = BorderStroke(1.5.dp, GlowingCyan.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (language == AppLanguage.FA) "🗺️ نقشه کارزار حماسی" else "🗺️ CAMPAIGN MAP",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = GlowingCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (language == AppLanguage.FA) "کارهای روزمره و مهارت‌های زندگی واقعی خود را کامل کنید تا فرمانروایی خود را گسترش دهید و نشان‌های مرموز کسب نمایید." else "Accomplish your real-life tasks to expand your sovereignty and unlock mystical items.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // Interactive Map Canvas and Node Markers
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0C071A))
                .border(1.5.dp, AmethystPurple.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
        ) {
            // Drawn routes on Map
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                
                // Route Nodes Points
                val p1 = Offset(w * 0.25f, h * 0.45f) // Solis
                val p2 = Offset(w * 0.75f, h * 0.35f) // Sages
                val p3 = Offset(w * 0.4f, h * 0.82f)  // Rogue's Refuge
                val p4 = Offset(w * 0.8f, h * 0.75f)  // Amethyst Spires

                drawCircle(color = GlowingCyan.copy(alpha = 0.15f), radius = 60f, center = p1)
                drawCircle(color = GlowingCyan.copy(alpha = 0.15f), radius = 60f, center = p2)
                drawCircle(color = GlowingCyan.copy(alpha = 0.15f), radius = 60f, center = p3)
                drawCircle(color = GlowingCyan.copy(alpha = 0.15f), radius = 60f, center = p4)

                // Drawn connecting pathways
                drawLine(color = AmethystPurple.copy(alpha = 0.6f), start = p1, end = p2, strokeWidth = 5f)
                drawLine(color = AmethystPurple.copy(alpha = 0.6f), start = p2, end = p4, strokeWidth = 5f)
                drawLine(color = AmethystPurple.copy(alpha = 0.6f), start = p1, end = p3, strokeWidth = 5f)
                drawLine(color = AmethystPurple.copy(alpha = 0.6f), start = p3, end = p4, strokeWidth = 5f)
            }

            // Region Icon Overlay Markers
            regions.forEach { reg ->
                val isUnlocked = profile.level >= reg.minLvl || prestigeVal > 0
                val (offsetX, offsetY) = when (reg.id) {
                    0 -> Pair(0.18f, 0.36f)
                    1 -> Pair(0.68f, 0.26f)
                    2 -> Pair(0.32f, 0.72f)
                    3 -> Pair(0.72f, 0.66f)
                    else -> Pair(0.5f, 0.5f)
                }

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val floatX = constraints.maxWidth * offsetX
                    val floatY = constraints.maxHeight * offsetY

                    Box(
                        modifier = Modifier
                            .absoluteOffset(
                                x = (floatX / LocalDensity.current.density).dp,
                                y = (floatY / LocalDensity.current.density).dp
                            )
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                if (selectedRegion == reg.id) GlowingCyan.copy(alpha = 0.25f)
                                else Color(0xFF1B1238)
                            )
                            .border(
                                width = if (selectedRegion == reg.id) 2.dp else 1.dp,
                                color = when {
                                    !isUnlocked -> Color.Gray.copy(alpha = 0.6f)
                                    selectedRegion == reg.id -> GlowingCyan
                                    else -> AmethystPurple
                                },
                                shape = CircleShape
                            )
                            .clickable {
                                viewModel.selectRegion(reg.id)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(text = if (isUnlocked) reg.icon else "🔒", fontSize = 18.sp)
                            Text(
                                text = "Lvl ${reg.minLvl}",
                                fontSize = 8.sp,
                                color = if (isUnlocked) LegendaryGold else Color.LightGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected Region Detail Card
        val curRegion = regions.getOrElse(selectedRegion) { regions.first() }
        val isSelectedUnlocked = profile.level >= curRegion.minLvl || prestigeVal > 0

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF140E26)),
            border = BorderStroke(1.dp, AmethystPurple)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (language == AppLanguage.FA) curRegion.nameFa else curRegion.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = GlowingCyan
                        )
                        val catDisp = if (language == AppLanguage.FA) {
                            val curRegionCats = when(curRegion.id) {
                                0 -> "قدرت فیزیکی، تندرستی، آمادگی جسمانی"
                                1 -> "یادگیری، تحصیلی، هوش و تمرکز"
                                2 -> "انضباط، کار، روتین‌های روزمرگی"
                                else -> "تمرکز اساطیری و پیشرفت همه‌جانبه"
                            }
                            "دسته‌های تمرکز: $curRegionCats"
                        } else "Focus Categories: ${curRegion.categories}"
                        Text(
                            text = catDisp,
                            fontSize = 11.sp,
                            color = Color.LightGray.copy(alpha = 0.8f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelectedUnlocked) GlowingCyan.copy(alpha = 0.1f)
                                else Color.Red.copy(alpha = 0.1f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isSelectedUnlocked) {
                                if (language == AppLanguage.FA) "آزاد شده" else "UNLOCKED"
                            } else {
                                if (language == AppLanguage.FA) "قفل است (سطح ${curRegion.minLvl}+)" else "LOCKED (Lvl ${curRegion.minLvl}+)"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelectedUnlocked) GlowingCyan else Color.Red
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isSelectedUnlocked) {
                    // Kingdom Experience progress
                    Text(
                        text = if (language == AppLanguage.FA) "حاکمیت پادشاهی: رتبه ${curRegion.rLvl}" else "Kingdom Sovereignty: Tier ${curRegion.rLvl}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LegendaryGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val pRatio = (curRegion.rXp.toFloat() / curRegion.rReq.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = pRatio,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = LegendaryGold,
                        trackColor = Color(0xFF281C44)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (language == AppLanguage.FA) "تجربه: ${curRegion.rXp} / ${curRegion.rReq}" else "Exp: ${curRegion.rXp} / ${curRegion.rReq}", 
                            fontSize = 9.sp, 
                            color = Color.Gray
                        )
                        Text(
                            text = if (language == AppLanguage.FA) "تقویت: +${curRegion.rLvl * 5}٪ شانس دریافت غنیمت" else "Boost: +${curRegion.rLvl * 5}% drop chance", 
                            fontSize = 9.sp, 
                            color = GlowingCyan
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = AmethystPurple.copy(alpha = 0.4f))

                    // Meet Local NPC Actions
                    val npcName = when (curRegion.id) {
                        0 -> if (language == AppLanguage.FA) "فرمانده تورین 🛡️" else "Commander Thorin 🛡️"
                        1 -> if (language == AppLanguage.FA) "ساحر بزرگ لایرا 🧙‍♀️" else "Archmage Lyra 🧙‍♀️"
                        2 -> if (language == AppLanguage.FA) "استاد بزرگ جک 🗡️" else "Grandmaster Jack 🗡️"
                        else -> if (language == AppLanguage.FA) "بافنده کریستال اتل ✨" else "Crystal Weaver Aethel ✨"
                    }

                    val dialogScript = when (curRegion.id) {
                        0 -> if (language == AppLanguage.FA) {
                            listOf(
                                "به میدان‌های آموزشی خوش آمدید! کارهای ورزشی و تندرستی خود را برطرف کنید تا مرزهای ما مستحکم بماند.",
                                "فرمانده لبخند می‌زند: 'روی فواصل منظم تمرکز کنید. داشتن جثه‌ای استوار و سالم، ذهن سالم به ارمغان می‌‌آورد!'"
                            )
                        } else {
                            listOf(
                                "Welcome to the training grounds! Complete strength and wellness objectives to fortify our borders.",
                                "The Commander smiles: 'Focus on regular athletic intervals. Building robust body health feeds robust intelligence!'"
                            )
                        }
                        1 -> if (language == AppLanguage.FA) {
                            listOf(
                                "آه، ذهن درخشان دیگری توفیق معرفت جسته است. بایگانی بزرگ ما مسیر آموزش‌های شما را رصد می‌کند.",
                                "کیمیاگر رون جادویی را بالا می‌برد: 'مطالعه را به جلسات کوتاه‌مدت متمرکز بخش کنید. فواصل متمرکز عمیق از خستگی ذهن جلوگیری می‌کند.'"
                            )
                        } else {
                            listOf(
                                "Ah, another stellar mind seeking illumination. Our grand archives track your learning targets.",
                                "The Archmage raises a rune: 'Break study into short, intense sprints. Deep focused intervals prevent mental fatigue.'"
                            )
                        }
                        2 -> if (language == AppLanguage.FA) {
                            listOf(
                                "به دنبال گذر از سد محدودیت‌های خویش هستید؟ بر روتین‌های روزمره‌تان مسلط شوید تا بی‌صدا از موانع عبور کنید.",
                                "جک می‌خندد: 'استمرار قوی‌ترین سلاح است. عادت‌های خود را به صورت پیاپی به هم گره بزنید تا خودکار آغاز شوند.'"
                            )
                        } else {
                            listOf(
                                "Looking to slip past limitations? Master your daily routines to slip past standard blocks unnoticed.",
                                "Jack laughs: 'Consistency is the ultimate weapon. Stack your habits sequentially so they trigger automatically.'"
                            )
                        }
                        else -> if (language == AppLanguage.FA) {
                            listOf(
                                "شما در مقابل پورتال جریان تمرکز ایستاده‌اید. بلورها هماهنگ با والاترین پله‌های پیشرفت شما می‌تپند.",
                                "بافنده نجوا می‌کند: 'هنگام دنبال کردن اهداف کلیدی، از حواس‌پرتی بپرهیزید. کتیبه‌های تاریخی خود را بخش به بخش خلق کنید.'"
                            )
                        } else {
                            listOf(
                                "You stand before the portal of flow. The crystals pulse in sync with your highest achievements.",
                                "The Weaver whispers: 'When tracking high priority milestones, avoid distraction. Build your legendary legacy piece by piece.'"
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = if (language == AppLanguage.FA) "نماینده قلمرو: " else "Representative: ", fontSize = 12.sp, color = Color.LightGray)
                        Text(text = npcName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GlowingCyan)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.talkToNpc(npcName, dialogScript) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AmethystPurple)
                    ) {
                        Text(
                            text = if (language == AppLanguage.FA) "هم‌کلام شدن با نماینده" else "Speak to NPC", 
                            color = Color.White, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Text(
                        text = if (language == AppLanguage.FA) {
                            "اعلامیه حاکمیت: این قلمرو عرفانی در مه‌ غلیظی پوشیده شده است. سطح بهره‌وری واقعی زندگی خود را به سطح ${curRegion.minLvl} برسانید تا در این مرزها رخنه کنید."
                        } else {
                            "Sovereign notice: This mystical territory is shrouded in fog. Increase your real-life productivity level to ${curRegion.minLvl} to penetrate the borders."
                        },
                        fontSize = 12.sp,
                        color = Color.LightGray.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Loot Chest Treasure Vault Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF140E26)),
            border = BorderStroke(1.dp, AmethystPurple)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (language == AppLanguage.FA) "📦 خزانه صندوق‌های پاداش حماسی" else "📦 REWARD CHESTS VAULT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = LegendaryGold
                )
                Text(
                    text = if (language == AppLanguage.FA) "صندوق‌های بدست آمده از عادات و کارهای کامل شده را باز کنید تا زره افسانه‌ای با ویژگی‌های پیشرفته و طلا دریافت کنید." else "Open chests dropped from completed habits and quests to gain high stat legendary armor & gold.",
                    fontSize = 11.sp,
                    color = Color.LightGray.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ChestItemCard(
                        title = if (language == AppLanguage.FA) "صندوق فرسوده" else "Common Chest",
                        count = minorC,
                        color = Color.Gray,
                        icon = "📦",
                        language = language,
                        onOpen = { viewModel.openChest("Minor") }
                    )
                    ChestItemCard(
                        title = if (language == AppLanguage.FA) "صندوق طلا" else "Gold chest",
                        count = goldC,
                        color = LegendaryGold,
                        icon = "🟡",
                        language = language,
                        onOpen = { viewModel.openChest("Gold") }
                    )
                    ChestItemCard(
                        title = if (language == AppLanguage.FA) "صندوق اساطیر" else "Mythic Chest",
                        count = legendaryC,
                        color = GlowingCyan,
                        icon = "💎",
                        language = language,
                        onOpen = { viewModel.openChest("Legendary") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // KINGDOM BUILDING & UPGRADES SYSTEM
        val isFarsiMap = language == AppLanguage.FA
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF140E26)),
            border = BorderStroke(1.5.dp, GlowingCyan.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isFarsiMap) "👑 توسعه و آبادانی سازه‌های امپراتوری" else "👑 EMPIRE STRUCTURES & HEAVY UPGRADES",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = GlowingCyan
                )
                Text(
                    text = if (isFarsiMap) "برای ارتقای هر سازه، طلا خرج کنید و با انجام کارهای سخت در دنیای واقعی، صلاحیت خود را اثبات نمایید!" else "Spend gold coffers and accomplish real-life category goals to qualify for legendary level upgrades!",
                    fontSize = 11.sp,
                    color = Color.LightGray.copy(alpha = 0.8f),
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Local compile-safe structure holder
                data class UpgradeStructure(
                    val id: String,
                    val title: String,
                    val level: Int,
                    val category: String,
                    val costGold: Int,
                    val requiredRealLifeQuests: Int
                )

                val buildingsList = listOf(
                    UpgradeStructure("health_temple", if (isFarsiMap) "معبد سلامت و طول عمر 🏥" else "Health & Wellness Temple 🏥", healthTempleLevel, "Health", healthTempleLevel * 125, healthTempleLevel * 2),
                    UpgradeStructure("fitness_arena", if (isFarsiMap) "ورزشگاه و کولوسئوم بدنسازی ⚔️" else "Fitness & Training Arena ⚔️", fitnessArenaLevel, "Fitness", fitnessArenaLevel * 125, fitnessArenaLevel * 2),
                    UpgradeStructure("learning_academy", if (isFarsiMap) "دارالحکمه و آکادمی دانش 📖" else "Sages Learning Academy 📖", learningAcademyLevel, "Learning", learningAcademyLevel * 125, learningAcademyLevel * 2),
                    UpgradeStructure("finance_vault", if (isFarsiMap) "خزانه طلا و کیمیاگری مالی 🪙" else "Alchemy Finance Vault 🪙", financeVaultLevel, "Finance", financeVaultLevel * 125, financeVaultLevel * 2),
                    UpgradeStructure("career_citadel", if (isFarsiMap) "ارگ اصلی صعود شغلی 🏰" else "Career & Trade Citadel 🏰", careerCitadelLevel, "Career", careerCitadelLevel * 125, careerCitadelLevel * 2),
                    UpgradeStructure("relationship_garden", if (isFarsiMap) "بوستان پردیس روابط و صمیمیت 🌸" else "Harmony Relationship Garden 🌸", relationshipGardenLevel, "Relationships", relationshipGardenLevel * 125, relationshipGardenLevel * 2)
                )

                buildingsList.forEach { b ->
                    val realityDoneCount = quests.count { it.isCompleted && it.category.equals(b.category, ignoreCase = true) }
                    val meetsRequirement = realityDoneCount >= b.requiredRealLifeQuests
                    val meetsGold = (profile?.gold ?: 0) >= b.costGold

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1439)),
                        border = BorderStroke(1.dp, if (meetsRequirement && meetsGold) GlowingCyan.copy(alpha = 0.3f) else Color.Red.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(b.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = LegendaryGold)
                                    Text(
                                        text = if (isFarsiMap) "سطح فعلی: ${b.level}" else "Current Level: ${b.level}",
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(GlowingCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isFarsiMap) "پاداش: +${b.level * 4}٪" else "Bonus: +${b.level * 4}%",
                                        fontSize = 10.sp,
                                        color = GlowingCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Requirements displays
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    val catLabelFA = when(b.category) {
                                        "Health" -> "سلامت"
                                        "Fitness" -> "تناسب اندام"
                                        "Learning" -> "یادگیری"
                                        "Finance" -> "مالی"
                                        "Career" -> "شغلی"
                                        else -> "روابط"
                                    }
                                    val reqText = if (isFarsiMap) {
                                        "پیش‌نیاز واقعی: انجام ${b.requiredRealLifeQuests} فعالیت $catLabelFA (انجام شده: $realityDoneCount)"
                                    } else {
                                        "Productivity Requirement: $realityDoneCount/${b.requiredRealLifeQuests} completed '${b.category}' tasks"
                                    }
                                    Text(
                                        text = reqText,
                                        fontSize = 10.sp,
                                        color = if (meetsRequirement) GlowingCyan else Color(0xFFFF4D4D)
                                    )
                                    
                                    val costText = if (isFarsiMap) {
                                        "هزینه نوسازی: 🪙 ${b.costGold} طلای صندوق"
                                    } else {
                                        "Cohesive Cost: 🪙 ${b.costGold} Gold"
                                    }
                                    Text(
                                        text = costText,
                                        fontSize = 10.sp,
                                        color = if (meetsGold) LegendaryGold else Color(0xFFFF4D4D)
                                    )
                                }

                                Button(
                                    onClick = { viewModel.upgradeKingdomBuilding(b.id, b.costGold, b.category, b.requiredRealLifeQuests) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (meetsRequirement && meetsGold) GlowingCyan else Color(0xFF2E244A)
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isFarsiMap) "🔨 بازسازی" else "🔨 Upgrade",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (meetsRequirement && meetsGold) DarkVoid else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // STORY QUEST PROGRESSION INTERFACE
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B1E)),
            border = BorderStroke(1.5.dp, LegendaryGold.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (language == AppLanguage.FA) "👑 خط داستانی فعال کارزار" else "👑 ACTIVE STORY CAMPAIGN",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = LegendaryGold
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (activeStory != null) {
                    val quest = activeStory!!
                    val questTitle = if (language == AppLanguage.FA) {
                        when (quest.id) {
                            "sq_1" -> "تامین سوخت فانوس خورشیدی ☀️"
                            "sq_2" -> "رمزگشایی کتیبه‌های فرزانگان 📖"
                            "sq_3" -> "نفوذ در سایه‌های پناهگاه 🗡️"
                            "sq_4" -> "شفای طاعون جادویی 🏥"
                            "sq_5" -> "کمال نهاییِ آگاهی ذهن 👑"
                            else -> quest.title
                        }
                    } else quest.title

                    val questDesc = if (language == AppLanguage.FA) {
                        when (quest.id) {
                            "sq_1" -> "مناره خورشیدی باستانی را در قلمروی سولیس با فعالیت‌های ورزشی و تندرستی مجدداً روشن کنید."
                            "sq_2" -> "کتیبه‌های جادوییِ تمرکز ذهن را در صخره‌های مرتفع با یادگیری و مطالعه ترجمه و رمزگشایی کنید."
                            "sq_3" -> "عادات روزانه منظم و با انضباط ایجاد کنید و بدون جلب توجه در مخفیگاه سایه‌ها پیش روی نمایید."
                            "sq_4" -> "انرژی‌های شفابخش تندرستی را برای مقابله با بیماری شیوع یافته در مناره‌های بنفش ارسال کنید."
                            "sq_5" -> "به نقاط عطف تمرکز ذهن (با انجام کارهای عمومی در سراسر دنیا) دست پیدا کنید."
                            else -> quest.desc
                        }
                    } else quest.desc

                    Text(text = questTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GlowingCyan)
                    Text(text = questDesc, fontSize = 11.sp, color = Color.LightGray)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            val catMapFA = if (language == AppLanguage.FA) {
                                when(quest.category) {
                                    "Strength" -> "قدرت فیزیکی"
                                    "Discipline" -> "انضباط فردی"
                                    "Learning" -> "یادگیری"
                                    else -> "تندرستی"
                                }
                            } else quest.category
                            Text(
                                text = if (language == AppLanguage.FA) "شاخه هدف: $catMapFA" else "Target Category: ${quest.category}", 
                                fontSize = 10.sp, 
                                color = LegendaryGold
                            )
                            val rewGoldText = if (language == AppLanguage.FA) "سکه طلا" else "Gold"
                            val rewXpText = if (language == AppLanguage.FA) "تجربه" else "XP"
                            val rewItemText = if (language == AppLanguage.FA) {
                                when(quest.rewardItem) {
                                    "Flame Sword" -> "شمشیر شعله‌ور سوزان"
                                    "Iron Plate Mail" -> "زره غول‌آسا فلزی"
                                    "Ruby Ring" -> "انگشتر یاقوت سرخ"
                                    else -> quest.rewardItem
                                }
                            } else quest.rewardItem
                            Text(
                                text = if (language == AppLanguage.FA) "جوایز: 🪙 ${quest.rewardGold} $rewGoldText، ✨ ${quest.rewardXp} $rewXpText، ⚔️ $rewItemText" else "Rewards: 🪙 ${quest.rewardGold} Gold, ✨ ${quest.rewardXp} XP, ⚔️ ${quest.rewardItem}", 
                                fontSize = 10.sp, 
                                color = Color.LightGray
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(Color(0xFF1F153F), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "${quest.progress} / ${quest.targetCount}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GlowingCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (quest.isCompleted) {
                        Button(
                            onClick = { viewModel.claimStoryQuestReward() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = LegendaryGold)
                        ) {
                            Text(
                                text = if (language == AppLanguage.FA) "دریافت پاداش داستانی کارزار" else "CLAIM STORY REWARD", 
                                color = DarkVoid, 
                                fontWeight = FontWeight.ExtraBold, 
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        Button(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E244A))
                        ) {
                            Text(
                                text = if (language == AppLanguage.FA) "کارهای فیزیکی واقعی را انجام دهید تا داستان پیش برود" else "COMPLETE REAL TASKS TO PROGRESS", 
                                color = Color.Gray, 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = if (language == AppLanguage.FA) {
                            "تبریک و تهنیت! شما تمامی ماموریت‌های کارزار داستانی را در قلمروی آلفا با موفقیت انجام داده‌اید! با فرمانده تورین یا معبد فرزانگان سخن بگویید تا صعود مجدد پرستیژ را آغاز کنید."
                        } else {
                            "Congratulations! You have successfully completed all story campaign quests in the alpha realm! Speak to Thorin or the High Sages to initiate high prestige loops."
                        },
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }
            }
        }
    }

    // NPC Dialog Overlay
    if (npcDialog != null) {
        val (npc, dialogueLines) = npcDialog!!
        var currentSpeechIndex by remember { mutableStateOf(0) }

        AlertDialog(
            onDismissRequest = { viewModel.dismissNpcDialog() },
            title = {
                Text(text = npc, color = GlowingCyan, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = dialogueLines.getOrElse(currentSpeechIndex) { dialogueLines.last() },
                    fontSize = 14.sp,
                    color = Color.White
                )
            },
            confirmButton = {
                if (currentSpeechIndex < dialogueLines.size - 1) {
                    Button(
                        onClick = { currentSpeechIndex++ },
                        colors = ButtonDefaults.buttonColors(containerColor = AmethystPurple)
                    ) {
                        Text(
                            text = if (language == AppLanguage.FA) "ادامه گفتگو" else "Continue", 
                            color = Color.White
                        )
                    }
                } else {
                    Button(
                        onClick = { viewModel.dismissNpcDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = GlowingCyan)
                    ) {
                        Text(
                            text = if (language == AppLanguage.FA) "بدرود، ای مبارز" else "Farewell", 
                            color = DarkVoid
                        )
                    }
                }
            },
            containerColor = Color(0xFF160E2D)
        )
    }

    // Loot Result Chest notification popup override
    if (chestOpenResult != null) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    text = if (language == AppLanguage.FA) "✨ دریافت نشان مرموز تصادفی ✨" else "✨ UNCOVERED MYSTERY DROP ✨", 
                    color = LegendaryGold, 
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (language == AppLanguage.FA) "🎉 موفقیت و دستاورد حماسی 🎉" else "🎉 SUCCESS 🎉", 
                        fontSize = 20.sp, 
                        color = GlowingCyan, 
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    val chestRes = chestOpenResult!!
                    val translatedResult = if (language == AppLanguage.FA) {
                        chestRes
                            .replace("Found ", "یافت شد ")
                            .replace(" Gold and ", " سکه طلا و ")
                            .replace("uncommon drop: ", "نشان کمیاب مغلوب شده: ")
                            .replace("rare drop: ", "نشان ارزشمند طلاکوب: ")
                            .replace("legendary armor: ", "زره افسانه‌ای منحصربه‌فرد باستانی: ")
                            .replace("Gold!", "سکه طلا!")
                            .replace("Flame Sword", "شمشیر شعله‌ور سوزان")
                            .replace("Iron Plate Mail", "زره غول‌آسا فلزی")
                            .replace("Ruby Ring", "انگشتر یاقوت سرخ")
                    } else chestRes
                    Text(text = translatedResult, fontSize = 14.sp, color = Color.White, textAlign = TextAlign.Center)
                }
            },
            confirmButton = {},
            containerColor = Color(0xFF160E35)
        )
    }
}

@Composable
fun ChestItemCard(title: String, count: Int, color: Color, icon: String, language: AppLanguage, onOpen: () -> Unit) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .clickable(enabled = count > 0, onClick = onOpen),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1435)),
        border = BorderStroke(1.dp, if (count > 0) color else Color.DarkGray)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = icon, fontSize = 24.sp)
            Text(text = title, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.LightGray, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .background(if (count > 0) color else Color.DarkGray, RoundedCornerShape(10.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                val ownedText = if (language == AppLanguage.FA) "موجود: $count" else "Owned: $count"
                Text(text = ownedText, fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Black)
            }
        }
    }
}

@Composable
fun LeaderboardScreen(viewModel: QuestViewModel, language: AppLanguage) {
    val rankingList by viewModel.leaderboard.collectAsState()

    // Ensure rankings are populated
    LaunchedEffect(Unit) {
        viewModel.updateRankings()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // League Header Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF100824)),
            border = BorderStroke(1.5.dp, LegendaryGold)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.FA) "🏆 معاهدات و رتبه‌بندی قهرمانان" else "🏆 COVENANT RANKINGS",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = LegendaryGold,
                        letterSpacing = 1.sp
                    )
                    Box(
                        modifier = Modifier
                            .background(GlowingCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.FA) "فصل سوم کارزار" else "SEASON III", 
                            fontSize = 9.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = GlowingCyan
                        )
                    }
                }
                Text(
                    text = if (language == AppLanguage.FA) "دوشادوش قهرمانان حماسه‌ساز در سایر قلمروها رقابت کنید. پایبندی به عادات روزانه، تکمیل ماموریت‌ها و کسب پیشرفت داستانی، جایگاه پیمان شما را ارتقا می‌دهد." else "Compete with other legendary heroes across the kingdoms. Ticking positive habits, completing quests, and claiming story milestones advances your covenant standing.",
                    fontSize = 11.sp,
                    color = Color.LightGray.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Rankings Leaderboard Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (language == AppLanguage.FA) "رتبه و کلاس قهرمانی" else "Hero Rank & Class", 
                fontSize = 11.sp, 
                fontWeight = FontWeight.Bold, 
                color = Color.Gray
            )
            Text(
                text = if (language == AppLanguage.FA) "امتیازات معاهده" else "Covenant Points", 
                fontSize = 11.sp, 
                fontWeight = FontWeight.Bold, 
                color = Color.Gray
            )
        }

        // Leaderboard Rows list
        rankingList.forEachIndexed { index, entry ->
            val bgContainerColor = when {
                entry.isUser -> GlowingCyan.copy(alpha = 0.12f)
                index == 0 -> Color(0xFF2C194D) // 1st Place Champion Gold Tint
                index == 1 -> Color(0xFF21153D) // 2nd Place Silver Tint
                else -> Color(0xFF140E26)
            }

            val borderColor = when {
                entry.isUser -> GlowingCyan
                index == 0 -> LegendaryGold
                else -> Color.Transparent
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = bgContainerColor),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Position circle indicator
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(
                                    when (index) {
                                        0 -> LegendaryGold
                                        1 -> Color.LightGray
                                        2 -> Color(0xFFCD7F32)
                                        else -> Color(0xFF281D44)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (index <= 2) Color.Black else Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = entry.name,
                                    fontWeight = if (entry.isUser) FontWeight.ExtraBold else FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (entry.isUser) GlowingCyan else Color.White
                                )
                                if (entry.prestige > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(LegendaryGold.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(text = "⭐${entry.prestige}", fontSize = 8.sp, color = LegendaryGold, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            val classDisp = if (language == AppLanguage.FA) {
                                val cName = when(entry.clazz) {
                                    "Warrior" -> "رزم‌آور"
                                    "Mage" -> "ساحر"
                                    else -> "سرکش"
                                }
                                "کلاس قهرمان: $cName"
                            } else "Class: ${entry.clazz}"
                            Text(
                                text = classDisp,
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Text(
                        text = "✨ ${entry.score}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (index == 0) LegendaryGold else GlowingCyan
                    )
                }
            }
        }
    }
}
