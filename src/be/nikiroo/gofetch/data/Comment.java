package be.nikiroo.gofetch.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Comment implements Iterable<Comment> {
	private String id;
	private String author;
	private String title;
	private String date;
	private String content;
	private List<Comment> children;

	public Comment(String id, String author, String title, String date,
			String content) {
		this.id = id;
		this.author = author;
		this.title = title;
		this.date = date;
		this.content = content;
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
	public String getContent() {
		return content;
	}

	public boolean isEmpty() {
		return children.isEmpty()
				&& ("" + author + title + content).trim().isEmpty();
	}

	@Override
	public Iterator<Comment> iterator() {
		return children.iterator();
	}
}
