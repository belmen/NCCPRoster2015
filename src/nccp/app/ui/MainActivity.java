package nccp.app.ui;

import nccp.app.R;
import nccp.app.data.DataCenter;
import nccp.app.parse.ParseManager;
import nccp.app.ui.MyToolbar.OnActionCollapsedListener;
import nccp.app.utils.Const;
import nccp.app.utils.Logger;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.parse.ParseUser;

public class MainActivity extends BaseActivity implements FragmentCallback {

	public static final String TAG = MainActivity.class.getSimpleName();

	private static final String TAB_PROGRAM = Const.PACKAGE_NAME + ".tab_program";
	private static final String TAB_STUDENT = Const.PACKAGE_NAME + ".tab_students";
	private static final String TAB_ATTENDANCE = Const.PACKAGE_NAME + ".tab_attendance";

	// Views
	private ProgressBar mProgress;
	private Spinner mSpProgram;
	
//	private FragmentHelper mFmHelper;
	private FragmentTabHost mTabHost;
	
	private String mCurrentTab = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		initToolbar();
		initViews();
		
		prepareToolbarForTab(mCurrentTab);
	}

	private void initToolbar() {
		MyToolbar tb = (MyToolbar) findViewById(R.id.main_toolbar);
		tb.setOnActionCollapsedListener(mOnActionCollapsedListener);
		mSpProgram = (Spinner) tb.findViewById(R.id.main_program_spinner);
		setSupportActionBar(tb);
	}
	
	private void initViews() {
		mProgress = (ProgressBar) findViewById(R.id.main_progress);
		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
		mTabHost.addTab(mTabHost.newTabSpec(TAB_PROGRAM).setIndicator(getString(R.string.tab_title_program)),
				ProgramFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec(TAB_STUDENT).setIndicator(getString(R.string.tab_title_students)),
				StudentsFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec(TAB_ATTENDANCE).setIndicator(getString(R.string.tab_title_attendence)),
				DummyTabFragment.class, null);
		mTabHost.setOnTabChangedListener(mTabChangeListener);
		mCurrentTab = mTabHost.getCurrentTabTag();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_logout) {
			handleLogout();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		handleLogout();
	}
	
	/**
	 * Show the progress bar on top banner
	 * @param show
	 */
	@Override
	public void showProgress(boolean show) {
		Logger.i(TAG, "Show progress bar: " + String.valueOf(show));
		if(show) {
			mProgress.setVisibility(View.VISIBLE);
		} else {
			mProgress.setVisibility(View.INVISIBLE);
		}
	}

	private void handleLogout() {
		new AlertDialog.Builder(MainActivity.this)
				.setTitle(R.string.dialog_title_logout)
				.setMessage(R.string.dialog_msg_logout)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								doLogout();
							}
						})
				.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								return;
							}
						}).show();
	}

	private void doLogout() {
		if(ParseManager.isDatabaseSet() && ParseUser.getCurrentUser() != null) {
			ParseUser.logOut(); // Log out parse user
		}
		DataCenter.clearData(); // Clear fetched data
		Intent intent = new Intent(MainActivity.this, LoginActivity.class);
		startActivity(intent);
		finish();
	}
	
	private void prepareToolbarForTab(String tabId) {
		ActionBar ab = getSupportActionBar();
		if(TAB_PROGRAM.equals(tabId)) { // Program tab
			ab.setDisplayShowTitleEnabled(false);
			mSpProgram.setVisibility(View.VISIBLE);
		} else if(TAB_STUDENT.equals(tabId)) { // Students tab
			ab.setTitle(getString(R.string.title_students));
			ab.setDisplayShowTitleEnabled(true);
			mSpProgram.setVisibility(View.GONE);
		} else if(TAB_ATTENDANCE.equals(tabId)) { // Attendance tab
			ab.setDisplayShowTitleEnabled(false);
			mSpProgram.setVisibility(View.VISIBLE);
		}
	}
	
	public static class DummyTabFragment extends Fragment {
		@Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                             Bundle savedInstanceState) {
	        View v = inflater.inflate(R.layout.dummy_fragment, container, false);
	        TextView tv = (TextView) v.findViewById(R.id.text);
	        tv.setText(this.getTag() + " Content");
	        return v;
	    }
	}
	
	private OnActionCollapsedListener mOnActionCollapsedListener =
			new OnActionCollapsedListener() {
		@Override
		public void onCollapsed() {
			prepareToolbarForTab(mCurrentTab);
		}
	};
	
	private OnTabChangeListener mTabChangeListener = new OnTabChangeListener() {
		
		@Override
		public void onTabChanged(String tabId) {
			mCurrentTab = tabId;
			prepareToolbarForTab(mCurrentTab);
		}
	};
}
