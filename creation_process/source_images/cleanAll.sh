#!/bin/bash
cd man
for file in ./*.svg; do
	echo "$file"
	#echo "${file/.svg/_changed.txt}" FOR RENAMING
	svgo --config=../config.yml --pretty --indent=2 -i "$file" -o ../../cleaned_source_images/man/"${file/.svg/_cleaned_temp_1.svg}"
done
cd ../woman
for file in ./*.svg; do
	echo "$file"
	#echo "${file/.svg/_changed.txt}" FOR RENAMING
	svgo --config=../config.yml --pretty --indent=2 -i "$file" -o ../../cleaned_source_images/woman/"${file/.svg/_cleaned_temp_1.svg}"
done