package nccp.app.ui;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.parse.ParseException;
import com.parse.SaveCallback;

import nccp.app.R;
import nccp.app.parse.object.Attendance;
import nccp.app.parse.object.Course;
import nccp.app.parse.object.Student;
import nccp.app.parse.proxy.AttendanceProxy;
import nccp.app.parse.proxy.CourseProxy;
import nccp.app.parse.proxy.StudentProxy;
import nccp.app.utils.Const;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class AttendanceEditorActivity extends ToolbarActivity {

	public static final String TAG = AttendanceEditorActivity.class.getSimpleName();
	
	private static final SimpleDateFormat dateFormat =
			new SimpleDateFormat("M/d/yyyy EEEE", Locale.US);
	private static final SimpleDateFormat timeFormat =
			new SimpleDateFormat("h:m a", Locale.US);
	
	// Views
	private TextView mTvStudentName;
	private TextView mTvCourse;
	private TextView mTvDate;
	private RadioGroup mRgAttended;
	private LinearLayout mTimeLayout;
	private Button mBtnTimeIn;
	private Button mBtnTimeOut;
	private EditText mEtComment;
	private TimePickerFragment mTimeInDialog;
	private TimePickerFragment mTimeOutDialog;
	// Date
	private Student mStudent = null;
	private Course mCourse = null;
	private Date mDate = null;
	private Attendance mAttendance = null;
	private Calendar mTimeIn = Calendar.getInstance(Locale.US);
	private boolean mTimeInSet = false;
	private Calendar mTimeOut = Calendar.getInstance(Locale.US);
	private boolean mTimeOutSet = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_attendance_editor);
		initViews();
		initToolbar();
		initData();
		fillFields();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == android.R.id.home) {
			handleSaveAttendance();
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void initViews() {
		mTvStudentName = (TextView) findViewById(R.id.attendance_editor_student_text);
		mTvCourse = (TextView) findViewById(R.id.attendance_editor_course_text);
		mTvDate = (TextView) findViewById(R.id.attendance_editor_date_text);
		mRgAttended = (RadioGroup) findViewById(R.id.attendance_editor_attended_rg);
		mRgAttended.setOnCheckedChangeListener(onCheckedChangeListener);
		mTimeLayout = (LinearLayout) findViewById(R.id.attendance_time_layout);
		mBtnTimeIn = (Button) findViewById(R.id.attendance_time_in_btn);
		mBtnTimeOut = (Button) findViewById(R.id.attendance_time_out_btn);
		mEtComment = (EditText) findViewById(R.id.attendance_editor_comment_edittext);
		mTimeInDialog = new TimePickerFragment();
		mTimeInDialog.setCallback(new TimePickerFragment.Callback() {
			@Override
			public void onTimeSet(int hourOfDay, int minute) {
				mTimeInSet = true;
				mTimeIn.set(Calendar.HOUR_OF_DAY, hourOfDay);
				mTimeIn.set(Calendar.MINUTE, minute);
				mBtnTimeIn.setText(timeFormat.format(mTimeIn.getTime()));
			}
		});
		mTimeOutDialog = new TimePickerFragment();
		mTimeOutDialog.setCallback(new TimePickerFragment.Callback() {
			@Override
			public void onTimeSet(int hourOfDay, int minute) {
				mTimeOutSet = true;
				mTimeOut.set(Calendar.HOUR_OF_DAY, hourOfDay);
				mTimeOut.set(Calendar.MINUTE, minute);
				mBtnTimeOut.setText(timeFormat.format(mTimeOut.getTime()));
			}
		});
	}

	private void initToolbar() {
		ActionBar ab = getSupportActionBar();
		ab.setTitle(R.string.title_attendance_editor);
		ab.setHomeButtonEnabled(true);
		ab.setHomeAsUpIndicator(R.drawable.ic_menu_check_24dp);
	}

	private void initData() {
		Intent intent = getIntent();
		if(intent != null) {
			StudentProxy studentProxy = (StudentProxy) intent.getSerializableExtra(Const.EXTRA_STUDENT);
			mStudent = StudentProxy.toParseObject(studentProxy);
			
			CourseProxy courseProxy = (CourseProxy) intent.getSerializableExtra(Const.EXTRA_COURSE);
			mCourse = CourseProxy.toParseObject(courseProxy);
			
			mDate = new Date(intent.getLongExtra(Const.EXTRA_DATE, 0));
			
			AttendanceProxy attendanceProxy = (AttendanceProxy) intent.getSerializableExtra(Const.EXTRA_ATTENDANCE);
			mAttendance = AttendanceProxy.toParseObject(attendanceProxy);
			
			if(mAttendance != null) {
				Date timeIn = mAttendance.getTimeIn();
				if(timeIn != null) {
					mTimeInSet = true;
					mTimeIn.setTime(timeIn);
				}
				Date timeOut = mAttendance.getTimeOut();
				if(timeOut != null) {
					mTimeOutSet = true;
					mTimeOut.setTime(timeOut);
				}
			}
		}
		
		if(mStudent == null || mCourse == null) { // We need these two extras
			finish();
		}
	}

	private void fillFields() {
		if(mStudent != null) {
			mTvStudentName.setText(mStudent.getFullName());
		}
		if(mCourse != null) {
			mTvCourse.setText(mCourse.getCourseName());
		}
		if(mDate != null) {
			mTvDate.setText(dateFormat.format(mDate));
		}
		if(mAttendance != null) {
			mRgAttended.check(mAttendance.isAttended() ?
			R.id.attendance_editor_rb_attended : R.id.attendance_editor_rb_notattended);
			
			if(mTimeInSet) {
				mBtnTimeIn.setText(timeFormat.format(mTimeIn.getTime()));
			}
			
			if(mTimeOutSet) {
				mBtnTimeOut.setText(timeFormat.format(mTimeOut.getTime()));
			}
			
			mEtComment.setText(mAttendance.getComment());
		}
	}

	private void handleSaveAttendance() {
		int checkedId = mRgAttended.getCheckedRadioButtonId();
		if(checkedId == -1) { // Unchecked
			Toast.makeText(AttendanceEditorActivity.this,
					R.string.msg_attendance_not_checked, Toast.LENGTH_SHORT).show();
			return;
		}
		
		boolean attended = false;
		if(checkedId == R.id.attendance_editor_rb_attended) {
			attended = true;
		} else if(checkedId == R.id.attendance_editor_rb_notattended) {
			attended = false;
		}
		
		if(mAttendance == null) { // New attendance
			mAttendance = new Attendance();
			mAttendance.setCourse(mCourse);
			mAttendance.setStudent(mStudent);
			mAttendance.setDate(mDate);
		}
		
		mAttendance.setAttended(attended);
		if(attended) { // Set time in and time out
			if(mTimeInSet) {
				mAttendance.setTimeIn(mTimeIn.getTime());
			}
			if(mTimeOutSet) {
				mAttendance.setTimeOut(mTimeOut.getTime());
			}
		} else { // Remove time in and time out
			mAttendance.removeTimeIn();
			mAttendance.removeTimeOut();
		}
		
		mAttendance.setComment(mEtComment.getText().toString());
		
		// Save attendance
		showProgressBar(true);
		mAttendance.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				showProgressBar(false);
				if(e == null) { // Success
					setResult(RESULT_OK);
					finish();
				} else { // Fail
					logAndToastException(TAG, e);
				}
			}
		});
	}
	
	public void onTimeInBtnClick(View v) {
		mTimeInDialog.updateTime(mTimeIn.get(Calendar.HOUR_OF_DAY), mTimeIn.get(Calendar.MINUTE));
		mTimeInDialog.show(getSupportFragmentManager(), "timeInPicker");
	}
	
	public void onTimeOutBtnClick(View v) {
		mTimeOutDialog.updateTime(mTimeOut.get(Calendar.HOUR_OF_DAY), mTimeOut.get(Calendar.MINUTE));
		mTimeOutDialog.show(getSupportFragmentManager(), "timeOutPicker");
	}
	
	private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			if(checkedId == R.id.attendance_editor_rb_attended) {
				mTimeLayout.setVisibility(View.VISIBLE);
			} else if(checkedId == R.id.attendance_editor_rb_notattended) {
				mTimeLayout.setVisibility(View.GONE);
			}
			mEtComment.setVisibility(View.VISIBLE);
		}
	};
}
