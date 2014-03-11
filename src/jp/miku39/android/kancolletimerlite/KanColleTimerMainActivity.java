package jp.miku39.android.kancolletimerlite;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class KanColleTimerMainActivity extends Activity implements
		InputTimerDialogFragment.Callback {
	final static String TAG = "KanColleTimerMainActivity";

	CountDownTimer[] mCountDownTimer;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 遠征で3つ、入渠で4つ
		mCountDownTimer = new CountDownTimer[7];

		initView();
	}

	@Override
	protected void onDestroy() {
		for (int i = 0; i < mCountDownTimer.length; i++) {
			if (mCountDownTimer[i] != null) {
				mCountDownTimer[i].cancel();
			}
		}
		super.onDestroy();
	}

	/**
	 * 指定のTextViewに残り時間の文字列を設定する。
	 * 
	 * @param tv
	 * @param hour
	 * @param min
	 * @param sec
	 */
	void setCountDownTimeText(TextView tv, Long hour, Long min, Long sec) {
		String h = hour < 10 ? "0" + hour : hour.toString();
		String m = min < 10 ? "0" + min : min.toString();
		String s = sec < 10 ? "0" + sec : sec.toString();
		tv.setText(h + ":" + m + ":" + s);
	}

	/**
	 * カウントダウンタイマーを作成して、開始する。
	 * 
	 * @param n
	 *            0:第2艦隊、1:第3艦隊、2:第4艦隊、3:入渠ドック1、...の0-6
	 * @param t
	 *            カウントダウン完了までの秒数
	 */
	void createCountDownTimer(final int n, long t) {
		final int ids[] = { R.id.tv_fleet_2_remain, R.id.tv_fleet_3_remain,
				R.id.tv_fleet_4_remain, R.id.tv_dock_1_remain,
				R.id.tv_dock_2_remain, R.id.tv_dock_3_remain,
				R.id.tv_dock_4_remain };

		if (mCountDownTimer[n] != null)
			mCountDownTimer[n].cancel();

		mCountDownTimer[n] = new CountDownTimer(t * 1000, 1000) {
			@Override
			public void onTick(long millisUntilFinished) {
				TextView tv = (TextView) findViewById(ids[n]);
				long tmp = millisUntilFinished / 1000;
				long min = tmp / 60 % 60;
				long hour = tmp / (60 * 60);
				long sec = tmp % 60;
				setCountDownTimeText(tv, hour, min, sec);
			}

			@Override
			public void onFinish() {
				TextView tv = (TextView) findViewById(ids[n]);
				tv.setText("Finished!");
			}
		}.start();
	}

	void initView() {
		Button btn;

		int ids[] = { R.id.btn_set_fleet_2_remain, R.id.btn_set_fleet_3_remain,
				R.id.btn_set_fleet_4_remain, R.id.btn_set_dock_1_remain,
				R.id.btn_set_dock_2_remain, R.id.btn_set_dock_3_remain,
				R.id.btn_set_dock_4_remain, };

		for (int i = 0; i < ids.length; i++) {
			btn = (Button) findViewById(ids[i]);

			final int ii = i;
			btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					enterRemainTime(ii);
				};
			});
		}

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
	 * タイマーnの時間を設定する
	 * 
	 * @param n
	 *            タイマーの番号
	 */
	void enterRemainTime(int n) {
		String caption[] = { "第2艦隊の遠征", "第3艦隊の遠征", "第4艦隊の遠征", "ドック1の修理時間",
				"ドック2の修理時間", "ドック3の修理時間", "ドック4の修理時間", };
		String text = "時間を入力してください。";

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		DialogFragment newFragment = InputTimerDialogFragment.newInstance(
				caption[n], text, n);
		newFragment.show(ft, "dialog");
	}

	@Override
	public void onReturnValue(InputTimerDialogFragment frag, String time_str,
			int n) {
		// 時間入力ダイアログからのコールバック
		if (time_str.equals("0000"))
			return;

		long hours = Long.parseLong(time_str.substring(0, 2));
		long minutes = Long.parseLong(time_str.substring(2));
		long t = (hours * 60 + minutes) * 60;
		createCountDownTimer(n, t);

		long now = System.currentTimeMillis() / 1000;
		setAlarm(this, now + t, n);
	}

	/**
	 * アラーム時刻をUNIX時間で指定する。
	 * 
	 * @param context
	 * @param t
	 *            アラームをしかける時刻をUNIX時間で。
	 * @param n
	 *            タイマーの番号
	 */
	static void setAlarm(Context context, long t, int n) {
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.setAction("timer-" + n);
		PendingIntent sender = PendingIntent
				.getBroadcast(context, 0, intent, 0);

		long now = System.currentTimeMillis();
		long target = t * 1000;
		long diff = (target - now) / 1000;

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.SECOND, (int) diff);

		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		// one shot
		am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);

		Log.d(TAG, "Start Alarm!");
	}

}
