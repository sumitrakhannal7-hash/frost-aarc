package com.example.engine

import kotlin.math.floor
import kotlin.math.sqrt

data class LevelInfo(
    val level: Int,
    val title: String,
    val currentLevelXp: Int,
    val nextLevelTargetXp: Int,
    val progress: Float
)

object XpEngine {

    fun calculateLevel(totalXp: Int): Int {
        if (totalXp <= 0) return 1
        return (floor(sqrt(totalXp.toDouble() / 50.0)) + 1).toInt()
    }

    fun getLevelTitle(level: Int): String {
        return when {
            level < 5 -> "Frostbite"
            level < 10 -> "Snowdrift"
            level < 15 -> "Iceforged"
            level < 20 -> "Glacier"
            else -> "Permafrost"
        }
    }

    fun getLevelInfo(totalXp: Int): LevelInfo {
        val level = calculateLevel(totalXp)
        val title = getLevelTitle(level)

        val xpForCurrentLevel = 50 * (level - 1) * (level - 1)
        val xpForNextLevel = 50 * level * level
        val requiredInLevel = xpForNextLevel - xpForCurrentLevel
        val earnedInLevel = (totalXp - xpForCurrentLevel).coerceAtLeast(0)

        val progress = if (requiredInLevel > 0) {
            (earnedInLevel.toFloat() / requiredInLevel.toFloat()).coerceIn(0f, 1f)
        } else 0f

        return LevelInfo(
            level = level,
            title = title,
            currentLevelXp = earnedInLevel,
            nextLevelTargetXp = requiredInLevel,
            progress = progress
        )
    }
}
