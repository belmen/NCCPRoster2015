package nccp.app.ui;

import java.util.List;

import nccp.app.R;
import nccp.app.data.DataCenter;
import nccp.app.parse.ParseManager;
import nccp.app.parse.object.Program;
import nccp.app.ui.BaseFragment.FragmentCallback;
import nccp.app.ui.MyToolbar.OnActionCollapsedListener;
import nccp.app.utils.Const;
import nccp.app.utils.Logger;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.parse.ParseUser;

public class MainActivity extends BaseActivity implements FragmentCallback {

	public static final String TAG = MainActivity.class.getSimpleName();

	private static final String TAB_PROGRAM = Const.PACKAGE_NAME + ".tab_program";
	private static final String TAB_ATTENDANCE = Const.PACKAGE_NAME + ".tab_attendance";
	private static final String TAB_STUDENT = Const.PACKAGE_NAME + ".tab_students";
	private static final String[] TAB_IDS = {TAB_PROGRAM, TAB_ATTENDANCE, TAB_STUDENT};

	// Views
	private ProgressBar mProgress;
	private Spinner mSpProgram;
	private ViewPager mTabPager;
	private ViewPagerTabs mViewPagerTabs;
	private TabPagerAdapter mTabPagerAdapter;
	private TabPagerListener mTabPagerListener = new TabPagerListener();
	// Fragments
	private ProgramFragment mProgramFragment;
	private AttendanceFragment mAttendanceFragment;
	private StudentsFragment mStudentsFragment;
	
//	private FragmentHelper mFmHelper;
//	private FragmentTabHost mTabHost;
	
	// Data
	private String[] mTabTitles;
	private String mCurrentTab = null;
	private ArrayAdapter<String> mProgramAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initToolbar();
		initViews();
		
