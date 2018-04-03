#!/bin/bash
cd ./man
for file in ./*01.svg; do
	echo "man step 1 $file"
	sed '
	s/#944D09/#793F07/g
	s/#F8BD9C/#C27E54/g
	s/#FFD1AF/#DDA169/g
	s/#DFA182/#AD673B/g
	s/#F3B190/#BE7A50/g' "$file" > "${file/_01.svg/_04.svg}"
done
cd ../woman
for file in ./*01.svg; do
	echo "woman step 1 $file"
	sed '
	s/#944D09/#793F07/g
	s/#F8BD9C/#C27E54/g
	s/#FFD1AF/#DDA169/g
	s/#DFA182/#AD673B/g
	s/#F3B190/#BE7A50/g' "$file" > "${file/_01.svg/_04.svg}"
done