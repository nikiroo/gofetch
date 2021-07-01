package be.nikiroo.gofetch.support;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.helper.DataUtil;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import be.nikiroo.gofetch.data.Story;
import be.nikiroo.utils.Downloader;
import be.nikiroo.utils.StringUtils;

/**
 * Base class for website support.
 * 
 * @author niki
 */
public abstract class BasicSupport {
	/**
	 * The {@link Downloader} to use for all web sites via
	 * {@link BasicSupport#open(URL)}
	 */
	static private Downloader downloader = new Downloader("gofetcher");

	static private String preselector;

	/**
	 * The optional cookies to use to get the site data.
	 */
	private Map<String, String> cookies = new HashMap<String, String>();

	private Type type;

	/**
	 * Login on the web site (this method does nothing by default, but can be
	 * overridden if needed).
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 * 
	 */
	public void login() throws IOException {
	}

	/**
	 * The website textual description, to add in the dispatcher page.
	 * <p>
	 * Should be short.
	 * 
	 * @return the description
	 */
	abstract public String getDescription();

	/**
	 * The {@link URL}s to process for this website.
	 * 
	 * @return the list of {@link URL}s
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	abstract protected List<Entry<URL, String>> getUrls() throws IOException;

	/**
	 * An extractor for the data present in the snippet article (the content
	 * about the article that you can find on articles listing pages).
	 * <p>
	 * This one is mandatory.
	 * 
	 * @return the extractor
	 */
	abstract BasicSnippetExtractor getSnippetExtractor();

	/**
	 * An extractor for the data of the actual article (on a dedicated page).
	 * <p>
	 * If not present, no data will be extracted outside of what was found on
	 * the snippet.
	 * 
	 * @return the extractor
	 */
	BasicFullArticleExtractor getFullArticleExtractor() {
		return null;
	}

	/**
	 * An extractor for the comments of the actual article.
	 * <p>
	 * If not present, no comments will be used.
	 * 
	 * @return the extractor
	 */
	BasicCommentExtractor getCommentExtractor() {
		return null;
	}

	/**
	 * The gopher "selector" to use for output.
	 * <p>
	 * A kind of "URL path", like "/news/" or "/misc/news/" or...
	 * 
	 * @return the selector
	 */
	public String getSelector() {
		return getSelector(getType());
	}

	/**
	 * The support type.
	 * 
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * List all the recent items, but only assure the internal URL to fetch it
	 * later on (until it has been fetched, the rest of the {@link Story} is not
	 * confirmed).
	 * <p>
	 * Note: an id or a date is required, but is allowed to change later on
	 * during the call to {@link BasicSupport#fetch(Story)}.
	 * 
	 * @return the list of new stories
	 * 
	 * @throws IOException
	 *             in case of I/O
	 */
	public List<Story> list() throws IOException {
		List<Story> list = new ArrayList<Story>();

		BasicSnippetExtractor extractor = getSnippetExtractor();

		login();
		for (Entry<URL, String> entry : getUrls()) {
			URL url = entry.getKey();
			String defaultCateg = entry.getValue();
			if (defaultCateg == null) {
				defaultCateg = "";
			}

			InputStream in = open(url);
			Document doc = DataUtil.load(in, "UTF-8", url.toString());

			list.addAll(extractor.fetchSnippets(doc, getType(), defaultCateg));
		}

		return list;
	}

	/**
	 * Fetch the full article content as well as all the comments associated to
	 * this {@link Story}, if any (can be empty, but not NULL).
	 * 
	 * @param story
	 *            the story to fetch the comments of
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public void fetch(Story story) throws IOException {
		URL url = new URL(story.getUrlInternal());
		InputStream in = open(url);
		try {
			Document doc = DataUtil.load(in, "UTF-8", url.toString());
			BasicFullArticleExtractor extractorFA = getFullArticleExtractor();
			if (extractorFA != null)
				extractorFA.fetchFullArticle(url, doc, story);

			BasicCommentExtractor extractorC = getCommentExtractor();
			if (extractorC != null)
				extractorC.fetchComments(url, doc, story);
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	/**
	 * Open a network resource.
	 * <p>
	 * You need to close the returned {@link InputStream} when done.
	 * 
	 * @param url
	 *            the source to open
	 * 
	 * @return the content
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	protected InputStream open(URL url) throws IOException {
		return downloader.open(url, url, cookies, null, null, null);
	}

	/**
	 * The support type.
	 * 
	 * @param type
	 *            the new type
	 */
	protected void setType(Type type) {
		this.type = type;
	}

