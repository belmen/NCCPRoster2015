package nccp.app.ui;

import nccp.app.R;
import nccp.app.parse.object.Student;
import nccp.app.utils.Logger;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class StudentDetailFragment extends Fragment {

	public static final String TAG = StudentDetailFragment.class.getSimpleName();
	
	private TextView mTvStudentId;
	private TextView mTvFirstName;
	private TextView mTvLastName;
	private TextView mTvGradeLevel;
	
	private Student mStudent = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Logger.i(TAG, TAG + "Detail fragment onCreateView");
		View v = inflater.inflate(R.layout.fragment_student_detail, container, false);
		mTvStudentId = (TextView) v.findViewById(R.id.student_detail_id_text);
		mTvFirstName = (TextView) v.findViewById(R.id.student_detail_firstname_text);
		mTvLastName = (TextView) v.findViewById(R.id.student_detail_lastname_text);
		mTvGradeLevel = (TextView) v.findViewById(R.id.student_detail_gradelevel_text);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		showStudentDetail();
	}

	public void setStudent(Student student) {
		mStudent = student;
		if(getView() != null) {
			showStudentDetail();
		}
	}

	private void showStudentDetail() {
		if(mStudent == null) {
			return;
		}
		mTvStudentId.setText(mStudent.getStudentId());
		mTvFirstName.setText(mStudent.getFirstName());
		mTvLastName.setText(mStudent.getLastName());
		mTvGradeLevel.setText(String.valueOf(mStudent.getGradeLevel()));
	}
}
