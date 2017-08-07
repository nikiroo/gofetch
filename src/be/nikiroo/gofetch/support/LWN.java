package be.nikiroo.gofetch.support;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.helper.DataUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import be.nikiroo.gofetch.data.Comment;
import be.nikiroo.gofetch.data.Story;

/**
 * Support <a href='https://lwn.net/'>https://lwn.net/</a>.
 * 
 * @author niki
 */
public class LWN extends BasicSupport {
	@Override
	public String getDescription() {
		return "LWN: Linux Weekly Newsletter";
	}

	@Override
	public List<Story> list() throws IOException {
		List<Story> list = new ArrayList<Story>();

		URL url = new URL("https://lwn.net/");
		InputStream in = open(url);
		Document doc = DataUtil.load(in, "UTF-8", url.toString());
		Elements stories = doc.getElementsByClass("pure-u-1");
		for (Element story : stories) {
			Elements titles = story.getElementsByClass("Headline");
			Elements listings = story.getElementsByClass("BlurbListing");
			if (titles.size() == 0) {
				continue;
			}
			if (listings.size() == 0) {
				continue;
			}

			Element listing = listings.get(0);
			if (listing.children().size() < 2) {
				continue;
			}

			String title = titles.get(0).text();
			String details = listing.children().get(0).text();
			String body = "";
			// All but the first and two last children
			for (int i = 1; i < listing.children().size() - 2; i++) {
				Element e = listing.children().get(i);
				body = body.trim() + " " + e.text().trim();
			}
			body = body.trim();

			String author = "";
			int pos = details.indexOf(" by ");
			if (pos >= 0) {
				author = details.substring(pos + " by ".length()).trim();
			}

			String date = "";
			pos = details.indexOf(" Posted ");
			if (pos >= 0) {
				date = details.substring(pos + " Posted ".length()).trim();
			}

			String id = "";
			String intUrl = "";
			String extUrl = "";
			for (Element idElem : story.getElementsByTag("a")) {
				// Last link is the story link
				intUrl = idElem.absUrl("href");
				pos = intUrl.indexOf("#Comments");
				if (pos >= 0) {
					intUrl = intUrl.substring(0, pos - 1);
				}
				id = intUrl.replaceAll("[^0-9]", "");
			}

			list.add(new Story(getType(), id, title, details, intUrl, extUrl,
					body));
		}

		return list;
	}

	@Override
	public void fetch(Story story) throws IOException {
		List<Comment> comments = new ArrayList<Comment>();
		String fullContent = story.getContent();

		// Do not try the paid-for stories...
		if (!story.getTitle().startsWith("[$]")) {
			URL url = new URL(story.getUrlInternal());
			InputStream in = open(url);
			Document doc = DataUtil.load(in, "UTF-8", url.toString());
			Elements fullContentElements = doc
					.getElementsByClass("ArticleText");
			if (fullContentElements.size() > 0) {
				// comments.addAll(getComments(listing.get(0)));
				fullContent = fullContentElements.get(0).text();
			}

			Elements listing = doc.getElementsByClass("lwn-u-1");
			if (listing.size() > 0) {
				comments.addAll(getComments(listing.get(0)));
			}
		} else {
			fullContent = "[$] Sorry, this article is currently available to LWN suscribers only [https://lwn.net/subscribe/].";
		}

		story.setFullContent(fullContent);
		story.setComments(comments);
	}

	private List<Comment> getComments(Element listing) {
		List<Comment> comments = new ArrayList<Comment>();
		for (Element commentElement : listing.children()) {
			if (commentElement.hasClass("CommentBox")) {
				Comment comment = getComment(commentElement);
				if (!comment.isEmpty()) {
					comments.add(comment);
				}
			} else if (commentElement.hasClass("Comment")) {
				if (comments.size() > 0) {
					comments.get(comments.size() - 1).addAll(
							getComments(commentElement));
				}
			}
		}
		return comments;
	}

	private Comment getComment(Element commentElement) {
		String title = firstOrEmpty(commentElement, "CommentTitle");
		String author = firstOrEmpty(commentElement, "CommentPoster");

		String date = "";
		int pos = author.lastIndexOf(" by ");
		if (pos >= 0) {
			date = author.substring(0, pos).trim();
			author = author.substring(pos + " by ".length()).trim();

			if (author.startsWith("Posted ")) {
				author = author.substring("Posted ".length()).trim();
			}
		}

		String content = "";
		Elements commentBodyElements = commentElement
				.getElementsByClass("CommentBody");
		if (commentBodyElements.size() > 0) {
			for (Node contentNode : commentBodyElements.get(0).childNodes()) {
				if (contentNode instanceof Element) {
					Element contentElement = (Element) contentNode;
					if (!contentElement.hasClass("CommentPoster")) {
						content = content.trim() + " "
								+ contentElement.text().trim();
					}
				} else {
					content = content.trim() + " "
							+ contentNode.outerHtml().trim();
				}

			}
			content = content.trim();
		}

		Comment comment = new Comment(commentElement.id(), author, title, date,
				content);

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
