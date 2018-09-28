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

/**
 * Support <a href="http://www.lemonde.fr/">http://www.lemonde.fr/</a>.
 * 
 * @author niki
 */
public class LeMonde extends BasicSupport {
	@Override
	public String getDescription() {
		return "Le Monde: Actualités et Infos en France et dans le monde";
	}

	@Override
	protected List<Entry<URL, String>> getUrls() throws IOException {
		List<Entry<URL, String>> urls = new ArrayList<Entry<URL, String>>();
		for (String topic : new String[] { "International", "Politique",
				"Société", "Sciences" }) {
			URL url = new URL("http://www.lemonde.fr/"
					+ topic.toLowerCase().replace("é", "e") + "/1.html");
			urls.add(new AbstractMap.SimpleEntry<URL, String>(url, topic));
		}

		return urls;
	}

	@Override
	protected List<Element> getArticles(Document doc) {
		return doc.getElementsByTag("article");
	}

	@Override
	protected String getArticleId(Document doc, Element article) {
		return ""; // will use the date
	}

	@Override
	protected String getArticleTitle(Document doc, Element article) {
		Element titleElement = article.getElementsByTag("h3").first();
		if (titleElement != null) {
			return titleElement.text();
		}

		return "";
	}

	@Override
	protected String getArticleAuthor(Document doc, Element article) {
		Element detailsElement = article.getElementsByClass("signature")
				.first();
		if (detailsElement != null) {
			return detailsElement.text();
		}

		return "";
	}

	@Override
	protected String getArticleDate(Document doc, Element article) {
		Element timeElement = article.getElementsByTag("time").first();
		if (timeElement != null) {
			return timeElement.attr("datetime");
		}

		return "";
	}

	@Override
	protected String getArticleCategory(Document doc, Element article,
			String currentCategory) {
		return currentCategory;
	}

	@Override
	protected String getArticleDetails(Document doc, Element article) {
		return "";
	}

	@Override
	protected String getArticleIntUrl(Document doc, Element article) {
		Element titleElement = article.getElementsByTag("h3").first();
		if (titleElement != null) {
			Element link = titleElement.getElementsByTag("a").first();
			if (link != null) {
				return link.absUrl("href");
			}
		}

		return "";
	}

	@Override
	protected String getArticleExtUrl(Document doc, Element article) {
		return "";
	}

	@Override
	protected String getArticleContent(Document doc, Element article) {
		Element contentElement = article.getElementsByClass("txt3").first();
		if (contentElement != null) {
			return getArticleText(contentElement);
		}

		return "";
	}

	@Override
	protected Element getFullArticle(Document doc) {
		return doc.getElementById("articleBody");
	}

	@Override
	protected List<Element> getFullArticleCommentPosts(Document doc, URL intUrl) {
		return null;
	}

	@Override
	protected ElementProcessor getElementProcessorFullArticle() {
		return new BasicElementProcessor() {
			@Override
			public boolean ignoreNode(Node node) {
				if (node instanceof Element) {
					Element element = (Element) node;
					if (element.hasClass("lire")) {
						return true;
					}
				}

				return false;
			}

			@Override
			public String isSubtitle(Node node) {
				if (node instanceof Element) {
					Element element = (Element) node;
					if (element.hasClass("intertitre")) {
						return element.text();
					}
				}
				return null;
			}
		};
	}

	// No comment on this site, horrible javascript system

	@Override
	protected List<Element> getCommentCommentPosts(Document doc,
			Element container) {
		return null;
	}

	@Override
	protected String getCommentId(Element post) {
		return null;
	}

	@Override
	protected String getCommentAuthor(Element post) {
		return null;
	}

	@Override
	protected String getCommentTitle(Element post) {
		return null;
	}

	@Override
	protected String getCommentDate(Element post) {
		return null;
	}

	@Override
	protected Element getCommentContentElement(Element post) {
		return null;
	}

	@Override
	protected ElementProcessor getElementProcessorComment() {
		return null;
	}
}
