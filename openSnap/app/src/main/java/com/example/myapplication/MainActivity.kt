package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.ComposeTutorialTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Surface
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sampleMessages = listOf(
            Message("Alice", "Hello!"),
            Message("Bob", "Hello!"),
            Message("Alice", "React if more fluent :/"),
            Message("Alice", "But it's not too bad, I miss hot-reload though"),
            Message("Alice", "45"),
            Message("Alice", "34"),
            Message("Alice", "32"),
            Message("Alice", "23"),
            Message("Alice", "123"),
            Message("Alice", "123"),
            Message("Alice", "123"),
            Message("Alice", "123"),
            Message("Alice", "125"),
        )


        // https://github.com/KaushalVasava/JetPackCompose_Basic/blob/navigate-back-with-result/app/src/main/java/com/lahsuak/apps/jetpackcomposebasic/MainActivity.kt
        setContent {
            val isDarkTheme = remember { mutableStateOf(true) }
            val navController = rememberNavController()
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            val hiddenBottomBar: List<String> = listOf("login_screen")

            ComposeTutorialTheme(darkTheme = isDarkTheme.value) {
                Scaffold(
                    bottomBar = {
                        if (currentRoute !in hiddenBottomBar) {
                            BottomAppBar(
                                modifier = Modifier.height(24.dp),
                                content = {
                                    Text(
                                        text = "Bottom Bar",
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login_screen",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login_screen") {
                            loginScreen()
                        }
                        composable("messages_screen") {
                            messageScreen(isDarkTheme, sampleMessages)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun loginScreen() {
    Text("Hello")
}

@Composable
fun messageScreen(isDarkTheme: MutableState<Boolean>, sampleMessages: List<Message>) {
    Column {
        // At least 2 different styles of text (0.5p)
        Row {
            Text(
                text = "Messages",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                ),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            //At least one clickable element that creates a visible change (2p)
            IconButton(
                onClick = { isDarkTheme.value = !isDarkTheme.value },
                modifier = Modifier.padding(top = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Toggle theme",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        //Scrolling and enough visual elements to need it (1p)
        MessageList(messages = sampleMessages)
    }
}

data class Message(val author: String, val body: String)

@Composable
fun MessageCard(msg: Message, modifier: Modifier = Modifier) {
    Row(modifier = Modifier.padding(all = 8.dp)) {
        // At least one custom image (1p)
        Image(
            painter = painterResource(R.drawable.dsc03221),
            contentDescription = "Contact profile picture",
            modifier = Modifier
                .padding(top = 16.dp)
                .size(40.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.padding(all = 8.dp)) {
            // At least 2 different styles of text (0.5p)
            Text(text = msg.author, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = msg.body)
        }
    }
}

@Composable
fun MessageList(messages: List<Message>, modifier: Modifier = Modifier) {
    // https://stackoverflow.com/questions/78523222/two-lazycolumn-on-one-screen-with-common-scroll
    LazyColumn(modifier = modifier.padding(all = 2.dp)) {
        items(messages) { message ->
            MessageCard(msg = message, modifier = Modifier.padding(bottom = 8.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMessageList() {
    val sampleMessages = listOf(
        Message("Alice", "Hello! How are you?"),
        Message("Bob", "I'm fine, thanks! What about you?"),
        Message("Charlie", "Jetpack Compose is awesome!"),
        Message("Daisy", "Don't forget to check the documentation.")
    )

    ComposeTutorialTheme() {
        Surface {
            MessageList(messages = sampleMessages)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMessageCard() {
    ComposeTutorialTheme() {
        Surface {
            MessageCard(
                msg = Message(
                    author = "John Doe",
                    body = "This is a sample message to preview the MessageCard."
                )
            )
        }
    }
}