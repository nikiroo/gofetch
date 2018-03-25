package be.nikiroo.gofetch.output;

import be.nikiroo.gofetch.data.Comment;
import be.nikiroo.gofetch.data.Story;
import be.nikiroo.gofetch.support.Type;
import be.nikiroo.utils.StringUtils;
import be.nikiroo.utils.StringUtils.Alignment;

public class Gopher extends Output {
	static private final int LINE_SIZE = 67;

	public Gopher(Type type, String hostname, String preselector, int port) {
		super(type, hostname, preselector, port);
	}

	@Override
	public String getIndexHeader() {
		StringBuilder builder = new StringBuilder();

		appendCenter(builder, true, "NEWS", "", true);
		appendLeft(builder, true, "", "");
		appendLeft(builder, true,
				"You will find here a few pages full of news.", "");
		appendLeft(builder, true, "", "");
		appendLeft(
				builder,
				true,
				"They are simply scrapped from their associated webpage and converted into a gopher friendly format, updated a few times a day.",
				"");
		appendLeft(builder, true, "", "");

		return builder.toString();
	}

	@Override
	public String getIndexFooter() {
		return "";
	}

	@Override
	public String exportHeader(Story story) {
		return append(new StringBuilder(), story, true).toString();
	}

	@Override
	public String export(Story story) {
		StringBuilder builder = new StringBuilder();
		append(builder, story, false);

		builder.append("\r\n");

		if (story.getComments() != null) {
			for (Comment comment : story.getComments()) {
				append(builder, false, comment, "  ");
			}
		}

		builder.append("\r\n");

		return builder.toString();
	}

	private StringBuilder append(StringBuilder builder, boolean menu,
			Comment comment, String space) {

		if (space.length() > LINE_SIZE - 20) {
			space = space.substring(0, LINE_SIZE - 20);
		}

		appendLeft(builder, menu, comment.getTitle(), "** ", "   ", space);

		if (comment.getAuthor() != null
				&& !comment.getAuthor().trim().isEmpty()) {
			appendLeft(builder, menu, "(" + comment.getAuthor() + ")", "   ",
					"   ", space);
			builder.append((menu ? "i" : "") + "\r\n");
		}

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

			appendLeft(builder, menu, line, prep, prep, space);
		}

		builder.append((menu ? "i" : "") + "\r\n");
		for (Comment subComment : comment) {
			append(builder, menu, subComment, space + "   ");
			builder.append((menu ? "i" : "") + "\r\n");
		}

		return builder;
	}

	private StringBuilder append(StringBuilder builder, Story story,
			boolean resume) {
		if (!resume) {
			appendCenter(builder, false, story.getTitle(), "  ", true);
			builder.append("\r\n");
			appendJustified(builder, false, story.getDetails(), "  ");
			builder.append("\r\n");

			builder.append("  o News link: ").append(story.getUrlInternal())
					.append("\r\n");
			builder.append("  o Source link: ").append(story.getUrlExternal())
					.append("\r\n");
			builder.append("\r\n");

			builder.append("\r\n");

			appendJustified(builder, false, story.getFullContent(), "    ");
			builder.append("\r\n");
		} else {
			builder.append('0').append(story.getTitle()) //
					.append('\t').append(story.getSelector()) //
					.append('\t').append(hostname) //
					.append('\t').append(port) //
					.append("\r\n");
			appendJustified(builder, true, story.getDetails(), "  ");
			builder.append("i\r\n");

			String content = story.getContent();
			if (!content.isEmpty()) {
				appendJustified(builder, true, content, "    ");
				builder.append("i\r\n");
			}
		}

		return builder;
	}

	// note: adds "i"
	private static void appendCenter(StringBuilder builder, boolean menu,
			String text, String space, boolean allCaps) {
		if (allCaps) {
			text = text.toUpperCase();
		}

		int size = LINE_SIZE - space.length();
		for (String line : StringUtils
				.justifyText(text, size, Alignment.CENTER)) {
			builder.append(menu ? "i" : "") //
					.append(space) //
					.append(line) //
					.append("\r\n");
		}
	}

	private static void appendJustified(StringBuilder builder, boolean menu,
			String text, String space) {
		for (String line : text.split("\n")) {
			int size = LINE_SIZE - space.length();
			for (String subline : StringUtils.justifyText(line, size,
					Alignment.JUSTIFY)) {
				builder.append(menu ? "i" : "") //
						.append(space) //
						.append(subline) //
						.append("\r\n");
			}
		}
	}

	private static void appendLeft(StringBuilder builder, boolean menu,
			String text, String space) {
		appendLeft(builder, menu, text, "", "", space);
	}

	private static void appendLeft(StringBuilder builder, boolean menu,
			String text, String prependFirst, String prependOthers, String space) {
		String prepend = prependFirst;
		for (String line : text.split("\n")) {
			for (String subline : StringUtils.justifyText(line, LINE_SIZE
					- space.length(), Alignment.LEFT)) {
				builder.append(menu ? "i" : "") //
						.append(space) //
						.append(prepend) //
						.append(subline) //
						.append("\r\n");
				prepend = prependOthers;
			}
		}
	}
}
