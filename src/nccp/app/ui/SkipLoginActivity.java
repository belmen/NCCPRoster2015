package nccp.app.ui;

import android.os.Bundle;

public class SkipLoginActivity extends LoginActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		doLogin("belmen", "belmen");
	}

}
