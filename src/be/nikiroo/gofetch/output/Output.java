package be.nikiroo.gofetch.output;

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
	 * The gopher hostname to use.
	 */
	protected String hostname;

	/**
	 * The sub directory and (pre-)selector to use for the resources.
	 */
	protected String preselector;

	/**
	 * The Gopher port to use.
	 */
	protected int port;

	/**
	 * Create a new {@link Output} class for the given type (which can be NULL).
	 * 
	 * @param type
	 *            the type or NULL for no type
	 * @param hostname
	 *            the gopher hostname to use
	 * @param preselector
	 *            the sub directory and (pre-)selector to use for the resources
	 * @param port
	 *            the Gopher port to use
	 */
	public Output(Type type, String hostname, String preselector, int port) {
		this.type = type;
		this.hostname = hostname;
		this.preselector = preselector;
		this.port = port;
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
	 * Export the header of a story (a <i>resume</i> mode).
	 * 
	 * @param story
	 *            the story
	 * 
	 * @return the resume
	 */
	abstract public String exportHeader(Story story);

	/**
	 * Export a full story with comments.
	 * 
	 * @param story
	 *            the story
	 * 
	 * @return the story
	 */
	abstract public String export(Story story);
}
