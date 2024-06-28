package com.silentgoat.flickrapp.gallery

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class GalleryError {
    NONE,
    CONNECTION_FAILED
}

sealed interface GalleryUiState {
    val isLoading: Boolean
    val error: GalleryError

    data class NoSearchResults(
        override val isLoading: Boolean,
        override val error: GalleryError
    ) : GalleryUiState

    data class HasSearchResults(
        val photos: List<GalleryPhotoUI>,
        val selectedIndex: Int,
        override val isLoading: Boolean,
        override val error: GalleryError
    ) : GalleryUiState
}

data class GalleryViewModelState(
    val photos: List<GalleryPhotoUI> = emptyList(),
    val selectedIndex:Int = -1,
    val isLoading: Boolean = false,
    val error: GalleryError = GalleryError.NONE
) {
    fun toUiState(): GalleryUiState {
        return if (photos.isEmpty()) {
            GalleryUiState.NoSearchResults(
                isLoading = isLoading,
                error = error
            )
        } else {
            GalleryUiState.HasSearchResults(
                photos = photos,
                selectedIndex = selectedIndex,
                isLoading = isLoading,
                error = error
            )
        }
    }
}

@HiltViewModel
class GalleryViewModel @Inject constructor(private val api: IGalleryApi): ViewModel() {
    companion object {
        val TAG = "GalleryViewModel"
    }

    private var isInit: Boolean = false

    private val viewModelState = MutableStateFlow(
        GalleryViewModelState()
    )

    private val searchMutableState = MutableStateFlow("")
    val searchState = searchMutableState.asStateFlow()

    val uiState = viewModelState
        .map { it.toUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = viewModelState.value.toUiState()
        )

    fun searchForPhotos(text: String) {
        preCheckViewModel()
        searchMutableState.update { text }
    }

    fun clearSearch() {
        preCheckViewModel()
        searchMutableState.update { "" }
        viewModelState.update { it.copy(photos = emptyList()) }
    }

    fun selectPhoto(index: Int) {
        preCheckViewModel()
        viewModelState.update { it.copy(selectedIndex = index) }
    }

    fun unselectPhoto() {
        preCheckViewModel()
        viewModelState.update { it.copy(selectedIndex = -1) }
    }

    private fun preCheckViewModel() {
        if (!isInit)
            error("GalleryViewModel was not initialized!")
    }

    @FlowPreview
    fun initialize() {
        if (isInit)
            return

        viewModelScope.launch {
            searchMutableState
                .filter { it.isNotEmpty() }
                .debounce(1000L)
                .collectLatest {
                    viewModelState.update { it.copy(isLoading = true) }
                    try {
                        val gallery = api.search(it)
                        viewModelState.update {
                            it.copy(
                                photos = gallery.items.toGalleryPhotoUI(),
                                isLoading = false
                            )
                        }
                    } catch (ex: Exception) {
                        Log.e(TAG, ex.message ?: "")
                        viewModelState.update { it.copy(error = GalleryError.CONNECTION_FAILED) }
                    }
                }
        }

        isInit = true
    }
}