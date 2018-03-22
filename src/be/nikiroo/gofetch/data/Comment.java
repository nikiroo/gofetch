package be.nikiroo.gofetch.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Comment implements Iterable<Comment> {
	private String id;
	private String author;
	private String title;
	private String date;
	private List<String> lines;
	private List<Comment> children;

	public Comment(String id, String author, String title, String date,
			List<String> lines) {
		this.id = id;
		this.author = author;
		this.title = title;
		this.date = date;
		this.lines = lines;
		this.children = new ArrayList<Comment>();
	}

	public void add(Comment comment) {
		children.add(comment);
	}

	public void addAll(List<Comment> comments) {
		children.addAll(comments);
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @return the content
	 */
	public List<String> getContentLines() {
		return lines;
	}

	/**
	 * Find a comment or sub-comment by its id.
	 * 
	 * @param id
	 *            the id to look for F
	 * @return this if it has the given id, or a child of this if the child have
	 *         the given id, or NULL if not
	 */
	public Comment getById(String id) {
		if (id != null) {
			if (id.equals(this.id)) {
				return this;
			}

			for (Comment subComment : this) {
				if (id.equals(subComment.getId())) {
					return subComment;
				}
			}
		}

		return null;
	}

	public boolean isEmpty() {
		return children.isEmpty() && lines.isEmpty()
				&& ("" + author + title).trim().isEmpty();
	}

	@Override
	public Iterator<Comment> iterator() {
		return children.iterator();
	}
}
