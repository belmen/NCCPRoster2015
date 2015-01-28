package nccp.app;

import nccp.app.parse.ParseManager;
import nccp.app.utils.Logger;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class MyApplication extends Application {

	public static final String TAG = "MyApplication";

	private static MyApplication mInstance = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Logger.setDebug(true);
		mInstance = this;
		// Initialize parse manager
		ParseManager.initialize(mInstance);
	}
	
	public static MyApplication getInstance() {
		return mInstance;
	}
	
	public static boolean isNetworkAvailable() {
		boolean state = false;
		ConnectivityManager conManager =
				(ConnectivityManager) MyApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
		if (networkInfo != null) {
			/**0： 3G, 1: WIFI*/
			Logger.d(TAG, "NetType = " + networkInfo.getType());
			if (networkInfo.isAvailable() && networkInfo.isConnected()) {
				Logger.d(TAG, "State.CONNECTED");
				state = true;
			} else {
				Logger.d(TAG, "State.NOT_CONNECTED");
				state = false;
			}
		}
		return state;
	}
}
