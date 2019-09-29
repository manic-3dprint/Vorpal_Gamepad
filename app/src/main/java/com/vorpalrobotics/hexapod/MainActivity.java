package com.vorpalrobotics.hexapod;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.Button;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * the Main Activity for the app
 */
public class MainActivity extends AppCompatActivity implements ConnectBTCaller, ArduinoThreadCaller
{
    public static final boolean DEBUG = false; // Set to true to get more debug messages
    private static final int IS_SETTINGS_CHANGED_REQUEST = 1;  // The preferences request code
    private static final String LOG_TAG = "DEBUG_MAIN"; // key for debug messages in Logcat
    private static final String DEFAULT_MODE = "W1"; // initial mode
    private boolean isPowerOn = false; // indicates whether the Gamepad power is on
    private static final ArrayList<Character> MODE_ROWS = new ArrayList<>(Arrays.asList('W','D','F','R'));
    private static final ArrayList<Character> MODE_COLUMNS = new ArrayList<>(Arrays.asList('1','2','3','4'));
    private static final ArrayList<Character> DPAD_BUTTONS = new ArrayList<>(Arrays.asList('w','f','l','r','b'));
    private static final String NO_DPAD = "s"; // dPad not pressed
    private String modeButton = DEFAULT_MODE; // mode button last pressed
    private String dPadButton = NO_DPAD; // dPad button currently pressed
    private boolean isConnectBluetoothAutomatically = true; // user preference to automatically connect to the Robot by bluetooth
    private boolean isConnectScratchX = false; // user preference to connect to scratchx
    private BluetoothSocket btSocket = null; // the Bluetooth socket of the bluetooth connection
    private String bluetoothAddress = ""; // bluetooth address of the connected Robot
    private boolean isBtConnecting = false; // if the Gamepad is in the process of connecting to the Robot by bluetooth
    private boolean isSound = true; // if the user selected the sound preference
    private boolean isPaused = false; // if the user is using another app on this Android device
    private boolean hasScratchXCommand = false; // command from scratchX
    public static final byte MESSAGE_ICON_BLUETOOTH = 0;
    public static final byte MESSAGE_ICON_STATE = 1;

    private static final long TIMER_INTERVAL = 2000; // how often to call timer milliseconds
    private Timer timer = null; // timer called every TIMER_INTERVAL milliseconds
    private TimerTask timerTask; // Task for timer
    private final Handler timerHandler = new Handler(); // Handler for timer
    private HandlerExtension messageHandler; // Handler for ArduinoThread messages
    private View main = null; // view for this activity

    // States for the Scratch Record/play function
    public static final byte SREC_STOPPED    = 0;
    public static final byte SREC_RECORDING  = 1;
    public static final byte SREC_PLAYING    = 2;

    // States for the Gamepad record/play function
    public static final byte  GREC_STOPPED   = 0;
    public static final byte  GREC_RECORDING = 1;
    public static final byte  GREC_PLAYING   = 2;
    public static final byte  GREC_PAUSED    = 3;
    public static final byte  GREC_REWINDING = 4;
    public static final byte  GREC_ERASING   = 5;

