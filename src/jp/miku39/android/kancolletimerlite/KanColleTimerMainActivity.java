package jp.miku39.android.kancolletimerlite;

import java.util.Calendar;

import jp.miku39.android.common.Lib;
import jp.miku39.android.kancolletimerlite.fragment.AboutDialogFragment;
import jp.miku39.android.kancolletimerlite.fragment.InputTimerDialogFragment;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class KanColleTimerMainActivity extends Activity implements
		InputTimerDialogFragment.Callback {
	final static String TAG = "KanColleTimerMainActivity";

	int mTimerSetButtonIds[] = { R.id.btn_set_fleet_2_remain, R.id.btn_set_fleet_3_remain,
			R.id.btn_set_fleet_4_remain, R.id.btn_set_dock_1_remain, R.id.btn_set_dock_2_remain,
			R.id.btn_set_dock_3_remain, R.id.btn_set_dock_4_remain, };
	int mTimerCancelButtonIds[] = { R.id.btn_cancel_1, R.id.btn_cancel_2, R.id.btn_cancel_3,
			R.id.btn_cancel_4, R.id.btn_cancel_5, R.id.btn_cancel_6, R.id.btn_cancel_7 };

	CountDownTimer[] mCountDownTimer;

	private final BroadcastReceiver mHandleGcmReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			KanColleTimerMainActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// GCMでタイマー更新がやってきたときにカウントダウンタイマーの更新を行う
					// AlarmManagerのセットはGCMレシーバー側ですでに行われているので不要
					updateCountDownTimer();
				}
			});
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 遠征で3つ、入渠で4つ
		mCountDownTimer = new CountDownTimer[7];

		initView();

		registerReceiver(mHandleGcmReceiver, new IntentFilter(Consts.ACTION_NOTIFY_GCM));
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateCountDownTimer();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mHandleGcmReceiver);

		for (int i = 0; i < mCountDownTimer.length; i++) {
			if (mCountDownTimer[i] != null) {
				mCountDownTimer[i].cancel();
			}
		}
		super.onDestroy();
	}

	/**
	 * Viewの初期設定を行う
	 */
	void initView() {
		Button btn;

		for (int i = 0; i < mTimerSetButtonIds.length; i++) {
			btn = (Button) findViewById(mTimerSetButtonIds[i]);

			final int ii = i;
			btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					enterRemainTime(ii);
				};
			});
		}

		for (int i = 0; i < mTimerCancelButtonIds.length; i++) {
			btn = (Button) findViewById(mTimerCancelButtonIds[i]);

			final int ii = i;
			btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					cancelTimer(ii);
				};
			});
		}
	}

	void cancelTimer(int n) {
		if (mCountDownTimer[n] != null)
			mCountDownTimer[n].cancel();

		final int ids[] = { R.id.tv_fleet_2_remain, R.id.tv_fleet_3_remain, R.id.tv_fleet_4_remain,
				R.id.tv_dock_1_remain, R.id.tv_dock_2_remain, R.id.tv_dock_3_remain,
				R.id.tv_dock_4_remain };
		TextView tv = (TextView) findViewById(ids[n]);
		tv.setText("00:00:00");

		saveTimer(0, n);
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
	 * タイマー設定を更新する
	 */
	void updateCountDownTimer() {
		for (int i = 0; i < mTimerSetButtonIds.length; i++) {
			long t = loadTimer(i);
			long now = System.currentTimeMillis() / 1000;
			if (t > now) {
				createCountDownTimer(i, t - now);
			}
		}
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
		final int ids[] = { R.id.tv_fleet_2_remain, R.id.tv_fleet_3_remain, R.id.tv_fleet_4_remain,
				R.id.tv_dock_1_remain, R.id.tv_dock_2_remain, R.id.tv_dock_3_remain,
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

	/**
	 * タイマーnの時間を設定する
	 * 
	 * @param n
	 *            タイマーの番号
	 */
	void enterRemainTime(int n) {
		String caption[] = { "第2艦隊の遠征", "第3艦隊の遠征", "第4艦隊の遠征", "ドック1の修理時間", "ドック2の修理時間",
				"ドック3の修理時間", "ドック4の修理時間", };
		String text = "時間を入力してください。";

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		DialogFragment newFragment = InputTimerDialogFragment.newInstance(caption[n], text, n);
		newFragment.show(ft, "dialog");
	}

	@Override
	public void onReturnValue(InputTimerDialogFragment frag, String time_str, int n) {
		// 時間入力ダイアログからのコールバック
		if (time_str.equals("0000"))
			return;

		long hours = Long.parseLong(time_str.substring(0, 2));
		long minutes = Long.parseLong(time_str.substring(2));
		long t = (hours * 60 + minutes) * 60;
		createCountDownTimer(n, t);

		long now = System.currentTimeMillis() / 1000;
		setAlarm(this, now + t, n);

		saveTimer(now + t, n);
	}

	/**
	 * アラーム時刻を保存する。
	 * 
	 * @param t
	 *            アラームの鳴る時刻をUNIX時間で。
	 * @param n
	 *            タイマーID
	 */
	void saveTimer(long t, int n) {
		String key = "timer-" + n;
		Lib.setLongValue(this, key, t);
	}

	/**
	 * 保存していたアラーム時刻を取得する。
	 * 
	 * @param n
	 *            タイマーID
	 * @return UNIX時間でアラーム時刻を返す
	 */
	Long loadTimer(int n) {
		String key = "timer-" + n;
		return Lib.getLongValue(this, key);
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
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);

		long now = System.currentTimeMillis();
		long target = t * 1000;
		long diff = (target - now) / 1000;

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.SECOND, (int) diff);

		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		// one shot
		am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);

		Log.d(TAG, "Start Alarm!");
	}

	/**
	 * Aboutダイアログを表示
	 */
	void showAbout() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		// Create and show the dialog.
		DialogFragment newFragment = AboutDialogFragment.newInstance();
		newFragment.show(ft, "dialog");
	}

	/**
	 * 設定アクティビティを表示
	 */
	void showPreferences() {
		Intent intent = new Intent(this, PreferenceActivity.class);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_sync_timer:
			startActivity(new Intent(this, RegistrationActivity.class));
			break;

		case R.id.menu_settings:
			showPreferences();
			break;

		case R.id.menu_about:
			showAbout();
			break;

		default:
			return super.onMenuItemSelected(featureId, item);
		}
		return true;
	}
}
