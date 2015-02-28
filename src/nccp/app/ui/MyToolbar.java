package nccp.app.ui;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;

/**
 * In the original Toolbar, all custom views will be visible after an action view,
 * such as a search view was collapsed, no matter they are hidden or not before the
 * action view was expanded. This creates some problems for custom views that are not
 * intended to be visible all the time (such as a progress bar).<br/>
 * This workaround adds a OnActionCollapsedListener to the Toolbar, so that you can
 * fix custom views' visibilities after the action view was collapsed.
 * @author Belmen
 *
 */
public class MyToolbar extends Toolbar {

	public interface OnActionCollapsedListener {
		void onCollapsed();
	}
	
	private OnActionCollapsedListener mOnActionCollapsedListener = null;
	
	public MyToolbar(Context context) {
		super(context);
	}

	public MyToolbar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void setOnActionCollapsedListener(
			OnActionCollapsedListener l) {
		this.mOnActionCollapsedListener = l;
	}

	@Override
	public void collapseActionView() {
		super.collapseActionView();
		if(mOnActionCollapsedListener != null) {
			mOnActionCollapsedListener.onCollapsed();
		}
	}
}
