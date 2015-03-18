package nccp.app.ui;

import nccp.app.parse.ParseManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.parse.ParseUser;

/**
 * Base activity for those after login
 * @author Belmen
 *
 */
public class BaseActivity extends ActionBarActivity {

	private boolean mCheckLogin = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(mCheckLogin &&
		   (!ParseManager.isDatabaseSet() || ParseUser.getCurrentUser() == null)) { // Not logged in
			// Go back to log in screen
			Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Finish all previous activities
			startActivity(intent);
		}
	}

	public void setCheckLogin(boolean checkLogin) {
		this.mCheckLogin = checkLogin;
	}
}
