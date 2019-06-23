# Vorpal_Gamepad
This is an Android app to replace the Gamepad for the Vorpal Hexapod Robot.

!!! This app is not associated in any way with the Vorpal Robotics Corporation !!!

The Vorpal Hexapod is a remote control six legged spider robot toy.
The web page is:
https://vorpalrobotics.com/wiki/index.php/Vorpal_The_Hexapod
<img src="https://vorpalrobotics.com/wiki/images/7/7b/Scamp-Leg-Raised.jpg" alt="Vorpal Robot"/>

The Vorpal Hexapod consists of the Robot itself, and a Gamepad to control the Robot remotely by bluetooth.

The app works the same as the Gamepad (trim/recording/playing/ScratchX are all implemented) and looks similar.
<img src="https://vorpalrobotics.com/wiki/images/9/93/Gamepad-Top-View-v2.png" alt="Vorpal Gamepad"/>
<img src="Vorpal_Gamepad_main_screen.png" alt="Vorpal Gamepad app"/>

## Getting Started

Get a Vorpal Hexapod Robot, buy it or assemble it. You will need the Robot, but not the Gamepad. Please support Vorpal Robotics by buying from their store:
https://vorpal-robotics-store.myshopify.com/collections/hexapod-kits/products/vorpal-the-hexapod-opts

The Robot has an HC-05 Bluetooth receiver. If you bought the kit, the Gamepad and Robot will be paired. You do not want the HC-05 in the Robot to be paired to the Gamepad. Recommended is to buy a separate HC-05 and replace the one in the Robot (that way you can put back the original if you want). The HC-05 needs to be 5V compatible. If you buy the HC-05 from Vorpal, it will be compatible. If you buy it somewhere else, check the specs for 5V compatibility (not 3.3V).

You will need to pair your Android device with the Robot, turn on the Robot, and follow: https://support.google.com/android/answer/9075925?hl=en (do not connect)

You will need the connection speed of the HC-05, the default speed is 9600. The Vorpal paired HC-05 speed is 38400. There are several on line tutorials on how to change the speed of the HC-05.

### Prerequisites

This app requires at least Android 7.0 (Nougat)

### Installing

start with the Robot off.

On your Android device:
- Install the app from the Google Play Store.
- Run the App
- The message at the bottom will say "No Paired Device".
- Click on the Settings gear icon on the right of the page, find Bluetooth Devices, click on it.
- There will be a list of all paired bluetooth devices, select the bluetooth for the Robot.
- On Bluetooth Speed, you can click on it to enter the speed that was set on the HC-05 in the Robot.
- You can modify the other Preferences (recommended to leave Connect Automatically to on).
- Return to the main screen.
- The message should say No Connection and the Bluetooth icon will be black.
- Use the On/Off switch on the upper right to "turn on" the Gamepad.
- Turn on your Robot. Turn the dial all the way clockwise to RC.
- After a few seconds the Bluetooth icon should turn blue, and the message will change.

Now, you can use the app just like the Gamepad.
(see below for using ScratchX)

### Guide

The app works just like the real Vorpal Gamepad, see the Guide at
http://vorpalrobotics.com/wiki/index.php/Vorpal_The_Hexapod_Gamepad_User_Guide

note - if the app and the Robot get unsynced, try turning off the Robot, counting to 10, and turning the Robot on again. (reconnect bluetooth if not set to automatic).

The right hand side has additional options:
- on/off button - this works like the power button on the Gamepad.
- help - this brings up the Help page.
- preferences - this brings up the Preferences page.
- bluetooth icon - displays the Bluetooth status, also starts and stops Bluetooth if the connect Bluetooth Automatically Preference is not set.
- status indicator - indicates the status of the app, trim, recording, etc.

On the bottom is a message line.

### ScratchX

The real Vorpal Gamepad uses ScratchX by connecting the Gamepad to the PC using a USB cable, and bringing up the Vorpal ScratchX web page.
The app uses ScratchX by connecting to the PC using wifi, and bringing up the ScratchX web page for the app on your PC.
You will need to host web pages that the app can access by wifi. To host web pages on your Windows PC, you can turn on IIS, there are several web pages/Youtube vidoes on how to do this, including https://www.betterhostreview.com/turn-on-iis-windows-10.html. There is a Chrome plugin to serve web pages, https://chrome.google.com/webstore/detail/web-server-for-chrome/ofhbbkphhbklhfoeikjpcbhemlocgigb.

- copy the file Vorpal-Gamepad-App-Scratch.js into the root folder of your web host.
- make sure wifi is turned on on your Android device.
- run the Vorpal_Gamepad app.
- click on the Settings gear and find the ScratchX section.
- copy the ip address in parenthesis - four numbers separated by periods.
- exit the settings page on the app.
- edit the Vorpal-Gamepad-App-Scratch.js file and replace the ???.???.???.??? with this ip address and save the file.
- open a browser on your PC and go to web page (replace localhost with the hosted location of the file, if necessary)
     http://scratchx.org/?url=http://localhost/Vorpal-Gamepad-App-Scratch.js#scratch
 
 You should now be able to control the Robot using ScratchX. This will work like using ScratchX with the real Gamepad.

## Building

This app is built with Android Studio using java and cpp for the Arduino code.

## Technical

This app uses:
- the Vorpal-Hexapod-Gamepad.ino file as a cpp file.
- file io directly from the cpp code.
- bluetooth and ScratchX serial streaming io are handled using java code.
- nanohttpd web server.
- Vorpal-Gamepad-App-Scratch.js ScratchX app.
