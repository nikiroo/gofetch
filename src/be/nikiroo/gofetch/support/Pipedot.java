package be.nikiroo.gofetch.support;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.helper.DataUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import be.nikiroo.gofetch.data.Comment;
import be.nikiroo.gofetch.data.Story;

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
	public List<Story> list() throws IOException {
		List<Story> list = new ArrayList<Story>();

		URL url = new URL("https://pipedot.org/");
		InputStream in = open(url);
		Document doc = DataUtil.load(in, "UTF-8", url.toString());
		Elements stories = doc.getElementsByClass("story");
		for (Element story : stories) {
			Elements titles = story.getElementsByTag("h1");
			if (titles.size() == 0) {
				continue;
			}

			Element title = titles.get(0);

			String id = "";
			for (Element idElem : story.getElementsByTag("a")) {
				if (idElem.attr("href").startsWith("/pipe/")) {
					id = idElem.attr("href").substring("/pipe/".length());
					break;
				}
			}

			String intUrl = null;
			String extUrl = null;

			Elements links = story.getElementsByTag("a");
			if (links.size() > 0) {
				intUrl = links.get(0).absUrl("href");
			}

			// Take first ext URL as original source
			for (Element link : links) {
				String uuu = link.absUrl("href");
				if (!uuu.isEmpty() && !uuu.contains("pipedot.org/")) {
					extUrl = uuu;
					break;
				}
			}

			String details = "";
			Elements detailsElements = story.getElementsByTag("div");
			if (detailsElements.size() > 0) {
				details = detailsElements.get(0).text();
			}

			String body = "";
			for (Element elem : story.children()) {
				String tag = elem.tag().toString();
				if (!tag.equals("header") && !tag.equals("footer")) {
					body = elem.text();
					break;
				}
			}

			list.add(new Story(getType(), id, title.text(), details, intUrl,
					extUrl, body));
		}

		return list;
	}

	@Override
	public void fetch(Story story) throws IOException {
		List<Comment> comments = new ArrayList<Comment>();

		URL url = new URL(story.getUrlInternal());
		InputStream in = open(url);
		Document doc = DataUtil.load(in, "UTF-8", url.toString());
		Elements listing = doc.getElementsByTag("main");
		if (listing.size() > 0) {
			comments.addAll(getComments(listing.get(0)));
		}

		story.setComments(comments);
	}

	private List<Comment> getComments(Element listing) {
		List<Comment> comments = new ArrayList<Comment>();
		for (Element commentElement : listing.children()) {
			if (commentElement.hasClass("comment")) {
				Comment comment = getComment(commentElement);
				if (!comment.isEmpty()) {
					comments.add(comment);
				}
			}
		}
		return comments;
	}

	private Comment getComment(Element commentElement) {
		String title = firstOrEmptyTag(commentElement, "h3");
		String author = firstOrEmpty(commentElement, "h4");
		String content = firstOrEmpty(commentElement, "comment-body");

		String date = "";
		int pos = author.lastIndexOf(" on ");
		if (pos >= 0) {
			date = author.substring(pos + " on ".length()).trim();
			author = author.substring(0, pos).trim();
		}

		Comment comment = new Comment(commentElement.id(), author, title, date,
				content);

		Elements commentOutline = commentElement
				.getElementsByClass("comment-outline");
		if (commentOutline.size() > 0) {
			comment.addAll(getComments(commentOutline.get(0)));
		}

		return comment;
	}

	/**
	 * Get the first element of the given class, or an empty {@link String} if
	 * none found.
	 * 
	 * @param element
	 *            the element to look in
	 * @param className
	 *            the class to look for
	 * 
	 * @return the value or an empty {@link String}
	 */
	private String firstOrEmpty(Element element, String className) {
		Elements subElements = element.getElementsByClass(className);
		if (subElements.size() > 0) {
			return subElements.get(0).text();
		}

		return "";
	}

	/**
	 * Get the first element of the given tag, or an empty {@link String} if
	 * none found.
	 * 
	 * @param element
	 *            the element to look in
	 * @param tagName
	 *            the tag to look for
	 * 
	 * @return the value or an empty {@link String}
	 */
	private String firstOrEmptyTag(Element element, String tagName) {
		Elements subElements = element.getElementsByTag(tagName);
		if (subElements.size() > 0) {
			return subElements.get(0).text();
		}

		return "";
	}
}
