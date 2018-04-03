#!/bin/bash
cd ./man
for file in ./*01.svg; do
	echo "man step 1 $file"
	sed '
	s/#944D09/#592E05/g
	s/#F8BD9C/#9A6442/g
	s/#FFD1AF/#B68456/g
	s/#DFA182/#86502D/g
	s/#F3B190/#97613F/g' "$file" > "${file/_01.svg/_05.svg}"
done
cd ../woman
for file in ./*01.svg; do
	echo "woman step 1 $file"
	sed '
	s/#944D09/#592E05/g
	s/#F8BD9C/#9A6442/g
	s/#FFD1AF/#B68456/g
	s/#DFA182/#86502D/g
	s/#F3B190/#97613F/g' "$file" > "${file/_01.svg/_05.svg}"
done