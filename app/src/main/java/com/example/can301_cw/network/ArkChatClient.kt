package com.example.can301_cw.network

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

        val userPayloadObj = JsonObject().apply {
            add("tags", JsonArray().also { arr -> tags.forEach { arr.add(it) } })
            addProperty("content", content)
            addProperty("isimage", if (isImage) 1 else 0)
        }

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
                        "properties": {},
                        "additionalProperties": true
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
                            "type": "object",
                            "properties": {
                              "targetId": {"type": "number"},
                              "relationship": {"type": "string"}
                            },
                            "required": ["targetId","relationship"],
                            "additionalProperties": false
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
                      "items": {"type": "string"}
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

        val requestJson = JsonObject().apply {
            addProperty("model", modelId)
            add("messages", JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("role", "system")
                    addProperty("content", "你是 AI 助手，必须按指定 JSON schema 返回，不得添加多余字段。")
                })
                add(JsonObject().apply {
                    addProperty("role", "user")
                    add("content", JsonArray().apply {
                        add(JsonObject().apply {
                            addProperty("type", "text")
                            addProperty("text", userPayloadObj.toString())
                        })
                    })
                })
            })
            add("response_format", JsonObject().apply {
                addProperty("type", "json_schema")
                add("json_schema", JsonObject().apply {
                    addProperty("name", "structured_summary")
                    add("schema", schemaObj)
                    addProperty("strict", true)
                })
            })
            add("thinking", JsonObject().apply { addProperty("type", "disabled") })
        }

        val jsonString = gson.toJson(requestJson)
        println("ArkChatClient Request JSON: $jsonString")

        return runCatching {
            executeRequestWithRetry(baseUrl, apiKey, jsonString)
        }
    }

    private fun executeRequestWithRetry(baseUrl: String, apiKey: String, jsonBody: String): String {
        val connection = (URL(baseUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 20_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $apiKey")
        }

        connection.outputStream.use { output ->
            output.write(jsonBody.toByteArray(Charsets.UTF_8))
            output.flush()
        }

        val responseCode = connection.responseCode
        val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream ?: connection.inputStream
        val body = stream.bufferedReader().use(BufferedReader::readText)
        connection.disconnect()

        if (responseCode !in 200..299) {
            error("HTTP $responseCode $body")
        }
        return body
    }
}
