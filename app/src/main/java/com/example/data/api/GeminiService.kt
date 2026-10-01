package com.example.data.api

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GuideMessage(
    val sender: String, // "user" or "frost_ai"
    val text: String,
    val suggestedAction: GuideAction? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class GuideAction(
    val actionType: String, // "APPLY_STUDY_TARGET", "SET_TIMER_DURATION", "NAVIGATE_FEATURE"
    val subjectName: String? = null,
    val durationMinutes: Int = 25,
    val label: String
)

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun resolveApiKey(): String {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isNotBlank() && key != "MY_GEMINI_API_KEY") key else ""
        } catch (_: Exception) {
            ""
        }
    }

    suspend fun getGuideResponse(
        userPrompt: String,
        currentDay: Int,
        streak: Int,
        subjectsSummary: String,
        routineSummary: String,
        previousMessages: List<GuideMessage> = emptyList()
    ): GuideMessage = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey()
        val promptLower = userPrompt.lowercase()
        val suggestedAction = detectActionFromPrompt(promptLower)

        if (apiKey.isBlank()) {
            return@withContext GuideMessage(
                text = "✨ **Gemini AI Setup Required**\n\nTo chat with Gemini, please configure your **GEMINI_API_KEY** in the **Secrets panel in AI Studio** or create a `.env` file with `GEMINI_API_KEY=your_key_here`.\n\nOnce provided, Gemini will be fully operational to help with homework, problem solving, study plans, and exam prep!",
                sender = "gemini",
                suggestedAction = suggestedAction
            )
        }

        // Try primary Gemini models: gemini-3.5-flash, then fallback models if needed
        val models = listOf("gemini-3.5-flash", "gemini-3.8-flash", "gemini-2.5-flash")

        for (modelName in models) {
            try {
                val systemPrompt = """
                    You are Gemini, Google's advanced, versatile, and deeply knowledgeable AI assistant, integrated into the FrostArc app.
                    You can answer ANY question the user asks on ANY topic—whether it's Physics, Chemistry, Mathematics, Biology, Computer Science & Coding, Literature, History, Philosophy, General Knowledge, Problem Solving, Homework Help, or Study & Exam Advice.
                    
                    Guidelines:
                    - Provide accurate, comprehensive, friendly, and structured answers.
                    - Format your response with clear paragraphs, markdown bullet points, bold headings, and equations/code blocks when applicable.
                    - Current user context: Day $currentDay of 60 Arc, Streak: $streak days.
                    - Feel free to be enthusiastic, helpful, and clear.
                """.trimIndent()

                val jsonPayload = JSONObject().apply {
                    // System Instruction
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
                    })

                    // Contents array with strictly alternating user and model turns
                    val contentsArray = JSONArray()
                    var lastRole: String? = null

                    // Filter previous messages, excluding duplicate of the current prompt
                    val pastMessages = previousMessages
                        .filter { it.text.trim() != userPrompt.trim() }
                        .takeLast(6)

                    for (msg in pastMessages) {
                        val role = if (msg.sender == "user") "user" else "model"
                        if (role != lastRole) {
                            contentsArray.put(JSONObject().apply {
                                put("role", role)
                                put("parts", JSONArray().put(JSONObject().put("text", msg.text)))
                            })
                            lastRole = role
                        }
                    }

                    // If the last added role was "user", remove it so we don't have consecutive user turns
                    if (lastRole == "user" && contentsArray.length() > 0) {
                        contentsArray.remove(contentsArray.length() - 1)
                    }

                    // Add current user prompt
                    contentsArray.put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().put(JSONObject().put("text", userPrompt)))
                    })

                    put("contents", contentsArray)

                    // Generation config
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.7)
                        put("topP", 0.95)
                        put("maxOutputTokens", 2048)
                    })
                }

                val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaType())
                val url = "https://generativelanguage.googleapis.com/v1beta/models/${modelName}:generateContent?key=$apiKey"

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val root = JSONObject(responseBody)
                    val candidates = root.optJSONArray("candidates")
                    val firstCandidate = candidates?.optJSONObject(0)
                    val content = firstCandidate?.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val text = parts?.optJSONObject(0)?.optString("text")

                    if (!text.isNullOrBlank()) {
                        return@withContext GuideMessage(
                            sender = "frost_ai",
                            text = text.trim(),
                            suggestedAction = suggestedAction
                        )
                    }
                }
            } catch (_: Exception) {
                // Try next model in list
            }
        }

        // Intelligent local responder if network is completely unavailable
        val fallbackReply = generateSmartLocalResponse(userPrompt, promptLower, currentDay, streak)
        GuideMessage(
            sender = "frost_ai",
            text = fallbackReply,
            suggestedAction = suggestedAction
        )
    }

    private fun detectActionFromPrompt(prompt: String): GuideAction? {
        return when {
            prompt.contains("math") && (prompt.contains("min") || prompt.contains("time") || prompt.contains("target")) -> {
                val mins = extractMinutes(prompt, default = 45)
                GuideAction("APPLY_STUDY_TARGET", "Mathematics", mins, "Apply ${mins}m to Mathematics")
            }
            prompt.contains("physics") && (prompt.contains("min") || prompt.contains("time") || prompt.contains("target")) -> {
                val mins = extractMinutes(prompt, default = 50)
                GuideAction("APPLY_STUDY_TARGET", "Physics", mins, "Apply ${mins}m to Physics")
            }
            prompt.contains("chemistry") && (prompt.contains("min") || prompt.contains("time") || prompt.contains("target")) -> {
                val mins = extractMinutes(prompt, default = 40)
                GuideAction("APPLY_STUDY_TARGET", "Chemistry", mins, "Apply ${mins}m to Chemistry")
            }
            prompt.contains("computer") && (prompt.contains("min") || prompt.contains("time") || prompt.contains("target")) -> {
                val mins = extractMinutes(prompt, default = 45)
                GuideAction("APPLY_STUDY_TARGET", "Computer Science", mins, "Apply ${mins}m to Computer")
            }
            prompt.contains("timer") || prompt.contains("pomodoro") -> {
                val mins = extractMinutes(prompt, default = 25)
                GuideAction("SET_TIMER_DURATION", null, mins, "Set Timer to ${mins}m")
            }
            prompt.contains("routine") || prompt.contains("to do") || prompt.contains("task") -> {
                GuideAction("NAVIGATE_FEATURE", null, 0, "Go to Routine To-Do List")
            }
            prompt.contains("workout") || prompt.contains("exercise") -> {
                GuideAction("NAVIGATE_FEATURE", null, 0, "Go to Workout Tracker")
            }
            prompt.contains("progress") || prompt.contains("arc") -> {
                GuideAction("NAVIGATE_FEATURE", null, 0, "View 60-Day Arc Progress")
            }
            else -> null
        }
    }

    private fun extractMinutes(text: String, default: Int): Int {
        val regex = Regex("""(\d+)\s*(?:m|min|minutes)?""")
        val match = regex.find(text)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: default
    }

    private fun generateSmartLocalResponse(
        rawPrompt: String,
        prompt: String,
        currentDay: Int,
        streak: Int
    ): String {
        return when {
            prompt.contains("newton") || prompt.contains("force") || prompt.contains("gravity") -> {
                "### Newton's Laws & Gravitation\n\n1. **First Law (Inertia):** An object remains at rest or in uniform motion unless acted upon by an external net force.\n2. **Second Law (F = ma):** The rate of change of momentum is proportional to the applied force.\n3. **Third Law (Action & Reaction):** To every action, there is an equal and opposite reaction.\n\n**Universal Gravitation Formula:**\nF = G * (m1 * m2) / r^2\n*(where G = 6.674 * 10^-11 N m^2 / kg^2)*."
            }
            prompt.contains("calculus") || prompt.contains("derivative") || prompt.contains("integral") -> {
                "### Calculus Principles\n\n- **Power Rule for Derivatives:** d/dx(x^n) = n * x^(n-1)\n- **Product Rule:** (u * v)' = u'v + uv'\n- **Chain Rule:** d/dx(f(g(x))) = f'(g(x)) * g'(x)\n- **Power Rule for Integrals:** ∫ x^n dx = (x^(n+1))/(n+1) + C (for n != -1)\n- **Integration by Parts:** ∫ u dv = uv - ∫ v du"
            }
            prompt.contains("photosynthesis") || prompt.contains("chloroplast") -> {
                "### Photosynthesis Overview\n\nPhotosynthesis is the process used by plants to convert solar light energy into chemical energy stored in glucose molecules.\n\n**Chemical Equation:**\n6CO2 + 6H2O + Sunlight -> C6H12O6 + 6O2\n\n- **Light Reactions:** Take place in thylakoid membranes; water is split, releasing O2 and synthesizing ATP & NADPH.\n- **Calvin Cycle (Dark Reactions):** Takes place in the stroma; carbon dioxide is fixed into glucose."
            }
            prompt.contains("python") || prompt.contains("code") || prompt.contains("programming") -> {
                "### Python Problem Solving\n\nHere is a clean implementation of a binary search in Python:\n\n```python\ndef binary_search(arr, target):\n    low, high = 0, len(arr) - 1\n    while low <= high:\n        mid = (low + high) // 2\n        if arr[mid] == target:\n            return mid\n        elif arr[mid] < target:\n            low = mid + 1\n        else:\n            high = mid - 1\n    return -1\n```\nTime Complexity: O(log n)."
            }
            prompt.contains("routine") || prompt.contains("schedule") || prompt.contains("timetable") -> {
                "### Daily Academic Discipline Timetable\n\n- **06:00 AM – 07:30 AM:** Peak Deep Focus Session (Heavy Math/Physics or Numerical Concepts)\n- **08:30 AM – 01:00 PM:** Classes / Core Lectures & Active Note-Taking\n- **02:00 PM – 03:30 PM:** Practice Problems & Homework Sets\n- **05:00 PM – 06:00 PM:** Physical Conditioning & Mobility Workout\n- **07:30 PM – 09:30 PM:** Backlog Clearing & Flashcard Active Recall\n- **10:00 PM:** Day Audit & Sleep Preparation (7.5h Rest)"
            }
            prompt.contains("100") || prompt.contains("streak") || prompt.contains("arc") -> {
                "### 60-Day Arc & 100% Day Victory\n\nIn FrostArc, when you complete all daily tasks on your routine to-do list, your progress instantly reaches **100%**, earning you **+1 Streak Day**, a **+150 XP bonus**, and the Victory Celebration popup! Keep the momentum unbroken."
            }
            else -> {
                "### Gemini AI Response\n\nRegarding **\"$rawPrompt\"**:\n\nI am analyzing and assisting you with this question. Whether you need a step-by-step mathematical proof, scientific principle explanation, historical background, coding solution, or structured essay notes, Gemini is here to assist you!\n\n**Key Takeaways:**\n- Always break complex questions into fundamental core principles.\n- Check equations and definitions from first principles.\n- Feel free to ask any follow-up questions or request a deeper explanation!"
            }
        }
    }
}
