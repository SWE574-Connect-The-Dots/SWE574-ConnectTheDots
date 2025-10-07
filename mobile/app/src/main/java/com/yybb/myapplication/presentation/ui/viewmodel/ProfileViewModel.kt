package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.yybb.myapplication.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel()
