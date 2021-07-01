package be.nikiroo.gofetch.support;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import be.nikiroo.gofetch.data.Comment;
import be.nikiroo.gofetch.data.Story;

abstract public class BasicCommentExtractor {
	/**
	 * Return the list of comment {@link Element}s from this optional container
	 * -- must <b>NOT</b> return the "container" as a comment {@link Element}.
	 * 
	 * @param doc
	 *            the (full article) document to work on
	 * @param intUrl
	 *            the internal {@link URL} this article was taken from (the
	 *            {@link URL} from the supported website)
	 * 
	 * @return the list of comment posts
	 */
	abstract protected List<Element> getFullArticleCommentPosts(Document doc,
			URL intUrl);

	/**
	 * Compute the ID of the given comment element.
	 * 
	 * @param post
	 *            the comment element
	 * 
	 * @return the ID
	 */
	abstract protected String getCommentId(Element post);

	/**
	 * Compute the author of the given comment element.
	 * 
	 * @param post
	 *            the comment element
	 * 
	 * @return the author
	 */
	abstract protected String getCommentAuthor(Element post);

	/**
	 * Compute the title of the given comment element.
	 * 
	 * @param post
	 *            the comment element
	 * 
	 * @return the title
	 */
	abstract protected String getCommentTitle(Element post);

	/**
	 * Compute the date of the given comment element.
	 * 
	 * @param post
	 *            the comment element
	 * 
	 * @return the date
	 */
	abstract protected String getCommentDate(Element post);

	/**
	 * Get the main of the given comment element, which can be NULL.
	 * 
	 * @param post
	 *            the comment element
	 * 
	 * @return the element
	 */
	abstract protected Element getCommentContentElement(Element post);

	/**
	 * The {@link ElementProcessor} to use to convert the main comment element
	 * (see {@link BasicSupport#getCommentContentElement(Element)}) into text.
	 * <p>
	 * See {@link BasicElementProcessor} for a working, basic implementation.
	 * <p>
	 * Can be NULL to simply use {@link Element#text()}.
	 * 
	 * @return the processor
	 */
	abstract protected BasicElementProcessor getElementProcessorComment();

	/**
	 * Return the list of subcomment {@link Element}s from this comment element
	 * -- must <b>NOT</b> return the "container" as a comment {@link Element}.
	 * 
	 * @param doc
	 *            the (full article) document to work on
	 * @param container
	 *            the container (a comment {@link Element})
	 * 
	 * @return the list of comment posts
	 */
	abstract protected List<Element> getCommentCommentPosts(Document doc,
			Element container);

	List<Comment> findComments(Document doc, List<Element> posts) {
		List<Comment> comments = new ArrayList<Comment>();
		if (posts != null) {
			for (Element post : posts) {
				String id = getCommentId(post).trim();
				String author = getCommentAuthor(post).trim();
				String title = getCommentTitle(post).trim();
				String date = getCommentDate(post).trim();

				List<String> content = new ArrayList<String>();

				if (id.isEmpty()) {
					id = date;
				}

				date = BasicSupport.date(date);

				Element contentE = getCommentContentElement(post);
				if (contentE != null) {
					BasicElementProcessor eProc = getElementProcessorComment();
					if (eProc != null) {
						for (String line : BasicSupport
								.toLines(contentE, eProc)) {
							content.add(line);
						}
					} else {
						content = Arrays.asList(contentE.text().split("\n"));
					}
				}

				Comment comment = new Comment(id, author, title, date, content);
				comment.addAll(findComments(doc,
						getCommentCommentPosts(doc, post)));

				if (!comment.isEmpty()) {
					comments.add(comment);
				}
			}
		}

		return comments;
	}

	void fetchComments(URL url, Document doc, Story story) {
		List<Element> posts = getFullArticleCommentPosts(doc, url);
		story.setComments(findComments(doc, posts));
	}
}
