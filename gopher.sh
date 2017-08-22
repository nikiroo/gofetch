#!/bin/bash

# Note: bash is required, sh will fail to output anything to the client
# Probably related to interactions beteween & and output redirections
# 
# It seems the timeout on netcat does not work when listening...
# So you will have to either kill the process or make a last connection
# (which will not be processed)

# $0 [prog] ([port])
# You can use the evironment variable $QUERY_STRING for the query

[ "$ADDR"    = "" ] && ADDR=127.0.0.1
[ "$LOG"     = "" ] && LOG=/tmp/gopher.service.err
[ "$PIDF"    = "" ] && PIDF="`mktemp`"
[ "$TIMEOUT" = "" ] && TIMEOUT=10
[ "$MAXCON"  = "" ] && MAXCON=1024

prog="$1"
port="$2"
defn="$3"
[ "$port"    = "" ] && port=70

if [ "$prog" = "" ]; then
	rm "$PIDF"
	echo "Syntax: $0 [prog] ([port]) (default non-7)" >&2
	echo '	You can use the evironment variable $QUERY_STRING for the query' >&2
	echo '	The default port is 70' >&2
	echo '	A default message for non-service (7) request can be given' >&2
	echo '	A filename will be displayed, you can delete it to stop the service' >&2
	echo '	(you still have to make a last connection on $port)' >&2
	exit 1
fi

tmpd=`mktemp -t -d .gopher-service.XXXXXX`
if [ ! -d "$tmpd" ]; then
	rm "$PIDF"
	echo "Cannot create temporary directory, aborting..." >&2
	exit 2
fi

echo "#/bin/sh
# PID: $$
# PID File (this file): $PIDF
# You can stop the service by deleting this file then making a last
# connection (which will not be processed) to the service:
rm \"$PIDF\"
nc -q0 $ADDR $port 2>/dev/null </dev/null
" > "$PIDF"

chmod u+rx "$PIDF"

echo "$PIDF"

i=0
while [ -e "$PIDF" ]; do
	i=`expr $i + 1`
	[ $i -gt "$MAXCON" ] && i=1
	fifo="$tmpd/fifo.$i"
	rm -f "$fifo" 2>/dev/null
	mkfifo "$fifo"
	< "$fifo" nc -l -q0 -w"$TIMEOUT" "$ADDR" -p "$port" | (
		if [ -e "$PIDF" ]; then
			read -r query
			selector="`echo "$query" | cut -f1 -d'	' | sed 's:[\r\n]::g'`"
			query="`echo "$query" | cut -f2 -d'	' | sed 's:[\r\n]::g'`"
			if [ "$query" = "" ]; then
				if [ "$defn" = "" ]; then
					echo "7Search	$selector	$ADDR	$port"
				else
					echo "$defn"
				fi
			else
				QUERY_STRING="$query" "$prog" 2>> "$LOG" &
			fi
		fi
	) > "$fifo"
done
rm -rf "$tmpd" 2>/dev/null
