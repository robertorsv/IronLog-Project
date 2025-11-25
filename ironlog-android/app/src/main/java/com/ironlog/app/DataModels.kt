package com.ironlog.app

import com.google.firebase.Timestamp

data class Workout(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val sets: List<WorkoutSet> = emptyList()
)

data class WorkoutSet(
    val exerciseId: String = "",
    val exerciseName: String = "",
    val weight: Double = 0.0,
    val reps: Int = 0
)
