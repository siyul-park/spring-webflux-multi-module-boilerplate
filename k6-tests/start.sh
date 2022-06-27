#!/bin/sh

DIRECTORY=.

for i in $DIRECTORY/k6-tests/dist/*[^stress,soak].test.js; do
  filename=$(basename "$i")
  filename="${filename%.*}"
  k6 run -e OUTPUT=result-"$filename".json "$i"
done
