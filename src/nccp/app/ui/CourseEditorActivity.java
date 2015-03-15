package nccp.app.ui;

import java.util.List;

import nccp.app.R;
import nccp.app.adapter.CourseAdapter;
import nccp.app.data.DataCenter;
import nccp.app.parse.object.Course;
import nccp.app.parse.object.Program;
import nccp.app.parse.object.ProgramClass;
import nccp.app.utils.Const;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

public class CourseEditorActivity extends BaseActivity {

	public static final String TAG = CourseEditorActivity.class.getSimpleName();

	// Views
	private ProgressBar mProgress;
	private ListView mLvCourse;
	private CourseEditorFragment mEditDialog;
	// Data
	private ProgramClass mProgramClass = null;
	private List<Course> mCourses = null;
	private CourseAdapter mAdapter = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_course_editor);
		initViews();
		initToolbar();
		initData();
		
		showCourses();
	}
	
	private void initViews() {
		mProgress = (ProgressBar) findViewById(R.id.course_editor_progress);
		mLvCourse = (ListView) findViewById(R.id.course_editor_listview);
		TextView tvEmpty = (TextView) findViewById(R.id.course_empty_text);
		mLvCourse.setEmptyView(tvEmpty);
		mAdapter = new CourseAdapter(CourseEditorActivity.this);
		mLvCourse.setAdapter(mAdapter);
		mEditDialog = new CourseEditorFragment();
	}

	private void initToolbar() {
		Toolbar tb = (Toolbar) findViewById(R.id.edit_course_toolbar);
		
		setSupportActionBar(tb);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_check_24dp);
		getSupportActionBar().setTitle(R.string.title_course_editor);
	}

	private void initData() {
		Intent intent = getIntent();
		Program program = null;
		if(intent != null) {
			int programIndex = intent.getIntExtra(Const.EXTRA_PROGRAM_INDEX, -1);
			if(programIndex != -1) {
				program = DataCenter.getPrograms().get(programIndex);
			}
			int classIndex = intent.getIntExtra(Const.EXTRA_CLASS_INDEX, -1);
			if(classIndex != -1 && program != null) {
				mProgramClass = program.getClasses().get(classIndex);
			}
		}
		
		if(program == null || mProgramClass == null) {
			finish();
			return;
		}
		
		mCourses = mProgramClass.getCourses();
		
	}

	private void showCourses() {
		mAdapter.setData(mCourses);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.course_editor, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == android.R.id.home) { // Done
			handleEditDone();
		} else if(id == R.id.action_new_course) { // New course
			handleAddCourse();
		}
		return super.onOptionsItemSelected(item);
	}

	private void handleEditDone() {
		// TODO Auto-generated method stub
		
	}

	private void handleAddCourse() {
		mEditDialog.show(getSupportFragmentManager(), "edit_course");
	}
	
	public static class CourseEditorFragment extends DialogFragment {
		
		public static final String TAG = CourseEditorFragment.class.getSimpleName();
		
		private EditText mEtCourseName;
		private Spinner mSpDayOfWeek;
		private TimePicker mTpTime;
		private Spinner mSpDuration;
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			View v = View.inflate(getActivity(), R.layout.dialog_edit_course, null);
			initViews(v);
			return new AlertDialog.Builder(getActivity())
			.setView(v)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			}).create();
		}

		private void initViews(View v) {
			mEtCourseName = (EditText) v.findViewById(R.id.edit_course_edittext);
			mSpDayOfWeek = (Spinner) v.findViewById(R.id.edit_course_dayofweek_spinner);
			mTpTime = (TimePicker) v.findViewById(R.id.edit_course_timepicker);
			mSpDuration = (Spinner) v.findViewById(R.id.edit_course_duration_spinner);
		}
	}
}
