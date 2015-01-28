package nccp.app.ui;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import nccp.app.utils.Const;
import nccp.app.utils.Logger;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class FragmentHelper {
	
public static final String TAG = "FragmentHelper";
	
	private static final String STATE_IDS = Const.PACKAGE_NAME + ".fragment_ids";
	private static final String STATE_CURRENT_FRAGMENT_ID = Const.PACKAGE_NAME + ".current_fragment_id";
	private static final String STATE_LAST_FRAGMENT = Const.PACKAGE_NAME + ".last_fragment";

	private Context mContext;
	private FragmentManager mManager;
	private int mContainerId;
	
	private Map<String, SoftReference<Fragment>> mFragments = new HashMap<String, SoftReference<Fragment>>();
	private String mCurrentFragmentId = null;
	private Fragment mLastFragment;
	
	public FragmentHelper(Context context, FragmentManager manager, int containerId) {
		mContext = context;
		mManager = manager;
		mContainerId = containerId;
	}
	
	public Fragment getFragment(String fragmentId) {
		Fragment fragment = null;
		SoftReference<Fragment> reference = mFragments.get(fragmentId);
		if(reference != null) {
			fragment = reference.get();
		}
		return fragment;
	}
	
	public String getCurrentFragmentId() {
		return mCurrentFragmentId;
	}
	
	public Fragment getCurrentFragment() {
		return getFragment(getCurrentFragmentId());
	}

	public void switchFragment(String id, Intent intent) {
		switchFragment(id, intent, -1);
	}
	
	public void switchFragment(String id, Intent intent, int transition) {
		if(intent == null) {
			return;
		}
		Fragment newFragment = getFragment(id);
		if(mLastFragment == null || mLastFragment != newFragment) {
			FragmentTransaction ft = mManager.beginTransaction();
			if(mLastFragment != null) {
				ft.detach(mLastFragment);
			}
			if(newFragment == null) {
				Logger.i(TAG, TAG + ": Instantiate a new fragment " + id);
				String fname = intent.getComponent().getClassName();
				Bundle args = intent.getExtras();
				newFragment = Fragment.instantiate(mContext, fname);
				if(args != null) {
					newFragment.setArguments(args);
				}
				mFragments.put(id, new SoftReference<Fragment>(newFragment));
				ft.add(mContainerId, newFragment, id);
			} else {
				Logger.i(TAG, TAG + ": Load an old fragment " + id);
				ft.attach(newFragment);
			}
			if(transition != -1) {
				ft.setTransition(transition);
			}
			mLastFragment = newFragment;
			mCurrentFragmentId = id;
			ft.commit();
//			mManager.executePendingTransactions();
		}
	}
	
	/**
	 * 在Bundle中保存状态
	 * @param bundle
	 */
	public void saveInBundle(Bundle bundle) {
		if(bundle == null) {
			return;
		}
		Logger.i(TAG, TAG + ": saving in bundle");
		ArrayList<String> idList = new ArrayList<String>(mFragments.keySet());
		bundle.putStringArrayList(STATE_IDS, idList);
		
		Iterator<Entry<String, SoftReference<Fragment>>> iter = mFragments.entrySet().iterator();
		Entry<String, SoftReference<Fragment>> entry;
		SoftReference<Fragment> reference;
		Fragment fragment;
		while (iter.hasNext()) {
			entry = iter.next();
			reference = entry.getValue();
			if(reference != null) {
				fragment = reference.get();
				if(fragment != null) {
					mManager.putFragment(bundle, entry.getKey(), fragment);
				}
			}
		}
		
		if(mCurrentFragmentId != null) {
			bundle.putString(STATE_CURRENT_FRAGMENT_ID, mCurrentFragmentId);
		}
		
		if(mLastFragment != null) {
			mManager.putFragment(bundle, STATE_LAST_FRAGMENT, mLastFragment);
		}
	}
	
	/**
	 * 在Bundle中保存状态，只保存当前的fragment
	 * @param bundle
	 */
	public void saveStateSimple(Bundle bundle) {
		if(bundle == null) {
			return;
		}
		Logger.i(TAG, TAG + ": saving in bundle simple");
		
		if(mCurrentFragmentId != null) {
			bundle.putString(STATE_CURRENT_FRAGMENT_ID, mCurrentFragmentId);
			
			ArrayList<String> idList = new ArrayList<String>(1);
			idList.add(mCurrentFragmentId);
			bundle.putStringArrayList(STATE_IDS, idList);
			
			Fragment currentFragment = getCurrentFragment();
			if(currentFragment != null) {
				mManager.putFragment(bundle, mCurrentFragmentId, currentFragment);
			}
		}
		
		if(mLastFragment != null) {
			mManager.putFragment(bundle, STATE_LAST_FRAGMENT, mLastFragment);
		}
	}
	
	/**
	 * 从Bundle中恢复状态
	 * @param bundle
	 */
	public void restoreFromBundle(Bundle bundle) {
		if(bundle == null) {
			return;
		}
		Logger.i(TAG, TAG + ": restoring from bundle");
		ArrayList<String> idList = bundle.getStringArrayList(STATE_IDS);
		if(idList != null) {
			Fragment fragment;
			for(String id : idList) {
				fragment = mManager.getFragment(bundle, id);
				if(fragment != null) {
					mFragments.put(id, new SoftReference<Fragment>(fragment));
				}
			}
		}
		
		String currentId = bundle.getString(STATE_CURRENT_FRAGMENT_ID);
		if(currentId != null) {
			mCurrentFragmentId = currentId;
		}
		
		Fragment lastFragment = mManager.getFragment(bundle, STATE_LAST_FRAGMENT);
		if(lastFragment != null) {
			mLastFragment = lastFragment;
		}
	}
}
