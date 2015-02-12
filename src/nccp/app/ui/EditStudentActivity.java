package nccp.app.ui;

import nccp.app.R;
import nccp.app.bean.Student;
import nccp.app.utils.Const;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;

public class EditStudentActivity extends ActionBarActivity {

	public static final String TAG = EditStudentActivity.class.getSimpleName();
	
	// Views
	private EditText mEtStudentId;
	private EditText mEtFirstName;
	private EditText mEtLastName;
	private Spinner mSpGradeLevel;
	private ProgressBar mProgressBar;
//	private EditText mEtAddress;
//	private EditText mEtSite;
//	private EditText mEtProgram;
//	private EditText mEtSchool;
//	private EditText mEtContactName;
//	private EditText mEtContactPhone;
//	private EditText mEtContactRelationship;
	
	// Data
	private Student mStudent = null;
	private String[] mGradeLevels = null;
	private boolean mInProgress = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_student);
		mGradeLevels = getResources().getStringArray(R.array.grade_levels);
		initViews();
		initToolbar();
		initData();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.student_editor, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem doneMenu = menu.getItem(0);
		doneMenu.setEnabled(!mInProgress);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == android.R.id.home) {
			finish();
			return true;
		} else if(id == R.id.action_student_editor_ok) {
			handleEditDone();
		}
		return super.onOptionsItemSelected(item);
	}

	private void initViews() {
		mEtStudentId = (EditText) findViewById(R.id.student_editor_id_edit);
		mEtFirstName = (EditText) findViewById(R.id.student_editor_first_name_edit);
		mEtLastName = (EditText) findViewById(R.id.student_editor_last_name_edit);
		mSpGradeLevel = (Spinner) findViewById(R.id.student_editor_gradelevel_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(EditStudentActivity.this,
				R.array.grade_levels, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpGradeLevel.setAdapter(adapter);
		mProgressBar = (ProgressBar) findViewById(R.id.student_editor_progress);
	}

	private void initToolbar() {
		Toolbar tb = (Toolbar) findViewById(R.id.edit_student_toolbar);
		setSupportActionBar(tb);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private void initData() {
		Intent intent = getIntent();
		if(intent != null) {
			mStudent = (Student) intent.getSerializableExtra(Const.EXTRA_STUDENT);
			
		}
		if(mStudent == null) {
			mStudent = new Student();
			getSupportActionBar().setTitle(R.string.title_add_student);
		} else {
			fillFields();
			getSupportActionBar().setTitle(R.string.title_edit_student);
		}
	}

	private void fillFields() {
		if(mStudent == null) {
			return;
		}
		mEtStudentId.setText(mStudent.getStudentId());
		mEtFirstName.setText(mStudent.getFirstName());
		mEtLastName.setText(mStudent.getLastName());
		String gradeLevel = String.valueOf(mStudent.getGradeLevel());
		SpinnerAdapter adapter = mSpGradeLevel.getAdapter();
		for(int i = 0; i < adapter.getCount(); ++i) {
			if(((String) adapter.getItem(i)).equals(gradeLevel)) {
				mSpGradeLevel.setSelection(i);
				break;
			}
		}
	}

	private void handleEditDone() {
		String firstname = mEtFirstName.getText().toString();
		if(firstname.length() == 0) {
			mEtFirstName.setError(getString(R.string.student_editor_error_firstname_empty));
			return;
		}
		
		String lastname = mEtLastName.getText().toString();
		String studentId = mEtStudentId.getText().toString();
		if(studentId.length() == 0) {
			mEtStudentId.setError(getString(R.string.student_editor_error_id_empty));
			return;
		}
		String gradeLevelStr = mGradeLevels[mSpGradeLevel.getSelectedItemPosition()];
		int gradeLevel = Integer.parseInt(gradeLevelStr);
		
		mStudent.setFirstName(firstname);
		mStudent.setLastName(lastname);
		mStudent.setStudentId(studentId);
		mStudent.setGradeLevel(gradeLevel);
		
		// Save student
		new SaveStudentTask(mStudent).execute();
	}
	
	private void handleSaveSuccess() {
		Intent intent = new Intent();
		intent.putExtra(Const.EXTRA_STUDENT, mStudent);
		setResult(RESULT_OK, intent);
		finish();
	}
	
	private class SaveStudentTask extends AsyncTask<Void, Void, Void> {

		private Student student;
		private ParseException e = null;

		public SaveStudentTask(Student student) {
			this.student = student;
		}

		@Override
		protected Void doInBackground(Void... params) {
			ParseObject obj = Student.toParseObject(student);
			try {
				obj.save();
			} catch (ParseException e) {
				this.e = e;
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			mInProgress = true;
			mProgressBar.setVisibility(View.VISIBLE);
			supportInvalidateOptionsMenu();
		}

		@Override
		protected void onPostExecute(Void result) {
			mInProgress = false;
			mProgressBar.setVisibility(View.GONE);
			supportInvalidateOptionsMenu();
			
			if(e == null) {
				handleSaveSuccess();
			} else {
				// Show fail toast
				Toast.makeText(EditStudentActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}
	}
}
