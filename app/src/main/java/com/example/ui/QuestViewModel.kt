package com.example.ui

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class QuestViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs by lazy {
        application.getSharedPreferences("lifequest_rpg_system", android.content.Context.MODE_PRIVATE)
    }

    private val repository: QuestRepository
    
    val allQuests: StateFlow<List<Quest>>
    val playerProfile: StateFlow<PlayerProfile>
    val allHabits: StateFlow<List<Habit>>
    val allGoals: StateFlow<List<Goal>>
    val activeBoss: StateFlow<ActiveBoss?>
    val allBosses: StateFlow<List<ActiveBoss>>
    val allChallenges: StateFlow<List<Challenge>>

    // In-app premium notification ledger
    private val _notifications = MutableStateFlow<List<String>>(
        listOf(
            "Welcome back, Hero! Daily adventurer bounty is awaiting claim.",
            "Guild Notice: Chaos Rift opening preparing for next Seasonal Raid."
        )
    )
    val notifications: StateFlow<List<String>> = _notifications.asStateFlow()

    // RETENTION: Returning user bonus triggered state
    private val _showReturningBonus = MutableStateFlow(false)
    val showReturningBonus: StateFlow<Boolean> = _showReturningBonus.asStateFlow()

    // MOTIVATION: Celebration animation triggering overlays
    private val _showCelebration = MutableStateFlow(false)
    val showCelebration: StateFlow<Boolean> = _showCelebration.asStateFlow()
    private val _celebrationMessage = MutableStateFlow("")
    val celebrationMessage: StateFlow<String> = _celebrationMessage.asStateFlow()

    // MOTIVATION: Smart encouragement helper status
    private val _smartEncouragement = MutableStateFlow("")
    val smartEncouragement: StateFlow<String> = _smartEncouragement.asStateFlow()

    // THEMATIC: Simulated limited-time flash offers (offline timer)
    private val _flashOfferTimeLeft = MutableStateFlow("1h 42m")
    val flashOfferTimeLeft: StateFlow<String> = _flashOfferTimeLeft.asStateFlow()

    // Preparations for Guild Chat Feed congrats
    private val _guildChatMessages = MutableStateFlow<List<Pair<String, String>>>(
        listOf(
            "Merlin_Prog" to "Welcome to the guild chat! Together we slay deadlines.",
            "Galahad_99" to "Let's grind some quests today!"
        )
    )
    val guildChatMessages: StateFlow<List<Pair<String, String>>> = _guildChatMessages.asStateFlow()

    // ==========================================
    // PREMIUM ENGAGEMENT FEATURES STATE FLOWS
    // ==========================================
    // Companion pet equipment
    private val _companionEquipment = MutableStateFlow(prefs.getString("companion_equipment", "None") ?: "None")
    val companionEquipment: StateFlow<String> = _companionEquipment.asStateFlow()

    // Character customization properties (Styles, Skins, Portraits)
    private val _currentHairstyle = MutableStateFlow(prefs.getString("current_hairstyle", "Noble Spikes 💇‍♂️") ?: "Noble Spikes 💇‍♂️")
    val currentHairstyle: StateFlow<String> = _currentHairstyle.asStateFlow()

    private val _currentArmorSkin = MutableStateFlow(prefs.getString("current_armor_skin", "Cloth Tunic") ?: "Cloth Tunic")
    val currentArmorSkin: StateFlow<String> = _currentArmorSkin.asStateFlow()

    private val _currentPortrait = MutableStateFlow(prefs.getString("current_portrait", "Default Knight") ?: "Default Knight")
    val currentPortrait: StateFlow<String> = _currentPortrait.asStateFlow()

    // Dynamic Weather state
    private val _currentWeather = MutableStateFlow(prefs.getString("current_weather", "Sunny ☀️") ?: "Sunny ☀️")
    val currentWeather: StateFlow<String> = _currentWeather.asStateFlow()

    // Mythic Chest inventory
    private val _mythicChests = MutableStateFlow(prefs.getInt("chests_mythic", 1))
    val mythicChests: StateFlow<Int> = _mythicChests.asStateFlow()

    // Simulated offline notifications history log
    private val _simulatedNotificationsLog = MutableStateFlow<List<String>>(
        listOf(
            "🐾 Baby Phoenix is chirping! Earn XP to feed and level it up!",
            "⚡ Thunderstorm Weather active: Boss takes doubled habit damage!",
            "👹 WARNING! A new bad habit specter has emerged. Complete tasks to defeat it!",
            "🎃 Holiday Event: Summer Solstice / Frost trial active! Earn legendary cosmetics."
        )
    )
    val simulatedNotificationsLog: StateFlow<List<String>> = _simulatedNotificationsLog.asStateFlow()

    // Seasonal/Holiday Events special quests list
    data class EventQuest(
        val id: String,
        val title: String,
        val details: String,
        val requiredCount: Int,
        val currentCount: Int,
        val completed: Boolean,
        val pointsReward: Int,
        val rewardItem: String
    )

    private val _eventQuests = MutableStateFlow<List<EventQuest>>(
        listOf(
            EventQuest("eq_1", "☀️ Solar Harvest Trial", "Complete 3 Discipline/Work Quests under the Solis Sun.", 3, 0, false, 150, "Solar Scepter ☀️"),
            EventQuest("eq_2", "🎃 Spooky Nightmare Defeat", "Spawn and damage a Bad Habit Habit Monster.", 1, 0, false, 250, "Premium Mythic Chest"),
            EventQuest("eq_3", "🧘 Temple Breathwork", "Tick positive health/mindfulness habits.", 2, 0, false, 120, "Ancient Bell 🔔")
        )
    )
    val eventQuests: StateFlow<List<EventQuest>> = _eventQuests.asStateFlow()

    // Event quest progressor
    fun progressEventQuest(id: String) {
        val list = _eventQuests.value.map { eq ->
            if (eq.id == id && !eq.completed) {
                val nextCount = eq.currentCount + 1
                val checkComp = nextCount >= eq.requiredCount
                if (checkComp) {
                    viewModelScope.launch {
                        val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
                        var curInv = profile.inventory
                        if (eq.rewardItem.isNotEmpty() && !curInv.contains(eq.rewardItem)) {
                            curInv = if (curInv.isBlank()) eq.rewardItem else "$curInv,${eq.rewardItem}"
                        }
                        repository.insertOrUpdateProfile(profile.copy(
                            gold = profile.gold + eq.pointsReward,
                            inventory = curInv
                        ))
                        triggerCelebration("🏆 EVENT SPECIAL REWARD!\nUnlocked: ${eq.rewardItem} & +${eq.pointsReward} Gold!")
                        addNotification("🎉 Event complete! Claimed event exclusive: ${eq.rewardItem}!")
                    }
                }
                eq.copy(currentCount = nextCount, completed = checkComp)
            } else eq
        }
        _eventQuests.value = list
    }

    // Companion active treat training (Pet Leveling)
    fun trainCompanionPet() {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            val cost = if (_currentWeather.value == "Blizzard ❄️") 25 else 35
            if (profile.gold >= cost) {
                var nextXp = profile.companionXp + 25
                var nextLvl = profile.companionLevel
                if (nextXp >= 100) {
                    nextXp -= 100
                    nextLvl += 1
                    triggerCelebration("🐾 PET LEVELED UP!\n${profile.companionName} reached Level $nextLvl!")
                    addNotification("🎉 Beautiful! Your companion ${profile.companionName} leveled up to Level $nextLvl!")
                } else {
                    addNotification("🐾 Fed tasty treats to ${profile.companionName}! (+25 Companion XP)")
                }
                repository.insertOrUpdateProfile(
                    profile.copy(
                        gold = profile.gold - cost,
                        companionXp = nextXp,
                        companionLevel = nextLvl
                    )
                )
            } else {
                addNotification("❌ Not enough Gold (requires $cost Gold for treats)!")
            }
        }
    }

    // Companion Equipment
    fun equipCompanionItem(item: String, goldCost: Int) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            if (profile.gold >= goldCost) {
                _companionEquipment.value = item
                prefs.edit().putString("companion_equipment", item).apply()
                if (goldCost > 0) {
                    repository.insertOrUpdateProfile(profile.copy(gold = profile.gold - goldCost))
                }
                addNotification("🐾 Equipped Companion Equipment: $item!")
                triggerCelebration("🐾 Companion Equipped!\nYour pet is now wearing $item!")
            } else {
                addNotification("❌ Not enough gold to buy $item!")
            }
        }
    }

    // Customization selectors
    fun changeHairstyle(style: String) {
        _currentHairstyle.value = style
        prefs.edit().putString("current_hairstyle", style).apply()
        addNotification("💇 Hair Transmuted: $style")
    }

    fun changeArmorSkin(skin: String) {
        _currentArmorSkin.value = skin
        prefs.edit().putString("current_armor_skin", skin).apply()
        addNotification("🛡️ Armor Skin equipped: $skin")
    }

    fun changePortrait(portrait: String) {
        _currentPortrait.value = portrait
        prefs.edit().putString("current_portrait", portrait).apply()
        addNotification("🌟 Epic Character Portrait updated: $portrait")
    }

    // Dynamic Weather Shrine praying
    fun prayForWeatherChange() {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            if (profile.gold >= 10) {
                val patterns = listOf(
                    "Sunny ☀️" to "Task rewards increased by +15%!",
                    "Thunderstorm ⚡" to "Positive tasks deal +1.5x damage to the active boss!",
                    "Blizzard ❄️" to "Feeding/training companion pets is 30% cheaper!",
                    "Acid Rain 🌧️" to "Incurs high difficulty, gain double progress on random events!",
                    "Cosmic Aurora 🌌" to "Permanent +15% Experience bonuses on everything completed!",
                    "Eclipse 🌑" to "Mythic Premium Loot Box drops items with double rate!"
                )
                val chosen = patterns.random()
                _currentWeather.value = chosen.first
                prefs.edit().putString("current_weather", chosen.first).apply()
                repository.insertOrUpdateProfile(profile.copy(gold = profile.gold - 10))
                
                triggerCelebration("🌌 WEATHER MIRACLE!\nSummoned: ${chosen.first}!")
                addNotification("🔮 Prayed at Shrine (-10 Gold). Weather is now ${chosen.first}. Effect: ${chosen.second}")
            } else {
                addNotification("❌ Praying at the Weather Shrine requires 10 Gold!")
            }
        }
    }

    // Purchase premium Mythic Chests
    fun buyMythicChestWithGold() {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            val costVal = if (_currentWeather.value == "Eclipse 🌑") 200 else 250
            if (profile.gold >= costVal) {
                val nextGold = profile.gold - costVal
                _mythicChests.value += 1
                prefs.edit().putInt("chests_mythic", _mythicChests.value).apply()
                repository.insertOrUpdateProfile(profile.copy(gold = nextGold))
                addNotification("💎 Purchased a premium Mythic Loot Box for $costVal Gold!")
            } else {
                addNotification("❌ Insufficient gold! Mythic Chest costs $costVal Gold.")
            }
        }
    }

    fun buyMythicChestWithGems() {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            if (profile.gems >= 15) {
                val nextGems = profile.gems - 15
                _mythicChests.value += 1
                prefs.edit().putInt("chests_mythic", _mythicChests.value).apply()
                repository.insertOrUpdateProfile(profile.copy(gems = nextGems))
                addNotification("💎 Purchased premium Mythic Loot Box for 15 Gems!")
            } else {
                addNotification("❌ Insufficient Gems! Premium Loot Box costs 15 Gems.")
            }
        }
    }

    // Open premium Mythic Loot Boxes
    fun openMythicChest() {
        viewModelScope.launch {
            if (_mythicChests.value <= 0) {
                addNotification("❌ No premium Mythic Loot Boxes available! Buy one from the shop.")
                return@launch
            }
            _mythicChests.value -= 1
            prefs.edit().putInt("chests_mythic", _mythicChests.value).apply()

            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            val rewardGold = (450..950).random()
            val rewardXp = (350..750).random()
            val gemsBonus = (6..18).random()
            
            val mythicDrops = listOf(
                "Excalibur Eternal 🗡️",
                "Grand Archmage Scepter 🪄",
                "Aegis Divine Bulwark 🛡️",
                "Overlord Dragon Ring 💍",
                "Chronos Infinity Hourglass ⏳"
            )
            val droppedItem = mythicDrops.random()
            
            var curInv = profile.inventory
            if (!curInv.contains(droppedItem)) {
                curInv = if (curInv.isBlank()) droppedItem else "$curInv,$droppedItem"
            }

            var currentXp = profile.xp + rewardXp
            var currentLevel = profile.level
            var tp = profile.talentPoints
            var threshold = currentLevel * 100
            var leveledUp = false

            while (currentXp >= threshold) {
                currentXp -= threshold
                currentLevel += 1
                threshold = currentLevel * 100
                leveledUp = true
                tp += 1
            }

            val updated = profile.copy(
                gold = profile.gold + rewardGold,
                gems = profile.gems + gemsBonus,
                xp = currentXp,
                level = currentLevel,
                talentPoints = tp,
                inventory = curInv
            )
            repository.insertOrUpdateProfile(updated)

            val textResult = "🎁 MYTHIC LOOT BOX OPENED!\nReceived: 🪙 $rewardGold Gold, 💎 $gemsBonus Gems, ✨ $rewardXp XP, and LEGENDARY DROPPED: '$droppedItem'!"
            _chestOpenResult.value = textResult
            addNotification(textResult)
            triggerCelebration("🔥 MYTHIC LEGENDARY DROP!\nObtained: $droppedItem!")

            delay(4000)
            _chestOpenResult.value = null
        }
    }

    // Offline / RPG-themed notifications simulation
    fun triggerSimulatedOfflineNotification() {
        viewModelScope.launch {
            val mockNotifications = listOf(
                "⚔️ Solis Alert: Bad habits are gathering! Log positive routines to defend!",
                "🌪️ Weather Spell Alert: Blizzard ❄️ is settling in Solis! Train companion pets at 30% discount!",
                "🐾 Companion is lonely! Open LifeQuest to Train & Feed treats!",
                "🎃 Event Raid: Spooky Nightmare Special Quest is now active in Rogue's Refuge!",
                "✨ Cosmic Mystery: Space Aurora weather has blessed the realm. Enjoy +15% XP boosts!"
            )
            val selectedMsg = mockNotifications.random()
            
            val currentLog = _simulatedNotificationsLog.value.toMutableList()
            currentLog.add(0, selectedMsg)
            _simulatedNotificationsLog.value = currentLog
            
            triggerCelebration("🔔 OFFLINE RPG LOG MESSAGE!\n$selectedMsg")
            addNotification("📱 RPG notification simulated: $selectedMsg")
        }
    }

    // Bad Habit Spawn Boss trigger
    fun spawnBadHabitBoss(habit: Habit) {
        viewModelScope.launch {
            val currentBoss = repository.activeBossDao.getActiveBossSync()
            val level = currentBoss?.level ?: 1
            val bossNames = listOf(
                "Phantasm of ${habit.title} 👻",
                "Gorgon of ${habit.title} 🐍",
                "Procrastination Golem of ${habit.title} 🗿",
                "Doom Demon [Habit: ${habit.title}] 👹",
                "Sugar Glutton Specter [${habit.title}] 🍩"
            )
            val selectedName = bossNames.random()
            
            val badHabitBoss = ActiveBoss(
                id = 1,
                name = selectedName,
                maxHp = 100 + level * 60,
                currentHp = 100 + level * 60,
                level = level,
                rewardGold = 90 + level * 20,
                rewardXp = 110 + level * 25,
                active = true
            )
            repository.insertBoss(badHabitBoss)
            triggerCelebration("🔥 HABIT MONSTER SUMMONED!\n⚠️ Defeat ${selectedName}!")
            addNotification("⚠️ Spawning Boss Habit Monster: ${selectedName}! Cleanse it by completing positive tasks.")
        }
    }

    // Level Up Overlay HUD triggers
    private val _levelUpEvent = MutableStateFlow<Int?>(null)
    val levelUpEvent: StateFlow<Int?> = _levelUpEvent.asStateFlow()

    // Floating combat-text-style XP notifier state
    private val _xpGainEvent = MutableStateFlow<Int?>(null)
    val xpGainEvent: StateFlow<Int?> = _xpGainEvent.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = QuestRepository(
            database.questDao(),
            database.playerProfileDao(),
            database.habitDao(),
            database.goalDao(),
            database.activeBossDao(),
            database.challengeDao()
        )
        
        allQuests = repository.allQuests.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        allHabits = repository.allHabits.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allGoals = repository.allGoals.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        activeBoss = repository.activeBoss.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        allBosses = repository.allBosses.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allChallenges = repository.allChallenges.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Expose player profile, initializing with default values if empty
        playerProfile = repository.playerProfile
            .map { profile ->
                profile ?: PlayerProfile(
                    id = 1,
                    heroName = "قهرمان",
                    level = 1,
                    xp = 0,
                    streak = 0,
                    lastCompletionTime = 0L,
                    strength = 10,
                    discipline = 10,
                    intelligence = 10,
                    vitality = 10,
                    gold = 100,
                    characterClass = "Warrior",
                    weapon = "خنجر تازه‌کار",
                    armor = "جامه پارچه‌ای",
                    ring = "None",
                    pet = "None"
                ).also { defaultProfile ->
                    viewModelScope.launch {
                        repository.insertOrUpdateProfile(defaultProfile)
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = PlayerProfile()
            )

        // Proactively seed initial database content if empty
        viewModelScope.launch {
            delay(1000)
            
            // Seed active boss if empty
            val boss = repository.activeBossDao.getActiveBossSync()
            if (boss == null) {
                repository.insertBoss(ActiveBoss(id = 1, name = "هیولای تنبلی 🦥", maxHp = 200, currentHp = 200, level = 1, rewardGold = 120, rewardXp = 150, active = true))
                repository.insertBoss(ActiveBoss(id = 2, name = "شبح شبکه‌های اجتماعی 💀", maxHp = 400, currentHp = 400, level = 2, rewardGold = 250, rewardXp = 300, active = false))
                addNotification("Boss Battle Ready: Sloth Behemoth is idling in the arena!")
            }

            // Seed challenges
            repository.challengeDao.getAllChallenges().first().let { items ->
                if (items.isEmpty()) {
                    repository.insertChallenge(Challenge(id = 1, title = "مسابقه هفتگی دانشمند پرتوان", requirement = "تکمیل ۳ ماموریت", progress = 0, targetValue = 3, rewardGold = 100, rewardXp = 80, type = "Weekly", completed = false))
                    repository.insertChallenge(Challenge(id = 2, title = "استاد ماهیانه عادات و نظم", requirement = "تیک زدن عادت‌ها ۱۵ مرتبه", progress = 0, targetValue = 15, rewardGold = 250, rewardXp = 200, type = "Monthly", completed = false))
                    repository.challengeDao.insertChallenge(Challenge(id = 3, title = "رویداد غارت بزرگ قهرمان صعود", requirement = "رسیدن به سطح ۵", progress = 1, targetValue = 5, rewardGold = 500, rewardXp = 400, type = "Seasonal", completed = false))
                    repository.challengeDao.insertChallenge(Challenge(id = 4, title = "استاد کارهای روزانه", requirement = "تکمیل ۱ گام هدف", progress = 0, targetValue = 1, rewardGold = 50, rewardXp = 45, type = "Daily", completed = false))
                    repository.challengeDao.insertChallenge(Challenge(id = 5, title = "غارت حماسی جهان", requirement = "نابودی غول فعال", progress = 0, targetValue = 1, rewardGold = 600, rewardXp = 500, type = "Event", completed = false))
                }
            }

            // RETENTION: Check for returning user bonuses (no activity in >= 12 hours)
            val profile = repository.getPlayerProfileSync()
            if (profile != null) {
                val now = System.currentTimeMillis()
                val profileLastActive = profile.lastActiveTime
                if (profileLastActive > 0L && (now - profileLastActive >= 12 * 60 * 60 * 1000L)) {
                    _showReturningBonus.value = true
                    val bonusGold = 150
                    val bonusGems = 15
                    val updated = profile.copy(
                        gold = profile.gold + bonusGold,
                        gems = profile.gems + bonusGems,
                        lastActiveTime = now
                    )
                    repository.insertOrUpdateProfile(updated)
                    addNotification("👑 WELCOME BACK, LEGENDARY HERO! Returning reward granted: +150 Gold & +15 Gems!")
                } else {
                    repository.insertOrUpdateProfile(profile.copy(lastActiveTime = now))
                }
            }

            // Simulated Flash offers offline countdown ticking
            viewModelScope.launch {
                var totalMin = 104
                while (true) {
                    delay(30 * 1000)
                    totalMin = if (totalMin <= 1) 120 else totalMin - 1
                    val h = totalMin / 60
                    val m = totalMin % 60
                    _flashOfferTimeLeft.value = "${h}h ${m}m"
                }
            }
        }
    }

    private fun translateToPersian(msg: String): String {
        var translated = msg
        if (translated.contains("Boss Battle Ready: Sloth Behemoth is idling in the arena!")) {
            return "نبر با غول آماده است: هیولای تنبلی 🦥 در میدان مبارزه منتظر است!"
        }
        if (translated.contains("New quest forged:")) {
            translated = translated
                .replace("New quest forged:", "ماموریت جدیدی خلق شد:")
                .replace("has been added.", "به لیست اضافه شد.")
                .replace("(Easy)", "(آسان)")
                .replace("(Medium)", "(متوسط)")
                .replace("(Hard)", "(سخت)")
        }
        if (translated.contains("Quest completed successfully!")) {
            translated = translated
                .replace("Quest completed successfully!", "ماموریت با موفقیت انجام شد!")
                .replace("XP", "تجربه")
                .replace("Gold", "سکه طلا")
        }
        if (translated.contains("Quest discarded into oblivion.")) {
            return "ماموریت به وادی فراموشی سپرده شد."
        }
        if (translated.contains("Habit formed:")) {
            translated = translated.replace("Habit formed:", "عادت جدید شکل گرفت:")
        }
        if (translated.contains("Positive habit triggered")) {
            translated = translated
                .replace("Positive habit triggered", "عادت مثبت تیک خورد")
                .replace("gold", "سکه طلا")
                .replace("XP", "تجربه")
        }
        if (translated.contains("Negative habit penalty incurred")) {
            translated = translated
                .replace("Negative habit penalty incurred", "جریمه عادت منفی اعمال شد")
                .replace("XP", "تجربه")
                .replace("Gold", "سکه طلا")
        }
        if (translated.contains("Long term Goal added:")) {
            translated = translated.replace("Long term Goal added:", "هدف طولانی‌مدت افزوده شد:")
        }
        if (translated.contains("EPIC GOAL COMPLETE:")) {
            translated = translated
                .replace("EPIC GOAL COMPLETE:", "🏆 هدف حماسی تکمیل شد:")
                .replace("Gold", "سکه طلا")
                .replace("XP Earned!", "تجربه کسب شد!")
        }
        if (translated.contains("Goal '") && translated.contains("updated")) {
            translated = translated.replace("updated", "به‌روزرسانی شد")
        }
        if (translated.contains("BOSS SLAIN!")) {
            translated = translated
                .replace("BOSS SLAIN!", "غول شکست خورد!")
                .replace("Slayed '", "مغلوب کردن '")
                .replace("'! Claimed", "'! کسب")
                .replace("gold", "سکه طلا")
                .replace("XP!", "تجربه!")
        }
        if (translated.contains("Slash! Dealt")) {
            translated = translated
                .replace("Slash! Dealt", "ضربه! وارد شدن")
                .replace("damage to Active Boss.", "واحد آسیب به غول فعال.")
        }
        if (translated.contains("Arcane Skill Unlocked!")) {
            translated = translated.replace("Arcane Skill Unlocked! Spend talent points correctly:", "مهارت مرموز آزاد شد! امتیاز استعداد را به درستی خرج کنید:")
        }
        if (translated.contains("Summoned new companion:")) {
            translated = translated
                .replace("Summoned new companion:", "همراه جدید احضار شد:")
                .replace("Baby Phoenix", "ققنوس کوچک")
        }
        if (translated.contains("Guild loyalty contribution done.")) {
            return "وفاداری به صنف ثبت شد. حضور مستمر +۲۵ طلا و +۱۵ امتیاز وفاداری دریافت شد!"
        }
        if (translated.contains("Restore complete: Journey files successfully synchronized!")) {
            return "بازیابی انجام شد: فایل‌های صعود با موفقیت هماهنگ شدند!"
        }
        if (translated.contains("Purchased shop bounty:")) {
            translated = translated
                .replace("Purchased shop bounty:", "خریداری شد:")
                .replace("Flame Sword", "شمشیر شعله‌ور سوزان")
                .replace("Iron Plate Mail", "زره غول‌آسا فلزی")
                .replace("Ruby Ring", "انگشتر یاقوت سرخ")
                .replace("XP Elixir Booster", "اکسیر افزایش‌دهنده تجربه")
        }
        if (translated.contains("Equipped armament item:")) {
            translated = translated
                .replace("Equipped armament item:", "تجهیز شد:")
                .replace("Flame Sword", "شمشیر شعله‌ور سوزان")
                .replace("Iron Plate Mail", "زره غول‌آسا فلزی")
                .replace("Ruby Ring", "انگشتر یاقوت سرخ")
                .replace("Novice Dagger", "خنجر تازه‌کار")
                .replace("Cloth Tunic", "جامه پارچه‌ای")
        }
        if (translated.contains("Daily reward claimed!")) {
            translated = translated
                .replace("Daily reward claimed!", "پاداش روزانه دریافت شد!")
                .replace("Gold", "طلا")
                .replace("Gems", "الماس")
        }
        if (translated.contains("Level Up! You have reached Level")) {
            translated = translated
                .replace("Level Up! You have reached Level", "ارتقاء سطح! شما به سطح")
                .replace("and earned", "رسیدید و")
                .replace("Talent Point.", "امتیاز استعداد دریافت کردید.")
        }
        if (translated.contains("Weekly progress chest unlocked!")) {
            return "صندوق پیشرفت هفتگی باز شد! دریافت: +۲۵0 طلا، +۲۰ الماس و +۱ صندوق طلا!"
        }
        if (translated.contains("MONTHLY OVERLORD TREASURE UNLOCKED!")) {
            return "گنج ماهیانه پادخواه آزاد شد! دریافت: +۶00 طلا، +۵0 الماس و +۱ صندوق اسطوره‌ای!"
        }
        if (translated.contains("WELCOME BACK, LEGENDARY HERO!")) {
            return "👑 خوش آمدید قهرمان افسانه‌ای! پاداش بازگشت اعطا شد: +۱۵0 طلا و +۱۵ الماس!"
        }
        if (translated.contains("Health Temple upgraded")) {
            translated = translated
                .replace("Health Temple upgraded to Level", "معبد سلامت به سطح")
                .replace("! Realm immunity and wellness increases.", " ارتقا یافت! مصونیت و تندرستی قلمرو افزایش می‌یابد.")
        }
        if (translated.contains("Fitness Arena upgraded")) {
            translated = translated
                .replace("Fitness Arena upgraded to Level", "میدان تناسب اندام به سطح")
                .replace("! Gladiators train here with pristine iron tools.", " ارتقا یافت! گلادیاتورها با ابزارهای آهنی ورزیده تمرین می‌کنند.")
        }
        if (translated.contains("Learning Academy upgraded")) {
            translated = translated
                .replace("Learning Academy upgraded to Level", "آکادمی یادگیری به سطح")
                .replace("! Library scrolls double in wisdom depth.", " ارتقا یافت! طومارهای دانش عمق بیشتری می‌یابند.")
        }
        if (translated.contains("Finance Vault upgraded")) {
            translated = translated
                .replace("Finance Vault upgraded to Level", "خزانه مالی به سطح")
                .replace("! Treasures are safe from high-tier rogue plunderers.", " ارتقا یافت! گنجینه‌ها از دستبرد غارتگران در امان خواهند بود.")
        }
        if (translated.contains("Career Citadel upgraded")) {
            translated = translated
                .replace("Career Citadel upgraded to Level", "ارگ شغلی به سطح")
                .replace("! Scribing alliances with international trade hubs.", " ارتقا یافت! پیمان‌های تجاری با هاب‌های بین‌المللی منعقد می‌شود.")
        }
        if (translated.contains("Relationship Garden upgraded")) {
            translated = translated
                .replace("Relationship Garden upgraded to Level", "بوستان روابط به سطح")
                .replace("! Healing blossom fragrances increase local empathy levels.", " ارتقا یافت! رایحه شکوفه‌های شفابخش صمیمیت را تقویت می‌کند.")
        }
        return translated
    }

    // Interactive Notification Queue log entries
    fun addNotification(msg: String) {
        val finalMsg = if (playerProfile.value?.languageCode == "fa") {
            translateToPersian(msg)
        } else {
            msg
        }
        val currentList = _notifications.value.toMutableList()
        currentList.add(0, finalMsg)
        // Cap notifications to latest 15
        if (currentList.size > 15) {
            currentList.removeLast()
        }
        _notifications.value = currentList
    }

    // Add Guild chat congratulations mechanics
    private fun sendGuildChatMessage(sender: String, message: String) {
        val list = _guildChatMessages.value.toMutableList()
        list.add(sender to message)
        if (list.size > 20) {
            list.removeAt(0)
        }
        _guildChatMessages.value = list
    }

    // 1. ADD / DELETE QUESTS
    fun addQuest(title: String, description: String, difficulty: String, category: String) {
        viewModelScope.launch {
            val quest = Quest(
                title = title,
                description = description,
                difficulty = difficulty,
                category = category,
                isCompleted = false
            )
            repository.insertQuest(quest)
            addNotification("New quest forged: '$title' ($difficulty) has been added.")
        }
    }

    fun parseAndAddQuest(rawText: String) {
        if (rawText.isBlank()) return
        val trimmed = rawText.trim()
        var title = trimmed
        var description = "Quick created mission"
        var difficulty = "Medium"
        var category = "Custom"
        val lower = trimmed.lowercase()

        when {
            lower.contains("german") || lower.contains("english") || lower.contains("spanish") ||
            lower.contains("french") || lower.contains("farsi") || lower.contains("language") ||
            lower.contains("deutsch") || lower.contains("englisch") ||
            lower.contains("زبان") || lower.contains("کتاب") || lower.contains("مطالعه") || 
            lower.contains("read") || lower.contains("study") || lower.contains("pages") ||
            lower.contains("صفحه") || lower.contains("درس") || lower.contains("یادگیری") -> {
                category = "Learning"
            }
            lower.contains("workout") || lower.contains("gym") || lower.contains("run") ||
            lower.contains("walk") || lower.contains("exercise") || lower.contains("yoga") ||
            lower.contains("ورزش") || lower.contains("باشگاه") || lower.contains("پیاده") ||
            lower.contains("دویدن") || lower.contains("شنا") || lower.contains("تندرستی") -> {
                category = "Fitness"
            }
            lower.contains("water") || lower.contains("sleep") || lower.contains("meditate") ||
            lower.contains("diet") || lower.contains("vitamin") || lower.contains("آب") ||
            lower.contains("مدیتیشن") || lower.contains("خواب") || lower.contains("سلامت") ||
            lower.contains("تغذیه") -> {
                category = "Health"
            }
            lower.contains("code") || lower.contains("project") || lower.contains("email") ||
            lower.contains("write") || lower.contains("meeting") || lower.contains("work") ||
            lower.contains("کار") || lower.contains("ایمیل") || lower.contains("پروژه") ||
            lower.contains("برنامه‌نویسی") || lower.contains("جلسه") || lower.contains("شغل") -> {
                category = "Work"
            }
        }

        when {
            lower.contains("45") || lower.contains("60") || lower.contains("90") ||
            lower.contains("120") || lower.contains("hour") || lower.contains("ساعت") ||
            lower.contains("سخت") || lower.contains("heavy") || lower.contains("difficult") ||
            lower.contains("دقیقه ۴۵") || lower.contains("دقیقه ۶۰") -> {
                difficulty = "Hard"
            }
            lower.contains("10") || lower.contains("15") || lower.contains("5") ||
            lower.contains("easy") || lower.contains("آسان") || lower.contains("ساده") ||
            lower.contains("دقیقه ۵") || lower.contains("دقیقه ۱۰") || lower.contains("دقیقه ۱۵") -> {
                difficulty = "Easy"
            }
            else -> {
                difficulty = "Medium"
            }
        }

        addQuest(title, description, difficulty, category)
    }

    fun completeQuest(quest: Quest) {
        if (quest.isCompleted) return
        
        viewModelScope.launch {
            // 1. Mark quest completed
            val updatedQuest = quest.copy(isCompleted = true, completedAt = System.currentTimeMillis())
            repository.updateQuest(updatedQuest)
            
            // 2. Fetch current profile
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            
            // 3. Compute base rewards based on difficulty
            val baseReward = when (quest.difficulty) {
                "Easy", "آسان", "Einfach" -> Pair(15, 10)
                "Medium", "متوسط", "Mittel" -> Pair(30, 20)
                "Hard", "سخت", "Schwer" -> Pair(50, 40)
                "Epic", "حماسی", "Epic", "Epos" -> Pair(100, 80)
                else -> Pair(15, 10)
            }
            
            var xpGain = baseReward.first
            var goldGain = baseReward.second
            
            // Apply Character Class progression bonuses
            if (profile.characterClass == "Warrior" && 
                (quest.category.equals("Strength", true) || 
                 quest.category.equals("Health", true) || 
                 quest.category.equals("Fitness", true))) {
                xpGain = (xpGain * 1.15f).toInt()
            }
            if (profile.characterClass == "Mage" && 
                (quest.category.equals("Discipline", true) || 
                 quest.category.equals("Learning", true) || 
                 quest.category.equals("Work", true))) {
                xpGain = (xpGain * 1.15f).toInt()
            }
            if (profile.characterClass == "Rogue") {
                goldGain = (goldGain * 1.15f).toInt()
            }
            
            // Pet equipment bonus Multiplier
            if (profile.pet == "Baby Phoenix") {
                xpGain = (xpGain * 1.15f).toInt()
            }

            // Skill Tree enhancements
            if (profile.unlockedSkills.contains("sage_legacy")) {
                xpGain = (xpGain * 1.20f).toInt() // +20% XP boost
            }
            if (profile.unlockedSkills.contains("gilded_fortune")) {
                goldGain = (goldGain * 1.20f).toInt() // +20% Gold boost
            }

            // Specialization passive bonuses
            if (profile.activeSpecialization == "Paladin") {
                xpGain = (xpGain * 1.20f).toInt()
            }
            if (profile.activeSpecialization == "Berserker" && (quest.category.equals("Strength", true) || quest.category.equals("Fitness", true))) {
                goldGain = (goldGain * 1.25f).toInt()
            }
            if (profile.activeSpecialization == "Assassin") {
                goldGain = (goldGain * 1.25f).toInt()
            }
            if (profile.activeSpecialization == "Savant") {
                xpGain = (xpGain * 1.20f).toInt()
            }
            if (profile.activeSpecialization == "Battlemage") {
                goldGain = (goldGain * 1.20f).toInt()
            }

            // XP/Gold booster scrolls multiplier active
            if (profile.boosterMultiplierLeft > 0) {
                xpGain *= 2
                goldGain *= 2
            }
            
            // Trigger combat XP pop-up
            _xpGainEvent.value = xpGain
            viewModelScope.launch {
                delay(2000)
                if (_xpGainEvent.value == xpGain) {
                    _xpGainEvent.value = null
                }
            }
            
            // 4. Calculate streak progression
            val now = System.currentTimeMillis()
            val newStreak = calculateNewStreak(profile.lastCompletionTime, now, profile.streak)
            val updatedLongestStreak = if (newStreak > profile.longestStreak) newStreak else profile.longestStreak
            
            // 5. Update attributes depending on category
            val statIncrement = when (quest.difficulty) {
                "Hard", "سخت", "Schwer", "Epic", "حماسی", "Epos" -> 2
                else -> 1
            }
            
            var newStrength = profile.strength
            var newDiscipline = profile.discipline
            var newIntelligence = profile.intelligence
            var newVitality = profile.vitality
            
            val wearWeaponBonus = if (profile.weapon == "Flame Sword") 3 else if (profile.weapon == "Shadow Blade") 2 else 0
            val wearArmorBonus = if (profile.armor == "Iron Plate Mail") 3 else if (profile.armor == "Assassin Leather") 2 else 0
            val wearRingBonus = if (profile.ring == "Ruby Ring") 2 else if (profile.ring == "Sapphire Ring") 1 else 0
            
            when (quest.category) {
                "Strength", "Fitness", "سلامت", "Health" -> {
                    newStrength += statIncrement
                    newVitality += statIncrement
                }
                "Discipline", "Work", "کار", "Personal Growth" -> {
                    newDiscipline += statIncrement
                }
                "Learning", "یادگیری" -> {
                    newIntelligence += statIncrement
                    newDiscipline += statIncrement
                }
                else -> {
                    newIntelligence += statIncrement
                    newVitality += statIncrement
                }
            }
            
            // Companion XP gains
            var companionXp = profile.companionXp + 15
            var companionLevel = profile.companionLevel
            if (companionXp >= 100) {
                companionXp -= 100
                companionLevel += 1
                addNotification("Companion leveled up! ${profile.companionName} is now Level $companionLevel!")
            }

            // 6. Earn XP and level up if threshold hit
            var currentXp = profile.xp + xpGain
            var currentLevel = profile.level
            var threshold = currentLevel * 100
            var leveledUp = false
            var finalTalentPoints = profile.talentPoints
            
            while (currentXp >= threshold) {
                currentXp -= threshold
                currentLevel += 1
                threshold = currentLevel * 100
                leveledUp = true
                finalTalentPoints += 1 // 1 Talent Point per Level Up
            }
            
            var hist = profile.levelHistory
            if (leveledUp) {
                _levelUpEvent.value = currentLevel
                hist = "$hist; Level $currentLevel reached"
                addNotification("Level Up! You have reached Level $currentLevel and earned 1 Talent Point.")
            }
            
            // Update Challenges increments (Weekly challenge count)
            incrementChallengeProgress("Weekly", 1)

            // Deal Damage to current boss
            damageBoss(quest.difficulty)

            // Progress Event Quests matches
            if (quest.category.equals("Discipline", true) || quest.category.equals("Work", true)) {
                progressEventQuest("eq_1")
            }

            // Simulated Online Guild mechanics congrats:
            if (leveledUp) {
                sendGuildChatMessage("Merlin_Prog", "Insane grind! Congrats on reaching Level $currentLevel, ${profile.heroName}!")
            } else {
                val rand = (1..3).random()
                if (rand == 1) {
                    sendGuildChatMessage("Galahad_99", "Awesome complete! Clear that backlog!")
                }
            }

            val completedQuestsAddition = profile.totalQuestsCompleted + 1
            val xpEarnedAddition = profile.totalXpEarned + xpGain
            
            val currentUnlocked = profile.unlockedAchievements.split(",").toMutableList().filter { it.isNotBlank() }
            val newUnlocked = mutableListOf<String>()
            newUnlocked.addAll(currentUnlocked)
            
            if (!newUnlocked.contains("first_quest") && completedQuestsAddition >= 1) {
                newUnlocked.add("first_quest")
            }
            if (!newUnlocked.contains("streak_7") && newStreak >= 7) {
                newUnlocked.add("streak_7")
            }
            if (!newUnlocked.contains("level_10") && currentLevel >= 10) {
                newUnlocked.add("level_10")
            }
            if (!newUnlocked.contains("quests_100") && completedQuestsAddition >= 10) {
                newUnlocked.add("quests_100")
            }
            if (!newUnlocked.contains("gold_hoarder") && (profile.gold + goldGain) >= 200) {
                newUnlocked.add("gold_hoarder")
            }
            
            val updatedProfile = profile.copy(
                level = currentLevel,
                xp = currentXp,
                streak = newStreak,
                lastCompletionTime = now,
                strength = newStrength,
                discipline = newDiscipline,
                intelligence = newIntelligence,
                vitality = newVitality,
                gold = profile.gold + goldGain,
                totalQuestsCompleted = completedQuestsAddition,
                totalXpEarned = xpEarnedAddition,
                longestStreak = updatedLongestStreak,
                unlockedAchievements = newUnlocked.joinToString(","),
                levelHistory = hist,
                talentPoints = finalTalentPoints,
                companionXp = companionXp,
                companionLevel = companionLevel,
                boosterMultiplierLeft = if (profile.boosterMultiplierLeft > 0) profile.boosterMultiplierLeft - 1 else 0
            )
            repository.insertOrUpdateProfile(updatedProfile)
            triggerTaskProgressionRewards(quest.category, quest.difficulty)
        }
    }

    fun deleteQuest(quest: Quest) {
        viewModelScope.launch {
            repository.deleteQuest(quest)
            addNotification("Quest discarded into oblivion.")
        }
    }


    // 2. HABIT TRACKING LOGICS
    fun addHabit(title: String, category: String, difficulty: String) {
        viewModelScope.launch {
            repository.insertHabit(Habit(title = title, category = category, difficulty = difficulty))
            addNotification("Habit formed: '$title'")
        }
    }

    fun tickHabit(habit: Habit, travelPositive: Boolean) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            var currentGold = profile.gold
            var currentXp = profile.xp
            var currentLevel = profile.level
            var totalXps = profile.totalXpEarned
            var talentPoints = profile.talentPoints

            val xpDelta = when (habit.difficulty) {
                "Easy" -> 10
                "Medium" -> 20
                "Hard" -> 35
                else -> 10
            }

            if (travelPositive) {
                // Earn rewards
                currentGold += (xpDelta / 2)
                currentXp += xpDelta
                totalXps += xpDelta
                
                var leveledUp = false
                var threshold = currentLevel * 100
                while (currentXp >= threshold) {
                    currentXp -= threshold
                    currentLevel += 1
                    threshold = currentLevel * 100
                    leveledUp = true
                    talentPoints += 1
                }
                
                if (leveledUp) {
                    _levelUpEvent.value = currentLevel
                    addNotification("Habit complete Level Up! Level $currentLevel reached.")
                }

                val updatedHabit = habit.copy(positiveCount = habit.positiveCount + 1, lastTickedTime = System.currentTimeMillis())
                repository.updateHabit(updatedHabit)
                addNotification("Positive habit triggered (+${xpDelta / 2} gold, +$xpDelta XP)")
                
                // Progress Event Quests
                if (habit.category.equals("Health", true) || habit.category.equals("Fitness", true) || habit.category.equals("Personal Growth", true)) {
                    progressEventQuest("eq_3")
                }

                // Damage boss
                damageBoss(habit.difficulty)

                // Progress challenges
                incrementChallengeProgress("Monthly", 1)
                triggerTaskProgressionRewards(habit.category, habit.difficulty)
            } else {
                // Deduct penalty (health defense) or slight XP reduction
                val penaltyXp = (xpDelta / 2).coerceAtMost(currentXp)
                currentXp -= penaltyXp
                currentGold = (currentGold - 5).coerceAtLeast(0)

                val updatedHabit = habit.copy(negativeCount = habit.negativeCount + 1, lastTickedTime = System.currentTimeMillis())
                repository.updateHabit(updatedHabit)
                addNotification("Negative habit penalty incurred (-$penaltyXp XP, -5 Gold)")

                // Progress Event Quest for summoning habit monster
                progressEventQuest("eq_2")

                // Spawn Bad Habit Boss
                spawnBadHabitBoss(habit)
            }

            val updatedProfile = profile.copy(
                gold = currentGold,
                xp = currentXp,
                level = currentLevel,
                totalXpEarned = totalXps,
                talentPoints = talentPoints
            )
            repository.insertOrUpdateProfile(updatedProfile)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }


    // 3. GOAL TRACKING LOGICS
    fun addGoal(title: String, targetSteps: Int, category: String) {
        viewModelScope.launch {
            repository.insertGoal(Goal(title = title, targetValue = targetSteps, currentValue = 0, category = category, completed = false))
            addNotification("Long term Goal added: '$title'")
        }
    }

    fun incrementGoalStep(goal: Goal) {
        viewModelScope.launch {
            if (goal.completed) return@launch
            triggerTaskProgressionRewards(goal.category, "Medium")
            val nextStep = goal.currentValue + 1
            val isCleared = nextStep >= goal.targetValue
            
            val updated = goal.copy(currentValue = nextStep, completed = isCleared)
            repository.updateGoal(updated)

            if (isCleared) {
                // Giant reward
                val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
                val goldBounty = 100
                val xpBounty = 150
                
                var currentXp = profile.xp + xpBounty
                var currentLevel = profile.level
                var talentPoints = profile.talentPoints
                var threshold = currentLevel * 100
                var leveledUp = false

                while (currentXp >= threshold) {
                    currentXp -= threshold
                    currentLevel += 1
                    threshold = currentLevel * 100
                    leveledUp = true
                    talentPoints += 1
                }

                if (leveledUp) {
                    _levelUpEvent.value = currentLevel
                }

                val updatedProfile = profile.copy(
                    gold = profile.gold + goldBounty,
                    xp = currentXp,
                    level = currentLevel,
                    talentPoints = talentPoints,
                    totalXpEarned = profile.totalXpEarned + xpBounty
                )
                repository.insertOrUpdateProfile(updatedProfile)
                addNotification("🏆 EPIC GOAL COMPLETE: '${goal.title}'! +$goldBounty Gold, +$xpBounty XP Earned!")
                sendGuildChatMessage("Galahad_99", "Incredible focus, ${profile.heroName}! Finished major goal: ${goal.title}")
            } else {
                addNotification("Goal '${goal.title}' updated ($nextStep/${goal.targetValue})")
            }
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }


    // 4. STORY MODE CHAPTER CHECK
    fun getStoryChapter(): String {
        val completed = playerProfile.value.totalQuestsCompleted
        val code = playerProfile.value.languageCode
        if (code == "fa") {
            return when {
                completed < 5 -> "فصل اول: خیزش تنبلی 🦥"
                completed < 12 -> "فصل دوم: سردابه حواس‌پرتی‌ها 💀"
                completed < 20 -> "فصل سوم: جریان اوج آگاهی 🌌"
                else -> "فصل چهارم: افسانه حاکم ذهن‌آگاه 👑"
            }
        } else if (code == "de") {
            return when {
                completed < 5 -> "Kapitel I: Aufstieg des Faultiers 🦥"
                completed < 12 -> "Kapitel II: Die Krypta der Ablenkungen 💀"
                completed < 20 -> "Kapitel III: Spitzen-Flow-Zustand 🌌"
                else -> "Kapitel IV: Legende des achtsamen Herrschers 👑"
            }
        }
        return when {
            completed < 5 -> "Chapter I: Rise of the Sloth 🦥"
            completed < 12 -> "Chapter II: The Crypt of Distractions 💀"
            completed < 20 -> "Chapter III: Peak Flow State 🌌"
            else -> "Chapter IV: Legend of the Mindful Sovereign 👑"
        }
    }

    fun getStoryProgress(): Float {
        val completed = playerProfile.value.totalQuestsCompleted
        return when {
            completed < 5 -> (completed / 5f)
            completed < 12 -> ((completed - 5) / 7f)
            completed < 20 -> ((completed - 12) / 8f)
            else -> 1.0f
        }
    }


    // 5. BOSS BATTLE OPERATIONS
    private suspend fun damageBoss(difficulty: String) {
        val bossInfo = repository.activeBossDao.getActiveBossSync() ?: return
        if (bossInfo.currentHp <= 0) return

        val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
        val dmg = when(difficulty) {
            "Easy" -> 15
            "Medium" -> 30
            "Hard" -> 60
            "Epic" -> 120
            else -> 20
        }
        val mult = if (profile.unlockedSkills.contains("double_damage")) 2 else 1
        val weatherMult = if (_currentWeather.value == "Thunderstorm ⚡") 1.5f else 1.0f
        val finalDmg = (dmg * mult * weatherMult).toInt()

        val nextHp = (bossInfo.currentHp - finalDmg).coerceAtLeast(0)
        if (nextHp == 0) {
            // Slay rewards
            val rawGold = bossInfo.rewardGold
            val rawXp = bossInfo.rewardXp
            
            var currentXp = profile.xp + rawXp
            var currentLevel = profile.level
            var talentPoints = profile.talentPoints
            var threshold = currentLevel * 100
            var leveledUp = false

            while (currentXp >= threshold) {
                currentXp -= threshold
                currentLevel += 1
                threshold = currentLevel * 100
                leveledUp = true
                talentPoints += 1
            }

            if (leveledUp) {
                _levelUpEvent.value = currentLevel
            }

            val updatedProfile = profile.copy(
                gold = profile.gold + rawGold,
                xp = currentXp,
                level = currentLevel,
                talentPoints = talentPoints,
                totalXpEarned = profile.totalXpEarned + rawXp
            )
            repository.insertOrUpdateProfile(updatedProfile)

            // Switch or upscale boss
            val newBoss = bossInfo.copy(
                level = bossInfo.level + 1,
                maxHp = bossInfo.maxHp + 80,
                currentHp = bossInfo.maxHp + 80,
                rewardGold = bossInfo.rewardGold + 50,
                rewardXp = bossInfo.rewardXp + 60
            )
            repository.updateBoss(newBoss)
            addNotification("⚔️ BOSS SLAIN! Slayed '${bossInfo.name}'! Claimed +$rawGold gold, +$rawXp XP!")
            sendGuildChatMessage("Merlin_Prog", "YES! ${profile.heroName} destroyed ${bossInfo.name}! Epic legendary achievements!")
        } else {
            repository.updateBoss(bossInfo.copy(currentHp = nextHp))
            addNotification("Slash! Dealt $finalDmg damage to Active Boss.")
        }
    }


    // 6. SKILL TREE NODES UNLOCK
    fun unlockSkillNode(skillId: String, tpCost: Int) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            if (profile.talentPoints >= tpCost && !profile.unlockedSkills.contains(skillId)) {
                val tpRemaining = profile.talentPoints - tpCost
                val list = profile.unlockedSkills.split(",").toMutableList().filter { it.isNotBlank() }
                val newList = list + skillId
                
                val updated = profile.copy(
                    talentPoints = tpRemaining,
                    unlockedSkills = newList.joinToString(",")
                )
                repository.insertOrUpdateProfile(updated)
                addNotification("Arcane Skill Unlocked! Spend talent points correctly: $skillId")
            }
        }
    }


    // 7. COMPANION SELECTION AND EVOLUTION
    fun selectCompanion(companionName: String, price: Int) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            if (profile.gold >= price && profile.companionName != companionName) {
                val updated = profile.copy(
                    gold = profile.gold - price,
                    companionName = companionName,
                    companionLevel = 1,
                    companionXp = 0
                )
                repository.insertOrUpdateProfile(updated)
                addNotification("Summoned new companion: $companionName!")
            }
        }
    }


    // 8. GUILD ACTIONS
    fun performGuildCheckIn() {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            val rewardGold = 25
            val updated = profile.copy(
                gold = profile.gold + rewardGold,
                guildContribution = profile.guildContribution + 15
            )
            repository.insertOrUpdateProfile(updated)
            addNotification("Guild loyalty contribution done. Check-in +25 Gold, +15 Loyalty points received!")
            sendGuildChatMessage(profile.heroName, "Checked in today for guild quests!")
        }
    }


    // 9. POWERFUL OFFLINE BACKUP AND RESTORE UTILITIES
    fun exportBackupToString(): String {
        val p = playerProfile.value
        val qList = allQuests.value
        val hList = allHabits.value
        val gList = allGoals.value
        
        val sb = StringBuilder()
        sb.append("LifeQuestBackup_V2\n")
        
        // Single Line Profile
        sb.append("PROFILE:")
          .append("${p.heroName};${p.level};${p.xp};${p.streak};${p.gold};")
          .append("${p.characterClass};${p.weapon};${p.armor};${p.ring};${p.pet};")
          .append("${p.unlockedSkills};${p.avatarUrl};${p.companionName};${p.companionLevel};")
          .append("${p.companionXp};${p.isDarkMode};${p.talentPoints};${p.guildName};")
          .append("${p.guildLevel};${p.guildContribution}\n")
        
        // Custom Quests lines
        qList.forEach { q ->
            sb.append("QUEST:")
              .append("${q.title};${q.description};${q.difficulty};")
              .append("${q.category};${q.isCompleted}\n")
        }
        
        // Custom Habits lines
        hList.forEach { h ->
            sb.append("HABIT:")
              .append("${h.title};${h.category};${h.positiveCount};")
              .append("${h.negativeCount};${h.difficulty}\n")
        }
        
        // Custom Goals lines
        gList.forEach { g ->
            sb.append("GOAL:")
              .append("${g.title};${g.targetValue};${g.currentValue};")
              .append("${g.category};${g.completed}\n")
        }
        
        return sb.toString()
    }

    fun importBackupFromString(backupStr: String): Boolean {
        if (backupStr.isBlank() || !backupStr.startsWith("LifeQuestBackup_")) {
            return false
        }
        
        viewModelScope.launch {
            try {
                val lines = backupStr.split("\n")
                val db = AppDatabase.getDatabase(getApplication())
                
                // Clear state for rewrite
                db.questDao().deleteAllQuests()
                db.habitDao().deleteAllHabits()
                db.goalDao().deleteAllGoals()
                
                lines.forEach { line ->
                    if (line.isBlank()) return@forEach
                    
                    if (line.startsWith("PROFILE:")) {
                        val csv = line.substring(8).split(";")
                        if (csv.size >= 16) {
                            val original = repository.getPlayerProfileSync() ?: PlayerProfile()
                            val parsedProfile = original.copy(
                                heroName = csv[0],
                                level = csv[1].toIntOrNull() ?: 1,
                                xp = csv[2].toIntOrNull() ?: 0,
                                streak = csv[3].toIntOrNull() ?: 0,
                                gold = csv[4].toIntOrNull() ?: 100,
                                characterClass = csv[5],
                                weapon = csv[6],
                                armor = csv[7],
                                ring = csv[8],
                                pet = csv[9],
                                unlockedSkills = csv[10],
                                avatarUrl = csv[11],
                                companionName = csv[12],
                                companionLevel = csv[13].toIntOrNull() ?: 1,
                                companionXp = csv[14].toIntOrNull() ?: 0,
                                isDarkMode = csv[15].toBooleanStrictOrNull() ?: true,
                                talentPoints = if (csv.size > 16) (csv[16].toIntOrNull() ?: 0) else 0,
                                guildName = if (csv.size > 17) csv[17] else "",
                                guildLevel = if (csv.size > 18) (csv[18].toIntOrNull() ?: 1) else 1,
                                guildContribution = if (csv.size > 19) (csv[19].toIntOrNull() ?: 0) else 0
                            )
                            repository.insertOrUpdateProfile(parsedProfile)
                        }
                    } else if (line.startsWith("QUEST:")) {
                        val csv = line.substring(6).split(";")
                        if (csv.size >= 5) {
                            repository.insertQuest(
                                Quest(
                                    title = csv[0],
                                    description = csv[1],
                                    difficulty = csv[2],
                                    category = csv[3],
                                    isCompleted = csv[4].toBooleanStrictOrNull() ?: false
                                )
                            )
                        }
                    } else if (line.startsWith("HABIT:")) {
                        val csv = line.substring(6).split(";")
                        if (csv.size >= 5) {
                            repository.insertHabit(
                                Habit(
                                    title = csv[0],
                                    category = csv[1],
                                    positiveCount = csv[2].toIntOrNull() ?: 0,
                                    negativeCount = csv[3].toIntOrNull() ?: 0,
                                    difficulty = csv[4]
                                )
                            )
                        }
                    } else if (line.startsWith("GOAL:")) {
                        val csv = line.substring(5).split(";")
                        if (csv.size >= 5) {
                            repository.insertGoal(
                                Goal(
                                    title = csv[0],
                                    targetValue = csv[1].toIntOrNull() ?: 100,
                                    currentValue = csv[2].toIntOrNull() ?: 0,
                                    category = csv[3],
                                    completed = csv[4].toBooleanStrictOrNull() ?: false
                                )
                            )
                        }
                    }
                }
                addNotification("Restore complete: Journey files successfully synchronized!")
            } catch (e: Exception) {
                // Return failed
                addNotification("Restore failed: Chronicle payload corrupt or incorrect.")
            }
        }
        return true
    }


    // 10. DUAL THEME PREFERENCES TONTROLLER
    fun toggleDarkMode() {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            val state = !profile.isDarkMode
            repository.insertOrUpdateProfile(profile.copy(isDarkMode = state))
            addNotification("Senses aligned: Switched to ${if (state) "Dark Mode" else "Light Mode"}")
        }
    }

    // Swapping profile Avatar selection
    fun changeAvatar(iconName: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            repository.insertOrUpdateProfile(profile.copy(avatarUrl = iconName))
            addNotification("Character layout evolved: Avatar switched to $iconName")
        }
    }


    // CHALLENGE INCREMENTS HELPER
    private suspend fun incrementChallengeProgress(challengeType: String, offset: Int) {
        val challenges = repository.challengeDao.getAllChallenges().first()
        challenges.forEach { challenge ->
            if (challenge.type == challengeType && !challenge.completed) {
                val nextProgress = challenge.progress + offset
                val completed = nextProgress >= challenge.targetValue
                repository.challengeDao.updateChallenge(challenge.copy(progress = nextStepProgress(nextProgress, challenge.targetValue), completed = completed))
                if (completed) {
                    val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
                    val p_up = profile.copy(
                        gold = profile.gold + challenge.rewardGold,
                        xp = profile.xp + challenge.rewardXp
                    )
                    repository.insertOrUpdateProfile(p_up)
                    addNotification("Challenge Conquered! Completed: '${challenge.title}' (+${challenge.rewardGold} Gold, +${challenge.rewardXp} XP)")
                }
            }
        }
    }

    private fun nextStepProgress(proposed: Int, cap: Int): Int {
        return proposed.coerceAtMost(cap)
    }


    // SHOPPING AT Fantasy Shop 
    fun purchaseShopItem(itemName: String, itemCost: Int, itemType: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            if (profile.gold >= itemCost) {
                val updatedGold = profile.gold - itemCost
                
                if (itemType == "Booster") {
                    val rawXpGain = 100
                    var currentXp = profile.xp + rawXpGain
                    var currentLevel = profile.level
                    var threshold = currentLevel * 100
                    var leveledUp = false
                    val xpEarnedAddition = profile.totalXpEarned + rawXpGain
                    var finalTalentPoints = profile.talentPoints

                    while (currentXp >= threshold) {
                        currentXp -= threshold
                        currentLevel += 1
                        threshold = currentLevel * 100
                        leveledUp = true
                        finalTalentPoints += 1
                    }

                    var hist = profile.levelHistory
                    if (leveledUp) {
                        _levelUpEvent.value = currentLevel
                        hist = "$hist; Level $currentLevel reached"
                    }

                    val updatedProfile = profile.copy(
                        gold = updatedGold,
                        xp = currentXp,
                        level = currentLevel,
                        totalXpEarned = xpEarnedAddition,
                        levelHistory = hist,
                        talentPoints = finalTalentPoints
                    )
                    repository.insertOrUpdateProfile(updatedProfile)
                } else {
                    val currentInventory = profile.inventory
                    val newInventory = if (currentInventory.isBlank()) itemName else "$currentInventory,$itemName"
                    
                    val updatedProfile = when (itemType) {
                        "Weapon" -> profile.copy(gold = updatedGold, inventory = newInventory, weapon = itemName)
                        "Armor" -> profile.copy(gold = updatedGold, inventory = newInventory, armor = itemName)
                        "Ring" -> profile.copy(gold = updatedGold, inventory = newInventory, ring = itemName)
                        "Pet" -> profile.copy(gold = updatedGold, inventory = newInventory, pet = itemName)
                        else -> profile.copy(gold = updatedGold, inventory = newInventory)
                    }
                    repository.insertOrUpdateProfile(updatedProfile)
                }
                addNotification("Purchased shop bounty: '$itemName'")
            }
        }
    }

    fun equipItem(itemName: String, itemType: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            val updatedProfile = when (itemType) {
                "Weapon" -> profile.copy(weapon = itemName)
                "Armor" -> profile.copy(armor = itemName)
                "Ring" -> profile.copy(ring = itemName)
                "Pet" -> profile.copy(pet = itemName)
                else -> profile
            }
            repository.insertOrUpdateProfile(updatedProfile)
            addNotification("Equipped armament item: $itemName")
        }
    }

    fun claimDailyReward() {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            val now = System.currentTimeMillis()
            
            if (now - profile.lastLoginRewardTime >= 24 * 60 * 60 * 1000L || profile.lastLoginRewardTime == 0L) {
                val doubleReward = profile.unlockedSkills.contains("phoenix_blessing")
                val goldBonus = if (doubleReward) 100 else 50
                val xpBonus = 30
                
                var currentXp = profile.xp + xpBonus
                var currentLevel = profile.level
                var threshold = currentLevel * 100
                var leveledUp = false
                var finalTalentPoints = profile.talentPoints
                
                while (currentXp >= threshold) {
                    currentXp -= threshold
                    currentLevel += 1
                    threshold = currentLevel * 100
                    leveledUp = true
                    finalTalentPoints += 1
                }
                
                var hist = profile.levelHistory
                if (leveledUp) {
                    _levelUpEvent.value = currentLevel
                    hist = "$hist; Level $currentLevel reached"
                }
                
                val updatedProfile = profile.copy(
                    gold = profile.gold + goldBonus,
                    xp = currentXp,
                    level = currentLevel,
                    totalXpEarned = profile.totalXpEarned + xpBonus,
                    lastLoginRewardTime = now,
                    levelHistory = hist,
                    talentPoints = finalTalentPoints
                )
                repository.insertOrUpdateProfile(updatedProfile)
                addNotification("Claimed bounty! +$goldBonus gold, +$xpBonus experience!")
            }
        }
    }

    fun selectCharacterClass(charClass: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            repository.insertOrUpdateProfile(profile.copy(characterClass = charClass))
            addNotification("Evolved class attributes: Welcomed to the path of the $charClass!")
        }
    }

    fun updateHeroName(name: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            repository.insertOrUpdateProfile(profile.copy(heroName = name))
        }
    }

    fun updateLanguage(langCode: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            repository.insertOrUpdateProfile(profile.copy(languageCode = langCode))
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            val database = AppDatabase.getDatabase(getApplication())
            database.questDao().deleteAllQuests()
            database.habitDao().deleteAllHabits()
            database.goalDao().deleteAllGoals()
            database.challengeDao().deleteAllChallenges()
            
            repository.insertOrUpdateProfile(
                PlayerProfile(
                    id = 1,
                    heroName = "قهرمان",
                    level = 1,
                    xp = 0,
                    streak = 0,
                    lastCompletionTime = 0L,
                    strength = 10,
                    discipline = 10,
                    intelligence = 10,
                    vitality = 10,
                    gold = 100,
                    characterClass = "Warrior",
                    weapon = "خنجر تازه‌کار",
                    armor = "جامه پارچه‌ای",
                    ring = "None",
                    pet = "None",
                    totalQuestsCompleted = 0,
                    totalXpEarned = 0,
                    longestStreak = 0,
                    inventory = "خنجر تازه‌کار,جامه پارچه‌ای",
                    unlockedAchievements = "",
                    lastLoginRewardTime = 0L,
                    languageCode = "fa",
                    levelHistory = "Level 1: Genesis",
                    isDarkMode = true,
                    talentPoints = 0,
                    unlockedSkills = "",
                    avatarUrl = "ic_rpg_hero",
                    companionName = "ققنوس کوچک",
                    companionLevel = 1,
                    companionXp = 0,
                    guildName = "",
                    guildLevel = 1,
                    guildContribution = 0,
                    backupHash = ""
                )
            )

            // Seed active boss
            repository.insertBoss(ActiveBoss(id = 1, name = "هیولای تنبلی 🦥", maxHp = 200, currentHp = 200, level = 1, rewardGold = 120, rewardXp = 150, active = true))
            
            // Seed base challenges
            repository.insertChallenge(Challenge(id = 1, title = "مسابقه هفتگی دانشمند پرتوان", requirement = "تکمیل ۳ ماموریت", progress = 0, targetValue = 3, rewardGold = 100, rewardXp = 80, type = "Weekly", completed = false))
            repository.insertChallenge(Challenge(id = 2, title = "استاد ماهیانه عادات و نظم", requirement = "تیک زدن عادت‌ها ۱۵ مرتبه", progress = 0, targetValue = 15, rewardGold = 250, rewardXp = 200, type = "Monthly", completed = false))
            repository.insertChallenge(Challenge(id = 3, title = "رویداد غارت بزرگ قهرمان صعود", requirement = "رسیدن به سطح ۵", progress = 1, targetValue = 5, rewardGold = 500, rewardXp = 400, type = "Seasonal", completed = false))
            
            addNotification("Adventure Reset: Your cycle back to the starting world line has occurred.")
        }
    }

    fun dismissLevelUpDialog() {
        _levelUpEvent.value = null
    }

    // ==========================================
    // EXPANISVE REAL FANTASY RPG ENGINE DEFINITIONS
    // ==========================================

    // Prestige system
    private val _prestigeRank = MutableStateFlow(prefs.getInt("prestige_rank", 0))
    val prestigeRank: StateFlow<Int> = _prestigeRank.asStateFlow()

    // Chests inventory
    private val _minorChests = MutableStateFlow(prefs.getInt("chests_minor", 2)) // Start with 2 chests for onboarding fun!
    val minorChests: StateFlow<Int> = _minorChests.asStateFlow()

    private val _goldChests = MutableStateFlow(prefs.getInt("chests_gold", 1)) // Start with 1 Gold Chest!
    val goldChests: StateFlow<Int> = _goldChests.asStateFlow()

    private val _legendaryChests = MutableStateFlow(prefs.getInt("chests_legendary", 0))
    val legendaryChests: StateFlow<Int> = _legendaryChests.asStateFlow()

    // Kingdom progression levels and XP
    private val _solisLvl = MutableStateFlow(prefs.getInt("kingdom_solis_lvl", 1))
    val solisLvl: StateFlow<Int> = _solisLvl.asStateFlow()
    private val _solisXp = MutableStateFlow(prefs.getInt("kingdom_solis_xp", 0))
    val solisXp: StateFlow<Int> = _solisXp.asStateFlow()

    private val _sagesLvl = MutableStateFlow(prefs.getInt("kingdom_sages_lvl", 1))
    val sagesLvl: StateFlow<Int> = _sagesLvl.asStateFlow()
    private val _sagesXp = MutableStateFlow(prefs.getInt("kingdom_sages_xp", 0))
    val sagesXp: StateFlow<Int> = _sagesXp.asStateFlow()

    private val _rogueLvl = MutableStateFlow(prefs.getInt("kingdom_rogue_lvl", 1))
    val rogueLvl: StateFlow<Int> = _rogueLvl.asStateFlow()
    private val _rogueXp = MutableStateFlow(prefs.getInt("kingdom_rogue_xp", 0))
    val rogueXp: StateFlow<Int> = _rogueXp.asStateFlow()

    // Story Quest class representation
    data class StoryQuest(
        val id: String,
        val title: String,
        val desc: String,
        val category: String,
        val targetCount: Int,
        val progress: Int,
        val isCompleted: Boolean,
        val rewardGold: Int,
        val rewardXp: Int,
        val rewardItem: String
    )

    private val storyQuestsDeck = listOf(
        StoryQuest("sq_1", "The Solar Beacon's Fuel", "Refuel the ancient Solar Lighthouse in Solis using physical wellness.", "Strength", 2, 0, false, 150, 200, "Aegis of Solis"),
        StoryQuest("sq_2", "Sages' Decrypted Runes", "Translate ancient mental focusing scrolls in Highcliff by learning.", "Learning", 3, 0, false, 250, 300, "Crown of the High Magus"),
        StoryQuest("sq_3", "Infiltrate shadows", "Establish standard habits and tick routines with stealth in Rogue's Refuge.", "Discipline", 4, 0, false, 350, 450, "Rogue Blade of Night"),
        StoryQuest("sq_4", "Cure the Spellweaver Plague", "Deliver crystalline vital energy to purge the Amethyst Spires sickness.", "Health", 3, 0, false, 500, 600, "Elixir of Infinity"),
        StoryQuest("sq_5", "Mindful Sovereign Apotheosis", "Conquer ultimate focus milestones (General category tasks) across the world.", "General", 5, 0, false, 1000, 1200, "Sovereign Crown of the Dawn")
    )

    private val _activeStoryQuest = MutableStateFlow<StoryQuest?>(null)
    val activeStoryQuest: StateFlow<StoryQuest?> = _activeStoryQuest.asStateFlow()

    private val _completedStoryQuestIds = MutableStateFlow<Set<String>>(emptySet())
    val completedStoryQuestIds: StateFlow<Set<String>> = _completedStoryQuestIds.asStateFlow()

    // Dialog state
    private val _activeNpcDialog = MutableStateFlow<Pair<String, List<String>>?>(null) // Name, lines
    val activeNpcDialog: StateFlow<Pair<String, List<String>>?> = _activeNpcDialog.asStateFlow()

    // Active visual region for background atmospheric colors
    private val _selectedRegionIndex = MutableStateFlow(0) // 0=Solis, 1=Highcliff, 2=Rogue, 3=Amethyst Spires
    val selectedRegionIndex: StateFlow<Int> = _selectedRegionIndex.asStateFlow()

    // Leaderboards
    data class LeaderboardEntry(val name: String, val clazz: String, val score: Int, val isUser: Boolean, val prestige: Int = 0)
    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()

    // Loot Animation results
    private val _chestOpenResult = MutableStateFlow<String?>(null)
    val chestOpenResult: StateFlow<String?> = _chestOpenResult.asStateFlow()

    private fun loadStoryState() {
        val activeId = prefs.getString("active_sq_id", "sq_1") ?: "sq_1"
        val activeProg = prefs.getInt("active_sq_prog", 0)
        val activeDone = prefs.getBoolean("active_sq_done", false)
        val completedString = prefs.getString("completed_sq_ids", "") ?: ""
        val completedSet = completedString.split(",").filter { it.isNotBlank() }.toSet()
        _completedStoryQuestIds.value = completedSet

        val sourceQuest = storyQuestsDeck.find { it.id == activeId } ?: storyQuestsDeck.first()
        _activeStoryQuest.value = sourceQuest.copy(
            progress = activeProg,
            isCompleted = activeDone || (completedSet.contains(activeId))
        )
    }

    private fun saveActiveStoryQuest(quest: StoryQuest) {
        prefs.edit()
            .putString("active_sq_id", quest.id)
            .putInt("active_sq_prog", quest.progress)
            .putBoolean("active_sq_done", quest.isCompleted)
            .apply()
    }

    fun selectRegion(idx: Int) {
        _selectedRegionIndex.value = idx
    }

    fun talkToNpc(npcName: String, dialog: List<String>) {
        _activeNpcDialog.value = Pair(npcName, dialog)
    }

    fun dismissNpcDialog() {
        _activeNpcDialog.value = null
    }

    // Story Quest operations
    fun claimStoryQuestReward() {
        val quest = _activeStoryQuest.value ?: return
        if (!quest.isCompleted) return

        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            
            // Add rewards
            val rawGold = quest.rewardGold
            val rawXp = quest.rewardXp
            val item = quest.rewardItem

            // Update inventory
            val curInv = profile.inventory
            val newInv = if (curInv.isBlank()) item else "$curInv,$item"

            var currentXp = profile.xp + rawXp
            var currentLevel = profile.level
            var tp = profile.talentPoints
            var threshold = currentLevel * 100
            var leveledUp = false

            while (currentXp >= threshold) {
                currentXp -= threshold
                currentLevel += 1
                threshold = currentLevel * 100
                leveledUp = true
                tp += 1
            }

            var hist = profile.levelHistory
            if (leveledUp) {
                _levelUpEvent.value = currentLevel
                hist = "$hist; Level $currentLevel reached"
            }

            // Save player profile
            val updated = profile.copy(
                gold = profile.gold + rawGold,
                xp = currentXp,
                level = currentLevel,
                talentPoints = tp,
                inventory = newInv,
                levelHistory = hist
            )
            repository.insertOrUpdateProfile(updated)

            // Mark completed
            val nextCompleted = _completedStoryQuestIds.value.toMutableSet()
            nextCompleted.add(quest.id)
            _completedStoryQuestIds.value = nextCompleted
            prefs.edit().putString("completed_sq_ids", nextCompleted.joinToString(",")).apply()

            addNotification("Story Quest Claimed! Received legendary gear: $item! +$rawGold Gold, +$rawXp XP.")
            sendGuildChatMessage("Merlin_Prog", "Wow, ${profile.heroName} just crafted the legendary '$item'!")

            // Select next Story Quest in the chain!
            val nextQuestIndex = storyQuestsDeck.indexOfFirst { it.id == quest.id } + 1
            if (nextQuestIndex < storyQuestsDeck.size) {
                val nextQuest = storyQuestsDeck[nextQuestIndex]
                _activeStoryQuest.value = nextQuest
                saveActiveStoryQuest(nextQuest)
                addNotification("New Chapter unlocked: '${nextQuest.title}'!")
            } else {
                _activeStoryQuest.value = null
                prefs.edit().remove("active_sq_id").apply()
            }
        }
    }

    // Chest Adding logic
    fun addMinorChest(count: Int) {
        _minorChests.value = (_minorChests.value + count).coerceAtLeast(0)
        prefs.edit().putInt("chests_minor", _minorChests.value).apply()
        addNotification("Loot Drop! Found [Common Chest] 📦!")
    }

    fun addGoldChest(count: Int) {
        _goldChests.value = (_goldChests.value + count).coerceAtLeast(0)
        prefs.edit().putInt("chests_gold", _goldChests.value).apply()
        addNotification("Bounty Uncovered! Received [Kingdom Gold Vault Chest] 🟡!")
    }

    fun addLegendaryChest(count: Int) {
        _legendaryChests.value = (_legendaryChests.value + count).coerceAtLeast(0)
        prefs.edit().putInt("chests_legendary", _legendaryChests.value).apply()
        addNotification("ASTONISHING DROP! Discovered ultra-rare [Mythic Obsidian Chest] 💎!")
    }

    // Chest Loot table opening
    fun openChest(type: String) {
        viewModelScope.launch {
            val count = when (type) {
                "Minor" -> _minorChests.value
                "Gold" -> _goldChests.value
                "Legendary" -> _legendaryChests.value
                else -> 0
            }
            if (count <= 0) return@launch

            // Decrement
            when (type) {
                "Minor" -> {
                    _minorChests.value -= 1
                    prefs.edit().putInt("chests_minor", _minorChests.value).apply()
                }
                "Gold" -> {
                    _goldChests.value -= 1
                    prefs.edit().putInt("chests_gold", _goldChests.value).apply()
                }
                "Legendary" -> {
                    _legendaryChests.value -= 1
                    prefs.edit().putInt("chests_legendary", _legendaryChests.value).apply()
                }
            }

            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            var rewardGold = 0
            var rewardXp = 0
            var droppedItem = "None"
            val rVal = (1..100).random()

            when (type) {
                "Minor" -> {
                    rewardGold = (25..60).random()
                    rewardXp = (15..35).random()
                    droppedItem = when {
                        rVal <= 15 -> "Swift Ring of Focus"
                        rVal <= 30 -> "Apprentice Spell Sphere"
                        else -> "None"
                    }
                }
                "Gold" -> {
                    rewardGold = (120..220).random()
                    rewardXp = (80..150).random()
                    droppedItem = when {
                        rVal <= 25 -> "Aegis of Solis"
                        rVal <= 50 -> "Rogue Blade of Night"
                        rVal <= 75 -> "Vanguard Shield"
                        else -> "None"
                    }
                }
                "Legendary" -> {
                    rewardGold = (400..800).random()
                    rewardXp = (300..600).random()
                    droppedItem = when {
                        rVal <= 35 -> "Crown of the High Magus"
                        rVal <= 70 -> "Sovereign Crown of the Dawn"
                        else -> "Elixir of Infinity"
                    }
                }
            }

            // Apply drops to profile
            var curInv = profile.inventory
            if (droppedItem != "None" && !curInv.contains(droppedItem)) {
                curInv = if (curInv.isBlank()) droppedItem else "$curInv,$droppedItem"
            }

            var currentXp = profile.xp + rewardXp
            var currentLevel = profile.level
            var tp = profile.talentPoints
            var threshold = currentLevel * 100
            var leveledUp = false

            while (currentXp >= threshold) {
                currentXp -= threshold
                currentLevel += 1
                threshold = currentLevel * 100
                leveledUp = true
                tp += 1
            }

            var hist = profile.levelHistory
            if (leveledUp) {
                _levelUpEvent.value = currentLevel
                hist = "$hist; Level $currentLevel reached"
            }

            val updated = profile.copy(
                gold = profile.gold + rewardGold,
                xp = currentXp,
                level = currentLevel,
                talentPoints = tp,
                inventory = curInv,
                levelHistory = hist
            )
            repository.insertOrUpdateProfile(updated)

            val textResult = if (droppedItem != "None") {
                "Opened $type Chest! Received: 🪙 $rewardGold Gold, ✨ $rewardXp XP, and Equipped Cargo item: '$droppedItem'!"
            } else {
                "Opened $type Chest! Received: 🪙 $rewardGold Gold and ✨ $rewardXp XP."
            }
            _chestOpenResult.value = textResult
            addNotification(textResult)

            delay(4000)
            _chestOpenResult.value = null
        }
    }

    // Leaderboards update and opponents point trigger
    fun addUserLeaderboardPoints(points: Int) {
        val nextPoints = prefs.getInt("leaderboard_user_points", 50) + points
        prefs.edit().putInt("leaderboard_user_points", nextPoints).apply()
        updateRankings()
    }

    fun updateRankings() {
        viewModelScope.launch {
            val userPoints = prefs.getInt("leaderboard_user_points", 80)
            val username = playerProfile.value.heroName.ifBlank { "You" }
            val curTime = System.currentTimeMillis()
            val lastUpdate = prefs.getLong("leaderboard_last_update", 0L)

            val hoursElapsed = if (lastUpdate == 0L) 0 else ((curTime - lastUpdate) / (3600000L)).toInt()
            
            var merlin = prefs.getInt("score_merlin", 360)
            var arthur = prefs.getInt("score_arthur", 310)
            var jeanne = prefs.getInt("score_jeanne", 250)
            var elena = prefs.getInt("score_elena", 190)
            var galahad = prefs.getInt("score_galahad", 110)

            if (hoursElapsed > 0) {
                merlin += (5..15).random() * hoursElapsed.coerceAtMost(24)
                arthur += (4..14).random() * hoursElapsed.coerceAtMost(24)
                jeanne += (3..13).random() * hoursElapsed.coerceAtMost(24)
                elena += (3..11).random() * hoursElapsed.coerceAtMost(24)
                galahad += (2..8).random() * hoursElapsed.coerceAtMost(24)

                prefs.edit()
                    .putInt("score_merlin", merlin)
                    .putInt("score_arthur", arthur)
                    .putInt("score_jeanne", jeanne)
                    .putInt("score_elena", elena)
                    .putInt("score_galahad", galahad)
                    .putLong("leaderboard_last_update", curTime)
                    .apply()
            } else if (lastUpdate == 0L) {
                prefs.edit().putLong("leaderboard_last_update", curTime).apply()
            }

            val entries = listOf(
                LeaderboardEntry(username, playerProfile.value.characterClass, userPoints, true, _prestigeRank.value),
                LeaderboardEntry("Merlin_Prog 🧙‍♂️", "Mage", merlin, false, 2),
                LeaderboardEntry("Arthur_Focus 👑", "Warrior", arthur, false, 1),
                LeaderboardEntry("Jeanne_Fit 🛡️", "Warrior", jeanne, false, 0),
                LeaderboardEntry("Elena_Study 📚", "Mage", elena, false, 0),
                LeaderboardEntry("Galahad_99 🗡️", "Rogue", galahad, false, 0)
            ).sortedByDescending { it.score }

            _leaderboard.value = entries
        }
    }

    // Prestige Method
    fun triggerPrestigeReset() {
        val level = playerProfile.value.level
        if (level < 20) {
            addNotification("Sovereignty block: Must achieve player level 20 to Prestige!")
            return
        }

        viewModelScope.launch {
            val oldProfile = repository.getPlayerProfileSync() ?: PlayerProfile()
            val nextPrestige = _prestigeRank.value + 1
            _prestigeRank.value = nextPrestige
            prefs.edit().putInt("prestige_rank", nextPrestige).apply()

            val resettingProfile = oldProfile.copy(
                level = 1,
                xp = 0,
                gold = 150 + (nextPrestige * 50),
                weapon = "خنجر تازه‌کار",
                armor = "جامه پارچه‌ای",
                ring = "None",
                strength = 10,
                discipline = 10,
                intelligence = 10,
                vitality = 10,
                levelHistory = "Prestige Rank $nextPrestige Ascended!"
            )
            repository.insertOrUpdateProfile(resettingProfile)

            _legendaryChests.value += 1
            _goldChests.value += 2
            prefs.edit()
                .putInt("chests_legendary", _legendaryChests.value)
                .putInt("chests_gold", _goldChests.value)
                .apply()

            addNotification("PRESTIGE ASCENSION TRIBUTE! Reset to Level 1, unlocked +${nextPrestige * 25}% permanent boost!")
            sendGuildChatMessage("Merlin_Prog", "All hail ${oldProfile.heroName}, WHO JUST REACHED PRESTIGE TIER $nextPrestige! 👑⭐")
            updateRankings()
        }
    }

    // Unified helper method for completions
    fun triggerTaskProgressionRewards(category: String, difficulty: String) {
        viewModelScope.launch {
            val mult = 1.0f + (_prestigeRank.value * 0.25f)
            val xpGain = (when (difficulty) {
                "Easy", "آسان", "Einfach" -> 20
                "Medium", "متوسط", "Mittel" -> 40
                "Hard", "سخت", "Schwer" -> 80
                "Epic", "حماسی", "Epos", "Epic" -> 160
                else -> 25
            } * mult).toInt()

            val currentProfile = repository.getPlayerProfileSync() ?: PlayerProfile()

            // 1. Kingdom progression
            if (category.equals("Strength", true) || category.equals("Fitness", true) || category.equals("Health", true) || category.equals("سلامت", true)) {
                val nextXp = _solisXp.value + xpGain
                val req = _solisLvl.value * 150
                if (nextXp >= req) {
                     _solisXp.value = nextXp - req
                     _solisLvl.value += 1
                     prefs.edit().putInt("kingdom_solis_lvl", _solisLvl.value).apply()
                     addNotification("Kingdom Solar Flame level is now ${_solisLvl.value}! Sovereign chests received.")
                     _goldChests.value += 1
                     prefs.edit().putInt("chests_gold", _goldChests.value).apply()
                } else {
                     _solisXp.value = nextXp
                }
                prefs.edit().putInt("kingdom_solis_xp", _solisXp.value).apply()
            } else if (category.equals("Learning", true) || category.equals("Academic", true) || category.equals("Intelligence", true) || category.equals("یادگیری", true)) {
                val nextXp = _sagesXp.value + xpGain
                val req = _sagesLvl.value * 150
                if (nextXp >= req) {
                     _sagesXp.value = nextXp - req
                     _sagesLvl.value += 1
                     prefs.edit().putInt("kingdom_sages_lvl", _sagesLvl.value).apply()
                     addNotification("Kingdom Chrono Sages evolved to ${_sagesLvl.value}! Spellwarden loot drop received.")
                     _goldChests.value += 1
                     prefs.edit().putInt("chests_gold", _goldChests.value).apply()
                } else {
                     _sagesXp.value = nextXp
                }
                prefs.edit().putInt("kingdom_sages_xp", _sagesXp.value).apply()
            } else {
                val nextXp = _rogueXp.value + xpGain
                val req = _rogueLvl.value * 150
                if (nextXp >= req) {
                     _rogueXp.value = nextXp - req
                     _rogueLvl.value += 1
                     prefs.edit().putInt("kingdom_rogue_lvl", _rogueLvl.value).apply()
                     addNotification("Stealth Guild levels increased to ${_rogueLvl.value}! Rogue chests earned.")
                     _goldChests.value += 1
                     prefs.edit().putInt("chests_gold", _goldChests.value).apply()
                } else {
                     _rogueXp.value = nextXp
                }
                prefs.edit().putInt("kingdom_rogue_xp", _rogueXp.value).apply()
            }

            // 2. Story Quest progression
            _activeStoryQuest.value?.let { activeQuest ->
                if (!activeQuest.isCompleted && (activeQuest.category.equals("General", true) || activeQuest.category.equals(category, true))) {
                    val nextProgress = activeQuest.progress + 1
                    val isNowDone = nextProgress >= activeQuest.targetCount
                    val updatedQuest = activeQuest.copy(
                        progress = nextProgress.coerceAtMost(activeQuest.targetCount),
                        isCompleted = isNowDone
                    )
                    _activeStoryQuest.value = updatedQuest
                    saveActiveStoryQuest(updatedQuest)
                    
                    if (isNowDone) {
                        addNotification("Story Quest Ready! '${activeQuest.title}' complete! Claim reward now on the World Map.")
                    } else {
                        addNotification("Story Quest progressed: '${activeQuest.title}' ($nextProgress/${activeQuest.targetCount})")
                    }
                }
            }

            // 3. Loot chest drop roll (e.g., 20% minor, 4% legendary)
            val roll = (1..100).random()
            if (roll <= 20) {
                _minorChests.value += 1
                prefs.edit().putInt("chests_minor", _minorChests.value).apply()
                addNotification("Drop Triggered: Discovered a [Common Arena Chest] 📦!")
            } else if (roll >= 97) {
                _legendaryChests.value += 1
                prefs.edit().putInt("chests_legendary", _legendaryChests.value).apply()
                addNotification("INCREDIBLE FIND: Discovered a [Mythic Obsidian Chest] 💎!")
            }

            // 4. Leaderboard user points progression
            val pts = when (difficulty) {
                "Easy", "آسان", "Einfach" -> 15
                "Medium", "متوسط", "Mittel" -> 30
                "Hard", "سخت", "Schwer" -> 60
                "Epic", "حماسی", "Epos", "Epic" -> 120
                else -> 20
            }
            addUserLeaderboardPoints(pts)
        }
    }

    private fun calculateNewStreak(lastCompletionTime: Long, currentTime: Long, currentStreak: Int): Int {
        if (lastCompletionTime == 0L) {
            return 1
        }
        
        val tz = java.util.TimeZone.getDefault()
        val nowOffset = currentTime + tz.getOffset(currentTime)
        val lastOffset = lastCompletionTime + tz.getOffset(lastCompletionTime)
        
        val dayNow = nowOffset / (24 * 60 * 60 * 1000L)
        val dayLast = lastOffset / (24 * 60 * 60 * 1000L)
        
        return when {
            dayNow == dayLast -> currentStreak
            dayNow - dayLast == 1L -> currentStreak + 1
            else -> 1
        }
    }

    // RETENTION: Claim specific day from the 7-day Login Rewards
    fun claimDailyBountyTrack(dayIdx: Int) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            val now = System.currentTimeMillis()
            
            val updatedClaimCount = profile.dailyRewardsClaimProgress + 1
            val (msg, goldG, gemG, minorC, goldC, legendC) = when (dayIdx) {
                1 -> Sextet("Day 1: Base rations", 50, 0, 1, 0, 0)
                2 -> Sextet("Day 2: Gem discovery", 0, 10, 0, 0, 0)
                3 -> Sextet("Day 3: Adventurer kit", 100, 0, 1, 0, 0)
                4 -> Sextet("Day 4: Gilded coin pouch", 150, 0, 0, 1, 0)
                5 -> Sextet("Day 5: Arcane vault fragment", 0, 25, 0, 0, 0)
                6 -> Sextet("Day 6: Royal armory drop", 200, 0, 0, 1, 0)
                7 -> Sextet("Day 7: Mythic Sovereign Crown", 300, 50, 0, 0, 1)
                else -> Sextet("Simulated Bounty", 50, 5, 0, 0, 0)
            }

            _minorChests.value = _minorChests.value + minorC
            _goldChests.value = _goldChests.value + goldC
            _legendaryChests.value = _legendaryChests.value + legendC
            
            prefs.edit()
                .putInt("chests_minor", _minorChests.value)
                .putInt("chests_gold", _goldChests.value)
                .putInt("chests_legendary", _legendaryChests.value)
                .apply()

            val nextProgress = if (updatedClaimCount >= 7) 0 else updatedClaimCount
            val updated = profile.copy(
                gold = profile.gold + goldG,
                gems = profile.gems + gemG,
                dailyRewardsClaimProgress = nextProgress,
                lastLoginRewardTime = now
            )
            repository.insertOrUpdateProfile(updated)
            
            addNotification("Daily reward claimed! +$goldG Gold, +$gemG Gems.")
            triggerCelebration("CONGRATS!\nClaimed $msg")
        }
    }

    // RETENTION: Claim Weekly Progress Chests
    fun claimWeeklyProgressChest() {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            val updated = profile.copy(
                weeklyChestClaimed = true,
                gold = profile.gold + 250,
                gems = profile.gems + 20
            )
            repository.insertOrUpdateProfile(updated)
            _goldChests.value += 1
            prefs.edit().putInt("chests_gold", _goldChests.value).apply()
            
            addNotification("Weekly progress chest unlocked! Got: +250 Gold, +20 Gems, and +1 Gold Chest!")
            triggerCelebration("WEEKLY ADVENTURER BONUS!\nLoaded with gold and loot!")
        }
    }

    // RETENTION: Claim Monthly Progress Chests
    fun claimMonthlyProgressChest() {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            val updated = profile.copy(
                monthlyChestClaimed = true,
                gold = profile.gold + 600,
                gems = profile.gems + 50
            )
            repository.insertOrUpdateProfile(updated)
            _legendaryChests.value += 1
            prefs.edit().putInt("chests_legendary", _legendaryChests.value).apply()
            
            addNotification("MONTHLY OVERLORD TREASURE UNLOCKED! +600 Gold, +50 Gems, +1 Mythic Chest!")
            triggerCelebration("EPIC MONTHLY OVERLORD VAULT!\nSovereign powers attained!")
        }
    }

    // MOTIVATION: Celebration triggering utility
    fun triggerCelebration(message: String) {
        _celebrationMessage.value = message
        _showCelebration.value = true
        viewModelScope.launch {
            delay(3500)
            _showCelebration.value = false
        }
    }

    fun dismissReturningBonus() {
        _showReturningBonus.value = false
    }

    // MOTIVATION: Encourage when missing tasks
    fun postSmartEncouragement(msg: String) {
        _smartEncouragement.value = msg
        viewModelScope.launch {
            delay(6000)
            _smartEncouragement.value = ""
        }
    }

    // RPG PROGRESSION: Choose specialized classes
    fun specializeCharacterClass(specName: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            val updated = profile.copy(activeSpecialization = specName)
            repository.insertOrUpdateProfile(updated)
            addNotification("Character Specialized: You are now a legendary $specName!")
            triggerCelebration("SPECCLASS ASCENDED!\nYou are now a $specName!")
        }
    }

    // ECONOMY: Buy active reward multipliers scroll
    fun buyBoosterScroll(type: String, costGold: Int, costGems: Int) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            if (profile.gold >= costGold && profile.gems >= costGems) {
                val updated = profile.copy(
                    gold = profile.gold - costGold,
                    gems = profile.gems - costGems,
                    boosterMultiplierLeft = profile.boosterMultiplierLeft + 3
                )
                repository.insertOrUpdateProfile(updated)
                addNotification("Purchased $type Multiplier Scroll (Active for next 3 tasks reward XP/Gold x2!)")
            } else {
                addNotification("Insufficient gold/gems to purchase Multiplier Scroll!")
            }
        }
    }

    // THEMATIC: Simulated limited-time flash offer item buy
    fun buyFlashOfferItem(itemName: String, goldCost: Int, gemCost: Int, itemType: String) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            if (profile.gold >= goldCost && profile.gems >= gemCost) {
                val currentInventory = profile.inventory
                val newInventory = if (currentInventory.isBlank()) itemName else "$currentInventory,$itemName"
                val updatedProgress = profile.itemCollectionBook
                val progressAdded = if (updatedProgress.contains(itemName)) updatedProgress else "$updatedProgress,$itemName"

                val updatedProfile = when (itemType) {
                    "Weapon" -> profile.copy(gold = profile.gold - goldCost, gems = profile.gems - gemCost, inventory = newInventory, itemCollectionBook = progressAdded, weapon = itemName)
                    "Armor" -> profile.copy(gold = profile.gold - goldCost, gems = profile.gems - gemCost, inventory = newInventory, itemCollectionBook = progressAdded, armor = itemName)
                    "Ring" -> profile.copy(gold = profile.gold - goldCost, gems = profile.gems - gemCost, inventory = newInventory, itemCollectionBook = progressAdded, ring = itemName)
                    "Pet" -> profile.copy(gold = profile.gold - goldCost, gems = profile.gems - gemCost, inventory = newInventory, itemCollectionBook = progressAdded, pet = itemName)
                    else -> profile.copy(gold = profile.gold - goldCost, gems = profile.gems - gemCost, inventory = newInventory, itemCollectionBook = progressAdded)
                }
                repository.insertOrUpdateProfile(updatedProfile)
                addNotification("Flash Item Acquired: $itemName!")
                triggerCelebration("RARE LIMITED ITEM DROP!\nAcquired $itemName!")
            } else {
                addNotification("Not enough gold or gems for flash offer!")
            }
        }
    }

    // CHALLENGES: Claim generic challenge reward
    fun claimChallengeRewards(challenge: Challenge) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            
            // Apply rewards
            var currentXp = profile.xp + challenge.rewardXp
            var currentLevel = profile.level
            var threshold = currentLevel * 100
            var tp = profile.talentPoints
            var leveledUp = false

            while (currentXp >= threshold) {
                currentXp -= threshold
                currentLevel += 1
                threshold = currentLevel * 100
                leveledUp = true
                tp += 1
            }

            var hist = profile.levelHistory
            if (leveledUp) {
                _levelUpEvent.value = currentLevel
                hist = "$hist; Level $currentLevel reached"
            }

            // Grant gems too based on challenge type
            val extraGems = when(challenge.type) {
                "Daily" -> 2
                "Weekly" -> 8
                "Monthly" -> 20
                "Seasonal" -> 35
                "Event" -> 50
                else -> 5
            }

            val updatedProfile = profile.copy(
                gold = profile.gold + challenge.rewardGold,
                gems = profile.gems + extraGems,
                xp = currentXp,
                level = currentLevel,
                talentPoints = tp,
                levelHistory = hist
            )
            repository.insertOrUpdateProfile(updatedProfile)

            // Mark challenge completed in DB
            val updatedChallenge = challenge.copy(completed = true, progress = challenge.targetValue)
            repository.challengeDao.insertChallenge(updatedChallenge) // onConflict = REPLACE

            addNotification("Challenge Unlocked: '${challenge.title}'! Received 🪙 ${challenge.rewardGold} Gold, ✨ ${challenge.rewardXp} XP, 💎 $extraGems Gems!")
            triggerCelebration("CHALLENGE COMPLETE!\nReceived +$extraGems Gems!")
        }
    }

    // UX: Save onboarding completed status
    fun completeOnboarding() {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            repository.insertOrUpdateProfile(profile.copy(onboardingCompleted = true))
        }
    }

    // UX: Toggle extra tutorial guide steps
    fun advanceTutorial(step: Int) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            repository.insertOrUpdateProfile(profile.copy(extraTutorialProgress = step))
        }
    }

    // ==========================================
    // AI STATE FLOWS AND GEMINI RPG CONTROLLERS
    // ==========================================
    private val _aiGeneratedQuests = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val aiGeneratedQuests: StateFlow<List<Pair<String, String>>> = _aiGeneratedQuests.asStateFlow()

    private val _isGeneratingQuests = MutableStateFlow(false)
    val isGeneratingQuests: StateFlow<Boolean> = _isGeneratingQuests.asStateFlow()

    private val _aiCoachAdvice = MutableStateFlow("")
    val aiCoachAdvice: StateFlow<String> = _aiCoachAdvice.asStateFlow()

    private val _isConsultingCoach = MutableStateFlow(false)
    val isConsultingCoach: StateFlow<Boolean> = _isConsultingCoach.asStateFlow()

    private val _aiDifficultyRecommendation = MutableStateFlow("")
    val aiDifficultyRecommendation: StateFlow<String> = _aiDifficultyRecommendation.asStateFlow()

    private val _isAnalyzingDifficulty = MutableStateFlow(false)
    val isAnalyzingDifficulty: StateFlow<Boolean> = _isAnalyzingDifficulty.asStateFlow()

    fun generateAIQuests(category: String, difficulty: String, isFarsi: Boolean) {
        viewModelScope.launch {
            _isGeneratingQuests.value = true
            _aiGeneratedQuests.value = emptyList()
            try {
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    delay(1000)
                    _aiGeneratedQuests.value = if (isFarsi) {
                        listOf(
                            "آسانسور ذهن 🧠" to "مدت زمان ۱۰ دقیقه مراقبه کامل و تنظیم برنامه تنفس عمیق در آغاز روز.",
                            "اکسیر تندرستی 🍏" to "نوشیدن مکرر آب سالم و انجام حرکات کششی سبک در فواصل منظم کاری.",
                            "کتیبه نقشه گنج 📓" to "لیست کردن اهداف بلند مدت ماهانه پیرامون پیشرفت فردی و مهارت‌آموزی جدید."
                        )
                    } else {
                        listOf(
                            "Clarity of the Mind Temple" to "Practice deep breathing and silent meditation for 10 minutes to clear mental noise.",
                            "Nourishment of the Elves" to "Consume a freshly made organic healthy meal and drink two cups of green tea.",
                            "Treasury Ledger Scroll" to "Document and log your micro-expenses for today in a financial tracking application."
                        )
                    }
                    _isGeneratingQuests.value = false
                    return@launch
                }

                val prompt = """
                    You are the Legendary AI Oracle of the LifeQuest RPG.
                    Generate exactly 3 personalized, fantasy-themed quest recommendations for the life category "$category" and difficulty level "$difficulty".
                    Let each quest title and detailed description represent real-life self-improvement or productivity tasks modified to sound like legendary RPG quests.
                    Language: ${if (isFarsi) "Farsi (Persian)" else "English"}.
                    Respond ONLY with the 3 quests in this exact format, with no conversational introduction or wrap-up:
                    Q1: [FANTASY TITLE]
                    D1: [SPECIFIC REAL LIFE ACTION REQUIRED TO COMPLETE THE QUEST]
                    Q2: [FANTASY TITLE]
                    D2: [SPECIFIC REAL LIFE ACTION REQUIRED]
                    Q3: [FANTASY TITLE]
                    D3: [SPECIFIC REAL LIFE ACTION REQUIRED]
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    generationConfig = GenerationConfig(temperature = 0.7f)
                )

                val response = GeminiClient.service.generateContent(apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                
                if (responseText.isNotBlank()) {
                    val list = mutableListOf<Pair<String, String>>()
                    var currentTitle = ""
                    responseText.split("\n").forEach { line ->
                        val trimmed = line.trim()
                        if (trimmed.startsWith("Q1:") || trimmed.startsWith("Q2:") || trimmed.startsWith("Q3:") || trimmed.startsWith("Quest 1:") || trimmed.startsWith("Quest 2:") || trimmed.startsWith("Quest 3:")) {
                            currentTitle = trimmed.substringAfter(":").trim()
                        } else if (trimmed.startsWith("D1:") || trimmed.startsWith("D2:") || trimmed.startsWith("D3:") || trimmed.startsWith("Desc 1:") || trimmed.startsWith("Desc 2:") || trimmed.startsWith("Desc 3:")) {
                            val descText = trimmed.substringAfter(":").trim()
                            if (currentTitle.isNotBlank() && descText.isNotBlank()) {
                                list.add(currentTitle to descText)
                                currentTitle = ""
                            }
                        }
                    }
                    if (list.size >= 1) {
                        _aiGeneratedQuests.value = list
                    } else {
                        // Dynamic backup parser lines
                        val lines = responseText.split("\n").filter { it.isNotBlank() }
                        val backupList = mutableListOf<Pair<String, String>>()
                        var idx = 0
                        while (idx < lines.size - 1) {
                            backupList.add(lines[idx].substringAfter(":").trim() to lines[idx+1].substringAfter(":").trim())
                            idx += 2
                        }
                        if (backupList.isNotEmpty()) {
                            _aiGeneratedQuests.value = backupList
                        } else {
                            throw Exception("Format unparseable")
                        }
                    }
                } else {
                    throw Exception("Empty response")
                }
            } catch (e: Exception) {
                _aiGeneratedQuests.value = if (isFarsi) {
                    listOf(
                        "آسانسور ذهن 🧠" to "مراقبه عمیق به مدت ۱۰ دقیقه جهت مهار افکار ناآرام در ابتدای روز.",
                        "کسب دانش کیمیاگری" to "خواندن حداقل ۱۰ صفحه از کتب حوزه تخصصی و ثبت برداشتی خلاق.",
                        "قلمرو ثروت و دارایی 🪙" to "محاسبه دقیق دخل و خرج هفتگی خود و تراز بودجه‌بندی معقول."
                    )
                } else {
                    listOf(
                        "Clarity of Mind Temple" to "Practice deep breathing and silent meditation for 10 minutes to clear mental noise.",
                        "Alchemist Study Ritual" to "Read at least 10 pages of any educational resources focusing on career growth.",
                        "Treasury Vault Balance" to "Compute your weekly micro-expenses and balance current passive cash savings."
                    )
                }
            } finally {
                _isGeneratingQuests.value = false
            }
        }
    }

    fun consultAIHabitCoach(habitDetails: String, isFarsi: Boolean) {
        viewModelScope.launch {
            _isConsultingCoach.value = true
            _aiCoachAdvice.value = ""
            try {
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    delay(1200)
                    _aiCoachAdvice.value = if (isFarsi) {
                        "با درود، قهرمان پادشاهی عادات! کلید اصلی پایداری عادات، قانون ۲ دقیقه است. رفتار بزرگ خود را به فعالیت چابک مینیاتوری تبدیل کنید مثلاً مطالعه را با خواندن ۱ صفحه کتاب آغاز نمایید تا انگیزه شما قفل نشود."
                    } else {
                        "Greetings, Routine Pathfinder! To build robust routines, implement the '2-Minute Rule'. Scale down any titanic routine to an incredibly easy mini-trigger (e.g., instead of 1 hour workout, pack gym gear) to eliminate psychic friction."
                    }
                    _isConsultingCoach.value = false
                    return@launch
                }

                val prompt = """
                    You are the legendary AI Habit Coach & Master of Routines in the LifeQuest RPG.
                    The hero is requesting advice, suggestions, or analysis regarding their current daily habits: "$habitDetails".
                    Provide an inspiring, highly practical, and tactical RPG-themed coaching advice to improve consistency and build positive habits.
                    Keep your response concise, highly motivational, and actionable.
                    Language: ${if (isFarsi) "Farsi (Persian)" else "English"}.
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    generationConfig = GenerationConfig(temperature = 0.75f)
                )

                val response = GeminiClient.service.generateContent(apiKey, request)
                _aiCoachAdvice.value = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Failed to receive advice."
            } catch (e: Exception) {
                _aiCoachAdvice.value = if (isFarsi) {
                    "ارواح عادات کائنات در حال تلاشند! همیشه مداومت داشته باشید: روزهای مبارزه مداوم است که سطح نهایی ما را تعیین می‌کند."
                } else {
                    "The habit matrix is a bit noisy right now. Strive to stay consistent; tiny regular increments scale into colossal power upgrades."
                }
            } finally {
                _isConsultingCoach.value = false
            }
        }
    }

    fun analyzePersonalDifficulty(completedCount: Int, pendingCount: Int, isFarsi: Boolean) {
        viewModelScope.launch {
            _isAnalyzingDifficulty.value = true
            _aiDifficultyRecommendation.value = ""
            try {
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    delay(1000)
                    _aiDifficultyRecommendation.value = if (isFarsi) {
                        "آمار حماسی شما ارزیابی شد! با توجه به توازن فعلی، پیشنهاد می‌کنم ۳۰ درصد ماموریت‌ها را بر روی درجه متوسط یا سخت بگذارید تا پاداش طلا و تجارب را تا ۱.۵ برابر فزونی ببخشید."
                    } else {
                        "Your performance matrix indicates steady rhythm! We recommend scaling 30% of your future quests into 'Medium' or 'Hard' difficulty. This increases your risk but enhances your total rewards efficiency by 50%!"
                    }
                    _isAnalyzingDifficulty.value = false
                    return@launch
                }

                val prompt = """
                    You are the Game Balance Architect of the LifeQuest RPG.
                    The user has completed $completedCount quests and has $pendingCount pending active quests.
                    Provide a personalized difficulty tuning recommendation based on their performance.
                    Suggest specific adjustments (such as adding or moving to harder quests) with motivational game-design flavor.
                    Language: ${if (isFarsi) "Farsi (Persian)" else "English"}.
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    generationConfig = GenerationConfig(temperature = 0.7f)
                )

                val response = GeminiClient.service.generateContent(apiKey, request)
                _aiDifficultyRecommendation.value = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Failed to analyze difficulty."
            } catch (e: Exception) {
                _aiDifficultyRecommendation.value = if (isFarsi) {
                    "تعادل چالش‌ها در حد خوبی قرار دارد. تلاش کنید هر از گاهی یک ماموریت با سطح سخت انتخاب کرده تا مرزهای ذهن خود را بشکافید!"
                } else {
                    "Your challenge homeostasis is robust. Venture outside your safe zone periodically with a 'Hard' difficulty quest to break cognitive barriers!"
                }
            } finally {
                _isAnalyzingDifficulty.value = false
            }
        }
    }

    // ==========================================
    // PERSISTENT KINGDOM BUILDING SYSTEM STATE
    // ==========================================
    private val _healthTempleLevel = MutableStateFlow(prefs.getInt("kingdom_building_health_temple", 1))
    val healthTempleLevel: StateFlow<Int> = _healthTempleLevel.asStateFlow()

    private val _fitnessArenaLevel = MutableStateFlow(prefs.getInt("kingdom_building_fitness_arena", 1))
    val fitnessArenaLevel: StateFlow<Int> = _fitnessArenaLevel.asStateFlow()

    private val _learningAcademyLevel = MutableStateFlow(prefs.getInt("kingdom_building_learning_academy", 1))
    val learningAcademyLevel: StateFlow<Int> = _learningAcademyLevel.asStateFlow()

    private val _financeVaultLevel = MutableStateFlow(prefs.getInt("kingdom_building_finance_vault", 1))
    val financeVaultLevel: StateFlow<Int> = _financeVaultLevel.asStateFlow()

    private val _careerCitadelLevel = MutableStateFlow(prefs.getInt("kingdom_building_career_citadel", 1))
    val careerCitadelLevel: StateFlow<Int> = _careerCitadelLevel.asStateFlow()

    private val _relationshipGardenLevel = MutableStateFlow(prefs.getInt("kingdom_building_relationship_garden", 1))
    val relationshipGardenLevel: StateFlow<Int> = _relationshipGardenLevel.asStateFlow()

    fun upgradeKingdomBuilding(buildingId: String, costGold: Int, categoryFilter: String, requiredQuestsOfCategory: Int) {
        viewModelScope.launch {
            val profile = repository.getPlayerProfileSync() ?: PlayerProfile()
            if (profile.gold < costGold) {
                addNotification("Cannot upgrade building: Lacking 🪙 $costGold gold chest funds.")
                return@launch
            }

            // Real-life checks on task completions
            val completedQuestsList = repository.allQuests.first()
            val completedCategoryQueryCount = completedQuestsList.count { it.isCompleted && it.category.equals(categoryFilter, ignoreCase = true) }
            
            if (completedCategoryQueryCount < requiredQuestsOfCategory) {
                val errorMsg = "Cannot upgrade structure: Requires completing $requiredQuestsOfCategory real-life '$categoryFilter' quests first! (You have completed only $completedCategoryQueryCount)"
                addNotification(errorMsg)
                triggerCelebration("REQUIREMENTS BLOCKED!")
                return@launch
            }

            // Subtract Gold
            val updatedProfile = profile.copy(gold = profile.gold - costGold)
            repository.insertOrUpdateProfile(updatedProfile)

            // Increment specific building levels
            when (buildingId) {
                "health_temple" -> {
                    val nextLvl = _healthTempleLevel.value + 1
                    prefs.edit().putInt("kingdom_building_health_temple", nextLvl).apply()
                    _healthTempleLevel.value = nextLvl
                    addNotification("👑 Health Temple upgraded to Level $nextLvl! Realm immunity and wellness increases.")
                }
                "fitness_arena" -> {
                    val nextLvl = _fitnessArenaLevel.value + 1
                    prefs.edit().putInt("kingdom_building_fitness_arena", nextLvl).apply()
                    _fitnessArenaLevel.value = nextLvl
                    addNotification("👑 Fitness Arena upgraded to Level $nextLvl! Gladiators train here with pristine iron tools.")
                }
                "learning_academy" -> {
                    val nextLvl = _learningAcademyLevel.value + 1
                    prefs.edit().putInt("kingdom_building_learning_academy", nextLvl).apply()
                    _learningAcademyLevel.value = nextLvl
                    addNotification("👑 Learning Academy upgraded to Level $nextLvl! Library scrolls double in wisdom depth.")
                }
                "finance_vault" -> {
                    val nextLvl = _financeVaultLevel.value + 1
                    prefs.edit().putInt("kingdom_building_finance_vault", nextLvl).apply()
                    _financeVaultLevel.value = nextLvl
                    addNotification("👑 Finance Vault upgraded to Level $nextLvl! Treasures are safe from high-tier rogue plunderers.")
                }
                "career_citadel" -> {
                    val nextLvl = _careerCitadelLevel.value + 1
                    prefs.edit().putInt("kingdom_building_career_citadel", nextLvl).apply()
                    _careerCitadelLevel.value = nextLvl
                    addNotification("👑 Career Citadel upgraded to Level $nextLvl! Scribing alliances with international trade hubs.")
                }
                "relationship_garden" -> {
                    val nextLvl = _relationshipGardenLevel.value + 1
                    prefs.edit().putInt("kingdom_building_relationship_garden", nextLvl).apply()
                    _relationshipGardenLevel.value = nextLvl
                    addNotification("👑 Relationship Garden upgraded to Level $nextLvl! Healing blossom fragrances increase local empathy levels.")
                }
            }
            triggerCelebration("STRUCTURE UPGRADED!\nReal life efforts bear fruit.")
        }
    }

    // Helper data structure
    data class Sextet<out A, out B, out C, out D, out E, out F>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E,
        val sixth: F
    )
}
