package com.example.myapplication

import android.Manifest
import android.content.Context
import android.media.ExifInterface
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.notifications.NotificationHelper
import com.example.myapplication.services.AuthResponse
import com.example.myapplication.services.AuthStoreManager
import com.example.myapplication.services.Message
import com.example.myapplication.services.NewUser
import com.example.myapplication.services.SettingsStore
import com.example.myapplication.services.authWithPassword
import com.example.myapplication.services.createUser
import com.example.myapplication.services.getImageURL
import com.example.myapplication.services.getMessages
import com.example.myapplication.services.uploadMessage
import com.example.myapplication.ui.theme.ComposeTutorialTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File


class MainActivity : ComponentActivity() {
    private lateinit var authStoreManager: AuthStoreManager
    private lateinit var settingsStore: SettingsStore
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authStoreManager = AuthStoreManager(this)
        notificationHelper = NotificationHelper(this)
        settingsStore = SettingsStore(this)

        setContent {
            val isDarkTheme = remember { mutableStateOf(true) }
            val navController = rememberNavController()
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            val hiddenBottomBar: List<String> = listOf("login_screen", "register_screen")
            val authResponse = remember { mutableStateOf<AuthResponse?>(null) }
            val messageViewModel = remember { MessageViewModel() }
            val cameraViewModel = remember { CameraPreviewViewModel() }

            // Two datastores because of interference
            LaunchedEffect(Unit) {
                val storeResponse = authStoreManager.getFromDataStore()
                isDarkTheme.value = settingsStore.getDarkMode().first()

                storeResponse.collect { response ->
                    authResponse.value = response

                    if (isAutenticated(authResponse.value)) {
                        Log.d("Authstore", "Token valid")
                        navController.navigate("home_screen")
                    } else {
                        Log.d("Authstore", "Token does not exist")
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
                                homeScreen(navController, authResponse, authStoreManager, notificationHelper, modifier = Modifier.fillMaxSize(), cameraViewModel)
                            }
                        }
                        composable("messages_screen") {
                            Middleware(
                                isAuthenticated = isAutenticated(authResponse),
                                undirect = { navController.navigate("login_screen") }
                            ) {
                                messageScreen(navController, authResponse, isDarkTheme, messageViewModel)
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

    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
                if (isAutenticated(authResponse) && authResponse != null) {
                    Toast.makeText(context, "Login successful", Toast.LENGTH_LONG).show()
                    authResponseState.value = authResponse
                    authStoreManager.saveToDataStore(authResponse)
                    navController.navigate("home_screen")
                } else{
                    Toast.makeText(context, "Login failed", Toast.LENGTH_LONG).show()
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
    notificationHelper: NotificationHelper,
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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

        // https://developer.android.com/guide/topics/ui/notifiers/toasts
        Button(onClick = {
            CoroutineScope(Dispatchers.Main).launch {
                createUser(NewUser(email, password, password, username))
                val authResponse = authWithPassword(email, password)
                if (isAutenticated(authResponse) && authResponse != null) {
                    Toast.makeText(context, "New user created", Toast.LENGTH_LONG).show()
                    authResponseState.value = authResponse
                    authStoreManager.saveToDataStore(authResponse)
                    navController.navigate("home_screen")
                } else{
                    Toast.makeText(context, "user creation failed", Toast.LENGTH_LONG).show()
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

    var text: MutableState<String> = remember { mutableStateOf("") }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("Camera permissions", "Granted")
        } else {
            Log.d("Camera permissions", "Failed")
        }
    }

    LaunchedEffect(cameraPermissionsState) {
        if (cameraPermissionsState.status.isGranted) {
            Log.d("Camera permissions", "Newly granted")
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        CameraPreview(cameraPermissionsState, modifier.fillMaxWidth().weight(1f), viewModel, authResponse, text)
        TextField(
            value = text.value,
            onValueChange = { text.value = it },
            label = { Text("Enter message") },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )
    }

}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreview(
    cameraPermissionsState: PermissionState,
    modifier: Modifier,
    viewModel: CameraPreviewViewModel,
    authResponse: MutableState<AuthResponse?>,
    text: MutableState<String>
) {
    if (cameraPermissionsState.status.isGranted) {
        CameraPreviewContent(viewModel, modifier, authResponse, text)
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .wrapContentSize()
                .widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("The applicationn needs camera permissions", textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { cameraPermissionsState.launchPermissionRequest() }) {
                Text("Give permissions to camera")
            }
        }
    }
}

// https://medium.com/androiddevelopers/tap-to-focus-mastering-camerax-transformations-in-jetpack-compose-440853280a6e
@Composable
fun CameraPreviewContent(
    viewModel: CameraPreviewViewModel,
    modifier: Modifier = Modifier,
    authResponse: MutableState<AuthResponse?>,
    text: MutableState<String>,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    val context = LocalContext.current
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
    val displayRotation = context.display?.rotation

    LaunchedEffect(lifecycleOwner) {
        viewModel.bindToCamera(context.applicationContext, lifecycleOwner)
    }

    surfaceRequest?.let { request ->
        CameraXViewfinder(
            surfaceRequest = request,
            modifier = modifier.pointerInput(Unit) {
                detectTapGestures {
                    capturePhoto(context, viewModel.imageCapture, displayRotation, authResponse, text)
                }
            }
        )
    }
}

// https://developer.android.com/media/camera/camerax/take-photo#kotlin
// https://github.com/Coding-Meet/Camera-Using-CameraX
// https://stackoverflow.com/questions/61172891/camerax-image-rotation-with-fixed-sreenorientation
fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture,
    displayRotation: Int?,
    authResponse: MutableState<AuthResponse?>,
    text: MutableState<String>
) {
    val photoFile = File(
        context.externalMediaDirs.firstOrNull(),
        "latest_photo.jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    val rotationDegrees = (displayRotation ?: Surface.ROTATION_0) + Surface.ROTATION_90

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val exif = ExifInterface(photoFile)
                val rotation = when (rotationDegrees) {
                    Surface.ROTATION_90 -> ExifInterface.ORIENTATION_ROTATE_90
                    Surface.ROTATION_180 -> ExifInterface.ORIENTATION_ROTATE_180
                    Surface.ROTATION_270 -> ExifInterface.ORIENTATION_ROTATE_270
                    else -> ExifInterface.ORIENTATION_NORMAL
                }
                exif.setAttribute(ExifInterface.TAG_ORIENTATION, rotation.toString())
                exif.saveAttributes()

                Toast.makeText(context, "Message sent", Toast.LENGTH_LONG).show()

                val atomicAuthResponse = authResponse.value
                if (atomicAuthResponse  != null) {
                    uploadMessage(atomicAuthResponse, text.value, photoFile)
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(context, "Photo capturing failed", Toast.LENGTH_LONG).show()
            }
        }
    )
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
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge.copy(
                color = MaterialTheme.colorScheme.primary, fontWeight = MaterialTheme.typography.headlineLarge.fontWeight),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Text("Email: ${authResponse.value?.record?.email}",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = MaterialTheme.colorScheme.primary),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

        Row(modifier = Modifier.padding(vertical = 6.dp, horizontal = 16.dp)) {
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

            Button(
                onClick = {
                    isDarkTheme.value = !isDarkTheme.value
                    CoroutineScope(Dispatchers.Main).launch {
                        settingsStore.saveToDataStore(isDarkTheme.value)
                    }
                },
                modifier = Modifier.padding(horizontal = 6.dp)
            ) {
                Text("Switch Theme")
            }
        }
    }
}

@Composable
fun messageScreen(navController: NavController, authResponse: MutableState<AuthResponse?>, isDarkTheme: MutableState<Boolean>, messageViewModel: MessageViewModel) {
    val context = LocalContext.current

    LaunchedEffect(authResponse.value) {
        if (authResponse.value == null) {
            Toast.makeText(context, "Authentication state not OK", Toast.LENGTH_LONG).show()
        } else if (messageViewModel.messagesState.value == null) {
            CoroutineScope(Dispatchers.IO).launch {
                val messages = getMessages(authResponse.value!!)
                messageViewModel.messagesState.value = messages
            }
        }
    }

    Column {
        Row {
            Text(
                text = "Messages",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
            )
            IconButton(onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    val messages = getMessages(authResponse.value!!)
                    messageViewModel.messagesState.value = messages
                }
            }, modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Icon(imageVector = Icons.Filled.Refresh, contentDescription = "fetch messages")

            }
        }

        val messages = messageViewModel.messagesState.value
        if (messages != null) {
            MessageList(authResponse, messages = messages.items)
        } else {
            Text("No messages available")
        }
    }
}


@Composable
fun MessageCard(authResponse: MutableState<AuthResponse?>, msg: Message, modifier: Modifier = Modifier) {
    Log.d("DEBUG123", msg.toString())
    val imagePainter = rememberAsyncImagePainter(getImageURL(authResponse.value, msg))
    Column (
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.padding(2.dp)) {
            // Null check if expand does not arrive from server. Some security settings might drop it.
            (msg.expand?.user?.name ?: null)?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.primary
                )
            }

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
                modifier = Modifier
                    .width(300.dp)
                    .height(200.dp)
                    .clip(RectangleShape)
                    .padding(0.dp)
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
    if (authResponse.value != null) {
        LazyColumn(modifier = modifier.padding(vertical = 24.dp, horizontal = 12.dp)) {
            items(messages) { message ->
                MessageCard(
                    authResponse,
                    msg = message,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}
