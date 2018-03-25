package be.nikiroo.gofetch;

import java.io.File;
import java.io.IOException;

import be.nikiroo.gofetch.support.Type;

/**
 * This class is tha main entry point of the program. It will parse the
 * arguments, checks them (and warn-and-exit if they are invalid) then call
 * {@link Fetcher#start()}.
 * 
 * @author niki
 */
public class Main {
	/**
	 * Main entry point.
	 * 
	 * @param args
	 *            save-to-dir selector-subdir type max hostname port
	 * 
	 * @throws IOException
	 *             in case of I/O error
	 */
	public static void main(String[] args) throws IOException {
		if (args.length < 6) {
			System.err
					.println("Syntax error: gofecth [target dir] [selector] [type or 'ALL'] [max stories] [hostname] [port]");
			System.exit(1);
		}

		String dirStr = args[0];
		String preselectorStr = args[1];
		String typeStr = args[2];
		String maxStoriesStr = args[3];
		String hostnameStr = args[4];
		String portStr = args[5];

		// Dir
		File dir = new File(dirStr);
		dir.mkdirs();

		if (!dir.exists()) {
			System.err.println("Cannot open/create the root directory: "
					+ dirStr);
			System.exit(1);
		}

		if (dir.isFile()) {
			System.err
					.println("Root directory exists and is a file: " + dirStr);
			System.exit(1);
		}

		// Selector base :
		// - empty is ok
		// - DO NOT end with /
		// - always starts with / if not empty
		String preselector = "";
		if (preselectorStr != null && !preselectorStr.startsWith("/")) {
			preselector = "/" + preselectorStr;
		}
		while (preselector.endsWith("/")) {
			preselector = preselector.substring(0, preselector.length() - 1);
		}

		// Type to download
		Type type = null;
		if (!"ALL".equals(typeStr)) {
			try {
				type = Type.valueOf(typeStr.toUpperCase());
			} catch (IllegalArgumentException e) {
				System.err.println("Invalid type: " + typeStr);
				System.exit(1);
			}
		}

		// Max number of stories to display in the cache
		int maxStories = 0;
		try {
			maxStories = Integer.parseInt(maxStoriesStr);
		} catch (NumberFormatException e) {
			System.err
					.println("The maximum number of stories cannot be parsed: "
							+ maxStoriesStr);
			System.exit(1);
		}

		//
		String hostname = hostnameStr;

		//
		int port = 0;
		try {
			port = Integer.parseInt(portStr);
		} catch (NumberFormatException e) {
			System.err.println("The port cannot be parsed: " + portStr);
			System.exit(1);
		}

		if (port < 0 || port > 65535) {
			System.err.println("Invalid port number: " + portStr);
			System.exit(1);
		}

		new Fetcher(dir, preselector, type, maxStories, hostname, port).start();
	}
}