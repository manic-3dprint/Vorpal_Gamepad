package com.vorpalrobotics.hexapod;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

/**
 * The Activity for the app Preferences page
 */
public class PreferencesActivity extends AppCompatActivity
{
    /**
     * Called when the activity is starting. (see super)
     * @param savedInstanceState (see super)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        if (findViewById(R.id.fragment_container) != null)
        {
            if (savedInstanceState != null)
            {
                return;
            }
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, new PreferencesFragment()).commit();
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
////        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }
}
