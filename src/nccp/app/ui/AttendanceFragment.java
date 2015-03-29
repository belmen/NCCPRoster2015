package nccp.app.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import nccp.app.R;
import nccp.app.adapter.AttendanceAdapter;
import nccp.app.data.DataCache;
import nccp.app.parse.object.Attendance;
import nccp.app.parse.object.Course;
import nccp.app.parse.object.Program;
import nccp.app.parse.object.ProgramClass;
import nccp.app.parse.object.Student;
import nccp.app.parse.proxy.AttendanceProxy;
import nccp.app.parse.proxy.CourseProxy;
import nccp.app.parse.proxy.StudentProxy;
import nccp.app.utils.Const;
import nccp.app.utils.Logger;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;


public class AttendanceFragment extends BaseFragment {

	public static final String TAG = AttendanceFragment.class.getSimpleName();
	
	private static final int REQUEST_EDIT_ATTENDANCE = 0;
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy EEE", Locale.US);
	
	// Views
	private RelativeLayout mAttendanceBannerView;
	private ListView mLvAttendance;
	private TextView mTvEmpty;
	private Button mBtnDate;
	private ImageButton mIbDateLeft;
	private ImageButton mIbDateRight;
	private Spinner mSpClass;
	private Spinner mSpCourse;
	private DatePickerFragment mDatePickerFragment;
	// Data
//	private boolean mChanged = false;
	private ProgramClass mCurrentClass = null;
	private List<Student> mCurrentStudents = null;
	private Course mCurrentCourse = null;
	private CourseProxy mCurrentCourseProxy = null;
	private ArrayAdapter<String> mClassAdapter;
	private ArrayAdapter<String> mCourseAdapter;
	private AttendanceAdapter mAttendanceAdapter;
	private Calendar mDate = Calendar.getInstance(Locale.US);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mClassAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item);
		mClassAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mCourseAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item);
		mCourseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mDatePickerFragment = new DatePickerFragment();
		mDatePickerFragment.setCallback(mDateSetCallback);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_attendance, container, false);
		mAttendanceBannerView = (RelativeLayout) v.findViewById(R.id.attendance_banner);
		mLvAttendance = (ListView) v.findViewById(R.id.attendance_listview);
		mLvAttendance.setOnItemClickListener(onAttendanceClick);
		mAttendanceAdapter = new AttendanceAdapter(getActivity());
		mLvAttendance.setAdapter(mAttendanceAdapter);
		mTvEmpty = (TextView) v.findViewById(R.id.attendance_empty_text);
		mBtnDate = (Button) v.findViewById(R.id.attendance_date_btn);
		mIbDateLeft = (ImageButton) v.findViewById(R.id.attendance_date_left_btn);
		mIbDateRight = (ImageButton) v.findViewById(R.id.attendance_date_right_btn);
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
		
		OnClickListener onDateLeftRightClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				int id = v.getId();
				if(id == R.id.attendance_date_left_btn) {
					mDate.add(Calendar.DATE, -1);
				} else if(id == R.id.attendance_date_right_btn) {
					mDate.add(Calendar.DATE, 1);
				}
				updateDateButton();
				updateCourseSpinner();
			}
		};
		mIbDateLeft.setOnClickListener(onDateLeftRightClick);
		mIbDateRight.setOnClickListener(onDateLeftRightClick);
		
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
//		setHasOptionsMenu(true);
		
		updateDateButton();
		updateViews(mCallback.getCurrentProgram());
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_EDIT_ATTENDANCE) { // Attendance edited
			if(resultCode == Activity.RESULT_OK) {
				updateAttendanceSheet();
			}
		}
	}

	public void refresh() {
		if(getView() != null) {
			updateViews(mCallback.getCurrentProgram());
		}
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
			mLvAttendance.setVisibility(View.INVISIBLE);
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
			mLvAttendance.setVisibility(View.INVISIBLE);
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
		for(ProgramClass programClass : programClasses) {
			mClassAdapter.add(programClass.getTitle());
		}
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
		if(mCurrentClass == null) {
			mCurrentStudents = null;
			mCurrentCourse = null;
			mCurrentCourseProxy = null;
			return;
		}
		List<Course> courses = mCurrentClass.getCourses();
		List<Student> students = DataCache.getStudents(programClass);
		boolean loadCourses = courses != null && courses.size() > 0 && !courses.get(0).isDataAvailable();
		boolean loadStudents = students == null;
		if(!loadCourses && !loadStudents) { // Don't need to fetch
			mCurrentStudents = students;
			onClassChanged();
		} else { // Fetch from remote
			new FetchCoursesAndStudentsTask(programClass, loadCourses, loadStudents).execute();
		}
	}
	
	private void onClassChanged() {
		if(mCurrentClass == null) {
			return;
		}
		// Update students adapter
		mAttendanceAdapter.setStudents(mCurrentStudents);
		
		// Update course spinner
		List<Course> courses = mCurrentClass.getCourses();
		if(courses == null || courses.size() == 0) {
			mSpCourse.setVisibility(View.INVISIBLE);
			mTvEmpty.setVisibility(View.VISIBLE);
			mTvEmpty.setText(getString(R.string.no_course_this_class, mCurrentClass.getTitle()));
			mCourseAdapter.clear();
			mCourseAdapter.notifyDataSetChanged();
		} else {
			updateCourseSpinner();
		}
	}
	
	private void updateCourseSpinner() {
		if(mCurrentClass == null) {
			return;
		}
		List<Course> courses = mCurrentClass.getCourses();
		List<Course> addedCourses = new ArrayList<Course>();
		// Update spinner
		int dayOfWeek = mDate.get(Calendar.DAY_OF_WEEK);
		mCourseAdapter.clear();
		if(courses != null) {
			for(Course course : courses) {
				if(course.getDayOfWeek() == dayOfWeek) {
					addedCourses.add(course);
					mCourseAdapter.add(course.getCourseName());
				}
			}
		}
		mCourseAdapter.notifyDataSetChanged();
		
		if(mCourseAdapter.getCount() == 0) { // No course today
			mSpCourse.setVisibility(View.INVISIBLE);
			mLvAttendance.setVisibility(View.INVISIBLE);
			mTvEmpty.setVisibility(View.VISIBLE);
			mTvEmpty.setText(getString(R.string.no_course_on_this_day));
			setCurrentCourse(null);
		} else {
			mSpCourse.setVisibility(View.VISIBLE);
			mLvAttendance.setVisibility(View.VISIBLE);
			mTvEmpty.setVisibility(View.INVISIBLE);
			
			boolean forceUpdate = mSpCourse.getSelectedItemPosition() == 0;
			mSpCourse.setSelection(0);
			if(forceUpdate) {
				setCurrentCourse(addedCourses.get(0));
			}
		}
	}
	
	private void setCurrentCourse(Course course) {
		mCurrentCourse = course;
		if(mCurrentCourse != null) {
			mCurrentCourseProxy = CourseProxy.fromParseObject(mCurrentCourse);
		}
		updateAttendanceSheet();
	}
	
	private void updateAttendanceSheet() {
		if(mCurrentClass == null || mCurrentCourse == null) {
			return;
		}
		if(mCurrentStudents == null || mCurrentStudents.size() == 0) { // No students
			mTvEmpty.setVisibility(View.VISIBLE);
			mTvEmpty.setText(getString(R.string.no_students_class, mCurrentClass.getTitle()));
			mLvAttendance.setVisibility(View.INVISIBLE);
		} else {
			mTvEmpty.setVisibility(View.INVISIBLE);
			mLvAttendance.setVisibility(View.VISIBLE);
			
//			List<Attendance> attendances = DataCache.getAttendance(mCurrentCourse);
//			if(attendances == null) {
			// Get attendance records from remote
			ParseQuery<Attendance> q = ParseQuery.getQuery(Attendance.class);
			q.whereEqualTo(Attendance.TAG_COURSE, mCurrentCourse);
			q.whereEqualTo(Attendance.TAG_DATE, Attendance.formatDate(mDate.getTime()));
			q.findInBackground(new FindCallback<Attendance>() {
				@Override
				public void done(List<Attendance> data, ParseException e) {
					if(e == null) {
						mAttendanceAdapter.clearAttendance();
						mAttendanceAdapter.addAllAttendance(data);
						mAttendanceAdapter.notifyDataSetChanged();
					} else {
						logAndToastException(TAG, e);
					}
				}
			});
//			} else {
//				mAttendanceAdapter.clearAttendance();
//				mAttendanceAdapter.addAllAttendance(attendances);
//				mAttendanceAdapter.notifyDataSetChanged();
//			}
		}
	}

	private void updateDateButton() {
		String str = dateFormat.format(mDate.getTime());
		mBtnDate.setText(str);
	}

	private OnItemSelectedListener onClassSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			Program program = mCallback.getCurrentProgram();
			if(program != null) {
				List<ProgramClass> programClasses = program.getClasses();
				if(position >= 0 && position < programClasses.size()) {
					setCurrentProgramClass(programClasses.get(position));
				}
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			
		}
	};
	
	private OnItemSelectedListener onCourseSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			if(mCurrentClass != null) {
				List<Course> courses = mCurrentClass.getCourses();
				if(position >= 0 && position < courses.size()) {
					setCurrentCourse(courses.get(position));
				}
			}
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
			updateCourseSpinner();
		}
	};
	
	private Comparator<Student> sortByStudentId = new Comparator<Student>() {
		@Override
		public int compare(Student lhs, Student rhs) {
			return lhs.getStudentId().compareTo(rhs.getStudentId());
		}
	};
	
	private OnItemClickListener onAttendanceClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Intent intent = new Intent(getActivity(), AttendanceEditorActivity.class);
			if(mDate != null) {
				intent.putExtra(Const.EXTRA_DATE, mDate.getTime().getTime());
			}
			if(mCurrentCourseProxy != null) {
				intent.putExtra(Const.EXTRA_COURSE, mCurrentCourseProxy);
			}
			Student student = (Student) parent.getItemAtPosition(position);
			if(student != null) {
				StudentProxy studentProxy = StudentProxy.fromParseObject(student);
				intent.putExtra(Const.EXTRA_STUDENT, studentProxy);
			}
			Attendance attendance = mAttendanceAdapter.getAttendance(student);
			if(attendance != null) {
				AttendanceProxy attendanceProxy = AttendanceProxy.fromParseObject(attendance);
				intent.putExtra(Const.EXTRA_ATTENDANCE, attendanceProxy);
			}
			startActivityForResult(intent, REQUEST_EDIT_ATTENDANCE);
		}
	};
	
	private class FetchCoursesAndStudentsTask extends AsyncTask<Void, Void, List<Student>> {

		private ProgramClass programClass;
		private boolean loadCourses;
		private boolean loadStudents;
		private ParseException e;

		public FetchCoursesAndStudentsTask(ProgramClass programClass,
				boolean loadCourses, boolean loadStudents) {
			this.programClass = programClass;
			this.loadCourses = loadCourses;
			this.loadStudents = loadStudents;
		}

		@Override
		protected void onPreExecute() {
			mCallback.showProgress(true);
		}

		@Override
		protected List<Student> doInBackground(Void... params) {
			List<Student> students = null;
			try {
				if(loadCourses) {
					List<Course> courses = programClass.getCourses();
					ParseObject.fetchAllIfNeeded(courses);
				}
				if(loadStudents) {
					ParseQuery<Student> q = ParseQuery.getQuery(Student.class);
					q.whereEqualTo(Student.TAG_ENROLLED_IN, programClass);
					students = q.find();
					Collections.sort(students, sortByStudentId);
				}
			} catch (ParseException e) {
				this.e = e;
			}
			return students;
		}

		@Override
		protected void onPostExecute(List<Student> result) {
			mCallback.showProgress(false);
			if(e == null) {
				if(loadStudents && result != null) {
					DataCache.setStudents(programClass, result);
					mCurrentStudents = result;
					onClassChanged();
				}
			} else {
				logAndToastException(TAG, e);
			}
		}
		
	}
}
