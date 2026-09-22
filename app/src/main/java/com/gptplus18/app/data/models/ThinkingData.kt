package com.gptplus18.app.data.models

data class ThinkingData(
    val steps: List<String> = emptyList(),
    val status: String = "think",
    val rawText: String? = null,  // ← نص التفكير الحقيقي من السيرفر
)
