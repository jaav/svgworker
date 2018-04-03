1. CLEANUP 
	svgo --config=config.yml --pretty --indent=2 -i Body_Woman_A1.svg -o ../cleaned_source_images/Body_Woman_A1_cleaned.svg
-> cleanAll.sh

2. REMOVE ADOBE STUFF
	sed '
	s/<[\/]*switch[\s]*>[\s]*/£££/g
	s/[\s]*<foreignObject.*$/£££/g' Body_Man_A1_cleaned.svg > Body_Man_A1_cleaned_NS.svg
	'
and
	sed -i '/^\s*£££/d' ./Body_Man_A1_cleaned_NS.svg
-> cleanAdobe.sh

3. PREPARE SVG FOR DYNAMIC COLOURING
	Run the svgworker over those files

4. SCALED PNG EXPORT
	svgexport Body_Woman_A1_cleaned.svg Body_Woman_A1_cleaned.png png 80% 9922:13000 992:1300

5. PDF EXPORT
	... is done on the new backentina server
	1. By CloudConvert OR
	2. By using Inkscape command



Skin colours
************

