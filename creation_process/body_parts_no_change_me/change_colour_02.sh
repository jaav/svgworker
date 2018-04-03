#!/bin/bash
cd ./man
for file in ./*01.svg; do
	echo "man step 1 $file"
	sed '
	s/#944D09/#944D09/g
	s/#F8BD9C/#F2C478/g
	s/#FFD1AF/#FFDA99/g
	s/#DFA182/#D4984C/g
	s/#F3B190/#EAAD60/g' "$file" > "${file/_01.svg/_02.svg}"
done
cd ../woman
for file in ./*01.svg; do
	echo "woman step 1 $file"
	sed '
	s/#944D09/#944D09/g
	s/#F8BD9C/#F2C478/g
	s/#FFD1AF/#FFDA99/g
	s/#DFA182/#D4984C/g
	s/#F3B190/#EAAD60/g' "$file" > "${file/_01.svg/_02.svg}"
done