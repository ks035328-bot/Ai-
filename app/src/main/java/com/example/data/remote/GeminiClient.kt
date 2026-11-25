package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class IntentResult(
    val actionType: String, // OPEN_APP, CLOSE_APP, CLOSE_ALL, ORGANIZE_DESKTOP, CLEAN_MEMORY, JOKE, CHAT
    val targetApp: String? = null,
    val responseSpeech: String
)

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    suspend fun processVoiceCommand(
        userPrompt: String,
        personaName: String = "Lumi",
        openAppsList: List<String> = emptyList()
    ): IntentResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext fallbackRuleBasedIntent(userPrompt, personaName, openAppsList)
        }

        val systemPrompt = """
            You are $personaName, a cute, helpful anime AI Assistant Girl that manages the user's desktop interface.
            Your job is to parse the user's voice command and respond with an action and a cute response.
            
            Current open apps on desktop: ${openAppsList.joinToString(", ")}.
            
            Identify the actionType from:
            - OPEN_APP (if user asks to open camera, browser, chrome, settings, notes, gallery, music, calculator, files, tasks, terminal, cleaner, weather)
            - CLOSE_APP (if user asks to close a specific app)
            - CLOSE_ALL (if user asks to close everything, close all apps, clear desktop)
            - ORGANIZE_DESKTOP (if user asks to organize, sort icons, arrange grid, clean layout)
            - CLEAN_MEMORY (if user asks to boost RAM, clean memory, clear cache, speed up)
            - JOKE (if user asks for a joke or playful chat)
            - CHAT (for general conversation)
            
            Respond ONLY in valid JSON format with keys:
            {
              "actionType": "OPEN_APP|CLOSE_APP|CLOSE_ALL|ORGANIZE_DESKTOP|CLEAN_MEMORY|JOKE|CHAT",
              "targetApp": "appName or null",
              "responseSpeech": "Short cheerful response under 25 words in voice of AI Assistant Girl"
            }
        """.trimIndent()

        val jsonPayload = """
            {
              "systemInstruction": {
                "parts": [{"text": ${escapeJson(systemPrompt)}}]
              },
              "contents": [
                {
                  "parts": [{"text": ${escapeJson(userPrompt)}}]
                }
              ]
            }
        """.trimIndent()

        try {
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(jsonPayload.toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful && responseBody.isNotEmpty()) {
                val parsed = parseGeminiText(responseBody)
                if (parsed != null) {
                    val intentJson = extractJsonBlock(parsed)
                    if (intentJson != null) {
                        val adapter = moshi.adapter(Map::class.java)
                        val map = adapter.fromJson(intentJson)
                        val action = map?.get("actionType")?.toString() ?: "CHAT"
                        val target = map?.get("targetApp")?.toString()
                        val speech = map?.get("responseSpeech")?.toString() ?: "I'm right here! How can I help organize your desktop?"
                        return@withContext IntentResult(action, target, speech)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext fallbackRuleBasedIntent(userPrompt, personaName, openAppsList)
    }

    private fun fallbackRuleBasedIntent(prompt: String, personaName: String, openAppsList: List<String>): IntentResult {
        val lower = prompt.lowercase().trim()

        return when {
            lower.contains("close all") || lower.contains("close everything") || lower.contains("clear all") || lower.contains("shut all") -> {
                IntentResult("CLOSE_ALL", null, "Closing all running desktop windows now! Everything is nice and tidy.")
            }
            lower.contains("close") -> {
                val target = findAppMatch(lower)
                if (target != null) {
                    IntentResult("CLOSE_APP", target, "Closing $target for you master!")
                } else {
                    IntentResult("CLOSE_ALL", null, "Closing active windows to keep your desktop organized!")
                }
            }
            lower.contains("open") || lower.contains("launch") || lower.contains("start") -> {
                val target = findAppMatch(lower) ?: "Browser"
                IntentResult("OPEN_APP", target, "Opening $target right away! Anything else?")
            }
            lower.contains("organize") || lower.contains("sort") || lower.contains("arrange") || lower.contains("clean desktop") -> {
                IntentResult("ORGANIZE_DESKTOP", null, "Desktop grid organized and sorted by category! Looking sharp.")
            }
            lower.contains("clean") || lower.contains("boost") || lower.contains("ram") || lower.contains("memory") -> {
                IntentResult("CLEAN_MEMORY", null, "Swept background memory! Saved 420 MB of RAM for smoother performance.")
            }
            lower.contains("joke") || lower.contains("funny") -> {
                IntentResult("JOKE", null, "Why do programmers prefer dark mode? Because light attracts bugs! Hehe~")
            }
            else -> {
                IntentResult("CHAT", null, "Hello! I am $personaName. Tell me to open an app, close background tasks, or clean your desktop!")
            }
        }
    }

    private fun findAppMatch(text: String): String? {
        val apps = listOf("Browser", "Camera", "Settings", "Notes", "Gallery", "Music Player", "Calculator", "Files Cabinet", "Task Matrix", "Terminal", "Memory Cleaner", "Weather Live")
        return apps.firstOrNull { text.contains(it.lowercase()) || (it == "Browser" && text.contains("chrome")) }
    }

    private fun parseGeminiText(json: String): String? {
        return try {
            val adapter = moshi.adapter(Map::class.java)
            val map = adapter.fromJson(json)
            val candidates = map?.get("candidates") as? List<*>
            val firstCand = candidates?.firstOrNull() as? Map<*, *>
            val content = firstCand?.get("content") as? Map<*, *>
            val parts = content?.get("parts") as? List<*>
            val firstPart = parts?.firstOrNull() as? Map<*, *>
            firstPart?.get("text")?.toString()
        } catch (e: Exception) {
            null
        }
    }

    private fun extractJsonBlock(raw: String): String? {
        val start = raw.indexOf('{')
        val end = raw.lastIndexOf('}')
        return if (start != -1 && end != -1 && end > start) {
            raw.substring(start, end + 1)
        } else null
    }

    private fun escapeJson(text: String): String {
        return "\"" + text.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r") + "\""
    }
}
