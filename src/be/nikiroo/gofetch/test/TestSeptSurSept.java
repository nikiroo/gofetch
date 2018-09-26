package be.nikiroo.gofetch.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import be.nikiroo.gofetch.support.SeptSurSept;
import be.nikiroo.gofetch.support.Type;

public class TestSeptSurSept extends TestBase {
	static private Type type = Type.SEPT_SUR_SEPT;
	static private TestBase base = null;

	public TestSeptSurSept(String[] args) {
		super(new SeptSurSept() {
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
