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
 * Support <a href="https://www.toolinux.com/">https://www.toolinux.com/</a>.
 * 
 * @author niki
 */
public class TooLinux extends BasicSupport {
	@Override
	public String getDescription() {
		return "TooLinux: Actualité généraliste sur Linux et les logiciels libres";
	}

	@Override
	public List<Story> list() throws IOException {
		List<Story> list = new ArrayList<Story>();

		URL url = new URL("https://www.toolinux.com/");
		InputStream in = downloader.open(url);
		Document doc = DataUtil.load(in, "UTF-8", url.toString());
		Elements articles = doc.getElementsByClass("hentry");
		for (Element article : articles) {
			String id = "";
			String intUrl = "";
			String extUrl = ""; // nope
			String title = "";
			String date = "";
			String details = "";
			String body = "";
			String author = ""; // nope
			String categ = ""; // nope

			Element urlElement = article.getElementsByTag("a").first();
			if (urlElement != null) {
				intUrl = urlElement.absUrl("href");
			}

			Element titleElement = article.getElementsByClass("entry-title")
					.first();
			if (titleElement != null) {
				title = StringUtils.unhtml(titleElement.text()).trim();
			}

			Element dateElement = article.getElementsByClass("published")
					.first();
			if (dateElement != null) {
				date = StringUtils.unhtml(dateElement.text()).trim();
				id = dateElement.attr("title").trim();
			}

			if (id.isEmpty()) {
				// fallback
				id = intUrl.replace("/", "_");
			}

			Element bodyElement = article.getElementsByClass("introduction")
					.first();
			if (bodyElement != null) {
				body = StringUtils.unhtml(bodyElement.text()).trim();
			}

			list.add(new Story(getType(), id, title, author, date, categ,
					details, intUrl, extUrl, body));
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
			Element article = doc.getElementById("content");
			if (article != null) {
				for (String line : toLines(article,
						new BasicElementProcessor() {
							@Override
							public boolean ignoreNode(Node node) {
								if ("notes".equals(node.attr("class"))) {
									return true;
								}
								return false;
							}
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
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}
}
