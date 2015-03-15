package nccp.app.ui;


public interface FragmentCallback {

	void showProgress(boolean show);
	
	void updateProgramSpinner(String selectedProgramName);
	
	int getCurrentProgramIndex();
}