	/**
	 * Add a cookie for all site connections.
	 * 
	 * @param name
	 *            the cookie name
	 * @param value
	 *            the value
	 */
	protected void addCookie(String name, String value) {
		cookies.put(name, value);
	}

	/**
	 * The {@link String} to append to the selector (the selector will be
	 * constructed as "this string" then "/type/".
	 * 
	 * @param preselector
	 *            the preselector to set
	 */
	static public void setPreselector(String preselector) {
		BasicSupport.preselector = preselector;
	}

	/**
	 * Return a {@link BasicSupport} that is compatible with the given
	 * {@link Type} if it exists (or NULL if not).
	 * 
	 * @param type
	 *            the type
	 * 
	 * @return a compatible {@link BasicSupport} if it exists (or NULL if not)
	 */
	static public BasicSupport getSupport(Type type) {
		BasicSupport support = null;

		if (type != null) {
			switch (type) {
			case SLASHDOT:
				support = new Slashdot();
				break;
			case LWN:
				support = new LWN();
				break;
			case LEMONDE:
				support = new LeMonde();
				break;
			case REGISTER:
				support = new TheRegister();
				break;
			case TOO_LINUX:
				support = new TooLinux();
				break;
			case ERE_NUMERIQUE:
				support = new EreNumerique();
				break;
			case PHORONIX:
				support = new Phoronix();
				break;
			case SEPT_SUR_SEPT:
				support = new SeptSurSept();
				break;
			case REDDIT:
				support = new Reddit();
				break;
			case DAARDAAR:
				support = new DaarDaar();
				break;
			}

			if (support != null) {
				support.setType(type);
			}
		}

		return support;
	}

	/**
	 * The gopher "selector" to use for output for this type, using the
	 * preselector.
	 * <p>
	 * A kind of "URL path", like "/news/" or "/misc/news/" or...
	 * 
	 * @param type
	 *            the type to get the selector of
	 * 
	 * @return the selector
	 */
	static public String getSelector(Type type) {
		return preselector + "/" + type + "/";
	}

