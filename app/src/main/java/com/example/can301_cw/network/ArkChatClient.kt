package com.example.can301_cw.network

import android.content.Context
import com.example.can301_cw.R
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * 仅使用 HttpURLConnection 的 Ark ChatCompletion 封装。
 * - 支持结构化响应（response_format: json_schema），遵循用户给定的 tags/content/isimage 协议。
 */
object ArkChatClient {
    private const val DEFAULT_BASE_URL = "https://ark.cn-beijing.volces.com/api/v3/chat/completions"
    private val gson = Gson()

    /**
     * 结构化对话：发送 {tags, content, isimage}，要求服务端返回指定 schema。
     * content: 文本或 base64(当 isImage=true 时)。
     * @return 原始 JSON 响应字符串
     */
    fun chatWithImageUrl(
        context: Context,
        tags: List<String>,
        content: String,
        isImage: Boolean,
        modelId: String = "doubao-seed-1-6-vision-250815",
        apiKeyResId: Int = R.string.ARK_API_KEY,
        baseUrl: String = DEFAULT_BASE_URL
    ): Result<String> {
        val apiKey = context.getString(apiKeyResId)
        return chatWithImageUrl(
            tags = tags,
            content = content,
            isImage = isImage,
            modelId = modelId,
            apiKey = apiKey,
            baseUrl = baseUrl
        )
    }

