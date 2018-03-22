package be.nikiroo.gofetch.support;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jsoup.helper.DataUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import be.nikiroo.gofetch.data.Comment;
import be.nikiroo.gofetch.data.Story;
import be.nikiroo.utils.StringUtils;

public class TheRegister extends BasicSupport {
	@Override
	public String getDescription() {
		return "The Register: Biting the hand that feeds IT";
	}

	@Override
	public List<Story> list() throws IOException {
		List<Story> list = new ArrayList<Story>();

		URL url = new URL("https://www.theregister.co.uk/");
		InputStream in = downloader.open(url);
		Document doc = DataUtil.load(in, "UTF-8", url.toString());
		Elements articles = doc.getElementsByClass("story_link");
		for (Element article : articles) {
			if (article.getElementsByClass("time_stamp").isEmpty()) {
				// Some articles are doubled,
				// but the second copy without the time info
				continue;
			}

			String id = "";
			String intUrl = article.absUrl("href");
			String extUrl = ""; // nope
			String title = "";
			String date = "";
			String details = "";
			String body = "";

			String topic = "";
			Element topicElement = article.previousElementSibling();
			if (topicElement != null) {
				topic = "[" + topicElement.text().trim() + "] ";
			}
			Element titleElement = article.getElementsByTag("h4").first();
			if (titleElement != null) {
				title = StringUtils.unhtml(titleElement.text()).trim();
			}
			title = topic + title;

			Element dateElement = article.getElementsByClass("time_stamp")
					.first();
			if (dateElement != null) {
				String epochS = dateElement.attr("data-epoch");
				if (epochS != null && !epochS.isEmpty()) {
					id = epochS;
					date = date(epochS);
				}
			}

			if (id.isEmpty()) {
				// fallback
				id = article.attr("href").replace("/", "_");
			}

			Element detailsElement = article.getElementsByClass("standfirst")
					.first();
			details = "(" + date + ") ";
			if (detailsElement != null) {
				details += StringUtils.unhtml(detailsElement.text()).trim();
			}

			list.add(new Story(getType(), id, title, details, intUrl, extUrl,
					body));
		}

		return list;
	}

	@Override
	public void fetch(Story story) throws IOException {
		String fullContent = story.getContent();
		List<Comment> comments = new ArrayList<Comment>();
		story.setComments(comments);

		URL url = new URL(story.getUrlInternal());
		InputStream in = downloader.open(url);
		try {
			Document doc = DataUtil.load(in, "UTF-8", url.toString());
			Element article = doc.getElementById("body");
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

			story.setFullContent(fullContent);

			// Get comments URL then parse it
			in.close();
			in = null;
			in = downloader
					.open(new URL("https://forums.theregister.co.uk/forum/1"
							+ url.getPath()));
			doc = DataUtil.load(in, "UTF-8", url.toString());
			Element posts = doc.getElementById("forum_posts");
			if (posts != null) {
				for (Element post : posts.getElementsByClass("post")) {
					String id = "";
					String author = "";
					String title = "";
					String date = "";
					List<String> content = new ArrayList<String>();

					Element idE = post.getElementsByTag("a").first();
					if (idE != null) {
						id = idE.attr("id");
						if (id.startsWith("c_")) {
							id = id.substring(2);
						}

						Element dateE = idE.getElementsByTag("span").first();
						if (dateE != null) {
							date = date(dateE.attr("data-epoch"));
						}
					}

					Element authorE = post.getElementsByClass("author").first();
					if (authorE != null) {
						author = StringUtils.unhtml(authorE.text()).trim();
					}

					Element titleE = post.getElementsByTag("h4").first();
					if (titleE != null) {
						title = StringUtils.unhtml(titleE.text()).trim();
					}

					Element contentE = post.getElementsByClass("body").first();
					if (contentE != null) {
						for (String line : toLines(contentE,
								new BasicElementProcessor() {
									@Override
									public boolean ignoreNode(Node node) {
										// TODO: ignore headlines/pub

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
								})) {
							content.add(line);
						}
					}

					Comment comment = new Comment(id, author, title, date,
							content);
					Comment parent = null;

					Element inReplyTo = post.getElementsByClass("in-reply-to")
							.first();
					if (inReplyTo != null) {
						String parentId = inReplyTo.absUrl("href");
						if (parentId != null && parentId.contains("/")) {
							int i = parentId.lastIndexOf('/');
							parentId = parentId.substring(i + 1);
							parent = story.getCommentById(parentId);
						}
					}

					if (parent == null) {
						comments.add(comment);
					} else {
						parent.add(comment);
					}
				}
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	// Return display date from epoch String, or "" if error
	private static String date(String epochString) {
		long epoch = 0;
		try {
			epoch = Long.parseLong(epochString);
		} catch (Exception e) {
			epoch = 0;
		}

		if (epoch > 0) {
			return new SimpleDateFormat("dd MMM YYYY").format(new Date(
					1000 * epoch));
		}

		return "";
	}
}
