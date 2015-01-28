package nccp.app.adapter;

import java.util.List;

import nccp.app.R;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Adapter for choosing school database
 * @author Belmen
 *
 */
public class DatabaseAdapter extends BaseAdapter {

	private Context mContext;
	private List<String> mData = null;
	
	public DatabaseAdapter(Context context) {
		this.mContext = context;
	}

	public void setData(List<String> data) {
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
			convertView = View.inflate(mContext, R.layout.database_item, null);
			vh = new ViewHolder();
			vh.name = (TextView) convertView.findViewById(R.id.database_item_text);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		String item = (String) getItem(position);
		if(item != null) {
			vh.name.setText(item);
		}
		return convertView;
	}

	static class ViewHolder {
		TextView name;
	}
}
