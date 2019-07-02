package com.vorpalrobotics.hexapod;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * The Activity for the app Help page
 */
public class HelpActivity extends AppCompatActivity
{
	public static final String LOG_TAG = "DEBUG_HELP"; // key for debug messages in Logcat

	/**
	 * Called when the activity is starting. (see super)
	 * @param savedInstanceState (see super)
	 */
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);

		ActionBar actionBar = this.getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		PackageManager manager = this.getPackageManager();
        try
		{
        	PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
        	TextView tv = findViewById(R.id.help_version);
        	tv.setText(info.versionName);
        }
        catch (NameNotFoundException x)
		{
			if (MainActivity.DEBUG) {
				Log.wtf(LOG_TAG, "HelpActivity Exception " + x.getMessage());
			}
        }
	}

	@Override
	/**
	 * (see super)
	 * @param item The menu item that was selected.
     *
	 * @return boolean Return false to allow normal menu processing to
     *         proceed, true to consume it here.
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
		}
		return super.onOptionsItemSelected(item);
	}
}
