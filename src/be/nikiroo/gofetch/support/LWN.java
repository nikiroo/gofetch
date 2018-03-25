package be.nikiroo.gofetch.support;

import java.io.IOException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import be.nikiroo.gofetch.data.Comment;
import be.nikiroo.gofetch.data.Story;

/**
 * Support <a href='https://lwn.net/'>https://lwn.net/</a>.
 * 
 * @author niki
 */
public class LWN extends BasicSupport {
	@Override
	public String getDescription() {
		return "LWN: Linux Weekly Newsletter";
	}

	@Override
	public void fetch(Story story) throws IOException {
		// Do not try the paid-for stories...
		if (!story.getTitle().startsWith("[$]")) {
			super.fetch(story);
		} else {
			String fullContent = "[$] Sorry, this article is currently available to LWN suscribers only [https://lwn.net/subscribe/].";
			story.setFullContent(fullContent);
			story.setComments(new ArrayList<Comment>());
		}
	}

	@Override
	protected List<Entry<URL, String>> getUrls() throws IOException {
		List<Entry<URL, String>> urls = new ArrayList<Entry<URL, String>>();
		urls.add(new AbstractMap.SimpleEntry<URL, String>(new URL(
				"https://lwn.net/"), ""));
		return urls;
	}

	@Override
	protected List<Element> getArticles(Document doc) {
		return doc.getElementsByClass("pure-u-1");
	}

	@Override
	protected String getArticleId(Document doc, Element article) {
		return getArticleIntUrl(doc, article).replaceAll("[^0-9]", "");
	}

	@Override
	protected String getArticleTitle(Document doc, Element article) {
		Element title = article.getElementsByClass("Headline").first();
		if (title != null) {
			return title.text();
		}

		return "";
	}

	@Override
	protected String getArticleAuthor(Document doc, Element article) {
		String author = "";
		String details = getArticleDetailsReal(article);
		int pos = details.indexOf(" by ");
		if (pos >= 0) {
			author = details.substring(pos + " by ".length()).trim();
		}

		return author;
	}

	@Override
	protected String getArticleDate(Document doc, Element article) {
		String date = "";
		String details = getArticleDetailsReal(article);
		int pos = details.indexOf(" Posted ");
		if (pos >= 0) {
			date = details.substring(pos + " Posted ".length()).trim();
			pos = date.indexOf(" by ");
			if (pos >= 0) {
				date = date.substring(0, pos).trim();
			}
		}

		return date;
	}

	@Override
	protected String getArticleCategory(Document doc, Element article,
			String currentCategory) {
		String categ = "";
		String details = getArticleDetailsReal(article);
		int pos = details.indexOf("]");
		if (pos >= 0) {
			categ = details.substring(1, pos).trim();
		}

		return categ;
	}

	@Override
	protected String getArticleDetails(Document doc, Element article) {
		return ""; // We actually extract all the values
	}

	@Override
	protected String getArticleIntUrl(Document doc, Element article) {
		String intUrl = "";
		for (Element idElem : article.getElementsByTag("a")) {
			// Last link is the story link
			intUrl = idElem.absUrl("href");
			int pos = intUrl.indexOf("#Comments");
			if (pos >= 0) {
				intUrl = intUrl.substring(0, pos - 1);
			}
		}

		return intUrl;
	}

	@Override
	protected String getArticleExtUrl(Document doc, Element article) {
		return "";
	}

	@Override
	protected String getArticleContent(Document doc, Element article) {
		Element listing = article.getElementsByClass("BlurbListing").first();
		if (listing != null && listing.children().size() >= 2) {
			String content = "";

			// All but the first and two last children
			for (int i = 1; i < listing.children().size() - 2; i++) {
				Element e = listing.children().get(i);
				content = content.trim() + " " + e.text().trim();
			}

			return content;
		}

		return "";
	}

	@Override
	protected Element getFullArticle(Document doc) {
		return doc.getElementsByClass("ArticleText").first();
	}

	@Override
	protected List<Element> getFullArticleCommentPosts(Document doc, URL intUrl) {
		return doc.getElementsByClass("lwn-u-1");
	}

	@Override
	protected ElementProcessor getElementProcessorFullArticle() {
		return new BasicElementProcessor() {
			@Override
			public boolean ignoreNode(Node node) {
				if (node instanceof Element) {
					Element el = (Element) node;
					if ("Log in".equals(el.text().trim())) {
						return true;
					}
				} else if (node instanceof TextNode) {
					TextNode text = (TextNode) node;
					String t = text.text().trim();
					if (t.equals("(") || t.equals("to post comments)")) {
						return true;
					}
				}

				return false;
			}
		};
	}

	@Override
	protected List<Element> getCommentCommentPosts(Document doc,
			Element container) {
		List<Element> commentElements = new ArrayList<Element>();
		if (container != null) {
			for (Element possibleCommentElement : container.children()) {
				if (possibleCommentElement.hasClass("CommentBox")) {
					commentElements.add(possibleCommentElement);
				} else if (possibleCommentElement.hasClass("Comment")) {
					commentElements.add(possibleCommentElement);
				}
			}
		}

		return commentElements;
	}

	@Override
	protected String getCommentId(Element post) {
		return post.id();
	}

	@Override
	protected String getCommentAuthor(Element post) {
		Element detailsE = post.getElementsByClass("CommentPoster").first();
		if (detailsE != null) {
			String details = detailsE.text();

			int pos = details.lastIndexOf(" by ");
			if (pos >= 0) {
				details = details.substring(pos + " by ".length()).trim();

				if (details.startsWith("Posted ")) {
					return details.substring("Posted ".length()).trim();
				}
			}
		}

		return "";
	}

	@Override
	protected String getCommentTitle(Element post) {
		Element title = post.getElementsByClass("CommentTitle").first();
		if (title != null) {
			return title.text();
		}

		return "";
	}

	@Override
	protected String getCommentDate(Element post) {
		Element detailsE = post.getElementsByClass("CommentPoster").first();
		if (detailsE != null) {
			String details = detailsE.text();

			int pos = details.lastIndexOf(" by ");
			if (pos >= 0) {
				return details.substring(0, pos).trim();
			}
		}

		return "";
	}

	@Override
	protected Element getCommentContentElement(Element post) {
		return post.getElementsByClass("CommentBody").first();
	}

	@Override
	protected ElementProcessor getElementProcessorComment() {
		return new BasicElementProcessor() {
			@Override
			public String processText(String text) {
				while (text.startsWith(">")) { // comments
					text = text.substring(1).trim();
				}

				return text;
			}

			@Override
			public boolean detectQuote(Node node) {
				if (node instanceof Element) {
					Element elementNode = (Element) node;
					if (elementNode.tagName().equals("blockquote")
							|| elementNode.hasClass("QuotedText")) {
						return true;
					}
				}

				return false;
			}

			@Override
			public boolean ignoreNode(Node node) {
				if (node instanceof Element) {
					Element elementNode = (Element) node;
					if (elementNode.hasClass("CommentPoster")) {
						return true;
					}
				}

				return false;
			}
		};
	}

	private String getArticleDetailsReal(Element article) {
		Element listing = article.getElementsByClass("BlurbListing").first();
		// Valid articles have 2+ listings
		if (listing != null && listing.children().size() >= 2) {
			return listing.children().get(0).text();
		}

		return "";
	}
}
