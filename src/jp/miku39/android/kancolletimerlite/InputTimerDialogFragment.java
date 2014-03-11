package jp.miku39.android.kancolletimerlite;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

public class InputTimerDialogFragment extends DialogFragment {
	final static String TAG = "InputTimerFragment";

	String mCaption;
	String mText;

	public interface Callback {
		public void onReturnValue(InputTimerDialogFragment frag, String str);
	}

	public static InputTimerDialogFragment newInstance(String caption,
			String text) {
		InputTimerDialogFragment f = new InputTimerDialogFragment();
		Bundle args = new Bundle();
		args.putString("caption", caption);
		args.putString("text", text);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mCaption = getArguments().getString("caption");
		mText = getArguments().getString("text");
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final View v = LayoutInflater.from(getActivity()).inflate(
				R.layout.input_timer_layout, null);

		NumberPicker numPicker;
		int ids[] = { R.id.numberPicker_hour1, R.id.numberPicker_hour10,
				R.id.numberPicker_min1, R.id.numberPicker_min10, };
		for (int i = 0; i < ids.length; i++) {
			numPicker = (NumberPicker) v.findViewById(ids[i]);
			numPicker.setMinValue(0);
			if (ids[i] == R.id.numberPicker_min10) {
				numPicker.setMaxValue(5);
			} else {
				numPicker.setMaxValue(9);
			}
		}

		return new AlertDialog.Builder(getActivity())
				.setIcon(R.drawable.ic_launcher)
				.setTitle(mCaption)
				.setMessage(mText)
				.setView(v)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// ((Callback) getActivity()).onReturnValue(
								// InputTimerFragment.this, str);
								dismiss();
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dismiss();
							}
						}).create();
	}

}
