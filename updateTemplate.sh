#!/usr/bin/env bash

FOLLOWUP_FILE=$1

if [ -z "$FOLLOWUP_FILE" ]
then
  echo "Usage:"
  SCRIPT_NAME=$(basename "$0")
  echo "./$SCRIPT_NAME FollowupNew.xlsm"
  exit 0
fi

function cleanup() {
  #9. And you're done.
  rm -rf "$TEMP_DIR"
  rm xpath.js
}
trap cleanup EXIT

wget -nv -nc http://gitlab/devops/xpath/raw/master/script/xpath.js
chmod +x xpath.js

#2. unzip the template
TEMP_DIR=$(mktemp -d XXX)
unzip "$FOLLOWUP_FILE" -d "$TEMP_DIR"

#1. Ensure 'From Jira' tab has no data at all
echo "Searching 'From Jira' worksheet relationship id"
REL_ID=$(./xpath.js "//sheet[@name='From Jira']/@id" "$TEMP_DIR/xl/workbook.xml")

if [ -z "$REL_ID" ]; then
  echo "Could not find 'From Jira' worksheet relationship id."
  exit 1
fi

echo "Searching worksheet xml file"
SHEET_ID=$(./xpath.js "//Relationship[@Id='$REL_ID']/@Target" "$TEMP_DIR/xl/_rels/workbook.xml.rels")

if [ -z "$SHEET_ID" ]; then
  echo "Could not find worksheet xml file."
  exit 1
fi

echo "Checking if worksheet is empty"
VALUES=$(./xpath.js "//sheetData/row[@r>1]/c/v/text()" "$TEMP_DIR/xl/$SHEET_ID")

if [ ! -z "$VALUES" ]; then
  echo "Input file has values and cannot be used."
  exit 1
fi

#3. run the following command:
xmllint --format "$TEMP_DIR/xl/worksheets/sheet7.xml" > sheet7-reformatted.xml

#4. open sheet7-reformatted.xml and copy the contents of tag <row r="1"..></row> over the same row on template src/main/resources/followup-template/sheet7-template.xml


#5. remove xl/worksheets/sheet7.xml and the reformatted file
rm -v "$TEMP_DIR/xl/$SHEET_ID"
rm -v sheet7-reformatted.xml

#6. copy the contents of ./xl/sharedStrings.xml and execute the following command:
cp "$TEMP_DIR/xl/sharedStrings.xml" ./sharedStrings.xml
xmllint --format sharedStrings.xml > ./src/main/resources/followup-template/sharedStrings-initial.xml

#7. remove sharedStrings.xml
rm ./sharedStrings.xml

#8. zip the contents again into ./src/main/resources/followup-template/Followup-template.xlsm
cd "$TEMP_DIR"
zip -FS -r ../src/main/resources/followup-template/Followup-template.xlsm .
cd ..
