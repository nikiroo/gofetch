package be.nikiroo.gofetch.support;

import org.jsoup.nodes.Node;

/**
 * Used to process an element into lines.
 * 
 * @author niki
 */
class BasicElementProcessor {
	/**
	 * Detect if this node is a quote and should be treated as such.
	 * 
	 * @param node
	 *            the node to check
	 * @return TRUE if it is
	 */
	public boolean detectQuote(Node node){
		return false;
	}

	/**
	 * Process text content (will be called on each text element, allowing you
	 * to modify it if needed).
	 * 
	 * @param text
	 *            the text to process
	 * 
	 * @return the resulting text
	 */
	public String processText(String text) {
		return text;
	}

	/**
	 * Ignore this node.
	 * 
	 * @param node
	 *            the node to ignore
	 * @return TRUE if it has to be ignored
	 */
	public boolean ignoreNode(Node node){
		return false;
	}

	/**
	 * Manually process this node (and return the manual processing value) if so
	 * desired.
	 * <p>
	 * If the node is manually processed, it and its children will not be
	 * automatically processed.
	 * 
	 * @param node
	 *            the node to optionally process
	 * 
	 * @return NULL if not processed (will thus be automatically processed as
	 *         usual), a {@link String} (may be empty) if we process it manually
	 *         -- the given {@link String} will be used instead of the usual
	 *         automatic processing if not NULL
	 */
	public String manualProcessing(Node node){
		return null;
	}

	/**
	 * This {@link Node} is a subtitle and should be treated as such
	 * (highlighted).
	 * 
	 * @param node
	 *            the node to check
	 * 
	 * @return NULL if it is not a subtitle, the subtitle to use if it is
	 */
	public String isSubtitle(Node node){
		return null;
	}
}