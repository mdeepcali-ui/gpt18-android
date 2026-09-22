package com.gptplus18.app.ui.screens.chat

object MessageHelpers {
    private val IMG_REGEX = Regex("""!\[[^\]]*\]\(([^)]+)\)""")
    private val AUDIO_REGEX = Regex("""\[AUDIO:([^\]]+)\]""")
    private val VIDEO_REGEX = Regex("""\[VIDEO:([^\]]+)\]""")

    fun extractImageUrl(text: String): String? =
        IMG_REGEX.find(text)?.groupValues?.getOrNull(1)

    fun extractAudioUrl(text: String): String? =
        AUDIO_REGEX.find(text)?.groupValues?.getOrNull(1)

    fun stripMediaMarkers(text: String): String {
        var out = text
        out = out.replace(IMG_REGEX, "").trim()
        out = out.replace(AUDIO_REGEX, "").trim()
        out = out.replace(VIDEO_REGEX, "").trim()
        return out
    }
}
