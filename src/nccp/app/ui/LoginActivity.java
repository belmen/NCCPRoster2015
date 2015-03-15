package nccp.app.ui;

import nccp.app.R;
import nccp.app.data.DataCenter;
import nccp.app.parse.ParseManager;
import nccp.app.utils.Const;
import nccp.app.utils.Logger;
import nccp.app.utils.PreferenceUtil;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends Activity {

	public static final String TAG = LoginActivity.class.getSimpleName();
	
	private static final int REQUEST_CHOOSE_DB = 0;
	private static final int REQUEST_REGISTER = 1;
	
	private Button mBtnDatabase;
	private EditText mEtUsername;
	private EditText mEtPassword;
	private Button mBtnLogin;
	private ProgressBar mProgressBar;
	private TextView mTvError;
	
	private String mDatabase = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		initViews();
		initPreference();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_CHOOSE_DB) { // Back from choose database
			if(resultCode == RESULT_OK && data != null) {
				// Clear inputs
				mEtUsername.setText("");
				mEtPassword.setText("");
				mEtUsername.requestFocus();
				String database = data.getStringExtra(Const.EXTRA_DATABASE);
				setDatabase(database);
			}
		} else if(requestCode == REQUEST_REGISTER) {
			if(resultCode == RESULT_OK) {
				loginSuccess();
			}
		}
	}

	// Event for choose database button
	public void onDatabaseClick(View v) {
		Intent intent = new Intent(LoginActivity.this, ChooseDBActivity.class);
		startActivityForResult(intent, REQUEST_CHOOSE_DB);
	}

	// Event for login button
	public void onLoginClick(View v) {
		// Validate fields
		if(!validateUsername()) return;
		if(!validatePassword()) return;
		
		String username = mEtUsername.getText().toString();
		String password = mEtPassword.getText().toString();
		
		doLogin(username, password);
	}
	
	// Event for register button
	public void onRegisterClick(View v) {
		Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
		startActivityForResult(intent, REQUEST_REGISTER);
	}

	private void initViews() {
		mBtnDatabase = (Button) findViewById(R.id.login_database_btn);
		mEtUsername = (EditText) findViewById(R.id.login_username_edittext);
		mEtPassword = (EditText) findViewById(R.id.login_password_edittext);
		mBtnLogin = (Button) findViewById(R.id.login_login_btn);
		mProgressBar = (ProgressBar) findViewById(R.id.login_progress);
		mTvError = (TextView) findViewById(R.id.login_error_text);
		
		setLoginFieldsEnabled(false);
	}
	
	// Pre-fill input field from preference
	private void initPreference() {
		String lastDataBase = PreferenceUtil.getString(LoginActivity.this, Const.PREF_LAST_DATABASE);
		if(lastDataBase != null) {
			setDatabase(lastDataBase);
		}
		String lastUsername = PreferenceUtil.getString(LoginActivity.this, Const.PREF_LAST_USERNAME);
		if(lastUsername != null) {
			mEtUsername.setText(lastUsername);
			mEtPassword.requestFocus();
		}
	}

	private void setLoginFieldsEnabled(boolean enable) {
		mEtUsername.setEnabled(enable);
		mEtPassword.setEnabled(enable);
		mBtnLogin.setEnabled(enable);
	}
	
	private void setDatabase(String database) {
		if(database == null) {
			return;
		}
		mDatabase = database;
		// Set parse database
		ParseManager.setDatabase(database);
		mBtnDatabase.setText(database);
		// Enable fields
		setLoginFieldsEnabled(true);
	}
	
	private boolean validateUsername() {
		boolean valid = false;
		String username = mEtUsername.getText().toString();
		if(username.length() == 0) {
			mEtUsername.setError(getString(R.string.login_username_empty));
		} else {
			mEtUsername.setError(null);
			valid = true;
		}
		return valid;
	}
	
	private boolean validatePassword() {
		boolean valid = false;
		String password = mEtPassword.getText().toString();
		if(password.length() == 0) {
			mEtPassword.setError(getString(R.string.login_password_empty));
		} else {
			mEtPassword.setError(null);
			valid = true;
		}
		return valid;
	}

	protected void hideInputMethod() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if(imm.isActive()) {
			imm.hideSoftInputFromWindow(getWindow().getDecorView().getApplicationWindowToken(), 0);
		}
	}
	
	protected void doLogin(final String username, final String password) {
		if(!ParseManager.isDatabaseSet()) {
			return;
		}
		
		hideInputMethod();
		mBtnDatabase.setEnabled(false);
		mBtnLogin.setEnabled(false);
		mProgressBar.setVisibility(View.VISIBLE);
		mTvError.setText(null);

		ParseUser.logInInBackground(username, password, new LogInCallback() {
			@Override
			public void done(ParseUser user, ParseException e) {
//				mBtnDatabase.setEnabled(true);
//				mBtnLogin.setEnabled(true);
//				mProgressBar.setVisibility(View.GONE);
				
				if(e == null) { // Ok
					storeLoginInputs(mDatabase, username);
					loginSuccess();
				} else { // Exception
					String errorMsg = e.getMessage();
					mTvError.setText(errorMsg);
					Logger.e(TAG, errorMsg);
				}
			}
		});
	}
	
	private void storeLoginInputs(String database, String username) {
		if(database != null) {
			PreferenceUtil.putString(LoginActivity.this, Const.PREF_LAST_DATABASE, database);
		}
		if(username != null) {
			PreferenceUtil.putString(LoginActivity.this, Const.PREF_LAST_USERNAME, username);
		}
	}
	
	private void loginSuccess() {
		DataCenter.fetchData(new DataCenter.Callback() {
			@Override
			public void onFetched(ParseException e) {
				mBtnDatabase.setEnabled(true);
				mBtnLogin.setEnabled(true);
				mProgressBar.setVisibility(View.GONE);
				
				if(e == null) {
					// Jump into main activity
					Intent intent = new Intent(LoginActivity.this, MainActivity.class);
					startActivity(intent);
					finish();
				} else {
					Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});
	}
}
