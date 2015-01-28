package nccp.app.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParseAppManager {

	public static class AppKey {
		public final String applicationId;
		public final String clientKey;
		
		public AppKey(String applicationId, String clientKey) {
			this.applicationId = applicationId;
			this.clientKey = clientKey;
		}
	}
	
	static {
		init();
	}
	
	private static Map<String, AppKey> mAppKeyMap;
	private static List<String> mApps;
	private static Map<String, String> mCreationCodeMap;
	
	private static void init() {
		mAppKeyMap = new LinkedHashMap<String, AppKey>();
		
		mAppKeyMap.put("Comegys", new AppKey("XcQ7KaD1QZeDBsehBsNB5oWG4rg70XiOPsv4Qxyg", "gmhySW6MCMOZT6BKsJzmYQw6Rqa1MNskYTGVQi9n"));
		mAppKeyMap.put("Wilson", new AppKey("3XucEC1ZC0NqZ9yhgJISZyJFmVY9XKHZ0ZMkAAlT", "gvEbgFl40PKG3x2SewFccrcnPnLAMf9pOhYyQ8y2"));
		mAppKeyMap.put("Lea", new AppKey("eY959u4MLn16MedbGFNff6xiNhZXRsRMCfLncL2l", "FV4GCz2eMmXnuXxGkQ2HFdPpfkBjIUvNKGH6cs8z"));
		mAppKeyMap.put("Sayre", new AppKey("NrRH4GXm4fUcEL5l8CmX6awCpOteAifKsvrbkY4q", "PtEhdM9AW1rSqnGGew5YYfacvAHiVIongO5gICjy"));
		mAppKeyMap.put("UCHS", new AppKey("JqWA8igCX4za22oqw5J5sz9Wwx1zBLLipTyAOXtb", "gO7MhEF1VhzmRBaS0M0uLugZONDRNW7F0F07wgZu"));
		mAppKeyMap.put("Huey", new AppKey("DxGsKYUcTbuH0cGjZEGhXIni6j7oSEMwoPaFAk8E", "HF0GOShdS43nMC3Rr3Bek9MM6TLhyxLfXqrEXOb1"));
		mAppKeyMap.put("West", new AppKey("QEXQUygyW9gnXSRvWMDuiTqrPV01rEvznmOx5ttA", "P8YSiMVM1iZIj1oa9iyAbBkQXnbGkxYbfbZxl14q"));
		mAppKeyMap.put("Bartrams", new AppKey("WE5KIALz4IUPzLtdNQu3IZuOi7mLE2MylJkzrAHg", "XXgTNbNu0akgdXf3VOmjkdQaZeADNlN2dQ41CiFt"));
		mAppKeyMap.put("SOTF", new AppKey("KeOFSqlOHe9A3qmhOxGLUIyXDRcPqlLu82ZrzmxK", "8RRN8ip8FvayPDqPeNIyYmzdidbqx5uiH9xZziqp"));
		mAppKeyMap.put("New 1", new AppKey("6fdU3KLp1XNBlI46hLK6hshFIfayFtVnjfXPRc70", "M9G5oCWQukwkjPxlHjke9Z6PZl1gCoMW4LX0sPHa"));
		mAppKeyMap.put("New 2", new AppKey("n9q9nrW125OviPw764tbZAB1dOqmyVLTYAGFh8bp", "hx4XpAQJgZMbp8rfT3xf8S4nGsLjCCJ2gCU6nk1O"));
		mAppKeyMap.put("New 3", new AppKey("PbQv4Qy2yCMVuzUrhso1d1EaeoukZCexYQJbYXcc", "GyS0lHkwI8Zdd6vfXihN8rrkGink1VvNIh6BqeUO"));
		mAppKeyMap.put("New 4", new AppKey("x5xYXe058e3lpUXJoK9eOYCeaTvmQn599Hzut6Fh", "GXRxR1lc9gIbPy1YuarDf1IME9UPE7fHrsffybZv"));
		mAppKeyMap.put("New 5", new AppKey("cuoXWbqvBKs8SUrhnyKdyNWiMPZxuDBZ31ehltVI", "tl8VMcFHu7u3haym9KSbRKEP61MmxPDvmL06dxeo"));
		
		mApps = new ArrayList<String>();
		Set<String> keys = mAppKeyMap.keySet();
		for(String app : keys) {
			mApps.add(app);
		}
		
		mCreationCodeMap = new HashMap<String, String>();
		mCreationCodeMap.put("OCWD", "Comegys");
		mCreationCodeMap.put("PIEB", "Wilson");
		mCreationCodeMap.put("NVSO", "Lea");
		mCreationCodeMap.put("REVH", "Sayre");
		mCreationCodeMap.put("MJOG", "UCHS");
		mCreationCodeMap.put("KEVJ", "Huey");
		mCreationCodeMap.put("LPRE", "West");
		mCreationCodeMap.put("MHFD", "Bartrams");
		mCreationCodeMap.put("XCZE", "SOTF");
		mCreationCodeMap.put("POFW", "New 1");
		mCreationCodeMap.put("PRED", "New 2");
		mCreationCodeMap.put("MNJT", "New 3");
		mCreationCodeMap.put("EDPJ", "New 4");
		mCreationCodeMap.put("LWQK", "New 5");
	}
	
	public static List<String> getAppList() {
		return new ArrayList<String>(mApps);
	}
	
	public static AppKey getAppKey(String appName) {
		return mAppKeyMap.get(appName);
	}
	
	public static String getAppFromCreationCode(String creationCode) {
		return mCreationCodeMap.get(creationCode);
	}
}
