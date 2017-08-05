package be.nikiroo.gofetch.output;

import java.util.List;

import be.nikiroo.gofetch.data.Comment;
import be.nikiroo.gofetch.data.Story;
import be.nikiroo.gofetch.support.BasicSupport.Type;

/**
 * Base class for output operations.
 * 
 * @author niki
 */
public abstract class Output {
	/**
	 * The type of source, can be NULL for no-type.
	 */
	protected Type type;

	/**
	 * Create a new {@link Output} class for the given type (which can be NULL).
	 * 
	 * @param type
	 *            the type or NULL for no type
	 */
	public Output(Type type) {
		this.type = type;
	}

	/**
	 * Get the header to use in the index file.
	 * 
	 * @return the header
	 */
	abstract public String getIndexHeader();

	/**
	 * Get the footer to use in the index file.
	 * 
	 * @return the footer
	 */
	abstract public String getIndexFooter();

	/**
	 * Export a story (in resume mode).
	 * 
	 * @param story
	 *            the story
	 * 
	 * @return the resume
	 */
	abstract public String export(Story story);

	/**
	 * Export a full story with comments.
	 * 
	 * @param story
	 *            the story
	 * @param comments
	 *            the comments
	 * 
	 * @return the story
	 */
	abstract public String export(Story story, List<Comment> comments);
}
