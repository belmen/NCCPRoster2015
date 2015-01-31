package nccp.app.bean;

import java.io.Serializable;

public class ParseBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String parseObjectId;

	public String getParseObjectId() {
		return parseObjectId;
	}

	public void setParseObjectId(String parseObjectId) {
		this.parseObjectId = parseObjectId;
	}
}
