package com.vorpalrobotics.hexapod;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
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
import com.vorpalrobotics.hexapod.AppState.BluetoothState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * the Main Activity for the app
 */
public class MainActivity extends AppCompatActivity
{
    private static final int IS_SETTINGS_CHANGED_REQUEST = 1;  // The preferences request code
    private static final String LOG_TAG = "DEBUG_MAIN"; // key for debug messages in Logcat
    private static final String DEFAULT_MODE = "W1"; // initial mode
    private static final ArrayList<Character> MODE_ROWS = new ArrayList<>(Arrays.asList('W','D','F','R'));
    private static final ArrayList<Character> MODE_COLUMNS = new ArrayList<>(Arrays.asList('1','2','3','4'));
    private static final ArrayList<Character> DPAD_BUTTONS = new ArrayList<>(Arrays.asList('w','f','l','r','b'));
    private static final String NO_DPAD = "s"; // dPad not pressed
    private String modeButton = DEFAULT_MODE; // mode button last pressed
    private String dPadButton = NO_DPAD; // dPad button currently pressed

    private static final long TIMER_INTERVAL = 2000; // how often to call timer milliseconds
    private Timer timer = null; // timer called every TIMER_INTERVAL milliseconds
    private TimerTask timerTask; // Task for timer
    private final Handler timerHandler = new Handler(); // Handler for timer
    private View main = null; // view for this activity
    private AppState appState;
    // States for the Scratch Record/play function
//  private static final byte SREC_STOPPED    = 0;
//    private static final byte SREC_RECORDING  = 1;
//    private static final byte SREC_PLAYING    = 2;

