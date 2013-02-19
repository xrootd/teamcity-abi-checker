#!/bin/bash

VERSION=0.2
NAME=teamcity-abi-checker-$VERSION

ARCHIVE_DEST=$PWD
DEST=$ARCHIVE_DEST/$NAME
SRC=..

set -e

mkdir -p $DEST

cp -R $SRC/agent $DEST
cp -R $SRC/server $DEST
cp -R $SRC/common $DEST
cp -R $SRC/lib $DEST
cp -R $SRC/lib-compile $DEST
cp -R $SRC/build.properties $DEST
cp -r $SRC/*.xml $DEST

tar -cvzf $NAME.tar.gz $NAME
rm -rf $DEST