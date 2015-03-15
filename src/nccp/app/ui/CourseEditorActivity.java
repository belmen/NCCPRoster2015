package nccp.app.ui;

import nccp.app.R;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ProgressBar;

public class CourseEditorActivity extends BaseActivity {

	public static final String TAG = CourseEditorActivity.class.getSimpleName();

	private ProgressBar mProgress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_course);
		initViews();
		initToolbar();
	}
	
	private void initViews() {
		mProgress = (ProgressBar) findViewById(R.id.edit_course_progress);
	}

	private void initToolbar() {
		Toolbar tb = (Toolbar) findViewById(R.id.edit_course_toolbar);
		
		setSupportActionBar(tb);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_check_24dp);
	}
}
