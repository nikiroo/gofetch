package be.nikiroo.gofetch.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import be.nikiroo.gofetch.support.Slashdot;
import be.nikiroo.gofetch.support.Type;

public class TestSlashdot extends TestBase {
	static private Type type = Type.SLASHDOT;
	static private TestBase base = null;

	public TestSlashdot(String[] args) {
		super(new Slashdot() {
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
