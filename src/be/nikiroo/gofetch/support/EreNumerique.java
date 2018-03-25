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
 * Support <a
 * href="https://www.erenumerique.fr/">https://www.erenumerique.fr/</a>.
 * 
 * @author niki
 */
public class EreNumerique extends BasicSupport {
	@Override
	public String getDescription() {
		return "Ère Numérique.FR: faites le bon choix !";
	}

	@Override
	protected List<Entry<URL, String>> getUrls() throws IOException {
		List<Entry<URL, String>> urls = new ArrayList<Entry<URL, String>>();
		for (String categ : new String[] { "Informatique" }) {
			URL url = new URL("https://www.erenumerique.fr/"
					+ categ.toLowerCase());
			urls.add(new AbstractMap.SimpleEntry<URL, String>(url, categ));
		}

		return urls;
	}

	@Override
	protected List<Element> getArticles(Document doc) {
		return doc.getElementsByClass("item-details");
	}

	@Override
	protected String getArticleId(Document doc, Element article) {
		return ""; // will use the date
	}

	@Override
	protected String getArticleTitle(Document doc, Element article) {
		Element titleElement = article.getElementsByTag("h2").first();
		if (titleElement != null) {
			return titleElement.text();
		}

		return "";
	}

	@Override
	protected String getArticleAuthor(Document doc, Element article) {
		Element authorElement = article.getElementsByClass(
				"td-post-author-name").first();
		if (authorElement != null) {
			authorElement = authorElement.getElementsByTag("a").first();
		}
		if (authorElement != null) {
			return authorElement.text();
		}

		return "";
	}

	@Override
	protected String getArticleDate(Document doc, Element article) {
		Element dateElement = article //
				.getElementsByTag("time").first();
		if (dateElement != null) {
			return dateElement.attr("datetime");
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
		Element contentElement = article.getElementsByClass("td-excerpt")
				.first();
		if (contentElement != null) {
			return contentElement.text();
		}

		return "";
	}

	@Override
	protected Element getFullArticle(Document doc) {
		Element article = doc.getElementsByTag("article").first();
		if (article != null) {
			article = article.getElementsByAttributeValue("itemprop",
					"articleBody").first();
		}

		return article;
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
