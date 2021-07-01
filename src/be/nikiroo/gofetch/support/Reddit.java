package be.nikiroo.gofetch.support;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import be.nikiroo.gofetch.data.Comment;
import be.nikiroo.gofetch.data.Story;

/**
 * Support <a href="https://www.reddit.com/">https://www.reddit.com/</a>.
 * 
 * @author niki
 */
public class Reddit extends BasicSupport {
	@Override
	public String getDescription() {
		return "Reddit: The front page of the internet";
	}

	@Override
	protected List<Entry<URL, String>> getUrls() throws IOException {
		List<Entry<URL, String>> urls = new ArrayList<Entry<URL, String>>();
		String base = "https://www.reddit.com/r/";
		urls.add(new AbstractMap.SimpleEntry<URL, String>(new URL(base
				+ "linux_gaming" + "/new/"), "linux_gaming"));

		return urls;
	}

	@Override
	protected List<Element> getArticles(Document doc) {
		List<Element> list = doc.getElementsByClass("thing");
		if (list.isEmpty()) {
			list = doc.getElementsByClass("Post");
		}
		if (list.isEmpty()) {
			list = doc.getElementsByClass("scrollerItem");
		}

		return list;
	}

	@Override
	protected String getArticleId(Document doc, Element article) {
		String date = getArticleDate(doc, article);
		String title = getArticleTitle(doc, article);

		String id = (date + "_" + title).replaceAll("[^a-zA-Z0-9_-]", "_");
		if (id.length() > 40) {
			id = id.substring(0, 40);
		}

		return id;
	}

	@Override
	protected String getArticleTitle(Document doc, Element article) {
		Elements els = article.getElementsByAttributeValue("data-event-action",
				"title");
		if (els == null || els.isEmpty()) {
			els = article.getElementsByTag("h2");
		}

		return els.first().text().trim();
	}

	@Override
	protected String getArticleAuthor(Document doc, Element article) {
		String user = article
				.getElementsByAttributeValueStarting("href", "/user/").text()
				.trim();
		if (user.startsWith("/u"))
			user = user.substring(3);
		return user;
	}

	@Override
	protected String getArticleDate(Document doc, Element article) {
		Element el = article.getElementsByClass("live-timestamp").first();
		if (el == null) {
			el = article.getElementsByAttributeValue("data-click-id",
					"timestamp").first();
		}

		String dateAgo = el.text().trim();
		return new SimpleDateFormat("yyyy-MM-dd") // _HH-mm
				.format(getDate(dateAgo));
	}

	@Override
	protected String getArticleCategory(Document doc, Element article,
			String currentCategory) {
		Elements categEls = article.getElementsByAttributeValueStarting("href",
				"/r/" + currentCategory + "/search=?q=flair_name");

		if (categEls.size() > 0) {
			return currentCategory + ", " + categEls.first().text().trim();
		}

		return currentCategory;
	}

	@Override
	protected String getArticleDetails(Document doc, Element article) {
		return "";
	}

	@Override
	protected String getArticleIntUrl(Document doc, Element article) {
		String url = article.absUrl("data-permalink");
		if (url == null || url.isEmpty()) {
			url = article
					.getElementsByAttributeValue("data-click-id", "timestamp")
					.first().absUrl("href");
		}

		return url;
	}

	@Override
	protected String getArticleExtUrl(Document doc, Element article) {
		Elements els = article.getElementsByAttributeValue("data-event-action",
				"title");
		if (els == null || els.isEmpty()) {
			els = article.getElementsByAttributeValue("data-click-id", "body");
		}

		Element url = els.first();
		if (!url.attr("href").trim().startsWith("/")) {
			return url.absUrl("href");
		}

		return "";
	}

	@Override
	protected String getArticleContent(Document doc, Element article) {
		Elements els = article.getElementsByClass("h2");
		if (els != null && !els.isEmpty()) {
			return els.first().text().trim();
		}

		return "";
	}

	@Override
	protected Element getFullArticle(Document doc) {
		Element element = doc.getElementsByAttributeValue("data-click-id",
				"body").first();
		if (element == null) {
			element = doc.getElementsByClass("ckueCN").first();
		}

		return element;
	}

