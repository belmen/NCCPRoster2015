package nccp.app.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import nccp.app.R;
import nccp.app.parse.object.Attendance;
import nccp.app.parse.object.Student;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AttendanceAdapter extends BaseAdapter {

	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("h:m a", Locale.US);
	
	private Context mContext;
	private List<Student> mStudents = null;
	private Map<String, Attendance> mAttendanceMap = new HashMap<String, Attendance>();
	
	public AttendanceAdapter(Context context) {
		this.mContext = context;
	}

	public void setStudents(List<Student> students) {
		this.mStudents = students;
	}

	public void addAttendance(Attendance attendance) {
		if(attendance != null) {
			mAttendanceMap.put(attendance.getStudent().getObjectId(), attendance);
		}
	}

	public void addAllAttendance(List<Attendance> attendances) {
		for(Attendance a : attendances) {
			addAttendance(a);
		}
	}
	
	public void clearAttendance() {
		mAttendanceMap.clear();
	}
	
	public Attendance getAttendance(Student student) {
		return mAttendanceMap.get(student.getObjectId());
	}
	
	@Override
	public int getCount() {
		return mStudents != null ? mStudents.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return mStudents != null ? mStudents.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh;
		if(convertView == null) {
			convertView = View.inflate(mContext, R.layout.item_attendance, null);
			vh = new ViewHolder();
			vh.id = (TextView) convertView.findViewById(R.id.item_attendance_id_text);
			vh.name = (TextView) convertView.findViewById(R.id.item_attendance_name_text);
			vh.time = (TextView) convertView.findViewById(R.id.item_attendance_time_text);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		Student student = (Student) getItem(position);
		if(student != null) {
			vh.id.setText(student.getStudentId());
			vh.name.setText(student.getFullName());
			
			Attendance attendance = getAttendance(student);
			if(attendance != null) {
				StringBuilder sb = new StringBuilder();
				Date timeIn = attendance.getTimeIn();
				if(timeIn != null) {
					sb.append(mContext.getString(R.string.attendance_time_in, timeFormat.format(timeIn)));
					
					Date timeOut = attendance.getTimeOut();
					if(timeOut != null) {
						sb.append('\n')
						.append(mContext.getString(R.string.attendance_time_out, timeFormat.format(timeOut)));
					}
				}
				vh.time.setText(sb.toString());
			} else {
				vh.time.setText("");
			}
		}
		return convertView;
	}

	static class ViewHolder {
		TextView id;
		TextView name;
		TextView time;
	}
}
