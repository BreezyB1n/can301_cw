package com.example.can301_cw.network

import com.example.can301_cw.network.ArkChatClient
import org.junit.Test
import java.util.Base64

class ArkChatClientTest {

    @Test
    fun callStructuredSchema() {
        val result = ArkChatClient.chatWithImageUrl(
            tags = listOf("测试", "1"),
            content = "测试",
            isImage = false
        )

        result.onSuccess { println("schema success: $it") }
            .onFailure { it.printStackTrace() }
    }

    @Test
    fun callStructuredSchemaWithImageBase64() {
        // 从 test resources 目录读取图片并转换为 base64
        val inputStream = javaClass.classLoader?.getResourceAsStream("test.jpg")
            ?: error("图片文件不存在，请将 test.jpg 放到 app/src/test/resources/ 目录下")
        
        val imageBytes = inputStream.readBytes()
        val base64Content = Base64.getEncoder().encodeToString(imageBytes)

        val result = ArkChatClient.chatWithImageUrl(
            tags = listOf("图片", "测试"),
            content = base64Content,
            isImage = true
        )

        result.onSuccess { println("schema (image) success: $it") }
            .onFailure { it.printStackTrace() }
    }
}
