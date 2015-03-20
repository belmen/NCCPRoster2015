package nccp.app.ui;

import java.util.ArrayList;
import java.util.List;

import nccp.app.R;
import nccp.app.adapter.StudentInfoAdapter;
import nccp.app.data.DataCenter;
import nccp.app.parse.object.Student;
import nccp.app.utils.Const;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class StudentsSelectorActivity extends ToolbarActivity {

	public static final String TAG = StudentsSelectorActivity.class.getSimpleName();

	// Views
	private ListView mLvStudents;
	// Data
	private List<Student> mAllStudents = null;
	private List<Student> mStudents = null;
	private StudentInfoAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_students_selector);
		initToolbar();
		initViews();
		initData();
		
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
	}
	
	private void initData() {
		mAllStudents = DataCenter.getStudents();
		mStudents = new ArrayList<Student>();
		if(mAllStudents != null) {
			mStudents.addAll(mAllStudents);
		}
	}

	private void updateTitle() {
		int count = mLvStudents.getCheckedItemCount();
		getSupportActionBar().setTitle(getString(R.string.title_students_selector, count));
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
}
