package nccp.app.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nccp.app.R;
import nccp.app.adapter.StudentInfoAdapter;
import nccp.app.data.DataCenter;
import nccp.app.parse.object.Student;
import nccp.app.utils.Const;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

public class StudentsSelectorActivity extends ToolbarActivity {

	public static final String TAG = StudentsSelectorActivity.class.getSimpleName();

	// Views
	private ListView mLvStudents;
	private CheckBox mCbStudentId;
	private CheckBox mCbFirstname;
	private CheckBox mCbLastname;
	private CheckBox mCbGradelevel;
	private AlertDialog mStudentIdFilterDialog;
	private AlertDialog mFirstnameFilterDialog;
	private AlertDialog mLastnameFilterDialog;
	private AlertDialog mGradeLevelFilterDialog;
	// Data
	private List<Student> mAllStudents = null;
	private List<Student> mStudents = null;
	private StudentInfoAdapter mAdapter;
	// Filters
	private boolean mCanceled = false;
	private String mStudentIdPref = null;
	private String mFirstnamePref = null;
	private String mLastnamePref = null;
	private int mGradeLevel = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_students_selector);
		initToolbar();
		initViews();
		initData();
		createDialogs();
		
		mAdapter.setData(mStudents);
		mAdapter.notifyDataSetChanged();
		updateTitle();
	}
	
	private void initToolbar() {
		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setHomeAsUpIndicator(R.drawable.ic_menu_check_24dp);
	}

	private void initViews() {
		mLvStudents = (ListView) findViewById(R.id.course_students_selector_listview);
		mLvStudents.setOnItemClickListener(mOnStudentClickListener);
		mAdapter = new StudentInfoAdapter(StudentsSelectorActivity.this);
		mLvStudents.setAdapter(mAdapter);
		TextView tvEmpty = (TextView) findViewById(R.id.course_students_selector_empty_text);
		mLvStudents.setEmptyView(tvEmpty);
		mCbStudentId = (CheckBox) findViewById(R.id.students_selector_id_cb);
		mCbStudentId.setOnCheckedChangeListener(mOnCheckedChangeListener);
		mCbFirstname = (CheckBox) findViewById(R.id.students_selector_firstname_cb);
		mCbFirstname.setOnCheckedChangeListener(mOnCheckedChangeListener);
		mCbLastname = (CheckBox) findViewById(R.id.students_selector_lastname_cb);
		mCbLastname.setOnCheckedChangeListener(mOnCheckedChangeListener);
		mCbGradelevel = (CheckBox) findViewById(R.id.students_selector_gradelevel_cb);
		mCbGradelevel.setOnCheckedChangeListener(mOnCheckedChangeListener);
	}
	
	private void createDialogs() {
		// Create student ID filter dialog
		View studentIdFilterView = View.inflate(StudentsSelectorActivity.this, R.layout.dialog_one_input, null);
		final EditText etStudentId = (EditText) studentIdFilterView.findViewById(R.id.dialog_one_input_et);
		etStudentId.setHint(R.string.hint_student_id);
		mStudentIdFilterDialog = new AlertDialog.Builder(StudentsSelectorActivity.this)
		.setTitle(R.string.dialog_title_student_id_filter)
		.setView(studentIdFilterView)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String pref = etStudentId.getText().toString();
				if(pref.length() > 0) {
					mStudentIdPref = pref;
					updateStudentsFilter();
				} else {
					mCanceled = true;
					mCbStudentId.setChecked(false);
				}
			}
		}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mCanceled = true;
				mCbStudentId.setChecked(false);
			}
		}).create();
		mStudentIdFilterDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				mCanceled = true;
				mCbStudentId.setChecked(false);
			}
		});
		
		// Create first name filter dialog
		View firstnameFilterView = View.inflate(StudentsSelectorActivity.this, R.layout.dialog_one_input, null);
		final EditText etFirstname = (EditText) firstnameFilterView.findViewById(R.id.dialog_one_input_et);
		etFirstname.setHint(R.string.hint_first_name);
		mFirstnameFilterDialog = new AlertDialog.Builder(StudentsSelectorActivity.this)
		.setTitle(R.string.dialog_title_first_name_filter)
		.setView(firstnameFilterView)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String pref = etFirstname.getText().toString();
				if(pref.length() > 0) {
					mFirstnamePref = pref;
					updateStudentsFilter();
				} else {
					mCanceled = true;
					mCbFirstname.setChecked(false);
				}
			}
		}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mCanceled = true;
				mCbFirstname.setChecked(false);
			}
		}).create();
		mFirstnameFilterDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				mCanceled = true;
				mCbFirstname.setChecked(false);
			}
		});
		
		// Create last time filter dialog
		View lastnameFilterView = View.inflate(StudentsSelectorActivity.this, R.layout.dialog_one_input, null);
		final EditText etLastname = (EditText) lastnameFilterView.findViewById(R.id.dialog_one_input_et);
		etLastname.setHint(R.string.hint_last_name);
		mLastnameFilterDialog = new AlertDialog.Builder(StudentsSelectorActivity.this)
		.setTitle(R.string.dialog_title_last_name_filter)
		.setView(lastnameFilterView)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String pref = etLastname.getText().toString();
				if(pref.length() > 0) {
					mLastnamePref = pref;
					updateStudentsFilter();
				} else {
					mCanceled = true;
					mCbLastname.setChecked(false);
				}
			}
			
		}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mCanceled = true;
				mCbLastname.setChecked(false);
			}
		}).create();
		mLastnameFilterDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				mCanceled = true;
				mCbLastname.setChecked(false);
			}
		});
		
		// Create grade level filter dialog
		final NumberPicker npGradeLevel = (NumberPicker) View.inflate(StudentsSelectorActivity.this,
				R.layout.dialog_grade_level_filter, null);
		npGradeLevel.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); // Disable edit mode
		npGradeLevel.setMinValue(1);
		npGradeLevel.setMaxValue(12);
		npGradeLevel.setWrapSelectorWheel(false);
		mGradeLevelFilterDialog = new AlertDialog.Builder(StudentsSelectorActivity.this)
		.setTitle(R.string.dialog_title_grade_level_filter)
		.setView(npGradeLevel)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mGradeLevel = npGradeLevel.getValue();
				updateStudentsFilter();
			}
		}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mCanceled = true;
				mCbGradelevel.setChecked(false);
			}
		}).create();
		mGradeLevelFilterDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				mCanceled = true;
				mCbGradelevel.setChecked(false);
			}
		});
	}
	
	private void initData() {
		mAllStudents = new ArrayList<Student>(DataCenter.getStudents());
		Collections.sort(mAllStudents, mSortByStudentId);
		mStudents = new ArrayList<Student>();
		if(mAllStudents != null) {
			mStudents.addAll(mAllStudents);
		}
	}

	private void updateTitle() {
		int count = mLvStudents.getCheckedItemCount();
		String title = count == 1 ? getString(R.string.title_1_student_selected)
				: getString(R.string.title_n_students_selected, count);
		getSupportActionBar().setTitle(title);
	}
	
	private void updateStudentsFilter() {
		if(mAllStudents == null) {
			return;
		}
		mStudents.clear();
		for(Student student : mAllStudents) {
			boolean add = true;
			if(mCbStudentId.isChecked() && mStudentIdPref != null) { // Filter by student ID
				String studentId = student.getStudentId().toLowerCase();
				if(studentId != null && !studentId.startsWith(mStudentIdPref.toLowerCase())) {
					add = false;
				}
			}
			if(add && mCbFirstname.isChecked() && mFirstnamePref != null) { // Filter by first name
				String firstname = student.getFirstName().toLowerCase();
				if(firstname != null && !firstname.startsWith(mFirstnamePref.toLowerCase())) {
					add = false;
				}
			}
			if(add && mCbLastname.isChecked() && mLastnamePref != null) { // Filter by last name
				String lastname = student.getLastName().toLowerCase();
				if(lastname != null && !lastname.startsWith(mLastnamePref.toLowerCase())) {
					add = false;
				}
			}
			if(add && mCbGradelevel.isChecked() && mGradeLevel != -1) { // Fiter by grade level
				if(student.getGradeLevel() != mGradeLevel) {
					add = false;
				}
			}
			
			if(add) {
				mStudents.add(student);
			}
		}
		
		mAdapter.notifyDataSetChanged();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.student_selector, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == android.R.id.home) { // Done
			handleSelectComplete();
			return true;
		} else if(id == R.id.action_select_all) { // Select all
			handleSelectAll();
			return true;
		} else if(id == R.id.action_reverse) { // Reverse selection
			handleReverseSelection();
			return true;
		} else if(id == R.id.action_clear) { // Clear selection
			handleClearSelection();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void handleSelectAll() {
		for(int i = 0; i < mAdapter.getCount(); ++i) {
			mLvStudents.setItemChecked(i, true);
		}
		updateTitle();
	}

	private void handleReverseSelection() {
		for(int i = 0; i < mAdapter.getCount(); ++i) {
			mLvStudents.setItemChecked(i, !mLvStudents.isItemChecked(i));
		}
		updateTitle();
	}

	private void handleClearSelection() {
		for(int i = 0; i < mAdapter.getCount(); ++i) {
			mLvStudents.setItemChecked(i, false);
		}
		updateTitle();
	}

	private void handleSelectComplete() {
		// Result selcted student as a list of their object id
		ArrayList<String> ids = new ArrayList<String>();
		for(int i = 0; i < mAdapter.getCount(); ++i) {
			if(mLvStudents.isItemChecked(i)) {
				Student student = (Student) mAdapter.getItem(i);
				ids.add(student.getObjectId());
			}
		}
		
		Intent intent = new Intent();
		intent.putStringArrayListExtra(Const.EXTRA_OBJECT_ID_LIST, ids);
		setResult(RESULT_OK, intent);
		finish();
	}

	private OnItemClickListener mOnStudentClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			updateTitle();
		}
	};
	
	private Comparator<Student> mSortByStudentId = new Comparator<Student>() {
		@Override
		public int compare(Student lhs, Student rhs) {
			return lhs.getStudentId().compareTo(rhs.getStudentId());
		}
	};
	
	private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(isChecked) {
				int id = buttonView.getId();
				if(id == R.id.students_selector_id_cb) {
					mStudentIdFilterDialog.show();
				} else if(id == R.id.students_selector_firstname_cb) {
					mFirstnameFilterDialog.show();
				} else if(id == R.id.students_selector_lastname_cb) {
					mLastnameFilterDialog.show();
				} else if(id == R.id.students_selector_gradelevel_cb) {
					mGradeLevelFilterDialog.show();
				}
			} else if(mCanceled) { // Filter dialog cancelled, do not update
				mCanceled = false;
			} else { // Filter unchecked, update
				updateStudentsFilter();
			}
		}
	};
	
}
