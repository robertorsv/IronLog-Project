package com.ironlog.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.ironlog.app.ui.theme.IronLogTheme
import kotlinx.coroutines.tasks.await
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import android.os.Build.VERSION.SDK_INT

// Data Model matching Firestore 'catalog_exercises'
data class Exercise(
    val id: String = "",
    val name: String = "",
    val target: String = "",
    val gifUrl: String = ""
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            Log.e("IronLog", "Firebase init failed", e)
        }

        setContent {
            IronLogTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentUser by remember { mutableStateOf(com.google.firebase.ktx.Firebase.auth.currentUser) }
                    
                    Log.d("IronLog", "MainActivity: currentUser is ${if (currentUser != null) "LOGGED IN: ${currentUser!!.uid}" else "NULL"}")

                    if (currentUser != null) {
                        MainScreen(
                            user = currentUser!!,
                            onLogout = {
                                com.google.firebase.ktx.Firebase.auth.signOut()
                                currentUser = null
                            }
                        )
                    } else {
                        LoginScreen(
                            onLoginSuccess = {
                                currentUser = com.google.firebase.ktx.Firebase.auth.currentUser
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(user: com.google.firebase.auth.FirebaseUser, onLogout: () -> Unit) {
    val userId = user.uid
    Log.d("IronLog", "MainScreen: Composing for user $userId")
    val navController = rememberNavController()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                NavigationBarItem(
                    icon = { Text("🏋️") }, // Placeholder icon
                    label = { Text("Registry") },
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        navController.navigate("registry") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Text("📅") }, // Placeholder icon
                    label = { Text("History") },
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        navController.navigate("history") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Text("👤") }, // Placeholder icon
                    label = { Text("Account") },
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        navController.navigate("account") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "registry",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("registry") {
                ExerciseCatalogScreen(user = user, onLogout = onLogout)
            }
            composable("history") {
                HistoryScreen(userId = userId)
            }
            composable("account") {
                AccountScreen(user = user, onLogout = onLogout)
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun ExerciseCatalogScreen(user: com.google.firebase.auth.FirebaseUser, onLogout: () -> Unit) {
    val userId = user.uid
    Log.d("IronLog", "ExerciseCatalogScreen: Composing for user $userId")
    var exercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Workout Session State
    var isWorkoutActive by remember { mutableStateOf(false) }
    var currentWorkoutSets by remember { mutableStateOf<List<WorkoutSet>>(emptyList()) }
    var showFinishDialog by remember { mutableStateOf(false) }

    // Fetch data from Firestore (Real-time listener)
    fun loadData() {
        isLoading = true
        errorMessage = null
        val db = FirebaseFirestore.getInstance()
        
        Log.d("IronLog", "Loading exercises for user: $userId")
        Log.d("IronLog", "Firestore path: users/$userId/exercises")
        
        db.collection("users").document(userId).collection("exercises")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("IronLog", "Listen failed.", e)
                    errorMessage = e.message
                    isLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    exercises = snapshot.documents.map { doc ->
                        val gifUrl = doc.getString("gifUrl") ?: ""
                        Log.d("IronLog", "Exercise: ${doc.getString("name")}, GIF URL: $gifUrl")
                        Exercise(
                            id = doc.id,
                            name = doc.getString("name") ?: "Unknown",
                            target = doc.getString("target") ?: "Unknown",
                            gifUrl = gifUrl
                        )
                    }
                    isLoading = false
                } else {
                    isLoading = false
                }
            }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isWorkoutActive) {
                        if (currentWorkoutSets.isNotEmpty()) {
                            showFinishDialog = true
                        } else {
                            // Cancel workout if empty? Or just stop.
                            isWorkoutActive = false
                        }
                    } else {
                        isWorkoutActive = true
                    }
                },
                containerColor = if (isWorkoutActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                contentColor = Color.White
            ) {
                if (isWorkoutActive) {
                    // Using a generic save icon (diskette) or checkmark
                    Text("💾", fontSize = 24.sp) 
                } else {
                    // Plus icon
                    Text("➕", fontSize = 24.sp)
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                // Error State
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error loading data", color = MaterialTheme.colorScheme.error)
                    Button(onClick = { loadData() }) { Text("Retry") }
                }
            } else if (exercises.isEmpty()) {
                // Empty State
                 Column(
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No exercises found", style = MaterialTheme.typography.titleMedium)
                    Text("Add exercises to your library first.", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp, top = 16.dp, start = 16.dp, end = 16.dp), // Padding for FAB
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Current Session View
                    if (isWorkoutActive) {
                        item {
                            CurrentSessionView(
                                sets = currentWorkoutSets,
                                onRemoveSet = { setToRemove ->
                                    currentWorkoutSets = currentWorkoutSets - setToRemove
                                },
                                onFinish = {
                                    if (currentWorkoutSets.isNotEmpty()) {
                                        showFinishDialog = true
                                    } else {
                                        isWorkoutActive = false
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    item {
                        Text(
                            text = "My Exercises",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    items(exercises) { exercise ->
                        ExerciseItem(exercise = exercise, onClick = { selectedExercise = exercise })
                    }
                }
            }

            // Log Set Dialog
            selectedExercise?.let { exercise ->
                LogSetDialog(
                    exercise = exercise,
                    onDismiss = { selectedExercise = null },
                    onSave = { weight, reps ->
                        if (!isWorkoutActive) {
                            // Auto-start workout if not active
                            isWorkoutActive = true
                        }
                        
                        val newSet = WorkoutSet(
                            exerciseId = exercise.id,
                            exerciseName = exercise.name,
                            weight = weight.toDoubleOrNull() ?: 0.0,
                            reps = reps.toIntOrNull() ?: 0
                        )
                        
                        currentWorkoutSets = currentWorkoutSets + newSet
                        selectedExercise = null
                    }
                )
            }
            
            // Finish Workout Dialog
            if (showFinishDialog) {
                FinishWorkoutDialog(
                    onDismiss = { showFinishDialog = false },
                    onSave = { workoutName ->
                        val db = FirebaseFirestore.getInstance()
                        val workout = Workout(
                            userId = user.uid,
                            name = workoutName,
                            timestamp = com.google.firebase.Timestamp.now(),
                            sets = currentWorkoutSets
                        )
                        
                        db.collection("workouts")
                            .add(workout)
                            .addOnSuccessListener { 
                                isWorkoutActive = false
                                currentWorkoutSets = emptyList()
                                showFinishDialog = false
                            }
                            .addOnFailureListener { e ->
                                Log.e("IronLog", "Error saving workout", e)
                            }
                    }
                )
            }
        }
    }
}

@Composable
fun UserProfileRow(user: com.google.firebase.auth.FirebaseUser, onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user.photoUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Profile Picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.displayName ?: "User",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = user.email ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
        
        // Logout
        IconButton(onClick = onLogout) {
            Icon(
                painter = painterResource(android.R.drawable.ic_lock_power_off), // Using generic power icon
                contentDescription = "Logout",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun AccountScreen(user: com.google.firebase.auth.FirebaseUser, onLogout: () -> Unit) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // Avatar
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user.photoUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Profile Picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Name
        Text(
            text = user.displayName ?: "User",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Email
        Text(
            text = user.email ?: "",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Switch Account Button
        OutlinedButton(
            onClick = {
                // Sign out from both Firebase and Google Sign-In to force account picker
                Firebase.auth.signOut()
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                googleSignInClient.signOut().addOnCompleteListener {
                    onLogout() // This will show the login screen
                }
            },
            modifier = Modifier.fillMaxWidth(0.7f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                painter = painterResource(android.R.drawable.ic_menu_revert),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Switch Account", fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Logout Button
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Icon(
                painter = painterResource(android.R.drawable.ic_lock_power_off),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout", fontSize = 16.sp)
        }
    }
}

@Composable
fun CurrentSessionView(
    sets: List<WorkoutSet>,
    onRemoveSet: (WorkoutSet) -> Unit,
    onFinish: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Current Session",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${sets.size} sets logged",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = onFinish,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Finish")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (sets.isEmpty()) {
                Text(
                    text = "Tap an exercise below to add sets.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                // Group sets by exercise
                val groupedSets = sets.groupBy { it.exerciseName }
                
                groupedSets.forEach { (exerciseName, exerciseSets) ->
                    Text(
                        text = exerciseName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    
                    exerciseSets.forEach { set ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${set.weight}kg x ${set.reps}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            IconButton(
                                onClick = { onRemoveSet(set) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(android.R.drawable.ic_menu_close_clear_cancel),
                                    contentDescription = "Remove Set",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinishWorkoutDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Finish Workout",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Workout Name (e.g., Leg Day)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(name.ifBlank { "Workout" }) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Save Workout", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseItem(exercise: Exercise, onClick: () -> Unit) {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image/GIF
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(exercise.gifUrl)
                    .addHeader("x-rapidapi-key", "4e0d7d2894msh3937f4d160a4429p112a03jsn696c1a82a08a")
                    .addHeader("x-rapidapi-host", "exercisedb.p.rapidapi.com")
                    .crossfade(true)
                    .listener(
                        onError = { _, result ->
                            Log.e("IronLog", "Failed to load image: ${exercise.gifUrl}", result.throwable)
                        },
                        onSuccess = { _, _ ->
                            Log.d("IronLog", "Successfully loaded image: ${exercise.gifUrl}")
                        }
                    )
                    .build(),
                imageLoader = imageLoader,
                contentDescription = exercise.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = exercise.target.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun LogSetDialog(
    exercise: Exercise,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Log Set",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Weight Input
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Reps Input
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text("Reps") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onSave(weight, reps) }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(weight, reps) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Save Set", color = Color.White)
                    }
                }
            }
        }
    }
    
    // Auto-focus the weight field when dialog opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
