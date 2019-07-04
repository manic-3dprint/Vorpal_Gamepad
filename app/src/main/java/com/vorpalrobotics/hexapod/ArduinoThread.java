package com.vorpalrobotics.hexapod;

import android.os.Bundle;
import android.os.Message;
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
    private static final int ARDUINO_COMMAND_DELAY = 2000; // arduino thread sleep milliseconds
    public static final boolean DEBUG_LOOP = true; // Set to true to get debug messages in loop - a whole lot of debug output
    private static final String LOG_TAG = "DEBUG_ARDUINO"; // key for debug messages in Logcat
    private static final int CHECK_CONSECUTIVE_BLUETOOTH_ERRORS = 8; // number of consecutive bluetooth errors before disconnect bluetooth
    private ArduinoThreadCaller callingActivity; // interface of MainActivity
    private static final byte[] BLUETOOTH_GOOD = {0}; // bluetooth no error
    private static final byte[] BLUETOOTH_ERROR = {1}; // bluetooth error
    private int consecutiveBluetoothErrors = 0; // number of consecutive bluetooth errors
    private ScratchXServer scratchXServer; // http servder to talk to ScratchX web page
    private boolean scratchXServerActive = false; // if the http server is active
    private boolean hasScratchXCommand = false; // a command was sent from ScratchX page

    /**
     * Constructor
     * @param callingActivity_ the Activity that started this Thread - implements interface ArduinoThreadCaller
     */
    ArduinoThread(ArduinoThreadCaller callingActivity_)
    {
        super();
        callingActivity = callingActivity_;
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
        if (MainActivity.DEBUG)
        {
            Log.wtf(LOG_TAG, "Arduino run");
        }
        while (!Thread.currentThread().isInterrupted())
        {
            try {
                if (DEBUG_LOOP)
                {
                    Log.wtf(LOG_TAG, "Arduino while loop");
                }
                if (callingActivity.isPowerOn() && callingActivity.isBtConnected() && !callingActivity.isPaused())
                {
                    byte[] serialInput = callingActivity.useScratchX() ? scratchXServer.getSerialInput() : ScratchXServer.EMPTY_SERIAL_INPUT;
                    if (DEBUG_LOOP && serialInput.length > 0)
                    {
                        Log.wtf(LOG_TAG, "loop serial input:" + new String(serialInput));
                    }
                    boolean newHasScratchXCommand = serialInput.length > 0;
                    if (newHasScratchXCommand || hasScratchXCommand) {
                        setScratchXCommand(newHasScratchXCommand);
                    }
                    hasScratchXCommand = newHasScratchXCommand;
                    byte[] bluetoothInput = receiveBluetooth();
                    if (bluetoothInput.length > 0 && DEBUG_LOOP) {
                        Log.wtf(LOG_TAG, "bluetooth receive:" + new String(bluetoothInput));
                    }
                    Calendar before = new GregorianCalendar();
                    byte[][] serialPlusBluetoothPlusIndicatorsOutput = arduinoLoop(serialInput, bluetoothInput);
                    Calendar after = new GregorianCalendar();
                    byte[] serialOutput = serialPlusBluetoothPlusIndicatorsOutput[0];
                    byte[] bluetoothOutput = serialPlusBluetoothPlusIndicatorsOutput[1];
                    byte[] stateIndicators = serialPlusBluetoothPlusIndicatorsOutput[2];
                    byte[] errorOutput = serialPlusBluetoothPlusIndicatorsOutput[3];
                    if (DEBUG_LOOP)
                    {
                        Log.wtf(LOG_TAG, "loop duration:" + (after.getTimeInMillis() - before.getTimeInMillis()) / 1000.00 + " seconds");
                        //Runtime.getRuntime().gc();
                        Log.wtf(LOG_TAG, "loop free memory:" + Runtime.getRuntime().freeMemory());
                    }
                    if (serialOutput.length > 0)
                    {
                        if (callingActivity.useScratchX()) {
                            scratchXServer.setSerialOutput(serialOutput);
                        }
                        if (DEBUG_LOOP)
                        {
                            Log.wtf(LOG_TAG, "loop serial output:" + new String(serialOutput));
                        }
                    }
                    if (bluetoothOutput.length > 0) {
                        sendBluetooth(bluetoothOutput);
                        if (DEBUG_LOOP)
                        {
                            Log.wtf(LOG_TAG, "loop bluetooth send:" + new String(bluetoothOutput));
                        }
                    }
                    setStatus(MainActivity.MESSAGE_ICON_STATE, stateIndicators);
                    if (errorOutput.length > 0 && DEBUG_LOOP)
                    {
                        Log.wtf(LOG_TAG, "loop error:" + new String(errorOutput));
                    }
                }
                Thread.sleep(ARDUINO_THREAD_DELAY);
                yield();
            } catch (Exception ex)
            {
                ///// Thread.currentThread().interrupt();
                if (MainActivity.DEBUG)
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
        setStatus(MainActivity.MESSAGE_ICON_BLUETOOTH, BLUETOOTH_GOOD);
        consecutiveBluetoothErrors = 0;
    }

    /**
     * indicate that the bluetooth connection has a problem
     */
    private void bluetoothError()
    {
        consecutiveBluetoothErrors++;
        if (consecutiveBluetoothErrors > CHECK_CONSECUTIVE_BLUETOOTH_ERRORS) {
            callingActivityDisconnectBluetooth();
            consecutiveBluetoothErrors = 0;
        } else {
            setStatus(MainActivity.MESSAGE_ICON_BLUETOOTH, BLUETOOTH_ERROR);
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
            if (callingActivity.getBluetoothSocket().getInputStream().available() > 0)
            {
                int length = callingActivity.getBluetoothSocket().getInputStream().read(buffer);
                bluetoothGood();
                data = new byte[length];
                System.arraycopy(buffer, 0, data, 0, length);
            }
        }
        catch (IOException e)
        {
//            setMessage(R.string.message_communication_error, R.color.colorMessageError);
            if (MainActivity.DEBUG)
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
            callingActivity.getBluetoothSocket().getOutputStream().write(data);
            bluetoothGood();
        }
        catch (IOException e) {
//            setMessage(R.string.message_communication_error, R.color.colorMessageError);
            if (MainActivity.DEBUG) {
                Log.wtf(LOG_TAG, "bluetooth send error:" + e.getMessage());
            }
            bluetoothError();
        }
    }

    // https://www.intertech.com/Blog/android-non-ui-to-ui-thread-communications-part-3-of-5/
    /**
     * set an icon on the Main view
     * @param icon which icon
     * @param status for state icon, the state indicators
     */
    private void setStatus(byte icon, byte[] status) {
        if (DEBUG_LOOP) {
            Log.wtf(LOG_TAG, "setStatus from the Arduino Thread");
        }
        Bundle msgBundle = new Bundle();
        msgBundle.putString("type", "icon");
        msgBundle.putByte("icon", icon);
        msgBundle.putByteArray("status", status);
        Message msg = new Message();
        msg.setData(msgBundle);
        callingActivity.getHandlerExtension().sendMessage(msg);
    }

    /**
     * tell the calling Activity that there is a scratchX command or not
     * @param hasScratchXCommand if there is a scratchX command
     */
    private void setScratchXCommand(boolean hasScratchXCommand) {
        Bundle msgBundle = new Bundle();
        msgBundle.putString("type", "scratchX");
        msgBundle.putBoolean("hasScratchXCommand", hasScratchXCommand);
        Message msg = new Message();
        msg.setData(msgBundle);
        callingActivity.getHandlerExtension().sendMessage(msg);
    }

    /**
     * disconnect the bluetooth connection to the Robot
     */
    private void callingActivityDisconnectBluetooth() {
        if (DEBUG_LOOP) {
            Log.wtf(LOG_TAG, "callingActivityDisconnectBluetooth from the Arduino Thread");
        }
        Bundle msgBundle = new Bundle();
        msgBundle.putString("type", "command");
        msgBundle.putString("command", "disconnectBluetooth");
        Message msg = new Message();
        msg.setData(msgBundle);
        callingActivity.getHandlerExtension().sendMessage(msg);
        try {
            Thread.sleep(ARDUINO_COMMAND_DELAY);
        } catch (InterruptedException ex)
        {
            if (MainActivity.DEBUG) {
                Log.wtf(LOG_TAG, "callingActivityDisconnectBluetooth error:" + ex.getMessage());
            }
        }
    }

    /////////////// C++ native routines, implemented in native-lib.cpp /////////////////

    public native byte[][] arduinoLoop(byte[] serialInput, byte[] bluetoothInput);

}
