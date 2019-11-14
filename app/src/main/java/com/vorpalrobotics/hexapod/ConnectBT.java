// https://create.arduino.cc/projecthub/mayooghgirish/arduino-bluetooth-basic-tutorial-d8b737
// modified to make the intent callable from the MainActivity
package com.vorpalrobotics.hexapod;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

import java.util.UUID;

public class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
{
    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String LOG_TAG = "DEBUG_CONNECT_BT"; // key for debug messages in Logcat
    private boolean ConnectSuccess = true; // if it's here, it's almost connected
    private BluetoothSocket bluetoothSocket = null; // the communications socket for the bluetooth connection
    private String bluetoothAddress; // bluetooth address of the Vorpal Robot
    private AppState appState; // AppState

    ConnectBT(AppState appState_)
    {
        super();
        appState = appState_;
        bluetoothAddress = appState.getBluetoothAddress();
    }

    @Override
    protected void onPreExecute()
    {
        appState.setBluetoothState(AppState.BluetoothState.CONNECTING);
    }

    @Override
    protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
    {
        try
        {
            BluetoothAdapter myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
            if (myBluetooth == null)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
                if (Utils.DEBUG)
                {
                    appState.setBluetoothState(AppState.BluetoothState.ERROR);
                    Log.wtf(LOG_TAG, "ConnectBT BluetoothAdapter is null");
                }
            } else {
                BluetoothDevice myDevice = myBluetooth.getRemoteDevice(bluetoothAddress);//connects to the device's bluetoothAddress and checks if it's available
                bluetoothSocket = myDevice.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                myBluetooth.cancelDiscovery();
                bluetoothSocket.connect();//start connection
            }
        }
        catch (Exception e)
        {
            ConnectSuccess = false;//if the try failed, you can check the exception here
            if (Utils.DEBUG_LOOP)
            {
                Log.wtf(LOG_TAG, "ConnectBT Exception " + e.getMessage());
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
    {
        super.onPostExecute(result);

        if (ConnectSuccess)
        {
            appState.setBluetoothSocket(bluetoothSocket);
            appState.setBluetoothState(AppState.BluetoothState.CONNECTED);
        }
        else
        {
            appState.setBluetoothState(AppState.BluetoothState.ERROR);
        }
    }
}
