package jp.miku39.android.kancolletimerlite;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Toast.makeText(context, "Alarm Received!", Toast.LENGTH_SHORT).show();
		Log.d("AlarmReceiver",
				"Alarm Received! : "
						+ intent.getIntExtra(Intent.EXTRA_ALARM_COUNT, 0));

		vibrate(context);
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

}
