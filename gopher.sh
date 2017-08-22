#!/bin/bash

# Note: bash is required, sh will fail to output anything to the client
# Probably related to interactions beteween & and output redirections

# $0 [prog] ([port])
# You can use the evironment variable $QUERY_STRING for the query

[ "$ADDR"    = "" ] && ADDR=127.0.0.1
[ "$LOG"     = "" ] && LOG=/tmp/gopher.service.err
[ "$PIDF"    = "" ] && PIDF="`mktemp`"
[ "$TIMEOUT" = "" ] && TIMEOUT=10
[ "$MAXCON"  = "" ] && MAXCON=1024

prog="$1"
port="$2"
[ "$port"    = "" ] && port=70

if [ "$prog" = "" ]; then
	rm "$PIDF"
	echo "Syntax: $0 [prog] ([port])" >&2
	echo '	You can use the evironment variable $QUERY_STRING for the query' >&2
	echo '	The default port is 70' >&2
	echo '	A filename will be displayed, you can delete it to stop the service' >&2
	echo '	(with a grace time of $TIMEOUT seconds)' >&2
	exit 1
fi

tmpd=`mktemp -t -d .gopher-service.XXXXXX`
if [ ! -d "$tmpd" ]; then
	rm "$PIDF"
	echo "Cannot create temporary directory, aborting..." >&2
	exit 2
fi

touch "$PIDF"
echo "$PIDF"

i=0
while [ -e "$PIDF" ]; do
	i=`expr $i + 1`
	[ $i -gt "$MAXCON" ] && i=1
	fifo="$tmpd/fifo.$i"
	rm -f "$fifo" 2>/dev/null
	mkfifo "$fifo"
	< "$fifo" nc -l -q0 -w"$TIMEOUT" "$ADDR" -p "$port" | (
		read -r query
		query="`echo "$query" | cut -f2 -d'	' | sed 's:[\r\n]::g'`"
		QUERY_STRING="$query" "$prog" 2>> "$LOG" &
	) > "$fifo"
done
rm -rf "$tmpd" 2>/dev/null
