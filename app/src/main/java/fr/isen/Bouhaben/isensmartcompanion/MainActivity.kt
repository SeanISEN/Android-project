// ------------------------------------ Package ------------------------------------
package fr.isen.Bouhaben.isensmartcompanion



// ------------------------------------ Imports ------------------------------------
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.ai.client.generativeai.GenerativeModel
import fr.isen.Bouhaben.isensmartcompanion.database.ChatDatabase
import fr.isen.Bouhaben.isensmartcompanion.database.ChatMessage
import fr.isen.Bouhaben.isensmartcompanion.ui.theme.ISENSmartCompanionTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.Calendar
import java.util.Date


// ------------------------------------ Structures ------------------------------------
/**
 * Represents an event entity used throughout the app.
 * This class is Parcelable, allowing it to be passed between activities.
 *
 * @property id Unique identifier of the event.
 * @property title Event title.
 * @property description Brief description of the event.
 * @property date Date when the event takes place.
 * @property location Event location.
 * @property category Category of the event.
 */
@Parcelize
data class Event(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val category: String
) : Parcelable



// ------------------------------------ Internet API ------------------------------------

/**
 * Retrofit API service to fetch events from Firebase.
 */
interface EventApiService {

    /**
     * Fetches the list of events from the remote database.
     *
     * @return List of [Event] objects.
     */
    @GET("events.json")
    suspend fun getEvents(): List<Event>
}

/**
 * Singleton object for managing the Retrofit instance.
 * Ensures a single instance of [Retrofit] is used across the application.
 */
object RetrofitInstance {

    // Base URL for Firebase Realtime Database
    private const val BASE_URL = "https://isen-smart-companion-default-rtdb.europe-west1.firebasedatabase.app/"

    /**
     * Lazy-initialized Retrofit instance with Gson converter.
     * Provides an implementation of [EventApiService] for network requests.
     */
    val api: EventApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // Set the base URL
            .addConverterFactory(GsonConverterFactory.create()) // Convert JSON response to Kotlin objects
            .build()
            .create(EventApiService::class.java)
    }
}



// ------------------------------------ AI Service ------------------------------------

/**
 * Singleton object responsible for handling AI interactions using Google's Generative AI model.
 */
object AiService {

    // Model name used for AI processing
    private const val MODEL_NAME = "gemini-1.5-flash"

    // API key retrieved from BuildConfig for security
    private val apiKey = BuildConfig.GOOGLE_AI_API_KEY

    /**
     * Instance of [GenerativeModel] initialized with the provided API key.
     */
    private val generativeModel = GenerativeModel(
        modelName = MODEL_NAME,
        apiKey = apiKey
    )

    /**
     * Generates an AI response for the given user input.
     * Saves the conversation (question & answer) to the local Room database.
     *
     * @param input The user input/question.
     * @param database The Room database instance for storing chat history.
     * @return The AI-generated response.
     */
    suspend fun getAiResponse(input: String, database: ChatDatabase): String {
        return try {
            // Generate AI response
            val response = generativeModel.generateContent(input)
            val aiResponse = response.text ?: "No response received"

            // Save the conversation to Room Database
            val message = ChatMessage(question = input, answer = aiResponse)
            database.chatMessageDao().insertMessage(message)

            // Return AI response
            aiResponse
        } catch (e: Exception) {
            // Handle API errors gracefully
            "Error: ${e.message}"
        }
    }
}



// ------------------------------------ HOME SCREEN ------------------------------------

/**
 * The main entry point of the application.
 * Handles navigation and initializes the Room database.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Initialize Room Database
        val database = ChatDatabase.getDatabase(this)

        setContent {
            ISENSmartCompanionTheme {
                val navController = rememberNavController()
                MainScreen(navController, database)
            }
        }
    }
}

/**
 * Preview function to visualize the Assistant UI in Android Studio.
 */
@Preview(showBackground = true)
@Composable
fun AssistantUIPreview() {
    ISENSmartCompanionTheme {
        val navController = rememberNavController()
        MainScreen(navController, database = TODO()) // TODO should be replaced with actual database instance in testing
    }
}

