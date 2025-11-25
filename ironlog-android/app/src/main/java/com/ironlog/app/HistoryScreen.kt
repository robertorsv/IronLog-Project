package com.ironlog.app

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HistoryScreen(userId: String) {
    var workouts by remember { mutableStateOf<List<Workout>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        val db = FirebaseFirestore.getInstance()
        db.collection("workouts")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("IronLog", "History listen failed", e)
                    isLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    workouts = snapshot.documents.mapNotNull { doc ->
                        try {
                            val setsData = doc.get("sets") as? List<Map<String, Any>> ?: emptyList()
                            val sets = setsData.map { setMap ->
                                WorkoutSet(
                                    exerciseId = setMap["exerciseId"] as? String ?: "",
                                    exerciseName = setMap["exerciseName"] as? String ?: "",
                                    weight = (setMap["weight"] as? Number)?.toDouble() ?: 0.0,
                                    reps = (setMap["reps"] as? Number)?.toInt() ?: 0
                                )
                            }
                            
                            Workout(
                                id = doc.id,
                                userId = doc.getString("userId") ?: "",
                                name = doc.getString("name") ?: "Workout",
                                timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now(),
                                sets = sets
                            )
                        } catch (e: Exception) {
                            Log.e("IronLog", "Error parsing workout", e)
                            null
                        }
                    }
                    isLoading = false
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Workout History",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (workouts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No workouts yet. Start one in the Registry!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(workouts) { workout ->
                    WorkoutCard(workout)
                }
            }
        }
    }
}

@Composable
fun WorkoutCard(workout: Workout) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workout.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = dateFormat.format(workout.timestamp.toDate()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                // Delete button
                IconButton(
                    onClick = {
                        // Delete workout from Firestore
                        val db = FirebaseFirestore.getInstance()
                        db.collection("workouts").document(workout.id).delete()
                            .addOnSuccessListener {
                                Log.d("IronLog", "Workout deleted: ${workout.id}")
                            }
                            .addOnFailureListener { e ->
                                Log.e("IronLog", "Error deleting workout", e)
                            }
                    }
                ) {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_menu_delete),
                        contentDescription = "Delete Workout",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Group sets by exercise for cleaner display
            val groupedSets = workout.sets.groupBy { it.exerciseName }
            
            groupedSets.forEach { (exerciseName, sets) ->
                Text(
                    text = exerciseName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                sets.forEachIndexed { index, set ->
                    Text(
                        text = "Set ${index + 1}: ${set.weight}kg x ${set.reps}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Text(
                text = "Total Sets: ${workout.sets.size}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
