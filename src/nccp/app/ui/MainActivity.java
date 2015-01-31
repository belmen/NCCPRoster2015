package nccp.app.ui;

import nccp.app.R;
import nccp.app.utils.Const;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	public static final String TAG = MainActivity.class.getSimpleName();

	private static final String TAB_PROGRAM = Const.PACKAGE_NAME + ".tab_program";
	private static final String TAB_STUDENT = Const.PACKAGE_NAME + ".tab_students";
	private static final String TAB_ATTENDENCE = Const.PACKAGE_NAME + ".tab_attendence";
	
	private FragmentHelper mFmHelper;
	private FragmentTabHost mTabHost;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		initViews();
	}

	private void initViews() {
		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

		mTabHost.addTab(
				mTabHost.newTabSpec(TAB_PROGRAM).setIndicator(getString(R.string.tab_title_program)),
				DummyTabFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec(TAB_STUDENT).setIndicator(getString(R.string.tab_title_students)),
				StudentsFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec(TAB_ATTENDENCE).setIndicator(getString(R.string.tab_title_attendence)),
				DummyTabFragment.class, null);
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
		// ParseUser.logOut();
		Intent intent = new Intent(MainActivity.this, LoginActivity.class);
		startActivity(intent);
		finish();
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
}
