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
 * Support <a href="https://www.7sur7.be/">https://www.7sur7.be/</a>.
 * 
 * @author niki
 */
public class SeptSurSept extends BasicSupport {
	@Override
	public String getDescription() {
		return "7sur7.be: Info, sport et showbiz, 24/24, 7/7";
	}

	@Override
	public void login() throws IOException {
		addCookie("pwrefr2", "");
		addCookie("pwv-atXMVFeyFP1Ki09i", "1");
		addCookie("pwg-atXMVFeyFP1Ki09i", "basic");

		addCookie("pwv", "1");
		addCookie("pw", "functional");

		URL url = new URL("https://www.7sur7.be/7s7/privacy/callback.do"
				+ "?redirectUri=/" + "&pwv=1" + "&pws=functional%7Canalytics"
				+ "&days=3650" + "&referrer=");

		open(url).close();
	}

	@Override
	protected List<Entry<URL, String>> getUrls() throws IOException {
		List<Entry<URL, String>> urls = new ArrayList<Entry<URL, String>>();
		URL url = new URL("https://www.7sur7.be/");
		urls.add(new AbstractMap.SimpleEntry<URL, String>(url, ""));

		return urls;
	}

	@Override
	protected List<Element> getArticles(Document doc) {
		return doc.getElementsByClass("clip");
	}

	@Override
	protected String getArticleId(Document doc, Element article) {
		String id = article.attr("id");
		if (id != null && id.startsWith("clip")) {
			return id.substring("clip".length());
		}

		return null;
	}

	@Override
	protected String getArticleTitle(Document doc, Element article) {
		return article.attr("data-title");
	}

	@Override
	protected String getArticleAuthor(Document doc, Element article) {
		return "";
	}

	@Override
	protected String getArticleDate(Document doc, Element article) {
		return article.attr("data-date");
	}

	@Override
	protected String getArticleCategory(Document doc, Element article,
			String currentCategory) {
		Element parent = article.parent();
		if (parent != null) {
			Element catElement = parent.previousElementSibling();
			if (catElement != null) {
				return catElement.text();
			}
		}

		return "";
	}

	@Override
	protected String getArticleDetails(Document doc, Element article) {
		return "";
	}

	@Override
	protected String getArticleIntUrl(Document doc, Element article) {
		return article.absUrl("href");
	}

	@Override
	protected String getArticleExtUrl(Document doc, Element article) {
		return "";
	}

	@Override
	protected String getArticleContent(Document doc, Element article) {
		return article.attr("data-intro").trim();
	}

	@Override
	protected Element getFullArticle(Document doc) {
		return doc.getElementById("detail_content");
	}

	@Override
	protected ElementProcessor getElementProcessorFullArticle() {
		return new BasicElementProcessor() {
			@Override
			public boolean ignoreNode(Node node) {
				return node.attr("class").equals("read_more")
						|| "teas_emopoll".equals(node.attr("id"))
						|| "teas_emopoll_facebook".equals(node.attr("id"))
						|| "soc_tools".equals(node.attr("id"));
			}

			@Override
			public String isSubtitle(Node node) {
				if (node instanceof Element) {
					Element element = (Element) node;
					if (element.tagName().equals("strong")) {
						return element.text();
					}
				}
				return null;
			}
		};
	}

	@Override
	protected List<Element> getFullArticleCommentPosts(Document doc, URL intUrl) {
		return null;
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
