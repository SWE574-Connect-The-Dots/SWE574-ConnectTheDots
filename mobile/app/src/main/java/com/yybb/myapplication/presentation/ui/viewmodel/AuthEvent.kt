package com.yybb.myapplication.presentation.ui.viewmodel

sealed class AuthEvent {
    object NavigateToMain : AuthEvent()
}
