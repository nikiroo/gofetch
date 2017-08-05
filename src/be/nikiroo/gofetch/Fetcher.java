package be.nikiroo.gofetch;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import be.nikiroo.gofetch.data.Comment;
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
		File cache = new File(dir, preselector);
		cache.mkdirs();
		File cacheHtml = new File(cache, "index.html");
		cache = new File(cache, ".cache");

		Output gopher = new Gopher(null, hostname, port);
		Output html = new Html(null);

		FileWriter writer = new FileWriter(cache);
		try {
			FileWriter writerHtml = new FileWriter(cacheHtml);
			try {
				writer.append(gopher.getIndexHeader());
				writerHtml.append(html.getIndexHeader());

				Type types[];
				if (type == null) {
					types = Type.values();
				} else {
					types = new Type[] { type };
				}

				BasicSupport.setPreselector(preselector);
				for (Type type : types) {
					BasicSupport support = BasicSupport.getSupport(type);
					list(support);

					writer.append("1" + support.getDescription()).append("\t")
							.append("1" + support.getSelector()) //
							.append("\t").append(hostname) //
							.append("\t").append(Integer.toString(port)) //
							.append("\r\n");
					String ref = support.getSelector();
					while (ref.startsWith("/")) {
						ref = ref.substring(1);
					}
					writerHtml.append("<div class='site'><a href='../" + ref
							+ "'>" + support.getDescription() + "</a></div>");
				}

				writer.append(gopher.getIndexFooter());
				writerHtml.append(html.getIndexFooter());
			} finally {
				writerHtml.close();
			}
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
		Output gopher = new Gopher(support.getType(), hostname, port);
		Output html = new Html(support.getType());

		new File(dir, support.getSelector()).mkdirs();

		System.err
				.print("Listing recent news for " + support.getType() + "...");
		List<Story> stories = support.list();
		System.err.println(" " + stories.size() + " stories found!");
		int i = 1;
		for (Story story : stories) {
			IOUtils.writeSmallFile(dir, story.getSelector() + ".header",
					gopher.export(story));
			IOUtils.writeSmallFile(dir, story.getSelector() + ".header.html",
					html.export(story));

			System.err.println(String.format("%02d/%02d", i, stories.size())
					+ " Fetching comments for story " + story.getId() + "...");
			List<Comment> comments = support.getComments(story);

			IOUtils.writeSmallFile(dir, story.getSelector(),
					gopher.export(story, comments));
			IOUtils.writeSmallFile(dir, story.getSelector() + ".html",
					html.export(story, comments));

			i++;
		}

		File varDir = new File(dir, support.getSelector());
		String[] headers = varDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".header");
			}
		});

		File cache = new File(varDir, ".cache");
		File cacheHtml = new File(varDir, "index.html");
		FileWriter writer = new FileWriter(cache);
		try {
			FileWriter writerHtml = new FileWriter(cacheHtml);
			try {
				if (headers.length > 0) {
					Arrays.sort(headers);
					int from = headers.length - 1;
					int to = headers.length - maxStories;
					if (to < 0) {
						to = 0;
					}
					for (i = from; i >= to; i--) {
						writer.append(IOUtils.readSmallFile(new File(varDir,
								headers[i])));

						writerHtml.append(IOUtils.readSmallFile(new File(
								varDir, headers[i] + ".html")));
					}
				}
			} finally {
				writerHtml.close();
			}
		} finally {
			writer.close();
		}
	}
}
