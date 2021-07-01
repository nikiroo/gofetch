package be.nikiroo.gofetch.support;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.jsoup.helper.DataUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import be.nikiroo.gofetch.data.Comment;
import be.nikiroo.gofetch.data.Story;

class Phoronix extends BasicSupport {
	@Override
	public String getDescription() {
		return "Phoronix: news regarding free and open-source software";
	}

	@Override
	protected List<Entry<URL, String>> getUrls() throws IOException {
		List<Entry<URL, String>> urls = new ArrayList<Entry<URL, String>>();
		urls.add(new AbstractMap.SimpleEntry<URL, String>(new URL(
				"https://www.phoronix.com/"), ""));
		return urls;
	}

	@Override
	protected List<Element> getArticles(Document doc) {
		return doc.getElementsByTag("article");
	}

	@Override
	protected String getArticleId(Document doc, Element article) {
		Element comments = article.getElementsByClass("comments").first();
		if (comments != null) {
			Element forumLink = comments.getElementsByTag("a").first();
			if (forumLink != null) {
				String id = forumLink.absUrl("href");
				int pos = id.lastIndexOf("/");
				if (pos >= 0) {
					id = id.substring(pos + 1);
				}

				return id;
			}
		}

		return "";
	}

	@Override
	protected String getArticleTitle(Document doc, Element article) {
		Element header = article.getElementsByTag("header").first();
		if (header != null) {
			return header.text();
		}

		return "";
	}

	@Override
	protected String getArticleAuthor(Document doc, Element article) {
		return "";
	}

	@Override
	protected String getArticleDate(Document doc, Element article) {
		return getArticleDetail(article, 0);
	}

	@Override
	protected String getArticleCategory(Document doc, Element article,
			String currentCategory) {
		return getArticleDetail(article, 1);
	}

	@Override
	protected String getArticleDetails(Document doc, Element article) {
		return getArticleDetail(article, 2);
	}

	private String getArticleDetail(Element article, int index) {
		Element details = article.getElementsByClass("details").first();
		if (details != null && details.childNodes().size() > index) {
			Node valueNode = details.childNodes().get(index);
			String value = "";
			if (valueNode instanceof TextNode) {
				value = ((TextNode) valueNode).text().trim();
			} else if (valueNode instanceof Element) {
				value = ((Element) valueNode).text().trim();
			}

			if (value.startsWith("-")) {
				value = value.substring(1).trim();
			}
			if (value.endsWith("-")) {
				value = value.substring(0, value.length() - 1).trim();
			}

			return value;
		}

		return "";
	}

	@Override
	protected String getArticleIntUrl(Document doc, Element article) {
		Element a = article.getElementsByTag("a").first();
		if (a != null) {
			return a.absUrl("href");
		}

		return "";
	}

	@Override
	protected String getArticleExtUrl(Document doc, Element article) {
		return "";
	}

	@Override
	protected String getArticleContent(Document doc, Element article) {
		Element p = article.getElementsByTag("p").first();
		if (p != null) {
			return getArticleText(p);
		}

		return "";
	}

	@Override
	protected Element getFullArticle(Document doc) {
		return doc.getElementsByClass("content").first();
	}

	@Override
	protected List<Element> getFullArticleCommentPosts(Document doc, URL intUrl) {
		Element linkToComments = doc.getElementsByClass("comments-label")
				.first();
		try {
			if (linkToComments != null) {
				Element a = linkToComments.getElementsByTag("a").first();
				if (a != null) {
					String url = a.absUrl("href");
					InputStream in = open(new URL(url));
					try {
						doc = DataUtil.load(in, "UTF-8", url.toString());
						return doc.getElementsByClass("b-post");
					} finally {
						in.close();
					}
				}
			}
		} catch (IOException e) {
		}

		return null;
	}

	@Override
	protected BasicElementProcessor getElementProcessorFullArticle() {
		return new BasicElementProcessor();
	}

	@Override
	protected List<Element> getCommentCommentPosts(Document doc,
			Element container) {
		return null;
	}

	@Override
	protected String getCommentId(Element post) {
		return post.id();
	}

	@Override
	protected String getCommentAuthor(Element post) {
		// We have an author, but no title, so, switch both:
		return "";
	}

	@Override
	protected String getCommentTitle(Element post) {
		// We have an author, but no title, so, switch both:
		Element author = post.getElementsByClass("author").first();
		if (author != null) {
			return author.text();
		}

		return "";
	}

	@Override
	protected String getCommentDate(Element post) {
		Element date = post.getElementsByTag("time").first();
		if (date != null) {
			return date.attr("datetime");
		}

		return "";
	}

	@Override
	protected Element getCommentContentElement(Element post) {
		return post.getElementsByClass("OLD__post-content-text").first();
	}

	@Override
	protected BasicElementProcessor getElementProcessorComment() {
		return new BasicElementProcessor() {
			@Override
			public boolean detectQuote(Node node) {
				if (node instanceof Element) {
					if (((Element) node).hasClass("quote_container")) {
						return true;
					}
				}

				return super.detectQuote(node);
			}

			@Override
			public boolean ignoreNode(Node node) {
				if (node instanceof Element) {
					if (((Element) node).hasClass("b-icon")) {
						return true;
					}
				}

				return super.ignoreNode(node);
			}
		};
	}

	@Override
	public void fetch(Story story) throws IOException {
		super.fetch(story);

		// First comment is a copy of the article, discard it
		List<Comment> comments = story.getComments();
		if (comments != null && comments.size() > 1) {
			comments = comments.subList(1, comments.size());
		}
		story.setComments(comments);
	}
}
