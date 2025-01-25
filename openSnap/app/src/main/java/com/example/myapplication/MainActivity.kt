package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import android.Manifest
import android.content.Context
import android.content.IntentFilter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.accompanist.permissions.isGranted


class MainActivity : ComponentActivity() {
    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var notificationHelper: NotificationHelper
    private val handler = Handler(Looper.getMainLooper())

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

        // https://github.com/KaushalVasava/JetPackCompose_Basic/blob/navigate-back-with-result/app/src/main/java/com/lahsuak/apps/jetpackcomposebasic/MainActivity.kt
        setContent {
            val isDarkTheme = remember { mutableStateOf(true) }
            val navController = rememberNavController()
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            val hiddenBottomBar: List<String> = listOf("login_screen", "register_screen")

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
                    // At least 2 distinct views that can be moved between using buttons you created (5p)
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
                            homeScreen(navController)
                        }
                        composable("messages_screen") {
                            messageScreen(navController, isDarkTheme, sampleMessages)
                        }
                        composable("settings_screen") {
                            settingsScreen(navController, handler, notificationHelper)
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
    // but circular navigation should be prevented! (5p)
    // https://foso.github.io/Jetpack-Compose-Playground/activity/backhandler/
    // https://stackoverflow.com/questions/67401294/jetpack-compose-close-application-by-button
    BackPressHandler()

    // https://medium.com/@rzmeneghelo/how-to-request-permissions-in-jetpack-compose-a-step-by-step-guide-7ce4b7782bd7
    val postNotificationPermissionsState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    val cameraPermissionsState = rememberPermissionState(Manifest.permission.CAMERA)

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

    LaunchedEffect(cameraPermissionsState) {
        if (cameraPermissionsState.status.isGranted) {
            Log.d("LaunnchedEffect Perm Camera", "ELse")
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column {
        Text("Hello login screen")
        Button(
            onClick = {
                Log.d("HELLO NOTIFI", "SD")
                notificationHelper.showBasicNotification()
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
        // Input and display at least some text and a picture in one view (5p)
        // https://stackoverflow.com/questions/78110240/how-to-pass-a-value-from-a-composable-function-to-another-composable-function-an
        imagePicker(selectedImageUri = selectedImageUri.value,
            onImageSelected = { uri ->
                Log.d("ImagePickerExample", "Selected Image URI: $uri , $selectedImageUri")
                val imagePath = saveImageToLocalStorage(uri, context)
                selectedImageUri.value = Uri.parse(imagePath)
                selectedImageVal.value = imagePath
                Log.d("ImagePickerExample", "Saved Image Path: $imagePath")
            }
        )
        // Display given text and picture in another view and retain these changes when restarting application (5p)
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
    handler: Handler,
    notificationHelper: NotificationHelper
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val dataStoreManager = DataStoreManager(context)
    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val profilePicture = remember { mutableStateOf("") }
    val stopFlag = remember { mutableStateOf(false) }
    // While the app is not on foreground (1p)
    var task = object : Runnable {
        override fun run() {
            if (stopFlag.value) {
                Log.d("Reminder", "Stopped TASK $stopFlag")
                return
            }
            // Trigger a notification (2p)
            notificationHelper.showStopNotification()
            handler.postDelayed(this, 10000)
        }
    }


    // https://stackoverflow.com/questions/70480709/what-is-the-useeffect-correspondent-in-android-compose-component
    LaunchedEffect(Unit) {
        dataStoreManager.getFromDataStore().collect { userStore ->
            username.value = userStore.username
            password.value = userStore.password
            profilePicture.value = userStore.profile_picture
        }
    }
    Column {
        Text("Hello settings")
        // but circular navigation should be prevented! (5p)
        // https://stackoverflow.com/questions/71789903/does-navoptionsbuilder-launchsingletop-work-with-nested-navigation-graphs-in-jet
        Button(onClick = {
            coroutineScope.launch {
                navController.navigate("login_screen") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
                dataStoreManager.clearDataStore()
                dataStoreManager.getFromDataStore().collect { userStore ->
                    username.value = userStore.username
                    password.value = userStore.password
                    profilePicture.value = userStore.profile_picture
                }
                Log.d("username AFTER", username.value)
            }
        }) {
            Text("Sign out")
        }

        Button(onClick = {
            val stopReceiver = StopNotificationReceiver(stopFlag)
            val intentFilter = IntentFilter("com.example.ACTION_STOP")
            context.registerReceiver(stopReceiver, intentFilter, Context.RECEIVER_EXPORTED)
            stopFlag.value = false
            handler.post(task)
        }) {
            Text("Start reminder")
        }

        Button(onClick = {
            handler.removeCallbacks(task)
        }) {
            Text("Stop reminder")
        }

        Text("username: ${username.value}")
        Text("password: ${password.value}")
        Text("Profile_picture: ${profilePicture.value}")
        if (profilePicture.value.isNotEmpty()) {
            val file = File(profilePicture.value)
            if (file.exists()) {
                Image(
                    painter = rememberAsyncImagePainter(file),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text("Profile picture not found.")
            }
        }
    }
}

@Composable
fun messageScreen(navController: NavController, isDarkTheme: MutableState<Boolean>, sampleMessages: List<Message>) {
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