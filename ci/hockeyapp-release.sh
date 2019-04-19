#!/usr/bin/env bash

NOTIFY=2

echo "Uploading: $IPA_PATH"

RELEASE_NOTES=`cat ./ci/release_notes.txt`

curl \
-F status="2" \
-F notify="${NOTIFY}" \
-F notes="$RELEASE_NOTES" \
-F notes_type="1" \
-F strategy="replace" \
-F ipa="@$1" \
-H "X-HockeyAppToken: $HOCKEY_APP_TOKEN" \
https://rink.hockeyapp.net/api/2/apps/upload

echo "\nHockeyapp upload complete."