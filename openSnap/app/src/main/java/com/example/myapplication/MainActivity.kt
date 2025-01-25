package com.example.myapplication

import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
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
import com.example.myapplication.notifications.NotificationHelper
import com.google.accompanist.permissions.isGranted


class MainActivity : ComponentActivity() {
    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataStoreManager = DataStoreManager(this)
        notificationHelper = NotificationHelper(this)

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

        setContent {
            val isDarkTheme = remember { mutableStateOf(true) }
            val navController = rememberNavController()
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            val hiddenBottomBar: List<String> = listOf("login_screen", "register_screen")
            val isAuthenticated = remember { mutableStateOf(false) }

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
                            loginScreen(navController, notificationHelper)
                        }
                        composable("register_screen") {
                            registerScreen(navController, dataStoreManager)
                        }
                        composable("home_screen") {
                            Middleware(
                                isAuthenticated = isAuthenticated.value,
                                undirect = { navController.navigate("login_screen") }
                            ) {
                                homeScreen(navController)
                            }
                        }
                        composable("messages_screen") {
                            Middleware(
                                isAuthenticated = isAuthenticated.value,
                                undirect = { navController.navigate("login_screen") }
                            ) {
                                messageScreen(navController, isDarkTheme, sampleMessages)
                            }
                        }
                        composable("settings_screen") {
                            Middleware(
                                isAuthenticated = isAuthenticated.value,
                                undirect = { navController.navigate("login_screen") }
                            ) {
                                settingsScreen(navController, notificationHelper)
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
fun loginScreen(navController: NavController, notificationHelper: NotificationHelper) {
    BackPressHandler()

    val postNotificationPermissionsState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("Camera", "Granted")
        } else {
            Log.d("Camera", "Failed")
        }
    }

    LaunchedEffect(postNotificationPermissionsState) {
        if (postNotificationPermissionsState.status.isGranted) {
            Log.d("LaunnchedEffect Perm notifications", "ELse")
        } else {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column {
        Text("Hello login screen")
        Button(
            onClick = {
                navController.navigate("home_screen") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            }
        ) {
            Text("Login")
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
fun registerScreen(navController: NavController, dataStoreManager: DataStoreManager) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val selectedImageUri = remember { mutableStateOf<Uri?>(null) }
    val selectedImageVal = remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val dataStoreManager = DataStoreManager(context)

    BackPressHandler()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Hello Register Screen", modifier = Modifier.padding(bottom = 16.dp, top= 16.dp))
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

        imagePicker(selectedImageUri = selectedImageUri.value,
            onImageSelected = { uri ->
                Log.d("ImagePickerExample", "Selected Image URI: $uri , $selectedImageUri")
                val imagePath = saveImageToLocalStorage(uri, context)
                selectedImageUri.value = Uri.parse(imagePath)
                selectedImageVal.value = imagePath
                Log.d("ImagePickerExample", "Saved Image Path: $imagePath")
            }
        )
        Button(onClick = {
            val user = userStore(
                username = username,
                password = password,
                profile_picture = selectedImageVal.value ?: ""
            )

            CoroutineScope(Dispatchers.IO).launch {
                dataStoreManager.saveToDataStore(user)
            }

            navController.navigate("home_screen")
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
    notificationHelper: NotificationHelper,
) {
    val context = LocalContext.current
    val dataStoreManager = DataStoreManager(context)
    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val profilePicture = remember { mutableStateOf("") }


    LaunchedEffect(Unit) {
        dataStoreManager.getFromDataStore().collect { userStore ->
            username.value = userStore.username
            password.value = userStore.password
            profilePicture.value = userStore.profile_picture
        }
    }
    Column {
        Text("Hello settings")
        Button(onClick = {
            navController.navigate("login_screen") {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true }
        }
        ) {
            Text("Sign out")
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