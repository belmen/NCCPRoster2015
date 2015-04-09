package nccp.app.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import nccp.app.R;
import nccp.app.adapter.StudentNameAdapter;
import nccp.app.data.DataCache;
import nccp.app.data.DataCenter;
import nccp.app.parse.object.ProgramClass;
import nccp.app.parse.object.Student;
import nccp.app.parse.proxy.StudentProxy;
import nccp.app.utils.Const;
import nccp.app.utils.Logger;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.MenuItemCompat.OnActionExpandListener;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

import com.parse.ParseException;

public class StudentsFragment extends BaseFragment implements OnQueryTextListener {
	
	public static final String TAG = StudentsFragment.class.getSimpleName();
	
	private static final String STATE_STUDENTS_FIRST = Const.PACKAGE_NAME + ".state.students.first";
	private static final String STATE_SELECTED_STUDENT_INDEX =
			Const.PACKAGE_NAME + ".state.students.selected_student_index";
	private static final String STATE_HIGHLIGHT_POSITION = Const.PACKAGE_NAME + ".state.students.highlight_position";
	
	private static final int REQUEST_ADD_STUDENT = 0;
	private static final int REQUEST_EDIT_STUDENT = 1;

	// Views
	private ExpandableListView mLvStudents;
	private StudentDetailFragment mStudentDetailFragment;
	// Adapter
	private StudentNameAdapter mAdapter = null;
	// Data
	private boolean mFirst = true;
	private Student mSelectedStudent = null;
	private int mHighlightPosition = -1;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = new StudentNameAdapter(getActivity());
		
		mStudentDetailFragment = new StudentDetailFragment();
		mStudentDetailFragment.setOnButtonClickListener(onDetailFragButtonClick);
		getChildFragmentManager().beginTransaction()
				.replace(R.id.student_detail_fragment_container, mStudentDetailFragment).commit();
		