    /**
     * 直接传入 apiKey 的版本，便于测试或自定义密钥来源。
     * @return 原始 JSON 响应字符串
     */
    fun chatWithImageUrl(
        tags: List<String>,
        content: String,
        isImage: Boolean,
        modelId: String = "doubao-seed-1-6-vision-250815",
        apiKey: String = ARK_API_KEY,
        baseUrl: String = DEFAULT_BASE_URL
    ): Result<String> {
        if (content.isBlank()) return Result.failure(IllegalArgumentException("content is blank"))
        if (apiKey.isBlank()) return Result.failure(IllegalArgumentException("apiKey is blank"))

        // schema 直接用 JSON 字符串定义，避免手写大量节点出错
        val schemaObj = JsonParser.parseString(
            """
            {
              "type": "object",
              "properties": {
                "mostPossibleCategory": {"type": "string"},
                "schedule": {
                  "type": "object",
                  "properties": {
                    "title": {"type": "string"},
                    "category": {"type": "string"},
                    "tasks": {
                      "type": "array",
                      "items": {
                        "type": "object",
                        "properties": {
                          "startTime": {"type": "string"},
                          "endTime": {"type": "string"},
                          "people": {"type": "array", "items": {"type": "string"}},
                          "theme": {"type": "string"},
                          "coreTasks": {"type": "array", "items": {"type": "string"}},
                          "position": {"type": "array", "items": {"type": "string"}},
                          "tags": {"type": "array", "items": {"type": "string"}, "maxItems": 6},
                          "category": {"type": "string"},
                          "suggestedActions": {"type": "array", "items": {"type": "string"}}
                        },
                        "required": ["startTime", "endTime", "people", "theme", "coreTasks", "position", "tags", "category", "suggestedActions"],
                        "additionalProperties": false
                      }
                    }
                  },
                  "required": ["title","category","tasks"],
                  "additionalProperties": false
                },
                "information": {
                  "type": "object",
                  "properties": {
                    "title": {"type": "string"},
                    "informationItems": {
                      "type": "array",
                      "items": {
                        "type": "object",
                        "properties": {
                          "id": {"type": "number"},
                          "header": {"type": "string"},
                          "content": {"type": "string"},
                          "node": {
                            "anyOf": [
                              {
                                "type": "object",
                                "properties": {
                                  "targetId": {"type": "number"},
                                  "relationship": {"type": "string"}
                                },
                                "required": ["targetId","relationship"],
                                "additionalProperties": false
                              },
                              {"type": "null"}
                            ]
                          }
                        },
                        "required": ["id","header","content","node"],
                        "additionalProperties": false
                      }
                    },
                    "relatedItems": {
                      "type": "array",
                      "items": {"type": "string"}
                    },
                    "summary": {"type": "string"},
                    "tags": {
                        "type": "array",
                        "items": {"type": "string"},
                        "maxItems": 6
                      }
                  },
                  "required": ["title","informationItems","relatedItems","summary","tags"],
                  "additionalProperties": false
                }
              },
              "required": ["mostPossibleCategory","schedule","information"],
              "additionalProperties": false
            }
            """.trimIndent()
        ).asJsonObject

        // 构建用户文本提示
        val textPrompt = JsonObject().apply {
            add("tags", JsonArray().also { arr -> tags.forEach { arr.add(it) } })
            addProperty("isimage", if (isImage) 1 else 0)
            addProperty("instruction", "Understand the image content according to the specified json schema and return the specified json format, without adding unnecessary fields. STRICTLY reply in English. STRICTLY LIMIT all 'tags' arrays to a maximum of 6 items. If there are more, select only the 6 most important tags. For 'startTime', use the format 'yyyy-MM-dd HH:mm'. If no specific time (HH:mm) is identified, use 'yyyy-MM-dd'. If the date cannot be determined, return 'Today'.")
            add("schema", schemaObj)
        }.toString()

        val requestJson = JsonObject().apply {
            addProperty("model", modelId)
            add("messages", JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("role", "user")
                    add("content", JsonArray().apply {
                        // 如果是图片，先添加 image_url
                        if (isImage) {
                            add(JsonObject().apply {
                                addProperty("type", "image_url")
                                add("image_url", JsonObject().apply {
                                    addProperty("url", "data:image/jpeg;base64,$content")
                                })
                            })
                        }
                        // 添加 text
                        add(JsonObject().apply {
                            addProperty("type", "text")
                            addProperty("text", if (isImage) textPrompt else content)
                        })
                    })
                })
            })
        }

        return runCatching {
            val connection = (URL(baseUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 30_000
                readTimeout = 120_000  // 图片处理需要较长时间
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
            }

            connection.outputStream.use { output ->
                output.write(gson.toJson(requestJson).toByteArray(Charsets.UTF_8))
                output.flush()
            }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream ?: connection.inputStream
            val body = stream.bufferedReader().use(BufferedReader::readText)
            connection.disconnect()

            if (responseCode !in 200..299) {
                error("HTTP $responseCode $body")
            }
            
            // 强制截断 tags，确保不超过 6 个
            sanitizeResponse(body)
        }
    }

    private fun sanitizeResponse(jsonResponse: String): String {
        return try {
            val responseObj = JsonParser.parseString(jsonResponse).asJsonObject
            val choices = responseObj.getAsJsonArray("choices")
            if (choices != null && choices.size() > 0) {
                val message = choices.get(0).asJsonObject.getAsJsonObject("message")
                val contentStr = message.get("content").asString
                
                // 解析 content 内部的 JSON
                val contentJson = JsonParser.parseString(contentStr).asJsonObject
                
                // 处理 schedule.tasks 中的 tags 和 taskStatus
                if (contentJson.has("schedule")) {
                    val schedule = contentJson.getAsJsonObject("schedule")
                    if (schedule.has("tasks")) {
                        val tasks = schedule.getAsJsonArray("tasks")
                        tasks.forEach { task ->
                            val taskObj = task.asJsonObject
                            if (taskObj.has("tags")) {
                                val tags = taskObj.getAsJsonArray("tags")
                                while (tags.size() > 6) {
                                    tags.remove(tags.size() - 1)
                                }
                            }
                            // 强制设置默认状态为 PENDING，以防 API 未返回
                            if (!taskObj.has("taskStatus")) {
                                taskObj.addProperty("taskStatus", "PENDING")
                            }
                            // 确保有 ID
                            if (!taskObj.has("id")) {
                                taskObj.addProperty("id", java.util.UUID.randomUUID().toString())
                            }
                        }
                    }
                }
                
                // 处理 information.tags
                if (contentJson.has("information")) {
                    val information = contentJson.getAsJsonObject("information")
                    if (information.has("tags")) {
                        val tags = information.getAsJsonArray("tags")
                        while (tags.size() > 6) {
                            tags.remove(tags.size() - 1)
                        }
                    }
                }
                
                // 更新 content
                message.addProperty("content", gson.toJson(contentJson))
                gson.toJson(responseObj)
            } else {
                jsonResponse
            }
        } catch (e: Exception) {
            // 如果解析失败，返回原始响应（避免因格式问题导致崩溃）
            e.printStackTrace()
            jsonResponse
        }
    }
}