    static
    {
        System.loadLibrary("native-lib");
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    /**
     * set up a button listener for a button
     * @param buttonId the view id of the button
     * @param buttonName the name of the button
     */
    private void setButtonListener(final int buttonId, final String buttonName)
    {
        findViewById(buttonId).setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        if (isSound)
                        {
                            main.playSoundEffect(SoundEffectConstants.CLICK);
                        }
                        clickButton(buttonName, true);
                        if (DEBUG)
                        {
                            Log.wtf(LOG_TAG, "Gamepad " + buttonName + " pressed");
                        }
                        if (buttonName.length() == 2 && MODE_ROWS.indexOf(buttonName.charAt(0)) > -1 && buttonName.charAt(0) != 'R' && MODE_COLUMNS.indexOf(buttonName.charAt(1)) > -1)
                        {
                            modeButton = buttonName;
                        }
                        if (buttonName.length() == 1 && DPAD_BUTTONS.indexOf(buttonName.charAt(0)) > -1)
                        {
                            dPadButton = buttonName;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        clickButton(buttonName, false);
                        if (DEBUG)
                        {
                            Log.wtf(LOG_TAG, "Gamepad " + buttonName + " released");
                        }
                        if (buttonName.length() == 1 && DPAD_BUTTONS.indexOf(buttonName.charAt(0)) > -1)
                        {
                            dPadButton = NO_DPAD;
                        }
                        break;
                }
                setNormalMessage();
                v.performClick();
                return false;
            }
        });
    }

    /**
     * set up the buttons on the main page
     */
    private void activateButtons()
    {
        final Button powerOnButton = findViewById(R.id.button_id_POWER_ON);
        powerOnButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                powerOn();
            }
        });
        final Button powerOffButton = findViewById(R.id.button_id_POWER_OFF);
        powerOffButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                powerOff();
            }
        });
        final ImageButton helpButton = findViewById(R.id.button_id_HELP);
        helpButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                help();
            }
        });
        final ImageButton settingsButton = findViewById(R.id.button_id_SETTINGS);
        settingsButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                preferences();
            }
        });
        final ImageButton bluetoothButton = findViewById(R.id.button_id_BLUETOOTH);
        bluetoothButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (!isConnectBluetoothAutomatically) {
                    if (isBtConnected()) {
                        disconnectBluetooth();
                    } else {
                        connectBluetooth();
                    }
                }
            }
        });
        for (int row_loop = 0; row_loop < 4; row_loop++)
        {
            for (int column_loop = 0;column_loop < 4; column_loop++)
            {
                String buttonName = ""+MODE_ROWS.get(row_loop)+MODE_COLUMNS.get(column_loop);
                int resourceId = MainActivity.this.getResources().getIdentifier("button_id_"+buttonName, "id", MainActivity.this.getPackageName());
                setButtonListener(resourceId, buttonName);
            }
        }
        setButtonListener(R.id.button_id_SPECIAL, "w");
        setButtonListener(R.id.button_id_FORWARD, "f");
        setButtonListener(R.id.button_id_LEFT, "l");
        setButtonListener(R.id.button_id_RIGHT, "r");
        setButtonListener(R.id.button_id_BACKWARD, "b");
    }

    /**
     * load the user preferences set in the Preferences page
     */
    private void loadPreferences()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        boolean isSDcard = prefs.getBoolean("SDcard", true);
        setSDcard(isSDcard);
        isSound = prefs.getBoolean("sound", true);
        bluetoothAddress = prefs.getString("bluetoothAddress", "");
        isConnectBluetoothAutomatically = prefs.getBoolean("connectBluetoothAutomatically", true);
        isConnectScratchX = prefs.getBoolean("connectScratchX", false);
        setNormalMessage();
    }

    /**
     * set a user message on the bottom of the page from the app state
     */
    private void setNormalMessage()
    {
        if (btSocket == null && "".equals(bluetoothAddress))
        {
            setMessage(R.string.message_no_paired_device, R.color.colorMessageError);
        } else if (btSocket == null)
        {
            setMessage(R.string.message_no_connection, R.color.colorMessageError);
        } else if (!isPowerOn) {
            setMessage(R.string.message_off, R.color.colorMessageError);
        } else if (hasScratchXCommand)
        {
            setMessage(R.string.message_scratchx_control, R.color.colorMessageScratch);
        } else {
            String nameOfResource = "message_action_" + modeButton + dPadButton;
            int resourceId = MainActivity.this.getResources().getIdentifier(nameOfResource, "string", MainActivity.this.getPackageName());
            if (resourceId != 0)
            {
                setMessage(resourceId, R.color.colorMessage);
            }
        }
    }

    /**
     * check that the Android device supports Bluetooth and ask the user to turn it on
     * if it is off (necessary for the Application to run)
     * @return the device has Bluetooth
     */
    private boolean checkBluetooth()
    {
        BluetoothAdapter myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (myBluetooth == null)
        {
            // the device has no bluetooth adapter
            setMessage(R.string.message_no_bluetooth, R.color.colorMessageError);
            return false;
        }
        else if (!myBluetooth.isEnabled())
        {
            // ask the user turn the bluetooth on
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon,1);
        }
        return true;
    }

    /**
     * app is paused (see super)
     */
    @Override
    protected void onPause()
    {
        super.onPause();
        isPaused = true;
    }

    /**
     * app is resumed (see super)
     */
    @Override
    protected void onResume()
    {
        super.onResume();
        isPaused = false;
    }

    /**
     *
     * @return true if the app is paused, false otherwise
     */
    @Override
    public boolean isPaused()
    {
        return isPaused;
    }

    /**
     *  start the Timer task
     */
    private void startTimer()
    {
        if (timer == null) {
            //set a new Timer
            timer = new Timer();

            //schedule the timer
            timer.schedule(timerTask, TIMER_INTERVAL, TIMER_INTERVAL); //
        }
    }

    /**
     * stop the Timer task
     */
    private void stopTimer()
    {
        //stop the timer, if it's not already null
        if (timer != null)
        {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * Called when the activity is starting. (see super)
     * @param savedInstanceState (see super)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean hasBluetooth = checkBluetooth();
        if (hasBluetooth)
        {
            loadPreferences();
            messageHandler = new HandlerExtension(this);
            ArduinoThread arduinoThread = new ArduinoThread(this);
            arduinoThread.start();
            main = findViewById(R.id.gamepad_id);
            activateButtons();
            timerTask = initializeTimerTask();
            startTimer();
            setInternalFileDir(getFilesDir().getAbsolutePath());
            powerOff();
        }
    }

    /**
     *  turn the power on (app)
     */
    private void powerOn()
    {
        if (DEBUG)
        {
            Log.wtf(LOG_TAG, "Gamepad powerOn");
        }
        gamepadPowerOn();
        Context context = getApplicationContext();
        Button powerOnButton = findViewById(R.id.button_id_POWER_ON);
        powerOnButton.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPowerButtonPressed));
        Button powerOffButton = findViewById(R.id.button_id_POWER_OFF);
        powerOffButton.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPowerButton));
        ConstraintLayout gamepadLayout = findViewById(R.id.gamepad_id);
        gamepadLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorGamepadOn));
        byte[] serialOutput = arduinoSetup();
        if (DEBUG)
        {
            Log.wtf(LOG_TAG, "setup " + new String(serialOutput));
        }
        isPowerOn = true;
        setNormalMessage();
    }

    /**
     * turn the power off (app)
     */
    private void powerOff()
    {
        isPowerOn = false;
        Context context = getApplicationContext();
        Button powerOnButton = findViewById(R.id.button_id_POWER_ON);
        powerOnButton.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPowerButton));
        Button powerOffButton = findViewById(R.id.button_id_POWER_OFF);
        powerOffButton.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPowerButtonPressed));
        ConstraintLayout gamepadLayout = findViewById(R.id.gamepad_id);
        gamepadLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorGamepadOff));
        gamepadPowerOff();
        modeButton = DEFAULT_MODE;
        if (DEBUG)
        {
            Log.wtf(LOG_TAG, "Gamepad powerOff");
        }
        ImageView stateIcon = findViewById(R.id.image_id_state);
        stateIcon.setImageResource(0);
        setNormalMessage();
    }

    /**
     * display the help page
     */
    private void help()
    {
        if (DEBUG)
        {
            Log.wtf(LOG_TAG, "Gamepad help");
        }
        Intent i = new Intent(this, HelpActivity.class);
        startActivity(i);
    }

    /**
     * display the preferences page
     */
    private void preferences()
    {
        if (DEBUG)
        {
            Log.wtf(LOG_TAG, "Gamepad preferences");
        }
        Intent i = new Intent(this, PreferencesActivity.class);
        startActivityForResult(i, IS_SETTINGS_CHANGED_REQUEST);
    }

    /**
     * called on return from the preferences page to set the preferences in the app
     * @param requestCode (see super)
     * @param resultCode (see super)
     * @param data (see super)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == IS_SETTINGS_CHANGED_REQUEST)
        {
            String saveBluetoothAddress = bluetoothAddress;
            loadPreferences();
            if (!bluetoothAddress.equals(saveBluetoothAddress))
            {
                if ("".equals(bluetoothAddress))
                {
                    disconnectBluetooth();
                    setMessage(R.string.message_no_paired_device, R.color.colorMessageError);
                } else {
                    connectBluetooth();
                }
            }
        }
    }

    /**
     * set a user message at the bottom of the page
     * @param messageId the user message
     * @param colorId the message color
     */
    @Override
    public void setMessage(int messageId, int colorId)
    {
        TextView message = findViewById(R.id.text_id_message);
        message.setText(messageId);
        message.setTextColor(ContextCompat.getColor(this, colorId));
    }

    /**
     * see if the power is turned on (the app)
     * @return true if the power is on, false otherwise
     */
    @Override
    public boolean isPowerOn()
    {
        return isPowerOn;
    }

    /**
     * get the use ScratchX preference
     * @return the use ScratchX preference
     */
    @Override
    public boolean useScratchX()
    {
        return isConnectScratchX;
    }

    // Bluetooth

    /**
     * set the socket for the bluetooth connection to the Robot
     * @param btSocket_ the bluetooth socket
     */
    @Override
    public void setBluetoothSocket(BluetoothSocket btSocket_)
    {
        btSocket = btSocket_;
        ImageButton bluetoothButton = findViewById(R.id.button_id_BLUETOOTH);
        bluetoothButton.setImageResource(R.drawable.bluetooth_button_connected);
        setNormalMessage();
        if (DEBUG)
        {
            Log.wtf(LOG_TAG, "Bluetooth connected");
        }
    }

    /**
     * get the connect bluetooth automatically preference
     * @return the connect bluetooth automatically preference
     */
    @Override
    public boolean isConnectBluetoothAutomatically()
    {
        return isConnectBluetoothAutomatically;
    }

    /**
     * indicate that the app is not currently attempting to connect to the Robot by bluetooth
     */
    @Override
    public void setNotConnecting()
    {
        isBtConnecting = false;
    }

    /**
     * attempt to connect to the Robot using bluetooth
     */
    private void connectBluetooth()
    {
        if (bluetoothAddress == null)
        {
            setMessage(R.string.message_no_paired_device, R.color.colorMessageError);
        } else {
            new ConnectBT(bluetoothAddress, this).execute();
        }
    }

    /**
     * disconnect the bluetooth connection
     */
    public void disconnectBluetooth()
    {
        if (btSocket!=null)
        {
            try {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            {
                Log.wtf(LOG_TAG, "disconnectBluetooth error:" + e.getMessage());
                setMessage(R.string.message_communication_error, R.color.colorMessageError);
            }
            btSocket = null;
        }
        ImageButton bluetoothButton = findViewById(R.id.button_id_BLUETOOTH);
        bluetoothButton.setImageResource(R.drawable.bluetooth_button_disconnected);
        if (DEBUG)
        {
            Log.wtf(LOG_TAG, "Bluetooth disconnected");
        }
        setNormalMessage();
    }

    /**
     * see if the Robot is connected by bluetooth
     * @return true if the Robot is connected by bluetooth, false otherwise
     */
    @Override
    public boolean isBtConnected()
    {
        return btSocket != null;
    }

    /**
     * get the Bluetooth socket to the Robot
     * @return the BluetoothSocket
     */
    @Override
    public BluetoothSocket getBluetoothSocket()
    {
        return btSocket;
    }

    // Timer Task

    /**
     * a timer task to periodically attempt to connect to the Robot via bluetooth
     * if the user has selected the connect bluetooth automatically preference
     * @return the Timer task
     */
    private TimerTask initializeTimerTask()
    {
        return new TimerTask()
        {
            public void run()
            {
                timerHandler.post(new Runnable()
                {
                    public void run()
                    {
                        if (isConnectBluetoothAutomatically && !isBtConnected() && !isBtConnecting && !"".equals(bluetoothAddress) && !isPaused)
                        {
                            isBtConnecting = true;
                            connectBluetooth();
                        }
                    }
                });
            }
        };
    }

    // https://www.intertech.com/Blog/android-non-ui-to-ui-thread-communications-part-3-of-5/
    /**
     * a Handler extension to handle messages from the Android Thread
     */
    public static class HandlerExtension extends Handler {
        private static final byte[] EMPTY_STATUS = { 0, SREC_STOPPED, GREC_STOPPED };
        private int stateId = -1;

        private final WeakReference<MainActivity> currentActivity;

        /**
         * Constructor
         * @param activity the Main Activity
         */
        HandlerExtension(MainActivity activity){
            currentActivity = new WeakReference<>(activity);
        }

        /**
         * display the status icon from the status indicators from the Arduino Thread
         * @param status the status indicators from the Arduino Thread
         */
        private void displayStateIcon(byte[] status)
        {
            MainActivity activity = currentActivity.get();
            int newStateId = 0;
            String logMessage = "no State";
            if (activity.hasScratchXCommand)
            {
                newStateId = R.drawable.kisspng_scratch_cat;
                logMessage = "Scratch State command";
            } else if (status[0] == 1)
            {
                newStateId = R.drawable.trimming_state;
                logMessage = "Trimming State trimming";
            } else if (status[1] == SREC_RECORDING)
            {
                newStateId = R.drawable.kisspng_scratch_cat;
                logMessage = "Scratch State recording";
            } else if (status[1] == SREC_PLAYING)
            {
                newStateId = R.drawable.kisspng_scratch_cat;
                logMessage = "Scratch State playing";
            } else if (status[2] == GREC_RECORDING)
            {
                newStateId = R.drawable.gamepad_recording_state;
                logMessage = "Gamepad State recording";
            } else if (status[2] == GREC_PLAYING)
            {
                newStateId = R.drawable.gamepad_playing_state;
                logMessage = "Gamepad State playing";
            } else if (status[2] == GREC_PAUSED)
            {
                newStateId = R.drawable.gamepad_paused_state;
                logMessage = "Gamepad State paused";
            } else if (status[2] == GREC_REWINDING)
            {
                newStateId = R.drawable.gamepad_rewinding_state;
                logMessage = "Gamepad State rewinding";
            } else if (status[2] == GREC_ERASING)
            {
                newStateId = R.drawable.gamepad_erasing_state;
                logMessage = "Gamepad State erasing";
            }
            if (newStateId != stateId)
            {
                ImageView stateIcon = activity.findViewById(R.id.image_id_state);
                stateIcon.setImageResource(newStateId);
                stateId = newStateId;
                if (MainActivity.DEBUG)
                {
                    Log.wtf(LOG_TAG, "state icon " + logMessage);
                }
            }
        }

        /**
         * handle an icon message
         * @param message icon message sent from the Arduino Thread
         */
        private void handleIconMessage(Message message)
        {
            MainActivity activity = currentActivity.get();
            byte icon = message.getData().getByte("icon");
            byte[] status = message.getData().getByteArray("status");
            if (icon == MESSAGE_ICON_BLUETOOTH && status != null && status.length == 1)
            {
                ImageView bluetoothButton = activity.findViewById(R.id.button_id_BLUETOOTH);
                bluetoothButton.setImageResource(status[0] == 0 ? R.drawable.bluetooth_button_connected : R.drawable.bluetooth_button_error);
                if (DEBUG && status[0] == 1)
                {
                    Log.wtf(LOG_TAG, "bluetooth error");
                }
            } else if (icon == MESSAGE_ICON_STATE && status != null && status.length == 3)
            {
                displayStateIcon(status);
            }
        }

        /**
         * handle a command message
         * @param message command message sent from the Arduino Thread
         */
        private void handleCommandMessage(Message message)
        {
            String command = message.getData().getString("command");
            if ("disconnectBluetooth".equals(command))
            {
                MainActivity activity = currentActivity.get();
                activity.disconnectBluetooth();
            }
        }

        /**
         * handle a ScratchX message
         * @param message ScratchX message sent from the Arduino Thread
         */
        private void handleScratchXMessage(Message message)
        {
            MainActivity activity = currentActivity.get();
            activity.hasScratchXCommand = message.getData().getBoolean("hasScratchXCommand");
            activity.setNormalMessage();
            displayStateIcon(EMPTY_STATUS);
        }

        /**
         * handle a message sent from the Arduino Thread
         * @param message the message sent from the Arduino Thread
         */
        @Override
        public void handleMessage(Message message){
            if (currentActivity.get() != null)
            {
                String type = message.getData().getString("type");
                if ("icon".equals(type))
                {
                    handleIconMessage(message);
                } else if ("command".equals(type))
                {
                    handleCommandMessage(message);
                } else if ("scratchX".equals(type))
                {
                    handleScratchXMessage(message);
                }
            }
        }
    }

    /**
     *
     */
    @Override
    public HandlerExtension getHandlerExtension()
    {
        return messageHandler;
    }

/////////////// C++ native routines, implemented in native-lib.cpp /////////////////

    public native byte[] arduinoSetup();

    public native void clickButton(String buttonName, boolean isOn);

    public native void gamepadPowerOn();

    public native void gamepadPowerOff();

    public native void setInternalFileDir(String internalFileDir);

    public native void setSDcard(boolean isSDcard);
}
