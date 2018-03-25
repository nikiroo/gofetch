package be.nikiroo.gofetch.support;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import be.nikiroo.gofetch.data.Story;
import be.nikiroo.utils.Downloader;

/**
 * Base class for website support.
 * 
 * @author niki
 */
public abstract class BasicSupport {
	/** The downloader to use for all websites. */
	protected static Downloader downloader = new Downloader("gofetcher");

	/**
	 * The support type (each website we support has a single type).
	 * 
	 * @author niki
	 */
	public enum Type {
		/** EN: Any, but mostly IT/Sci */
		SLASHDOT,
		/** EN: Clone of Slashdot, mostly abandoned */
		PIPEDOT,
		/** EN: Linux */
		LWN,
		/** FR: Any */
		LEMONDE,
		/** EN: IT */
		REGISTER,
		/** FR: Linux */
		TOO_LINUX,
		/** FR: IT */
		ERE_NUMERIQUE,
	}

	/**
	 * Used to process an element into lines.
	 * 
	 * @author niki
	 */
	public interface ElementProcessor {
		/**
		 * Detect if this node is a quote and should be trated as such.
		 * 
		 * @param node
		 *            the node to check
		 * @return TRUE if it is
		 */
		public boolean detectQuote(Node node);

		/**
		 * Process text content (will be called on each text element, allowing
		 * you to modify it if needed).
		 * 
		 * @param text
		 *            the text to process
		 * 
		 * @return the resulting text
		 */
		public String processText(String text);

		/**
		 * Ignore this node.
		 * 
		 * @param node
		 *            the node to ignore
		 * @return TRUE if it has to be ignored
		 */
		public boolean ignoreNode(Node node);

		/**
		 * Manually process this node (and return the manual processing value)
		 * if so desired.
		 * <p>
		 * If the node is manually processed, it and its children will not be
		 * automatically processed.
		 * 
		 * @param node
		 *            the node to optionally process
		 * 
		 * @return NULL if not processed (will thus be automatically processed
		 *         as usual), a {@link String} (may be empty) if we process it
		 *         manually -- the given {@link String} will be used instead of
		 *         the usual automatic processing if not NULL
		 */
		public String manualProcessing(Node node);

		/**
		 * This {@link Node} is a subtitle and should be treated as such
		 * (highlighted).
		 * 
		 * @param node
		 *            the node to check
		 * 
		 * @return NULL if it is not a subtitle, the subtitle to use if it is
		 */
		public String isSubtitle(Node node);
	}

	/**
	 * A default {@link ElementProcessor} (will not detect or process anything
	 * manually).
	 * 
	 * @author niki
	 */
	public class BasicElementProcessor implements ElementProcessor {
		@Override
		public boolean detectQuote(Node node) {
			return false;
		}

		@Override
		public String processText(String text) {
			return text;
		}

		@Override
		public boolean ignoreNode(Node node) {
			return false;
		}

		@Override
		public String manualProcessing(Node node) {
			return null;
		}

		@Override
		public String isSubtitle(Node node) {
			return null;
		}
	}

	static private String preselector;

	private Type type;

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
	abstract public List<Story> list() throws IOException;

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
	abstract public void fetch(Story story) throws IOException;

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
	 * Get the first {@link Element} of the given class, or an empty span
	 * {@link Element} if none found.
	 * 
	 * @param element
	 *            the element to look in
	 * @param className
	 *            the class to look for
	 * 
	 * @return the value or an empty span {@link Element}
	 */
	static protected Element firstOrEmpty(Element element, String className) {
		Elements subElements = element.getElementsByClass(className);
		if (subElements.size() > 0) {
			return subElements.get(0);
		}

		return new Element("span");
	}

	/**
	 * Get the first {@link Element} of the given tag, or an empty span
	 * {@link Element} if none found.
	 * 
	 * @param element
	 *            the element to look in
	 * @param tagName
	 *            the tag to look for
	 * 
	 * @return the value or an empty span {@link Element}
	 */
	static protected Element firstOrEmptyTag(Element element, String tagName) {
		Elements subElements = element.getElementsByTag(tagName);
		if (subElements.size() > 0) {
			return subElements.get(0);
		}

		return new Element("span");
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

		for (int i = 0; i < lines.size(); i++) {
			lines.set(i, lines.get(i).replace("  ", " ").trim());
		}

		return lines;
	}

	/**
	 * Reformat the date if possible.
	 * 
	 * @param date
	 *            the input date
	 * 
	 * @return the reformated date, or the same value if it was not parsable
	 */
	static protected String date(String date) {
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
