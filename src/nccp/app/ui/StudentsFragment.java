package nccp.app.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import nccp.app.R;
import nccp.app.adapter.StudentAdapter;
import nccp.app.bean.Student;
import nccp.app.utils.Const;
import nccp.app.utils.Logger;
import nccp.app.utils.ParseBeanUtil;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.MenuItemCompat.OnActionExpandListener;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnCloseListener;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class StudentsFragment extends Fragment implements OnQueryTextListener, OnCloseListener {
	
	public static final String TAG = StudentsFragment.class.getSimpleName();
	
	private static final String STATE_STUDENTS_FIRST = Const.PACKAGE_NAME + ".state.students.first";
	private static final String STATE_STUDENTS_DATA = Const.PACKAGE_NAME + ".state.students.data";
	private static final String STATE_SELECTED_STUDENT = Const.PACKAGE_NAME + ".state.students.selected_student";
	private static final String STATE_HIGHLIGHT_POSITION = Const.PACKAGE_NAME + ".state.students.highlight_position";
	
	private static final int REQUEST_ADD_STUDENT = 0;
	private static final int REQUEST_EDIT_STUDENT = 1;

	private FragmentCallback mCallback = null;
	
	// Views
	private ExpandableListView mLvStudents;
//	private ProgressBar mProgressBar;
	private StudentDetailFragment mStudentDetailFragment;
	// Adapter
	private StudentAdapter mAdapter = null;
	// Data
	private boolean mFirst = true;
	private List<Student> mStudents = null;
	private Student mSelectedStudent = null;
	private int mHighlightPosition = -1;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (FragmentCallback) activity;
		} catch(ClassCastException e) {
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Logger.i(TAG, TAG + " onCreate");
		super.onCreate(savedInstanceState);
		mAdapter = new StudentAdapter(getActivity());
		
		mStudentDetailFragment = new StudentDetailFragment();
		getChildFragmentManager().beginTransaction()
				.replace(R.id.student_detail_fragment_container, mStudentDetailFragment).commit();
		
		if(savedInstanceState != null) {
			Logger.i(TAG, TAG + " savedInstanceState not null");
			mFirst = savedInstanceState.getBoolean(STATE_STUDENTS_FIRST);
			mStudents = (List<Student>) savedInstanceState.getSerializable(STATE_STUDENTS_DATA);
			mSelectedStudent = (Student) savedInstanceState.getSerializable(STATE_SELECTED_STUDENT);
			mHighlightPosition = savedInstanceState.getInt(STATE_HIGHLIGHT_POSITION, -1);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
//		Logger.i(TAG, TAG + " onCreateView");
		View v = inflater.inflate(R.layout.fragment_students, container, false);
		mLvStudents = (ExpandableListView) v.findViewById(R.id.students_listview);
		mLvStudents.setOnGroupClickListener(mOnGroupClickListener);
		mLvStudents.setOnChildClickListener(mOnChildClickListener);
		View emptyView = v.findViewById(R.id.students_empty_text);
		mLvStudents.setEmptyView(emptyView);
		mLvStudents.setAdapter(mAdapter);
//		mProgressBar = (ProgressBar) v.findViewById(R.id.students_progress);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
//		Logger.i(TAG, TAG + " onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		
		showList(mStudents);
		if(mSelectedStudent != null) {
			mStudentDetailFragment.setStudent(mSelectedStudent);
		} else {
			getChildFragmentManager().beginTransaction().hide(mStudentDetailFragment)
			.commit();
		}
		if(mFirst) {
			mFirst = false;
			refreshList();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.student_fragment_menu, menu);
		if(isDetailOpen()) {
			inflater.inflate(R.menu.student_fragment_menu_edit, menu);
		}
		// Add search action
		MenuItem item = menu.findItem(R.id.action_search_student);
		SearchView sv = (SearchView) MenuItemCompat.getActionView(item);
		sv.setOnQueryTextListener(this);
//		sv.setOnCloseListener(this);
		sv.setQueryHint(getString(R.string.students_search_hint));
		MenuItemCompat.setActionView(item, sv);
//		MenuItemCompat.setOnActionExpandListener(item, new OnActionExpandListener() {
//			@Override
//			public boolean onMenuItemActionExpand(MenuItem arg0) {
//				return true;
//			}
//			
//			@Override
//			public boolean onMenuItemActionCollapse(MenuItem arg0) {
//				if(mCallback != null) {
//					mCallback.showProgress(false);
//				}
//				return true;
//			}
//		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == R.id.action_add_student) {
			handleAddStudent();
		} else if(id == R.id.action_edit_student) {
			handleEditStudent();
		} else if(id == R.id.action_remove_student) {
			handleRemoveStudent();
		}else if(id == R.id.action_convert) {
//			convertStudents();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_STUDENTS_FIRST, mFirst);
		if(mStudents != null) {
			outState.putSerializable(STATE_STUDENTS_DATA, (Serializable) mStudents);
		}
		if(mSelectedStudent != null) {
			outState.putSerializable(STATE_SELECTED_STUDENT, mSelectedStudent);
		}
		outState.putInt(STATE_HIGHLIGHT_POSITION, mHighlightPosition);
		// Save detail fragment
//		getChildFragmentManager().putFragment(outState, STATE_DETAIL_FRAGMENT, mStudentDetailFragment);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_ADD_STUDENT) { // Handle add student
			if(resultCode == Activity.RESULT_OK) {
				Student student = null;
				if(data != null) {
					student = (Student) data.getSerializableExtra(Const.EXTRA_STUDENT);
				}
				if(student != null) {
					handleStudentAdded(student);
				}
			}
		} else if(requestCode == REQUEST_EDIT_STUDENT) { // Handle edit student
			if(resultCode == Activity.RESULT_OK) {
				Student student = null;
				if(data != null) {
					student = (Student) data.getSerializableExtra(Const.EXTRA_STUDENT);
				}
				if(student != null) {
					handleStudentUpdated(student);
				}
			}
		}
	}

	private void refreshList() {
		new GetStudentsTask().execute();
//		ParseQuery<ParseObject> query = ParseQuery.getQuery("Student");
//		query.findInBackground(new FindCallback<ParseObject>() {
//			@Override
//			public void done(List<ParseObject> data, ParseException e) {
//				mProgressBar.setVisibility(View.INVISIBLE);
//				if(e == null) {
////					mStudents = BeanUtil.fromParseObjects(data, Student.class);
//					mStudents = new ArrayList<Student>();
//					for(ParseObject object : data) {
//						Student student = new Student();
//						String name = object.getString("Name");
////						Logger.d(TAG, "Name: " + name);
//						String[] names = name.split(",");
//						if(names.length >= 2) {
//							student.setFirstName(names[1].trim());
//							student.setLastName(names[0].trim());
//						} else {
//							student.setFirstName(names[0].trim());
//							student.setLastName("");
//						}
//						student.setStudentId(String.valueOf(object.getNumber("ID").intValue()));
//						student.setGradeLevel(object.getNumber("Parse_1112GradeLevel").intValue());
//						
//						mStudents.add(student);
//					}
//					showList(mStudents);
//				} else {
//					Logger.e(TAG, e.getMessage());
//				}
//			}
//		});
	}

	private void showList(List<Student> studentList) {
		if(studentList == null) {
			return;
		}
		List<String> initials = null;
		Map<String, List<Student>> data = null;
		Collections.sort(studentList, mFirstNameComparator);
		initials = new ArrayList<String>();
		data = new HashMap<String, List<Student>>();
		for(Student student : studentList) {
			String initial = student.getFirstNameInitial();
			List<Student> students = data.get(initial);
			if(students == null) {
				students = new ArrayList<Student>();
				data.put(initial, students);
			}
			students.add(student);
		}
		for(String initial : data.keySet()) {
			initials.add(initial);
		}
		Collections.sort(initials);
		mAdapter.setData(initials, data);
		mAdapter.notifyDataSetChanged();
		// Expand groups
		for(int i = 0; i < mAdapter.getGroupCount(); ++i) {
			mLvStudents.expandGroup(i);
		}
	}

	private void handleEditStudent() {
		if(mSelectedStudent == null) {
			return;
		}
		Intent intent = new Intent(getActivity(), EditStudentActivity.class);
		intent.putExtra(Const.EXTRA_STUDENT, mSelectedStudent);
		startActivityForResult(intent, REQUEST_EDIT_STUDENT);
	}

	private void handleAddStudent() {
		Intent intent = new Intent(getActivity(), EditStudentActivity.class);
		startActivityForResult(intent, REQUEST_ADD_STUDENT);
	}
	
	private void handleRemoveStudent() {
		if(mSelectedStudent == null) {
			return;
		}
		new AlertDialog.Builder(getActivity())
		.setTitle(R.string.dialog_title_remove_student)
		.setMessage(getString(R.string.dialog_msg_remove_student, mSelectedStudent.getFullName()))
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				doRemoveStudent(mSelectedStudent);
			}
		}).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		}).show();
	}

	// Convert student 
	private void convertStudents() {
//		if(mStudents == null) {
//			return;
//		}
//		ParseObject.saveAllInBackground(mStudents, new SaveCallback() {
//			@Override
//			public void done(ParseException e) {
//				if(e == null) {
//					Toast.makeText(getActivity(), "Convert complete", Toast.LENGTH_SHORT).show();
//				}
//			}
//		});
	}

	private void doRemoveStudent(Student student) {
		new RemoveStudentTask(student).execute();
	}
	
	private boolean isDetailOpen() {
		return mSelectedStudent != null;
	}
	
	private void closeDetail() {
		mSelectedStudent = null;
		FragmentTransaction trans = getChildFragmentManager().beginTransaction();
		trans.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
		.hide(mStudentDetailFragment).commitAllowingStateLoss();
		
		// Update menu
		ActivityCompat.invalidateOptionsMenu(getActivity());
	}
	
	private void handleStudentAdded(Student newStudent) {
		if(mStudents == null) {
			mStudents = new ArrayList<Student>();
		}
		mStudents.add(newStudent);
		// Update student list
		showList(mStudents);
		// Close detail and cancle highlight
		if(isDetailOpen()) {
			closeDetail();
			mLvStudents.setItemChecked(mHighlightPosition, false);
			mHighlightPosition = -1;
		}
		// Scroll to new student's position
		long position = mAdapter.getStudentPosition(newStudent);
		if(position != -1) {
			int highlightPosition = mLvStudents.getFlatListPosition(position);
			mLvStudents.setSelection(highlightPosition);
		}
	}

	private void handleStudentUpdated(Student updatedStudent) {
		if(mStudents == null) {
			handleStudentAdded(updatedStudent);
		}
		int i;
		for(i = 0; i < mStudents.size(); ++i) {
			Student s = mStudents.get(i);
			if(s.getParseObjectId().equals(updatedStudent.getParseObjectId())) {
				break;
			}
		}
		if(i < mStudents.size()) {
			mStudents.remove(i);
			mStudents.add(updatedStudent);
			// Update student list
			showList(mStudents);
			// Update detail panel
			mSelectedStudent = updatedStudent;
			mStudentDetailFragment.setStudent(mSelectedStudent);
			// Update highlight position
			updateHighlightPosition();
		}
	}
	
	private void handleStudentRemoved(Student removedStudent) {
		if(mHighlightPosition != -1) { // Cancel highlight
			mLvStudents.setItemChecked(mHighlightPosition, false);
			mHighlightPosition = -1;
		}
		closeDetail();
		// Refresh student list
		mStudents.remove(removedStudent);
		showList(mStudents);
	}
	
	private void updateHighlightPosition() {
		if(mSelectedStudent == null) {
			return;
		}
		long position = mAdapter.getStudentPosition(mSelectedStudent);
		if(position != -1) {
			int highlightPosition = mLvStudents.getFlatListPosition(position);
			if(mHighlightPosition != -1 && mHighlightPosition != highlightPosition) {
				mLvStudents.setItemChecked(mHighlightPosition, false);
			}
			mLvStudents.setItemChecked(highlightPosition, true);
			mHighlightPosition = highlightPosition;
			mLvStudents.setSelection(highlightPosition); // Scroll to new highlight position
		}
	}

	private OnGroupClickListener mOnGroupClickListener = new OnGroupClickListener() {
		@Override
		public boolean onGroupClick(ExpandableListView parent, View v,
				int groupPosition, long id) {
			// Disable collapse
			return true;
		}
	};
	
	private OnChildClickListener mOnChildClickListener = new OnChildClickListener() {
		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {
			int flatPosition = parent.getFlatListPosition(
					ExpandableListView.getPackedPositionForChild(groupPosition, childPosition));
			Student item = (Student) mAdapter.getChild(groupPosition, childPosition);
			if(mSelectedStudent == null
			|| (mSelectedStudent.getParseObjectId() != null
				&& !mSelectedStudent.getParseObjectId().equals(item.getParseObjectId()))) {
				if(!isDetailOpen()) {
					FragmentTransaction trans = getChildFragmentManager().beginTransaction();
					trans.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
					.show(mStudentDetailFragment).commit();
					// Update menu
					ActivityCompat.invalidateOptionsMenu(getActivity());
				}
				mSelectedStudent = item;
				mStudentDetailFragment.setStudent(mSelectedStudent);
				// Highlight list item
				parent.setItemChecked(flatPosition, true);
				mHighlightPosition = flatPosition;
			} else {
				// Cancel list item highlight
				parent.setItemChecked(flatPosition, false);
				mHighlightPosition = -1;
				closeDetail();
			}
			
//			if(mSelectedPosition != position) {
//				mSelectedPosition = position;
//				Student item = (Student) mAdapter.getChild(groupPosition, childPosition);
//				FragmentTransaction trans = getChildFragmentManager().beginTransaction();
//				trans.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
//				.show(mStudentDetailFragment).commit();
//				mStudentDetailFragment.setStudent(item);
//			} else {
//				mSelectedPosition = -1;
//				FragmentTransaction trans = getChildFragmentManager().beginTransaction();
//				trans.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
//				.hide(mStudentDetailFragment).commit();
//			}
			return true;
		}
	};
	
	private Comparator<Student> mFirstNameComparator = new Comparator<Student>() {

		@Override
		public int compare(Student lhs, Student rhs) {
			return lhs.getFirstName().compareTo(rhs.getFirstName());
		}
	};

	@Override
	public boolean onQueryTextChange(String newText) {
		if(mStudents == null) {
			return true;
		}
		newText = newText.toLowerCase(Locale.US);
		List<Student> result = new ArrayList<Student>();
		for(Student student: mStudents) {
			String firstName = student.getFirstName().toLowerCase(Locale.US);
			String lastName = student.getLastName().toLowerCase(Locale.US);
			if(firstName.startsWith(newText) || lastName.startsWith(newText)) {
				result.add(student);
			}
		}
		mAdapter.setHighlight(newText);
		showList(result);
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

	@Override
	public boolean onClose() {
		Logger.i(TAG, "StudentsFragment onClose");
		mAdapter.setHighlight("");
		showList(mStudents);
		return true;
	}
	
	public class GetStudentsTask extends AsyncTask<Void, Void, List<ParseObject>> {

		private ParseException e;
		
		@Override
		protected void onPreExecute() {
			if(mCallback != null) {
				mCallback.showProgress(true);
			}
		}

		@Override
		protected List<ParseObject> doInBackground(Void... params) {
			ParseQuery<ParseObject> query = ParseQuery.getQuery(Student.TAG_OBJECT);
			List<ParseObject> result = null;
			try {
				result = query.find();
			} catch (ParseException e) {
				this.e = e;
			}
			return result;
		}

		@Override
		protected void onPostExecute(List<ParseObject> result) {
			if(mCallback != null) {
				mCallback.showProgress(false);
			}
			if(e == null) {
				mStudents = ParseBeanUtil.fromParseObjects(result, Student.class);
				showList(mStudents);
			} else {
				Logger.e(TAG, e.getMessage());
				Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	public class RemoveStudentTask extends AsyncTask<Void, Void, Void> {

		private Student student;
		private ParseException e = null;
		
		public RemoveStudentTask(Student student) {
			this.student = student;
		}

		@Override
		protected Void doInBackground(Void... params) {
			ParseObject obj = Student.toParseObject(student);
			try {
				obj.delete();
			} catch (ParseException e) {
				this.e = e;
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			if(mCallback != null) {
				mCallback.showProgress(true);
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			if(mCallback != null) {
				mCallback.showProgress(false);
			}
			if(e == null) {
				handleStudentRemoved(student);
			} else {
				Logger.e(TAG, e.getMessage(), e);
				Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
	}
}
