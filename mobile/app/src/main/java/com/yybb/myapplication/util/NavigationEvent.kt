package com.yybb.myapplication.util

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow

object NavigationEvent {
    private val _events = Channel<Event>()
    val events = _events.receiveAsFlow()

    suspend fun emit(event: Event) {
        _events.send(event)
    }

    sealed class Event {
        object NavigateToLogin : Event()
    }
}
