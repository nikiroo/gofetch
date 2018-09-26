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
		return "7SUR7.be Info, sport et showbiz, 24/24, 7/7";
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
	protected List<Element> getFullArticleCommentPosts(Document doc, URL intUrl) {
		return getSubCommentElements(doc.getElementsByClass("comment-list")
				.first());
	}

	@Override
	protected ElementProcessor getElementProcessorFullArticle() {
		return new BasicElementProcessor() {
			@Override
			public boolean ignoreNode(Node node) {
				return node.attr("class").contains("chapo");
			}

			@Override
			public String isSubtitle(Node node) {
				if (node instanceof Element) {
					Element element = (Element) node;
					if (element.tagName().startsWith("h")
							&& element.tagName().length() == 2) {
						return element.text();
					}
				}
				return null;
			}
		};
	}

	@Override
	protected List<Element> getCommentCommentPosts(Document doc,
			Element container) {
		return getSubCommentElements(container.getElementsByClass("children")
				.first());
	}

	@Override
	protected String getCommentId(Element post) {
		Element idE = post.getElementsByTag("a").first();
		if (idE != null) {
			return idE.attr("id");
		}

		return "";
	}

	@Override
	protected String getCommentAuthor(Element post) {
		// Since we have no title, we switch with author
		return "";
	}

	@Override
	protected String getCommentTitle(Element post) {
		// Since we have no title, we switch with author
		Element authorE = post.getElementsByTag("footer").first();
		if (authorE != null) {
			authorE = authorE.getElementsByTag("cite").first();
		}
		if (authorE != null) {
			return authorE.text();
		}

		return "";
	}

	@Override
	protected String getCommentDate(Element post) {
		Element idE = post.getElementsByTag("a").first();
		if (idE != null) {
			Element dateE = idE.getElementsByTag("span").first();
			if (dateE != null) {
				return dateE.attr("data-epoch");
			}
		}

		return "";
	}

	@Override
	protected Element getCommentContentElement(Element post) {
		Element contentE = post.getElementsByClass("comment-content").first();
		return contentE;
	}

	@Override
	protected ElementProcessor getElementProcessorComment() {
		return new BasicElementProcessor() {
			@Override
			public boolean ignoreNode(Node node) {
				if (node instanceof Element) {
					Element el = (Element) node;
					if ("h4".equals(el.tagName())) {
						return true;
					}
				}

				return false;
			}
		};
	}

	private List<Element> getSubCommentElements(Element posts) {
		List<Element> commentElements = new ArrayList<Element>();
		if (posts != null) {
			for (Element possibleCommentElement : posts.children()) {
				if (possibleCommentElement.hasClass("comment")) {
					commentElements.add(possibleCommentElement);
				}
			}
		}

		return commentElements;
	}
}
