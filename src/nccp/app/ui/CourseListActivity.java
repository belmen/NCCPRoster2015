package nccp.app.ui;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import nccp.app.R;
import nccp.app.adapter.CourseAdapter;
import nccp.app.data.DataCenter;
import nccp.app.parse.object.Course;
import nccp.app.parse.object.Program;
import nccp.app.parse.object.ProgramClass;
import nccp.app.utils.Const;
import nccp.app.utils.Logger;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;

public class CourseListActivity extends ToolbarActivity {

	public static final String TAG = CourseListActivity.class.getSimpleName();
	
	private static final int REQUEST_EDIT_COURSE = 0;
	
	// Views
	private ListView mLvCourse;
//	private CourseEditorFragment mEditDialog;
	// Data
//	private ProgramClass mProgramClass = null;
	private int mProgramIndex = -1;
	private int mClassIndex = -1;
	private List<Course> mCourses = null;
	private CourseAdapter mAdapter = null;
	private boolean mChanged = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_course_list);
		initViews();
		initToolbar();
		initData();
		
		mCourses = getCourses();
		showCourses(mCourses);
	}
	
	private void initViews() {
		mLvCourse = (ListView) findViewById(R.id.course_list_listview);
		mLvCourse.setOnItemClickListener(mOnCourseClickListener);
		mLvCourse.setMultiChoiceModeListener(mRemoveModeListener);
		TextView tvEmpty = (TextView) findViewById(R.id.course_empty_text);
		mLvCourse.setEmptyView(tvEmpty);
		mAdapter = new CourseAdapter(CourseListActivity.this);
		mLvCourse.setAdapter(mAdapter);
//		mEditDialog = new CourseEditorFragment();
	}

	private void initToolbar() {
		ActionBar ab = getSupportActionBar();
//		ab.setDisplayHomeAsUpEnabled(true);
//		ab.setHomeAsUpIndicator(R.drawable.ic_menu_check_24dp);
		ab.setTitle(R.string.title_course_list);
	}

	private void initData() {
		Intent intent = getIntent();
		
		if(intent != null) {
			mProgramIndex = intent.getIntExtra(Const.EXTRA_PROGRAM_INDEX, -1);
			mClassIndex = intent.getIntExtra(Const.EXTRA_CLASS_INDEX, -1);
		}
		
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
	
	private List<Course> getCourses() {
		List<Course> course = null;
		ProgramClass programClass = getProgramClass();
		if(programClass != null) {
			course = programClass.getCourses();
		}
		return course;
	}

	private void showCourses(List<Course> courses) {
		if(courses != null) {
			mAdapter.setData(courses);
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.course_list, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == R.id.action_new_course) { // New course
			handleAddCourse();
		} else if(id == R.id.action_remove_course) { // Enter remove mode
			startRemoveMode();
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

	private void startRemoveMode() {
		if(mAdapter.getCount() > 0) { // Has items
			mLvCourse.setItemChecked(0, true); // Check one to automatically enter action mode
			mLvCourse.clearChoices(); // Clear that choice
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_EDIT_COURSE) {
			if(resultCode == RESULT_OK) {
				mChanged = true; // Mark as changed
				mCourses = getCourses();
				showCourses(mCourses);
			}
		}
	}

	private void handleAddCourse() {
		Intent intent = new Intent(CourseListActivity.this, CourseEditorActivity.class);
		intent.putExtra(Const.EXTRA_PROGRAM_INDEX, mProgramIndex);
		intent.putExtra(Const.EXTRA_CLASS_INDEX, mClassIndex);
		startActivityForResult(intent, REQUEST_EDIT_COURSE);
	}
	

	private void handleRemoveCourses(Set<Integer> indices) {
		new RemoveCoursesTask(getProgramClass(), indices).execute();
	}

	private void handleRemoveSuccess() {
		mChanged = true; // Mark as changed
		mAdapter.notifyDataSetChanged();
	}
	
	private OnItemClickListener mOnCourseClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// Launch editor activity
			Intent intent = new Intent(CourseListActivity.this, CourseEditorActivity.class);
			intent.putExtra(Const.EXTRA_PROGRAM_INDEX, mProgramIndex);
			intent.putExtra(Const.EXTRA_CLASS_INDEX, mClassIndex);
			intent.putExtra(Const.EXTRA_COURSE_INDEX, position);
			startActivityForResult(intent, REQUEST_EDIT_COURSE);
		}
	};
	
	private MultiChoiceModeListener mRemoveModeListener = new MultiChoiceModeListener() {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			mode.setTitle(R.string.title_course_remove_mode);
			getMenuInflater().inflate(R.menu.remove_course, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			int id = item.getItemId();
			if(id == R.id.action_remove_commit) {
				handleCommitRemove(mode);
				return true;
			}
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
		}

		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position,
				long id, boolean checked) {
		}
		
		private void handleCommitRemove(final ActionMode mode) {
			int count = mLvCourse.getCheckedItemCount();
			if(count == 0) {
				Toast.makeText(CourseListActivity.this,
						R.string.msg_no_course_selected, Toast.LENGTH_SHORT).show();
			} else {
				final Set<Integer> indices = new HashSet<Integer>();
				for(int i = 0; i < mAdapter.getCount(); ++i) {
					if(mLvCourse.isItemChecked(i)) {
						indices.add(i);
					}
				}
				// Show dialog
				Course first = null;
				for(Integer index : indices) {
					first = (Course) mAdapter.getItem(index);
					break;
				}
				String msg = count > 1 ? getString(R.string.dialog_msg_remove_course, count)
						: getString(R.string.dialog_msg_remove_course_1, first.getCourseName());
				new AlertDialog.Builder(CourseListActivity.this)
				.setTitle(R.string.dialog_title_remove_course)
				.setMessage(msg)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						handleRemoveCourses(indices);
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
	
	private class RemoveCoursesTask extends AsyncTask<Void, Void, Void> {

		private ProgramClass programClass;
		private Set<Integer> indices;
		private ParseException e;
		
		public RemoveCoursesTask(ProgramClass programClass, Set<Integer> indices) {
			this.programClass = programClass;
			this.indices = indices;
		}

		@Override
		protected void onPreExecute() {
			showProgressBar(true);
		}

		@Override
		protected Void doInBackground(Void... params) {
			List<Course> courses = programClass.getCourses();
			try {
				Iterator<Course> iter = courses.iterator();
				int i = 0;
				while(iter.hasNext()) {
					Course course = iter.next();
					if(indices.contains(i)) {
						course.delete(); // Remove course from remote DB
						iter.remove(); // Remove from list
					}
					++i;
				}
				programClass.save();
			} catch (ParseException e) {
				this.e = e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			showProgressBar(false);
			if(e == null) {
				handleRemoveSuccess();
			} else {
				Logger.e(TAG, e.getMessage(), e);
				Toast.makeText(CourseListActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
	}
}
