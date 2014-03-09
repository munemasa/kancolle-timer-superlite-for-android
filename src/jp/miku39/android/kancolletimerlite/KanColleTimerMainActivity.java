package jp.miku39.android.kancolletimerlite;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class KanColleTimerMainActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initView();
	}

	void initView() {
		Button btn;

		btn = (Button) findViewById(R.id.btn_set_fleet_2_remain);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setAlarm(0);
			}
		});

		btn = (Button) findViewById(R.id.btn_test);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(KanColleTimerMainActivity.this,
						TestActivity.class);
				startActivity(intent);
			}
		});
	}

	/**
	 * アラーム時刻をUNIX時間で指定する。
	 * 
	 * @param t
	 */
	void setAlarm(long t) {
		Intent intent = new Intent(this, AlarmReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(this, 0,
				intent, 0);

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.SECOND, 10);

		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		// one shot
		alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
				sender);

		Toast.makeText(this, "Start Alarm!", Toast.LENGTH_SHORT)
				.show();
	}

}
