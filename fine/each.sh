#!/bin/bash

NUM=`echo $1 | perl -pe 's/^.*(\d+).*$/\1/'`
echo $REVISION $COMMENT $BEGIN_DATE $NUM
ruby fine/main.rb . "$REVISION" "$COMMENT" "$BEGIN_DATE" "$NUM" || true
