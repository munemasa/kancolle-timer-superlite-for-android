package jp.miku39.android.kancolletimerlite;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
	final static String TAG = "AlarmReceiver";

	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;

	@Override
	public void onReceive(Context context, Intent intent) {
		vibrate(context);

		// AlarmのActionは"timer-X"となっている
		String action = intent.getAction();
		String number = action.substring("timer-".length());

		String msg;
		int n = Integer.parseInt(number, 10);
		switch (n) {
		case 0:
			msg = "第2艦隊が遠征から帰還しました。";
			break;
		case 1:
			msg = "第3艦隊が遠征から帰還しました。";
			break;
		case 2:
			msg = "第4艦隊が遠征から帰還しました。";
			break;
		default:
			msg = "完了しました。";
			break;
		}
		sendNotification(context, msg, n);
	}

	void vibrate(Context context) {
		try {
			Vibrator vibrator = (Vibrator) context
					.getSystemService(Context.VIBRATOR_SERVICE);
			long[] pattern = { 250, 1000, 500, 1000, 500, 1000 };
			vibrator.vibrate(pattern, -1);
		} catch (Exception e) {
		}
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void sendNotification(Context context, String msg, int n) {
		mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				new Intent(context, TestActivity.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(context.getString(R.string.app_name))
				.setAutoCancel(true)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID + n, mBuilder.build());
	}

}
