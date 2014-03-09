package jp.miku39.android.kancolletimerlite;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;

import jp.miku39.android.common.Lib;
import net.arnx.jsonic.JSON;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
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
							setAlarm(tmp, i);
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

	/**
	 * アラーム時刻をUNIX時間で指定する。
	 * 
	 * @param t
	 *            アラームを鳴らす時刻をUNIX時間で。
	 * @param n
	 */
	void setAlarm(long t, int n) {
		Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
		intent.setAction("timer-"+n);
		PendingIntent sender = PendingIntent.getBroadcast(
				getApplicationContext(), 0, intent, 0);

		long now = System.currentTimeMillis();
		long target = t * 1000;
		long diff = (target - now) / 1000;

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.SECOND, (int) diff);

		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		// one shot
		am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
	}

}