/**
 * MainScreen manages the navigation between different screens.
 *
 * @param navController The navigation controller for switching between screens.
 * @param database The Room database instance for chat history storage.
 */
@Composable
fun MainScreen(navController: NavHostController, database: ChatDatabase) {
    // Calculate bottom padding based on system navigation bars
    val bottomPadding = with(LocalDensity.current) {
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFFFF5F5), // Background color theme
        bottomBar = {
            BottomMenuBar(navController, bottomPadding)
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = "home",
            Modifier.padding(innerPadding)
        ) {
            composable("home") { AssistantUI(database) }
            composable("event") { EventScreen() }
            composable("history") { HistoryScreen(database) } // ✅ Pass database to history screen
        }
    }
}

/**
 * Composable function for the main Assistant UI where users can chat with ISEN Bot.
 *
 * @param database The Room database instance for storing conversation history.
 */
@Composable
fun AssistantUI(database: ChatDatabase) {
    var userInput by remember { mutableStateOf(TextFieldValue()) }
    var chatMessages by remember { mutableStateOf<List<Pair<String, Boolean>>>(emptyList()) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ✅ Header Section: ISEN Bot Title & Logo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ISEN BOT",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFFD32F2F) // ISEN Red Color
            )

            // ✅ ISEN Logo
            Image(
                painter = painterResource(id = R.drawable.isen_logo), // Ensure the image exists in resources
                contentDescription = "ISEN Logo",
                modifier = Modifier
                    .size(80.dp) // Adjust size if needed
                    .padding(8.dp)
            )
        }

        // ✅ Chat History Display
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState
        ) {
            items(chatMessages) { messagePair ->
                val (message, isUser) = messagePair
                ChatBubble(message = message, isUser = isUser)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // ✅ User Input & Send Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                label = { Text("Type your message") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD32F2F),
                    unfocusedBorderColor = Color(0xFFB71C1C),
                    cursorColor = Color(0xFFD32F2F)
                )
            )

            // ✅ Send Button
            Button(
                onClick = {
                    if (userInput.text.isNotBlank()) {
                        val newMessages = chatMessages + Pair(userInput.text, true)
                        chatMessages = newMessages
                        val inputText = userInput.text
                        userInput = TextFieldValue("")

                        // ✅ Fetch AI response and save to database
                        coroutineScope.launch {
                            val aiResponse = AiService.getAiResponse(inputText, database)
                            chatMessages = chatMessages + Pair(aiResponse, false)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                )
            ) {
                Text("Send")
            }
        }
    }
}

/**
 * Composable function representing a single chat bubble in the conversation.
 *
 * @param message The message content.
 * @param isUser Boolean flag to determine if the message is from the user (true) or AI (false).
 */
@Composable
fun ChatBubble(message: String, isUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isUser) Color(0xFFD32F2F) else Color(0xFFFFE5E5), // Red for user, light red for AI
                    shape = MaterialTheme.shapes.medium
                )
                .padding(12.dp)
                .widthIn(max = 280.dp) // ✅ Ensures a max width without breaking layout
        ) {
            Text(
                text = message,
                color = if (isUser) Color.White else Color.Black
            )
        }
    }
}



// ------------------------------------ EVENT SCREEN ------------------------------------

/**
 * EventDetailActivity: Displays details of a selected event.
 */
class EventDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Retrieve the passed event object from Intent
        val event = intent.getParcelableExtra<Event>("event")

        setContent {
            ISENSmartCompanionTheme {
                if (event != null) {
                    EventDetailScreen(event)
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Event not found",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Displays the list of upcoming events.
 * Fetches data from Firebase using Retrofit.
 */
@Composable
fun EventScreen() {
    val context = LocalContext.current
    var events by remember { mutableStateOf<List<Event>?>(null) }
    var isVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) } // Store error messages

    // Fetch events when screen is first displayed
    LaunchedEffect(Unit) {
        isVisible = true
        try {
            val fetchedEvents = RetrofitInstance.api.getEvents()
            events = fetchedEvents
            errorMessage = null // Reset error message on success
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = "Failed to load events: ${e.message}" // Display error
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ✅ Title Animation
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(initialOffsetY = { -50 })
        ) {
            Text(
                text = "Upcoming Events",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFFD32F2F), // ISEN Theme Red
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // ✅ Loading Indicator
        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFFD32F2F))
        }
        // ✅ Error Handling
        else if (errorMessage != null) {
            Text(text = errorMessage ?: "Unknown error", color = Color.Red)
        }
        // ✅ No Events Found
        else if (events.isNullOrEmpty()) {
            Text(text = "No events available", color = Color.Gray)
        }
        // ✅ Display Events List
        else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(events!!) { index, event ->
                    val backgroundColor =
                        if (index % 2 == 0) Color(0xFFFFCCCC) else Color(0xFFFFE5E5) // Alternate colors

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { 100 })
                    ) {
                        EventCard(event, backgroundColor, context)
                    }
                }
            }
        }
    }
}

