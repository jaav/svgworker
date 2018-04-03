#!/bin/bash
cd ./man
for file in ./Hand_skin_0*.svg; do
	echo "man step 1 $file"
	sed -i.bak s/SVGID_/SSVVGGIIDD_/g "$file"
done
cd ../woman
for file in ./*01.svg; do
	echo "woman step 1 $file"
	sed -i.bak s/SVGID_/SSVVGGIIDD_/g "$file"
done