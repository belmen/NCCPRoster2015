package nccp.app.parse;

import nccp.app.parse.ParseAppManager.AppKey;
import nccp.app.utils.Logger;
import android.content.Context;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;

public class ParseManager {
	
	public static final String TAG = ParseManager.class.getSimpleName();

	private static Context mContext;
	private static String mDatabase = null;
	
	public static void initialize(Context context) {
		mContext = context;
		// Initialize parse objects
//		ParseObject.registerSubclass(Student.class);
	}
	
	public static void setDatabase(String database) {
		AppKey key = ParseAppManager.getAppKey(database);
		if(mContext != null && key != null) {
			mDatabase = database;
			Parse.initialize(mContext, key.applicationId, key.clientKey);
			Logger.d(TAG, "Database changed to " + database);
			Logger.d(TAG, "Application ID: " + key.applicationId);
			Logger.d(TAG, "Client key: " + key.clientKey);
		}
	}
	
	public static boolean isDatabaseSet() {
		return mDatabase != null;
	}

	/**
	 * Create new account
	 * @param username
	 * @param password
	 * @return Created new account
	 * @throws ParseException
	 */
	public static ParseUser createAccount(String username, String password) throws ParseException {
		ParseUser user = new ParseUser();
		user.setUsername(username);
		user.setPassword(password);
		user.signUp();
		return user;
	}
}
