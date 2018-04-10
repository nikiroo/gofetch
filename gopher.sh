#!/bin/sh

# $0 [server] ([selector]) ([port]) ([mode])
# 	server: the gopher server to dig to
#	selector: the gopher selector (default is empty)
#	port: the port to use (default is 70)
#	mode: the filetype mode (default depends upon selector)
#		0: plain text
#		1: menu (dir-like)
#		...
#		download: fake mode to download the result without changes

# ENV variables:
#	LESS: will be used with 'less' (think: "export LESS=-r")
#	LINK_COLOR: escape sequences colour (def: 2)
#		- : means no escape sequence
#		1 : means colour 1
#		2 : means colour 2
#		[...]

SERVER="$1"
SELECTOR="$2"
PORT="$3"
MODE="$4"

# Defaults:
[ "$PORT" = "" ] && PORT=70
if [ "$MODE" = "" ]; then
	# "" or dir-like selector? -> 1 ; if not -> 0 
	echo "$SELECTOR" | grep "/$" >/dev/null && MODE=1 || MODE=0
	[ "$SELECTOR" = "" ] && MODE=1
fi

if [ "$SERVER" = "" ]; then
	echo "Syntax error: $0 [SERVER] ([SELECTOR]) ([PORT]) ([MODE])" >&2
	exit 2
fi

# can be "-" for no escape sequences
[ "$LINK_COLOR" = ""  ] && LINK_COLOR=2

# Start and end link tags
SL=
EL=
if [ "$LINK_COLOR" != "-" ]; then
	SL="`tput setf $LINK_COLOR``tput setaf $LINK_COLOR`"
	EL="`tput init`";
fi

PREFIX="[0-9hIg]"

# $0 [FILE]
# Display a gopher menu for the given resource
cat_menu() {
	i=0
	cat "$1" | grep "^i\|^$PREFIX" | while read ln; do
		ln="`echo "$ln" | cut -f1`"
		if echo "$ln" | grep "^i" >/dev/null 2>&1; then
			echo "$ln" | sed "s:^.:	:g"
		elif echo "$ln" | grep "^$PREFIX" >/dev/null 2>&1; then
			i=`expr $i + 1`
			i=`printf %2.f $i`
			field="`echo "$ln" | cut -c1`"
			case "$field" in
				0) typ='TXT';;
				1) typ='DIR';; # menu, actually
				7) typ='(?)';; # query
				8) typ='TEL';; # TELnet (not TELephone)
				h) typ='WEB';; # HTML
				g) typ='GIF';;
				I) typ='IMG';;
				+) typ='SVR';; # redundant server
				*) typ='!!!';;
			esac
			echo "$ln" | sed "s:^.\\(.*\\):$typ $i	$SL\\1$EL:g"
		#else
			# Bad line
		fi
	done
}

# $0 [FILE] [INDEX] [FIELD]
# Get a field from the given-by-index link in FILE
#
# Fields:
# 	1 = type/name
# 	2 = selector
# 	3 = server
# 	4 = port
getsel() {
	cat "$1" | grep "^$PREFIX" | tail -n+"$2" | head -n 1 | cut -f"$3"
}

# Save page content to 'tmp' file
tmp="`mktemp -t gofetch.current_page.XXXXXX`"
finish() {
  rm -rf "$tmp" "$tmp.jpg"
}
trap finish EXIT

if [ $MODE = 1 ]; then
	echo "$SELECTOR" | nc "$SERVER" "$PORT" | sed 's:\r::g' > "$tmp"
else
	echo "$SELECTOR" | nc "$SERVER" "$PORT" > "$tmp"
fi

# Process page content
case "$MODE" in
download)
	# Special, fake mode, only from top-level
	cat "$tmp"
;;
0)
	cat "$tmp" | less
;;
1)
	CHOICE=start
	while [ "$CHOICE" != "" ]; do
		cat_menu "$tmp" | less
		read -p "[$SELECTOR]: " CHOICE
		index="`expr 1 \* "$CHOICE" 2>/dev/null`"
		if [ "$index" != "" ]; then
			goto_server="`getsel "$tmp" $index 3`"
			goto_sel="`getsel "$tmp" $index 2`"
			goto_port="`getsel "$tmp" $index 4`"
			goto_mode="`getsel "$tmp" $index 1 | cut -c1`"
			sh "$0" "$goto_server" "$goto_sel" "$goto_port" "$goto_mode"
		fi
	done
;;
7)
	read -p "Query: " SEARCH
	selQuery="$SELECTOR	$SEARCH"
	
	exec sh "$0" "$SERVER" "$selQuery" "$PORT" 1
;;
8)
	telnet -l "$SELECTOR" "$SERVER" "$PORT"
	read DUMMY
;;
9)
	echo "<BINARY FILE>" | less
;;
g|I)
	if img2aa --help >/dev/null 2>&1; then
		img2aa --mode=DITHERING \
			--width=74 "$tmp" | less
	elif jp2a -h >/dev/null 2>&1; then
		if convert -h >/dev/null 2>&1; then
			convert "$tmp" "$tmp.jpg"
			jp2a --border --chars=" .-+=o8#"\
				--width=74 "$tmp.jpg" | less
		else
			echo "required program not found to view images: convert" \
				| less
		fi
	else
		echo "required program not found to view images:" \
			jp2a or img2aa | less
	fi
;;
*)
	echo "unknwon selector mode: <$MODE>" | less
	exit 3
;;
esac
