package com.yybb.myapplication.presentation.ui.viewmodel

sealed class SpacesEvent {
    data class NavigateToSpaceDetails(val spaceId: Int) : SpacesEvent()
}
