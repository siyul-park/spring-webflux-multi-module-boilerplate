#!/bin/sh

DIRECTORY=.

for i in $DIRECTORY/k6-tests/dist/*[^stress,soak].test.js; do
    k6 run $i
done
