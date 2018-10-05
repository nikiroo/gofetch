package be.nikiroo.gofetch.support;

import be.nikiroo.gofetch.data.Story;
import be.nikiroo.gofetch.data.Comment;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

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
		urls.add(new AbstractMap.SimpleEntry<URL, String>(
			new URL(base + "linux_gaming" + "/new/"), "linux_gaming"
		));

		return urls;
	}

	@Override
	protected List<Element> getArticles(Document doc) {
		return doc.getElementsByClass("thing");
	}

	@Override
	protected String getArticleId(Document doc, Element article) {
		// Use the date, Luke
		return "";
	}

	@Override
	protected String getArticleTitle(Document doc, Element article) {
		return article.getElementsByAttributeValue(
			"data-event-action", "title").first().text().trim();
	}
	
	@Override
	protected String getArticleAuthor(Document doc, Element article) {
		return article.getElementsByAttributeValueStarting(
			"href", "/user/"
		).text().trim();
	}

	@Override
	protected String getArticleDate(Document doc, Element article) {
		return article.getElementsByClass("live-timestamp")
			.attr("datetime").trim();
	}

	@Override
	protected String getArticleCategory(Document doc, Element article,
			String currentCategory) {
		Elements categEls = article.getElementsByAttributeValueStarting(
			"href", "/r/" + currentCategory + "/search=?q=flair_name"
		);
		
		if (categEls.size() > 0) {
			return currentCategory + ", " 
				+ categEls.first().text().trim();
		}
		
		return currentCategory;
	}

	@Override
	protected String getArticleDetails(Document doc, Element article) {
		return "";
	}

	@Override
	protected String getArticleIntUrl(Document doc, Element article) {
		return article.getElementsByClass("thing").first()
			.absUrl("data-permalink");
	}

	@Override
	protected String getArticleExtUrl(Document doc, Element article) {
		Element url = article.getElementsByAttributeValue(
			"data-event-action", "title").first();
		if (!url.attr("href").trim().startsWith("/")) {
			return url.absUrl("href");
		}
		
		return "";
	}

	@Override
	protected String getArticleContent(Document doc, Element article) {
		return "";
	}

	@Override
	protected Element getFullArticle(Document doc) {
		return doc.getElementsByClass("ckueCN").first();
	}

	@Override
	protected ElementProcessor getElementProcessorFullArticle() {
		return new BasicElementProcessor();
	}

	@Override
	protected List<Element> getFullArticleCommentPosts(Document doc, URL intUrl) {
		return doc.getElementsByClass("jHfOJm");
	}

	@Override
	protected List<Element> getCommentCommentPosts(Document doc,
			Element container) {
		List<Element> elements = new LinkedList<Element>();
		for (Element el : container.children()) {
			elements.addAll(el.getElementsByClass("jHfOJm"));
		}
		
		return elements;
	}

	@Override
	protected String getCommentId(Element post) {
		int level = 1;
		Elements els = post.getElementsByClass("imyGpC");
		if (els.size() > 0) {
			String l = els.first().text().trim()
				.replace("level ", "");
			try {
				level = Integer.parseInt(l);
			} catch(NumberFormatException e) {
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
		Elements els = post.getElementsByClass("RVnoX");
		if (els.size() > 0) {
			return els.first().text().trim();
		}
		
		els = post.getElementsByClass("kzePTH");
		if (els.size() > 0) {
			return els.first().text().trim();
		}
		
		return "";
	}

	@Override
	protected String getCommentDate(Element post) {
		return post.getElementsByClass("hJDlLH")
			.first().text().trim();
	}

	@Override
	protected Element getCommentContentElement(Element post) {
		return post.getElementsByClass("ckueCN")
			.first();
	}

	@Override
	protected ElementProcessor getElementProcessorComment() {
		return new BasicElementProcessor();
	}
	
	@Override
	public void fetch(Story story) throws IOException {
		super.fetch(story);
		
		List<Comment> comments = new LinkedList<Comment>();
		Map<Integer, Comment> lastOfLevel = 
			new HashMap<Integer, Comment>();
		
		for (Comment c : story.getComments()) {
			int level = Integer.parseInt(c.getId());
			lastOfLevel.put(level, c);
			if (level <= 1) {
				comments.add(c);
			} else {
				Comment parent = lastOfLevel.get(level - 1);
				if (parent != null ){
					parent.add(c);
				} else {
					// bad data
					comments.add(c);
				}
			}
		}
		
		story.setComments(comments);
	}
}