/**
 * A single event card displaying event details.
 *
 * @param event The event data object.
 * @param backgroundColor Background color for alternating effect.
 * @param context The current context for navigation.
 */
@Composable
fun EventCard(event: Event, backgroundColor: Color, context: android.content.Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 8.dp)
            .clickable {
                val intent = Intent(context, EventDetailActivity::class.java)
                intent.putExtra("event", event)
                context.startActivity(intent)
            },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFD32F2F) // ISEN Red
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = event.date,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF880E4F)
            )
            Text(
                text = event.location,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF880E4F)
            )
        }
    }
}

/**
 * EventDetailScreen: Displays full details of a selected event.
 *
 * @param event The event data object.
 */
@Composable
fun EventDetailScreen(event: Event) {
    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(false) }

    // ✅ State to track if notifications are enabled
    val prefs = context.getSharedPreferences("event_prefs", Context.MODE_PRIVATE)
    var isNotified by remember { mutableStateOf(prefs.getBoolean(event.id, false)) }

    // ✅ Toggle notification state & save in preferences
    fun toggleNotification() {
        isNotified = !isNotified
        prefs.edit().putBoolean(event.id, isNotified).apply()
        if (isNotified) scheduleNotification(context, event)
    }

    // ✅ Start animation when the screen is displayed
    LaunchedEffect(Unit) { isVisible = true }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ✅ Header Section with Animation (Title Only)
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFD32F2F)) // ISEN Red
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Event Details Card with Expand Animation
        AnimatedVisibility(
            visible = isVisible,
            enter = expandVertically() + fadeIn()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE5E5)),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailRow("📅 Date", event.date)
                    DetailRow("📍 Location", event.location)
                    DetailRow("🏷 Category", event.category)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF880E4F)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ✅ Bottom Row: Back Button & Notification Bell
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(700)) + slideInVertically(initialOffsetY = { it / 2 })
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ✅ Back Button
                Button(
                    onClick = { (context as? ComponentActivity)?.finish() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Back to Events")
                }

                // ✅ Notification Bell with Animated Transition
                IconButton(
                    onClick = { toggleNotification() },
                    modifier = Modifier.size(64.dp) // 🔥 2x Size 🔥
                ) {
                    Crossfade(targetState = isNotified, animationSpec = tween(500)) { state ->
                        Image(
                            painter = painterResource(if (state) R.drawable.notifon else R.drawable.notifoff),
                            contentDescription = "Notification Toggle",
                            modifier = Modifier.size(64.dp) // 🔥 2x Size 🔥
                        )
                    }
                }
            }
        }
    }
}


fun getNotificationState(sharedPreferences: SharedPreferences, eventId: String): Boolean {
    return sharedPreferences.getBoolean(eventId, false)
}

fun saveNotificationState(sharedPreferences: SharedPreferences, eventId: String, isNotified: Boolean) {
    sharedPreferences.edit().putBoolean(eventId, isNotified).apply()
}

fun scheduleNotification(context: Context, event: Event) {
    val intent = Intent(context, NotificationHelper::class.java).apply {
        putExtra("event_title", event.title)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context, event.id.hashCode(), intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val triggerTime = Calendar.getInstance().timeInMillis + 10_000 // 10 seconds later

    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
}

/**
 * DetailRow: Displays a key-value pair in the EventDetailScreen.
 *
 * @param label The title (e.g., "📅 Date").
 * @param value The value (e.g., "March 21, 2025").
 */
@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFD32F2F),
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black
        )
    }
}



