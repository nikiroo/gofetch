#!/bin/sh

# default:
PREFIX=/usr/local
PROGS="java javac jar make sed"

valid=true
while [ "$*" != "" ]; do
	key=`echo "$1" | cut -f1 -d=`
	val=`echo "$1" | cut -f2 -d=`
	case "$key" in
	--)
	;;
	--help) #		This help message
		echo The following arguments can be used:
		cat "$0" | grep '^\s*--' | grep '#' | while read ln; do
			cmd=`echo "$ln" | cut -f1 -d')'`
			msg=`echo "$ln" | cut -f2 -d'#'`
			echo "	$cmd$msg"
		done
	;;
	--prefix) #=PATH	Change the prefix to the given path
		PREFIX="$val"
	;;
	*)
		echo "Unsupported parameter: '$1'" >&2
		echo >&2
		sh "$0" --help >&2
		valid=false
	;;
	esac
	shift
done

[ $valid = false ] && exit 1

MESS="A required program cannot be found:"
for prog in $PROGS; do
	out="`whereis -b "$prog" 2>/dev/null`"
	if [ "$out" = "$prog:" ]; then
		echo "$MESS $prog" >&2
		valid=false
	fi
done

[ $valid = false ] && exit 2

if [ "`whereis tput`" = "tput:" ]; then
	ok='"[ ok ]"';
	ko='"[ !! ]"';
	cols=80;
else
	ok='"`tput bold`[`tput setf 2` OK `tput init``tput bold`]`tput init`"';
	ko='"`tput bold`[`tput setf 4` !! `tput init``tput bold`]`tput init`"';
	cols='"`tput cols`"';
fi;

echo "MAIN = be/nikiroo/gofetch/Main" > Makefile
echo "MORE = " >> Makefile
echo "TEST = be/nikiroo/gofetch/test/Test" >> Makefile
echo "TEST_PARAMS = $cols $ok $ko" >> Makefile
echo "NAME = gofetch" >> Makefile
echo "PREFIX = $PREFIX" >> Makefile
echo "JAR_FLAGS += -C bin/ org -C bin/ be -C bin/ VERSION" >> Makefile
#echo "SJAR_FLAGS += -C src/ org -C src/ jexer -C src/ be -C ./ LICENSE -C ./ README.md -C ./ VERSION" >> Makefile

cat Makefile.base >> Makefile

