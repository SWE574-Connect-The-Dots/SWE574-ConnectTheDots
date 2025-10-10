package com.yybb.myapplication.presentation.ui.viewmodel

sealed class AuthEvent {
    object NavigateToMain : AuthEvent()
    object NavigateToRegister : AuthEvent()
    object NavigateToLogin : AuthEvent()
}