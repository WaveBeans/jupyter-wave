OUTPUT_FILE=$1
VERSION=$2
ADDITIONAL_REPOSITORIES=$3

WAVEBEANS_VERSION=$(cat ../gradle.properties | grep wavebeansVersion  | sed -E "s/[^=]+=//")

echo "Generating library descriptor..."
echo "VERSION: '$VERSION'"
echo "WAVEBEANS_VERSION: '$WAVEBEANS_VERSION'"

cat jupyter-wave.json.tpl \
  | sed "s/\$VERSION/$VERSION/" \
  | sed "s/\$WAVEBEANS_VERSION/$WAVEBEANS_VERSION/" \
  | sed "s%\$ADDITIONAL_REPOSITORIES%$ADDITIONAL_REPOSITORIES%" \
  > "$OUTPUT_FILE"
