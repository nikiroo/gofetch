package be.nikiroo.gofetch.output;

import be.nikiroo.gofetch.StringJustifier;
import be.nikiroo.gofetch.data.Comment;
import be.nikiroo.gofetch.data.Story;
import be.nikiroo.gofetch.support.BasicSupport.Type;

public class Gopher extends Output {
	static private final int LINE_SIZE = 67;

	public Gopher(Type type, String hostname, String preselector, int port) {
		super(type, hostname, preselector, port);
	}

	@Override
	public String getIndexHeader() {
		StringBuilder builder = new StringBuilder();

		appendCenter(builder, "NEWS", true);
		appendLeft(builder, "", "");
		appendLeft(builder, "You will find here a few pages full of news.", "");
		appendLeft(builder, "", "");
		appendLeft(
				builder,
				"They are simply scrapped from their associated webpage and converted into a gopher friendly format, updated a few times a day.",
				"");
		appendLeft(builder, "", "");

		return builder.toString();
	}

	@Override
	public String getIndexFooter() {
		return "";
	}

	@Override
	public String exportHeader(Story story) {
		return append(new StringBuilder(), story, true).append("i\r\ni\r\n")
				.toString();
	}

	@Override
	public String export(Story story) {
		StringBuilder builder = new StringBuilder();
		append(builder, story, false);

		builder.append("i\r\n");

		if (story.getComments() != null) {
			for (Comment comment : story.getComments()) {
				append(builder, comment, "");
			}
		}

		builder.append("i\r\n");

		return builder.toString();
	}

	private StringBuilder append(StringBuilder builder, Comment comment,
			String space) {

		if (space.length() > LINE_SIZE - 20) {
			space = space.substring(0, LINE_SIZE - 20);
		}

		appendLeft(builder, comment.getTitle(), "** ", "   ", space);
		appendLeft(builder, "(" + comment.getAuthor() + ")", "   ", "   ",
				space);

		builder.append("i\r\n");

		for (String line : comment.getContentLines()) {
			int depth = 0;
			while (line.length() > depth && line.charAt(depth) == '>') {
				depth++;
			}
			line = line.substring(depth).trim();

			String prep = "   ";
			for (int i = 0; i < depth; i++) {
				prep += ">";
			}

			if (depth > 0) {
				prep += " ";
			}

			appendLeft(builder, line, prep, prep, space);
		}

		builder.append("i\r\n");
		for (Comment subComment : comment) {
			append(builder, subComment, space + "   ");
			builder.append("i\r\n");
		}

		return builder;
	}

	private StringBuilder append(StringBuilder builder, Story story,
			boolean resume) {
		if (!resume) {
			appendCenter(builder, story.getTitle(), true);
			builder.append("i\r\n");
			appendJustified(builder, story.getDetails(), "  ");
			builder.append("i\r\n");

			builder.append("i  o News link: ").append(story.getUrlInternal())
					.append("\r\n");
			builder.append("i  o Source link: ").append(story.getUrlExternal())
					.append("\r\n");
			builder.append("i\r\n");

			builder.append("i\r\n");

			appendJustified(builder, story.getFullContent(), "    ");
		} else {
			builder.append('1').append(story.getTitle()) //
					.append('\t').append(story.getSelector()) //
					.append('\t').append(hostname) //
					.append('\t').append(port) //
					.append("\r\n");
			appendJustified(builder, story.getDetails(), "  ");
			builder.append("i\r\n");

			appendJustified(builder, story.getContent(), "    ");
		}

		builder.append("i\r\n");

		return builder;
	}

	// note: adds "i"
	private static void appendCenter(StringBuilder builder, String text,
			boolean allCaps) {
		if (allCaps) {
			text = text.toUpperCase();
		}

		for (String line : StringJustifier.center(text, LINE_SIZE)) {
			builder.append("i").append(line).append("\r\n");
		}
	}

	// note: adds "i"
	private static void appendJustified(StringBuilder builder, String text,
			String space) {
		for (String line : StringJustifier.full(text,
				LINE_SIZE - space.length())) {
			builder.append("i").append(line).append("\r\n");
		}
	}

	// note: adds "i"
	private static void appendLeft(StringBuilder builder, String text,
			String space) {
		appendLeft(builder, text, "", "", space);
	}

	// note: adds "i"
	private static void appendLeft(StringBuilder builder, String text,
			String prependFirst, String prependOthers, String space) {
		String prepend = prependFirst;
		for (String line : StringJustifier.left(text,
				LINE_SIZE - space.length())) {
			builder.append("i").append(space).append(prepend).append(line)
					.append("\r\n");
			prepend = prependOthers;
		}
	}
}
