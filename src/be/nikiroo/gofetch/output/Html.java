package be.nikiroo.gofetch.output;

import be.nikiroo.gofetch.data.Comment;
import be.nikiroo.gofetch.data.Story;
import be.nikiroo.gofetch.support.BasicSupport;
import be.nikiroo.gofetch.support.Type;
import be.nikiroo.utils.StringUtils;

public class Html extends Output {
	public Html(Type type, String hostname, String preselector, int port) {
		super(type, hostname, preselector, port);
	}

	@Override
	public String getMainIndexHeader() {
		String sel = preselector;
		if (!sel.isEmpty()) {
			sel = "/1" + sel;
		}

		String gopherUrl = "gopher://" + hostname + sel + ":" + port;

		StringBuilder builder = new StringBuilder();
		appendPre(builder);

		builder.append("<h2>News</h2>\n"//
				+ "<p>You will find here a few pages full of news, mirroring <a href='"
				+ gopherUrl + "'>"
				+ gopherUrl
				+ "</a>.</p>\n"//
				+ "<p>They are simply scrapped from their associated webpage and updated a few times a day.</p>\n"//
		);

		appendPost(builder);

		return builder.toString();
	}

	@Override
	public String getMainIndexFooter() {
		return "";
	}

	@Override
	public String getIndexHeader(BasicSupport support) {
		return "<h1>" + support.getDescription() + "</h1>\n<br/><br/>";
	}

	@Override
	public String getIndexFooter(BasicSupport support) {
		return "";
	}

	@Override
	public String exportHeader(Story story) {
		StringBuilder builder = new StringBuilder();

		appendPre(builder);

		builder.append("<div class='story-header'>\n");
		appendHtml(builder, story, true);
		builder.append("<hr/>\n");
		builder.append("</div>\n");

		appendPost(builder);

		return builder.toString();
	}

	@Override
	public String export(Story story) {
		StringBuilder builder = new StringBuilder();
		appendPre(builder);

		builder.append("<div class='story'>\n");
		appendHtml(builder, story, false);
		builder.append("<hr/>\n");

		if (story.getComments() != null) {
			for (Comment comment : story.getComments()) {
				appendHtml(builder, comment, "  ");
			}
		}

		builder.append("</div>\n");

		appendPost(builder);

		return builder.toString();
	}

	private void appendPre(StringBuilder builder) {
		builder.append("<!DOCTYPE html>\n");
		builder.append("<html>\n");
		builder.append("<head>\n");
		builder.append("  <meta http-equiv='content-type' content='text/html; charset=utf-8'>\n");
		builder.append("  <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
		builder.append("  <style type='text/css'>\n");
		builder.append("    body { margin: 1em 15%; }\n");
		builder.append("  </style>\n");
		builder.append("</head>\n");
		builder.append("<body>\n");
	}

	private void appendPost(StringBuilder builder) {
		builder.append("</body>\n");
	}

	private void appendHtml(StringBuilder builder, Comment comment, String space) {
		builder.append(space)
				.append("<div class='comment' style='display: block; margin-left: 80px'>\n");
		builder.append(space).append("  <h3>").append(comment.getTitle())
				.append("</h3>\n");
		builder.append(space)
				.append("  <div class='by' style='font-style: italic;'>")
				.append(comment.getAuthor()).append("</div>\n");
		builder.append(space).append("  <div class='comment_content'>");
		for (String line : comment.getContentLines()) {
			builder.append("<p>" + line + "</p>");
		}
		builder.append("</div>\n");
		for (Comment subComment : comment) {
			appendHtml(builder, subComment, space + "  ");
		}
		builder.append(space).append("</div>\n");
	}

	private StringBuilder appendHtml(StringBuilder builder, Story story,
			boolean resume) {
		if (resume) {
			builder.append("	<h2><a href='" + story.getId() + ".html'>"
					+ story.getTitle() + "</a></h2>\n");
		} else {
			builder.append("	<h2>" + story.getTitle() + "</h2>\n");
		}

		builder.append("	<div class='details'>");
		if (story.getDetails() != null && !story.getDetails().isEmpty()) {
			builder.append("(")
					.append(StringUtils.xmlEscape(story.getDetails()))
					.append(")");
		}
		builder.append("</div>\n");
		builder.append("	<br/>\n");

		if (!resume) {
			builder.append("    <ul>\n");
			builder.append("    <li>Reference: <a href=''>" + story.getId()
					+ "</a></li>\n");
			builder.append("        <li>News link: <a href='"
					+ story.getUrlInternal() + "'>" + story.getUrlInternal()
					+ "</a></li>\n");
			builder.append("        <li>Source link: <a href='"
					+ story.getUrlExternal() + "'>" + story.getUrlExternal()
					+ "</a></li>\n");
			builder.append("    </ul>\n");
			builder.append("	<br/>\n");
		}

		builder.append("	<div class='content' style='text-align: justify'>\n");
		if (resume) {
			builder.append("		" + StringUtils.xmlEscape(story.getContent())
					+ "\n");
		} else {
			builder.append("		"
					+ StringUtils.xmlEscape(story.getFullContent())
							.replace("\n", "<br/>").replace("[ ", "<h3>")
							.replace(" ]", "</h3>") + "\n");
		}
		builder.append("	</div>\n");

		return builder;
	}
}
