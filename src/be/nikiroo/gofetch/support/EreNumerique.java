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
import be.nikiroo.utils.StringUtils;

/**
 * Support <a
 * href="https://www.erenumerique.fr/">https://www.erenumerique.fr/</a>.
 * 
 * @author niki
 */
public class EreNumerique extends BasicSupport {
	@Override
	public String getDescription() {
		return "Ère Numérique.FR: faites le bon choix !";
	}

	@Override
	public List<Story> list() throws IOException {
		List<Story> list = new ArrayList<Story>();

		for (String categ : new String[] { "informatique" }) {
			URL url = new URL("https://www.erenumerique.fr/" + categ);
			InputStream in = downloader.open(url);
			Document doc = DataUtil.load(in, "UTF-8", url.toString());
			Elements articles = doc.getElementsByClass("item-details");
			for (Element article : articles) {
				String id = "";
				String intUrl = "";
				String extUrl = ""; // nope
				String title = "";
				String date = "";
				String author = "";
				String details = "";
				String body = "";

				// MUST NOT fail:
				Element dateElement = article //
						.getElementsByTag("time").first();
				if (dateElement == null) {
					continue;
				}

				Element urlElement = article.getElementsByTag("a").first();
				if (urlElement != null) {
					intUrl = urlElement.absUrl("href");
				}

				id = dateElement.attr("datetime").replace(":", "_")
						.replace("+", "_");
				date = date(dateElement.attr("datetime"));

				Element titleElement = article.getElementsByTag("h2").first();
				if (titleElement != null) {
					title = StringUtils.unhtml(titleElement.text()).trim();
				}

				Element authorElement = article.getElementsByClass(
						"td-post-author-name").first();
				if (authorElement != null) {
					authorElement = authorElement.getElementsByTag("a").first();
				}
				if (authorElement != null) {
					author = StringUtils.unhtml(authorElement.text()).trim();
				}

				Element contentElement = article.getElementsByClass(
						"td-excerpt").first();
				if (contentElement != null) {
					body = StringUtils.unhtml(contentElement.text()).trim();
				}

				list.add(new Story(getType(), id, title, author, date, categ,
						details, intUrl, extUrl, body));
			}
		}

		return list;
	}

	@Override
	public void fetch(Story story) throws IOException {
		String fullContent = story.getContent();

		URL url = new URL(story.getUrlInternal());
		InputStream in = downloader.open(url);
		try {
			Document doc = DataUtil.load(in, "UTF-8", url.toString());
			Element article = doc.getElementsByTag("article").first();
			if (article != null) {
				for (String line : toLines(article,
						new BasicElementProcessor() {
							// TODO: ignore headlines/pub
						})) {
					fullContent += line + "\n";
				}

				// Content is too tight with a single break per line:
				fullContent = fullContent.replace("\n", "\n\n") //
						.replace("\n\n\n\n", "\n\n") //
						.replace("\n\n\n\n", "\n\n") //
						.trim();
			}

			// Get comments URL then parse it, if possible
			Element posts = doc.getElementsByClass("comment-list").first();

			story.setFullContent(fullContent);
			story.setComments(getComments(posts));
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	private List<Comment> getComments(Element posts) {
		List<Comment> comments = new ArrayList<Comment>();
		if (posts != null) {
			for (Element post : posts.children()) {
				if (!post.hasClass("comment")) {
					continue;
				}

				String id = "";
				String author = "";
				String title = "";
				String date = "";
				List<String> content = new ArrayList<String>();

				Element authorE = post.getElementsByTag("footer").first();
				if (authorE != null) {
					authorE = authorE.getElementsByTag("cite").first();
				}
				if (authorE != null) {
					author = StringUtils.unhtml(authorE.text()).trim();
				}

				Element idE = post.getElementsByTag("a").first();
				if (idE != null) {
					id = idE.attr("id");
					Element dateE = idE.getElementsByTag("span").first();
					if (dateE != null) {
						date = date(dateE.attr("data-epoch"));
					}
				}

				Element contentE = post.getElementsByClass("comment-content")
						.first();
				if (contentE != null) {
					for (String line : toLines(contentE,
							new BasicElementProcessor() {
								@Override
								public boolean ignoreNode(Node node) {
									// TODO: ignore headlines/pub
									if (node instanceof Element) {
										Element el = (Element) node;
										if ("h4".equals(el.tagName())) {
											return true;
										}
									}

									return false;
								}
							})) {
						content.add(line);
					}
				}

				// Since we have no title but still an author, let's switch:
				title = author;
				author = "";
				Comment comment = new Comment(id, author, title, date, content);
				comments.add(comment);

				Element children = post.getElementsByClass("children").first();
				comment.addAll(getComments(children));
			}
		}

		return comments;
	}
}
