package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.DailyMetricsEntity
import com.example.data.local.HabitEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class GeminiRepository(
    private val apiService: GeminiApiService = GeminiApiService.create()
) {
    private val tag = "GeminiRepository"

    suspend fun sendChatMessage(
        userMessage: String,
        metrics: DailyMetricsEntity,
        activeHabits: List<HabitEntity>,
        chatHistorySummary: String = ""
    ): GeminiCoachResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(tag, "Gemini API key is not set. Generating contextual smart response.")
            return@withContext generateLocalSmartResponse(userMessage, metrics)
        }

        val systemPrompt = buildSystemPrompt(metrics, activeHabits)
        val requestJson = buildRequestPayload(systemPrompt, userMessage)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toString().toRequestBody(mediaType)

        try {
            val responseBody = apiService.generateContent(apiKey, requestBody)
            val rawString = responseBody.string()
            parseGeminiResponse(rawString)
        } catch (e: Exception) {
            Log.e(tag, "Error generating AI response", e)
            generateLocalSmartResponse(userMessage, metrics, errorMsg = e.localizedMessage)
        }
    }

    suspend fun generateDailyInsight(
        metrics: DailyMetricsEntity,
        activeHabits: List<HabitEntity>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateLocalInsight(metrics)
        }

        val prompt = """
            Current Health Metrics:
            - Water: ${metrics.waterCurrentMl}/${metrics.waterTargetMl} ml (${(metrics.waterCurrentMl * 100 / metrics.waterTargetMl.coerceAtLeast(1))}%)
            - Sleep: ${metrics.sleepHours}/${metrics.sleepTargetHours} hrs (${(metrics.sleepHours * 100 / metrics.sleepTargetHours.coerceAtLeast(0.1f)).toInt()}%)
            - Steps: ${metrics.stepCurrent}/${metrics.stepTarget} steps (${(metrics.stepCurrent * 100 / metrics.stepTarget.coerceAtLeast(1))}%)
            - Workout: ${metrics.workoutMinutes}/${metrics.workoutTargetMinutes} mins (${(metrics.workoutMinutes * 100 / metrics.workoutTargetMinutes.coerceAtLeast(1))}%)
            - Micro-Habits: ${activeHabits.count { it.completed }}/${activeHabits.size} completed
            
            Give a single concise 1-2 sentence encouraging AI Insight summarizing current progress and the single best next action. Do NOT return JSON, just the plain text insight.
        """.trimIndent()

        val requestJson = buildRequestPayload(
            systemInstruction = "You are a concise, motivating health and habit AI coach. Keep your insight strictly to 1 or 2 high-impact sentences for a mobile banner.",
            userPrompt = prompt
        )
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toString().toRequestBody(mediaType)

        try {
            val responseBody = apiService.generateContent(apiKey, requestBody)
            val parsed = parseGeminiResponse(responseBody.string())
            if (parsed.replyText.isNotBlank()) parsed.replyText else generateLocalInsight(metrics)
        } catch (e: Exception) {
            Log.e(tag, "Error generating AI insight", e)
            generateLocalInsight(metrics)
        }
    }

    private fun buildSystemPrompt(metrics: DailyMetricsEntity, habits: List<HabitEntity>): String {
        val habitSummary = if (habits.isEmpty()) "None currently" else habits.joinToString("; ") {
            "${it.title} (${if (it.completed) "Done" else "Pending"})"
        }
        return """
            You are an empathetic, proactive, and concise AI Health & Habit Coach on mobile.
            You have live access to the user's dashboard metrics for today:
            - Water Intake: ${metrics.waterCurrentMl} / ${metrics.waterTargetMl} ml
            - Sleep: ${metrics.sleepHours} / ${metrics.sleepTargetHours} hrs
            - Step Count: ${metrics.stepCurrent} / ${metrics.stepTarget} steps
            - Workouts: ${metrics.workoutMinutes} / ${metrics.workoutTargetMinutes} mins
            - Micro-Habits: $habitSummary
            - Streak: ${metrics.streakDays} days
            
            Rules:
            1. Keep responses brief, encouraging, and actionable (1-3 sentences total, optimized for mobile screens).
            2. When the user asks for recommendations or status, reference their actual metrics above and give 1-2 immediate next steps.
            3. AUTOMATED GOAL SETTING: Whenever the user expresses a goal or desire (e.g. "I want to run a 5k next month", "help me sleep by 10 PM", "cut down sugar", "drink more water", "meditate daily"), break it down into 1 to 3 bite-sized daily micro-habits.
               You MUST include a JSON block in your reply with format:
               ```json
               {
                 "habits": [
                   {
                     "title": "Actionable micro-habit (e.g. 10-min light jog)",
                     "category": "fitness|hydration|sleep|mindfulness|nutrition|custom",
                     "targetValue": 1,
                     "unit": "times|mins|glasses|pages",
                     "reason": "Why this micro-habit works"
                   }
                 ]
               }
               ```
               Along with a short, motivating message explaining that you've added these micro-habits to their top dashboard!
        """.trimIndent()
    }

    private fun buildRequestPayload(systemInstruction: String, userPrompt: String): JSONObject {
        val root = JSONObject()

        // System Instruction
        val systemObj = JSONObject()
        val sysParts = JSONArray()
        val sysPart = JSONObject()
        sysPart.put("text", systemInstruction)
        sysParts.put(sysPart)
        systemObj.put("parts", sysParts)
        root.put("systemInstruction", systemObj)

        // Contents
        val contentsArr = JSONArray()
        val contentObj = JSONObject()
        val partsArr = JSONArray()
        val partObj = JSONObject()
        partObj.put("text", userPrompt)
        partsArr.put(partObj)
        contentObj.put("parts", partsArr)
        contentsArr.put(contentObj)
        root.put("contents", contentsArr)

        // Generation Config
        val genConfig = JSONObject()
        genConfig.put("temperature", 0.7)
        genConfig.put("topP", 0.95)
        root.put("generationConfig", genConfig)

        return root
    }

    private fun parseGeminiResponse(rawJson: String): GeminiCoachResult {
        return try {
            val root = JSONObject(rawJson)
            val candidates = root.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textBuilder = StringBuilder()

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val p = parts.optJSONObject(i)
                    val t = p?.optString("text", "") ?: ""
                    textBuilder.append(t)
                }
            }

            val fullText = textBuilder.toString()
            val extractedHabits = extractHabitsFromJsonBlock(fullText)
            val cleanReply = cleanTextFromCodeBlocks(fullText)

            GeminiCoachResult(
                replyText = cleanReply.ifBlank { fullText },
                generatedHabits = extractedHabits,
                rawJson = fullText
            )
        } catch (e: Exception) {
            Log.e(tag, "Error parsing Gemini REST response: $rawJson", e)
            GeminiCoachResult(replyText = "I've analyzed your progress! Keep building steady daily momentum.")
        }
    }

    private fun extractHabitsFromJsonBlock(text: String): List<MicroHabitParsed> {
        val habits = mutableListOf<MicroHabitParsed>()
        try {
            val jsonRegex = "```(?:json)?\\s*(\\{[\\s\\S]*?\\})\\s*```".toRegex(RegexOption.IGNORE_CASE)
            val match = jsonRegex.find(text)
            val jsonString = match?.groupValues?.getOrNull(1)
                ?: if (text.trim().startsWith("{") && text.trim().endsWith("}")) text.trim() else null

            if (jsonString != null) {
                val jsonObject = JSONObject(jsonString)
                val habitsArray = jsonObject.optJSONArray("habits")
                if (habitsArray != null) {
                    for (i in 0 until habitsArray.length()) {
                        val hObj = habitsArray.optJSONObject(i) ?: continue
                        val title = hObj.optString("title", "").trim()
                        if (title.isNotBlank()) {
                            val category = hObj.optString("category", "custom").lowercase()
                            val target = hObj.optInt("targetValue", 1)
                            val unit = hObj.optString("unit", "times")
                            val reason = if (hObj.has("reason")) hObj.optString("reason") else null
                            habits.add(
                                MicroHabitParsed(
                                    title = title,
                                    category = category,
                                    targetValue = target,
                                    unit = unit,
                                    reason = reason
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error extracting habit JSON", e)
        }
        return habits
    }

    private fun cleanTextFromCodeBlocks(text: String): String {
        return text.replace("```(?:json)?[\\s\\S]*?```".toRegex(RegexOption.IGNORE_CASE), "")
            .trim()
    }

    private fun generateLocalSmartResponse(
        userMessage: String,
        metrics: DailyMetricsEntity,
        errorMsg: String? = null
    ): GeminiCoachResult {
        val lower = userMessage.lowercase()
        val waterPercent = (metrics.waterCurrentMl * 100 / metrics.waterTargetMl.coerceAtLeast(1))
        val stepPercent = (metrics.stepCurrent * 100 / metrics.stepTarget.coerceAtLeast(1))

        if (lower.contains("5k") || lower.contains("run")) {
            val habits = listOf(
                MicroHabitParsed("15-min interval walk/jog", "fitness", 1, "times", "Gradual cardio foundation"),
                MicroHabitParsed("5-min post-run calf & hamstring stretch", "fitness", 1, "times", "Prevents injury"),
                MicroHabitParsed("Hydrate with 500ml water before running", "hydration", 1, "times", "Optimal muscle endurance")
            )
            return GeminiCoachResult(
                replyText = "Target 5K unlocked! I've created 3 daily micro-habits on your dashboard to ramp up your aerobic base step by step.",
                generatedHabits = habits
            )
        }

        if (lower.contains("sleep") || lower.contains("10 pm") || lower.contains("night")) {
            val habits = listOf(
                MicroHabitParsed("Screens off by 9:30 PM", "sleep", 1, "times", "Reduces melatonin disruption"),
                MicroHabitParsed("5-min calm diaphragmatic breathing", "mindfulness", 1, "times", "Lowers cortisol for deep rest")
            )
            return GeminiCoachResult(
                replyText = "Great bedtime initiative! I've added 2 evening micro-habits to your dashboard to help you wind down smoothly by 10 PM.",
                generatedHabits = habits
            )
        }

        if (lower.contains("water") || lower.contains("hydrat")) {
            val habits = listOf(
                MicroHabitParsed("Drink 250ml glass of water upon waking", "hydration", 1, "times", "Kickstarts metabolism"),
                MicroHabitParsed("Refill water bottle by 2:00 PM", "hydration", 1, "times", "Mid-day hydration checkpoint")
            )
            return GeminiCoachResult(
                replyText = "Hydration booster activated! You are currently at $waterPercent% of your target. I've added 2 checkpoint habits to your dashboard.",
                generatedHabits = habits
            )
        }

        if (lower.contains("status") || lower.contains("analyze") || lower.contains("progress") || lower.contains("recommend")) {
            val tip = if (waterPercent < 70) {
                "Your hydration is at $waterPercent%. Drink 250ml of water now to sustain your energy."
            } else if (stepPercent < 70) {
                "You're at $stepPercent% steps! A short 15-minute walk will easily get you to your 10k goal."
            } else {
                "Excellent consistency today! All primary targets are tracking well. Keep this momentum!"
            }
            return GeminiCoachResult(
                replyText = "Dashboard Analysis: $tip"
            )
        }

        // Generic goal setting fallback
        if (lower.contains("goal") || lower.contains("habit") || lower.contains("start")) {
            val habits = listOf(
                MicroHabitParsed("5-minute morning mindful focus", "mindfulness", 1, "times", "Sets positive daily intention")
            )
            return GeminiCoachResult(
                replyText = "Every big goal begins with a micro-step. I've added a starter micro-habit to your dashboard to get you rolling!",
                generatedHabits = habits
            )
        }

        val defaultReply = if (waterPercent < 80) {
            "You've logged ${metrics.stepCurrent} steps today, but hydration is at $waterPercent%. Drink 200ml of water now to stay balanced!"
        } else {
            "Great work staying on track today! Your ${metrics.streakDays}-day streak is going strong. What goal should we conquer next?"
        }

        return GeminiCoachResult(replyText = defaultReply)
    }

    private fun generateLocalInsight(metrics: DailyMetricsEntity): String {
        val waterPercent = (metrics.waterCurrentMl * 100 / metrics.waterTargetMl.coerceAtLeast(1))
        val stepPercent = (metrics.stepCurrent * 100 / metrics.stepTarget.coerceAtLeast(1))
        val sleepPercent = (metrics.sleepHours * 100 / metrics.sleepTargetHours.coerceAtLeast(0.1f)).toInt()

        return when {
            waterPercent < 60 -> "Hydration is at $waterPercent% today. Drink 300ml of water right now to replenish focus and energy."
            stepPercent >= 100 -> "Incredible job! Step goal crushed at $stepPercent%. Hydrate and take a moment to stretch."
            stepPercent < 60 -> "You're at ${metrics.stepCurrent} steps ($stepPercent%). A 15-minute brisk walk will quickly close the gap."
            sleepPercent < 80 -> "You got ${metrics.sleepHours}h sleep last night. Prioritize light movement and an early wind-down tonight."
            else -> "Solid daily balance! ${metrics.streakDays}-day streak active. Keep up the high energy across all metrics."
        }
    }
}
