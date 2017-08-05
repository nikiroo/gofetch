# GoFetch

GoFetch is a simple web scrapper that outputs gopher-ready files.
You point it to your gopher directory, you launch it, and you have a
gopher view of the supported news sites.

## Supported websites

- Slashdot: News for nerds, stuff that matters!

## Supported platforms

Any platform with at lest Java 1.6 on it should be ok.

## Usage

```java -jar gofetch.jar [dir] [selector] [type] [max] [hostname] [port]```

- dir: the target directory where to store the files
- selector: the gopher selector to prepend (also a sub-directory in [dir])
- max: the maximum number of stories to show on the main page
- hostname: the gopher hostname
- port: the gopher port

## Compilation

```./configure.sh && make```

You can also import the java sources into, say, [Eclipse](https://eclipse.org/), and create a runnable JAR file from there.

### Dependant libraries (included)

- libs/nikiroo-utils-sources.jar: some shared utility functions I also use elsewhere
- [libs/jsoup-sources.jar](https://jsoup.org/): a nice library to parse HTML

Nothing else but Java 1.6+.

Note that calling ```make libs``` will export the libraries into the src/ directory.

