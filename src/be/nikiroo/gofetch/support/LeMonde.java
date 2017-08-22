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

public class LeMonde extends BasicSupport {
	@Override
	public String getDescription() {
		return "Le Monde: Actualités et Infos en France et dans le monde";
	}

	@Override
	public List<Story> list() throws IOException {
		List<Story> list = new ArrayList<Story>();

		for (String topic : new String[] { "international", "politique",
				"societe", "sciences" }) {
			URL url = new URL("http://www.lemonde.fr/" + topic + "/1.html");
			InputStream in = open(url);
			Document doc = DataUtil.load(in, "UTF-8", url.toString());
			Elements articles = doc.getElementsByTag("article");
			for (Element article : articles) {
				Elements times = article.getElementsByTag("time");
				Elements titleElements = article.getElementsByTag("h3");
				Elements contentElements = article.getElementsByClass("txt3");
				if (times.size() > 0 && titleElements.size() > 0
						&& contentElements.size() > 0) {
					String id = times.get(0).attr("datetime").replace(":", "_");
					String title = "[" + topic + "] "
							+ titleElements.get(0).text();
					String content = contentElements.get(0).text();
					String intUrl = "";
					String extUrl = "";
					String details = "";

					Elements detailsElements = article
							.getElementsByClass("signature");
					if (detailsElements.size() > 0) {
						details = detailsElements.get(0).text();
					}

					Elements links = titleElements.get(0).getElementsByTag("a");
					if (links.size() > 0) {
						intUrl = links.get(0).absUrl("href");
						list.add(new Story(getType(), id, title, details,
								intUrl, extUrl, content));
					}
				}
			}
		}

		return list;
	}

	@Override
	public void fetch(Story story) throws IOException {
		String fullContent = story.getContent();
		List<Comment> comments = new ArrayList<Comment>();

		// Note: no comments on this site as far as I can see (or maybe with
		// some javascript, I need to check...)

		URL url = new URL(story.getUrlInternal());
		InputStream in = open(url);
		Document doc = DataUtil.load(in, "UTF-8", url.toString());
		Element article = doc.getElementById("articleBody");
		if (article != null) {
			for (String line : toLines(article, new QuoteProcessor() {
				@Override
				public String processText(String text) {
					return text;
				}

				@Override
				public boolean ignoreNode(Node node) {
					if (node instanceof Element) {
						Element element = (Element) node;
						if (element.hasClass("lire")) {
							return true;
						}
					}

					return false;
				}

				@Override
				public boolean detectQuote(Node node) {
					return false;
				}

				@Override
				public String manualProcessing(Node node) {
					if (node instanceof Element) {
						Element element = (Element) node;
						if (element.hasClass("intertitre")) {
							return "\n[ " + element.text() + " ]\n";
						}
					}
					return null;
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
		story.setComments(comments);
	}
}
