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
 * Support <a href='https://slashdot.org/'>https://slashdot.org/</a>.
 * 
 * @author niki
 */
public class Slashdot extends BasicSupport {
	@Override
	public String getDescription() {
		return "Slashdot: News for nerds, stuff that matters!";
	}

	@Override
	public List<Story> list() throws IOException {
		List<Story> list = new ArrayList<Story>();

		URL url = new URL("https://slashdot.org/");
		InputStream in = open(url);
		Document doc = DataUtil.load(in, "UTF-8", url.toString());
		Elements articles = doc.getElementsByTag("header");
		for (Element article : articles) {
			Elements titles = article.getElementsByClass("story-title");
			if (titles.size() == 0) {
				continue;
			}

			Element title = titles.get(0);

			String id = "" + title.attr("id");
			if (id.startsWith("title-")) {
				id = id.substring("title-".length());
			}

			Elements links = title.getElementsByTag("a");
			String intUrl = null;
			String extUrl = null;
			if (links.size() > 0) {
				intUrl = links.get(0).absUrl("href");
			}
			if (links.size() > 1) {
				extUrl = links.get(1).absUrl("href");
			}

			String details = "";
			Elements detailsElements = article.getElementsByClass("details");
			if (detailsElements.size() > 0) {
				details = detailsElements.get(0).text();
			}

			String body = "";
			Element bodyElement = doc.getElementById("text-" + id);
			if (bodyElement != null) {
				body = bodyElement.text();
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
		Element listing = doc.getElementById("commentlisting");
		if (listing != null) {
			comments.addAll(getComments(listing));
		}

		story.setComments(comments);
	}

	private List<Comment> getComments(Element listing) {
		List<Comment> comments = new ArrayList<Comment>();
		Comment lastComment = null;
		for (Element commentElement : listing.children()) {
			if (commentElement.hasClass("comment")) {
				if (!commentElement.hasClass("hidden")) {
					lastComment = getComment(commentElement);
					comments.add(lastComment);
				}

				List<Comment> subComments = new ArrayList<Comment>();
				for (Element child : commentElement.children()) {
					if (child.id().contains("commtree_")) {
						subComments.addAll(getComments(child));
					}
				}

				if (lastComment == null) {
					comments.addAll(subComments);
				} else {
					lastComment.addAll(subComments);
				}
			}
		}

		return comments;
	}

	/**
	 * Get a comment from the given element.
	 * 
	 * @param commentElement
	 *            the element to get the comment of.
	 * 
	 * @return the comment, <b>NOT</b> including sub-comments
	 */
	private Comment getComment(Element commentElement) {
		String title = firstOrEmpty(commentElement, "title").text();
		String author = firstOrEmpty(commentElement, "by").text();
		String date = firstOrEmpty(commentElement, "otherdetails").text();
		Element content = firstOrEmpty(commentElement, "commentBody");

		return new Comment(commentElement.id(), author, title, date,
				toLines(content));
	}

	private List<String> toLines(Element element) {
		return toLines(element, new BasicElementProcessor() {
			@Override
			public String processText(String text) {
				while (text.startsWith(">")) { // comment in one-liners
					text = text.substring(1).trim();
				}

				return text;
			}

			@Override
			public boolean detectQuote(Node node) {
				if (node instanceof Element) {
					Element elementNode = (Element) node;
					if (elementNode.tagName().equals("blockquote")
							|| elementNode.hasClass("quote")
							|| (elementNode.tagName().equals("p")
									&& elementNode.textNodes().size() == 1 && elementNode
									.textNodes().get(0).getWholeText()
									.startsWith(">"))) {
						return true;
					}
				}

				return false;
			}
		});
	}
}