		mCurrentTab = TAB_PROGRAM;
		prepareToolbarForTab(mCurrentTab);
		updateProgramSpinner(null);
	}

	private void initToolbar() {
		MyToolbar tb = (MyToolbar) findViewById(R.id.main_toolbar);
		tb.setOnActionCollapsedListener(mOnActionCollapsedListener);
		mSpProgram = (Spinner) tb.findViewById(R.id.main_program_spinner);
		mProgramAdapter = new ArrayAdapter<String>(MainActivity.this,
				R.layout.item_program_spinner);
		mProgramAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpProgram.setAdapter(mProgramAdapter);
		mSpProgram.setOnItemSelectedListener(onProgramSelectedListener);
		setSupportActionBar(tb);
	}
	
	private void initViews() {
		mProgress = (ProgressBar) findViewById(R.id.main_progress);
//		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
//		mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
//		mTabHost.addTab(mTabHost.newTabSpec(TAB_PROGRAM).setIndicator(getString(R.string.tab_title_program)),
//				ProgramFragment.class, null);
//		mTabHost.addTab(mTabHost.newTabSpec(TAB_ATTENDANCE).setIndicator(getString(R.string.tab_title_attendence)),
//				AttendanceFragment.class, null);
//		mTabHost.addTab(mTabHost.newTabSpec(TAB_STUDENT).setIndicator(getString(R.string.tab_title_students)),
//				StudentsFragment.class, null);
//		mTabHost.setOnTabChangedListener(mTabChangeListener);
//		mCurrentTab = mTabHost.getCurrentTabTag();
		
		mTabTitles = new String[3];
		mTabTitles[0] = getString(R.string.tab_title_program);
		mTabTitles[1] = getString(R.string.tab_title_attendence);
		mTabTitles[2] = getString(R.string.tab_title_students);
		
		mTabPager = (ViewPager) findViewById(R.id.main_tab_pager);
		mViewPagerTabs = (ViewPagerTabs) findViewById(R.id.main_pager_tabs);
		mTabPagerAdapter = new TabPagerAdapter();
		mTabPager.setAdapter(mTabPagerAdapter);
		mTabPager.setOnPageChangeListener(mTabPagerListener);
		mViewPagerTabs.setViewPager(mTabPager);
		
		final FragmentManager fm = getSupportFragmentManager();
		// Hide all tabs (the current tab will later be reshown once a tab is selected)
		final FragmentTransaction transaction = fm.beginTransaction();
		
		mProgramFragment = (ProgramFragment) fm.findFragmentByTag(TAB_PROGRAM);
		mAttendanceFragment = (AttendanceFragment) fm.findFragmentByTag(TAB_ATTENDANCE);
		mStudentsFragment = (StudentsFragment) fm.findFragmentByTag(TAB_STUDENT);
		if(mProgramFragment == null) {
			mProgramFragment = new ProgramFragment();
			mAttendanceFragment = new AttendanceFragment();
			mStudentsFragment = new StudentsFragment();
			
			transaction.add(R.id.main_tab_pager, mProgramFragment, TAB_PROGRAM);
			transaction.add(R.id.main_tab_pager, mAttendanceFragment, TAB_ATTENDANCE);
			transaction.add(R.id.main_tab_pager, mStudentsFragment, TAB_STUDENT);
		}
		
		transaction.hide(mProgramFragment);
		transaction.hide(mAttendanceFragment);
		transaction.hide(mStudentsFragment);
		
		transaction.commitAllowingStateLoss();
		fm.executePendingTransactions();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main_activity_actions, menu);
		return true;
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
//		Logger.i(TAG, "Show progress bar: " + String.valueOf(show));
		if(show) {
			mProgress.setVisibility(View.VISIBLE);
		} else {
			mProgress.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void updateProgramSpinner(String selectedProgramName) {
		List<Program> programs = DataCenter.getPrograms();
		// Update tab
		prepareToolbarForTab(mCurrentTab);
		
		// Update spinner
		mProgramAdapter.clear();
		for(Program program : programs) {
			mProgramAdapter.add(program.getProgramName());
		}
		mProgramAdapter.notifyDataSetChanged();
		
		// If spinner's current selected index == newindex,
		// OnItemSelectedListener would not be fired, then we need force update for program
		boolean forceUpdate = false; 
		// Select program
		if(selectedProgramName != null) {
			int index = -1;
			for(int i = 0; i < programs.size(); ++i) {
				if(selectedProgramName.equals(programs.get(i).getProgramName())) {
					index = i;
					break;
				}
			}
			if(index != -1) {
				forceUpdate = mSpProgram.getSelectedItemPosition() == index;
				mSpProgram.setSelection(index);
				if(forceUpdate) {
					onProgramChanged(programs.get(index));
				}
			}
		} else if(!programs.isEmpty()) { // Select first program
			forceUpdate = mSpProgram.getSelectedItemPosition() == 0;
			mSpProgram.setSelection(0);
			if(forceUpdate) {
				onProgramChanged(programs.get(0));
			}
		} else { // No programs
			onProgramChanged(null);
		}
	}

	@Override
	public int getCurrentProgramIndex() {
		return mSpProgram.getSelectedItemPosition();
	}

	@Override
	public Program getCurrentProgram() {
		Program currentProgram = null;
		int index = getCurrentProgramIndex();
		List<Program> programs = DataCenter.getPrograms();
		if(programs != null && index >= 0 && index < programs.size()) {
			currentProgram = programs.get(index);
		}
		return currentProgram;
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
		if(TAB_PROGRAM.equals(tabId) || TAB_ATTENDANCE.equals(tabId)) { // Program or attendance tab
			List<Program> programs = DataCenter.getPrograms();
			if(programs != null && programs.size() > 0) {
				ab.setDisplayShowTitleEnabled(false);
				mSpProgram.setVisibility(View.VISIBLE);
			} else { // No programs
				ab.setDisplayShowTitleEnabled(true);
				String title;
				if(TAB_PROGRAM.equals(tabId)) {
					title = getString(R.string.title_programs);
				} else {
					title = getString(R.string.title_attendance);
				}
				ab.setTitle(title);
				mSpProgram.setVisibility(View.GONE);
			}
		} else if(TAB_STUDENT.equals(tabId)) { // Students tab
			ab.setTitle(getString(R.string.title_students));
			ab.setDisplayShowTitleEnabled(true);
			mSpProgram.setVisibility(View.GONE);
		}
	}
	
	private void onProgramChanged(Program program) {
		// Notify two fragments
//		ProgramFragment programFragment = (ProgramFragment) getSupportFragmentManager()
//				.findFragmentByTag(TAB_PROGRAM);
//		if(programFragment != null) {
//			programFragment.setProgram(program);
//		}
//		AttendanceFragment attendanceFragment = (AttendanceFragment) getSupportFragmentManager()
//				.findFragmentByTag(TAB_ATTENDANCE);
//		if(attendanceFragment != null) {
//			attendanceFragment.setProgram(program);
//		}
		mProgramFragment.setProgram(program);
		mAttendanceFragment.setProgram(program);
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
	
//	private OnTabChangeListener mTabChangeListener = new OnTabChangeListener() {
//		
//		@Override
//		public void onTabChanged(String tabId) {
//			mCurrentTab = tabId;
//			prepareToolbarForTab(mCurrentTab);
//		}
//	};
	
	private OnItemSelectedListener onProgramSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			List<Program> programs = DataCenter.getPrograms();
			Program program = programs.get(position);
			onProgramChanged(program);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			
		}
	};
	
	private class TabPagerAdapter extends PagerAdapter {
		
		private final FragmentManager mFragmentManager;
        private FragmentTransaction mCurTransaction = null;

		public TabPagerAdapter() {
			mFragmentManager = getSupportFragmentManager();
		}

		@Override
		public int getCount() {
			return TAB_IDS.length;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return ((Fragment) object).getView() == view;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			if(mCurTransaction == null) {
				mCurTransaction = mFragmentManager.beginTransaction();
			}
			Fragment f = getFragment(position);
			mCurTransaction.show(f);
			
			return f;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			if(mCurTransaction == null) {
				mCurTransaction = mFragmentManager.beginTransaction();
			}
			mCurTransaction.hide((Fragment) object);
		}

		@Override
		public void startUpdate(ViewGroup container) {
		}

		@Override
		public void finishUpdate(ViewGroup container) {
			if (mCurTransaction != null) {
                mCurTransaction.commitAllowingStateLoss();
                mCurTransaction = null;
                mFragmentManager.executePendingTransactions();
            }
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mTabTitles[position];
		}

		public Fragment getFragment(int position) {
			if(position == 0) {
				return mProgramFragment;
			} else if(position == 1) {
				return mAttendanceFragment;
			} else if(position == 2) {
				return mStudentsFragment;
			} else {
				throw new IllegalArgumentException("position: " + position);
			}
		}
	}
	
	private class TabPagerListener implements ViewPager.OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int state) {
            mViewPagerTabs.onPageScrollStateChanged(state);
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			mViewPagerTabs.onPageScrolled(position, positionOffset, positionOffsetPixels);
		}

		@Override
		public void onPageSelected(int position) {
			mCurrentTab = TAB_IDS[position];
			prepareToolbarForTab(mCurrentTab);
            mViewPagerTabs.onPageSelected(position);
            invalidateFragmentMenus(position);
            
            // We may have changed the program, tell attendance fragment to refresh
            if(mCurrentTab.equals(TAB_ATTENDANCE)) {
            	mAttendanceFragment.refresh();
            }
		}
		
		/*
		 * http://stackoverflow.com/questions/25965750/optionsmenu-of-fragments-in-viewpager-showing-each-others-buttons
		 */
		private void invalidateFragmentMenus(int position) {
			for (int i = 0; i < mTabPagerAdapter.getCount(); i++) {
				mTabPagerAdapter.getFragment(i).setHasOptionsMenu(i == position);
			}
			invalidateOptionsMenu();
		}
	}
}
