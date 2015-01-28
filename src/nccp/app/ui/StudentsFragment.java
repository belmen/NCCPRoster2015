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
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

	// Views
	private ExpandableListView mLvStudents;
	private ProgressBar mProgressBar;
	// Adapter
	private StudentAdapter mAdapter = null;
	// Data
	private boolean mFirst = true;
	private List<Student> mStudents = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
//		Logger.i(TAG, TAG + " onCreate");
		super.onCreate(savedInstanceState);
		mAdapter = new StudentAdapter(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
//		Logger.i(TAG, TAG + " onCreateView");
		View v = inflater.inflate(R.layout.students_fragment, container, false);
		mLvStudents = (ExpandableListView) v.findViewById(R.id.students_listview);
		mLvStudents.setOnGroupClickListener(mOnGroupClickListener);
		mLvStudents.setOnChildClickListener(mOnChildClickListener);
		View emptyView = v.findViewById(R.id.students_empty_text);
		mLvStudents.setEmptyView(emptyView);
		mLvStudents.setAdapter(mAdapter);
		mProgressBar = (ProgressBar) v.findViewById(R.id.students_progress);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
//		Logger.i(TAG, TAG + " onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		if(savedInstanceState != null) {
			Logger.i(TAG, TAG + " savedInstanceState not null");
			mFirst = savedInstanceState.getBoolean(STATE_STUDENTS_FIRST);
			mStudents = (List<Student>) savedInstanceState.getSerializable(STATE_STUDENTS_DATA);
			showList(mStudents);
		} else {
			Logger.i(TAG, TAG + " savedInstanceState is null");
			if(mFirst) {
				mFirst = false;
				refreshList();
			}
		}
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Add search action
		MenuItem item = menu.add(getString(R.string.action_search_students));
		item.setIcon(android.R.drawable.ic_menu_search);
		MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM
                | MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		SearchView sv = new SearchView(getActivity());
		sv.setOnQueryTextListener(this);
		sv.setQueryHint(getString(R.string.students_search_hint));
		MenuItemCompat.setActionView(item, sv);
	}

	private void refreshList() {
		mProgressBar.setVisibility(View.VISIBLE);
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Student");
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> data, ParseException e) {
				mProgressBar.setVisibility(View.INVISIBLE);
				if(e == null) {
//					mStudents = BeanUtil.fromParseObjects(data, Student.class);
					mStudents = new ArrayList<Student>();
					for(ParseObject object : data) {
						String name = object.getString("Name");
//						Logger.d(TAG, "Name: " + name);
						String[] names = name.split(",");
						Student student = new Student();
						if(names.length >= 2) {
							student.setFirstName(names[1].trim());
							student.setLastName(names[0].trim());
						} else {
							student.setFirstName(names[0].trim());
							student.setLastName("");
						}
						mStudents.add(student);
					}
					showList(mStudents);
				} else {
					Logger.e(TAG, e.getMessage());
				}
			}
		});
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
			return false;
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
