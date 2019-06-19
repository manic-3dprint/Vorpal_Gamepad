package com.vorpalrobotics.hexapod;

import android.bluetooth.BluetoothSocket;

/**
 * A caller to the Arduino Thread
 *
 */
public interface ArduinoThreadCaller {
    /**
     * indicates whether the Gamepad power is on or off
     * @return Gamepad power on/off
     */
    boolean isPowerOn();

    /**
     * indicates if there is a Bluetooth connection to the Robot
     * @return true if there is a bluetooth connection, false otherwise
     */
    boolean isBtConnected();

    /**
     * get the Bluetooth socket for the connection to the Robot
     * @return the Bluetooth socket for the Robot connection
     */
    BluetoothSocket getBluetoothSocket();

    /**
     * indicates whether the app is paused
     * @return true if the app is paused, false otherwise
     */
    boolean isPaused();

    /**
     * get the Message Handler from the calling Activity
     * @return Message Handler
     */
    MainActivity.HandlerExtension getHandlerExtension();

    /**
     * indicates that the user has selected the use ScratchX preference
     * @return true if the user wants to use ScratchX, false otherwise
     */
    boolean useScratchX();
}
