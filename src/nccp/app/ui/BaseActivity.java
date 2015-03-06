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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState != null && // Brought back from saved instance
		   !ParseManager.isDatabaseSet() || ParseUser.getCurrentUser() == null) { // Not logged in
			// Go back to log in screen
			Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
			startActivity(intent);
			finish();
		}
	}
}
