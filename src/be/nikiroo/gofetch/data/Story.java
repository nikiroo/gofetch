package be.nikiroo.gofetch.data;

import java.net.URL;
import java.util.List;

import be.nikiroo.gofetch.support.BasicSupport;
import be.nikiroo.gofetch.support.Type;

/**
 * A news story.
 * 
 * @author niki
 */
public class Story {
	private Type type;
	private String id;
	private String title;
	private String author;
	private String date;
	private String category;
	private String details;
	private String urlInternal;
	private String urlExternal;
	private String content;

	private String fullContent;
	private List<Comment> comments;

	/**
	 * Create a news story.
	 * 
	 * @param type
	 *            the source {@link Type}
	 * @param id
	 *            the news ID
	 * @param title
	 *            the news title
	 * @param author
	 *            the author name for the details
	 * @param date
	 *            the post date for the details
	 * @param category
	 *            the category for the details
	 * @param details
	 *            some details to add to the title (author, date and category
	 *            will be added in the getter if available)
	 * @param urlInternal
	 *            the {@link URL} to get this news on the associated news site
	 * @param urlExternal
	 *            an external {@link URL} that serve as the news' source, if any
	 * @param content
	 *            the story content
	 */
	public Story(Type type, String id, String title, String author,
			String date, String category, String details, String urlInternal,
			String urlExternal, String content) {
		this.type = type;
		this.id = id;
		this.title = title;
		this.author = author;
		this.date = date;
		this.category = category;
		this.details = details;
		this.urlInternal = urlInternal;
		this.urlExternal = urlExternal;
		this.content = content;

		// Defaults fullContent to content
		this.fullContent = content;
	}

	public String getSelector() {
		return BasicSupport.getSelector(type) + id;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the details
	 */
	public String getDetails() {
		String details = "";

		if (category != null && !category.trim().isEmpty())
			details += "[" + category + "] ";
		if (date != null && !date.trim().isEmpty())
			details += date + " ";
		if (author != null && !author.trim().isEmpty())
			details += "(" + this.author + ") ";
		if (this.details != null && !this.details.trim().isEmpty())
			details += "\n" + this.details;

		return details.trim();
	}

	/**
	 * @return the url
	 */
	public String getUrlInternal() {
		return urlInternal;
	}

	/**
	 * @return the urlExternal
	 */
	public String getUrlExternal() {
		return urlExternal;
	}

	/**
	 * @return the body
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @return the fullContent
	 */
	public String getFullContent() {
		return fullContent;
	}

	/**
	 * @param fullContent
	 *            the fullContent to set
	 */
	public void setFullContent(String fullContent) {
		this.fullContent = fullContent;
	}

	/**
	 * @return the comments
	 */
	public List<Comment> getComments() {
		return comments;
	}

	/**
	 * @param comments
	 *            the comments to set
	 */
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	/**
	 * Find a comment or sub-comment by its id.
	 * 
	 * @param id
	 *            the id to look for F
	 * @return this if it has the given id, or a child of this if the child have
	 *         the given id, or NULL if not
	 */
	public Comment getCommentById(String id) {
		if (id != null && comments != null) {
			for (Comment comment : comments) {
				Comment found = comment.getById(id);
				if (found != null) {
					return found;
				}
			}
		}

		return null;
	}
}