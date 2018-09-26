package be.nikiroo.gofetch.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import be.nikiroo.gofetch.support.LWN;
import be.nikiroo.gofetch.support.Type;

public class TestLWN extends TestBase {
	static private Type type = Type.LWN;
	static private TestBase base = null;

	public TestLWN(String[] args) {
		super(new LWN() {
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
