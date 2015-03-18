package nccp.app.adapter;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import nccp.app.R;
import nccp.app.parse.object.Course;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CourseAdapter extends BaseAdapter {

	private static SimpleDateFormat courseDateFormat =
			new SimpleDateFormat("EEEE h:mm a", Locale.US);
	private Context mContext;
	private List<Course> mData = null;;
	
	public CourseAdapter(Context context) {
		this.mContext = context;
	}

	public void setData(List<Course> data) {
		this.mData = data;
	}

	@Override
	public int getCount() {
		return mData != null ? mData.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return mData != null ? mData.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh;
		if(convertView == null) {
			convertView = View.inflate(mContext, R.layout.item_course, null);
			vh = new ViewHolder();
			vh.name = (TextView) convertView.findViewById(R.id.course_name);
			vh.info = (TextView) convertView.findViewById(R.id.course_info);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		
		Course item = (Course) getItem(position);
		if(item != null) {
			vh.name.setText(item.getCourseName());
			
			String time = mContext.getString(R.string.list_item_course_time,
					courseDateFormat.format(item.getTime()),
					item.getDuration());
			vh.info.setText(time);
		}
		
		return convertView;
	}

	static class ViewHolder {
		TextView name;
		TextView info;
	}
}
