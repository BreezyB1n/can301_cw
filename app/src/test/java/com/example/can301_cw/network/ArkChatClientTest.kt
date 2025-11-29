package com.example.can301_cw.network

import org.junit.Test

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
        // 使用给定图片 URL（服务端需支持 image_url 解析或自行下载转 base64）
        val imageUrl = "https://ark-project.tos-cn-beijing.ivolces.com/images/view.jpeg"

        val result = ArkChatClient.chatWithImageUrl(
            tags = listOf("图片", "测试"),
            content = imageUrl,
            isImage = true
        )

        result.onSuccess { println("schema (image) success: $it") }
            .onFailure { it.printStackTrace() }
    }
}
