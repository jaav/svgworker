#!/bin/bash
cd ../body_parts_no_change_me/man
for file in ./Body_skin_0*.svg; do
	echo "exporting ... $file"
	svgexport "$file" ./"${file/.svg/.png}" png 80% 9922:13000 992:1300

done
cd ../woman
for file in ./Body_skin_0*.svg; do
	echo "exporting ... $file"
	svgexport "$file" ./"${file/.svg/.png}" png 80% 9922:13000 992:1300
done
cd ../man
for file in ./*.png; do
	echo "converting step 1"
	convert "${file}" -trim "${file/Body_skin_0/man_cropped_0}"
	echo "converting step 2"
	convert "${file/Body_skin_0/man_cropped_0}" -resize '256x256>' -gravity center -background transparent -extent 264x264 "${file/Body_skin_0/man_0}"
done
cd ../woman
for file in ./*.png; do
	echo "converting step 1"
	convert "${file}" -trim "${file/Body_skin_0/woman_cropped_0}"
	echo "converting step 2"
	convert "${file/Body_skin_0/woman_cropped_0}" -resize '256x256>' -gravity center -background transparent -extent 264x264 "${file/Body_skin_0/woman_0}"
done

