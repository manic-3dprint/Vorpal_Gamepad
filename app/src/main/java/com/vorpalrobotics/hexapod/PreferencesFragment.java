package com.vorpalrobotics.hexapod;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;

import java.util.ArrayList;
import java.util.Set;

/**
 * Preferences Fragment for the app Preferences page
 */
public class PreferencesFragment extends PreferenceFragment
{
    /**
     * set the title of the bluetooth address preferrence
     * @param value
     */
    private void updateBluetoothAddressTitle(String value) {
        ListPreference bluetoothAddressPreference = (ListPreference) findPreference("bluetoothAddress");
        int bluetoothAddressIndex = bluetoothAddressPreference.findIndexOfValue(value);
        if (bluetoothAddressIndex != -1) {
            String bluetoothAddressValue =  bluetoothAddressPreference.getEntries()[bluetoothAddressIndex].toString();
            String titleText = getResources().getString(R.string.preferences_bluetooth_device) + " - ";
            String fullTitleText = titleText + bluetoothAddressValue;
            SpannableStringBuilder str = new SpannableStringBuilder(fullTitleText);
            str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), titleText.length(), fullTitleText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            bluetoothAddressPreference.setTitle(str);
//          bluetoothAddressPreference.setTitle(getResources().getString(R.string.preferences_bluetooth_device) + " - " + bluetoothAddressValue);
        }
    }

    /**
     * this method will display a list of all the Bluetooth devices that have been
     * paired to this Android device. The user will select the Robot from the list.
     */
    private void addBluetoothDevices()
    {
        BluetoothAdapter myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (myBluetooth != null) {
            ListPreference bluetoothListPref = new ListPreference(getActivity());
            Set<BluetoothDevice> pairedDevices = myBluetooth.getBondedDevices();
            ArrayList<CharSequence> entriesList = new ArrayList<>();
            ArrayList<CharSequence> entryValuesList = new ArrayList<>();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice bt : pairedDevices) {
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
 //           bluetoothListPref.setTitle(getResources().getString(R.string.preferences_bluetooth_device));
            bluetoothListPref.setSummary(getResources().getString(R.string.preferences_bluetooth_device_summary));
            bluetoothListPref.setPersistent(true);

            PreferenceCategory targetCategory = (PreferenceCategory) findPreference("MAIN_CATEGORY");
            targetCategory.addPreference(bluetoothListPref);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            updateBluetoothAddressTitle(prefs.getString("bluetoothAddress", ""));
            bluetoothListPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    updateBluetoothAddressTitle(newValue.toString());
                    return true;
                }
            });
        }
    }

    /**
     * set the title of the vorpal version preferrence
     * @param value
     */
    private void updateVorpalVersionTitle(String value) {
        ListPreference vorpalVersionPreference = (ListPreference) findPreference("vorpalVersion");
        int vorpalVersionIndex = vorpalVersionPreference.findIndexOfValue(value);
        if (vorpalVersionIndex != -1) {
            String vorpalVersionValue =  vorpalVersionPreference.getEntries()[vorpalVersionIndex].toString();
            String titleText = getResources().getString(R.string.preferences_vorpal_version) + " - ";
            String fullTitleText = titleText + vorpalVersionValue;
            SpannableStringBuilder str = new SpannableStringBuilder(fullTitleText);
            str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), titleText.length(), fullTitleText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            vorpalVersionPreference.setTitle(str);
//          vorpalVersionPreference.setTitle(getResources().getString(R.string.preferences_vorpal_version) + " - " + vorpalVersionValue);
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
        final ListPreference vorpalVersionPreference = (ListPreference) findPreference("vorpalVersion");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        updateVorpalVersionTitle(prefs.getString("vorpalVersion", "3"));
        vorpalVersionPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                updateVorpalVersionTitle(newValue.toString());
                return true;
            }
        });
    }
}
