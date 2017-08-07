package be.nikiroo.gofetch;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import be.nikiroo.gofetch.data.Story;
import be.nikiroo.gofetch.output.Gopher;
import be.nikiroo.gofetch.output.Html;
import be.nikiroo.gofetch.output.Output;
import be.nikiroo.gofetch.support.BasicSupport;
import be.nikiroo.gofetch.support.BasicSupport.Type;
import be.nikiroo.utils.IOUtils;

/**
 * The class that will manage the fetch operations.
 * <p>
 * It will scrap the required websites and process them to disk.
 * 
 * @author niki
 */
public class Fetcher {
	private File dir;
	private String preselector;
	private int maxStories;
	private String hostname;
	private int port;
	private Type type;

	/**
	 * Prepare a new {@link Fetcher}.
	 * 
	 * @param dir
	 *            the target directory where to save the files (won't have
	 *            impact on the files' content)
	 * @param preselector
	 *            the sub directory and (pre-)selector to use for the resources
	 *            (<b>will</b> have an impact on the files' content)
	 * @param type
	 *            the type of news to get (or the special keyword ALL to get all
	 *            of the supported sources)
	 * @param maxStories
	 *            the maximum number of stories to show on the resume page
	 * @param hostname
	 *            the gopher host to use (<b>will</b> have an impact on the
	 *            files' content)
	 * @param port
	 *            the gopher port to use (<b>will</b> have an impact on the
	 *            files' content)
	 */
	public Fetcher(File dir, String preselector, Type type, int maxStories,
			String hostname, int port) {
		this.dir = dir;
		this.preselector = preselector;
		this.type = type;
		this.maxStories = maxStories;
		this.hostname = hostname;
		this.port = port;
	}

	/**
	 * Start the fetching operation.
	 * <p>
	 * This method will handle the main pages itself, and will call
	 * {@link Fetcher#list(BasicSupport)} for the stories.
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public void start() throws IOException {
		StringBuilder gopherBuilder = new StringBuilder();
		StringBuilder htmlBuilder = new StringBuilder();

		BasicSupport.setPreselector(preselector);
		for (Type type : Type.values()) {
			BasicSupport support = BasicSupport.getSupport(type);

			if (type == this.type || this.type == null) {
				list(support);
			}

			gopherBuilder.append("1" + support.getDescription()).append("\t")
					.append("1" + support.getSelector()) //
					.append("\t").append(hostname) //
					.append("\t").append(Integer.toString(port)) //
					.append("\r\n");

			String ref = support.getSelector();
			while (ref.startsWith("/")) {
				ref = ref.substring(1);
			}
			htmlBuilder.append("<div class='site'><a href='../" + ref + "'>"
					+ support.getDescription() + "</a></div>\n");
		}

		File gopherCache = new File(dir, preselector);
		gopherCache.mkdirs();
		File htmlIndex = new File(gopherCache, "index.html");
		gopherCache = new File(gopherCache, ".cache");

		Output gopher = new Gopher(null, hostname, preselector, port);
		Output html = new Html(null, hostname, preselector, port);

		FileWriter writer = new FileWriter(gopherCache);
		try {
			writer.append(gopher.getIndexHeader());
			writer.append(gopherBuilder.toString());
			writer.append(gopher.getIndexFooter());
		} finally {
			writer.close();
		}

		try {
			writer = new FileWriter(htmlIndex);
			writer.append(html.getIndexHeader());
			writer.append(htmlBuilder.toString());
			writer.append(html.getIndexFooter());
		} finally {
			writer.close();
		}
	}

	/**
	 * Process the stories for the given {@link BasicSupport} to disk.
	 * 
	 * @param support
	 *            the {@link BasicSupport} to download from
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 **/
	private void list(BasicSupport support) throws IOException {
		// Get stories:
		System.err
				.print("Listing recent news for " + support.getType() + "...");
		List<Story> stories = support.list();
		System.err.println(" " + stories.size() + " stories found!");

		// Get comments (and update stories if needed):
		int i = 1;
		for (Story story : stories) {
			System.err.println(String.format("%02d/%02d", i, stories.size())
					+ " Fetching full story " + story.getId() + "...");
			support.fetch(story);
			i++;
		}

		Output gopher = new Gopher(support.getType(), hostname, preselector,
				port);
		Output html = new Html(support.getType(), hostname, preselector, port);

		new File(dir, support.getSelector()).mkdirs();

		for (Story story : stories) {
			IOUtils.writeSmallFile(dir, story.getSelector() + ".header",
					gopher.exportHeader(story));
			IOUtils.writeSmallFile(dir, story.getSelector() + ".header.html",
					html.exportHeader(story));

			IOUtils.writeSmallFile(dir, story.getSelector(),
					gopher.export(story));
			IOUtils.writeSmallFile(dir, story.getSelector() + ".html",
					html.export(story));
		}

		// Finding headers of all stories in cache:
		File varDir = new File(dir, support.getSelector());
		String[] headers = varDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".header");
			}
		});

		// Finding which ones to show:
		int from = 0;
		int to = 0;
		if (headers.length > 0) {
			Arrays.sort(headers);
			from = headers.length - 1;
			to = headers.length - maxStories;
			if (to < 0) {
				to = 0;
			}
		}

		// Writing the cache/index files with the stories:
		File gopherCache = new File(varDir, ".cache");
		FileWriter writer = new FileWriter(gopherCache);
		try {
			for (i = from; i >= to; i--) {
				writer.append(IOUtils
						.readSmallFile(new File(varDir, headers[i])));
			}
		} finally {
			writer.close();
		}

		File htmlIndex = new File(varDir, "index.html");
		writer = new FileWriter(htmlIndex);
		try {
			for (i = from; i >= to; i--) {
				writer.append(IOUtils.readSmallFile(new File(varDir, headers[i]
						+ ".html")));
			}
		} finally {
			writer.close();
		}
	}
}
