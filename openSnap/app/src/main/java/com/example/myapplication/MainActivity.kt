package com.example.myapplication

import android.os.Bundle
import android.util.Log
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
import com.example.myapplication.ui.theme.ComposeTutorialTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Star
import com.example.myapplication.notifications.NotificationHelper
import com.example.myapplication.services.AuthResponse
import com.example.myapplication.services.AuthStoreManager
import com.example.myapplication.services.NewUser
import com.example.myapplication.services.authWithPassword
import com.example.myapplication.services.createUser
import com.google.accompanist.permissions.isGranted


class MainActivity : ComponentActivity() {
    private lateinit var authStoreManager: AuthStoreManager
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // https://pocketbase.io/docs/api-records/#verification

        authStoreManager = AuthStoreManager(this)
        notificationHelper = NotificationHelper(this)

        val sampleMessages = listOf(
            Message("Alice", "Hello!"),
            Message("Bob", "Hello!"),
            Message("Alice", "React is more fluent :/"),
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

        setContent {
            val isDarkTheme = remember { mutableStateOf(true) }
            val navController = rememberNavController()
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            val hiddenBottomBar: List<String> = listOf("login_screen", "register_screen")
            val authResponse = remember { mutableStateOf<AuthResponse?>(null) }

            ComposeTutorialTheme(darkTheme = isDarkTheme.value) {
                Scaffold(
                    bottomBar = {
                        if (currentRoute !in hiddenBottomBar) {
                            BottomAppBar(
                                modifier = Modifier.height(48.dp),
                                content = {
                                    Row (horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()){
                                        IconButton(onClick = { navController.navigate("messages_screen")}, modifier = Modifier.padding(end = 28.dp) ) {
                                            Icon(imageVector = Icons.Default.Face, contentDescription = "Go to Messages", modifier = Modifier.size(36.dp))
                                        }
                                        IconButton(onClick = {
                                            navController.navigate("home_screen") {
                                                popUpTo("home_screen") { inclusive = true }
                                            }
                                        }, modifier = Modifier.padding(end = 28.dp)) {
                                            Icon(imageVector = Icons.Default.Home, contentDescription = "Go to Home", modifier = Modifier.size(36.dp))
                                        }
                                        IconButton(onClick = { navController.navigate("settings_screen")}, modifier = Modifier.padding(end = 28.dp)) {
                                            Icon(imageVector = Icons.Default.Settings, contentDescription = "Go to Settings", modifier = Modifier.size(36.dp))
                                        }
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    // Wanted to keep it simple so very loosely based on this
                    // https://auth0.com/blog/android-authentication-jetpack-compose-part-1/
                    // https://medium.com/@anna972606/navigation-in-jetpack-compose-p2-c24e1f145372
                    // Basic idea from NextJS where all routes are first intercepted with middleware hence 'Middleware' component.
                    // Proper auth is done in the backend - never trust the frontend - Pocketbase has RLS, row-level-security to handle user information safely.
                    NavHost(
                        navController = navController,
                        startDestination = "login_screen",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login_screen") {
                            loginScreen(navController, authResponse, authStoreManager, notificationHelper)
                        }
                        composable("register_screen") {
                            registerScreen(navController, authResponse, authStoreManager, notificationHelper)
                        }
                        composable("home_screen") {
                            Middleware(
                                isAuthenticated = isAutenticated(authResponse),
                                undirect = { navController.navigate("login_screen") }
                            ) {
                                homeScreen(navController)
                            }
                        }
                        composable("messages_screen") {
                            Middleware(
                                isAuthenticated = isAutenticated(authResponse),
                                undirect = { navController.navigate("login_screen") }
                            ) {
                                messageScreen(navController, isDarkTheme, sampleMessages)
                            }
                        }
                        composable("settings_screen") {
                            Middleware(
                                isAuthenticated = isAutenticated(authResponse),
                                undirect = { navController.navigate("login_screen") }
                            ) {
                                settingsScreen(navController, authResponse, authStoreManager, notificationHelper, isDarkTheme)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun loginScreen(
    navController: NavController,
    authResponseState: MutableState<AuthResponse?>,
    authStoreManager: AuthStoreManager,
    notificationHelper1: NotificationHelper
) {
    BackPressHandler()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally)
    {
        Text("Login screen")
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Enter your email") },
            modifier = Modifier.fillMaxWidth(0.6f)
        )
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Enter your password") },
            modifier = Modifier.fillMaxWidth(0.6f)
        )
        Button(onClick = {
            CoroutineScope(Dispatchers.Main).launch {
                val authResponse = authWithPassword(email, password)
                Log.d("HERE", "Login TRY: ${authResponse.toString()}")
                if (isAutenticated(authResponse)) {
                    authResponseState.value = authResponse
                    navController.navigate("home_screen")
                } else{
                    Log.d("FAILED", "STAY")
                }
            }
        }) {
            Text("Submit")
        }
        Button(
            onClick = {
                navController.navigate("register_screen") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            }
        ) {
            Text("Sign up instead!")
        }
    }
}

@Composable
fun registerScreen(
    navController: NavController,
    authResponseState: MutableState<AuthResponse?>,
    authStoreManager: AuthStoreManager,
    notificationHelper: NotificationHelper, ) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    BackPressHandler()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Hello Register Screen", modifier = Modifier.padding(bottom = 16.dp, top= 16.dp))
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Enter your email") },
            modifier = Modifier.fillMaxWidth(0.6f)
        )
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Enter your username") },
            modifier = Modifier.fillMaxWidth(0.6f)
        )
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Enter your password") },
            modifier = Modifier.fillMaxWidth(0.6f)
        )

        Button(onClick = {
            CoroutineScope(Dispatchers.Main).launch {
                createUser(NewUser(email, password, password, username))
                val authResponse = authWithPassword(email, password)
                Log.d("HERE", "NEW USER CREATED ${authResponse.toString()}")
                if (isAutenticated(authResponse)) {
                    authResponseState.value = authResponse
                    navController.navigate("home_screen")
                } else{
                    Log.d("FAILED", "STAY")
                }

            }
        }) {
            Text("Submit")
        }
    }
}


@Composable
fun homeScreen(navController: NavController) {
    Column {
        Text("Hello home screen")
    }
}

@Composable
fun settingsScreen(
    navController: NavController,
    authResponse: MutableState<AuthResponse?>,
    authStoreManager: AuthStoreManager,
    notificationHelper: NotificationHelper,
    isDarkTheme: MutableState<Boolean>,
) {
    val username = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        authStoreManager.getFromDataStore().collect { userStore ->
            username.value = userStore.record.email
        }
    }

    Column {
        Text("Settings")
        Text("Email: ${authResponse.value?.record?.email}")
        Button(onClick = {
            authResponse.value = null
            navController.navigate("login_screen") {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true }
        }
        ) {
            Text("Sign out")
        }
        IconButton(
            onClick = { isDarkTheme.value = !isDarkTheme.value },
            modifier = Modifier.padding(top = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Toggle theme",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun messageScreen(navController: NavController, isDarkTheme: MutableState<Boolean>, sampleMessages: List<Message>) {
    Column {
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
        MessageList(messages = sampleMessages)
    }
}

data class Message(val author: String, val body: String)

@Composable
fun MessageCard(msg: Message, modifier: Modifier = Modifier) {
    Row(modifier = Modifier.padding(all = 8.dp)) {
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
            Text(text = msg.author, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = msg.body)
        }
    }
}

@Composable
fun MessageList(messages: List<Message>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.padding(all = 2.dp)) {
        items(messages) { message ->
            MessageCard(msg = message, modifier = Modifier.padding(bottom = 8.dp))
        }
    }
}

// Notifications
// Camera
// Chatroom
// Load from storage