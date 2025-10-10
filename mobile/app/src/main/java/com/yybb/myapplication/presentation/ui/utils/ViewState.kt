package com.yybb.myapplication.presentation.ui.utils

sealed interface ViewState<out T> {
    object Loading : ViewState<Nothing>
    data class Success<T>(val data: T) : ViewState<T>
    data class Error(val message: String) : ViewState<Nothing>
}
