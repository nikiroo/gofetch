package be.nikiroo.gofetch.output;

import java.util.List;

import be.nikiroo.gofetch.data.Comment;
import be.nikiroo.gofetch.data.Story;
import be.nikiroo.gofetch.support.BasicSupport.Type;

public class Html extends Output {
	public Html(Type type) {
		super(type);
	}

	@Override
	public String getIndexHeader() {
		return "<h1>Hello world!</h1><br/>TODO\n";
	}

	@Override
	public String getIndexFooter() {
		return "";
	}

	@Override
	public String export(Story story) {
		return appendHtml(new StringBuilder(), story, true).append("<hr/>\n")
				.toString();
	}

	@Override
	public String export(Story story, List<Comment> comments) {
		StringBuilder builder = new StringBuilder();
		appendHtml(builder, story, false);

		// TODO: ext link and link

		builder.append("<hr/>");
		for (Comment comment : comments) {
			appendHtml(builder, comment, "  ");
		}

		return builder.toString();
	}

	private void appendHtml(StringBuilder builder, Comment comment, String space) {
		builder.append(space).append(
				"<div class='comment' style='display: block; margin-left: "
						+ (20 * space.length()) + "px'>");
		builder.append(space).append("  <h2>").append(comment.getTitle())
				.append("</h2>\n");
		builder.append(space).append("  <div class='by'>")
				.append(comment.getAuthor()).append("</div>\n");
		builder.append(space).append("  <div class='comment_content'>")
				.append(comment.getContent()).append("</div>\n");
		for (Comment subComment : comment) {
			appendHtml(builder, subComment, space + "  ");
		}
		builder.append(space).append("</div>");
	}

	private StringBuilder appendHtml(StringBuilder builder, Story story,
			boolean links) {
		// TODO
		builder.append("<div class='story'>");
		if (links) {
			builder.append("	<h1><a href='" + story.getId() + ".html'>"
					+ story.getTitle() + "</a></h1>");
		} else {
			builder.append("	<h1>" + story.getTitle() + "</h1>");
		}
		builder.append("	<div class='details'>(" + story.getDetails()
				+ ")</div>");
		builder.append("	<br/>");
		builder.append("	<div class='content'>");
		builder.append("		" + story.getContent());
		builder.append("	</div>");
		builder.append("</div>");

		return builder;
	}
}
