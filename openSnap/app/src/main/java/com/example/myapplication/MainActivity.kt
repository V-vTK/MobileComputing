package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.RectangleShape
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.notifications.NotificationHelper
import com.example.myapplication.services.AuthResponse
import com.example.myapplication.services.AuthStoreManager
import com.example.myapplication.services.Message
import com.example.myapplication.services.NewUser
import com.example.myapplication.services.SettingsStore
import com.example.myapplication.services.authWithPassword
import com.example.myapplication.services.createUser
import com.example.myapplication.services.getImage
import com.example.myapplication.services.getImageURL
import com.example.myapplication.services.getMessages
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.flow.first
import android.Manifest
import androidx.camera.compose.CameraXViewfinder
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale


class MainActivity : ComponentActivity() {
    private lateinit var authStoreManager: AuthStoreManager
    private lateinit var settingsStore: SettingsStore
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // https://pocketbase.io/docs/api-records/#verification

        authStoreManager = AuthStoreManager(this)
        notificationHelper = NotificationHelper(this)
        settingsStore = SettingsStore(this)

        setContent {
            val isDarkTheme = remember { mutableStateOf(true) }
            val navController = rememberNavController()
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            val hiddenBottomBar: List<String> = listOf("login_screen", "register_screen")
            val authResponse = remember { mutableStateOf<AuthResponse?>(null) }

            // Two datastores because of interference
            LaunchedEffect(Unit) {
                val storeResponse = authStoreManager.getFromDataStore()
                isDarkTheme.value = settingsStore.getDarkMode().first()

                storeResponse.collect { response ->
                    Log.d("FROM STORE", response.toString())
                    authResponse.value = response

                    if (isAutenticated(authResponse.value)) {
                        Log.d("AUTHSTORE", "Token valid")
                        navController.navigate("home_screen")
                    } else {
                        Log.d("AUTHSTORE", "Token does not exist")
                        navController.navigate("login_screen")
                    }
                }

            }

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
                                val viewModel = remember { CameraPreviewViewModel() }
                                homeScreen(navController, authResponse, authStoreManager, notificationHelper, modifier = Modifier.fillMaxSize(), viewModel)
                            }
                        }
                        composable("messages_screen") {
                            Middleware(
                                isAuthenticated = isAutenticated(authResponse),
                                undirect = { navController.navigate("login_screen") }
                            ) {
                                messageScreen(navController, authResponse, isDarkTheme)
                            }
                        }
                        composable("settings_screen") {
                            Middleware(
                                isAuthenticated = isAutenticated(authResponse),
                                undirect = { navController.navigate("login_screen") }
                            ) {
                                settingsScreen(navController, authResponse, authStoreManager, notificationHelper, settingsStore, isDarkTheme)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun loginScreen(
    navController: NavController,
    authResponseState: MutableState<AuthResponse?>,
    authStoreManager: AuthStoreManager,
    notificationHelper: NotificationHelper
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
                if (isAutenticated(authResponse) && authResponse != null) {
                    authResponseState.value = authResponse
                    authStoreManager.saveToDataStore(authResponse)
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
                if (isAutenticated(authResponse) && authResponse != null) {
                    authResponseState.value = authResponse
                    authStoreManager.saveToDataStore(authResponse)
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


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun homeScreen(
    navController: NavController,
    authResponse: MutableState<AuthResponse?>,
    authStoreManager: AuthStoreManager,
    notificationHelper: NotificationHelper,
    modifier: Modifier,
    viewModel: CameraPreviewViewModel
) {
    // https://aboyi.medium.com/how-to-make-your-own-android-camera-app-without-knowing-how-aca3364358b
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

    LaunchedEffect(cameraPermissionsState) {
        if (cameraPermissionsState.status.isGranted) {
            Log.d("LaunnchedEffect Perm notifications", "Granted")
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column {
        Text("Hello home screen")
        Button(onClick = {
            CoroutineScope(Dispatchers.IO).launch {
                if (authResponse.value != null) {
                    val messages = getMessages(authResponse.value!!)
                    Log.d("MESSAGES", messages.toString())
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                } else {
                    Log.d("AUTHRESPONSE", "NONE")
                }
            }
        }
        ) {
            Text("TEst messages")
        }
        CameraPreview(cameraPermissionsState, modifier, viewModel)

    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreview(cameraPermissionsState: PermissionState, modifier: Modifier, viewModel: CameraPreviewViewModel) {
    if (cameraPermissionsState.status.isGranted) {
        CameraPreviewContent(viewModel, modifier)
    } else {
        Column(
            modifier = modifier.fillMaxSize().wrapContentSize().widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val textToShow = if (cameraPermissionsState.status.shouldShowRationale) {
                "Whoops! Looks like we need your camera to work our magic!" +
                        "Don't worry, we just wanna see your pretty face (and maybe some cats).  " +
                        "Grant us permission and let's get this party started!"
            } else {
                "Hi there! We need your camera to work our magic! ✨\n" +
                        "Grant us permission and let's get this party started! \uD83C\uDF89"
            }
            Text(textToShow, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { cameraPermissionsState.launchPermissionRequest() }) {
                Text("Unleash the Camera!")
            }
        }
    }
}

@Composable
fun CameraPreviewContent(
    viewModel: CameraPreviewViewModel,
    modifier: Modifier = Modifier,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(lifecycleOwner) {
        viewModel.bindToCamera(context.applicationContext, lifecycleOwner)
    }

    surfaceRequest?.let { request ->
        CameraXViewfinder(
            surfaceRequest = request,
            modifier = modifier
        )
    }
}

@Composable
fun settingsScreen(
    navController: NavController,
    authResponse: MutableState<AuthResponse?>,
    authStoreManager: AuthStoreManager,
    notificationHelper: NotificationHelper,
    settingsStore: SettingsStore,
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
            CoroutineScope(Dispatchers.Main).launch {
                authResponse.value = null
                authStoreManager.clearDataStore()
            }
            navController.navigate("login_screen") {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true }
            isDarkTheme.value = true
        }
        ) {
            Text("Sign out")
        }
        IconButton(
            onClick = {
                isDarkTheme.value = !isDarkTheme.value
                CoroutineScope(Dispatchers.Main).launch {
                    settingsStore.saveToDataStore(isDarkTheme.value)
                }
            },
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
fun messageScreen(navController: NavController, authResponse: MutableState<AuthResponse?>, isDarkTheme: MutableState<Boolean>) {
    val messagesState = remember { mutableStateOf<List<Message>?>(null) }

    LaunchedEffect(authResponse.value) {
        if (authResponse.value != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val messages = getMessages(authResponse.value!!)
                Log.d("MESSAGES", messages.toString())
                if (messages != null) {
                    messagesState.value = messages.items
                    getImage(authResponse.value!!, messages.items[0].collectionId, messages.items[0].id, messages.items[0].photo)
                }
            }
        } else {
            Log.d("AUTHRESPONSE", "NONE")
        }
    }

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
        messagesState.value?.let { messages ->
            MessageList(authResponse, messages = messages)
        } ?: run {
            Log.d("NULL", "NO MESSAGE")
        }
    }
}


@Composable
fun MessageCard(authResponse: MutableState<AuthResponse?>, msg: Message, modifier: Modifier = Modifier) {
    val imagePainter = rememberAsyncImagePainter(getImageURL(authResponse.value, msg))
    Column (Modifier.fillMaxWidth().border(2.dp, MaterialTheme.colorScheme.onSecondary, RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.background).padding(vertical = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.padding(2.dp)) {
            Text(
                text = msg.expand.user.name,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = msg.updated.substring(0, msg.updated.lastIndexOf('.')),
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Image(
                painter = imagePainter,
                contentDescription = "Picture",
                modifier = Modifier.width(250.dp).height(150.dp).clip(RectangleShape).padding(0.dp)
            )
            Text(
                text = msg.text,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(0.dp)
            )
        }
    }
}

@Composable
fun MessageList(authResponse: MutableState<AuthResponse?>, messages: List<Message>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.padding(vertical = 24.dp, horizontal = 12.dp)) {
        items(messages) { message ->
            MessageCard(authResponse, msg = message, modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

// Notifications
// Camera, upload