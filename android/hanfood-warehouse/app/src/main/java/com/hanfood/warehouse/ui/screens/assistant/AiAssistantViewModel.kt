package com.hanfood.warehouse.ui.screens.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanfood.warehouse.ai.AiEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean, val timestamp: Long = System.currentTimeMillis())

data class AssistantUiState(
    val messages: List<ChatMessage> = emptyList(),
    val thinking: Boolean = false,
    val input: String = ""
)

class AiAssistantViewModel(private val engine: AiEngine) : ViewModel() {

    private val _state = MutableStateFlow(
        AssistantUiState(
            messages = listOf(
                ChatMessage(
                    text = "Assalomu alaykum! Men HAN FOOD ombor bo'yicha AI yordamchiman. " +
                        "Qoldiqlar, kirim-chiqim va mijozlar haqida savol bering — javobni to'g'ridan-to'g'ri " +
                        "ombor ma'lumotlaridan hisoblab beraman.",
                    isUser = false
                )
            )
        )
    )
    val state: StateFlow<AssistantUiState> = _state.asStateFlow()

    val suggestedQuestions: List<String> = engine.suggestedQuestions()

    fun onInputChange(value: String) {
        _state.value = _state.value.copy(input = value)
    }

    fun send(text: String = _state.value.input) {
        val question = text.trim()
        if (question.isEmpty() || _state.value.thinking) return
        _state.value = _state.value.copy(
            messages = _state.value.messages + ChatMessage(question, isUser = true),
            input = "",
            thinking = true
        )
        viewModelScope.launch {
            val answer = engine.answer(question)
            _state.value = _state.value.copy(
                messages = _state.value.messages + ChatMessage(answer, isUser = false),
                thinking = false
            )
        }
    }
}
