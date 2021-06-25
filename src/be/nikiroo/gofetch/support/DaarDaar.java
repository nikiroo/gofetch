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

import be.nikiroo.gofetch.data.Story;

/**
 * Support <a href="https://daardaar.be/">https://daardaar.be/</a>.
 * 
 * @author niki
 */
public class DaarDaar extends BasicSupport {

	@Override
	public String getDescription() {
		return "DaarDaar: Le meilleur de la presse flamande en français";
	}

	@Override
	protected List<Entry<URL, String>> getUrls() throws IOException {
		List<Entry<URL, String>> urls = new ArrayList<Entry<URL, String>>();
		urls.add(new AbstractMap.SimpleEntry<URL, String>(new URL(
				"https://daardaar.be/rubriques/"), "Information"));
		return urls;
	}

	@Override
	protected List<Element> getArticles(Document doc) {
		return doc.getElementsByTag("article");
	}

	@Override
	protected String getArticleId(Document doc, Element article) {
		Element img = article.getElementsByTag("img").first();
		if (img != null) {
			String src = img.absUrl("src");
			if (src != null) {
				String tab[] = src.split("/");
				String name = tab[tab.length - 1];
				String month = tab[tab.length - 2];
				String year = tab[tab.length - 3];

				name = name.split("\\?")[0];
				name = name.split("\\.")[0];

				return year + "-" + month + "_" + name;
			}
		}

		return ""; // oops.
	}

	@Override
	protected String getArticleTitle(Document doc, Element article) {
		Element titleElement = article.getElementsByTag("p").first();
		if (titleElement != null) {
			return titleElement.text();
		}

		return "";
	}

	@Override
	protected String getArticleAuthor(Document doc, Element article) {
		Element el = article.getElementsByClass("journal").first();
		if (el != null) {
			el = el.getElementsByTag("img").first();
			if (el != null) {
				return "" + el.attr("alt");
			}
		}

		return "";
	}

	@Override
	protected String getArticleDate(Document doc, Element article) {
		String yyymm = getArticleId(doc, article).split("_")[0];
		if (!yyymm.isEmpty()) {
			yyymm = yyymm + "-01";
		}

		return yyymm;
	}

	@Override
	protected String getArticleCategory(Document doc, Element article,
			String currentCategory) {
		Element el = article.getElementsByClass("cat").first();
		if (el != null) {
			return el.text();
		}

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
		return "";
	}

	@Override
	protected Element getFullArticle(Document doc) {
		return doc.getElementsByClass("article__body").first();
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
				String clazz = node.attr("class");
				if (clazz.contains("sharedaddy"))
					return true;

				return false;
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
		return null;
	}

	@Override
	protected String getCommentId(Element post) {
		return "";
	}

	@Override
	protected String getCommentAuthor(Element post) {
		return "";
	}

	@Override
	protected String getCommentTitle(Element post) {
		return "";
	}

	@Override
	protected String getCommentDate(Element post) {
		return "";
	}

	@Override
	protected Element getCommentContentElement(Element post) {
		return null;
	}

	@Override
	protected ElementProcessor getElementProcessorComment() {
		return null;
	}

	@Override
	protected void ready(Story story, Document doc, Element el) {
		Element original = doc.getElementsByClass("art_original").first();
		if (original != null) {
			story.setUrlExternal(original.absUrl("href"));
		}
		super.ready(story, doc, el);
	}
}
