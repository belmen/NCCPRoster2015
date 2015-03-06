package nccp.app.ui;

import nccp.app.R;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ProgramFragment extends Fragment {

	public static final String TAG = ProgramFragment.class.getSimpleName();

	// Views
	private TextView mEmptyView;
	// Data
	private boolean mFirst = true; 
	private FragmentCallback mCallback = null;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (FragmentCallback) activity;
		} catch (ClassCastException e) {
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.program_fragment, container, false);
		mEmptyView = (TextView) v.findViewById(R.id.program_empty_text);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.program_fragment, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == R.id.action_add_program) {
			handleAddProgram();
			return true;
		} else if(id == R.id.action_delete_program) {
			handleDeleteProgram();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void handleAddProgram() {
		
	}
	
	private void handleDeleteProgram() {
		
	}
	
	private void fetchPrograms() {
		
	}
}
