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
		InputStream in = downloader.open(url);
		Document doc = DataUtil.load(in, "UTF-8", url.toString());
		Elements articles = doc.getElementsByClass("pure-u-1");
		for (Element article : articles) {
			Elements titles = article.getElementsByClass("Headline");
			Elements listings = article.getElementsByClass("BlurbListing");
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
			for (Element idElem : article.getElementsByTag("a")) {
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
			InputStream in = downloader.open(url);
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
		String title = firstOrEmpty(commentElement, "CommentTitle").text();
		String author = firstOrEmpty(commentElement, "CommentPoster").text();

		String date = "";
		int pos = author.lastIndexOf(" by ");
		if (pos >= 0) {
			date = author.substring(0, pos).trim();
			author = author.substring(pos + " by ".length()).trim();

			if (author.startsWith("Posted ")) {
				author = author.substring("Posted ".length()).trim();
			}
		}

		Element content = null;
		Elements commentBodyElements = commentElement
				.getElementsByClass("CommentBody");
		if (commentBodyElements.size() > 0) {
			content = commentBodyElements.get(0);
		}

		Comment comment = new Comment(commentElement.id(), author, title, date,
				toLines(content));

		return comment;
	}

	private List<String> toLines(Element element) {
		return toLines(element, new BasicElementProcessor() {
			@Override
			public String processText(String text) {
				while (text.startsWith(">")) { // comments
					text = text.substring(1).trim();
				}

				return text;
			}

			@Override
			public boolean detectQuote(Node node) {
				if (node instanceof Element) {
					Element elementNode = (Element) node;
					if (elementNode.tagName().equals("blockquote")
							|| elementNode.hasClass("QuotedText")) {
						return true;
					}
				}

				return false;
			}

			@Override
			public boolean ignoreNode(Node node) {
				if (node instanceof Element) {
					Element elementNode = (Element) node;
					if (elementNode.hasClass("CommentPoster")) {
						return true;
					}
				}

				return false;
			}
		});
	}
}
