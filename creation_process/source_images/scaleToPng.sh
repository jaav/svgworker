#!/bin/bash
cd ../cleaned_source_images/man
for file in ./*_cleaned.svg; do
	echo "exporting $file to man's folder"
	svgexport "$file" ../../png_scaled_images/man/"${file/_cleaned.svg/_fortina.png}" png 80% 9922:13000 992:1300

done
cd ../woman
for file in ./*_cleaned.svg; do
	echo "exporting $file to woman's folder"
	svgexport "$file" ../../png_scaled_images/woman/"${file/_cleaned.svg/_fortina.png}" png 80% 9922:13000 992:1300
done
cd ../../png_scaled_images/man
for file in ./*_fortina.png; do
	echo "converting _fortina to a _cropped_fortine.svg file"
	convert "${file}" -trim "${file/_fortina.png/_cropped_fortina.png}"
	echo "converting _cropped_fortina.png file to a .png file"
	convert "${file/_fortina.png/_cropped_fortina.png}" -resize '256x256>' -gravity center -background transparent -extent 264x264 "${file/_fortina.png/.png}"
done
cd ../woman
for file in ./*_fortina.png; do
	echo "$file"
	convert "${file}" -trim "${file/_fortina.svg/_cropped_fortina.png}"
	convert "${file/_fortina.svg/_cropped_fortina.png}" -resize '256x256>' -gravity center -background transparent -extent 264x264 "${file/_cropped_fortina.svg/.png}"
done

