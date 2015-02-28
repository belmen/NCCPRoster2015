package nccp.app.parse.object;

import com.parse.ParseObject;

public class BaseParseObject extends ParseObject {

	@Override
	public boolean equals(Object o) {
		BaseParseObject b = (BaseParseObject) o;
		return getObjectId().equals(b.getObjectId());
	}

}
