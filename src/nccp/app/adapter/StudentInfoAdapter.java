package nccp.app.adapter;

import java.util.List;

import nccp.app.R;
import nccp.app.parse.object.Student;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class StudentInfoAdapter extends BaseAdapter {

	private Context mContext;
	private List<Student> mData;
	
	public StudentInfoAdapter(Context context) {
		this.mContext = context;
	}

	public void setData(List<Student> data) {
		this.mData = data;
	}

	@Override
	public int getCount() {
		return mData != null ? mData.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return mData != null ? mData.get(position) : 0;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh;
		if(convertView == null) {
			convertView = View.inflate(mContext, R.layout.item_student_selector, null);
			vh = new ViewHolder();
			vh.id = (TextView) convertView.findViewById(R.id.item_add_student_id);
			vh.firstname = (TextView) convertView.findViewById(R.id.item_add_student_firstname);
			vh.lastname = (TextView) convertView.findViewById(R.id.item_add_student_lastname);
			vh.gradelevel = (TextView) convertView.findViewById(R.id.item_add_student_gradelevel);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		
		Student item = (Student) getItem(position);
		if(item != null) {
			vh.id.setText(item.getStudentId());
			vh.firstname.setText(item.getFirstName());
			vh.lastname.setText(item.getLastName());
			vh.gradelevel.setText(String.valueOf(item.getGradeLevel()));
		}
		return convertView;
	}

	static class ViewHolder {
		TextView id;
		TextView firstname;
		TextView lastname;
		TextView gradelevel;
	}
}
