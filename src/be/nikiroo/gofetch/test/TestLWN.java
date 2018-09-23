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

		map.put(new URL("https://lwn.net/"), new File("index.html"));

		map.put(new URL("https://lwn.net/Articles/763252/"), new File(
				"Articles/763252.html"));
		map.put(new URL("https://lwn.net/Articles/763987/"), new File(
				"Articles/763987.html"));
		map.put(new URL("https://lwn.net/Articles/764046"), new File(
				"Articles/764046.html"));
		map.put(new URL("https://lwn.net/Articles/764055"), new File(
				"Articles/764055.html"));
		map.put(new URL("https://lwn.net/Articles/764130"), new File(
				"Articles/764130.html"));
		map.put(new URL("https://lwn.net/Articles/764182"), new File(
				"Articles/764182.html"));
		map.put(new URL("https://lwn.net/Articles/764184/"), new File(
				"Articles/764184.html"));
		map.put(new URL("https://lwn.net/Articles/764202/"), new File(
				"Articles/764202.html"));
		map.put(new URL("https://lwn.net/Articles/764219"), new File(
				"Articles/764219.html"));
		map.put(new URL("https://lwn.net/Articles/764300"), new File(
				"Articles/764300.html"));
		map.put(new URL("https://lwn.net/Articles/764321/"), new File(
				"Articles/764321.html"));

		return map;
	}

	public TestLWN(String[] args) {
		super(new LWN() {
			@Override
			protected InputStream open(URL url) throws IOException {
				return doOpen(this, getMap(), url);
			}

			@Override
			public Type getType() {
				return Type.LWN;
			}
		}, args);
	}
}
