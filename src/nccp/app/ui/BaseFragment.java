package nccp.app.ui;

import nccp.app.utils.Logger;
import android.support.v4.app.Fragment;
import android.widget.Toast;

public class BaseFragment extends Fragment {

	public static final String TAG = BaseFragment.class.getSimpleName();
	
	public interface FragmentCallback {
		void showProgress(boolean show);
		void updateProgramSpinner(String selectedProgramName);
		int getCurrentProgramIndex();
	}
	
	public void logAndToastException(String tag, Exception e) {
		Logger.e(tag, e.getMessage(), e);
		Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
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
	};
	
	protected FragmentCallback mCallback = mDummyCallback;
}
