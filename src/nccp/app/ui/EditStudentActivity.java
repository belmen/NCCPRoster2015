package nccp.app.ui;

import nccp.app.R;
import nccp.app.bean.Student;
import nccp.app.utils.Const;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class EditStudentActivity extends ActionBarActivity {

	public static final String TAG = EditStudentActivity.class.getSimpleName();
	
	private EditText mEtStudentId;
	private EditText mEtFirstName;
	private EditText mEtLastName;
	private Spinner mSpGradeLevel;
//	private EditText mEtAddress;
//	private EditText mEtSite;
//	private EditText mEtProgram;
//	private EditText mEtSchool;
//	private EditText mEtContactName;
//	private EditText mEtContactPhone;
//	private EditText mEtContactRelationship;
	
	private Student mStudent = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_student);
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
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == android.R.id.home) {
			finish();
			return true;
		} else if(id == R.id.action_student_editor_ok) {
			
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
		} else {
			fillFields();
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
	
}
