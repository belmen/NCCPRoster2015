package nccp.app.parse.proxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import nccp.app.utils.Logger;

import com.parse.ParseObject;

public class ParseProxyUtil {
	
	public static final String TAG = ParseProxyUtil.class.getSimpleName();

	@SuppressWarnings("unchecked")
	public static <P, O extends ParseObject> List<P> fromParseObjects(List<O> objs,
			Class<O> objectType, Class<P> proxyType) {
		if(objs == null) {
			return null;
		}
		
		Method method = null;
		try {
			method = proxyType.getDeclaredMethod("fromParseObject", objectType);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Proxy " + proxyType.getName() +
					" does not contain a public static called fromParseObject");
		}
		
		final int size = objs.size();
		List<P> result = new ArrayList<P>(size);
		for(O obj : objs) {
			P p;
			try {
				p = (P) method.invoke(null, obj);
				if(p != null) {
					result.add(p);
				}
			} catch (Exception e) {
				Logger.e(TAG, e.getMessage(), e);
				return null;
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static <P, O extends ParseObject> List<O> toParseObjects(List<P> proxies,
			Class<O> objectType, Class<P> proxyType) {
		if(proxies == null) {
			return null;
		}
		
		Method method = null;
		try {
			method = proxyType.getDeclaredMethod("toParseObject", proxyType);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Bean type " + proxyType.getName() +
					" does not contain a public static called toParseObject");
		}
		
		final int size = proxies.size();
		List<O> result = new ArrayList<O>(size);
		for(P bean : proxies) {
			try {
				O obj = (O) method.invoke(null, bean);
				if(obj != null) {
					result.add(obj);
				}
			} catch (Exception e) {
				Logger.e(TAG, e.getMessage(), e);
				return null;
			}
		}
		return result;
	}
}
