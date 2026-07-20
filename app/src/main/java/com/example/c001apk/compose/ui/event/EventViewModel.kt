package com.example.c001apk.compose.ui.event

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.compose.logic.model.EventDetailResponse
import com.example.c001apk.compose.logic.repository.NetworkRepo
import com.example.c001apk.compose.logic.state.LoadingState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = EventViewModel.ViewModelFactory::class)
class EventViewModel @AssistedInject constructor(
    @Assisted private val id: String,
    private val networkRepo: NetworkRepo,
) : ViewModel() {

    @AssistedFactory
    interface ViewModelFactory {
        fun create(id: String): EventViewModel
    }

    var eventState by mutableStateOf<LoadingState<EventDetailResponse.Data>>(LoadingState.Loading)
        private set

    init {
        fetchEvent()
    }

    fun fetchEvent() {
        eventState = LoadingState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getEventDetail(id).collect { eventState = it }
        }
    }
}
