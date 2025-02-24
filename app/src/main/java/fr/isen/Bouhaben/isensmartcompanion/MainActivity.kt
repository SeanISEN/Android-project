package fr.isen.Bouhaben.isensmartcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    var question by remember { mutableStateOf(TextFieldValue()) }
    var response by remember { mutableStateOf("Ask me anything!") }
    var savedQuestion by remember { mutableStateOf("") }
    var isButtonClicked by remember { mutableStateOf(false) }
    var showQuestionSubmitted by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.isen_logo),
                contentDescription = "ISEN Logo",
                modifier = Modifier
                    .size(100.dp)
                    .padding(8.dp)
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ISEN BOT",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFFD32F2F)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = question,
                onValueChange = { question = it },
                label = { Text("Ask a question", color = Color(0xFFB71C1C)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD32F2F),
                    unfocusedBorderColor = Color(0xFFB71C1C),
                    cursorColor = Color(0xFFD32F2F),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isButtonClicked = !isButtonClicked
                    if (question.text.isNotBlank()) {
                        savedQuestion = question.text
                        response = "You asked: ${question.text}"
                        question = TextFieldValue("")
                        showQuestionSubmitted = true
                    } else {
                        response = "Please enter a question."
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                )
            ) {
                Text(text = if (showQuestionSubmitted) "Question Submitted" else "Send")
            }

            LaunchedEffect(showQuestionSubmitted) {
                if (showQuestionSubmitted) {
                    delay(3000)
                    showQuestionSubmitted = false
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = response, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(16.dp))

        }
    }
}


@Composable
fun EventScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Event Page", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun HistoryScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "History Page", style = MaterialTheme.typography.headlineMedium)
    }
}

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

@Preview(showBackground = true)
@Composable
fun AssistantUIPreview() {
    ISENSmartCompanionTheme {
        val navController = rememberNavController()
        MainScreen(navController)
    }
}