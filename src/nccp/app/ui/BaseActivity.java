package nccp.app.ui;

import nccp.app.parse.ParseManager;

import com.parse.ParseUser;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.inputmethod.InputMethodManager;

public class BaseActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(!ParseManager.isDatabaseSet() || ParseUser.getCurrentUser() == null) { // Not logged in
			// Go back to log in screen
			Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
			startActivity(intent);
			finish();
		}
	}

	protected void hideInputMethod() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if(imm.isActive()) {
			imm.hideSoftInputFromWindow(getWindow().getDecorView().getApplicationWindowToken(), 0);
		}
	}
}
