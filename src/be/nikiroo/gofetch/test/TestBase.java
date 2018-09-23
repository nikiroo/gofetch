package be.nikiroo.gofetch.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import be.nikiroo.gofetch.data.Story;
import be.nikiroo.gofetch.output.Gopher;
import be.nikiroo.gofetch.output.Html;
import be.nikiroo.gofetch.output.Output;
import be.nikiroo.gofetch.support.BasicSupport;
import be.nikiroo.utils.IOUtils;
import be.nikiroo.utils.test.TestCase;
import be.nikiroo.utils.test.TestLauncher;

/**
 * Base class for {@link BasicSupport}s testing.
 * 
 * @author niki
 */
abstract class TestBase extends TestLauncher {
	public TestBase(BasicSupport support, String[] args) {
		super(support.getType().toString(), args);
		addTest(support);
	}

	static protected InputStream doOpen(Map<URL, File> map, URL url)
			throws IOException {
		File file = map.get(url);
		if (file == null) {
			throw new FileNotFoundException("Test file not found for URL: "
					+ url);
		}

		return new FileInputStream(file);

	}

	private void addTest(final BasicSupport support) {
		addTest(new TestCase("Processing example data") {
			@Override
			public void test() throws Exception {
				File expected = new File("test/expected/" + support.getType());
				File actual = new File("test/result/" + support.getType());

				Output gopher = new Gopher(support.getType(), "", "", 70);
				Output html = new Html(support.getType(), "", "", 80);

				for (Story story : support.list()) {
					IOUtils.writeSmallFile(new File(actual, story.getId()
							+ ".header"), gopher.exportHeader(story));
					IOUtils.writeSmallFile(
							new File(actual, story.getId() + ""),
							gopher.export(story));
					IOUtils.writeSmallFile(new File(actual, story.getId()
							+ ".header.html"), html.exportHeader(story));
					IOUtils.writeSmallFile(new File(actual, story.getId()
							+ ".html"), html.export(story));
				}

				assertEquals(expected, actual);
			}
		});
	}
}
