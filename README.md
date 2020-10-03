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
<img src="images/Vorpal_Gamepad_main_screen.png" alt="Vorpal Gamepad app"/>

## Getting Started

Get a Vorpal Hexapod Robot, buy it or assemble it. You will need the Robot, but not the Gamepad. Please support Vorpal Robotics by buying from their store:
https://vorpal-robotics-store.myshopify.com/collections/hexapod-kits/products/vorpal-the-hexapod-opts

The Robot has an HC-05 Bluetooth receiver. If you bought the kit from Vorpal Robotics, the Gamepad and Robot will be paired. You do not want the HC-05 in the Robot to be paired to the Gamepad. Recommended is to buy a separate HC-05 and replace the one in the Robot (that way you can put back the original if you want). The HC-05 needs to be 5V compatible. If you buy the HC-05 from Vorpal Robotics, it will be compatible. If you buy it somewhere else, check the specs for 5V compatibility (not 3.3V). Note - one user said that he used an HC-06 successfully.

You will need to make sure that the connection speed of the HC-05 in the robot is 38400 - this is the speed that the app uses.
If you buy the kit or just the HC-05 from Vorpal Robotics, the HC-05 speed is set to 38400. But if you buy the HC-05 from somewhere else, you will need to change the speed to 38400 (many HC-05 default to 9600). There are several on-line tutorials on how to change the speed of the HC-05.

You will need to pair your Android device with the Robot, turn on the Robot, and follow: https://support.google.com/android/answer/9075925?hl=en (do not connect)

### Prerequisites

This app requires at least Android Ice Cream Sandwich (4.0)

### Installing

start with the Robot off.

On your Android device:
- Install the app from the Google Play Store.
- Run the App
- The message at the bottom will say "No Paired Device".
- Click on the Settings gear icon on the right of the page, find Bluetooth Devices, click on it.
- There will be a list of all paired bluetooth devices, select the bluetooth for the Robot.
- Make sure that the correct version of the Vorpal Hexapod is selected - all new kits will use version 3
- You can modify the other Preferences (recommended to leave Connect Automatically to on).
- Return to the main screen.
- The message should say No Connection and the Bluetooth icon will be black.
- Use the On/Off switch on the upper right to "turn on" the Gamepad.
- Turn on your Robot. Turn the dial all the way clockwise to RC.
- After a few seconds the Bluetooth icon should turn blue, and the message will change.

Now, you can use the app just like the Gamepad.
(note - ScratchX functionality was removed)

### Guide

The app works just like the real Vorpal Gamepad, see the Guide at
http://vorpalrobotics.com/wiki/index.php/Vorpal_The_Hexapod_Gamepad_User_Guide

note - if the app and the Robot get unsynced, (leave app turned on) try turning off the Robot, counting to 10, and turning the Robot on again.

The right hand side has additional options:
- on/off button - this works like the power button on the Gamepad.
- help - this brings up the Help page.
- preferences - this brings up the Preferences page.
- bluetooth icon - displays the Bluetooth status, also starts and stops Bluetooth if the connect Bluetooth Automatically Preference is not set.
- status indicator - indicates the status of the app, trim, recording, etc.

On the bottom is a message line.

## Troubleshooting
- use "Connect Automatically" in preferences
- start the app and turn it on before starting the robot
- make sure the robot dial is set to RC
- make sure the app is connected to the correct Bluetooth Device
- make sure the app is using the correct version of the Vorpal Hexapod
- if it cannot connect, make sure the speed of the HC-05 in the robot is set to 38400
- if you press a mode button too quickly, it may not register. You should press until you hear a beep.
- Fight mode F3 and F4 do not work correctly, too slow

## Building

This app is built with Android Studio using java and cpp for the Arduino code.

## Technical

This app uses:
- the Vorpal-Hexapod-Gamepad.ino file from Vorpal Robotics as a cpp file.
- file io directly from the cpp code.
- Bluetooth serial streaming io are handled using java code.
- nanohttpd web server.
