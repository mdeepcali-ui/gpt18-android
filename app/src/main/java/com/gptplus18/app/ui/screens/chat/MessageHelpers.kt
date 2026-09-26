package com.gptplus18.app.ui.screens.chat

object MessageHelpers {
    private val IMG_REGEX = Regex("""!\[[^\]]*\]\(([^)]+)\)""")
    private val AUDIO_REGEX = Regex("""\[AUDIO:([^\]]+)\]""")
    private val VIDEO_REGEX = Regex("""\[VIDEO:([^\]]+)\]""")
    private val CHOICE_REGEX = Regex("""\[\[CHOICE:([^\]]+)\]\]""")

    fun extractImageUrl(text: String): String? =
        IMG_REGEX.find(text)?.groupValues?.getOrNull(1)

    fun extractAudioUrl(text: String): String? =
        AUDIO_REGEX.find(text)?.groupValues?.getOrNull(1)

    fun extractVideoUrl(text: String): String? =
        VIDEO_REGEX.find(text)?.groupValues?.getOrNull(1)

    fun extractChoices(text: String): List<String> {
        val m = CHOICE_REGEX.find(text) ?: return emptyList()
        return m.groupValues[1].split("|").map { it.trim() }.filter { it.isNotEmpty() }
    }

    fun stripChoiceMarkers(text: String): String {
        return text.replace(CHOICE_REGEX, "").trim()
    }

    fun stripMediaMarkers(text: String): String {
        var out = text
        out = out.replace(IMG_REGEX, "").trim()
        out = out.replace(AUDIO_REGEX, "").trim()
        out = out.replace(VIDEO_REGEX, "").trim()
        out = out.replace(CHOICE_REGEX, "").trim()
        return out
    }
}
