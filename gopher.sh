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
# 	When asked for a number, you can also enter 'n' or 'p' to get the 
# 	next or previous item (+1 or -1)

# ENV variables:
#	LINK_COLOR: escape sequences colour (def: 2)
#		- : means no escape sequence
# 		* : means bold
#		1 : means colour 1
#		2 : means colour 2
#		[...]
#	TITLE_COLOR: escape sequences colour (def: *) for titles (same as above)
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
PREFIX_G="\(=> \)"
PREFIX_GT="\(\#..\)"

PROTOCOL="`echo $URI | sed 's|^\([^:]*://\)\?\([^:/]*\)\(:[^/]*\)\?/\?\(.*\)\?|\1|g'`"
SERVER="`echo $URI | sed 's|^\([^:]*://\)\?\([^:/]*\)\(:[^/]*\)\?/\?\(.*\)\?|\2|g'`"
PORT="`echo $URI | sed 's|^\([^:]*://\)\?\([^:/]*\)\(:[^/]*\)\?/\?\(.*\)\?|\3|g'`"
SELECTOR="`echo $URI | sed 's|^\([^:]*://\)\?\([^:/]*\)\(:[^/]*\)\?/\?\(.*\)\?|\4|g'`"
MODE=

PORT="`echo "$PORT" | sed 's/^://'`"

# Defaults:
[ "$PROTOCOL" = "" ] && PROTOCOL=gopher://
if [ "$PORT" = "" ];then
	case "$PROTOCOL" in
	http://)   PORT=80 ;;
	https://)  PORT=443;;
	gemini://) PORT=1965;;
	gopher://) PORT=70 ;;
	esac
fi

if [ "$MODE" = "" ]; then
	# "" or dir-like selector? -> 1 ; if not -> 0 
	echo "$SELECTOR" | grep "/$" >/dev/null && MODE=1 || MODE=0
	echo "$SELECTOR" | grep "^0/" >/dev/null && MODE=0
	echo "$SELECTOR" | grep "^1/" >/dev/null && MODE=1
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
[ "$TITLE_COLOR" = ""  ] && TITLE_COLOR='*'

# Start and end link tags
SL=
EL=
if [ "$LINK_COLOR" != "-" ]; then
	if [ "$LINK_COLOR" = '*' ]; then
		ST="`tput smso`"
		ET="`tput rmso`"
	else
		SL="`tput setf $LINK_COLOR``tput setaf $LINK_COLOR`"
		EL="`tput sgr0``tput init`"
	fi
	export LESS="${LESS} -R"
fi
# Start and end title tags
ST=
ET=
if [ "$TITLE_COLOR" != "-" ]; then
	if [ "$TITLE_COLOR" = '*' ]; then
		ST="`tput smso`"
		ET="`tput rmso`"
	else
		ST="`tput setf $TITLE_COLOR``tput setaf $TITLE_COLOR`"
		ET="`tput sgr0``tput init`"
	fi
	export LESS="${LESS} -R"
fi

# Invert image viewer
if [ "$INVERT" = 1 ]; then
	INVERT="--invert"
else
	INVERT=
fi

