package nccp.app.ui;

import nccp.app.R;
import nccp.app.utils.Logger;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class BannerActivity extends BaseActivity {

	public static final String TAG = "BannerActivity";
	
	private ImageButton mBtnBack;
	private TextView mTvTitle;
	private ProgressBar mProgressBar;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
//		Logger.i(TAG, "mTvTitle is " + (mTvTitle == null ? "null" : "not null"));
//		if(mTvTitle != null) {
//			mTvTitle.startAnimation(mAnimTitleIn);
//		}
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
		LinearLayout layout = (LinearLayout) View.inflate(this, R.layout.banner_activity, null);
		initViews(layout);
		if(view != null) {
			layout.addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}
		super.setContentView(layout, params);
	}
	
	public void setBannerTitle(int resId) {
		if(mTvTitle != null) {
			mTvTitle.setText(resId);
		}
	}
	
	public void setBannerTitle(String title) {
		if(mTvTitle != null) {
			mTvTitle.setText(title);
		}
	}
	
	public void showBackButton(boolean show) {
		if(mBtnBack != null) {
			if(show) {
				mBtnBack.setVisibility(View.VISIBLE);
			} else {
				mBtnBack.setVisibility(View.INVISIBLE);
			}
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
	
	private void initViews(View layout) {
		mBtnBack = (ImageButton) layout.findViewById(R.id.banner_back_btn);
		mBtnBack.setOnClickListener(onBackClick);
		mTvTitle = (TextView) layout.findViewById(R.id.banner_title_tv);
		mProgressBar = (ProgressBar) layout.findViewById(R.id.banner_progress);
	}
	
	private OnClickListener onBackClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			onBackPressed();
			finish();
		}
	};
}
