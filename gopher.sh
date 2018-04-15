#!/bin/sh

# $0 [gopher uri] ([mode])
# 	uri: the gopher URI to dig to, e.g.:
#		- gopher://sdf.org:70/1/faq
#		- sdf.org/faq
#		- sdf.org/1/faq
#		- http://gopher.nikiroo.be:80/news
#		- ...
#		default port is 70 for gopher, 80 for http, 443 for https
#		default filetype depends upon selector:
#		- 0: plain text
#		- 1: menu (dir-like)
#		- ...
#		- download: fake mode to download the result without changes

# Manual:
# 	When asked for a number (non-dialog uses only), you can also
#	enter 'n' or 'p' to get the next or previous item (+1 or -1)

# ENV variables:
# 	USE_DIALOG: force the usage of the command 'dialog'
#		  : true if dialog is found
#		0 : do not
#		1 : force the use of dialog
#	LINK_COLOR: escape sequences colour (def: 2)
#		- : means no escape sequence
#		1 : means colour 1
#		2 : means colour 2
#		[...]
# 	INVERT    : invert the output for image viewing (for white backgrounds)
# 		0 : do not invert (default)
#		1 : invert 

# EXIT Codes:
#	  0: ok
#	  1: syntax error
#	  2: cannot contact server
#	  3: unknown selector mode
#	255: special exit more 'q'

URI="$1"

PREFIX="[0-9hIg+]"

PROTOCOL="`echo $URI | sed 's|^\([^:]*://\)\?\([^:/]*\)\(:[^/]*\)\?/\?\(.*\)\?|\1|g'`"
SERVER="`echo $URI | sed 's|^\([^:]*://\)\?\([^:/]*\)\(:[^/]*\)\?/\?\(.*\)\?|\2|g'`"
PORT="`echo $URI | sed 's|^\([^:]*://\)\?\([^:/]*\)\(:[^/]*\)\?/\?\(.*\)\?|\3|g'`"
SELECTOR="`echo $URI | sed 's|^\([^:]*://\)\?\([^:/]*\)\(:[^/]*\)\?/\?\(.*\)\?|\4|g'`"
MODE=

PORT="`echo "$PORT" | sed 's/^://'`"

# Defaults:
if [ "$PORT" = "" ];then
	if [ "$PROTOCOL" = http ]; then
		PORT=80
	elif [ "$PROTOCOL" = https ]; then
		PORT=443
	else
		PORT=70
	fi
fi

if [ "$MODE" = "" ]; then
	# "" or dir-like selector? -> 1 ; if not -> 0 
	echo "$SELECTOR" | grep "/$" >/dev/null && MODE=1 || MODE=0
	[ "$SELECTOR" = "" ] && MODE=1
	
	# check explicit modes:
	if echo "$SELECTOR" | grep "^/\?\($PREFIX\|download\)/" >/dev/null; then
		MODE="`echo "$SELECTOR" | sed 's:^/\?\([^/]*\)/\(.*\):\1:'`"
		SELECTOR="`echo "$SELECTOR" | sed 's:^/\?\([^/]*\)/\(.*\):\2:'`"
	fi
fi

if [ "$SERVER" = "" ]; then
	echo "Syntax error: $0 [gopher uri]" >&2
	exit 1
fi

# can be "-" for no escape sequences
[ "$LINK_COLOR" = ""  ] && LINK_COLOR=2

# Start and end link tags
SL=
EL=
if [ "$LINK_COLOR" != "-" ]; then
	SL="`tput setf $LINK_COLOR``tput setaf $LINK_COLOR`"
	EL="`tput init`"
	export LESS="${LESS}-R"
fi

# 'dialog' or text
if [ "$USE_DIALOG" = "" ]; then
	if dialog --help >/dev/null 2>&1; then
		USE_DIALOG=1
	else
		USE_DIALOG=0
	fi
fi

# Invert image viewer
if [ "$INVERT" = 1 ]; then
	INVERT="--invert"
else
	INVERT=
fi

