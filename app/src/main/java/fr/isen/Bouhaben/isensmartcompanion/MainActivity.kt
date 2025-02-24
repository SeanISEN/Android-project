package fr.isen.Bouhaben.isensmartcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import fr.isen.Bouhaben.isensmartcompanion.ui.theme.ISENSmartCompanionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ISENSmartCompanionTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFFFF5F5)
    ) { innerPadding ->
        AssistantUI(modifier = Modifier.padding(innerPadding))
    }
}

@Composable
fun AssistantUI(modifier: Modifier = Modifier) {
    var question by remember { mutableStateOf(TextFieldValue()) }
    var response by remember { mutableStateOf("Ask me anything!") }
    var savedQuestion by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Row with Logo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp), // Add padding below the logo
            horizontalArrangement = Arrangement.Start,
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

        // Main Content Column
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

            // Input Field
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

            // Button to Send
            Button(
                onClick = {
                    if (question.text.isNotBlank()) {
                        savedQuestion = question.text
                        response = "Question Submitted"
                        question = TextFieldValue("")
                    } else {
                        response = "Please enter a question."
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                )
            ) {
                Text(text = "Send")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Response Text
            Text(text = response, color = Color.DarkGray)

            Spacer(modifier = Modifier.height(16.dp))

            // Display Saved Question
            if (savedQuestion.isNotBlank()) {
                Text(text = "Previous question: $savedQuestion", color = Color.Gray)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AssistantUIPreview() {
    ISENSmartCompanionTheme {
        MainScreen()
    }
}