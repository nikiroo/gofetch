package be.nikiroo.gofetch.support;

import java.io.IOException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import be.nikiroo.gofetch.data.Story;

/**
 * Support <a href="https://www.7sur7.be/">https://www.7sur7.be/</a>.
 * 
 * @author niki
 */
public class SeptSurSept extends BasicSupport {
	static final int MAX_ID_SIZE = 40;

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
	BasicSnippetExtractor getSnippetExtractor() {
		return new BasicSnippetExtractor() {
			@Override
			protected List<Element> getSnippets(Document doc) {
				return doc.getElementsByTag("article");
			}

			@Override
			protected String getArticleId(Document doc, Element article) {
				Element el = article.getElementsByTag("a").first();
				if (el != null) {
					String href = el.absUrl("href");
					if (href.endsWith("/"))
						href = href.substring(0, href.length() - 1);
					String tab[] = href.split("/");
					String id = tab[tab.length - 1];
					if (id.length() > MAX_ID_SIZE)
						id = id.substring(0, MAX_ID_SIZE);
					return id;
				}

				return null;
			}

			@Override
			String getArticleIntUrl(Document doc, Element article) {
				Element el = article.getElementsByTag("a").first();
				if (el != null)
					return el.absUrl("href");
				return null;
			}
		};
	}

	@Override
	BasicFullArticleExtractor getFullArticleExtractor() {
		return new BasicFullArticleExtractor() {
			@Override
			protected Element getFullArticle(Document doc) {
				return doc.getElementsByClass("article__wrapper").first();
			}

			@Override
			protected BasicElementProcessor getElementProcessorFullArticle() {
				return new BasicElementProcessor() {
					@Override
					public boolean ignoreNode(Node node) {
						String clazz = node.attr("class");

						return clazz.contains("article__meta") //
								|| clazz.contains("article__title") //
								|| clazz.contains("sharing") //
								|| clazz.contains("advertisement") //
								|| clazz.contains("mail-a-friend") //
								|| clazz.contains("article-login-gate") //
								|| clazz.contains("snippet") //
						;
					}

					@Override
					public String isSubtitle(Node node) {
						if (node instanceof Element) {
							Element element = (Element) node;

							String tag = element.tagName();
							if (tag.equals("strong") //
									|| tag.equals("h2")) {
								return element.text();
							}

							String clazz = element.attr("class");
							if (clazz.contains("article__intro")) {
								String text = element.text();
								if (text.startsWith("Mise à jour")) {
									text = text.substring("Mise à jour"
											.length());
								}
								return text;
							}

						}

						return null;
					}
				};
			}

			@Override
			protected void ready(Story story, Document doc, Element el) {
				String title = "";
				String author = "";
				final String date[] = new String[] { "" };

				Element titleEl = doc.getElementsByClass("article__title")
						.first();
				if (titleEl != null)
					title = titleEl.text();

				Iterator<Element> iter = doc.getElementsByTag("meta")
						.iterator();
				while (iter.hasNext()) {
					Element metaEl = iter.next();
					String item = metaEl.attr("itemprop");
					if (item != null && item.equals("datePublished")) {
						String d = metaEl.attr("content");
						if (d.length() >= 18) {
							d = d.substring(0, 10) + "_" + d.substring(11, 19);
						}
						d = d.replace(":", "-");
						date[0] = d;
					}
				}

				Element authorEl = doc.getElementsByClass(
						"article__credit-source").first();
				if (authorEl != null)
					author = authorEl.text();

				story.setTitle(title);
				story.setId(date[0] + "_" + story.getId());
				story.setDate(date[0]);
				story.setAuthor(author);

				super.ready(story, doc, el);
			}
		};
	}
}
