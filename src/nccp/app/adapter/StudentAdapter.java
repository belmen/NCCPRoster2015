package nccp.app.adapter;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import nccp.app.R;
import nccp.app.parse.object.Student;
import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class StudentAdapter extends BaseExpandableListAdapter {
	
	public static final String TAG = StudentAdapter.class.getSimpleName();
	
	private static final String HIGHLIGHT_COLOR = "#00aeed";

	private Context mContext;
	private List<String> mInitials = null;
	private Map<String, List<Student>> mData = null;
	private String mHighlight = "";
	
	public StudentAdapter(Context context) {
		this.mContext = context;
	}

	public void setData(List<String> initials, Map<String, List<Student>> data) {
		this.mInitials = initials;
		this.mData = data;
	}

	public void setHighlight(String highlight) {
		this.mHighlight = highlight;
	}

	@Override
	public int getGroupCount() {
		return mInitials != null ? mInitials.size() : 0;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if(mInitials == null || mData == null) {
			return 0;
		}
		List<Student> child = mData.get(mInitials.get(groupPosition));
		return child != null ? child.size() : 0;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mInitials != null ? mInitials.get(groupPosition) : null;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		if(mInitials == null || mData == null) {
			return null;
		}
		List<Student> child = mData.get(mInitials.get(groupPosition));
		return child != null ? child.get(childPosition) : null;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return groupPosition * 100 + childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		GroupViewHolder vh;
		if(convertView == null) {
			convertView = View.inflate(mContext, R.layout.item_student_group, null);
			vh = new GroupViewHolder();
			vh.initial = (TextView) convertView;
			convertView.setTag(vh);
		} else {
			vh = (GroupViewHolder) convertView.getTag();
		}
		String item = (String) getGroup(groupPosition);
		if(item != null) {
			vh.initial.setText(item);
		}
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ChildViewHolder vh;
		if(convertView == null) {
			convertView = View.inflate(mContext, R.layout.item_student_child, null);
			vh = new ChildViewHolder();
			vh.name = (TextView) convertView;
			convertView.setTag(vh);
		} else {
			vh = (ChildViewHolder) convertView.getTag();
		}
		Student item = (Student) getChild(groupPosition, childPosition);
		if(item != null) {
			String firstName = item.getFirstName().trim();
			String lastName = item.getLastName().trim();
			if(mHighlight != null && mHighlight.length() != 0) {
				firstName = highlightedText(firstName, mHighlight);
				lastName = highlightedText(lastName, mHighlight);
			}
			String name = firstName + " " + lastName;
			vh.name.setText(Html.fromHtml(name));
		}
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	static class GroupViewHolder {
		TextView initial;
	}
	
	static class ChildViewHolder {
		TextView name;
	}
	
	private String highlightedText(String text, String highlight) {
		int start = text.toLowerCase(Locale.US).indexOf(highlight.toLowerCase(Locale.US));
		if(start == 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(text.substring(0, start))
			.append("<font color=\"").append(HIGHLIGHT_COLOR).append("\">")
			.append(text.substring(start, start + highlight.length())).append("</font>")
			.append(text.substring(start + highlight.length()));
			return sb.toString();
		} else {
			return text;
		}
	}
	
	public long getStudentPosition(Student student) {
		String initial = student.getFirstNameInitial();
		int groupPosition = mInitials.indexOf(initial);
		if(groupPosition != -1) {
			List<Student> students = mData.get(initial);
			if(students != null) {
				int childPosition = students.indexOf(student);
				if(childPosition != -1) {
					return ExpandableListView.getPackedPositionForChild(groupPosition, childPosition);
				}
			}
		}
		return -1;
	}
}
