package jp.miku39.android.kancolletimerlite;

import java.math.BigDecimal;
import java.util.ArrayList;

import jp.miku39.android.common.Lib;
import net.arnx.jsonic.JSON;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {
	final static String TAG = "GcmIntentService";

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) { // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
				Log.d(TAG, "Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				Log.d(TAG, "Deleted messages on server: " + extras.toString());
				// If it's a regular GCM message, do some work.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {

				// TODO プッシュ通知の受信処理を行う
				String source = extras.getString("message", "[]");
				Log.d(TAG, "Received: " + source);

				try {
					ArrayList<BigDecimal> timer_decoded = JSON.decode(source);
					ArrayList<Long> timer = new ArrayList<Long>();
					long now = System.currentTimeMillis() / 1000;
					for (int i = 0; i < timer_decoded.size(); i++) {
						Long tmp = timer_decoded.get(i).longValue();
						timer.add(tmp);

						// shared preferenceのtimer-0, timer-1, ... に完了時刻を保存する
						Lib.setLongValue(getApplicationContext(), "timer-" + i,
								tmp);

						if (tmp > now) {
							KanColleTimerMainActivity.setAlarm(this, tmp, i);
						}
					}
				} catch (Exception e) {
					Log.d(TAG, "Invalid JSON string from GCM.");
				}
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

}
