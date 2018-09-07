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
import org.jsoup.select.Elements;

/**
 * Support <a href='https://slashdot.org/'>https://slashdot.org/</a>.
 * 
 * @author niki
 */
public class Slashdot extends BasicSupport {
	@Override
	public String getDescription() {
		return "Slashdot: News for nerds, stuff that matters!";
	}

	@Override
	protected List<Entry<URL, String>> getUrls() throws IOException {
		List<Entry<URL, String>> urls = new ArrayList<Entry<URL, String>>();
		urls.add(new AbstractMap.SimpleEntry<URL, String>(new URL(
				"https://slashdot.org/"), ""));
		return urls;
	}

	@Override
	protected List<Element> getArticles(Document doc) {
		return doc.getElementsByTag("header");
	}

	@Override
	protected String getArticleId(Document doc, Element article) {
		Element title = article.getElementsByClass("story-title").first();
		if (title != null) {
			String id = title.attr("id");
			if (id.startsWith("title-")) {
				id = id.substring("title-".length());
			}

			while (id.length() < 10) {
				id = "0" + id;
			}

			return id;
		}

		return "";
	}

	@Override
	protected String getArticleTitle(Document doc, Element article) {
		Element title = article.getElementsByClass("story-title").first();
		if (title != null) {
			return title.text();
		}

		return "";
	}

	@Override
	protected String getArticleAuthor(Document doc, Element article) {
		// details: "Posted by AUTHOR on DATE from the further-crackdown dept."
		String details = getArticleDetailsReal(article);
		int pos = details.indexOf(" on ");
		if (details.startsWith("Posted by ") && pos >= 0) {
			return details.substring("Posted by ".length(), pos).trim();
		}

		return "";
	}

	@Override
	protected String getArticleDate(Document doc, Element article) {
		// Do not try bad articles
		if (getArticleId(doc, article).isEmpty()) {
			return "";
		}

		Element dateElement = doc.getElementsByTag("time").first();
		if (dateElement != null) {
			String date = dateElement.text().trim();
			if (date.startsWith("on ")) {
				date = date.substring("on ".length());
			}

			return date;
		}

		return "";
	}

	@Override
	protected String getArticleCategory(Document doc, Element article,
			String currentCategory) {
		Element categElement = doc.getElementsByClass("topic").first();
		if (categElement != null) {
			return categElement.text();
		}

		return "";
	}

	@Override
	protected String getArticleDetails(Document doc, Element article) {
		// details: "Posted by AUTHOR on DATE from the further-crackdown dept."
		String details = getArticleDetailsReal(article);
		int pos = details.indexOf(" from the ");
		if (pos >= 0) {
			return details.substring(pos).trim();
		}

		return "";
	}

	@Override
	protected String getArticleIntUrl(Document doc, Element article) {
		Element title = article.getElementsByClass("story-title").first();
		if (title != null) {
			Elements links = title.getElementsByTag("a");
			if (links.size() > 0) {
				return links.get(0).absUrl("href");
			}
		}
		return "";
	}

	@Override
	protected String getArticleExtUrl(Document doc, Element article) {
		Element title = article.getElementsByClass("story-title").first();
		if (title != null) {
			Elements links = title.getElementsByTag("a");
			if (links.size() > 1) {
				return links.get(1).absUrl("href");
			}
		}
		return "";
	}

	@Override
	protected String getArticleContent(Document doc, Element article) {
		Element contentElement = doc //
				.getElementById("text-" + getArticleId(doc, article));
		if (contentElement != null) {
			return contentElement.text();
		}

		return "";
	}

	@Override
	protected Element getFullArticle(Document doc) {
		return null;
	}

	@Override
	protected List<Element> getFullArticleCommentPosts(Document doc, URL intUrl) {
		List<Element> commentElements = new ArrayList<Element>();
		Element listing = doc.getElementById("commentlisting");
		if (listing != null) {
			for (Element commentElement : listing.children()) {
				if (commentElement.hasClass("comment")) {
					commentElements.add(commentElement);
				}
			}
		}

		return commentElements;
	}

	@Override
	protected ElementProcessor getElementProcessorFullArticle() {
		return null;
	}

	@Override
	protected List<Element> getCommentCommentPosts(Document doc,
			Element container) {
		List<Element> commentElements = new ArrayList<Element>();
		for (Element child : container.children()) {
			if (child.id().contains("commtree_")) {
				for (Element sub : child.children()) {
					if (sub.hasClass("comment")) {
						commentElements.add(sub);
					}
				}
			}
		}

		return commentElements;
	}

	@Override
	protected String getCommentId(Element post) {
		if (post.hasClass("hidden")) {
			return "";
		}

		return post.id();
	}

	@Override
	protected String getCommentAuthor(Element post) {
		if (post.hasClass("hidden")) {
			return "";
		}

		Element author = post.getElementsByClass("by").first();
		if (author != null) {
			return author.text();
		}

		return "";
	}

	@Override
	protected String getCommentTitle(Element post) {
		if (post.hasClass("hidden")) {
			return "";
		}

		Element title = post.getElementsByClass("title").first();
		if (title != null) {
			return title.text();
		}

		return "";
	}

	@Override
	protected String getCommentDate(Element post) {
		if (post.hasClass("hidden")) {
			return "";
		}

		Element date = post.getElementsByClass("otherdetails").first();
		if (date != null) {
			return date.text();
		}

		return "";
	}

	@Override
	protected Element getCommentContentElement(Element post) {
		if (post.hasClass("hidden")) {
			return null;
		}

		return post.getElementsByClass("commentBody").first();
	}

	@Override
	protected ElementProcessor getElementProcessorComment() {
		return new BasicElementProcessor() {
			@Override
			public String processText(String text) {
				while (text.startsWith(">")) { // comment in one-liners
					text = text.substring(1).trim();
				}

				return text;
			}

			@Override
			public boolean detectQuote(Node node) {
				if (node instanceof Element) {
					Element elementNode = (Element) node;
					if (elementNode.tagName().equals("blockquote")
							|| elementNode.hasClass("quote")
							|| (elementNode.tagName().equals("p")
									&& elementNode.textNodes().size() == 1 && elementNode
									.textNodes().get(0).getWholeText()
									.startsWith(">"))) {
						return true;
					}
				}

				return false;
			}
		};
	}

	private String getArticleDetailsReal(Element article) {
		Element detailsElement = article.getElementsByClass("details").first();
		if (detailsElement != null) {
			return detailsElement.text();
		}

		return "";
	}
}
