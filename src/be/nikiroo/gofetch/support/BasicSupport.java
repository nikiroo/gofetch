package be.nikiroo.gofetch.support;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.jsoup.helper.DataUtil;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import be.nikiroo.gofetch.data.Comment;
import be.nikiroo.gofetch.data.Story;
import be.nikiroo.utils.Downloader;
import be.nikiroo.utils.StringUtils;

/**
 * Base class for website support.
 * 
 * @author niki
 */
public abstract class BasicSupport {
	/** The downloader to use for all websites. */
	static protected Downloader downloader = new Downloader("gofetcher");

	static private String preselector;

	private Type type;

	/**
	 * The website textual description, to add in the dispatcher page.
	 * <p>
	 * Should be short.
	 * 
	 * @return the description
	 */
	abstract public String getDescription();

	/**
	 * The gopher "selector" to use for output.
	 * <p>
	 * A kind of "URL path", like "/news/" or "/misc/news/" or...
	 * 
	 * @return the selector
	 */
	public String getSelector() {
		return getSelector(type);
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
	 * List all the recent items, but only assure the ID and internal URL to
	 * fetch it later on (until it has been fetched, the rest of the
	 * {@link Story} is not confirmed).
	 * 
	 * @return the list of new stories
	 * 
	 * @throws IOException
	 *             in case of I/O
	 */
	public List<Story> list() throws IOException {
		List<Story> list = new ArrayList<Story>();

		for (Entry<URL, String> entry : getUrls()) {
			URL url = entry.getKey();
			String defaultCateg = entry.getValue();
			if (defaultCateg == null) {
				defaultCateg = "";
			}

			InputStream in = downloader.open(url);
			Document doc = DataUtil.load(in, "UTF-8", url.toString());
			List<Element> articles = getArticles(doc);
			for (Element article : articles) {
				String id = getArticleId(doc, article).trim();
				String title = getArticleTitle(doc, article).trim();
				String author = getArticleAuthor(doc, article).trim();
				String date = getArticleDate(doc, article).trim();
				String categ = getArticleCategory(doc, article, defaultCateg)
						.trim();
				String details = getArticleDetails(doc, article).trim();
				String intUrl = getArticleIntUrl(doc, article).trim();
				String extUrl = getArticleExtUrl(doc, article).trim();
				String content = getArticleContent(doc, article).trim();

				if (id.isEmpty() && date.isEmpty()) {
					continue;
				}

				if (!id.isEmpty()) {
					while (id.length() < 10) {
						id = "0" + id;
					}
				} else {
					id = date.replace(":", "_").replace("+", "_");
				}

				date = date(date);

				list.add(new Story(getType(), id, title, author, date, categ,
						details, intUrl, extUrl, content));
			}
		}

		return list;
	}

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
	 * The article {@link Element}s of this document.
	 * 
	 * @param doc
	 *            the main document for the current category
	 * 
	 * @return the articles
	 */
	abstract protected List<Element> getArticles(Document doc);

	/**
	 * The ID of the article (defaults to the date element if empty).
	 * 
	 * @param doc
	 *            the main document for the current category
	 * @param article
	 *            the article to look into
	 * 
	 * @return the ID
	 */
	abstract protected String getArticleId(Document doc, Element article);

	/**
	 * The article title to display.
	 * 
	 * @param doc
	 *            the main document for the current category
	 * @param article
	 *            the article to look into
	 * 
	 * @return the title
	 */
	abstract protected String getArticleTitle(Document doc, Element article);

	/**
	 * The optional article author.
	 * 
	 * @param doc
	 *            the main document for the current category
	 * @param article
	 *            the article to look into
	 * 
	 * @return the author
	 */
	abstract protected String getArticleAuthor(Document doc, Element article);

	/**
	 * The optional article date.
	 * 
	 * @param doc
	 *            the main document for the current category
	 * @param article
	 *            the article to look into
	 * 
	 * @return the date
	 */
	abstract protected String getArticleDate(Document doc, Element article);

	/**
	 * the optional article category.
	 * 
	 * @param doc
	 *            the main document for the current category
	 * @param article
	 *            the article to look into
	 * @param currentCategory
	 *            the currently listed category if any (can be NULL)
	 * 
	 * @return the category
	 */
	abstract protected String getArticleCategory(Document doc, Element article,
			String currentCategory);

	/**
	 * the optional details of the article (can replace the date, author and
	 * category, for instance).
	 * 
	 * @param doc
	 *            the main document for the current category
	 * @param article
	 *            the article to look into
	 * 
	 * @return the details
	 */
	abstract protected String getArticleDetails(Document doc, Element article);

	/**
	 * The (required) {@link URL} that points to the news page on the supported
	 * website.
	 * 
	 * @param doc
	 *            the main document for the current category
	 * @param article
	 *            the article to look into
	 * 
	 * @return the internal {@link URL}
	 */
	abstract protected String getArticleIntUrl(Document doc, Element article);

	/**
	 * the optional {@link URL} that points to an external website for more
	 * information.
	 * 
	 * @param doc
	 *            the main document for the current category
	 * @param article
	 *            the article to look into
	 * 
	 * @return the external {@link URL}
	 */
	abstract protected String getArticleExtUrl(Document doc, Element article);

	/**
	 * The optional article short-content (not the full content, that will be
	 * fetched by {@link BasicSupport#fetch(Story)}).
	 * 
	 * @param doc
	 *            the main document for the current category
	 * @param article
	 *            the article to look into
	 * 
	 * @return the short content
	 */
	abstract protected String getArticleContent(Document doc, Element article);

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
		String fullContent = "";

		URL url = new URL(story.getUrlInternal());
		InputStream in = downloader.open(url);
		try {
			Document doc = DataUtil.load(in, "UTF-8", url.toString());
			Element article = getFullArticle(doc);
			if (article != null) {
				StringBuilder builder = new StringBuilder();
				ElementProcessor eProc = getElementProcessorFullArticle();
				if (eProc != null) {
					for (String line : toLines(article, eProc)) {
						builder.append(line + "\n");
					}
				} else {
					builder.append(article.text());
				}

				// Content is too tight with a single break per line:
				fullContent = builder.toString().replace("\n", "\n\n") //
						.replace("\n\n\n\n", "\n\n") //
						.replace("\n\n\n\n", "\n\n") //
						.trim();
			}

			if (fullContent.isEmpty()) {
				fullContent = story.getContent();
			}

			story.setFullContent(fullContent);
			story.setComments(getComments(doc,
					getFullArticleCommentPosts(doc, url)));
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	/**
	 * Return the full article if available.
	 * 
	 * @param doc
	 *            the (full article) document to work on
	 * 
	 * @return the article or NULL
	 */
	abstract protected Element getFullArticle(Document doc);

	/**
	 * Return the list of comment {@link Element}s from this optional container
	 * -- must <b>NOT</b> return the "container" as a comment {@link Element}.
	 * 
	 * @param doc
	 *            the (full article) document to work on
	 * @param intUrl
	 *            the internal {@link URL} this article wa taken from (the
	 *            {@link URL} from the supported website)
	 * 
	 * @return the list of comment posts
	 */
	abstract protected List<Element> getFullArticleCommentPosts(Document doc,
			URL intUrl);

	/**
	 * The {@link ElementProcessor} to use to convert the main article element
	 * (see {@link BasicSupport#getFullArticle(Document)}) into text.
	 * <p>
	 * See {@link BasicElementProcessor} for a working, basic implementation.
	 * <p>
	 * Can be NULL to simply use {@link Element#text()}.
	 * 
	 * @return the processor, or NULL
	 */
	abstract protected ElementProcessor getElementProcessorFullArticle();

	/**
	 * Convert the comment elements into {@link Comment}s
	 * 
	 * @param doc
	 *            the document we work on
	 * @param posts
	 *            the comment elements
	 * 
	 * @return the converted {@link Comment}s
	 */
	private List<Comment> getComments(Document doc, List<Element> posts) {
		List<Comment> comments = new ArrayList<Comment>();
		if (posts != null) {
			for (Element post : posts) {
				String id = getCommentId(post).trim();
				String author = getCommentAuthor(post).trim();
				String title = getCommentTitle(post).trim();
				String date = getCommentDate(post).trim();

				List<String> content = new ArrayList<String>();

				if (id.isEmpty()) {
					id = date;
				}

				date = date(date);

				Element contentE = getCommentContentElement(post);
				if (contentE != null) {
					ElementProcessor eProc = getElementProcessorComment();
					if (eProc != null) {
						for (String line : toLines(contentE, eProc)) {
							content.add(line);
						}
					} else {
						content = Arrays.asList(contentE.text().split("\n"));
					}
				}

				Comment comment = new Comment(id, author, title, date, content);
				comment.addAll(getComments(doc,
						getCommentCommentPosts(doc, post)));

				if (!comment.isEmpty()) {
					comments.add(comment);
				}
			}
		}

		return comments;
	}

	/**
	 * Return the list of subcomment {@link Element}s from this comment element
	 * -- must <b>NOT</b> return the "container" as a comment {@link Element}.
	 * 
	 * @param doc
	 *            the (full article) document to work on
	 * @param container
	 *            the container (a comment {@link Element})
	 * 
	 * @return the list of comment posts
	 */
	abstract protected List<Element> getCommentCommentPosts(Document doc,
			Element container);

	/**
	 * Compute the ID of the given comment element.
	 * 
	 * @param post
	 *            the comment element
	 * 
	 * @return the ID
	 */
	abstract protected String getCommentId(Element post);

	/**
	 * Compute the author of the given comment element.
	 * 
	 * @param post
	 *            the comment element
	 * 
	 * @return the author
	 */
	abstract protected String getCommentAuthor(Element post);

	/**
	 * Compute the title of the given comment element.
	 * 
	 * @param post
	 *            the comment element
	 * 
	 * @return the title
	 */
	abstract protected String getCommentTitle(Element post);

	/**
	 * Compute the date of the given comment element.
	 * 
	 * @param post
	 *            the comment element
	 * 
	 * @return the date
	 */
	abstract protected String getCommentDate(Element post);

	/**
	 * Get the main of the given comment element, which can be NULL.
	 * 
	 * @param post
	 *            the comment element
	 * 
	 * @return the element
	 */
	abstract protected Element getCommentContentElement(Element post);

	/**
	 * The {@link ElementProcessor} to use to convert the main comment element
	 * (see {@link BasicSupport#getCommentContentElement(Element)}) into text.
	 * <p>
	 * See {@link BasicElementProcessor} for a working, basic implementation.
	 * <p>
	 * Can be NULL to simply use {@link Element#text()}.
	 * 
	 * @return the processor
	 */
	abstract protected ElementProcessor getElementProcessorComment();

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
			case PIPEDOT:
				support = new Pipedot();
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
	static protected List<String> toLines(Element element,
			final ElementProcessor elementProcessor) {
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
	static private String date(String date) {
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
		} catch (ParseException e) {
			return date;
		}
	}
}
