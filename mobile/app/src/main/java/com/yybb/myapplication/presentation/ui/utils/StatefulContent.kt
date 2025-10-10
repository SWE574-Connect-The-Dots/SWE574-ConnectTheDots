package com.yybb.myapplication.presentation.ui.utils

import androidx.compose.runtime.Composable

@Composable
fun <T> StatefulContent(
    state: ViewState<T>,
    loadingContent: @Composable () -> Unit,
    errorContent: @Composable (String) -> Unit,
    successContent: @Composable (T) -> Unit
) {
    when (state) {
        is ViewState.Loading -> loadingContent()
        is ViewState.Success -> successContent(state.data)
        is ViewState.Error -> errorContent(state.message)
    }
}
