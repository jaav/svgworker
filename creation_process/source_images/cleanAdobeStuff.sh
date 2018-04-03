#!/bin/bash
cd ../cleaned_source_images/man
for file in ./*_cleaned_temp_1.svg; do
	echo "man step 1 $file"
	sed '
	s/<[\/]*switch[\s]*>[\s]*/£££/g
	s/[\s]*<foreignObject.*$/£££/g' "$file" > "${file/_cleaned_temp_1.svg/_cleaned.svg}"
done
for file in ./*_cleaned.svg; do
	echo "man step 2 $file"
	sed -i '/^\s*£££/d' "${file}"
done
cd ../woman
for file in ./*_cleaned_temp_1.svg; do
	echo "woman step 1 $file"
	sed '
	s/<[\/]*switch[\s]*>[\s]*/£££/g
	s/[\s]*<foreignObject.*$/£££/g' "$file" > "${file/_cleaned_temp_1.svg/_cleaned.svg}"
done
for file in ./*_cleaned.svg; do
	echo "woman step 2 $file"
	sed -i '/^\s*£££/d' "${file}"
done