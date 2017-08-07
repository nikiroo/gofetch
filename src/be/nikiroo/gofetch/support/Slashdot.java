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
		Elements stories = doc.getElementsByTag("header");
		for (Element story : stories) {
			Elements titles = story.getElementsByClass("story-title");
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
			Elements detailsElements = story.getElementsByClass("details");
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
		String title = firstOrEmpty(commentElement, "title");
		String author = firstOrEmpty(commentElement, "by");
		String content = firstOrEmpty(commentElement, "commentBody");
		String date = firstOrEmpty(commentElement, "otherdetails");

		Comment comment = new Comment(commentElement.id(), author, title, date,
				content);

		for (Element child : commentElement.children()) {
			if (child.id().contains("commtree_")) {
				comment.addAll(getComments(child));
			}
		}

		return comment;
	}

	private String firstOrEmpty(Element element, String className) {
		Elements subElements = element.getElementsByClass(className);
		if (subElements.size() > 0) {
			return subElements.get(0).text();
		}

		return "";
	}
}
