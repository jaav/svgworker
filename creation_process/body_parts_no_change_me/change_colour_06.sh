#!/bin/bash
cd ./man
for file in ./*01.svg; do
	echo "man step 1 $file"
	sed '
	s/#944D09/#371C03/g
	s/#F8BD9C/#66432C/g
	s/#FFD1AF/#825E3E/g
	s/#DFA182/#52311C/g
	s/#F3B190/#634029/g' "$file" > "${file/_01.svg/_06.svg}"
done
cd ../woman
for file in ./*01.svg; do
	echo "woman step 1 $file"
	sed '
	s/#944D09/#371C03/g
	s/#F8BD9C/#66432C/g
	s/#FFD1AF/#825E3E/g
	s/#DFA182/#52311C/g
	s/#F3B190/#634029/g' "$file" > "${file/_01.svg/_06.svg}"
done