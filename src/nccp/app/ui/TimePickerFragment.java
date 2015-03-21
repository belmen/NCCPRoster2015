package nccp.app.ui;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

public class TimePickerFragment extends DialogFragment implements
		TimePickerDialog.OnTimeSetListener {
	private int hourOfDay;
	private int minute;

	public interface Callback {
		void onTimeSet(int hourOfDay, int minute);
	}

	private Callback callback;

	public void setCallback(Callback callback) {
		this.callback = callback;
	}

	public void updateTime(int hourOfDay, int minute) {
		this.hourOfDay = hourOfDay;
		this.minute = minute;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new TimePickerDialog(getActivity(), this, hourOfDay, minute,
				DateFormat.is24HourFormat(getActivity()));
	}

	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		TimePickerDialog dialog = (TimePickerDialog) getDialog();
		dialog.updateTime(hourOfDay, minute);
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		if (callback != null) {
			callback.onTimeSet(hourOfDay, minute);
		}
	}
}
