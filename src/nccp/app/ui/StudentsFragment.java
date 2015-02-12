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
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
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
import android.widget.ProgressBar;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class StudentsFragment extends Fragment implements OnQueryTextListener, OnCloseListener {
	
	public static final String TAG = StudentsFragment.class.getSimpleName();
	
	private static final String STATE_STUDENTS_FIRST = Const.PACKAGE_NAME + ".state.students.first";
	private static final String STATE_STUDENTS_DATA = Const.PACKAGE_NAME + ".state.students.data";
	
	private static final int REQUEST_ADD_STUDENT = 0;
	private static final int REQUEST_EDIT_STUDENT = 1;

	// Views
	private ExpandableListView mLvStudents;
	private ProgressBar mProgressBar;
	private StudentDetailFragment mStudentDetailFragment;
	// Adapter
	private StudentAdapter mAdapter = null;
	// Data
	private boolean mFirst = true;
	private List<Student> mStudents = null;
//	private long mSelectedPosition = -1;
	private Student mSelectedStudent = null;
	private int mHighlightPosition = -1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
//		Logger.i(TAG, TAG + " onCreate");
		super.onCreate(savedInstanceState);
		mAdapter = new StudentAdapter(getActivity());
		mStudentDetailFragment = new StudentDetailFragment();
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
		mProgressBar = (ProgressBar) v.findViewById(R.id.students_progress);

		// Init and hide detail fragment
		if(!mStudentDetailFragment.isAdded()) {
			FragmentTransaction trans = getChildFragmentManager().beginTransaction();
			trans.add(R.id.student_detail_fragment_container, mStudentDetailFragment)
			.hide(mStudentDetailFragment).commit();
		}
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
//		Logger.i(TAG, TAG + " onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		if(savedInstanceState != null) {
//			Logger.i(TAG, TAG + " savedInstanceState not null");
			mFirst = savedInstanceState.getBoolean(STATE_STUDENTS_FIRST);
			mStudents = (List<Student>) savedInstanceState.getSerializable(STATE_STUDENTS_DATA);
			showList(mStudents);
		} else {
//			Logger.i(TAG, TAG + " savedInstanceState is null");
			if(mFirst) {
				mFirst = false;
				refreshList();
			}
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
		sv.setQueryHint(getString(R.string.students_search_hint));
		MenuItemCompat.setActionView(item, sv);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == R.id.action_add_student) {
			handleAddStudent();
		} else if(id == R.id.action_edit_student) {
			handleEditStudent();
		} else if(id == R.id.action_convert) {
//			convertStudents();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(mStudents != null) {
			outState.putBoolean(STATE_STUDENTS_FIRST, mFirst);
			outState.putSerializable(STATE_STUDENTS_DATA, (Serializable) mStudents);
		}
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
		mProgressBar.setVisibility(View.VISIBLE);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Student.TAG_OBJECT);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> data, ParseException e) {
				mProgressBar.setVisibility(View.INVISIBLE);
				if(e == null) {
					mStudents = ParseBeanUtil.fromParseObjects(data, Student.class);
					showList(mStudents);
				} else {
					Logger.e(TAG, e.getMessage());
				}
			}
		});
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

	private boolean isDetailOpen() {
		return mSelectedStudent != null;
	}
	
	private void closeDetail() {
		mSelectedStudent = null;
		FragmentTransaction trans = getChildFragmentManager().beginTransaction();
		trans.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
		.hide(mStudentDetailFragment).commit();
		
		// Update menu
		ActivityCompat.invalidateOptionsMenu(getActivity());
	}
	
	private void handleStudentAdded(Student student) {
		if(mStudents == null) {
			mStudents = new ArrayList<Student>();
		}
		mStudents.add(student);
		// Update student list
		showList(mStudents);
		// Close detail and cancle highlight
		if(isDetailOpen()) {
			closeDetail();
			mLvStudents.setItemChecked(mHighlightPosition, false);
			mHighlightPosition = -1;
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
			if(!isDetailOpen()
			|| !mSelectedStudent.getParseObjectId().equals(item.getParseObjectId())) {
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
		mAdapter.setHighlight("");
		showList(mStudents);
		return true;
	}
}