    // States for the Gamepad record/play function
//  private static final byte GREC_STOPPED   = 0;
    private static final byte GREC_RECORDING = 1;
    private static final byte GREC_PLAYING   = 2;
    private static final byte GREC_PAUSED    = 3;
    private static final byte GREC_REWINDING = 4;
    private static final byte GREC_ERASING   = 5;

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
                        if (appState.isSound())
                        {
                            main.playSoundEffect(SoundEffectConstants.CLICK);
                        }
                        clickButton(buttonName, true);
                        if (Utils.DEBUG)
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
                        if (Utils.DEBUG)
                        {
                            Log.wtf(LOG_TAG, "Gamepad " + buttonName + " released");
                        }
                        if (buttonName.length() == 1 && DPAD_BUTTONS.indexOf(buttonName.charAt(0)) > -1)
                        {
                            dPadButton = NO_DPAD;
                        }
                        break;
                }
                displayAppState();
                v.performClick();
                return false;
            }
        });
    }

    /**
     * set up the buttons on the main page
     */
    private void activateMenu()
    {
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
        final ImageButton bluetoothButton = findViewById(R.id.button_id_BLUETOOTH);
        bluetoothButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (!appState.isConnectBluetoothAutomatically()) {
                    if (appState.getBluetoothState() == BluetoothState.CONNECTED) {
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
        setSDcard(prefs.getBoolean("SDcard", true));
        setVorpalVersion(Integer.parseInt(prefs.getString("vorpalVersion", "3")));
        appState.setSound(prefs.getBoolean("sound", true));
        appState.setBluetoothAddress(prefs.getString("bluetoothAddress", ""));
        appState.setConnectBluetoothAutomatically(prefs.getBoolean("connectBluetoothAutomatically", true));
        displayAppState();
    }

    /**
     * display the bluetooth icon in the correct state
     */
    private void displayBluetoothIcon() {
        ImageView bluetoothButton = findViewById(R.id.button_id_BLUETOOTH);
        int bluetoothStatusIcon = appState.getBluetoothState().getIconId();
        bluetoothButton.setImageResource(bluetoothStatusIcon);
    }

    /**
     * display the correct state icon
     */
    private void displayStateIcon() {
        int newStateIcon = 0; // no state icon
        if (appState.getTrimState() == (byte)1)
        {
            newStateIcon = R.drawable.trimming_state;
        } else if (appState.getGamepadState() == GREC_RECORDING)
        {
            newStateIcon = R.drawable.gamepad_recording_state;
        } else if (appState.getGamepadState() == GREC_PLAYING)
        {
            newStateIcon = R.drawable.gamepad_playing_state;
        } else if (appState.getGamepadState() == GREC_PAUSED)
        {
            newStateIcon = R.drawable.gamepad_paused_state;
        } else if (appState.getGamepadState() == GREC_REWINDING)
        {
            newStateIcon = R.drawable.gamepad_rewinding_state;
        } else if (appState.getGamepadState() == GREC_ERASING)
        {
            newStateIcon = R.drawable.gamepad_erasing_state;
        }
        ImageView stateIconView = findViewById(R.id.image_id_state);
        stateIconView.setImageResource(newStateIcon);
    }

    /**
     * set indicator icons and a user message on the bottom of the page from the app state
     */
    private void displayMessage()
    {
        if (appState.getBluetoothState() != BluetoothState.CONNECTED)
        {
            setMessage(appState.getBluetoothState().getMessageId(), R.color.colorMessageError);
        } else if (!appState.isPowerOn()) {
            setMessage(R.string.message_off, R.color.colorMessageError);
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
     * display the state of the application
     */
    public void displayAppState() {
        displayBluetoothIcon();
        displayStateIcon();
        displayMessage();
    }

    /**
     * check that the Android device supports Bluetooth and ask the user to turn it on
     * if it is off (necessary for the Application to run)
     * @return the device has Bluetooth
     */
    private boolean checkHasBluetooth()
    {
        BluetoothAdapter myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (myBluetooth == null)
        {
            // the device has no bluetooth adapter
            appState.setBluetoothState(BluetoothState.UNAVAILABLE);
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
        appState.setPaused(true);
    }

    /**
     * app is resumed (see super)
     */
    @Override
    protected void onResume()
    {
        super.onResume();
        appState.setPaused(false);
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
        appState = new AppState();
        setContentView(R.layout.activity_main);
        boolean hasBluetooth = checkHasBluetooth();
        activateMenu();
        if (hasBluetooth)
        {
            loadPreferences();
            ArduinoThread arduinoThread = new ArduinoThread(appState);
            arduinoThread.start();
            main = findViewById(R.id.gamepad_id);
            activateButtons();
            timerTask = initializeTimerTask();
            startTimer();
            setInternalFileDir(getFilesDir().getAbsolutePath());
            powerOff();
        } else {
            displayAppState();
        }
    }

    /**
     *  turn the power on (app)
     */
    private void powerOn()
    {
        if (Utils.DEBUG)
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
        if (Utils.DEBUG)
        {
            Log.wtf(LOG_TAG, "setup " + new String(serialOutput));
        }
        appState.setPowerOn(true);
        displayAppState();
    }

    /**
     * turn the power off (app)
     */
    private void powerOff()
    {
        appState.setPowerOn(false);
        Context context = getApplicationContext();
        Button powerOnButton = findViewById(R.id.button_id_POWER_ON);
        powerOnButton.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPowerButton));
        Button powerOffButton = findViewById(R.id.button_id_POWER_OFF);
        powerOffButton.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPowerButtonPressed));
        ConstraintLayout gamepadLayout = findViewById(R.id.gamepad_id);
        gamepadLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorGamepadOff));
        gamepadPowerOff();
        modeButton = DEFAULT_MODE;
        if (Utils.DEBUG)
        {
            Log.wtf(LOG_TAG, "Gamepad powerOff");
        }
        displayAppState();
    }

    /**
     * display the help page
     */
    private void help()
    {
        if (Utils.DEBUG)
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
        if (Utils.DEBUG)
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
            String saveBluetoothAddress = appState.getBluetoothAddress();
            loadPreferences();
            if (!appState.getBluetoothAddress().equals(saveBluetoothAddress))
            {
                disconnectBluetooth();
                // don't connect here
            }
        }
    }

    /**
     * set a user message at the bottom of the page
     * @param messageId the user message
     * @param colorId the message color
     */
    public void setMessage(int messageId, int colorId)
    {
        TextView message = findViewById(R.id.text_id_message);
        message.setText(messageId);
        message.setTextColor(ContextCompat.getColor(this, colorId));
    }

     // Bluetooth

    /**
     * attempt to connect to the Robot using bluetooth
     */
    private void connectBluetooth()
    {
        if (!"".equals(appState.getBluetoothAddress()))
        {
            new ConnectBT(appState).execute();
        }
    }

    /**
     * disconnect the bluetooth connection
     */
    public void disconnectBluetooth()
    {
        String status = appState.disconnectBluetooth();
        if (Utils.DEBUG)
        {
            if (status != null) {
                Log.wtf(LOG_TAG, "disconnectBluetooth error:" + status);
            } else {
                Log.wtf(LOG_TAG, "Bluetooth disconnected");
            }
        }
        displayAppState();
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
                        if (appState.isConnectBluetoothAutomatically() && (appState.getBluetoothState() == BluetoothState.DISCONNECTED || appState.getBluetoothState() == BluetoothState.ERROR) && !"".equals(appState.getBluetoothAddress()) && !appState.isPaused())
                        {
                            connectBluetooth();
                        }
                        displayAppState();
                    }
                });
            }
        };
    }

/////////////// C++ native routines, implemented in native-lib.cpp /////////////////

    public native byte[] arduinoSetup();

    public native void clickButton(String buttonName, boolean isOn);

    public native void gamepadPowerOn();

    public native void gamepadPowerOff();

    public native void setInternalFileDir(String internalFileDir);

    public native void setSDcard(boolean isSDcard);

    public native void setVorpalVersion(int vorpalVersion);
}