		if(savedInstanceState != null) {
			Logger.i(TAG, TAG + " savedInstanceState not null");
			mFirst = savedInstanceState.getBoolean(STATE_STUDENTS_FIRST);
			mHighlightPosition = savedInstanceState.getInt(STATE_HIGHLIGHT_POSITION, -1);
			int selectedIndex = savedInstanceState.getInt(STATE_SELECTED_STUDENT_INDEX);
			List<Student> students = DataCenter.getStudents();
			if(selectedIndex >= 0 && selectedIndex < students.size()) {
				mSelectedStudent = students.get(selectedIndex);
			}
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
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
//		Logger.i(TAG, TAG + " onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		
		showList(DataCenter.getStudents());
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
		Logger.i(TAG, "onCreateOptionsMenu");
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.student_fragment_menu, menu);
//		if(isDetailOpen()) {
//			inflater.inflate(R.menu.student_fragment_menu_edit, menu);
//		}
		// Add search action
		MenuItem item = menu.findItem(R.id.action_search_student);
		SearchView sv = (SearchView) MenuItemCompat.getActionView(item);
		sv.setOnQueryTextListener(this);
		sv.setQueryHint(getString(R.string.students_search_hint));
		MenuItemCompat.setActionView(item, sv);
		MenuItemCompat.setOnActionExpandListener(item, new OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionExpand(MenuItem arg0) {
				// Close detail and cancel highlight
				if(isDetailOpen()) {
					closeDetail();
					mLvStudents.setItemChecked(mHighlightPosition, false);
					mHighlightPosition = -1;
				}
				return true;
			}
			
			@Override
			public boolean onMenuItemActionCollapse(MenuItem arg0) {
				return true;
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == R.id.action_new_student) {
			handleAddStudent();
		}
//		else if(id == R.id.action_edit_student) {
//			handleEditStudent();
//		} else if(id == R.id.action_remove_student) {
//			handleRemoveStudent();
//		}
//		else if(id == R.id.action_convert_student) {
//			convertStudents();
//			return true;
//		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_STUDENTS_FIRST, mFirst);
		if(mSelectedStudent != null) {
			List<Student> students = DataCenter.getStudents();
			int index = students.indexOf(mSelectedStudent);
			outState.putInt(STATE_SELECTED_STUDENT_INDEX, index);
		}
		outState.putInt(STATE_HIGHLIGHT_POSITION, mHighlightPosition);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_ADD_STUDENT) { // Handle add student
			if(resultCode == Activity.RESULT_OK) {
				Student student = null;
				if(data != null) {
					StudentProxy proxy = (StudentProxy) data.getSerializableExtra(Const.EXTRA_STUDENT);
					if(proxy != null) {
						student = StudentProxy.toParseObject(proxy);
					}
				}
				if(student != null) {
					handleStudentAdded(student);
				}
			}
		} else if(requestCode == REQUEST_EDIT_STUDENT) { // Handle edit student
			if(resultCode == Activity.RESULT_OK) {
				Student student = null;
				if(data != null) {
					StudentProxy proxy = (StudentProxy) data.getSerializableExtra(Const.EXTRA_STUDENT);
					if(proxy != null) {
						student = StudentProxy.toParseObject(proxy);
					}
				}
				if(student != null) {
					handleStudentUpdated(student);
				}
			}
		}
	}

	private void refreshList() {
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
		Intent intent = new Intent(getActivity(), StudentEditorActivity.class);
		StudentProxy proxy = StudentProxy.fromParseObject(mSelectedStudent);
		intent.putExtra(Const.EXTRA_STUDENT, proxy);
		startActivityForResult(intent, REQUEST_EDIT_STUDENT);
	}

	private void handleAddStudent() {
		Intent intent = new Intent(getActivity(), StudentEditorActivity.class);
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
//	private void convertStudents() {
//		List<Student> students = DataCenter.getStudents();
//		if(students == null) {
//			return;
//		}
//		new ConvertStudentTask(students).execute();
//	}

	private void doRemoveStudent(Student student) {
		// Clear the cache of its enroll program class
		ProgramClass programClass = student.getEnrolledIn();
		if(programClass != null) {
			List<Student> students = DataCache.getStudents(programClass);
			if(students != null) {
				students.remove(student);
			}
		}
		
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
//		ActivityCompat.invalidateOptionsMenu(getActivity());
	}
	
	private void handleStudentAdded(Student newStudent) {
		DataCenter.addStudent(newStudent); // Add into local data center
		List<Student> students = DataCenter.getStudents();
//		students.add(newStudent);
		// Update student list
		showList(students);
		// Close detail and cancel highlight
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
		List<Student> students = DataCenter.getStudents();
		if(students == null) {
			handleStudentAdded(updatedStudent);
		}
		int i;
		for(i = 0; i < students.size(); ++i) {
			Student s = students.get(i);
			if(s.getObjectId().equals(updatedStudent.getObjectId())) {
				break;
			}
		}
		if(i < students.size()) {
			students.remove(i);
			students.add(updatedStudent);
			// Update student list
			showList(students);
			// Update detail panel
			mSelectedStudent = updatedStudent;
			mStudentDetailFragment.setStudent(mSelectedStudent);
			// Update highlight position
			updateHighlightPosition();
		}
	}
	
	private void handleStudentRemoved(Student removedStudent) {
		DataCenter.removeStudent(removedStudent); // Remove from local data center
		List<Student> students = DataCenter.getStudents();
		if(mHighlightPosition != -1) { // Cancel highlight
			mLvStudents.setItemChecked(mHighlightPosition, false);
			mHighlightPosition = -1;
		}
		closeDetail();
		// Refresh student list
//		students.remove(removedStudent);
		showList(students);
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
			|| (mSelectedStudent.getObjectId() != null
				&& !mSelectedStudent.getObjectId().equals(item.getObjectId()))) {
				if(!isDetailOpen()) {
					FragmentTransaction trans = getChildFragmentManager().beginTransaction();
					trans.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
					.show(mStudentDetailFragment).commit();
					// Update menu
//					ActivityCompat.invalidateOptionsMenu(getActivity());
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
			return true;
		}
	};
	
	private Comparator<Student> mFirstNameComparator = new Comparator<Student>() {

		@Override
		public int compare(Student lhs, Student rhs) {
			return lhs.getFirstName().compareTo(rhs.getFirstName());
		}
	};
	
	private OnClickListener onDetailFragButtonClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int id = v.getId();
			if(id == R.id.student_detail_edit_btn) {
				handleEditStudent();
			} else if(id == R.id.student_detail_delete_btn) {
				handleRemoveStudent();
			}
		}
	};

	@Override
	public boolean onQueryTextChange(String newText) {
		if(DataCenter.getStudents() == null) {
			return true;
		}
		newText = newText.toLowerCase(Locale.US);
		List<Student> result = new ArrayList<Student>();
		for(Student student: DataCenter.getStudents()) {
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

	public class RemoveStudentTask extends AsyncTask<Void, Void, Void> {

		private Student student;
		private ParseException e = null;
		
		public RemoveStudentTask(Student student) {
			this.student = student;
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				student.delete();
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
				logAndToastException(TAG, e);
			}
		}
	}
}
