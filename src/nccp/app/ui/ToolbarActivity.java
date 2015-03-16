package nccp.app.ui;

import nccp.app.R;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class ToolbarActivity extends BaseActivity {

	public static final String TAG = ToolbarActivity.class.getSimpleName();

	private RelativeLayout mContentView;
	private ProgressBar mProgressBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContentView = (RelativeLayout) View.inflate(this, R.layout.activity_toolbar, null);
		super.setContentView(mContentView);
		initViews();
	}
	
	private void initViews() {
		mProgressBar = (ProgressBar) findViewById(R.id.toolbar_activity_progress);
		Toolbar tb = (Toolbar) findViewById(R.id.activity_toolbar);
		setSupportActionBar(tb);
	}

	@Override
	public void setContentView(int layoutResID) {
		setContentView(View.inflate(this, layoutResID, null));
	}

	@Override
	public void setContentView(View view) {
		setContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	@Override
	public void setContentView(View view, LayoutParams params) {
		if(view != null) {
			RelativeLayout.LayoutParams lp =
				new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			lp.addRule(RelativeLayout.BELOW, R.id.activity_toolbar);
			mContentView.addView(view, lp);
		}
	}
	
	public void showProgressBar(boolean show) {
		if(mProgressBar != null) {
			if(show) {
				mProgressBar.setVisibility(View.VISIBLE);
			} else {
				mProgressBar.setVisibility(View.INVISIBLE);
			}
		}
	}
}
