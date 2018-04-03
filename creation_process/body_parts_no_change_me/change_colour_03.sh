#!/bin/bash
cd ./man
for file in ./*01.svg; do
	echo "man step 1 $file"
	sed '
	s/#944D09/#944D09/g
	s/#F8BD9C/#E69664/g
	s/#FFD1AF/#FFB979/g
	s/#DFA182/#CF7B46/g
	s/#F3B190/#E0905E/g' "$file" > "${file/_01.svg/_03.svg}"
done
cd ../woman
for file in ./*01.svg; do
	echo "woman step 1 $file"
	sed '
	s/#944D09/#944D09/g
	s/#F8BD9C/#E69664/g
	s/#FFD1AF/#FFB979/g
	s/#DFA182/#CF7B46/g
	s/#F3B190/#E0905E/g' "$file" > "${file/_01.svg/_03.svg}"
done