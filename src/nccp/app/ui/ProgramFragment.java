package nccp.app.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nccp.app.R;
import nccp.app.data.DataCache;
import nccp.app.data.DataCenter;
import nccp.app.parse.object.Course;
import nccp.app.parse.object.Program;
import nccp.app.parse.object.ProgramClass;
import nccp.app.parse.object.Student;
import nccp.app.utils.Const;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class ProgramFragment extends BaseFragment {

	public static final String TAG = ProgramFragment.class.getSimpleName();

	private static final int REQUEST_EDIT_COURSE = 0;
	private static final int REQUEST_EDIT_STUDENT = 1;
	
	// Views
	private TextView mEmptyView;
	private TextView mTvClassEmpty;
	private ImageButton mIbRenameClass;
	private ImageButton mIbDeleteClass;
	private Spinner mSpClass;
	private LinearLayout mClassBannerView;
	private ScrollView mClassScrollView;
	private TextView mTvCourseCount;
	private TextView mTvStudentCount;
	private TextView[] mTvStudents = new TextView[2];
	// Data
	private Program mCurrentProgram = null;
	private ArrayAdapter<String> mClassAdapter = null;
	private List<LinearLayout> mCourseGridColumns = new ArrayList<LinearLayout>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mClassAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item);
		mClassAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_program, container, false);
		mEmptyView = (TextView) v.findViewById(R.id.program_empty_text);
		mTvClassEmpty = (TextView) v.findViewById(R.id.program_class_empty_text);
		ImageButton ibAddClass = (ImageButton) v.findViewById(R.id.program_class_add_btn);
		ibAddClass.setOnClickListener(onButtonsClickListener);
		mIbRenameClass = (ImageButton) v.findViewById(R.id.program_class_rename_btn);
		mIbRenameClass.setOnClickListener(onButtonsClickListener);
		mIbDeleteClass = (ImageButton) v.findViewById(R.id.program_class_delete_btn);
		mIbDeleteClass.setOnClickListener(onButtonsClickListener);
		mSpClass = (Spinner) v.findViewById(R.id.program_class_spinner);
		mSpClass.setOnItemSelectedListener(onClassSelectedListener);
		mSpClass.setAdapter(mClassAdapter);
		mClassBannerView = (LinearLayout) v.findViewById(R.id.program_class_banner);
		mClassScrollView = (ScrollView) v.findViewById(R.id.program_class_scroller);
		Button ibEditCourse = (Button) v.findViewById(R.id.program_courses_edit_btn);
		ibEditCourse.setOnClickListener(onButtonsClickListener);
		Button ibEditStudents = (Button) v.findViewById(R.id.program_students_edit_btn);
		ibEditStudents.setOnClickListener(onButtonsClickListener);
		mTvStudents[0] = (TextView) v.findViewById(R.id.program_students_text1);
		mTvStudents[1] = (TextView) v.findViewById(R.id.program_students_text2);
		mTvCourseCount = (TextView) v.findViewById(R.id.program_class_course_count_text);
		mTvStudentCount = (TextView) v.findViewById(R.id.program_class_student_count_text);
		
		// Init course grid
		mCourseGridColumns.clear();
		mCourseGridColumns.add((LinearLayout) v.findViewById(R.id.course_schedule_sunday_layout));
		mCourseGridColumns.add((LinearLayout) v.findViewById(R.id.course_schedule_monday_layout));
		mCourseGridColumns.add((LinearLayout) v.findViewById(R.id.course_schedule_tuesday_layout));
		mCourseGridColumns.add((LinearLayout) v.findViewById(R.id.course_schedule_wednesday_layout));
		mCourseGridColumns.add((LinearLayout) v.findViewById(R.id.course_schedule_thursday_layout));
		mCourseGridColumns.add((LinearLayout) v.findViewById(R.id.course_schedule_friday_layout));
		mCourseGridColumns.add((LinearLayout) v.findViewById(R.id.course_schedule_saturday_layout));
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		updateViews();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.program_fragment_menu, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		MenuItem renameMenu = menu.getItem(2);
		MenuItem deleteMenu = menu.getItem(3);
		List<Program> programs = DataCenter.getPrograms();
		if(programs == null || programs.size() == 0) {
			renameMenu.setEnabled(false);
			deleteMenu.setEnabled(false);
		} else {
			renameMenu.setEnabled(true);
			deleteMenu.setEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == R.id.action_add_program) {
			handleAddProgram();
			return true;
		} else if(id == R.id.action_rename_program) {
			handleRenameProgram();
			return true;
		} else if(id == R.id.action_delete_program) {
			handleDeleteProgram();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_EDIT_COURSE) { // Course edited
			if(resultCode == Activity.RESULT_OK) {
				// Update current course grid
				ProgramClass currentClass = getCurrentProgramClass();
				if(currentClass != null) {
					showCourses(currentClass.getCourses());
				}
			}
		} else if(requestCode == REQUEST_EDIT_STUDENT) { // Student edited
			if(resultCode == Activity.RESULT_OK) {
				// Update students grid
				ProgramClass currentClass = getCurrentProgramClass();
				if(currentClass != null) {
					showStudents(currentClass);
				}
			}
		}
	}

	public void setProgram(Program program) {
		if((mCurrentProgram == null && program == null)
		|| (mCurrentProgram != null && program != null && mCurrentProgram.equals(program))) {
			return;
		}
		mCurrentProgram = program;
		if(getView() != null) {
			updateViews();
		}
	}
	
	private ProgramClass getCurrentProgramClass() {
		ProgramClass currentClass = null;
		if(mCurrentProgram != null) {
			List<ProgramClass> classes = mCurrentProgram.getClasses();
			int classIndex = mSpClass.getSelectedItemPosition();
			if(classIndex >= 0 && classIndex < classes.size()) {
				currentClass = classes.get(classIndex);
				showCourses(currentClass.getCourses());
			}
		}
		return currentClass;
	}
	
	private void updateViews() {
		if(mCurrentProgram == null) { // No programs
			mEmptyView.setVisibility(View.VISIBLE);
			mClassBannerView.setVisibility(View.INVISIBLE);
			mClassScrollView.setVisibility(View.INVISIBLE);
			return;
		}
		mEmptyView.setVisibility(View.INVISIBLE);
		mClassBannerView.setVisibility(View.VISIBLE);
		
		updateClassBanner(null);
	}
	
	private void fetchClasses(final String selectedClassName) {
		if(mCurrentProgram == null) {
			return;
		}
		List<ProgramClass> classes = mCurrentProgram.getClasses();
		if(classes.size() > 0 && !classes.get(0).isDataAvailable()) { // Need to fetch
			mCallback.showProgress(true);
			ParseObject.fetchAllIfNeededInBackground(mCurrentProgram.getClasses(),
					new FindCallback<ProgramClass>() {
				@Override
				public void done(List<ProgramClass> data, ParseException e) {
					mCallback.showProgress(false);
					if(e == null) {
						updateClassSpinner(selectedClassName);
					} else {
						logAndToastException(TAG, e);
					}
				}
			});
		} else {
			updateClassSpinner(selectedClassName);
		}
	}
	
	private void updateClassBanner(String selectedClassName) {
		List<ProgramClass> classes = mCurrentProgram.getClasses();
		if(classes == null || classes.size() == 0) { // No classes
			mTvClassEmpty.setVisibility(View.VISIBLE);
			mSpClass.setVisibility(View.GONE);
			mIbRenameClass.setEnabled(false);
			mIbDeleteClass.setEnabled(false);
			mClassScrollView.setVisibility(View.INVISIBLE);
		} else {
			mTvClassEmpty.setVisibility(View.GONE);
			mSpClass.setVisibility(View.VISIBLE);
			mIbRenameClass.setEnabled(true);
			mIbDeleteClass.setEnabled(true);
			mClassScrollView.setVisibility(View.VISIBLE);
			
			fetchClasses(selectedClassName);
		}
	}
	
	private void updateClassSpinner(String selectedClassName) {
		if(mCurrentProgram == null) {
			return;
		}
		List<ProgramClass> classes = mCurrentProgram.getClasses();
		if(classes == null) {
			return;
		}
		// Update class spinner
		mClassAdapter.clear();
		for(ProgramClass c : classes) {
			mClassAdapter.add(c.getTitle());
		}
		mClassAdapter.notifyDataSetChanged();
		
		// If spinner's current selected index == newindex,
		// OnItemSelectedListener would not be fired, then we need force update for program
		boolean forceUpdate = false;
		// Select class
		if(selectedClassName != null) {
			int index = -1;
			for(int i = 0; i < classes.size(); ++i) {
				if(selectedClassName.equals(classes.get(i).getTitle())) {
					index = i;
					break;
				}
			}
			if(index != -1) {
				forceUpdate = mSpClass.getSelectedItemPosition() == index;
				mSpClass.setSelection(index);
				if(forceUpdate) {
					showClassInfo(classes.get(index));
				}
			}
		} else if(!classes.isEmpty()) {
			forceUpdate = mSpClass.getSelectedItemPosition() == 0;
			mSpClass.setSelection(0);
			if(forceUpdate) {
				showClassInfo(classes.get(0));
			}
		}
	}
	
	private void showClassInfo(ProgramClass programClass) {
		showCourses(programClass.getCourses());
		showStudents(programClass);
	}
	
	private void showCourses(List<Course> courses) {
		if(courses == null) {
			return;
		}
		if(courses.size() > 0 && !courses.get(0).isDataAvailable()) { // Need to fetch
			mCallback.showProgress(true);
			ParseObject.fetchAllIfNeededInBackground(courses, new FindCallback<Course>() {
				@Override
				public void done(List<Course> data, ParseException e) {
					mCallback.showProgress(false);
					if(e == null) { // Success
						updateCourseView(data);
					} else { // Fail
						logAndToastException(TAG, e);
					}
				}
			});
		} else {
			updateCourseView(courses);
		}
	}
	
	private void updateCourseView(List<Course> courses) {
		if(courses == null) {
			return;
		}
		// Update count
		int count = courses.size();
		if(count == 0) {
			mTvCourseCount.setText(R.string.program_class_course_count_0);
		} else if(count == 1) {
			mTvCourseCount.setText(R.string.program_class_course_count_1);
		} else {
			mTvCourseCount.setText(getString(R.string.program_class_course_count, count));
		}
		
		// Clear all columns
		for(LinearLayout ll : mCourseGridColumns) {
			ll.removeAllViews();
		}
		
		// Margin: 2dp
		int margin = (int) getResources().getDimension(R.dimen.dp_2);
		// Add items
		for(Course course : courses) {
			TextView tvCourse = (TextView) View.inflate(getActivity(), R.layout.view_course_item, null);
			tvCourse.setText(course.getCourseName());
			LinearLayout ll = mCourseGridColumns.get(course.getDayOfWeek() - 1);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			lp.setMargins(margin, margin, margin, margin);
			ll.addView(tvCourse, lp);
		}
	}

	private void showStudents(final ProgramClass programClass) {
		if(programClass == null) {
			return;
		}
		List<Student> students = DataCache.getStudents(programClass);
		if(students != null) { // Show cached students
			updateStudentsView(students);
		} else { // Fetch from remove
			ParseQuery<Student> q = ParseQuery.getQuery(Student.class);
			q.whereEqualTo(Student.TAG_ENROLLED_IN, programClass);
			q.findInBackground(new FindCallback<Student>() {
				@Override
				public void done(List<Student> data, ParseException e) {
					if(e == null) { // Success
						Collections.sort(data, sortByStudentId);
						DataCache.setStudents(programClass, data);
						updateStudentsView(data);
					} else { // Fail
						logAndToastException(TAG, e);
					}
				}
			});
		}
	}
	
	private void updateStudentsView(List<Student> students) {
		if(students == null) {
			return;
		}
		// Update count
		int count = students.size();
		if(count == 0) {
			mTvStudentCount.setText(R.string.program_class_student_count_0);
		} else if(count == 1) {
			mTvStudentCount.setText(R.string.program_class_student_count_1);
		} else {
			mTvStudentCount.setText(getString(R.string.program_class_student_count, count));
		}
		
		StringBuilder[] builders = new StringBuilder[2];
		builders[0] = new StringBuilder();
		builders[1] = new StringBuilder();
		for(int i = 0; i < students.size(); ++i) {
			Student student = students.get(i);
			StringBuilder sb = builders[i % 2];
			sb.append(student.getFullName()).append('\n');
		}
		mTvStudents[0].setText(builders[0].toString());
		mTvStudents[1].setText(builders[1].toString());
	}

	private void handleAddProgram() {
		View view = View.inflate(getActivity(), R.layout.dialog_one_input, null);
		final EditText etProgramName = (EditText) view.findViewById(R.id.dialog_one_input_et);
		new AlertDialog.Builder(getActivity())
		.setTitle(R.string.dialog_title_add_program)
		.setView(view)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String programName = etProgramName.getText().toString();
				doAddProgram(programName);
			}
		}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		}).show();
	}
	
	private void handleRenameProgram() {
		if(mCurrentProgram == null) {
			return;
		}
		View view = View.inflate(getActivity(), R.layout.dialog_one_input, null);
		final EditText etProgramName = (EditText) view.findViewById(R.id.dialog_one_input_et);
		String oldName = mCurrentProgram.getProgramName();
		etProgramName.setText(oldName);
		etProgramName.setSelection(0, oldName.length());
		new AlertDialog.Builder(getActivity())
		.setTitle(R.string.dialog_title_rename_program)
		.setView(view)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String programName = etProgramName.getText().toString();
				doRenameProgram(programName);
			}
		}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		}).show();
	}
	
	private void handleDeleteProgram() {
		if(mCurrentProgram == null) {
			return;
		}
		new AlertDialog.Builder(getActivity())
		.setTitle(R.string.dialog_title_delete_program)
		.setMessage(getString(R.string.dialog_msg_delete_program, mCurrentProgram.getProgramName()))
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				doDeleteProgram(mCurrentProgram);
			}
		}).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		}).show();
	}
	
	private void doAddProgram(String programName) {
		// Check empty
		if(programName == null || programName.length() == 0) {
			Toast.makeText(getActivity(), R.string.dialog_error_program_name_empty, Toast.LENGTH_SHORT)
			.show();
			return;
		}
		List<Program> programs = DataCenter.getPrograms();
		// Check duplicate
		for(Program program : programs) {
			if(programName.equals(program.getProgramName())) {
				Toast.makeText(getActivity(),
						getString(R.string.error_program_name_exists, programName), Toast.LENGTH_SHORT)
				.show();
				return;
			}
		}
		
		Program program = new Program();
		program.setProgramName(programName);
		programs.add(program); // Add new program to data center
		new SaveProgramTask(program).execute();
	}
	
	private void doRenameProgram(String programName) {
		if(mCurrentProgram == null) {
			return;
		}
		if(programName == null || programName.length() == 0) {
			Toast.makeText(getActivity(), R.string.dialog_error_program_name_empty, Toast.LENGTH_SHORT)
			.show();
			return;
		}
		if(programName.equals(mCurrentProgram.getProgramName())) {
			Toast.makeText(getActivity(), R.string.dialog_error_program_name_same, Toast.LENGTH_SHORT)
			.show();
			return;
		}
		mCurrentProgram.setProgramName(programName);
		new SaveProgramTask(mCurrentProgram).execute();
	}
	
	private void handleProgramChanged(final Program changedProgram) {
		if(mCurrentProgram != null && changedProgram != null
		&& changedProgram.equals(mCurrentProgram)) { // Rename program
			mCallback.updateProgramSpinner(mCurrentProgram.getProgramName());
		} else { // Add or delete program
			// Update menu
			ActivityCompat.invalidateOptionsMenu(getActivity());
			// Update tab and spinner
			String pname = null;
			if(changedProgram != null) {
				pname = changedProgram.getProgramName();
			}
			mCallback.updateProgramSpinner(pname);
		}
	}
	
	private void doDeleteProgram(Program program) {
		if(program != null) {
			DataCenter.getPrograms().remove(program); // Delete from datacenter
			new DeleteProgramTask(program).execute();
		}
	}
	
	private void handleAddClass() {
		View v = View.inflate(getActivity(), R.layout.dialog_one_input, null);
		final EditText et = (EditText) v.findViewById(R.id.dialog_one_input_et);
		et.setHint(R.string.dialog_hint_class_name);
		new AlertDialog.Builder(getActivity())
		.setTitle(R.string.dialog_title_add_class)
		.setView(v)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String newClassName = et.getText().toString();
				doAddClass(newClassName);
			}
		}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		}).show();
	}
	
	private void doAddClass(final String newClassName) {
		if(mCurrentProgram == null) {
			return;
		}
		// Check empty
		if(newClassName.length() == 0) {
			Toast.makeText(getActivity(), R.string.error_program_class_name_empty,
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		// Check duplicates
		List<ProgramClass> classes = mCurrentProgram.getClasses();
		if(classes != null) {
			for(ProgramClass c : classes) {
				if(newClassName.equals(c.getTitle())) {
					Toast.makeText(getActivity(),
							getString(R.string.error_class_name_exists, newClassName), Toast.LENGTH_SHORT)
					.show();
					return;
				}
			}
		}
		
		ProgramClass newClass = new ProgramClass();
		newClass.setTitle(newClassName);
		mCurrentProgram.addClass(newClass);
		Collections.sort(mCurrentProgram.getClasses(), classComparator);
		
		mCallback.showProgress(true);
		mCurrentProgram.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				mCallback.showProgress(false);
				if(e == null) { // Success
					updateClassBanner(newClassName);
				} else { // Fail
					logAndToastException(TAG, e);
				}
			}
		});
	}
	
	private void handleRenameClass() {
		if(mCurrentProgram == null) {
			return;
		}
		List<ProgramClass> classes = mCurrentProgram.getClasses();
		if(classes == null || classes.size() == 0) {
			return;
		}
		final ProgramClass currentClass = classes.get(mSpClass.getSelectedItemPosition());
		
		View v = View.inflate(getActivity(), R.layout.dialog_one_input, null);
		final EditText et = (EditText) v.findViewById(R.id.dialog_one_input_et);
		et.setHint(R.string.dialog_hint_class_name);
		String oldName = currentClass.getTitle();
		et.setText(oldName);
		et.setSelection(0, oldName.length());
		new AlertDialog.Builder(getActivity())
		.setTitle(R.string.dialog_title_rename_class)
		.setView(v)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String newClassName = et.getText().toString();
				doRenameClass(currentClass, newClassName);
			}
		}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		}).show();
	}
	
	private void doRenameClass(ProgramClass renameClass, final String newName) {
		if(renameClass == null) {
			return;
		}
		if(newName.length() == 0) {
			Toast.makeText(getActivity(), R.string.error_program_class_name_empty,
					Toast.LENGTH_SHORT).show();
			return;
		}
		if(newName.equals(renameClass.getTitle())) {
			Toast.makeText(getActivity(), R.string.error_program_class_not_changing,
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		renameClass.setTitle(newName);
		List<ProgramClass> classes = mCurrentProgram.getClasses();
		Collections.sort(classes, classComparator); // Sort classes
		
		mCallback.showProgress(true);
		mCurrentProgram.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				mCallback.showProgress(false);
				if(e == null) { // Success
					updateClassSpinner(newName);
				} else {
					logAndToastException(TAG, e);
				}
			}
		});
	}
	
	private void handleDeleteClass() {
		if(mCurrentProgram == null) {
			return;
		}
		List<ProgramClass> classes = mCurrentProgram.getClasses();
		if(classes == null || classes.size() == 0) {
			return;
		}
		final ProgramClass currentClass = classes.get(mSpClass.getSelectedItemPosition());
		
		new AlertDialog.Builder(getActivity())
		.setTitle(R.string.dialog_title_delete_class)
		.setMessage(getString(R.string.dialog_msg_delete_class, currentClass.getTitle()))
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				doDeleteClass(currentClass);
			}
		}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		}).show();
	}
	
	private void doDeleteClass(ProgramClass currentClass) {
		if(mCurrentProgram == null || currentClass == null) {
			return;
		}
		new DeleteClassTask(mCurrentProgram, currentClass).execute();
	}
	
	private void handleEditCourses() {
		Intent intent = new Intent(getActivity(), CourseListActivity.class);
		int programIndex = mCallback.getCurrentProgramIndex();
		int classIndex = mSpClass.getSelectedItemPosition();
		intent.putExtra(Const.EXTRA_PROGRAM_INDEX, programIndex);
		intent.putExtra(Const.EXTRA_CLASS_INDEX, classIndex);
		startActivityForResult(intent, REQUEST_EDIT_COURSE);
	}
	
	private void handleEditStudents() {
		Intent intent = new Intent(getActivity(), CourseStudentListActivity.class);
		int programIndex = mCallback.getCurrentProgramIndex();
		int classIndex = mSpClass.getSelectedItemPosition();
		intent.putExtra(Const.EXTRA_PROGRAM_INDEX, programIndex);
		intent.putExtra(Const.EXTRA_CLASS_INDEX, classIndex);
		startActivityForResult(intent, REQUEST_EDIT_STUDENT);
	}

	private OnClickListener onButtonsClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int id = v.getId();
			if(id == R.id.program_class_add_btn) {
				handleAddClass();
			} else if(id == R.id.program_class_rename_btn) {
				handleRenameClass();
			} else if(id == R.id.program_class_delete_btn) {
				handleDeleteClass();
			} else if(id == R.id.program_courses_edit_btn) {
				handleEditCourses();
			} else if(id == R.id.program_students_edit_btn) {
				handleEditStudents();
			}
		}
	};
	
	private Comparator<ProgramClass> classComparator = new Comparator<ProgramClass>() {
		@Override
		public int compare(ProgramClass lhs, ProgramClass rhs) {
			return lhs.getTitle().compareTo(rhs.getTitle());
		}
	};
	
	private Comparator<Student> sortByStudentId = new Comparator<Student>() {

		@Override
		public int compare(Student lhs, Student rhs) {
			return lhs.getStudentId().compareTo(rhs.getStudentId());
		}
	};
	
	private OnItemSelectedListener onClassSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			if(mCurrentProgram != null) {
				List<ProgramClass> classes = mCurrentProgram.getClasses();
				ProgramClass programClass = classes.get(position);
				showClassInfo(programClass);
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			
		}
	};
	
	class SaveProgramTask extends AsyncTask<Void, Void, Void> {

		private Program program;
		private ParseException e;
		
		public SaveProgramTask(Program program) {
			this.program = program;
		}

		@Override
		protected void onPreExecute() {
			mCallback.showProgress(true);
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				program.save();
			} catch (ParseException e) {
				this.e = e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mCallback.showProgress(false);
			if(e == null) { // Success
				handleProgramChanged(program);
			} else {
				logAndToastException(TAG, e);
			}
		}
		
	}
	
	class DeleteProgramTask extends AsyncTask<Void, Void, Void> {
		
		private Program program;
		private ParseException e;

		public DeleteProgramTask(Program program) {
			this.program = program;
		}

		@Override
		protected void onPreExecute() {
			mCallback.showProgress(true);
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				// Delete all classes
				List<ProgramClass> classes = program.getClasses();
				for(ProgramClass programClass : classes) {
					// Delete all courses
					List<Course> courses = programClass.getCourses();
					for(Course course : courses) {
						course.delete();
					}
					
					// Delete students links to this class
					ParseQuery<Student> q = ParseQuery.getQuery(Student.class);
					q.whereEqualTo(Student.TAG_ENROLLED_IN, programClass);
					List<Student> students = q.find();
					if(students != null) {
						for(Student s : students) {
							s.removeEnrolledIn();
							s.save();
						}
					}
					
					// Delete class
					programClass.delete();
				}
				program.delete();
			} catch (ParseException e) {
				this.e = e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mCallback.showProgress(false);
			if(e == null) { // Success
				Toast.makeText(getActivity(), getString(R.string.msg_has_been_removed, program.getProgramName()),
						Toast.LENGTH_SHORT).show();
				handleProgramChanged(null);
			} else {
				logAndToastException(TAG, e);
			}
		}
	}
	
	class DeleteClassTask extends AsyncTask<Void, Void, Void> {

		private ParseException e;
		private Program program;
		private ProgramClass programClass;
		
		public DeleteClassTask(Program program, ProgramClass programClass) {
			this.program = program;
			this.programClass = programClass;
		}

		@Override
		protected void onPreExecute() {
			mCallback.showProgress(true);
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				if(program.getClasses().remove(programClass)) {
					program.save();
				}
				programClass.delete();
			} catch (ParseException e) {
				this.e = e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mCallback.showProgress(false);
			if(e == null) { // Success
				Toast.makeText(getActivity(), getString(R.string.msg_has_been_removed, programClass.getTitle()),
						Toast.LENGTH_SHORT).show();
				updateClassBanner(null);
			} else {
				logAndToastException(TAG, e);
			}
		}
	}
}
