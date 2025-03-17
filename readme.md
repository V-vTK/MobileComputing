# Mobile computing
Deviced to try something new rather than proceeding with React Native. Jetpack compose has a lot of similarities with React but in my opinion it's missing some key features like Tailwind and "good" component libraries. Still a lot of knowledge transfers directlty over from React, which was a pleasant surprise. Final course grade was 5/5 including full points from all homeworks and final project.

Homeworks are under HW branches and final project is under master branch.

### Final project (15 +10) ###
"If your project is very good, you may get extra points towards the course grade to make up for lost points from previous homework assignments (total homework points will not exceed 50). In general, implementing more features can earn you more points, but in order to earn extra points, the work must be high quality as it will be evaluated more strictly beyond 15p."

Authentication and caching (10p)
- User is able to register and log in trough the application
- User authentication (JWT) token is saved onto the device and automatic login is completed if cached token is found.
- Darkmode is saved on device and activated accordingly
- NextJS like middleware to easily check routes for authentication. This is done on the frontend so inherently not safe but still offers benefits trough usability. The backend (Pocketbase) does autentication for all API-calls.
- Only some routes contain the bottom bar  

Serverside functionality (5p)
- The user is able to send messages to a common chat
- Other can see these messages
- The user can't send chats under others name and without sign in up
- The user is able to easily send pictures taken by the applications camera.
- There is direct feedback given by "toasts" on many features like login, sign up, message sent.

Camera (10p)
- Native Android camera is used inside the app
- The camera is integrated into the app's features
- Permissions are asked for the camera
- Complete camera preview 

# Localhosted installation
Either use the default server hosted by me or change the Pocketbase address to the localhosted version.
Get the pocketbase executible https://pocketbase.io/ unzip and run ./pocketbase serve in the same folder.
Load the backup, which contains the collection settings. After that the application should work. Launch the app inside Android studio simulator.

Admin credentials for the backup are:
- test@email.com
- 1234567890


### HW1 ###
Requirements:
- At least one custom image (1p)
- At least 2 different styles of text (1p)
- Scrolling and enough visual elements to need it (1p)
- At least one clickable element that creates a visible change (2p)

### HW2 ###
Requirements:
- At least one custom image (1p)
- At least 2 different styles of text (1p)
- Scrolling and enough visual elements to need it (1p)
- At least one clickable element that creates a visible change (2p)

### HW3 ###
Requirements:
- Input and display at least some text and a picture in one view (5p)
    - For example, username and profile picture. Image can be loaded from gallery or internet. When the image is picked, it should show up in that view
- Display given text and picture in another view and retain these changes when restarting application (5p)

### HW4 ###
Requirements:
- Trigger a notification (2p)
    - …while the app is not on foreground (1p)
- Notification can be interacted with (something happens if you tap it) (2p)
- Use any type of sensor listed here: Sensors Overview | Android Developers (5p)
     - Sensor data must be either displayed or otherwise used in the app
- Instead of a sensor, you may also use some public online API that reports real-time data
    - For example, you can find APIs that provide data about Oulu here
