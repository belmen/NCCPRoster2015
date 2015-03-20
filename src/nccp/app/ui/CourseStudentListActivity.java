package nccp.app.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nccp.app.R;
import nccp.app.adapter.StudentInfoAdapter;
import nccp.app.data.DataCenter;
import nccp.app.parse.object.Program;
import nccp.app.parse.object.ProgramClass;
import nccp.app.parse.object.Student;
import nccp.app.utils.Const;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class CourseStudentListActivity extends ToolbarActivity {

	public static final String TAG = CourseStudentListActivity.class.getSimpleName();
	
	private static final int REQUEST_ADD_STUDENTS = 0;
	
	// Views
	private ListView mLvStudents;
	// Data
	private int mProgramIndex = -1;
	private int mClassIndex = -1;
	private List<Student> mStudents = null;
	private StudentInfoAdapter mAdapter = null;
	private boolean mInProgress = false;
	private boolean mChanged = false;
	private boolean mForceEnterRemoveMode = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_course_student_list);
		initViews();
		initToolbar();
		initData();
		showStudents();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.course_student_list, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == R.id.action_add_students) { // Add students
			handleAddStudents();
			return true;
		} else if(id == R.id.action_remove_students) { // Remove students
			handleRemoveStudents();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		if(mChanged) {
			setResult(RESULT_OK);
		}
		super.onBackPressed();
	}

	private void initViews() {
		mLvStudents = (ListView) findViewById(R.id.course_student_list_listview);
		mLvStudents.setMultiChoiceModeListener(mRemoveModeListener);
		mAdapter = new StudentInfoAdapter(CourseStudentListActivity.this);
		mLvStudents.setAdapter(mAdapter);
		TextView tvEmpty = (TextView) findViewById(R.id.course_student_empty_text);
		mLvStudents.setEmptyView(tvEmpty);
	}
	
	private void initToolbar() {
		ActionBar ab = getSupportActionBar();
		ab.setTitle(R.string.title_student_list);
	}
	
	private void initData() {
		Intent intent = getIntent();
		
		if(intent != null) {
			mProgramIndex = intent.getIntExtra(Const.EXTRA_PROGRAM_INDEX, -1);
			mClassIndex = intent.getIntExtra(Const.EXTRA_CLASS_INDEX, -1);
		}
		// We need these two extras for this activity
		if(mProgramIndex == -1 || mClassIndex == -1) {
			finish();
			return;
		}
	}
	
	private ProgramClass getProgramClass() {
		ProgramClass programClass = null;
		Program program = null;
		if(mProgramIndex != -1) {
			program = DataCenter.getPrograms().get(mProgramIndex);
		}
		if(program != null && mClassIndex != -1) {
			programClass = program.getClasses().get(mClassIndex);
		}
		return programClass;
	}
	
	private void showStudents() {
		final ProgramClass programClass = getProgramClass();
		if(programClass == null) {
			return;
		}
		List<Student> students = DataCenter.getCachedStudents(programClass);
		if(students != null) { // Show cached data
			mStudents = students;
			mAdapter.setData(mStudents);
			mAdapter.notifyDataSetChanged();
		} else { // Fetch from remote
			showProgressBar(true);
			ParseQuery<Student> q = ParseQuery.getQuery(Student.class);
			q.whereEqualTo(Student.TAG_ENROLLED_IN, programClass);
			q.findInBackground(new FindCallback<Student>() {
				@Override
				public void done(List<Student> data, ParseException e) {
					showProgressBar(false);
					if(e == null) { // Success
//						programClass.setCachedStudents(data);
						DataCenter.setCachedStudents(programClass, data);
						mStudents = data;
						mAdapter.setData(mStudents);
						mAdapter.notifyDataSetChanged();
					} else { // Fail
						logAndToastException(e);
					}
				}
			});
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_ADD_STUDENTS) {
			if(resultCode == RESULT_OK) {
				List<String> ids = data.getStringArrayListExtra(Const.EXTRA_OBJECT_ID_LIST);
				if(ids != null) {
					handleStudentsSelected(ids);
				}
			}
		}
	}

	private void handleAddStudents() {
		Intent intent = new Intent(CourseStudentListActivity.this, StudentsSelectorActivity.class);
		startActivityForResult(intent, REQUEST_ADD_STUDENTS);
	}

	private void handleRemoveStudents() {
		if(mAdapter.getCount() > 0) { // Has items
			mForceEnterRemoveMode = true;
			mLvStudents.setItemChecked(0, true); // Check one to automatically enter action mode
			mLvStudents.clearChoices(); // Clear that choice
		}
	}
	
	private void handleStudentsSelected(List<String> ids) {
		if(mInProgress) {
			return;
		}
		final List<Student> addedStudents = new ArrayList<Student>();
		ProgramClass programClass = getProgramClass();
		
		Set<String> enrolledIds = new HashSet<String>();
		for(Student student : mStudents) {
			enrolledIds.add(student.getObjectId());
		}
		int existCounter = 0;
		for(String id : ids) {
			if(enrolledIds.contains(id)) { // Check duplicates
				++existCounter;
			} else {
				Student student = DataCenter.getStudentByObjectId(id);
				if(student != null) {
					// Remove from old program class cache
					ProgramClass oldClass = student.getEnrolledIn();
					if(oldClass != null) {
						List<Student> cachedStudents = DataCenter.getCachedStudents(oldClass);
						if(cachedStudents != null) {
							cachedStudents.remove(student);
						}
					}
					
					student.setEnrolledIn(programClass);
					addedStudents.add(student);
				}
			}
		}
		// Add student to cache
		List<Student> cachedStudents = DataCenter.getCachedStudents(programClass);
		if(cachedStudents != null) {
			cachedStudents.addAll(addedStudents);
		} else {
			DataCenter.setCachedStudents(programClass, addedStudents);
		}
		
		// Save to remote
		final int existed = existCounter;
		if(addedStudents.size() > 0) {
			ParseObject.saveAllInBackground(addedStudents, new SaveCallback() {
				@Override
				public void done(ParseException e) {
					if(e == null) { // Success
						handleStudentAdded(addedStudents, existed);
					} else {
						logAndToastException(e);
					}
				}
			});
		} else {
			handleStudentAdded(addedStudents, existed);
		}
	}
	
	private void handleStudentAdded(List<Student> addedStudents, int existed) {
		int added = addedStudents.size();
		if(added > 0) {
			mChanged = true;
			mAdapter.notifyDataSetChanged();
		}
		
		String msg;
		if(added == 1) {
			if(existed == 0) {
				msg = getString(R.string.msg_students_added_1);
			} else {
				msg = getString(R.string.msg_students_added_1_exists, existed);
			}
		} else {
			if(existed == 0) {
				msg = getString(R.string.msg_students_added, added);
			} else {
				msg = getString(R.string.msg_students_added_existed, added, existed);
			}
		}
		Toast.makeText(CourseStudentListActivity.this, msg, Toast.LENGTH_SHORT).show();
	}
	
	private void handleRemoveStudents(List<Student> students) {
		new RemoveStudentsTask(students).execute();
	}
	

	private void handleStudentRemoved(List<Student> removedStudents) {
		mChanged = true;
		ProgramClass programClass = getProgramClass();
		List<Student> cachedStudents = DataCenter.getCachedStudents(programClass);
		if(cachedStudents != null) {
			cachedStudents.removeAll(removedStudents);
		}
		mAdapter.notifyDataSetChanged();
	}
	
	private MultiChoiceModeListener mRemoveModeListener = new MultiChoiceModeListener() {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			getMenuInflater().inflate(R.menu.remove_course_student, menu);
			return true;
		}
		
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
		
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// Set title back
			getSupportActionBar().setTitle(R.string.title_student_list);
		}
		
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			int id = item.getItemId();
			if(id == R.id.action_select_all) {
				handleSelectAll();
				return true;
			} else if(id == R.id.action_remove_commit) {
				handleRemoveCommit(mode);
				return true;
			}
			return false;
		}
		
		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position,
				long id, boolean checked) {
			updateTitle(mode);
		}
		
		private void updateTitle(ActionMode mode) {
			int count;
			if(mForceEnterRemoveMode) {
				mForceEnterRemoveMode = false;
				count = 0;
			} else {
				count = mLvStudents.getCheckedItemCount();
			}
			String title = count == 1 ? getString(R.string.title_1_student_selected)
					: getString(R.string.title_n_students_selected, count);
			mode.setTitle(title);
		}

		private void handleSelectAll() {
			for(int i = 0; i < mAdapter.getCount(); ++i) {
				mLvStudents.setItemChecked(i, true);
			}
		}

		private void handleRemoveCommit(final ActionMode mode) {
			int count = mLvStudents.getCheckedItemCount();
			if(count == 0) {
				Toast.makeText(CourseStudentListActivity.this,
						R.string.msg_no_course_selected, Toast.LENGTH_SHORT).show();
			} else {
				final List<Student> students = new ArrayList<Student>();
				for(int i = 0; i < mAdapter.getCount(); ++i) {
					if(mLvStudents.isItemChecked(i)) {
						students.add((Student) mAdapter.getItem(i));
					}
				}
				// Show dialog
				Student first = students.get(0);
				ProgramClass programClass = getProgramClass();
				String msg = count > 1 ?
					getString(R.string.dialog_msg_remove_student_from_class,
							count, programClass.getTitle())
					: getString(R.string.dialog_msg_remove_student_from_class_1,
							first.getFullName(), programClass.getTitle());
				new AlertDialog.Builder(CourseStudentListActivity.this)
				.setTitle(R.string.dialog_title_remove_student)
				.setMessage(msg)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						handleRemoveStudents(students);
						mode.finish();
					}
				}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						return;
					}
				}).show();
			}
		}
	};
	
	private class RemoveStudentsTask extends AsyncTask<Void, Void, Void> {
		
		private List<Student> students;
		private ParseException e;

		public RemoveStudentsTask(List<Student> students) {
			this.students = students;
		}

		@Override
		protected void onPreExecute() {
			mInProgress = true;
			showProgressBar(true);
		}

		@Override
		protected Void doInBackground(Void... params) {
			for(Student student : students) {
				try {
					student.removeEnrolledIn();
					student.save();
				} catch (ParseException e) {
					this.e = e;
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mInProgress = false;
			showProgressBar(false);
			if(e == null) { // Success
				handleStudentRemoved(students);
			} else { // Fail
				logAndToastException(e);
			}
		}
	}
}
