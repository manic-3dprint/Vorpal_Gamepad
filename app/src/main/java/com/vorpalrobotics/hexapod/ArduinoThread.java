package com.vorpalrobotics.hexapod;

import android.util.Log;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * This is a Thread that handles the (simulated) bluetooth activity on the Gamepad
 *
 */
public class ArduinoThread extends Thread {
    private static final int ARDUINO_THREAD_DELAY = 100; // arduino thread sleep milliseconds
    private static final String LOG_TAG = "DEBUG_ARDUINO"; // key for debug messages in Logcat
    private static final int CHECK_CONSECUTIVE_BLUETOOTH_ERRORS = 8; // number of consecutive bluetooth errors before disconnect bluetooth
    private AppState appState; // AppState
    private int consecutiveBluetoothErrors = 0; // number of consecutive bluetooth errors
    private ScratchXServer scratchXServer; // http servder to talk to ScratchX web page
    private boolean scratchXServerActive = false; // if the http server is active
    private boolean hasScratchXCommand = false; // a command was sent from ScratchX page

    /**
     * Constructor
     * @param appState_ the AppState
     */
    ArduinoThread(AppState appState_)
    {
        super();
        appState = appState_;
        scratchXServer = new NanoHttpdScratchXServer();
        scratchXServerActive = scratchXServer.startMe();
    }

    /**
     * standard Thread run()
     * this is a loop that calls the Arduino loop() function with input set and out processed
     */
    @Override
    public void run()
    {
        if (Utils.DEBUG)
        {
            Log.wtf(LOG_TAG, "Arduino run");
        }
        while (!Thread.currentThread().isInterrupted())
        {
            try {
                if (Utils.DEBUG_LOOP)
                {
                    Log.wtf(LOG_TAG, "Arduino while loop");
                }
                if (appState.isPowerOn() && appState.getBluetoothState() == AppState.BluetoothState.CONNECTED && !appState.isPaused())
                {
                    byte[] serialInput = appState.isConnectScratchX() ? scratchXServer.getSerialInput() : ScratchXServer.EMPTY_SERIAL_INPUT;
                    if (Utils.DEBUG_LOOP && serialInput.length > 0)
                    {
                        Log.wtf(LOG_TAG, "loop serial input:" + new String(serialInput));
                    }
                    boolean newHasScratchXCommand = serialInput.length > 0;
                    if (newHasScratchXCommand || hasScratchXCommand) {
                        appState.setScratchXCommand(newHasScratchXCommand);
                    }
                    hasScratchXCommand = newHasScratchXCommand;
                    byte[] bluetoothInput = receiveBluetooth();
                    if (bluetoothInput.length > 0 && Utils.DEBUG_LOOP) {
                        Log.wtf(LOG_TAG, "bluetooth receive:" + new String(bluetoothInput));
                    }
                    Calendar before = new GregorianCalendar();
                    byte[][] serialPlusBluetoothPlusIndicatorsOutput = arduinoLoop(serialInput, bluetoothInput);
                    Calendar after = new GregorianCalendar();
                    byte[] serialOutput = serialPlusBluetoothPlusIndicatorsOutput[0];
                    byte[] bluetoothOutput = serialPlusBluetoothPlusIndicatorsOutput[1];
                    byte[] stateIndicators = serialPlusBluetoothPlusIndicatorsOutput[2];
                    byte[] errorOutput = serialPlusBluetoothPlusIndicatorsOutput[3];
                    if (Utils.DEBUG_LOOP)
                    {
                        Log.wtf(LOG_TAG, "loop duration:" + (after.getTimeInMillis() - before.getTimeInMillis()) / 1000.00 + " seconds");
                        //Runtime.getRuntime().gc();
                        Log.wtf(LOG_TAG, "loop free memory:" + Runtime.getRuntime().freeMemory());
                    }
                    if (serialOutput.length > 0)
                    {
                        if (appState.isConnectScratchX()) {
                            scratchXServer.setSerialOutput(serialOutput);
                        }
                        if (Utils.DEBUG_LOOP)
                        {
                            Log.wtf(LOG_TAG, "loop serial output:" + new String(serialOutput));
                        }
                    }
                    if (bluetoothOutput.length > 0) {
                        sendBluetooth(bluetoothOutput);
                        if (Utils.DEBUG_LOOP)
                        {
                            Log.wtf(LOG_TAG, "loop bluetooth send:" + new String(bluetoothOutput));
                        }
                    }
                    appState.setScratchXing(stateIndicators[0] == 1);
                    appState.setScratchState(stateIndicators[1]);
                    appState.setGamepadState(stateIndicators[2]);
                    if (errorOutput.length > 0 && Utils.DEBUG_LOOP)
                    {
                        Log.wtf(LOG_TAG, "loop error:" + new String(errorOutput));
                    }
                }
                Thread.sleep(ARDUINO_THREAD_DELAY);
                yield();
            } catch (Exception ex)
            {
                ///// Thread.currentThread().interrupt();
                if (Utils.DEBUG)
                {
                    Log.wtf(LOG_TAG, "Arduino Thread Exception " + ex.getMessage());
                }
            }
        }
    }

    /**
     * indicate that the bluetooth connection is working correctly
     */
    private void bluetoothGood()
    {
        appState.setBluetoothState(AppState.BluetoothState.CONNECTED);
        consecutiveBluetoothErrors = 0;
    }

    /**
     * indicate that the bluetooth connection has a problem
     */
    private void bluetoothError()
    {
        consecutiveBluetoothErrors++;
        appState.setBluetoothState(AppState.BluetoothState.ERROR);
        if (consecutiveBluetoothErrors > CHECK_CONSECUTIVE_BLUETOOTH_ERRORS) {
            appState.disconnectBluetooth();
            consecutiveBluetoothErrors = 0;
        }
    }

    /**
     * receive the bluetooth data from the Robot
     * @return data from the Robot
     */
    private byte[] receiveBluetooth()
    {
        byte[] data = new byte[0];
        byte[] buffer = new byte[1024];
        try
        {
            if (appState.getBluetoothSocket().getInputStream().available() > 0)
            {
                int length = appState.getBluetoothSocket().getInputStream().read(buffer);
                bluetoothGood();
                data = new byte[length];
                System.arraycopy(buffer, 0, data, 0, length);
            }
        }
        catch (IOException e)
        {
            if (Utils.DEBUG)
            {
                Log.wtf(LOG_TAG, "bluetooth receive error:" + e.getMessage());
            }
            bluetoothError();
        }
        return data;
    }

    /**
     * send the bluetooth data to the Robot
     * @param data data for the Robot
     */
    private void sendBluetooth(byte[] data)
    {
        try
        {
            appState.getBluetoothSocket().getOutputStream().write(data);
            bluetoothGood();
        }
        catch (IOException e) {
            if (Utils.DEBUG) {
                Log.wtf(LOG_TAG, "bluetooth send error:" + e.getMessage());
            }
            bluetoothError();
        }
    }

    /////////////// C++ native routines, implemented in native-lib.cpp /////////////////

    public native byte[][] arduinoLoop(byte[] serialInput, byte[] bluetoothInput);
}
