package nccp.app.ui;

import java.util.List;

import nccp.app.R;
import nccp.app.data.DataCenter;
import nccp.app.parse.object.Program;
import nccp.app.utils.Logger;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;

public class ProgramFragment extends Fragment {

	public static final String TAG = ProgramFragment.class.getSimpleName();

	// Views
	private TextView mEmptyView;
	// Data
	private boolean mFirst = true; 
	private FragmentCallback mCallback = null;
	private Program mCurrentProgram = null;
	
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
		updateViews();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.program_fragment, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		MenuItem deleteMenu = menu.getItem(2);
		List<Program> programs = DataCenter.getPrograms();
		if(programs == null || programs.size() == 0) {
			deleteMenu.setEnabled(false);
		} else {
			deleteMenu.setEnabled(true);
		}
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
	
	public void setProgram(Program program) {
		if((mCurrentProgram == null && program == null)
		|| (mCurrentProgram != null && program != null && mCurrentProgram.equals(program))) {
			return;
		}
		mCurrentProgram = program;
		if(getView() != null) {
			updateViews();
		}
	}
	
	private void updateViews() {
		if(mCurrentProgram == null) {
			mEmptyView.setVisibility(View.VISIBLE);
			return;
		}
		mEmptyView.setVisibility(View.INVISIBLE);
	}
	
	private void handleAddProgram() {
		View view = View.inflate(getActivity(), R.layout.dialog_add_program, null);
		final EditText etProgramName = (EditText) view.findViewById(R.id.dialog_add_program_et);
		new AlertDialog.Builder(getActivity())
		.setTitle(R.string.dialog_title_add_program)
		.setView(view)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String programName = etProgramName.getText().toString();
				doAddProgram(programName);
			}
		}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		}).show();
	}
	
	private void handleDeleteProgram() {
		if(mCurrentProgram == null) {
			return;
		}
		new AlertDialog.Builder(getActivity())
		.setTitle(R.string.dialog_title_delete_program)
		.setMessage(getString(R.string.dialog_msg_delete_program, mCurrentProgram.getProgramName()))
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				doDeleteProgram(mCurrentProgram);
			}
		}).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		}).show();
	}
	
	private void doAddProgram(String programName) {
		if(programName == null || programName.length() == 0) {
			Toast.makeText(getActivity(), R.string.dialog_error_program_name_empty, Toast.LENGTH_SHORT)
			.show();
			return;
		}
		Program program = new Program();
		program.setProgramName(programName);
		new SaveProgramTask(program).execute();
	}
	
	private void handleProgramChanged(final Program newProgram) {
		DataCenter.fetchPrograms(new DataCenter.Callback() {
			@Override
			public void onFetched(ParseException e) {
				// Update menu
				ActivityCompat.invalidateOptionsMenu(getActivity());
				// Update tab and spinner
				if(mCallback != null) {
					String pname = null;
					if(newProgram != null) {
						pname = newProgram.getProgramName();
					}
					mCallback.updateProgramSpinner(pname);
				}
			}
		});
	}
	
	private void doDeleteProgram(Program program) {
		if(program != null) {
			new DeleteProgramTask(program).execute();
		}
	}
	
	class SaveProgramTask extends AsyncTask<Void, Void, Void> {

		private Program program;
		private ParseException e;
		
		public SaveProgramTask(Program program) {
			this.program = program;
		}

		@Override
		protected void onPreExecute() {
			if(mCallback != null) {
				mCallback.showProgress(true);
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				program.save();
			} catch (ParseException e) {
				this.e = e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(mCallback != null) {
				mCallback.showProgress(false);
			}
			if(e == null) { // Success
				handleProgramChanged(program);
			} else {
				Logger.e(TAG, e.getMessage(), e);
				Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
		
	}
	
	class DeleteProgramTask extends AsyncTask<Void, Void, Void> {
		
		private Program program;
		private ParseException e;

		public DeleteProgramTask(Program program) {
			this.program = program;
		}

		@Override
		protected void onPreExecute() {
			if(mCallback != null) {
				mCallback.showProgress(true);
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				program.delete();
			} catch (ParseException e) {
				this.e = e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if(mCallback != null) {
				mCallback.showProgress(false);
			}
			if(e == null) { // Success
				handleProgramChanged(null);
			} else {
				Logger.e(TAG, e.getMessage(), e);
				Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
	}
}
