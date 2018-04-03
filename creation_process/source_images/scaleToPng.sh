#!/bin/bash
cd ../cleaned_source_images/man
for file in ./*_cleaned_NS.svg; do
	echo "$file"
	#echo "${file/.svg/_changed.txt}" FOR RENAMING
	svgexport "$file" "${file/_cleaned_NS.svg/_fortina.png}" png 80% 9922:13000 992:1300
	convert "${file/_cleaned_NS.svg/_fortina.png}" -trim "${file/_cleaned_NS.svg/_cropped_fortina.png}"
	convert "${file/_cleaned_NS.svg/_cropped_fortina.png}" -resize '256x256>' -gravity center -background transparent -extent 264x264 "${file/_cleaned_NS.svg/_cropped_resized_fortina.png}"

done
cd ../woman
for file in ./*_cleaned_NS.svg; do
	echo "$file"
	svgexport "$file" "${file/_cleaned_NS.svg/_fortina.png}" png 80% 9922:13000 992:1300
	convert "${file/_cleaned_NS.svg/_fortina.png}" -trim "${file/_cleaned_NS.svg/_cropped_fortina.png}"
	convert "${file/_cleaned_NS.svg/_cropped_fortina.png}" -resize '256x256>' -gravity center -background transparent -extent 264x264 "${file/_cleaned_NS.svg/_cropped_resized_fortina.png}"
done