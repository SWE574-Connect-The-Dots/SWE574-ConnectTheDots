package com.yybb.myapplication.data.repository

import com.yybb.myapplication.data.model.Space
import com.yybb.myapplication.data.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ProfileRepository @Inject constructor() {
    // Mock data
    private var user = User(
        id = "1",
        username = "Michael",
        profession = "Basketball Player",
        bio = "Passionate about building efficient and user-friendly digital solutions, Esra enjoys tackling technical challenges that combine creativity and problem-solving. In addition to her technical skills, she values teamwork, continuous learning, and contributing to innovative projects that make a real-world impact.",
        dateOfBirth = "05.06.1975",
        joinedDate = "18.05.2025",
        ownedSpaces = listOf(
            Space("1", "Are We Losing the Art of Genuin...", "michael"),
            Space("2", "Are We Losing the Art of Genuin...", "michael"),
            Space("3", "Are We Losing the Art of Genuin...", "michael"),
            Space("4", "Are We Losing the Art of Genuin...", "michael")
        ),
        joinedSpaces = listOf(
            Space("5", "Are We Losing the Art of Genuin...", "someone"),
            Space("6", "Are We Losing the Art of Genuin...", "someone"),
            Space("7", "Are We Losing the Art of Genuin...", "someone"),
            Space("8", "Are We Losing the Art of Genuin...", "someone")
        )
    )

    fun getProfile(userId: String): Flow<User> = flow {
        // In a real app, you would fetch this from a data source
        // and handle different userIds.
        emit(user)
    }

    suspend fun updateProfile(userId: String, profession: String, bio: String) {
        // Simulate a network call
        delay(1000)
        user = user.copy(profession = profession, bio = bio)
    }
}