# $0 [FILE]
# Display a gopher menu for the given resource
cat_menu() {
	i=0
	cat "$1" | sed 's:\\:\\\\\\\\:g' | while read ln; do
		if echo "$ln" | grep "^i" >/dev/null 2>&1; then
			echo "$ln" | sed "s:^.\([^\t]*\).*$:	\1:g"
		elif echo "$ln" | grep "^\($PREFIX\)\|\($PREFIX_G\)" >/dev/null 2>&1; then
			i=`expr $i + 1`
			[ $i -le 9 ] && i=0$i 
			field="`echo "$ln" | cut -c1`"
			field3="`echo "$ln" | cut -c1-3`"
			[ "$PROTOCOL" != gemini:// ] && field3=
			
			newln="`echo "$ln" | cut -c4-`"
			case "$field3" in
			'=> ')
				typ='LNK' # gemini link
				newln="`echo "$newln" | sed 's:\t: :g'`"
				f1="`echo "$newln" | cut -f1  -d' '`" # link
				f2="`echo "$newln" | cut -f2- -d ' '`" # detail
				[ "$f1" = "$f2" ] && f2= || f2=" ($f2)"
				
				echo "${SL}${typ}${EL} $i	${SL}${f1}${EL}${f2}"
			;;
			*)
				if [ "$PROTOCOL" = gopher:// ]; then
					newln="`echo "$ln" | cut -c2-`"
					newln="`echo "$newln" | sed "s:\\([^\t]*\\).*:\1:g"`"
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
					echo "${SL}${typ}${EL} $i	${SL}${newln}${EL}"
				else
					echo "$ln"
				fi
			;;
			esac
		elif echo "$ln" | grep "^$PREFIX_GT" >/dev/null 2>&1; then
			field3="`echo "$ln" | cut -c1-3`"
			case "$field3" in
				'#'??)
					echo "${ST}${ln}${ET}";;
				*)
					echo "$ln";;
			esac
		else
			echo "$ln"
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
	cat "$1" | grep "^$PREFIX\|\($PREFIX_G\)" | tail -n+"$2" | head -n 1 | cut -f"$3"
}

# Save page content to 'tmp' file
tmp="`mktemp -t gofetch.current_page.XXXXXX`"
finish() {
  rm -rf "$tmp" "$tmp.jpg" "$tmp.menu" "$tmp.choice"
}
trap finish EXIT

ok=true
if [ "$PROTOCOL" = gopher:// ]; then
	echo "$SELECTOR" | nc "$SERVER" "$PORT" > "$tmp" || ok=false
elif [ "$PROTOCOL" = gemini:// ]; then
	echo "${PROTOCOL}$SERVER:$PORT/$SELECTOR" \
		| openssl s_client -crlf -quiet -connect $SERVER:$PORT > "$tmp" 2>/dev/null
	sline="`cat "$tmp" | head -n1`"
	<"$tmp" tail -n +2  > "$tmp".2
	mv -f "$tmp".2 "$tmp"
	if [ "`echo "$sline" | cut -c1,4- | sed 's:/.*::g'`" = 2text ]; then
		MODE=1
	else
		MODE=0
	fi
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
		
		[ "$CHOICE" = q ] && exit 255 # force-quit
		index="`expr 1 \* "$CHOICE" 2>/dev/null`"
		if [ "$index" != "" ]; then
			if [ "$PROTOCOL" = gemini:// ]; then
				goto_gem="`getsel "$tmp" $index 1- | cut -c3-`"
				goto_gem="`echo "$goto_gem" | sed 's:^\s*::g;s:\s*$::g'`"
				goto_gem="`echo "$goto_gem" | cut -f1`"
				
				if ! echo "$goto_gem" | grep '.://' >/dev/null; then
					if ! echo "$goto_gem" | grep '^/' >/dev/null; then
						goto_gem="$SERVER:$PORT/$goto_gem"
					else
						goto_gem="$SERVER:$PORT/$SELECTOR/$goto_gem"
					fi
					goto_gem="gemini://$goto_gem"
				fi
				
				echo "Digging to [$goto_gem]..."
				
				sh "$0" "$goto_gem"
				[ $? = 255 ] && exit 255 # force-quit
			else
				goto_server="`getsel "$tmp" $index 3`"
				goto_sel="`getsel "$tmp" $index 2`"
				goto_port="`getsel "$tmp" $index 4`"
				goto_mode="`getsel "$tmp" $index 1 | cut -c1`"
				echo "Digging to $index [$goto_server:$goto_port] $goto_mode [$goto_sel]..."
				sh "$0" "$goto_server:$goto_port/$goto_mode/$goto_sel"
				[ $? = 255 ] && exit 255 # force-quit
			fi
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

