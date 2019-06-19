package com.vorpalrobotics.hexapod;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * The Activity for the app Help page
 */
public class HelpActivity extends Activity
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

	/**
	 * The user clicked the done button
	 * @param v the View
	 */
	public void clickDone(View v)
	{
		finish();
	}
}
