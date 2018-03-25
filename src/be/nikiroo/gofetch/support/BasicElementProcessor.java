package be.nikiroo.gofetch.support;

import org.jsoup.nodes.Node;

/**
 * A default {@link ElementProcessor} (will not detect or process anything
 * manually).
 * 
 * @author niki
 */
class BasicElementProcessor implements ElementProcessor {
	@Override
	public boolean detectQuote(Node node) {
		return false;
	}

	@Override
	public String processText(String text) {
		return text;
	}

	@Override
	public boolean ignoreNode(Node node) {
		return false;
	}

	@Override
	public String manualProcessing(Node node) {
		return null;
	}

	@Override
	public String isSubtitle(Node node) {
		return null;
	}
}
