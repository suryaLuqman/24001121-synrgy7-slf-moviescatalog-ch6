package com.slf.moviescatalog.utils

import android.util.Patterns

object Validation {
    fun isValidEmail(email: CharSequence): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: CharSequence): Boolean {
        // Aturan validasi password minimal 8 karakter
        return password.length >= 8
    }
}
