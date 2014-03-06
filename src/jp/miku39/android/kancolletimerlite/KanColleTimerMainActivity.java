package jp.miku39.android.kancolletimerlite;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class KanColleTimerMainActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initView();
	}

	void initView() {
		Button btn = (Button) findViewById(R.id.btn_test);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(KanColleTimerMainActivity.this,
						TestActivity.class);
				startActivity(intent);
			}
		});
	}

}
