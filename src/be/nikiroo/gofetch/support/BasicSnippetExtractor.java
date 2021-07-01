package be.nikiroo.gofetch.support;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import be.nikiroo.gofetch.data.Story;

/**
 * Data extractor for a snippet (what we see about an article on a articles
 * listing page).
 * 
 * @author niki
 */
abstract public class BasicSnippetExtractor {
	/**
	 * The article {@link Element}s of this document.
	 * 
	 * @param doc
	 *            the main document for the current category
	 * 
	 * @return the articles
	 */
	abstract protected List<Element> getSnippets(Document doc);

	/**
	 * The ID of the article (defaults to the date element if empty).
	 * 
	 * @param doc
	 *            the main document for the current category
	 * @param article
	 *            the article to look into
	 * 
	 * @return the ID
	 */
	abstract protected String getArticleId(Document doc, Element article);

	/**
	 * The article title to display.
	 * 
	 * @param doc
	 *            the main document for the current category
	 * @param article
	 *            the article to look into
	 * 
	 * @return the title
	 */
	abstract protected String getArticleTitle(Document doc, Element article);

	/**
	 * The optional article author.
	 * 
	 * @param doc
	 *            the main document for the current category
	 * @param article
	 *            the article to look into
	 * 
	 * @return the author
	 */
	abstract protected String getArticleAuthor(Document doc, Element article);

	/**
	 * The optional article date.
	 * 
	 * @param doc
	 *            the main document for the current category
	 * @param article
	 *            the article to look into
	 * 
	 * @return the date
	 */
	abstract protected String getArticleDate(Document doc, Element article);

	/**
	 * the optional article category.
	 * 
	 * @param doc
	 *            the main document for the current category
	 * @param article
	 *            the article to look into
	 * @param currentCategory
	 *            the currently listed category if any (can be NULL)
	 * 
	 * @return the category
	 */
	abstract protected String getArticleCategory(Document doc, Element article,
			String currentCategory);

	/**
	 * the optional details of the article (can replace the date, author and
	 * category, for instance).
	 * 
	 * @param doc
	 *            the main document for the current category
	 * @param article
	 *            the article to look into
	 * 
	 * @return the details
	 */
	abstract protected String getArticleDetails(Document doc, Element article);

	/**
	 * The (required) {@link URL} that points to the news page on the supported
	 * website.
	 * 
	 * @param doc
	 *            the main document for the current category
	 * @param article
	 *            the article to look into
	 * 
	 * @return the internal {@link URL}
	 */
	abstract protected String getArticleIntUrl(Document doc, Element article);

	/**
	 * the optional {@link URL} that points to an external website for more
	 * information.
	 * 
	 * @param doc
	 *            the main document for the current category
	 * @param article
	 *            the article to look into
	 * 
	 * @return the external {@link URL}
	 */
	abstract protected String getArticleExtUrl(Document doc, Element article);

	/**
	 * The optional article short-content (not the full content, that will be
	 * fetched by {@link BasicSupport#fetch(Story)}).
	 * 
	 * @param doc
	 *            the main document for the current category
	 * @param article
	 *            the article to look into
	 * 
	 * @return the short content
	 */
	abstract protected String getArticleContent(Document doc, Element article);

	List<Story> fetchSnippets(Document doc, Type type, String defaultCateg) {
		List<Story> list = new ArrayList<Story>();

		List<Element> articles = getSnippets(doc);
		for (Element article : articles) {
			String id = getArticleId(doc, article).trim();
			String title = getArticleTitle(doc, article).trim();
			String author = getArticleAuthor(doc, article).trim();
			String date = getArticleDate(doc, article).trim();
			String categ = getArticleCategory(doc, article, defaultCateg)
					.trim();
			String details = getArticleDetails(doc, article).trim();
			String intUrl = getArticleIntUrl(doc, article).trim();
			String extUrl = getArticleExtUrl(doc, article).trim();
			String content = getArticleContent(doc, article).trim();

			if (id.isEmpty() && date.isEmpty()) {
				continue;
			}

			if (!id.isEmpty()) {
				while (id.length() < 10) {
					id = "0" + id;
				}
			} else {
				id = date.replace(":", "_").replace("+", "_").replace("/", "-");
			}

			date = BasicSupport.date(date);

			list.add(new Story(type, id, title, author, date, categ, details,
					intUrl, extUrl, content));
		}

		return list;
	}
}
