package nccp.app.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import nccp.app.R;
import nccp.app.data.DataCenter;
import nccp.app.parse.object.Course;
import nccp.app.parse.object.Program;
import nccp.app.parse.object.ProgramClass;
import nccp.app.utils.Const;
import nccp.app.utils.Logger;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.parse.ParseException;

public class CourseEditorActivity extends ToolbarActivity {

	public static final String TAG = CourseEditorActivity.class.getSimpleName();
	
	private static final SimpleDateFormat courseTimeFormat = new SimpleDateFormat("h:mm a", Locale.US);
	
	private static final int DEFAULT_HOURS_OF_DAY = 9;
	private static final int DEFAULT_MINUTES = 0;
	private static final int DEFAULT_DURATION = 18;
	
	// Views
	private EditText mEtCourseName;
	private Spinner mSpDayOfWeek;
	private Button mBtnTime;
//	private EditText mEtDuration;
	private NumberPicker mNpDuration;
	private CourseTimePickerFragment mTimePicker;
	// Data
	private boolean mInProgress = false;
	private ProgramClass mProgramClass;
	private Course mCourse = null;
	private Calendar mTime = Calendar.getInstance(Locale.US);
	private String[] mDurationValues = getDurationValues();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_course_editor);
		initViews();
		initToolbar();
		initData();

		fillFields();
	}
	
	private void initViews() {
		mEtCourseName = (EditText) findViewById(R.id.edit_course_edittext);
		mSpDayOfWeek = (Spinner) findViewById(R.id.edit_course_dayofweek_spinner);
		mBtnTime = (Button) findViewById(R.id.edit_course_time_btn);
//		mEtDuration = (EditText) findViewById(R.id.edit_course_duration_edittext);
		mNpDuration = (NumberPicker) findViewById(R.id.edit_course_duration_numpicker);
		mNpDuration.setDisplayedValues(mDurationValues);
		mNpDuration.setMinValue(0);
		mNpDuration.setMaxValue(mDurationValues.length - 1);
		mNpDuration.setWrapSelectorWheel(false);
		mTimePicker = new CourseTimePickerFragment();
		mTimePicker.setCallback(mTimePickerCallback);
	}
	
	private static String[] getDurationValues() {
		List<String> list = new ArrayList<String>();
		for(int i = 0; i <= 1000; i += 5) {
			list.add(String.valueOf(i));
		}
		String[] arr = new String[list.size()];
		list.toArray(arr);
		return arr;
	}

	private void initToolbar() {
		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setHomeAsUpIndicator(R.drawable.ic_menu_check_24dp);
		ab.setTitle(R.string.title_edit_course);
	}
	
	private void initData() {
		Intent intent = getIntent();
		Program program = null;
		if(intent != null) {
			int programIndex = intent.getIntExtra(Const.EXTRA_PROGRAM_INDEX, -1);
			if(programIndex != -1) {
				program = DataCenter.getPrograms().get(programIndex);
			}
			int classIndex = intent.getIntExtra(Const.EXTRA_CLASS_INDEX, -1);
			if(classIndex != -1 && program != null) {
				mProgramClass = program.getClasses().get(classIndex);
			}
		}
		
		if(program == null || mProgramClass == null) {
			finish();
			return;
		}
		
		int courseIndex = intent.getIntExtra(Const.EXTRA_COURSE_INDEX, -1);
		if(courseIndex != -1) { // Edit course
			getSupportActionBar().setTitle(R.string.title_edit_course);
			mCourse = mProgramClass.getCourses().get(courseIndex);
			mTime.setTime(mCourse.getTime());
		} else { // New course
			getSupportActionBar().setTitle(R.string.title_new_course);
			mTime.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			mTime.set(Calendar.HOUR_OF_DAY, DEFAULT_HOURS_OF_DAY);
			mTime.set(Calendar.MINUTE, DEFAULT_MINUTES);
			mNpDuration.setValue(DEFAULT_DURATION);
		}
	}
	
	private void fillFields() {
		mSpDayOfWeek.setSelection(mTime.get(Calendar.DAY_OF_WEEK) - 1);
		updateTimeButton();
		
		if(mCourse != null) {
			String courseName = mCourse.getCourseName();
			if(courseName != null && courseName.length() > 0) {
				mEtCourseName.setText(courseName);
				mEtCourseName.setSelection(0, courseName.length());
			}
			mNpDuration.setValue(mCourse.getDuration() / 5);
//			mEtDuration.setText(String.valueOf(mCourse.getDuration()));
		}
	}

	private void updateTimeButton() {
		String timeStr = courseTimeFormat.format(mTime.getTime());
		mBtnTime.setText(timeStr);
	}
	
	public void onCourseTimeBtnClick(View v) {
		int hod = mTime.get(Calendar.HOUR_OF_DAY);
		int minute = mTime.get(Calendar.MINUTE);
		mTimePicker.updateTime(hod, minute);
		mTimePicker.show(getSupportFragmentManager(), "timePicker");
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == android.R.id.home) {
			handleDoneClicked();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void handleDoneClicked() {
		if(mInProgress) {
			return;
		}
		// Check name
		String courseName = mEtCourseName.getText().toString();
		if(courseName.length() == 0) {
			mEtCourseName.setError(getString(R.string.error_course_name_empty));
			return;
		}
		int dayOfWeek = mSpDayOfWeek.getSelectedItemPosition() + 1;
		int hourOfDay = mTime.get(Calendar.HOUR_OF_DAY);
		int minute = mTime.get(Calendar.MINUTE);
		// Check duration
//		String durationStr = mEtDuration.getText().toString();
//		if(durationStr.length() == 0) {
//			mEtDuration.setError(getString(R.string.error_course_duration_empty));
//			return;
//		}
		int duration = Integer.parseInt(mDurationValues[mNpDuration.getValue()]);
//		try {
//			duration = Integer.parseInt(durationStr);
//		} catch (NumberFormatException e) {
//			Logger.e(TAG, e.getMessage(), e);
//		}
//		if(duration == -1) {
//			mEtDuration.setError(getString(R.string.error_course_duration_invalid));
//			return;
//		}
		
		if(mCourse == null) {
			mCourse = new Course();
			mProgramClass.addCourse(mCourse);
		}
		mCourse.setCourseName(courseName);
		mCourse.setDayOfWeek(dayOfWeek);
		mCourse.setCourseTime(hourOfDay, minute);
		mCourse.setDuration(duration);
		Collections.sort(mProgramClass.getCourses());
		
		new SaveCourseTask(mProgramClass, mCourse).execute();
	}
	
	private void handleSaveSuccess() {
		Toast.makeText(CourseEditorActivity.this,
				getString(R.string.msg_course_saved), Toast.LENGTH_SHORT).show();
		setResult(RESULT_OK);
		finish();
	}

	private CourseTimePickerFragment.Callback mTimePickerCallback = new CourseTimePickerFragment.Callback() {
		@Override
		public void onTimeSet(int hourOfDay, int minute) {
			mTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
			mTime.set(Calendar.MINUTE, minute);
			updateTimeButton();
		}
	};

	public static class CourseTimePickerFragment extends DialogFragment
		implements TimePickerDialog.OnTimeSetListener {

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
			if(callback != null) {
				callback.onTimeSet(hourOfDay, minute);
			}
		}
	}
	
	private class SaveCourseTask extends AsyncTask<Void, Void, Void> {

		private ProgramClass programClass;
		private Course course;
		private ParseException e;
		
		public SaveCourseTask(ProgramClass programClass, Course course) {
			this.programClass = programClass;
			this.course = course;
		}

		@Override
		protected void onPreExecute() {
			mInProgress = true;
			showProgressBar(true);
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				course.save();
				programClass.save();
			} catch (ParseException e) {
				this.e = e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mInProgress = false;
			showProgressBar(false);
			if(e == null) { // Success
				handleSaveSuccess();
			} else { // Fail
				Logger.e(TAG, e.getMessage(), e);
				Toast.makeText(CourseEditorActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
	}
}
