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
 * Support <a href="https://www.toolinux.com/">https://www.toolinux.com/</a>.
 * 
 * @author niki
 */
public class TooLinux extends BasicSupport {
	@Override
	public String getDescription() {
		return "TooLinux: Actualité généraliste sur Linux et les logiciels libres";
	}

	@Override
	protected List<Entry<URL, String>> getUrls() throws IOException {
		List<Entry<URL, String>> urls = new ArrayList<Entry<URL, String>>();
		urls.add(new AbstractMap.SimpleEntry<URL, String>(new URL(
				"https://www.toolinux.com/"), ""));
		return urls;
	}

	@Override
	protected List<Element> getArticles(Document doc) {
		return doc.getElementsByClass("hentry");
	}

	@Override
	protected String getArticleId(Document doc, Element article) {
		return ""; // We use the date
	}

	@Override
	protected String getArticleTitle(Document doc, Element article) {
		Element titleElement = article.getElementsByClass("entry-title")
				.first();
		if (titleElement != null) {
			return titleElement.text();
		}

		return "";
	}

	@Override
	protected String getArticleAuthor(Document doc, Element article) {
		return "";
	}

	@Override
	protected String getArticleDate(Document doc, Element article) {
		Element dateElement = article.getElementsByClass("published").first();
		if (dateElement != null) {
			return dateElement.text();
		}

		return "";
	}

	@Override
	protected String getArticleCategory(Document doc, Element article,
			String currentCategory) {
		return "";
	}

	@Override
	protected String getArticleDetails(Document doc, Element article) {
		return "";
	}

	@Override
	protected String getArticleIntUrl(Document doc, Element article) {
		Element urlElement = article.getElementsByTag("a").first();
		if (urlElement != null) {
			return urlElement.absUrl("href");
		}

		return "";
	}

	@Override
	protected String getArticleExtUrl(Document doc, Element article) {
		return "";
	}

	@Override
	protected String getArticleContent(Document doc, Element article) {
		Element content = article.getElementsByClass("introduction").first();
		if (content != null) {
			return content.text();
		}

		return "";
	}

	@Override
	protected Element getFullArticle(Document doc) {
		return doc.getElementById("content");
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
				if ("notes".equals(node.attr("class"))) {
					return true;
				}
				return false;
			}
		};
	}

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
