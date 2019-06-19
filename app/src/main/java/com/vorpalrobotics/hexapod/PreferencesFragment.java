package com.vorpalrobotics.hexapod;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

import java.util.ArrayList;
import java.util.Set;

/**
 * Preferences Fragment for the app Preferences page
 */
public class PreferencesFragment extends PreferenceFragment
{
    /**
     * this method will display a list of all the Bluetooth devices that have been
     * paired to this Android device. The user will select the Robot from the list.
     */
    private void addBluetoothDevices()
    {
        ListPreference bluetoothListPref = new ListPreference(getActivity());

        BluetoothAdapter myBluetooth = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = myBluetooth.getBondedDevices();
        ArrayList<CharSequence> entriesList = new ArrayList<>();
        ArrayList<CharSequence> entryValuesList = new ArrayList<>();

        if (pairedDevices.size()>0)
        {
            for(BluetoothDevice bt : pairedDevices)
            {
                entriesList.add(bt.getName() + " (" + bt.getAddress() + ")");
                entryValuesList.add(bt.getAddress());
            }
            entriesList.add(getResources().getString(R.string.bluetooth_disconnect));
            entryValuesList.add("");
        }
        CharSequence[] entries = new CharSequence[entriesList.size()];
        entries = entriesList.toArray(entries);
        CharSequence[] entryValues = new CharSequence[entryValuesList.size()];
        entryValues = entryValuesList.toArray(entryValues);

        bluetoothListPref.setKey("bluetoothAddress");
        bluetoothListPref.setEntries(entries);
        bluetoothListPref.setEntryValues(entryValues);
        bluetoothListPref.setTitle(getResources().getString(R.string.preferences_bluetooth_device));
        bluetoothListPref.setSummary(getResources().getString(R.string.preferences_bluetooth_device_summary));
        bluetoothListPref.setPersistent(true);

        PreferenceCategory targetCategory = (PreferenceCategory) findPreference("MAIN_CATEGORY");
        targetCategory.addPreference(bluetoothListPref);
    }
    
    /**
     * this method will determine if there is a wifi connection, and then display the
     * use ScratchX preference, displaying the IP address of the Android device
     */
    private void addScratchX() {
        String ipAddressString = Utils.getIPAddress(true);
        if (!"".equals(ipAddressString)) {
            CheckBoxPreference scratchPref = new CheckBoxPreference(getActivity());
            scratchPref.setKey("connectScratchX");
            scratchPref.setTitle(getResources().getString(R.string.preferences_connect_to_scratchx));
            scratchPref.setSummary(getResources().getString(R.string.preferences_connect_to_scratchx_summary) + " (" + ipAddressString + ")");
            PreferenceCategory targetCategory = (PreferenceCategory) findPreference("MAIN_CATEGORY");
            targetCategory.addPreference(scratchPref);
        }
    }

    /**
     * Called when the fragment is starting. (see super)
     * @param savedInstanceState (see super)
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        addBluetoothDevices();
        addScratchX();
        Preference doneButton = getPreferenceManager().findPreference("exitlink");
        if (doneButton != null)
        {
            doneButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    getActivity().finish();
                    return true;
                }
            });
        }
    }
}