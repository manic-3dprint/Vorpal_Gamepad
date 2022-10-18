package com.vorpalrobotics.hexapod;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

class AppState {
    public enum BluetoothState {
        UNAVAILABLE(R.drawable.bluetooth_button_unavailable, R.string.message_no_bluetooth),
        UNPAIRED(R.drawable.bluetooth_button_disconnected, R.string.message_no_paired_device),
        DISCONNECTED(R.drawable.bluetooth_button_disconnected, R.string.message_no_connection),
        CONNECTING(R.drawable.bluetooth_button_connecting, R.string.message_connecting),
        CONNECTED(R.drawable.bluetooth_button_connected, 0),
        ERROR(R.drawable.bluetooth_button_error, R.string.message_bluetooth_error);

        private int iconId;
        private int messageId;

        // getter method
        public int getIconId()
        {
            return this.iconId;
        }

        // getter method
        public int getMessageId()
        {
            return this.messageId;
        }

        BluetoothState(int iconId_, int messageId_)
        {
            iconId = iconId_;
            messageId = messageId_;
        }
    }
    private static final String LOG_TAG = "DEBUG_STATE"; // key for debug messages in Logcat
    // note - sd card prefeence not saved here
    private boolean isPowerOn_ = false; // indicates whether the Gamepad power is on
    private boolean isPaused_ = false; // if the user is using another app on this Android device
    private boolean isSound_ = true; // if the user selected the sound preference
    private boolean isConnectBluetoothAutomatically_ = true; // user preference to automatically connect to the Robot by bluetooth
    private String bluetoothAddress_ = ""; // bluetooth address of the connected Robot
    private BluetoothSocket bluetoothSocket_ = null; // the Bluetooth socket of the bluetooth connection
    private BluetoothState bluetoothState_ = BluetoothState.UNAVAILABLE;
    private byte trimState_;
    private byte gamepadState_;

    /**
     * indicates whether the Gamepad power is on or off
     * @return Gamepad power on/off
     */
    boolean isPowerOn()
    {
        return isPowerOn_;
    }

    /**
     * save the power on state
     * @param newIsPowerOn_ the power on state
     */
    void setPowerOn(boolean newIsPowerOn_)
    {
        this.isPowerOn_ = newIsPowerOn_;
    }

    /**
     * indicates whether the app is paused
     * @return true if the app is paused, false otherwise
     */
    boolean isPaused()
    {
        return isPaused_;
    }

    /**
     * save the paused state
     * @param newIsPaused_ the paused state
     */
    void setPaused(boolean newIsPaused_)
    {
        isPaused_ = newIsPaused_;
    }

    /**
     * indicates whether the sound preference is selected
     * @return sound preference
     */
    boolean isSound()
    {
        return isSound_;
    }

    /**
     * save the sound preference
     * @param newIsSound_ the sound preference
     */
    void setSound(boolean newIsSound_)
    {
        isSound_ = newIsSound_;
    }

    /**
     * indicates if the caller selected the connect bluetooth automatically preference
     * @return the connect bluetooth automatically preference
     */
    boolean isConnectBluetoothAutomatically()
    {
        return isConnectBluetoothAutomatically_;
    }

    /**
     * save the connect bluetooth automatically preference
     * @param newIsConnectBluetoothAutomatically_ the connect bluetooth automatically preference
     */

    void setConnectBluetoothAutomatically(boolean newIsConnectBluetoothAutomatically_)
    {
        isConnectBluetoothAutomatically_ = newIsConnectBluetoothAutomatically_;
    }

    /**
     * get the bluetooth address of the selected device
     * @return bluetooth address
     */
    String getBluetoothAddress()
    {
        return bluetoothAddress_;
    }

    /**
     * save the bluetooth address of the selected device
     * @param newBluetoothAddress_ the bluetooth address of the selected device
     */
    void setBluetoothAddress(String newBluetoothAddress_)
    {
        bluetoothAddress_ = newBluetoothAddress_;
        if (bluetoothAddress_.equals("")) {
            bluetoothState_ = BluetoothState.UNPAIRED;
        } else {
            bluetoothState_ = BluetoothState.DISCONNECTED;
        }
    }

    /**
     * get the Bluetooth socket for the connection to the Robot
     * @return the Bluetooth socket for the Robot connection
     */
    BluetoothSocket getBluetoothSocket()
    {
        return bluetoothSocket_;
    }

    /**
     * save the Bluetooth socket that is created to the caller
     * @param newBluetoothSocket_ the bluetooth socket to the Robot
     */
    void setBluetoothSocket(BluetoothSocket newBluetoothSocket_)
    {
        bluetoothSocket_ = newBluetoothSocket_;
        if (Utils.DEBUG)
        {
            Log.wtf(LOG_TAG, "Bluetooth connected");
        }
    }

    String disconnectBluetooth() {
        String status = null;
        if (bluetoothSocket_ != null) {
            try {
                bluetoothSocket_.close(); //close connection
            } catch (IOException e) {
                status = e.getMessage();
            }
            bluetoothSocket_ = null;
        }
        return status;
    }

    BluetoothState getBluetoothState() {
        return bluetoothState_;
    }

    void setBluetoothState(BluetoothState bluetoothState__) {
        this.bluetoothState_ = bluetoothState__;
    }

    byte getTrimState() {
        return trimState_;
    }

    void setTrimState(byte trimState__) {
        this.trimState_ = trimState__;
    }

    byte getGamepadState() {
        return gamepadState_;
    }

    void setGamepadState(byte gamepadState__) {
        this.gamepadState_ = gamepadState__;
    }
}
