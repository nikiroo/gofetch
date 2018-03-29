# GoFetch
GoFetch is a simple web scrapper that outputs gopher-ready files.

## Synopsis

```gofetch``` [*dir*] [*selector*] [*type*] [*max*] [*hostname*] [*port*]

## Description

You point the program to your gopher directory, you launch it, and you have a
gopher view of the supported news sites.

The program will also helpfully create a ```gophermap``` and an ```index.html``` page for each supported website, as well as a general page to link to all the subpages (*selector*/```gophermap``` and *selector*/```index.html```).

### Supported websites

- Slashdot: News for nerds, stuff that matters!
- Pipedot: News for nerds, without the corporate slant
- LWN: Linux Weekly Newsletter
- Le Monde: Actualités et Infos en France et dans le monde
- The Register: Biting the hand that feeds IT
- TooLinux: Actualité généraliste sur Linux et les logiciels libres
- Ère Numérique.FR: faites le bon choix !
- Phoronix: news regarding free and open-source software

### Supported platforms

Any platform with at lest Java 1.6 on it should be ok.

## Options

- ```gofetch``` [*dir*] [*selector*] [*type*] [*max*] [*hostname*] [*port*]
- ```java -jar gofetch.jar``` [*dir*] [*selector*] [*type*] [*max*] [*hostname*] [*port*]

- *dir*: the target directory where to store the files
- *selector*: the gopher selector to prepend (also a sub-directory in [dir])
- *type*: the supported website (see the ```Supported websites``` section) in upper case or the special keyword ```ALL``` for all of them
- *max*: the maximum number of stories to show on the main page
- *hostname*: the gopher hostname
- *port*: the gopher port

## Compilation

```./configure.sh && make```

You can also import the java sources into, say, [Eclipse](https://eclipse.org/), and create a runnable JAR file from there.

#### Dependant libraries (included)

- libs/nikiroo-utils-sources.jar: some shared utility functions I also use elsewhere
- [libs/jsoup-sources.jar](https://jsoup.org/): a nice library to parse HTML

Nothing else but Java 1.6+.

Note that calling ```make libs``` will export the libraries into the src/ directory.

## Author

GoFetch was written by Niki Roo <niki@nikiroo.be>