	/**
	 * Process the given element into text (each line is a text paragraph and
	 * can be prepended with ">" signs to indicate a quote or sub-quote or
	 * sub-sub-quote...).
	 * 
	 * @param element
	 *            the element to process
	 * @param elementProcessor
	 *            the element processor, must not be NULL
	 * 
	 * @return text lines, each line is a paragraph
	 */
	static List<String> toLines(Element element,
			final BasicElementProcessor elementProcessor) {
		final List<String> lines = new ArrayList<String>();
		final StringBuilder currentLine = new StringBuilder();
		final List<Integer> quoted = new ArrayList<Integer>();
		final List<Node> ignoredNodes = new ArrayList<Node>();
		final List<String> footnotes = new ArrayList<String>();

		if (element != null) {
			new NodeTraversor(new NodeVisitor() {
				@Override
				public void head(Node node, int depth) {
					String manual = null;
					boolean ignore = elementProcessor.ignoreNode(node)
							|| ignoredNodes.contains(node.parentNode());
					// Manual processing
					if (!ignore) {
						manual = elementProcessor.manualProcessing(node);
						if (manual != null) {
							currentLine.append(manual);
							ignore = true;
						}
					}

					// Subtitle check
					if (!ignore) {
						String subtitle = elementProcessor.isSubtitle(node);
						if (subtitle != null) {
							subtitle = subtitle.trim();
							currentLine.append("\n[ " + subtitle + " ]\n");
							ignore = true;
						}
					}

					// <pre> check
					if (!ignore) {
						if (node instanceof Element) {
							Element el = (Element) node;
							if ("pre".equals(el.tagName())) {
								currentLine.append(StringUtils
										.unhtml(el.text()).trim());
								ignore = true;
							}
						}
					}

					if (ignore) {
						ignoredNodes.add(node);
						return;
					}

					String prep = "";
					for (int i = 0; i < quoted.size(); i++) {
						prep += ">";
					}
					prep += " ";

					boolean enterQuote = elementProcessor.detectQuote(node);
					boolean leaveQuote = quoted.contains(depth);

					if (enterQuote) {
						quoted.add(depth);
					}

					if (leaveQuote) {
						quoted.remove(Integer.valueOf(depth));
					}

					if (enterQuote || leaveQuote) {
						if (currentLine.length() > 0) {
							if (currentLine.charAt(currentLine.length() - 1) == '\n') {
								currentLine.setLength(currentLine.length() - 1);
							}
							for (String l : currentLine.toString().split("\n")) {
								lines.add(prep + l);
							}
						}
						currentLine.setLength(0);
					}

					if (node instanceof Element) {
						Element element = (Element) node;
						boolean block = element.isBlock()
								|| element.tagName().equalsIgnoreCase("br");
						if (block && currentLine.length() > 0) {
							currentLine.append("\n");
						}

						if (!element.absUrl("href").trim().isEmpty()) {
							footnotes.add(element.absUrl("href"));
							currentLine.append("[" + footnotes.size() + "]");
						}
					} else if (node instanceof TextNode) {
						TextNode textNode = (TextNode) node;
						String line = StringUtil.normaliseWhitespace(textNode
								.getWholeText());

						currentLine.append(elementProcessor.processText(line));
						currentLine.append(" ");
					}
				}

				@Override
				public void tail(Node node, int depth) {
				}
			}).traverse(element);
		}

		if (currentLine.length() > 0) {
			String prep = "";
			for (int i = 0; i < quoted.size(); i++) {
				prep += ">";
			}
			prep += " ";
			if (currentLine.length() > 0) {
				if (currentLine.charAt(currentLine.length() - 1) == '\n') {
					currentLine.setLength(currentLine.length() - 1);
				}
				for (String l : currentLine.toString().split("\n")) {
					lines.add(prep + l);
				}
			}
		}

		// Fix spaces and nbsp, remove multiple following blank lines
		List<String> linesCopy = new ArrayList<String>(lines.size());
		long blanks = 0;
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i).replace(" ", " ") // nbsp -> space
					.replace("  ", " ").trim();
			if (line.isEmpty()) {
				blanks++;
			} else {
				blanks = 0;
			}

			if (blanks < 2) {
				linesCopy.add(line);
			}
		}

		// Footnotes insertion
		if (footnotes.size() > 0) {
			linesCopy.add("");
			linesCopy.add("");
			linesCopy.add("");
			linesCopy.add("");
			for (int i = 0; i < footnotes.size(); i++) {
				linesCopy.add("[" + (i + 1) + "] " + footnotes.get(i));
			}
		}

		return linesCopy;
	}

	/**
	 * Reformat the date if possible.
	 * 
	 * @param date
	 *            the input date
	 * 
	 * @return the reformated date, or the same value if it was not parsable
	 */
	static String date(String date) {
		SimpleDateFormat out = new SimpleDateFormat("yyyy/MM/dd");

		long epoch = 0;
		try {
			epoch = Long.parseLong(date.trim());
		} catch (Exception e) {
			epoch = 0;
		}

		if (epoch > 0) {
			return out.format(new Date(1000 * epoch));
		}

		try {
			Date dat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
					.parse(date.trim());
			return out.format(dat);
		} catch (Exception e) {
			return date;
		}
	}
}
