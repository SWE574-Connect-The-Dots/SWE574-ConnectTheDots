package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SpaceDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val spaceId: String = checkNotNull(savedStateHandle["spaceId"])
}
