package nccp.app.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment
	implements DatePickerDialog.OnDateSetListener {

	public interface Callback {
		void onDateSet(int year, int monthOfYear, int dayOfMonth);
	}

	private int year;
	private int monthOfYear;
	private int dayOfMonth;
	private Callback callback = null;

	public void setCallback(Callback callback) {
		this.callback = callback;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public void setMonthOfYear(int monthOfYear) {
		this.monthOfYear = monthOfYear;
	}

	public void setDayOfMonth(int dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new DatePickerDialog(getActivity(), this, year, monthOfYear, dayOfMonth);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		DatePickerDialog dialog = (DatePickerDialog) getDialog();
		dialog.updateDate(year, monthOfYear, dayOfMonth);
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		if (callback != null) {
			callback.onDateSet(year, monthOfYear, dayOfMonth);
		}
	}
}