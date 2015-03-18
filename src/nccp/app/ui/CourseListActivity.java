package nccp.app.ui;

import java.util.List;

import nccp.app.R;
import nccp.app.adapter.CourseAdapter;
import nccp.app.data.DataCenter;
import nccp.app.parse.object.Course;
import nccp.app.parse.object.Program;
import nccp.app.parse.object.ProgramClass;
import nccp.app.utils.Const;
import android.content.Intent;
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
		mLvCourse = (ListView) findViewById(R.id.course_editor_listview);
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
	
	private List<Course> getCourses() {
		List<Course> course = null;
		Program program = null;
		ProgramClass programClass = null;
		if(mProgramIndex != -1) {
			program = DataCenter.getPrograms().get(mProgramIndex);
		}
		if(program != null && mClassIndex != -1) {
			programClass = program.getClasses().get(mClassIndex);
		}
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
				mCourses = getCourses();
				showCourses(mCourses);
			}
		}
	}

//	private void handleEditDone() {
//		setResult(RESULT_OK);
//		finish();
//	}

	private void handleAddCourse() {
		Intent intent = new Intent(CourseListActivity.this, CourseEditorActivity.class);
		intent.putExtra(Const.EXTRA_PROGRAM_INDEX, mProgramIndex);
		intent.putExtra(Const.EXTRA_CLASS_INDEX, mClassIndex);
		startActivityForResult(intent, REQUEST_EDIT_COURSE);
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
			if(id == R.id.action_remove_course) {
				
				return true;
			}
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position,
				long id, boolean checked) {
			// TODO Auto-generated method stub
			
		}
		
	};
}
