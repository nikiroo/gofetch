package be.nikiroo.gofetch.support;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.helper.DataUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import be.nikiroo.gofetch.data.Comment;
import be.nikiroo.gofetch.data.Story;

/**
 * Support <a
 * href="https://www.theregister.co.uk/">https://www.theregister.co.uk/</a>.
 * 
 * @author niki
 */
public class TheRegister extends BasicSupport {
	private Map<String, String> commentReplies = new HashMap<String, String>();

	@Override
	public String getDescription() {
		return "The Register: Biting the hand that feeds IT";
	}

	@Override
	public void fetch(Story story) throws IOException {
		super.fetch(story);

		// Update comment replies
		List<Comment> comments = new ArrayList<Comment>();
		for (Comment comment : story.getComments()) {
			if (commentReplies.containsKey(comment.getId())) {
				String inReplyToId = commentReplies.get(comment.getId());
				Comment inReplyTo = story.getCommentById(inReplyToId);
				if (inReplyTo != null) {
					inReplyTo.add(comment);
				} else {
					comments.add(comment);
				}
			} else {
				comments.add(comment);
			}
		}
		story.setComments(comments);
	}

	@Override
	protected List<Entry<URL, String>> getUrls() throws IOException {
		List<Entry<URL, String>> urls = new ArrayList<Entry<URL, String>>();
		urls.add(new AbstractMap.SimpleEntry<URL, String>(new URL(
				"https://www.theregister.co.uk/"), ""));
		return urls;
	}

	@Override
	protected List<Element> getArticles(Document doc) {
		return doc.getElementsByClass("story_link");
	}

	@Override
	protected String getArticleId(Document doc, Element article) {
		return "";
	}

	@Override
	protected String getArticleTitle(Document doc, Element article) {
		Element titleElement = article.getElementsByTag("h4").first();
		if (titleElement != null) {
			return titleElement.text();
		}

		return "";
	}

	@Override
	protected String getArticleAuthor(Document doc, Element article) {
		return "";
	}

	@Override
	protected String getArticleDate(Document doc, Element article) {
		Element dateElement = article.getElementsByClass("time_stamp").first();
		if (dateElement != null) {
			return dateElement.attr("data-epoch");
		}

		return "";
	}

	@Override
	protected String getArticleCategory(Document doc, Element article,
			String currentCategory) {
		Element categElement = article.previousElementSibling();
		if (categElement != null) {
			return categElement.text();
		}

		return "";
	}

	@Override
	protected String getArticleDetails(Document doc, Element article) {
		// We have some "details" but no content, so we switch them:
		return "";
	}

	@Override
	protected String getArticleIntUrl(Document doc, Element article) {
		return article.absUrl("href");
	}

	@Override
	protected String getArticleExtUrl(Document doc, Element article) {
		return "";
	}

	@Override
	protected String getArticleContent(Document doc, Element article) {
		// We have some "details" but no content, so we switch them:
		Element detailsElement = article.getElementsByClass("standfirst")
				.first();
		if (detailsElement != null) {
			return getArticleText(detailsElement);
		}

		return "";
	}

	@Override
	protected Element getFullArticle(Document doc) {
		return doc.getElementById("body");
	}

	@Override
	protected List<Element> getFullArticleCommentPosts(Document doc, URL intUrl) {
		List<Element> commentElements = new ArrayList<Element>();

		// Get comments URL then parse it
		try {
			URL url = new URL("https://forums.theregister.co.uk/forum/1"
					+ intUrl.getPath());
			InputStream in = open(url);
			try {
				doc = DataUtil.load(in, "UTF-8", url.toString());
				Element posts = doc.getElementById("forum_posts");
				if (posts != null) {
					for (Element post : posts.getElementsByClass("post")) {
						commentElements.add(post);
						Element inReplyTo = post.getElementsByClass(
								"in-reply-to").first();
						if (inReplyTo != null) {
							String parentId = inReplyTo.absUrl("href");
							if (parentId != null && parentId.contains("/")) {
								int i = parentId.lastIndexOf('/');
								parentId = parentId.substring(i + 1);

								commentReplies
										.put(getCommentId(post), parentId);
							}
						}
					}
				}
			} finally {
				in.close();
			}
		} catch (IOException e) {
		}

		return commentElements;
	}

	@Override
	protected ElementProcessor getElementProcessorFullArticle() {
		return new BasicElementProcessor();
	}

	@Override
	protected List<Element> getCommentCommentPosts(Document doc,
			Element container) {
		return null;
	}

	@Override
	protected String getCommentId(Element post) {
		Element idE = post.getElementsByTag("a").first();
		if (idE != null) {
			String id = idE.attr("id");
			if (id.startsWith("c_")) {
				id = id.substring(2);
			}

			return id;
		}

		return "";
	}

	@Override
	protected String getCommentAuthor(Element post) {
		Element author = post.getElementsByClass("author").first();
		if (author != null) {
			return author.text();
		}

		return "";
	}

	@Override
	protected String getCommentTitle(Element post) {
		Element title = post.getElementsByTag("h4").first();
		if (title != null) {
			return title.text();
		}

		return "";
	}

	@Override
	protected String getCommentDate(Element post) {
		Element id = post.getElementsByTag("a").first();
		if (id != null) {
			Element date = id.getElementsByTag("span").first();
			if (date != null) {
				return date.attr("data-epoch");
			}
		}

		return "";
	}

	@Override
	protected Element getCommentContentElement(Element post) {
		return post.getElementsByClass("body").first();
	}

	@Override
	protected ElementProcessor getElementProcessorComment() {
		return new BasicElementProcessor() {
			@Override
			public boolean ignoreNode(Node node) {
				// Remove the comment title (which has
				// already been processed earlier)
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
}
