//------------------------------------Packages------------------------------------
package fr.isen.Bouhaben.isensmartcompanion



//------------------------------------Imports------------------------------------
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import fr.isen.Bouhaben.isensmartcompanion.ui.theme.ISENSmartCompanionTheme
import kotlinx.coroutines.delay
import kotlinx.parcelize.Parcelize
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET




//------------------------------------Structures------------------------------------
@Parcelize
data class Event(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val category: String
) : Parcelable



//------------------------------------Internet------------------------------------
interface EventApiService {
    @GET("events.json") // Endpoint from Firebase
    suspend fun getEvents(): List<Event>
}

object RetrofitInstance {
    private const val BASE_URL = "https://isen-smart-companion-default-rtdb.europe-west1.firebasedatabase.app/"

    val api: EventApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EventApiService::class.java)
    }
}



//------------------------------------HOME------------------------------------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ISENSmartCompanionTheme {
                val navController = rememberNavController()
                MainScreen(navController)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AssistantUIPreview() {
    ISENSmartCompanionTheme {
        val navController = rememberNavController()
        MainScreen(navController)
    }
}

@Composable
fun MainScreen(navController: NavHostController) {
    val bottomPadding = with(LocalDensity.current) {
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFFFF5F5),
        bottomBar = {
            BottomMenuBar(navController, bottomPadding)
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = "home", Modifier.padding(innerPadding)) {
            composable("home") { AssistantUI() }
            composable("event") { EventScreen() }
            composable("history") { HistoryScreen() }
        }
    }
}

@Composable
fun AssistantUI(modifier: Modifier = Modifier) {
    var userInput by remember { mutableStateOf(TextFieldValue()) }
    var chatMessages by remember { mutableStateOf<List<Pair<String, Boolean>>>(emptyList()) }

    // Scroll state for LazyColumn
    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Chat Header with ISEN Logo on the Top-Right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween, // Align text left & image right
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ISEN BOT",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFFD32F2F)
            )

            // ISEN Logo
            Image(
                painter = painterResource(id = R.drawable.isen_logo), // Ensure the correct name
                contentDescription = "ISEN Logo",
                modifier = Modifier
                    .size(80.dp) // Adjust size if needed
                    .padding(8.dp)
            )
        }

        // Chat History (Scrollable, Auto-Scroll Enabled)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState // Attach scroll state
        ) {
            items(chatMessages) { messagePair ->
                val (message, isUser) = messagePair
                ChatBubble(message = message, isUser = isUser)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // **✅ Auto-scroll effect placed outside onClick**
        LaunchedEffect(chatMessages.size) {
            if (chatMessages.isNotEmpty()) {
                listState.animateScrollToItem(chatMessages.size - 1)
            }
        }

        // User Input Box & Send Button (Pinned at Bottom)
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

            Button(
                onClick = {
                    if (userInput.text.isNotBlank()) {
                        val newMessages = chatMessages + Pair(userInput.text, true) // User message
                        userInput = TextFieldValue("")

                        chatMessages = newMessages + Pair("...", false) // Simulate AI response
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


@Composable
fun ChatBubble(message: String, isUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isUser) Color(0xFFD32F2F) else Color(0xFFFFE5E5),
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




//------------------------------------EVENT------------------------------------
class EventDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Retrieve the passed event object
        val event = intent.getParcelableExtra<Event>("event")

        setContent {
            ISENSmartCompanionTheme {
                if (event != null) {
                    EventDetailScreen(event)
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Event not found", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        }
    }
}


@Composable
fun EventScreen() {
    val context = LocalContext.current
    var events by remember { mutableStateOf<List<Event>?>(null) }
    var isVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) } // Store error message

    // Fetch events when screen is first displayed
    LaunchedEffect(Unit) {
        isVisible = true
        try {
            val fetchedEvents = RetrofitInstance.api.getEvents()
            events = fetchedEvents
            errorMessage = null // Reset error on success
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = "Failed to load events: ${e.message}" // Store error message
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
        // Title animation
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(initialOffsetY = { -50 })
        ) {
            Text(
                text = "Upcoming Events",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFFD32F2F),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFFD32F2F))
        } else if (errorMessage != null) {
            // Display error message
            Text(text = errorMessage ?: "Unknown error", color = Color.Red)
        } else if (events.isNullOrEmpty()) {
            // No events found in response
            Text(text = "No events available", color = Color.Gray)
        } else {
            // Display events
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(events!!) { index, event ->
                    val backgroundColor = if (index % 2 == 0) Color(0xFFFFCCCC) else Color(0xFFFFE5E5) // Alternate colors

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { 100 })
                    ) {
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
                                    color = Color(0xFFD32F2F)
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
                }
            }
        }
    }
}

@Composable
fun EventDetailScreen(event: Event) {
    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(false) }

    // Start animation when the screen is displayed
    LaunchedEffect(Unit) { isVisible = true }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Section with Animation
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFD32F2F))
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

        // Event Details Card with Expand Animation
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

        // Back Button with Fade & Slide Animation
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(700)) + slideInVertically(initialOffsetY = { it / 2 })
        ) {
            Button(
                onClick = {
                    (context as? ComponentActivity)?.finish()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(text = "Back to Events")
            }
        }
    }
}

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



//------------------------------------HISTORY------------------------------------
@Composable
fun HistoryScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "History Page", style = MaterialTheme.typography.headlineMedium)
    }
}



//------------------------------------MENU------------------------------------
@Composable
fun BottomMenuBar(navController: NavHostController, bottomPadding: Dp) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            AnimatedVisibility(
                visible = isMenuExpanded,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
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
                        Image(
                            painter = painterResource(id = R.drawable.home),
                            contentDescription = "Home",
                            modifier = Modifier
                                .size(50.dp)
                                .clickable {
                                    navController.navigate("home")
                                    isMenuExpanded = false
                                }
                        )
                        Image(
                            painter = painterResource(id = R.drawable.event),
                            contentDescription = "Event",
                            modifier = Modifier
                                .size(50.dp)
                                .clickable {
                                    navController.navigate("event")
                                    isMenuExpanded = false
                                }
                        )
                        Image(
                            painter = painterResource(id = R.drawable.history),
                            contentDescription = "History",
                            modifier = Modifier
                                .size(50.dp)
                                .clickable {
                                    navController.navigate("history")
                                    isMenuExpanded = false
                                }
                        )
                    }
                }
            }
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
