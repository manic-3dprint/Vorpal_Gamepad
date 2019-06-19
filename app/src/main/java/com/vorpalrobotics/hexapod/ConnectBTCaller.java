package com.vorpalrobotics.hexapod;

import android.bluetooth.BluetoothSocket;

/**
 * A caller to the Bluetooth connector Task
 *
 */
public interface ConnectBTCaller {
    /**
     * set a message on the caller
     * @param messageId the message id
     * @param colorId the color of the message
     */
    void setMessage(int messageId, int colorId);

    /**
     * save the Bluetooth socket that is created to the caller
     * @param btSocket the bluetooth socket to the Robot
     */
    void setBluetoothSocket(BluetoothSocket btSocket);

    /**
     * indicates if the caller selected the connect bluetooth automatically preference
     * @return the connect bluetooth automatically preference
     */
    boolean isConnectBluetoothAutomatically();

    /**
     * indicate to the caller that the task is not connecting (it is finished)
     */
    void setNotConnecting();
}
