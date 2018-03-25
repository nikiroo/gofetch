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
 * Support <a href='https://pipedot.org/'>https://pipedot.org/</a>.
 * 
 * @author niki
 */
public class Pipedot extends BasicSupport {
	@Override
	public String getDescription() {
		return "Pipedot: News for nerds, without the corporate slant";
	}

	@Override
	protected List<Entry<URL, String>> getUrls() throws IOException {
		List<Entry<URL, String>> urls = new ArrayList<Entry<URL, String>>();
		urls.add(new AbstractMap.SimpleEntry<URL, String>(new URL(
				"https://pipedot.org/"), ""));
		return urls;
	}

	@Override
	protected List<Element> getArticles(Document doc) {
		return doc.getElementsByClass("story");
	}

	@Override
	protected String getArticleId(Document doc, Element article) {
		// Don't try on bad articles
		if (getArticleTitle(doc, article).isEmpty()) {
			return "";
		}

		for (Element idElem : article.getElementsByTag("a")) {
			if (idElem.attr("href").startsWith("/pipe/")) {
				return idElem.attr("href").substring("/pipe/".length());
			}
		}

		return "";
	}

	@Override
	protected String getArticleTitle(Document doc, Element article) {
		Element title = article.getElementsByTag("h1").first();
		if (title != null) {
			return title.text();
		}

		return "";
	}

	@Override
	protected String getArticleAuthor(Document doc, Element article) {
		String value = getArticleDetailsReal(article);
		int pos = value.indexOf("by ");
		if (pos >= 0) {
			value = value.substring(pos + "by ".length()).trim();
			pos = value.indexOf(" in ");
			if (pos >= 0) {
				value = value.substring(0, pos).trim();
			}

			return value;
		}

		return "";
	}

	@Override
	protected String getArticleDate(Document doc, Element article) {
		Element dateElement = article.getElementsByTag("time").first();
		if (dateElement != null) {
			return dateElement.attr("datetime");
		}

		return "";
	}

	@Override
	protected String getArticleCategory(Document doc, Element article,
			String currentCategory) {
		String value = getArticleDetailsReal(article);
		int pos = value.indexOf(" in ");
		if (pos >= 0) {
			value = value.substring(pos + " in ".length()).trim();
			pos = value.indexOf(" on ");
			if (pos >= 0) {
				value = value.substring(0, pos).trim();
			}

			return value;
		}

		return "";
	}

	@Override
	protected String getArticleDetails(Document doc, Element article) {
		return ""; // We alrady extracted all the info
	}

	@Override
	protected String getArticleIntUrl(Document doc, Element article) {
		Element link = article.getElementsByTag("a").first();
		if (link != null) {
			return link.absUrl("href");
		}

		return "";
	}

	@Override
	protected String getArticleExtUrl(Document doc, Element article) {
		Element link = article.getElementsByTag("a").first();
		if (link != null) {
			String possibleExtLink = link.absUrl("href").trim();
			if (!possibleExtLink.isEmpty()
					&& !possibleExtLink.contains("pipedot.org/")) {
				return possibleExtLink;
			}
		}

		return "";
	}

	@Override
	protected String getArticleContent(Document doc, Element article) {
		for (Element elem : article.children()) {
			String tag = elem.tagName();
			if (!tag.equals("header") && !tag.equals("footer")) {
				return elem.text();
			}
		}

		return "";
	}

	@Override
	protected Element getFullArticle(Document doc) {
		return null;
	}

	@Override
	protected List<Element> getFullArticleCommentPosts(Document doc, URL intUrl) {
		return getCommentElements(doc.getElementsByTag("main").first());
	}

	@Override
	protected ElementProcessor getElementProcessorFullArticle() {
		return new BasicElementProcessor();
	}

	@Override
	protected List<Element> getCommentCommentPosts(Document doc,
			Element container) {

		if (container != null) {
			container = container.getElementsByClass("comment-outline").first();
		}

		return getCommentElements(container);
	}

	@Override
	protected String getCommentId(Element post) {
		return post.id();
	}

	@Override
	protected String getCommentAuthor(Element post) {
		Element authorDateE = post.getElementsByTag("h3").first();
		if (authorDateE != null) {
			String authorDate = authorDateE.text();
			int pos = authorDate.lastIndexOf(" on ");
			if (pos >= 0) {
				return authorDate.substring(0, pos).trim();
			}
		}

		return "";
	}

	@Override
	protected String getCommentTitle(Element post) {
		Element title = post.getElementsByTag("h3").first();
		if (title != null) {
			return title.text();
		}

		return "";
	}

	@Override
	protected String getCommentDate(Element post) {
		Element authorDateE = post.getElementsByTag("h3").first();
		if (authorDateE != null) {
			String authorDate = authorDateE.text();
			int pos = authorDate.lastIndexOf(" on ");
			if (pos >= 0) {
				return authorDate.substring(pos + " on ".length()).trim();
			}
		}

		return "";
	}

	@Override
	protected Element getCommentContentElement(Element post) {
		return post.getElementsByClass("comment-body").first();
	}

	@Override
	protected ElementProcessor getElementProcessorComment() {
		return new BasicElementProcessor() {
			@Override
			public boolean detectQuote(Node node) {
				if (node instanceof Element) {
					Element elementNode = (Element) node;
					if (elementNode.tagName().equals("blockquote")
							|| elementNode.hasClass("quote")) {
						return true;
					}
				}

				return false;
			}
		};
	}

	private String getArticleDetailsReal(Element article) {
		Elements detailsElements = article.getElementsByTag("div");
		if (detailsElements.size() > 0) {
			return detailsElements.get(0).text().trim();
		}

		return "";
	}

	private List<Element> getCommentElements(Element container) {
		List<Element> commentElements = new ArrayList<Element>();
		if (container != null) {
			for (Element commentElement : container.children()) {
				if (commentElement.hasClass("comment")) {
					commentElements.add(commentElement);
				}
			}
		}
		return commentElements;
	}
}
