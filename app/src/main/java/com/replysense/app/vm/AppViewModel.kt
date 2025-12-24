package com.replysense.app.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.replysense.app.db.ReplyHistoryEntity
import com.replysense.app.net.ConversationTurn
import com.replysense.app.net.ReplyRequest
import com.replysense.app.net.ReplyResponse
import com.replysense.app.repo.ReplyRepository
import com.replysense.app.util.ConversationParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Preset(val label: String) {
    DEFAULT("Default"),
    FLIRTY("Flirty"),
    SERIOUS("Serious"),
    APOLOGY("Apology"),
    CONFIDENT("Confident"),
    SHORT("Short")
}

data class UiState(
    val tab: Int = 0, // 0=Compose, 1=History
    val variants: Int = 3,

    // conversation thread
    val turns: List<ConversationTurn> = listOf(ConversationTurn("them", "")),

    // controls (strings; include "auto" as an option)
    val vibe: String = "auto",
    val tone: String = "auto",
    val writingStyle: String = "auto",
    val textQuality: String = "auto",
    val emojiLevel: String = "auto",
    val spiceLevel: String = "auto",
    val age: String = "auto",
    val punctuationPreference: String = "auto",

    val preset: Preset = Preset.DEFAULT,

    val isLoading: Boolean = false,
    val error: String? = null,

    val detectedNotes: String? = null,
    val replies: List<String> = emptyList(),
    val lastSavedId: Long? = null,

    val history: List<ReplyHistoryEntity> = emptyList(),
    val favoritesOnly: Boolean = false
)

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ReplyRepository(app.applicationContext)
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var incomingApplied = false

    init {
        viewModelScope.launch {
            repo.observeHistory().collect { list ->
                _state.update { s ->
                    val visible = if (s.favoritesOnly) list.filter { it.isFavorite } else list
                    s.copy(history = visible)
                }
            }
        }
    }

    fun setTab(tab: Int) = _state.update { it.copy(tab = tab) }

    fun applyIncomingTextOnce(text: String) {
        if (incomingApplied) return
        incomingApplied = true
        applyIncomingTextForce(text)
    }

    fun applyIncomingTextForce(text: String) {
        val parsed = ConversationParser.parse(text)
        _state.update { it.copy(tab = 0, turns = parsed) }
    }

    fun smartExtract(rawBlock: String) {
        val parsed = ConversationParser.parse(rawBlock)
        _state.update { it.copy(turns = parsed, error = null) }
    }

    fun setFavoritesOnly(v: Boolean) = _state.update { it.copy(favoritesOnly = v) }

    fun setVariants(v: Int) = _state.update { it.copy(variants = v.coerceIn(1, 6)) }

    fun setVibe(v: String) = _state.update { it.copy(vibe = v) }
    fun setTone(v: String) = _state.update { it.copy(tone = v) }
    fun setWritingStyle(v: String) = _state.update { it.copy(writingStyle = v) }
    fun setTextQuality(v: String) = _state.update { it.copy(textQuality = v) }
    fun setEmojiLevel(v: String) = _state.update { it.copy(emojiLevel = v) }
    fun setSpiceLevel(v: String) = _state.update { it.copy(spiceLevel = v) }
    fun setAge(v: String) = _state.update { it.copy(age = v) }
    fun setPunctuationPreference(v: String) = _state.update { it.copy(punctuationPreference = v) }

    fun setPreset(p: Preset) {
        _state.update { s ->
            // Presets *steer* without nuking auto vibe. All values remain valid tokens.
            val (tone, writingStyle, textQuality, emojiLevel, spiceLevel) = when (p) {
                Preset.DEFAULT -> listOf("auto", "auto", "auto", "auto", "auto")
                Preset.FLIRTY -> listOf("playful", "casual", "auto", "auto", s.spiceLevel) // user controls spice separately
                Preset.SERIOUS -> listOf("serious", "clean", "perfect", "0", "0")
                Preset.APOLOGY -> listOf("supportive", "clean", "perfect", "0", "0")
                Preset.CONFIDENT -> listOf("confident", "clean", "perfect", "0", "0")
                Preset.SHORT -> listOf("auto", "auto", "auto", "auto", "auto")
            }

            s.copy(
                preset = p,
                tone = tone,
                writingStyle = writingStyle,
                textQuality = textQuality,
                emojiLevel = emojiLevel,
                // if preset is serious/apology/confident, force spice off
                spiceLevel = spiceLevel
            )
        }
    }

    fun addTurn(from: String) {
        _state.update { it.copy(turns = it.turns + ConversationTurn(from, "")) }
    }

    fun removeTurn(index: Int) {
        _state.update { s ->
            val n = s.turns.toMutableList()
            if (n.size <= 1) return@update s
            if (index in n.indices) n.removeAt(index)
            s.copy(turns = n)
        }
    }

    fun updateTurn(index: Int, from: String? = null, text: String? = null) {
        _state.update { s ->
            val n = s.turns.toMutableList()
            if (index !in n.indices) return@update s
            val cur = n[index]
            n[index] = ConversationTurn(from ?: cur.from, text ?: cur.text)
            s.copy(turns = n, error = null)
        }
    }

    fun clearAll() {
        incomingApplied = false
        _state.value = UiState()
    }

    fun toggleFavorite(entity: ReplyHistoryEntity) {
        viewModelScope.launch { repo.toggleFavorite(entity) }
    }

    fun deleteHistory(entity: ReplyHistoryEntity) {
        viewModelScope.launch { repo.delete(entity) }
    }

    fun generate() {
        val s = _state.value
        val cleaned = s.turns
            .map { it.copy(text = it.text.trim()) }
            .filter { it.text.isNotBlank() }

        if (cleaned.isEmpty()) {
            _state.update { it.copy(error = "Add at least one message in the thread.") }
            return
        }

        val payload = ReplyRequest(
            conversation = cleaned,
            variants = s.variants,
            vibe = s.vibe,
            tone = s.tone,
            writingStyle = s.writingStyle,
            textQuality = s.textQuality,
            emojiLevel = s.emojiLevel,
            spiceLevel = s.spiceLevel,
            age = s.age,
            punctuationPreference = s.punctuationPreference
        )

        _state.update { it.copy(isLoading = true, error = null, replies = emptyList(), detectedNotes = null) }

        viewModelScope.launch {
            val res = repo.generateAndSave(payload)
            res.fold(
                onSuccess = { (response, id) ->
                    val replies = response.result?.replies.orEmpty().let { list ->
                        if (s.preset == Preset.SHORT) list.map { shorten(it) } else list
                    }
                    val notes = response.result?.vibe?.notes
                    _state.update { it.copy(isLoading = false, replies = replies, detectedNotes = notes, lastSavedId = id) }
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Something broke.") }
                }
            )
        }
    }

    private fun shorten(x: String): String {
        val t = x.trim().replace(Regex("\\s+"), " ")
        return if (t.length <= 120) t else t.take(117).trimEnd() + "…"
    }
}

data class DecodedHistory(
    val conversation: List<ConversationTurn>,
    val request: ReplyRequest,
    val response: ReplyResponse
)
