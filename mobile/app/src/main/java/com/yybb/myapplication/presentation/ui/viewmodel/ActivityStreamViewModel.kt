package com.yybb.myapplication.presentation.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.yybb.myapplication.data.repository.ActivityStreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ActivityStreamViewModel @Inject constructor(
    private val repository: ActivityStreamRepository
) : ViewModel()
