package nccp.app.ui;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import nccp.app.R;
import nccp.app.parse.object.Course;
import nccp.app.parse.object.Program;
import nccp.app.parse.object.ProgramClass;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;


public class AttendanceFragment extends BaseFragment {

	public static final String TAG = AttendanceFragment.class.getSimpleName();
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy EEEE", Locale.US);
	
	// Views
	private RelativeLayout mAttendanceBannerView;
	private ScrollView mAttendanceScrollView;
	private TextView mTvEmpty;
	private Button mBtnDate;
	private Spinner mSpClass;
	private Spinner mSpCourse;
	private DatePickerFragment mDatePickerFragment;
	// Data
	private ProgramClass mCurrentClass = null;
	private ArrayAdapter<ProgramClass> mClassAdapter;
	private ArrayAdapter<Course> mCourseAdapter;
	private Calendar mDate = Calendar.getInstance(Locale.US);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mClassAdapter = new ArrayAdapter<ProgramClass>(getActivity(), android.R.layout.simple_spinner_item);
		mClassAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mCourseAdapter = new ArrayAdapter<Course>(getActivity(), android.R.layout.simple_spinner_item);
		mCourseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mDatePickerFragment = new DatePickerFragment();
		mDatePickerFragment.setCallback(mDateSetCallback);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_attendance, container, false);
		mAttendanceBannerView = (RelativeLayout) v.findViewById(R.id.attendance_banner);
		mAttendanceScrollView = (ScrollView) v.findViewById(R.id.attendance_scroller);
		mTvEmpty = (TextView) v.findViewById(R.id.attendance_empty_text);
		mBtnDate = (Button) v.findViewById(R.id.attendance_date_btn);
		mSpClass = (Spinner) v.findViewById(R.id.attendance_class_spinner);
		mSpClass.setOnItemSelectedListener(onClassSelectedListener);
		mSpClass.setAdapter(mClassAdapter);
		mSpCourse = (Spinner) v.findViewById(R.id.attendance_course_spinner);
		mSpCourse.setOnItemSelectedListener(onCourseSelectedListener);
		mSpCourse.setAdapter(mCourseAdapter);
		mBtnDate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mDatePickerFragment.setYear(mDate.get(Calendar.YEAR));
				mDatePickerFragment.setMonthOfYear(mDate.get(Calendar.MONTH));
				mDatePickerFragment.setDayOfMonth(mDate.get(Calendar.DAY_OF_MONTH));
				mDatePickerFragment.show(getChildFragmentManager(), "datePicker");
			}
		});
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		
		updateDateButton();
		updateViews(mCallback.getCurrentProgram());
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.attendance_program, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	
	public void setProgram(Program program) {
		if(getView() != null) {
			updateViews(program);
		}
	}
	
	private void updateViews(Program program) {
		if(program == null) { // No program
			mTvEmpty.setVisibility(View.VISIBLE);
			mTvEmpty.setText(R.string.no_programs);
			mAttendanceBannerView.setVisibility(View.INVISIBLE);
			mAttendanceScrollView.setVisibility(View.INVISIBLE);
			mClassAdapter.clear();
			mClassAdapter.notifyDataSetChanged();
			mCourseAdapter.clear();
			mCourseAdapter.notifyDataSetChanged();
		} else {
			mAttendanceBannerView.setVisibility(View.VISIBLE);
			
			updateBanner(program);
		}
	}

	private void updateBanner(Program program) {
		if(program == null) {
			return;
		}
		final List<ProgramClass> programClasses = program.getClasses();
		if(programClasses == null || programClasses.size() == 0) { // No class
			mAttendanceBannerView.setVisibility(View.INVISIBLE);
			mAttendanceScrollView.setVisibility(View.INVISIBLE);
			mTvEmpty.setVisibility(View.VISIBLE);
			mTvEmpty.setText(R.string.no_class);
			mClassAdapter.clear();
			mClassAdapter.notifyDataSetChanged();
		} else {
			mAttendanceBannerView.setVisibility(View.VISIBLE);
			// Fetch if needed
			if(!programClasses.get(0).isDataAvailable()) {
				mCallback.showProgress(true);
				ParseObject.fetchAllIfNeededInBackground(programClasses,
						new FindCallback<ProgramClass>() {
					@Override
					public void done(List<ProgramClass> data, ParseException e) {
						mCallback.showProgress(false);
						if(e == null) {
							updateClassSpinner(programClasses);
						} else {
							logAndToastException(TAG, e);
						}
					}
				});
			} else { // Already fetched
				updateClassSpinner(programClasses);
			}
		}
	}

	private void updateClassSpinner(List<ProgramClass> programClasses) {
		if(programClasses == null) {
			return;
		}
		// Update spinner adapter
		mClassAdapter.clear();
		mClassAdapter.addAll(programClasses);
		mClassAdapter.notifyDataSetChanged();
		
		if(programClasses.size() > 0) {
			// If spinner's current selected index == newindex,
			// OnItemSelectedListener would not be fired, then we need force update for program
			boolean forceUpdate = mSpClass.getSelectedItemPosition() == 0;
			mSpClass.setSelection(0);
			if(forceUpdate) {
				setCurrentProgramClass(programClasses.get(0));
			}
		}
	}
	
	private void setCurrentProgramClass(ProgramClass programClass) {
		mCurrentClass = programClass;
		onClassOrDateChanged();
	}
	
	private void onClassOrDateChanged() {
		if(mCurrentClass == null) {
			return;
		}
		List<Course> courses = mCurrentClass.getCourses();
		if(courses == null || courses.size() == 0) {
			mSpCourse.setVisibility(View.INVISIBLE);
			mTvEmpty.setVisibility(View.VISIBLE);
			mTvEmpty.setText(getString(R.string.no_course_this_class, mCurrentClass.getTitle()));
			mCourseAdapter.clear();
			mCourseAdapter.notifyDataSetChanged();
		} else {
			if(!courses.get(0).isDataAvailable()) { // Need to fetch from remove
				mCallback.showProgress(true);
				ParseObject.fetchAllIfNeededInBackground(courses, new FindCallback<Course>() {
					@Override
					public void done(List<Course> data, ParseException e) {
						mCallback.showProgress(false);
						if(e == null) {
							updateCourseSpinner(data);
						} else {
							logAndToastException(TAG, e);
						}
					}
				});
			} else {
				updateCourseSpinner(courses);
			}
		}
	}
	
	private void updateCourseSpinner(List<Course> courses) {
		if(courses == null) {
			return;
		}
		// Update spinner
		int dayOfWeek = mDate.get(Calendar.DAY_OF_WEEK);
		mCourseAdapter.clear();
		if(courses != null) {
			for(Course course : courses) {
				if(course.getDayOfWeek() == dayOfWeek) {
					mCourseAdapter.add(course);
				}
			}
		}
		mCourseAdapter.notifyDataSetChanged();
		
		if(mCourseAdapter.getCount() == 0) { // No course today
			mSpCourse.setVisibility(View.INVISIBLE);
			mAttendanceScrollView.setVisibility(View.INVISIBLE);
			mTvEmpty.setVisibility(View.VISIBLE);
			mTvEmpty.setText(getString(R.string.no_course_on_this_day));
		} else {
			mSpCourse.setVisibility(View.VISIBLE);
			mAttendanceScrollView.setVisibility(View.VISIBLE);
			mTvEmpty.setVisibility(View.INVISIBLE);
			
			boolean forceUpdate = mSpCourse.getSelectedItemPosition() == 0;
			mSpCourse.setSelection(0);
			if(forceUpdate) {
				showAttendanceSheet((Course) mCourseAdapter.getItem(0));
			}
		}
	}
	
	private void showAttendanceSheet(Course item) {
		// TODO Auto-generated method stub
		
	}

	private void updateDateButton() {
		String str = dateFormat.format(mDate.getTime());
		mBtnDate.setText(str);
	}

	private OnItemSelectedListener onClassSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			ProgramClass programClass = (ProgramClass) parent.getItemAtPosition(position);
			setCurrentProgramClass(programClass);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			
		}
	};
	
	private OnItemSelectedListener onCourseSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			Course course = (Course) parent.getItemAtPosition(position);
			showAttendanceSheet(course);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			
		}
	};
	
	private DatePickerFragment.Callback mDateSetCallback = new DatePickerFragment.Callback() {
		@Override
		public void onDateSet(int year, int monthOfYear, int dayOfMonth) {
			mDate.set(Calendar.YEAR, year);
			mDate.set(Calendar.MONTH, monthOfYear);
			mDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			updateDateButton();
			onClassOrDateChanged();
		}
	};
}
