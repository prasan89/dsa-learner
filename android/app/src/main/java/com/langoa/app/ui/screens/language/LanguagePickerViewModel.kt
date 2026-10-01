package com.langoa.app.ui.screens.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.langoa.app.domain.model.Language
import com.langoa.app.domain.model.SUPPORTED_LANGUAGES
import com.langoa.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguagePickerViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _languages = MutableStateFlow(SUPPORTED_LANGUAGES)
    val languages: StateFlow<List<Language>> = _languages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun selectLanguage(languageCode: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.saveSelectedLanguage(languageCode)
            _isLoading.value = false
            onSuccess()
        }
    }
}
