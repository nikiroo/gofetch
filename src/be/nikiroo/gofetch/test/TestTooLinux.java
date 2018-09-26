package be.nikiroo.gofetch.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import be.nikiroo.gofetch.support.TooLinux;
import be.nikiroo.gofetch.support.Type;

public class TestTooLinux extends TestBase {
	static private Type type = Type.TOO_LINUX;
	static private TestBase base = null;

	public TestTooLinux(String[] args) {
		super(new TooLinux() {
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