# $0 [FILE] (dialog)
# Display a gopher menu for the given resource
cat_menu() {
	i=0
	cat "$1" | grep "^i\|^$PREFIX" | sed 's:\\:\\\\\\\\:g' | while read ln; do
		if echo "$ln" | grep "^i" >/dev/null 2>&1; then
			if [ "$2" != dialog ]; then
				echo "$ln" | sed "s:^.\([^\t]*\).*$:	\1:g"
			else
				echo "$ln" | sed 's:":'"''"':g;s:^.\([^\t]*\).*$:"      " "\1":g'
			fi
		elif echo "$ln" | grep "^$PREFIX" >/dev/null 2>&1; then
			i=`expr $i + 1`
			[ $i -le 9 ] && i=0$i 
			field="`echo "$ln" | cut -c1`"
			case "$field" in
				0) typ='TXT';;
				1) typ='DIR';; # menu, actually
				7) typ='(?)';; # query
				8) typ='TEL';; # TELnet (not TELephone)
				h) typ='WEB';; # HTML
				I) typ='IMG';;
				g) typ='GIF';;
				+) typ='SVR';; # redundant server
				*) typ='!!!';;
			esac
			if [ "$2" != dialog ]; then
				echo "$ln" | sed "s:^.\\([^\t]*\\).*:$typ $i	$SL\\1$EL:g"
			else
				echo "$ln" | sed "s:"'"'":'':g;s:^.\\([^\t]*\\).*:"'"'"$typ $i"'"'" "'"\1"'":g"
			fi
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
  rm -rf "$tmp" "$tmp.jpg" "$tmp.menu" "$tmp.choice"
}
trap finish EXIT

ok=true
if [ "$PROTOCOL" = gopher:// -o "$PROTOCOL" = "" ]; then
	echo "$SELECTOR" | nc "$SERVER" "$PORT" > "$tmp" || ok=false
else
	if wget -h >/dev/null 2>&1; then
		wget "${PROTOCOL}$SERVER:$PORT/$SELECTOR" -O "$tmp" >/dev/null 2>&1
	else
		curl "${PROTOCOL}$SERVER:$PORT/$SELECTOR" > "$tmp" 2>/dev/null
	fi
fi

if [ $ok = false ]; then
	echo Cannot contact gopher uri "[$URI]" >&2
	exit 2
fi

if [ $MODE = 1 ]; then
	sed --in-place 's:\r\n:\n:g;s:\r::g' "$tmp"
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
1|+)
	CHOICE=0
	while [ "$CHOICE" != "" ]; do
		if [ "$USE_DIALOG" = 1 ]; then
			> "$tmp.menu"
			cat_menu "$tmp" dialog | while read ln; do
				echo -n " $ln" >> "$tmp.menu"
			done
			[ "$LINES"   = "" ] && LINES=`tput lines`
			[ "$COLUMNS" = "" ] && COLUMNS=`tput cols`
			title="$SERVER: $SELECTOR"
			dialog	--extra-button --extra-label Back \
				--cancel-label Exit \
				--no-shadow \
				--menu "$title" \
				"$LINES" "$COLUMNS" "$LINES" \
				--file "$tmp.menu" 2>"$tmp.choice"
			val=$?
			clear
			
			if [ $val = 3 ]; then
				CHOICE=""
			elif [ $val = 1 ]; then
				CHOICE="q"
			else
				CHOICE="`cat "$tmp.choice" | cut -c5-`"
			fi
			
			rm "$tmp.menu"
			rm "$tmp.choice"
		else
			cat_menu "$tmp" | less
			read -p "[$SELECTOR]: " NEW_CHOICE
			if [ "$NEW_CHOICE" = p ]; then
				CHOICE=`expr "$CHOICE" - 1 2>/dev/null`
				if [ "$CHOICE" -lt 0 ]; then
					CHOICE=`cat "$tmp" | grep "^$PREFIX" | wc --lines`
				fi
			elif [ "$NEW_CHOICE" = n ]; then
				CHOICE=`expr "$CHOICE" + 1 2>/dev/null`
			else
				CHOICE="$NEW_CHOICE"
			fi
		fi
		
		[ "$CHOICE" = q ] && exit 255 # force-quit
		index="`expr 1 \* "$CHOICE" 2>/dev/null`"
		if [ "$index" != "" ]; then
			goto_server="`getsel "$tmp" $index 3`"
			goto_sel="`getsel "$tmp" $index 2`"
			goto_port="`getsel "$tmp" $index 4`"
			goto_mode="`getsel "$tmp" $index 1 | cut -c1`"
			echo "Digging to [$goto_server:$goto_port] $index [$goto_sel]..."
			sh "$0" "$goto_server:$goto_port/$goto_mode/$goto_sel"
			[ $? = 255 ] && exit 255 # force-quit
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
			$INVERT \
			--width=74 "$tmp" | less
	elif jp2a -h >/dev/null 2>&1; then
		if convert -h >/dev/null 2>&1; then
			convert "$tmp" "$tmp.jpg"
			jp2a --border --chars=" .-+=o8#"\
				$INVERT \
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

