package jp.miku39.android.kancolletimerlite;

import java.io.IOException;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class RegistrationActivity extends Activity {
	final static String TAG = "RegistrationActivity";
	private static String sRegisterUrl = "http://kancolletimer-gae.appspot.com/gcm/register";

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	public static final String PROPERTY_REG_ID = "registration_id";
	public static final String PROPERTY_KEY_1 = "key1";
	public static final String PROPERTY_KEY_2 = "key2";

	private static final String PROPERTY_APP_VERSION = "appVersion";

	GoogleCloudMessaging mGcm;
	SharedPreferences mPrefs;
	Context mContext;
	String mSenderId;
	String mGcmRegistrationId;
	String mKey1, mKey2;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.registration_activity_layout);

		mSenderId = getString(R.string.gcm_sender_id);
		mContext = this;

		final SharedPreferences prefs = getGCMPreferences(this);
		String key1 = prefs.getString(PROPERTY_KEY_1, "");
		String key2 = prefs.getString(PROPERTY_KEY_2, "");

		EditText et;
		et = (EditText) findViewById(R.id.edit_key1);
		et.setText(key1);
		et = (EditText) findViewById(R.id.edit_key2);
		et.setText(key2);

		Button btn = (Button) findViewById(R.id.btn_register);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (checkPlayServices()) {
					Log.d(TAG, "Ready to use Google Play services.");
					mGcm = GoogleCloudMessaging.getInstance(RegistrationActivity.this);
					mGcmRegistrationId = getRegistrationId(mContext);

					EditText et = (EditText) findViewById(R.id.edit_key1);
					mKey1 = et.getEditableText().toString();
					et = (EditText) findViewById(R.id.edit_key2);
					mKey2 = et.getEditableText().toString();

					registerInBackground();

					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		checkPlayServices();
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If it
	 * doesn't, display a dialog that allows users to download the APK from the
	 * Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {
		// This sample app persists the registration ID in shared preferences,
		// but
		// how you store the regID in your app is up to you.
		return getSharedPreferences(KanColleTimerMainActivity.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Object, Object, Object>() {

			@Override
			protected Object doInBackground(Object... params) {
				String msg = "";
				try {
					if (mGcm == null) {
						mGcm = GoogleCloudMessaging.getInstance(mContext);
					}
					mGcmRegistrationId = mGcm.register(mSenderId);
					msg = "Device registered, registration ID=" + mGcmRegistrationId;

					// You should send the registration ID to your server over
					// HTTP,
					// so it can use GCM/HTTP or CCS to send messages to your
					// app.
					// The request to your server should be authenticated if
					// your app
					// is using accounts.
					sendRegistrationIdToBackend();

					// For this demo: we don't need to send it because the
					// device
					// will send upstream messages to a server that echo back
					// the
					// message using the 'from' address in the message.

					// Persist the regID - no need to register again.
					storeRegistrationId(mContext, mGcmRegistrationId);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				}
				return msg;
			}
		}.execute(null, null, null);
	}

	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		Log.i(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	/**
	 * Sends the registration ID to your server over HTTP, so it can use
	 * GCM/HTTP or CCS to send messages to your app. Not needed for this demo
	 * since the device sends upstream messages to a server that echoes back the
	 * message using the 'from' address in the message.
	 */
	private void sendRegistrationIdToBackend() {
		Log.d(TAG, "GCM Registration Id: " + mGcmRegistrationId);

		HashMap<String, String> form = new HashMap<String, String>();
		form.put("key1", mKey1);
		form.put("key2", mKey2);
		form.put("registration_id", mGcmRegistrationId);
		String ret = Http.postRequest(sRegisterUrl, form);

		Log.d(TAG, ret);
		if (ret.contains("OK")) {
			final SharedPreferences prefs = getGCMPreferences(RegistrationActivity.this);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(PROPERTY_KEY_1, mKey1);
			editor.putString(PROPERTY_KEY_2, mKey2);
			editor.commit();

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(RegistrationActivity.this, R.string.registered,
							Toast.LENGTH_LONG).show();
				}
			});
		} else {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(RegistrationActivity.this, R.string.failed_to_register,
							Toast.LENGTH_LONG).show();
				}
			});
		}
	}

}
