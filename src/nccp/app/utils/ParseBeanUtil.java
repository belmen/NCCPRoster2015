package nccp.app.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import nccp.app.bean.ParseBean;

import com.parse.ParseObject;

public class ParseBeanUtil {
	
	public static final String TAG = ParseBeanUtil.class.getSimpleName();

	@SuppressWarnings("unchecked")
	public static <T extends ParseBean> List<T> fromParseObjects(List<ParseObject> objs, Class<T> beanType) {
		if(objs == null) {
			return null;
		}
		
		Method method = null;
		try {
			method = beanType.getDeclaredMethod("fromParseObject", ParseObject.class);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Bean type " + beanType.getName() +
					" does not contain a public static called fromParseObject");
		}
		
		final int size = objs.size();
		List<T> result = new ArrayList<T>(size);
		for(ParseObject obj : objs) {
			T o;
			try {
				o = (T) method.invoke(null, obj);
				if(o != null) {
					result.add(o);
				}
			} catch (Exception e) {
				Logger.e(TAG, e.getMessage(), e);
				return null;
			}
		}
		return result;
	}
}
