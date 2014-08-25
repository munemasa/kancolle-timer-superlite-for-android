package jp.miku39.android.kancolletimerlite;

import jp.miku39.android.common.Lib;
import android.app.Notification;
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
		//vibrate(context);

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
		case 3:
			msg = "ドック1の艦船の修理が完了しました。";
			break;
		case 4:
			msg = "ドック2の艦船の修理が完了しました。";
			break;
		case 5:
			msg = "ドック3の艦船の修理が完了しました。";
			break;
		case 6:
			msg = "ドック4の艦船の修理が完了しました。";
			break;
		default:
			msg = "完了しました。";
			break;
		}
		sendNotification(context, msg, n);
	}

	void vibrate(Context context) {
		try {
			if( !Lib.getPrefBool(context, "notify_vibration") ) return;

			Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
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

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context,
				TestActivity.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(context.getString(R.string.app_name)).setAutoCancel(true)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg)).setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		Notification notification = mBuilder.build();

		if( Lib.getPrefBool(context, "notify_sound") ){
			notification.defaults |= Notification.DEFAULT_SOUND;
		}
		if( Lib.getPrefBool(context, "notify_vibration") ){
			notification.defaults |= Notification.DEFAULT_VIBRATE;
		}
		if( Lib.getPrefBool(context, "notify_light") ){
			// デバイス次第か？
			notification.defaults |= Notification.DEFAULT_LIGHTS;
			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
			notification.ledARGB = 0xff00ff00;
			notification.ledOnMS = 300;
			notification.ledOffMS = 1000;
		}

		mNotificationManager.notify(NOTIFICATION_ID + n, notification);
	}

}
