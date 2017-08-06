package be.nikiroo.gofetch.output;

import java.util.List;

import be.nikiroo.gofetch.data.Comment;
import be.nikiroo.gofetch.data.Story;
import be.nikiroo.gofetch.support.BasicSupport.Type;

public class Html extends Output {
	public Html(Type type, String hostname, String preselector, int port) {
		super(type, hostname, preselector, port);
	}

	@Override
	public String getIndexHeader() {
		String gopherUrl = "gopher://" + hostname + preselector + ":" + port;

		return "<h1>News</h1>\n"//
				+ "<p>You will find here a few pages full of news, mirroring <a href='"
				+ gopherUrl + "'>"
				+ gopherUrl
				+ "</a>.</p>\n"//
				+ "<br/>\n"//
				+ "<p>They are simply scrapped from their associated webpage and updated a few times a day.</p>\n"//
				+ "<br/>\n"//
		;
	}

	@Override
	public String getIndexFooter() {
		return "";
	}

	@Override
	public String export(Story story) {
		StringBuilder builder = new StringBuilder();

		builder.append("<div class='story-header'>\n");
		appendHtml(builder, story, true);
		builder.append("<hr/>\n");
		builder.append("</div>\n");

		return builder.toString();
	}

	@Override
	public String export(Story story, List<Comment> comments) {
		StringBuilder builder = new StringBuilder();

		builder.append("<div class='story'>\n");
		appendHtml(builder, story, false);
		builder.append("<hr/>\n");

		if (comments != null) {
			for (Comment comment : comments) {
				appendHtml(builder, comment, "  ");
			}
		}

		builder.append("</div>\n");

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
			boolean resume) {
		if (resume) {
			builder.append("	<h1><a href='" + story.getId() + ".html'>"
					+ story.getTitle() + "</a></h1>\n");
		} else {
			builder.append("	<h1>" + story.getTitle() + "</h1>\n");
		}
		builder.append("	<div class='details'>(" + story.getDetails()
				+ ")</div>\n");
		builder.append("	<br/>\n");

		if (!resume) {
			builder.append("    <ul>\n");
			builder.append("        <li>News link: <a href='"
					+ story.getUrlInternal() + "'>" + story.getUrlInternal()
					+ "</a></li>\n");
			builder.append("        <li>Source link: <a href='"
					+ story.getUrlExternal() + "'>" + story.getUrlExternal()
					+ "</a></li>\n");
			builder.append("    </ul>\n");
			builder.append("	<br/>\n");
		}

		builder.append("	<div class='content'>\n");
		builder.append("		" + story.getContent() + "\n");
		builder.append("	</div>\n");

		return builder;
	}
}
