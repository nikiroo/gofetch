#!/bin/sh

# $0 [prog ($1 will be the query)] ([port])

[ "$ADDR"    = "" ] && ADDR=127.0.0.1
[ "$LOG"     = "" ] && LOG=/tmp/log.err
[ "$PIDF"    = "" ] && PIDF="`mktemp`"
[ "$TIMEOUT" = "" ] && TIMEOUT=10
[ "$MAXCON"  = "" ] && MAXCON=1024
prog="$1"
port="$2"
[ "$port"    = "" ] && port=70

if [ "$prog" = "" ]; then
	rm "$PIDF"
	echo "Syntax: $0 [prog (\$1 will be the query)] ([port])" >&2
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
	rm -f "$fifo"
	mkfifo "$fifo"
	< "$fifo" nc -l -q0 -w"$TIMEOUT" "$ADDR" -p "$port" | (
		read -r query
		query="`echo "$query" | cut -f2 -d'	'`"
		(
			"$prog" "$query" 2>> "$LOG"
			rm "$fifo"
		)&
	) > "$fifo"
done
rm -rf "$tmpd"

