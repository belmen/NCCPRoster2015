package nccp.app.ui;

import java.util.List;

import nccp.app.R;
import nccp.app.data.DataCenter;
import nccp.app.parse.ParseManager;
import nccp.app.parse.object.Program;
import nccp.app.ui.MyToolbar.OnActionCollapsedListener;
import nccp.app.utils.Const;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
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
	
	// Data
	private String mCurrentTab = null;
	private ArrayAdapter<String> mProgramAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initToolbar();
		initViews();
		
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
				ab.setTitle(getString(R.string.title_programs));
				ab.setDisplayShowTitleEnabled(true);
				mSpProgram.setVisibility(View.GONE);
			}
		} else if(TAB_STUDENT.equals(tabId)) { // Students tab
			ab.setTitle(getString(R.string.title_students));
			ab.setDisplayShowTitleEnabled(true);
			mSpProgram.setVisibility(View.GONE);
		}
	}
	
	private void onProgramChanged(Program program) {
		if(TAB_PROGRAM.equals(mCurrentTab)) {
			ProgramFragment f = (ProgramFragment) getSupportFragmentManager()
					.findFragmentByTag(TAB_PROGRAM);
			if(f != null) {
				f.setProgram(program);
			}
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
}
