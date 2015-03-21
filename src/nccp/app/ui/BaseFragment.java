package nccp.app.ui;

import nccp.app.parse.object.Program;
import nccp.app.utils.Logger;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.widget.Toast;

public class BaseFragment extends Fragment {

	public static final String TAG = BaseFragment.class.getSimpleName();
	
	public interface FragmentCallback {
		void showProgress(boolean show);
		void updateProgramSpinner(String selectedProgramName);
		int getCurrentProgramIndex();
		Program getCurrentProgram();
	}
	
	public interface ProgramChangedListener {
		void onProgramChanged();
	}
	
	private FragmentCallback mDummyCallback = new FragmentCallback() {
		
		@Override
		public void updateProgramSpinner(String selectedProgramName) {
		}
		
		@Override
		public void showProgress(boolean show) {
		}
		
		@Override
		public int getCurrentProgramIndex() {
			return -1;
		}

		@Override
		public Program getCurrentProgram() {
			return null;
		}
	};
	
	protected FragmentCallback mCallback = mDummyCallback;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		FragmentCallback callback = null;
		try {
			callback = (FragmentCallback) activity;
		} catch (ClassCastException e) {
		}
		setFragmentCallback(callback);
	}
	
	public void setFragmentCallback(FragmentCallback callback) {
		if(callback == null) {
			mCallback = mDummyCallback;
		} else {
			mCallback = callback;
		}
	}
	
	public void logAndToastException(String tag, Exception e) {
		Logger.e(tag, e.getMessage(), e);
		Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
	}
}
