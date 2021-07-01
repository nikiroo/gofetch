package be.nikiroo.gofetch.support;

import java.net.URL;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import be.nikiroo.gofetch.data.Story;

/**
 * Data extractor for a full article (the article on its own page)
 * 
 * @author niki
 */
abstract public class BasicFullArticleExtractor {
	/**
	 * TODO: often called via a hack for Snippets handling, need to fix that...
	 * <p>
	 * Return the text from this {@link Element}, using the
	 * {@link BasicSupport#getElementProcessorFullArticle()} processor logic.
	 * 
	 * @param article
	 *            the element to extract the text from
	 * 
	 * @return the text
	 */
	protected String getArticleText(Element article) {
		StringBuilder builder = new StringBuilder();
		BasicElementProcessor eProc = getElementProcessorFullArticle();
		if (eProc != null) {
			for (String line : BasicSupport.toLines(article, eProc)) {
				builder.append(line + "\n");
			}
		} else {
			builder.append(article.text());
		}

		// Content is too tight with a single break per line:
		return builder.toString().replace("\n", "\n\n") //
				.replace("\n\n\n\n", "\n\n") //
				.replace("\n\n\n\n", "\n\n") //
				.trim();
	}

	/**
	 * Return the full article if available (this is the article to retrieve
	 * from the newly downloaded page at {@link Story#getUrlInternal()}).
	 * 
	 * @param doc
	 *            the (full article) document to work on
	 * 
	 * @return the article or NULL
	 */
	abstract protected Element getFullArticle(Document doc);

	/**
	 * The {@link ElementProcessor} to use to convert the main article element
	 * (see {@link BasicSupport#getFullArticle(Document)}) into text.
	 * <p>
	 * See {@link BasicElementProcessor} for a working, basic implementation.
	 * <p>
	 * Can be NULL to simply use {@link Element#text()}.
	 * 
	 * @return the processor, or NULL
	 */
	abstract protected BasicElementProcessor getElementProcessorFullArticle();

	/**
	 * This {@link Story} is ready, you can intercept this method if you need to
	 * finalise something.
	 * 
	 * @param story
	 *            the complete Story, comments included
	 * @param doc
	 *            the <b>article</b> document (not the main page)
	 * @param el
	 *            the article main element
	 */
	protected void ready(Story story, Document doc, Element el) {
	}

	void fetchFullArticle(URL url, Document doc, Story story) {
		String fullContent = "";

		Element article = getFullArticle(doc);
		if (article != null) {
			fullContent = getArticleText(article);
		}

		if (fullContent.isEmpty()) {
			fullContent = story.getContent();
		}

		story.setFullContent(fullContent);

		ready(story, doc, article);
	}
}