// ------------------------------------ HISTORY SCREEN ------------------------------------

/**
 * Displays the history of AI chat messages stored in the database.
 *
 * @param database The Room database instance containing chat messages.
 */
@Composable
fun HistoryScreen(database: ChatDatabase) {
    val chatMessages by database.chatMessageDao().getAllMessages().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    var isVisible by remember { mutableStateOf(false) } // Animation trigger

    // ✅ Start animation on load
    LaunchedEffect(Unit) { isVisible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ✅ Title Animation
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(initialOffsetY = { -50 })
        ) {
            Text(
                text = "Chat History",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFFD32F2F), // ISEN Red
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // ✅ Clear History Button (Visible only if messages exist)
        if (chatMessages.isNotEmpty()) {
            Button(
                onClick = { coroutineScope.launch { database.chatMessageDao().deleteAll() } },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Clear History")
            }
        }

        // ✅ No Messages Found
        if (chatMessages.isEmpty()) {
            Text(text = "No chat history available.", color = Color.Gray)
        }
        // ✅ Display Chat History List
        else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(chatMessages) { index, message ->
                    val backgroundColor =
                        if (index % 2 == 0) Color(0xFFFFCCCC) else Color(0xFFFFE5E5) // Alternate colors

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { 100 })
                    ) {
                        ChatHistoryCard(message, backgroundColor, database, coroutineScope)
                    }
                }
            }
        }
    }
}

/**
 * Displays a single chat history item in a Card layout.
 *
 * @param message The chat message object.
 * @param backgroundColor The background color for alternating effect.
 * @param database The Room database instance.
 * @param coroutineScope Coroutine scope for database operations.
 */
@Composable
fun ChatHistoryCard(
    message: ChatMessage,
    backgroundColor: Color,
    database: ChatDatabase,
    coroutineScope: CoroutineScope
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Question: ${message.question}",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFD32F2F) // ISEN Red
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Response: ${message.answer}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF880E4F)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Date: ${Date(message.timestamp)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            // ✅ Delete Single Message Button
            Button(
                onClick = {
                    coroutineScope.launch { database.chatMessageDao().deleteMessage(message) }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB71C1C),
                    contentColor = Color.White
                ),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Delete")
            }
        }
    }
}



// ------------------------------------ BOTTOM NAVIGATION MENU ------------------------------------

/**
 * A bottom navigation bar with expandable menu options.
 *
 * @param navController The navigation controller to manage screen transitions.
 * @param bottomPadding The padding required to accommodate system UI elements.
 */
@Composable
fun BottomMenuBar(navController: NavHostController, bottomPadding: Dp) {
    var isMenuExpanded by remember { mutableStateOf(false) } // Track menu state

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ✅ Expandable Menu Section
            AnimatedVisibility(
                visible = isMenuExpanded,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                MenuOptions(navController) { isMenuExpanded = false }
            }

            // ✅ Bottom Menu Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = bottomPadding),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { isMenuExpanded = !isMenuExpanded }) {
                    Image(
                        painter = painterResource(id = R.drawable.menu_icon),
                        contentDescription = "Menu",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

/**
 * Displays the menu options with navigation buttons.
 *
 * @param navController The navigation controller for handling navigation.
 * @param onMenuClose Callback to close the menu after selecting an option.
 */
@Composable
fun MenuOptions(navController: NavHostController, onMenuClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MenuItem(R.drawable.home, "Home") {
                navController.navigate("home")
                onMenuClose()
            }
            MenuItem(R.drawable.event, "Event") {
                navController.navigate("event")
                onMenuClose()
            }
            MenuItem(R.drawable.history, "History") {
                navController.navigate("history")
                onMenuClose()
            }
        }
    }
}

/**
 * A single menu item representing a navigation option.
 *
 * @param iconResId The resource ID for the icon.
 * @param description The content description for accessibility.
 * @param onClick Action to perform when clicked.
 */
@Composable
fun MenuItem(iconResId: Int, description: String, onClick: () -> Unit) {
    Image(
        painter = painterResource(id = iconResId),
        contentDescription = description,
        modifier = Modifier
            .size(50.dp)
            .clickable(onClick = onClick)
    )
}