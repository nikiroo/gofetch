package be.nikiroo.gofetch.support;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.zip.GZIPInputStream;

import be.nikiroo.gofetch.data.Comment;
import be.nikiroo.gofetch.data.Story;

public abstract class BasicSupport {
	public enum Type {
		SLASHDOT, PIPEDOT, LWN,
	}

	static private String preselector;

	private Type type;

	abstract public List<Story> list() throws IOException;

	abstract public List<Comment> getComments(Story story) throws IOException;

	abstract public String getDescription();

	public String getSelector() {
		return getSelector(type);
	}

	public Type getType() {
		return type;
	}

	protected void setType(Type type) {
		this.type = type;
	}

	/**
	 * @param preselector
	 *            the preselector to set
	 */
	static public void setPreselector(String preselector) {
		BasicSupport.preselector = preselector;
	}

	static public BasicSupport getSupport(Type type) {
		BasicSupport support = null;

		if (type != null) {
			switch (type) {
			case SLASHDOT:
				support = new Slashdot();
				break;
			case PIPEDOT:
				support = new Pipedot();
				break;
			case LWN:
				support = new LWN();
				break;
			}

			if (support != null) {
				support.setType(type);
			}
		}

		return support;
	}

	static public String getSelector(Type type) {
		return preselector + "/" + type + "/";
	}

	// TODO: check Downloader.java?
	static protected InputStream open(URL url) throws IOException {
		URLConnection conn = url.openConnection();
		conn.connect();
		InputStream in = conn.getInputStream();
		if ("gzip".equals(conn.getContentEncoding())) {
			in = new GZIPInputStream(in);
		}

		return in;
	}
}
