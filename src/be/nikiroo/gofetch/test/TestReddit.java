
package be.nikiroo.gofetch.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import be.nikiroo.gofetch.support.Reddit;
import be.nikiroo.gofetch.support.Type;

public class TestReddit extends TestBase {
	static private Type type = Type.REDDIT;
	static private TestBase base = null;

	public TestReddit(String[] args) {
		super(new Reddit() {
			@Override
			protected InputStream open(URL url) throws IOException {
				return base.download(url);
			}

			@Override
			public Type getType() {
				return type;
			}
		}, args);

		base = this;
	}
}