	@Override
	protected BasicElementProcessor getElementProcessorFullArticle() {
		return new BasicElementProcessor();
	}

	@Override
	protected List<Element> getFullArticleCommentPosts(Document doc, URL intUrl) {
		Elements posts = doc.getElementsByClass("jHfOJm");
		if (posts.isEmpty()) {
			posts = doc.getElementsByClass("eCeBkc");
		}
		if (posts.isEmpty()) {
			posts = doc.getElementsByClass("gxtxxZ");
		}

		return posts;
	}

	@Override
	protected List<Element> getCommentCommentPosts(Document doc,
			Element container) {

		List<Element> elements = new LinkedList<Element>();
		for (Element el : container.children()) {
			// elements.addAll(el.getElementsByClass("jHfOJm"));
			elements.addAll(el.getElementsByClass("emJXdb"));
		}

		return elements;
	}

	@Override
	protected String getCommentId(Element post) {
		int level = 1;
		Elements els = post.getElementsByClass("imyGpC");

		if (!els.isEmpty()) {
			String l = els.first().text().trim().replace("level ", "");
			try {
				level = Integer.parseInt(l);
			} catch (NumberFormatException e) {
			}
		}

		return Integer.toString(level);
	}

	@Override
	protected String getCommentAuthor(Element post) {
		// Since we have no title, we switch with author
		return "";
	}

	@Override
	protected String getCommentTitle(Element post) {
		// Since we have no title, we switch with author

		Element authorEl = post.getElementsByClass("RVnoX").first();
		if (authorEl == null)
			authorEl = post.getElementsByClass("kzePTH").first();
		if (authorEl == null)
			authorEl = post.getElementsByClass("jczTlv").first();

		if (authorEl != null)
			return authorEl.text().trim();

		return "";
	}

	@Override
	protected String getCommentDate(Element post) {
		Element elAgo = post.getElementsByClass("hJDlLH").first();
		if (elAgo == null)
			elAgo = post.getElementsByClass("hDplaG").first();

		if (elAgo != null) {
			String dateAgo = elAgo.text().trim();
			return new SimpleDateFormat("yyyy-MM-dd_HH-mm")
					.format(getDate(dateAgo));
		}

		return "";
	}

	@Override
	protected Element getCommentContentElement(Element post) {
		return post.getElementsByClass("ckueCN").first();
	}

	@Override
	protected BasicElementProcessor getElementProcessorComment() {
		return new BasicElementProcessor();
	}

	@Override
	public void fetch(Story story) throws IOException {
		super.fetch(story);

		List<Comment> comments = new LinkedList<Comment>();
		Map<Integer, Comment> lastOfLevel = new HashMap<Integer, Comment>();

		if (!story.getComments().isEmpty()) {
			// comments are saved under a main ID (which is a copy of comment 1)
			// TODO: fix the cause instead of working around it here
			for (Comment c : story.getComments().get(0)) {
				int level = Integer.parseInt(c.getId());
				lastOfLevel.put(level, c);
				if (level <= 1) {
					comments.add(c);
				} else {
					Comment parent = lastOfLevel.get(level - 1);
					if (parent != null) {
						parent.add(c);
					} else {
						// bad data
						comments.add(c);
					}
				}
			}
		}

		story.setComments(comments);
	}

	// 2 hours ago -> 18/10/2018 21:00
	private Date getDate(String dateAgo) {
		int h = 0;
		if (dateAgo.endsWith("hour ago")) {
			h = 1;
		} else if (dateAgo.endsWith("hours ago")) {
			dateAgo = dateAgo.replace("hours ago", "").trim();
			h = Integer.parseInt(dateAgo);
		} else if (dateAgo.endsWith("day ago")) {
			h = 24;
		} else if (dateAgo.endsWith("days ago")) {
			dateAgo = dateAgo.replace("days ago", "").trim();
			h = Integer.parseInt(dateAgo) * 24;
		}

		long now = new Date().getTime(); // in ms since 1970
		now = now / (1000l * 60l * 60l); // in hours since 1970
		long then = now - h; // in hours since 1970
		then = then * (1000l * 60l * 60l); // in ms since 1970

		return new Date(then);
	}
}
