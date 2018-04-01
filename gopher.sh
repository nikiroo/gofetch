#!/bin/sh

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

# $0 [FILE]
cat_menu() {
	i=0
	cat "$1" | grep '^[i0-9]' | while read ln; do
		ln="`echo "$ln" | cut -f1`"
		if echo "$ln" | grep "^i" >/dev/null 2>&1; then
			echo "$ln" | sed "s:^.:	:g"
		elif echo "$ln" | grep "^[0-9]" >/dev/null 2>&1; then
			i=`expr $i + 1`
			i=`printf %2.f $i`
			field="`echo "$ln" | cut -c1`"
			case "$field" in
				0) typ='TXT';;
				1) typ='DIR';;
				7) typ='SRH';;
				8) typ='TEL';;
				*) typ='???';;
			esac
			echo "$ln" | sed "s:^.:$typ $i	:g"
		#else
			# Bad line
		fi
	done
}

# $0 [FILE]
choices() {
	i=0
	cat "$1" | grep '^[0-9]' | while read choice; do
		i=`expr $i + 1`
		i=`printf %2.f $i`
		
		echo "$i --> $choice"
	done
}

# $0 [FILE] [INDEX] [FIELD]
# Fields:
# 	1 = type/name
# 	2 = selector
# 	3 = server
# 	4 = port
getsel() {
	cat "$1" | grep '^[0-9]' | tail -n+"$2" | head -n 1 | cut -f"$3"
}

tmp="`mktemp gofetch.current_page.XXXXX`"
finish() {
  rm -rf "$tmp"
}
trap finish EXIT

if [ $MODE = 1 ]; then
	echo "$SELECTOR" | nc "$SERVER" "$PORT" | sed 's:\r::g' > "$tmp"
else
	echo "$SELECTOR" | nc "$SERVER" "$PORT" > "$tmp"
fi

case "$MODE" in
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
*)
	echo "unknwon selector mode: <$MODE>" | less
	exit 3
;;
esac
