package be.nikiroo.gofetch.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import be.nikiroo.gofetch.support.LWN;
import be.nikiroo.gofetch.support.Type;

public class TestLWN extends TestBase {

	static private Map<URL, File> getMap() throws MalformedURLException {
		Map<URL, File> map = new HashMap<URL, File>();
		map.put(new URL("http://fanfan.be/"), new File("/tmp/none"));
		return map;
	}

	public TestLWN(String[] args) {
		super(new LWN() {
			@Override
			protected InputStream open(URL url) throws IOException {
				return doOpen(getMap(), url);
			}

			@Override
			public Type getType() {
				return Type.LWN;
			}
		}, args);
	}
}
