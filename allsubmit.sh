#!/usr/bin/env bash

for fn in `ls output`
do
    cat output/$fn | python submit.py "all submission"
done
