package nccp.app.ui;

import java.util.Locale;

import nccp.app.R;
import nccp.app.parse.ParseAppManager;
import nccp.app.parse.ParseManager;
import nccp.app.utils.Logger;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class RegisterActivity extends ActionBarActivity {

	public static final String TAG = RegisterActivity.class.getSimpleName();

	private Button mBtnCreate;
	private EditText mEtUsername;
	private EditText mEtPassword;
	private EditText mEtPasswordCfm;
	private EditText mEtCreationCode;
	private TextView mTvError;
	private ProgressBar mProgressBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.register_activity);
		getSupportActionBar().setTitle(R.string.title_register_account);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//		setBannerTitle(R.string.title_register_account);
		initViews();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		hideInputMethod();
	}
	
	// On create button click
	public void onCreateClick(View v) {
//		setSupportProgressBarIndeterminateVisibility(true);
		
		// Validate input
		if(!validateUsername()) return;
		if(!validatePassword()) return;
		if(!validatePasswordCfm()) return;
		if(!validateCreationCode()) return;
		// Verify creation code
		String creationCode = mEtCreationCode.getText().toString().trim().toUpperCase(Locale.US);
		String database = ParseAppManager.getAppFromCreationCode(creationCode);
		if(database == null) {
			mEtCreationCode.setError(getString(R.string.register_incorrect_creation_code));
			return;
		}
		
		String username = mEtUsername.getText().toString();
		String password = mEtPassword.getText().toString();
		
		doRegister(database, username, password);
	}

	private void initViews() {
		mBtnCreate = (Button) findViewById(R.id.register_create_btn);
		mEtUsername = (EditText) findViewById(R.id.register_username_et);
		mEtPassword = (EditText) findViewById(R.id.register_password_et);
		mEtPasswordCfm = (EditText) findViewById(R.id.register_passwordcfm_et);
		mEtCreationCode = (EditText) findViewById(R.id.register_creationcode_et);
		mTvError = (TextView) findViewById(R.id.register_error_text);
		mProgressBar = (ProgressBar) findViewById(R.id.register_progress);
		
		mEtUsername.setOnFocusChangeListener(onEtFocusChangedListener);
		mEtPassword.setOnFocusChangeListener(onEtFocusChangedListener);
		mEtPasswordCfm.setOnFocusChangeListener(onEtFocusChangedListener);
		mEtCreationCode.setOnFocusChangeListener(onEtFocusChangedListener);
	}
	
	private boolean validateUsername() {
		boolean valid = false;
		String username = mEtUsername.getText().toString();
		if(username.length() == 0) {
			mEtUsername.setError(getString(R.string.register_error_username));
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
			mEtPassword.setError(getString(R.string.register_error_password));
		} else {
			mEtPassword.setError(null);
			valid = true;
		}
		return valid;
	}
	
	private boolean validatePasswordCfm() {
		boolean valid = false;
		String password = mEtPassword.getText().toString();
		String passwordCfm = mEtPasswordCfm.getText().toString();
		if(!password.equals(passwordCfm)) {
			mEtPasswordCfm.setError(getString(R.string.register_error_password_cfm));
		} else {
			mEtPasswordCfm.setError(null);
			valid = true;
		}
		return valid;
	}
	
	private boolean validateCreationCode() {
		boolean valid = false;
		String creationCode = mEtCreationCode.getText().toString();
		if(creationCode.length() == 0) {
			mEtCreationCode.setError(getString(R.string.register_error_creation_code));
		} else {
			mEtCreationCode.setError(null);
			valid = true;
		}
		return valid;
	}
	
	private void doRegister(String database, String username, String password) {
		hideInputMethod();
		mBtnCreate.setEnabled(false);
		mTvError.setText(null);
		mProgressBar.setVisibility(View.VISIBLE);
		
		// Set database
		ParseManager.setDatabase(database);
		// Sign up parse user
		ParseUser user = new ParseUser();
		user.setUsername(username);
		user.setPassword(password);
		user.signUpInBackground(new SignUpCallback() {
			@Override
			public void done(ParseException e) {
				mBtnCreate.setEnabled(true);
				mProgressBar.setVisibility(View.GONE);
				
				if(e == null) { // ok
					registerSuccess();
				} else { // exception
					String errorMsg = e.getMessage();
					Logger.e(TAG, errorMsg, e);
					mTvError.setText(errorMsg);
				}
			}
		});
	}
	
	private void registerSuccess() {
		setResult(RESULT_OK);
		finish();
	}
	
	private void hideInputMethod() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if(imm.isActive()) {
			imm.hideSoftInputFromWindow(getWindow().getDecorView().getApplicationWindowToken(), 0);
		}
	}
	
	private OnFocusChangeListener onEtFocusChangedListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			int id = v.getId();
			if(id == R.id.register_username_et) {
				if(!hasFocus) {
					validateUsername();
				}
			} else if(id == R.id.register_password_et) {
				if(!hasFocus) {
					validatePassword();
				}
			} else if(id == R.id.register_passwordcfm_et) {
				if(!hasFocus) {
					validatePasswordCfm();
				}
			} else if(id == R.id.register_creationcode_et) {
				if(!hasFocus) {
					validateCreationCode();
				}
			}
		}
	};
}
