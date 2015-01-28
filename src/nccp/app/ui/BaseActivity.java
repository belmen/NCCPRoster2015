package nccp.app.ui;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.inputmethod.InputMethodManager;

public class BaseActivity extends FragmentActivity {

	protected void hideInputMethod() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if(imm.isActive()) {
			imm.hideSoftInputFromWindow(getWindow().getDecorView().getApplicationWindowToken(), 0);
		}
	}
}
